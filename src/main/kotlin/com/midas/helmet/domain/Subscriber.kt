package com.midas.helmet.domain

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.midas.helmet.configuration.ApplicationProperties
import com.midas.helmet.domain.value.EmailAddress
import com.midas.helmet.services.email.EmailService
import com.midas.helmet.services.LoggingService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.util.*

class Subscriber {
    @Component
    private class SpringAdapter(
        private val applicationProperties: ApplicationProperties,
        private val emailService: EmailService,
        private val loggingService: LoggingService
    ) {
        @PostConstruct
        fun init() {
            Subscriber.applicationProperties = applicationProperties
            Subscriber.loggingService        = loggingService
            Subscriber.emailService          = emailService
            dynamoDb                         = AmazonDynamoDBAsyncClientBuilder.standard().build()
            tableName                        = "Subscriber-"+ applicationProperties.environment
        }
    }
    companion object {
        private lateinit var applicationProperties: ApplicationProperties
        private lateinit var loggingService: LoggingService
        private lateinit var emailService: EmailService
        private lateinit var dynamoDb: AmazonDynamoDB
        private val usedTokens: MutableSet<String> = HashSet()
        private val recentSubscribedEmails: MutableSet<String> = HashSet()
        private lateinit var tableName: String
        fun sendConfirmationEmail(email: EmailAddress, timeZoneOffset: String) {
            if (recentSubscribedEmails.contains(email.toString())) {
                loggingService.log("User attempting to resubscribe...")
                return
            }
            recentSubscribedEmails.add(email.toString())

            val token = createSubscriber(
                email          = email.toString(),
                timeZoneOffset = timeZoneOffset
            )
            if(token == null) {
                loggingService.log("User already has a DynamoDB record and is trying to resubscribe...")
                return
            }

            emailService.send(
                toAddress = email,
                subject   = "Confirm your email address",
                body      = createConfirmationEmailBody(token = token)
            )
        }

        fun confirmEmailAddress(token: String) : Boolean {
            if (usedTokens.contains(token)) {
                loggingService.log("User clicking the same confirmation link")
                return false
            }
            usedTokens.add(token)

            return confirmSubscriber(confirmationToken = token)
        }

        private fun createConfirmationEmailBody(token: String) : String {
            return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head></head><body> To confirm your email address<a href=\"${applicationProperties.rootUrl}/confirm-email/${URLEncoder.encode(token,"UTF-8")}\"> click here</a></body></html>"
        }

        private fun createSubscriber(email: String, timeZoneOffset: String) : String? {
            /**See if there is already a subscriber record for this email **/
            if(checkIfEmailAlreadyExists(email)) {
                return null
            }

            val token = UUID.randomUUID().toString().replace("-","")
            val m: MutableMap<String, AttributeValue> = mutableMapOf()
            m["EmailAddress"]        = AttributeValue(email)
            m["Confirmed"]           = AttributeValue().withBOOL(false)
            m["ConfirmationToken"]   = AttributeValue(token)
            m["TimeZoneOffset"]      = AttributeValue(timeZoneOffset)
            m["LastUpdateTimestamp"] = AttributeValue().withN("${System.currentTimeMillis()}")

            dynamoDb.putItem(tableName, m)

            return token
        }

        /** Able to use 'Query' because thiis 'query' uses the primary key (at least the partition portion)**/
        private fun checkIfEmailAlreadyExists(email: String) : Boolean {
            val result = dynamoDb.query(
                QueryRequest().withTableName(tableName).
                withExpressionAttributeValues(mapOf(":email" to AttributeValue(email))).
                withKeyConditionExpression("EmailAddress = :email")
            )
            return result.count != 0
        }

        private fun confirmSubscriber(confirmationToken: String) : Boolean {
            usedTokens.add(confirmationToken)

            val emailAddress = getEmailFromToken(confirmationToken)
            if(emailAddress == null) {
                loggingService.log("User supplied a bad token")
                return false
            }

            val itemKey = mutableMapOf<String, AttributeValue>()
            itemKey["ConfirmationToken"] = AttributeValue().withS(confirmationToken)
            itemKey["EmailAddress"]      = AttributeValue().withS(emailAddress)

            val updatedValues = mutableMapOf<String, AttributeValueUpdate>()
            updatedValues["Confirmed"] = AttributeValueUpdate(
                AttributeValue().withBOOL(true),
                AttributeAction.PUT
            )
            updatedValues["LastUpdateTimestamp"] = AttributeValueUpdate(
                AttributeValue().withN("${System.currentTimeMillis()}"),
                AttributeAction.PUT
            )

            dynamoDb.updateItem(
                UpdateItemRequest(
                    "Subscriber-"+ applicationProperties.environment,
                    itemKey,
                    updatedValues
                )
            )
            return true
        }

        /** Apparently you need to do a scan for non-primary key 'queries'. If using the primary key than you can 'Query' **/
        private fun getEmailFromToken(token: String) : String? {
            val result = dynamoDb.scan(
                ScanRequest().withTableName(tableName).
                withExpressionAttributeValues(
                    mapOf(":token" to AttributeValue(token))
                ).withIndexName("Token-Index").withFilterExpression("ConfirmationToken = :token")
            )
            if(result.count == 0) return null

            return result.items[0]["EmailAddress"]!!.s
        }
    }
}