package com.midas.helmet.controllers

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestHeader

@ControllerAdvice
class HelmetControllerAdvice {
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
}