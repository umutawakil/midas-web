package com.midas.helmet.utilities

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.midas.helmet.services.LoggingService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.concurrent.Future

/**
 * Created by Usman Mutawakil on 10/26/22.
 */
@Service
class WorkerUtils {

    @Component
    class SpringAdapter(
        @Autowired private val loggingService: LoggingService
    ) {
        @PostConstruct
        fun init() {
            WorkerUtils.loggingService = loggingService
        }

    }
    companion object {
        private lateinit var loggingService: LoggingService
        fun buildAmazonSQSClientBuilder(awsUploadAccessKey: String, awsUploadSecretKey: String) : AmazonSQSAsyncClientBuilder {
            println("Building SQS client....")
            return AmazonSQSAsyncClient.asyncBuilder().withCredentials(
                AWSStaticCredentialsProvider(
                    BasicAWSCredentials(
                        awsUploadAccessKey, awsUploadSecretKey
                    )
                )
            )
        }

        //TODO: Why is this not being used in place of a custom read operation in emailService?
        fun readFromQueue(
            queryUrl: String,
            sqsClient: AmazonSQSAsync,
            maxAudioQueueBatchSize: Int,
            forEachMessage: (inputMessage: Message) -> Unit
        ) {
            val result: Future<ReceiveMessageResult> = getMessages(
                queryUrl          = queryUrl,
                sqsClient         = sqsClient,
                maxQueueBatchSize = maxAudioQueueBatchSize
            )
            val receiveMessageResult: ReceiveMessageResult = result.get()

            for (m: Message in receiveMessageResult.messages) {
                try {
                    forEachMessage(m)
                    sqsClient.deleteMessageAsync(queryUrl, m.receiptHandle)

                } catch (exception: Exception) {
                    loggingService.log("Exception reading from queue. Message body: ${m.body}", exception)
                    sqsClient.deleteMessageAsync(queryUrl, m.receiptHandle)
                }
            }
        }

        private fun getMessages(queryUrl: String, sqsClient: AmazonSQSAsync, maxQueueBatchSize: Int) : Future<ReceiveMessageResult> {
            return sqsClient.receiveMessageAsync(
                ReceiveMessageRequest().
                withQueueUrl(queryUrl).
                withMaxNumberOfMessages(maxQueueBatchSize)
            )
        }

        fun runInLoop(threadName: String, threadSleepMillis: Long, work: ()-> Unit) {
            val thread = Thread {
                var lastTime: Long
                while (true) {
                    lastTime = System.currentTimeMillis()
                    try {
                        work()
                    } catch (exception: Exception) {
                        loggingService.log("Error in loop $threadName", exception)
                    }
                    var elapsed = System.currentTimeMillis() - lastTime
                    if (elapsed < threadSleepMillis) {
                        val diff = threadSleepMillis - elapsed
                        Thread.sleep(diff)
                    }
                }
            }
            thread.name = threadName
            thread.start()
        }
    }
}