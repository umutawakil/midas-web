package com.midas.helmet.controllers

import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.midas.helmet.services.LoggingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.servlet.resource.NoResourceFoundException


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
        request: HttpServletRequest?,
        response: HttpServletResponse
    ) {
        if ((ex !is NoResourceFoundException) && (ex !is HttpRequestMethodNotSupportedException)) {
            loggingService.log(
                message = "Unhandled error occurred: (HTTP: ${response.status})",
                ex      = ex
            )
        }
        response.sendRedirect("/error")
    }


}