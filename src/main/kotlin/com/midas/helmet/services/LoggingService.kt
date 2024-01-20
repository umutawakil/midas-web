package com.midas.helmet.services

import com.amazonaws.services.simpleemail.model.*
import com.midas.helmet.configuration.ApplicationProperties
import com.midas.helmet.services.email.EmailClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LoggingService (
    @Autowired private val emailClient: EmailClient,
    @Autowired private val applicationProperties: ApplicationProperties
){
    private val errorMap: MutableSet<String> = HashSet()
    fun log(message: String) {
        println(message)
    }

    fun log(message: String, ex: Exception) {
        println(message)
        ex.printStackTrace()
        if(errorMap.contains(message)) {
            return
        } else {
            errorMap.add(message)
            emailClient.send(
                SendEmailRequest(
                    "Bloip <${applicationProperties.serviceEmailAddress}>",
                    Destination(listOf(applicationProperties.adminEmailAddress)),
                    Message(
                        Content(message).withCharset(Charsets.UTF_8.name()),
                        Body().withText(Content(ex.message))
                    )
                )
            )
        }
    }
}