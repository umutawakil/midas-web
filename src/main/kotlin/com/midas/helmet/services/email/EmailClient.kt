package com.midas.helmet.services.email

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.midas.helmet.configuration.ApplicationProperties
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class EmailClient(
    @Autowired private val applicationProperties: ApplicationProperties,
) {
    private lateinit var sesClient: AmazonSimpleEmailServiceAsync

    @PostConstruct
    fun init() {
        /*sesClient = AmazonSimpleEmailServiceAsyncClientBuilder.standard().
        withCredentials(
            AWSStaticCredentialsProvider(
                BasicAWSCredentials(
                    System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_SECRET_ACCESS_KEY")
                )
            )
        ).build()*/
        sesClient = AmazonSimpleEmailServiceAsyncClientBuilder.standard().build()
    }

    fun send(sendEmailRequest: SendEmailRequest) {
        sesClient.sendEmail(sendEmailRequest)
    }
}