package com.midas.helmet

import com.midas.helmet.configuration.ApplicationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(ApplicationProperties::class)
@SpringBootApplication
class HelmetApplication

fun main(args: Array<String>) {
	runApplication<HelmetApplication>(*args)
}
