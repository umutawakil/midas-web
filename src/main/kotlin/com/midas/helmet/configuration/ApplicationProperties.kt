package com.midas.helmet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
class ApplicationProperties {
    val rootUrl: String
    val standardResultsPerPage: Int
    val defaultVolatilityLimit: Int
    val defaultTimeWindow: Int
    
    val awsAccessKey: String
    val awsSecretKey: String
    val emailQueuePollInterval: Long
    val delayBetweenEmailSend: Long
    val emailQueueUrl: String
    val environment: String
    val serviceEmailAddress: String
    val adminEmailAddress: String
    constructor(
        rootUrl: String,
        standardResultsPerPage: Int,
        defaultVolatilityLimit: Int,
        defaultTimeWindow: Int,

         awsAccessKey: String,
         awsSecretKey: String,
         emailQueuePollInterval: Long,
         delayBetweenEmailSend: Long,
         emailQueueUrl: String,
         environment: String,
         serviceEmailAddress: String,
         adminEmailAddress: String
    ) {
        this.rootUrl                = rootUrl
        this.standardResultsPerPage = standardResultsPerPage
        this.defaultVolatilityLimit = defaultVolatilityLimit
        this.defaultTimeWindow      = defaultTimeWindow

        this.awsAccessKey           = awsAccessKey
        this.awsSecretKey           = awsSecretKey
        this.emailQueuePollInterval = emailQueuePollInterval
        this.delayBetweenEmailSend  = delayBetweenEmailSend
        this.emailQueueUrl          = emailQueueUrl
        this.environment            = environment
        this.serviceEmailAddress    = serviceEmailAddress
        this.adminEmailAddress      = adminEmailAddress
    }

}