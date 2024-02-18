package com.midas.helmet.controllers

import com.midas.helmet.configuration.ApplicationProperties
import com.midas.helmet.domain.StockInfo
import com.midas.helmet.domain.Subscriber
import com.midas.helmet.domain.UnsupportedTicker
import com.midas.helmet.domain.value.EmailAddress
import com.midas.helmet.services.LoggingService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
        @RequestParam(name="ticker", required = false) ticker: String?,
        @RequestParam(name="start", required = false) s: Int?,
        @RequestParam(name="profitability", required = false) p: Boolean?,
        @RequestParam(name="healthcare_bio", required = false) h: Boolean?,
        @RequestParam(name="order_descending", required = false) o: Boolean?,
        @RequestParam(name="time_window", required = false)  t: Int?,
        @RequestParam(name="volatility_limit", required = false)  v: Int?
    ): String {
        loggingService.log("Homepage requested")
        if((ticker != null) && ticker.isNotEmpty()) { /** TODO: This hack exists because the same form for search/filter is used to get a specific ticker **/
            return "redirect:/ticker/$ticker"
        }

        val start             = s?: 0
        val profitability     = p ?: false
        val healthcareBiotech = h ?: false
        val orderDescending   = o ?: true
        val timeWindow        = t ?: applicationProperties.defaultTimeWindow
        val volatilityLimit   = v ?: applicationProperties.defaultVolatilityLimit

        val results: List<StockInfo.StockInfoDto> = if(profitability) {
            StockInfo.queryProfitableStocks(
                start             = start,
                size              = applicationProperties.standardResultsPerPage + 1,
                timeWindow        = timeWindow,
                min               = (-1.0)*abs(volatilityLimit),
                max               = 1.0*abs(volatilityLimit),
                orderDescending   = orderDescending,
                healthcareBiotech = healthcareBiotech
            )
        } else {
            StockInfo.queryAllStocks(
                start             = start,
                size              = applicationProperties.standardResultsPerPage + 1,
                timeWindow        = timeWindow,
                min               = (-1.0)*abs(volatilityLimit),
                max               = 1.0*abs(volatilityLimit),
                orderDescending   = orderDescending,
                healthcareBiotech = healthcareBiotech
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

        model["timeWindow"]        = timeWindow
        model["volatilityLimit"]   = volatilityLimit
        model["profitability"]     = if(profitability) { 1 } else { 0}
        model["orderDescending"]   = if(orderDescending) { 1 } else { 0}
        model["healthcareBiotech"] = if(healthcareBiotech) { 1 } else { 0}

        return "index"
    }

    @GetMapping("/ticker/{t}")
    fun getTickerInfo(
        model: Model,
        @PathVariable(name="t", required = true) ticker: String
    ): String {
        loggingService.log("Ticker info requested")
        val stocks            = StockInfo.queryTickerWindows(ticker)
        model["stocks"]       = stocks
        model["unsupported"]  = if (UnsupportedTicker.isNotSupported(ticker)) { true } else { false }
        model["notFound"]     = if (stocks.isEmpty()) { true } else { false }
        model["ticker"]       = ticker

        if(stocks.isNotEmpty() && stocks[0].name != null) {
            model["name"] = stocks[0].name
        }

        if (stocks.isNotEmpty()) {
            model["profitMargin"]    = stocks[0].profitMargin
            model["debtRatio"]       = stocks[0].debtRatio
            model["flagBurnRate"]    = stocks[0].flagBurnRate
            model["flagDebtRatio"]   = stocks[0].flagDebtRatio
            model["cashBurnRate"]    = stocks[0].cashBurnRate
            model["cashBurnRateMag"] = stocks[0].cashBurnRateMag
        }

        return "ticker"
    }

    @PostMapping("/newsletter")
    @ResponseBody
    fun subscribe(
        model: Model,
        @RequestParam(name="email", required = true) email: String,
        @RequestParam(name="time_zone_offset", required = true) timeZoneOffset: String
    ): String {
        loggingService.log("Newsletter subscription request for : $email")
        Subscriber.sendConfirmationEmail(
            email          = EmailAddress(email),
            timeZoneOffset = timeZoneOffset
        )
        return "1"
    }

    @GetMapping("/confirm-email/{token}")
    fun confirmEmailAddress(
        model: Model,
        @PathVariable(name="token", required = true) token: String,
        response: HttpServletResponse
    ) : String  {
        if (Subscriber.confirmEmailAddress(token = token)) {
            loggingService.log("New email confirmation!!")
            return "standard-pages/email-confirmed"
            //response.writer.print("Email address confirmed!")
        } else {
            return "standard-pages/invalid-email-confirmation-link"
            //response.writer.print("Invalid or expired link.")
        }
        //return ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/error")
    fun showError(
        model: Model,
        response: HttpServletResponse
    ) : String {
        if (response.status == 404) {
            model["message"] = "Unknown page"
        } else {
            model["message"] = "An unknown error has occurred. Let us know on Twitter @BoipApp"
        }
        return "/error.html"
    }
}