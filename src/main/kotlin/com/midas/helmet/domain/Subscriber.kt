package com.midas.helmet.domain

import com.midas.helmet.configuration.ApplicationProperties
import com.midas.helmet.domain.value.EmailAddress
import com.midas.helmet.repositories.SubscriberRepository
import com.midas.helmet.services.email.EmailService
import com.midas.helmet.services.LoggingService
import jakarta.annotation.PostConstruct
import jakarta.persistence.*
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.util.*

@Entity
@Table(name="subscriber", schema = "helmet")
class Subscriber {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id   : Long = -1L
    private val email: EmailAddress
    private var confirmed = false
    private val timeZoneOffset: String
    private val confirmationToken: String

    constructor(email: EmailAddress, timeZoneOffset: String) {
        this.email             = email
        this.timeZoneOffset    = timeZoneOffset
        this.confirmationToken = UUID.randomUUID().toString().replace("-","")
    }

    @Component
    private class SpringAdapter(
        private val applicationProperties: ApplicationProperties,
        private val subscriberRepository: SubscriberRepository,
        private val emailService: EmailService,
        private val loggingService: LoggingService
    ) {
        @PostConstruct
        fun init() {
            Subscriber.applicationProperties = applicationProperties
            Subscriber.loggingService        = loggingService
            Subscriber.subscriberRepository  = subscriberRepository
            Subscriber.emailService          = emailService

            for (s in subscriberRepository.findAll()) {
                subscribersByEmail[s.email] = s
                subscribersByToken[s.confirmationToken] = s
            }
        }
    }
    companion object {
        private lateinit var applicationProperties: ApplicationProperties
        private lateinit var loggingService: LoggingService
        private lateinit var subscriberRepository: SubscriberRepository
        private lateinit var emailService: EmailService

        private val subscribersByEmail: MutableMap<EmailAddress, Subscriber> = HashMap()
        private val subscribersByToken: MutableMap<String, Subscriber> = HashMap()
        fun sendConfirmationEmail(email: EmailAddress, timeZoneOffset: String) {
            if (subscribersByEmail.containsKey(email)) {
                loggingService.log("User attempting to resubscribe...")
                return
            }

            val s = save(
                Subscriber(
                    email          = email,
                    timeZoneOffset = timeZoneOffset)
            )

            emailService.send(
                toAddress = email,
                subject   = "Confirm your email address",
                body      = createConfirmationEmailBody(s)
            )
        }

        fun confirmEmailAddress(token: String) : Boolean {
            val s: Subscriber = subscribersByToken[token] ?: return false
            s.confirmed = true
            save(s)
            return true
        }

        private fun createConfirmationEmailBody(s: Subscriber) : String {
            return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head></head><body> To confirm your email address<a href=\"${applicationProperties.rootUrl}/confirm-email/${URLEncoder.encode(s.confirmationToken,"UTF-8")}\"> click here</a></body></html>"
        }

        private fun save(s: Subscriber) : Subscriber {
            val ns                                   = subscriberRepository.save(s)
            subscribersByToken[ns.confirmationToken] = ns
            subscribersByEmail[ns.email]             = ns

            return ns
        }
    }
}