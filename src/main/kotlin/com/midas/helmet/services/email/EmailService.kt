package com.midas.helmet.services.email

import com.amazonaws.services.simpleemail.model.*
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.midas.helmet.configuration.ApplicationProperties
import com.midas.helmet.domain.value.EmailAddress
import com.midas.helmet.services.LoggingService
import com.midas.helmet.utilities.WorkerUtils
import jakarta.annotation.PostConstruct
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.*

@Service
class EmailService(
    @Autowired private val applicationProperties: ApplicationProperties,
    @Autowired private val loggingService: LoggingService,
    @Autowired private val emailClient: EmailClient,
    @Autowired private val workerUtilsAdapter: WorkerUtils.SpringAdapter
) {
    private lateinit var sqsClient: AmazonSQSAsync

    private val executorService: ExecutorService = ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue<Runnable>()
    )

    @PostConstruct
    fun init() {
        workerUtilsAdapter.init()

        sqsClient = WorkerUtils.buildAmazonSQSClientBuilder(
            applicationProperties.awsAccessKey,
            applicationProperties.awsSecretKey
        ).build()

        WorkerUtils.runInLoop(
            threadName        = "EmailService",
            threadSleepMillis = applicationProperties.emailQueuePollInterval
        ) {
            readFromQueueAndSendEmails()
        }
    }

    fun send(toAddress: EmailAddress, subject: String, body: String) {
        executorService.execute {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("to", toAddress.value)
                jsonObject.put("subject", subject)
                jsonObject.put("body", body)

                val sqsMessage = SendMessageRequest()
                    .withQueueUrl(applicationProperties.emailQueueUrl)
                    .withMessageBody(jsonObject.toString())
                    .withMessageGroupId("email-" + applicationProperties.environment)
                sqsClient.sendMessage(sqsMessage)

            } catch (exception: Exception) {
                loggingService.log(message = "Exception in email enqueuing", ex = exception)
            }
        }
    }

    private fun sendHelper(toAddress: EmailAddress, subject: String, body: String) {
        emailClient.send(
            SendEmailRequest(
                "Bloip <${applicationProperties.serviceEmailAddress}>",
                Destination(listOf(toAddress.value)),
                Message(
                    Content(subject).withCharset(Charsets.UTF_8.name()),
                    Body().withHtml(Content(body).withCharset(Charsets.UTF_8.name()))
                )
            )
        )
    }

    private fun readFromQueueAndSendEmails() {
        val messageResult: Future<ReceiveMessageResult> = sqsClient.receiveMessageAsync(
            ReceiveMessageRequest().
            withQueueUrl(applicationProperties.emailQueueUrl).
            withMaxNumberOfMessages(1)
        )
        val receiveMessageResult: ReceiveMessageResult = messageResult.get()
        for (m in receiveMessageResult.messages) {
           Thread.sleep(applicationProperties.delayBetweenEmailSend) //TODO: At the time of this comment its 1000 (ms)

           lateinit var jsonObject: JSONObject
            try {
                jsonObject = (JSONParser().parse(m.body)) as JSONObject
                sendHelper(
                    toAddress = EmailAddress(jsonObject.get("to") as String),
                    subject   = jsonObject.get("subject") as String,
                    body      = jsonObject.get("body") as String
                )
            } catch(e: Exception) {
                loggingService.log("Error reading sending email from queue. JSON: $jsonObject", e)
            }
            sqsClient.deleteMessageAsync(applicationProperties.emailQueueUrl, m.receiptHandle)
        }
    }
}