package com.midas.helmet.controllers

import com.midas.helmet.services.LoggingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestHeader


@ControllerAdvice
class HelmetControllerAdvice(
    @Autowired private val loggingService: LoggingService
) {
    @ModelAttribute("isNotMobile")
    fun isNotMobile(@RequestHeader("user-agent") input: String):Boolean {
        val userAgent = input.lowercase()
        val mobileKeyWords = setOf(
            "mobile",
            "tablet",
            "ios",
            "iphone",
            "ipad",
            "tablet",
            "android"
        )
        for(m in mobileKeyWords) {
            if (userAgent.contains(m)) {
                return false
            }
        }
        return true
    }

    @ExceptionHandler(Exception::class)
    fun handle(
        ex: Exception,
        request: HttpServletRequest?, response: HttpServletResponse
    ): ResponseEntity<Any?>? {
        loggingService.log(message="Unhandled error occurred", ex = ex )

        return if (ex is NullPointerException) {
            ResponseEntity<Any?>(HttpStatus.BAD_REQUEST)
        } else ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<Any>()
    }


}