package com.midas.helmet.services

import com.amazonaws.services.simpleemail.model.*
import com.midas.helmet.configuration.ApplicationProperties
import com.midas.helmet.services.email.EmailClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LoggingService (
    @Autowired private val emailClient: EmailClient,
    @Autowired private val applicationProperties: ApplicationProperties
){
    private val logger: Logger = LoggerFactory.getLogger(LoggingService::class.java);
    private val errorMap: MutableSet<String> = HashSet()
    fun log(message: String) {
        logger.info(message)
    }

    fun log(message: String, ex: Exception) {
        logger.error(message, ex)

        if(errorMap.contains(message)) {
            return
        } else {
            errorMap.add(message)
            val emailRequest = SendEmailRequest(
                "Bloip <${applicationProperties.serviceEmailAddress}>",
                Destination(listOf(applicationProperties.adminEmailAddress)),
                Message(
                    Content(message).withCharset(Charsets.UTF_8.name()),
                    Body().withText(Content(ex.message))
                )
            )
            logger.info(emailRequest.toString())
            emailClient.send(
                emailRequest
            )
        }
    }
}