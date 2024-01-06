package com.midas.helmet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HelmetApplication

fun main(args: Array<String>) {
	runApplication<HelmetApplication>(*args)
}
