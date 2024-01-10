package com.midas.helmet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
class ApplicationProperties {
    val standardResultsPerPage: Int
    val defaultVolatilityLimit: Int
    val defaultTimeWindow: Int
    constructor(
        standardResultsPerPage: Int,
        defaultVolatilityLimit: Int,
        defaultTimeWindow: Int
    ) {
        this.standardResultsPerPage = standardResultsPerPage
        this.defaultVolatilityLimit = defaultVolatilityLimit
        this.defaultTimeWindow      = defaultTimeWindow
    }

}