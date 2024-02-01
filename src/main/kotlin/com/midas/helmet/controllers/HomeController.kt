package com.midas.helmet.controllers

import com.midas.helmet.configuration.ApplicationProperties
import com.midas.helmet.domain.StockInfo
import com.midas.helmet.domain.Subscriber
import com.midas.helmet.domain.UnsupportedTicker
import com.midas.helmet.domain.value.EmailAddress
import com.midas.helmet.services.LoggingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.math.abs

@Controller
class HomeController(
    @Autowired private val applicationProperties: ApplicationProperties,
    @Autowired private val loggingService: LoggingService
) {
    @GetMapping("/")
    fun index(
        model: Model,
        @RequestParam(name="start", required = false) s: Int?,
        @RequestParam(name="profitability", required = false) p: Boolean?,
        @RequestParam(name="order_descending", required = false) o: Boolean?,
        @RequestParam(name="time_window", required = false)  t: Int?,
        @RequestParam(name="volatility_limit", required = false)  v: Int?
    ): String {
        val start           = s?: 0
        val profitability   = p ?: false
        val orderDescending = o ?: true
        val timeWindow      = t ?: applicationProperties.defaultTimeWindow
        val volatilityLimit = v ?: applicationProperties.defaultVolatilityLimit

        val results: List<StockInfo.StockInfoDto> = if(profitability) {
            StockInfo.queryProfitableStocks(
                start           = start,
                size            = applicationProperties.standardResultsPerPage + 1,
                timeWindow      = timeWindow,
                min             = (-1.0)*abs(volatilityLimit),
                max             = 1.0*abs(volatilityLimit),
                orderDescending = orderDescending
            )
        } else {
            StockInfo.queryAllStocks(
                start           = start,
                size            = applicationProperties.standardResultsPerPage + 1,
                timeWindow      = timeWindow,
                min             = (-1.0)*abs(volatilityLimit),
                max             = 1.0*abs(volatilityLimit),
                orderDescending = orderDescending
            )
        }

        /** TODO: Needs unit tests -> Pagination caveats....**/
        if(results.size == applicationProperties.standardResultsPerPage + 1) {
            model["stocks"] = results.subList(0,results.size - 1)
            model["next"]   = start + applicationProperties.standardResultsPerPage
        }  else {
            model["stocks"] = results
        }
        if(start > 0) {
            model["back"] = start - applicationProperties.standardResultsPerPage
        }

        model["timeWindow"]      = timeWindow
        model["volatilityLimit"] = volatilityLimit
        model["profitability"]   = if(profitability) { 1 } else { 0}
        model["orderDescending"] = if(orderDescending) { 1 } else { 0}

        return "index"
    }

    @GetMapping("/ticker/{t}")
    fun getTickerInfo(
        model: Model,
        @PathVariable(name="t", required = true) ticker: String
    ): String {
        val stocks            = StockInfo.queryTickerWindows(ticker)
        model["stocks"]       = stocks
        model["unsupported"]  = if (UnsupportedTicker.isNotSupported(ticker)) { true } else { null }
        model["notFound"]     = if (stocks.isEmpty()) { true } else { null }
        model["ticker"]       = ticker

        return "ticker"
    }

    @PostMapping("/newsletter")
    @ResponseBody
    fun subscribe(
        model: Model,
        @RequestParam(name="email", required = true) email: String,
        @RequestParam(name="time_zone_offset", required = true) timeZoneOffset: String
    ): String {
        Subscriber.sendConfirmationEmail(
            email          = EmailAddress(email),
            timeZoneOffset = timeZoneOffset
        )
        return "1"
    }

    @GetMapping("/confirm-email/{token}")
    @ResponseBody
    fun confirmEmailAddress(
        model: Model,
        @PathVariable(name="token", required = true) token: String
    ): String {
        if(Subscriber.confirmEmailAddress(token = token)) {
            return "Email address confirmed!"
        }
        return "Invalid or expired link."
    }
}