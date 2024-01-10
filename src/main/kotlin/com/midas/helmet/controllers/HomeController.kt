package com.midas.helmet.controllers

import com.midas.helmet.configuration.ApplicationProperties
import com.midas.helmet.domain.StockInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.math.abs

@Controller
class HomeController(
    @Autowired private val applicationProperties: ApplicationProperties
) {
    @GetMapping("/")
    fun index(
        model: Model,
        @RequestParam(name="ticker", required = false) ticker: String?,
        @RequestParam(name="start", required = false) s: Int?,
        @RequestParam(name="profitability", required = false) p: Boolean?,
        @RequestParam(name="time_window", required = false)  t: Int?,
        @RequestParam(name="volatility_limit", required = false)  v: Int?
    ): String {
        val start           = if(ticker?.isNotEmpty() == true) { 0 } else {s?: 0 }
        val profitability   = if(ticker?.isNotEmpty() == true) { false } else {p ?: false}
        val timeWindow      = t ?: applicationProperties.defaultTimeWindow
        val volatilityLimit = if(ticker?.isNotEmpty() == true) { 1000 } else {v ?: applicationProperties.defaultVolatilityLimit }

        /** TODO: Temporary hack so extra pages aren't made to support viewTicker logic **/
        if(ticker?.isNotEmpty() == true) {
            model["stocks"] = StockInfo.queryTicker(
                ticker     = ticker,
                timeWindow = timeWindow
            )

            model["ticker"]          = ticker
            model["timeWindow"]      = timeWindow
            model["volatilityLimit"] = volatilityLimit
            model["profitability"]   = profitability

            return "index"
        }

        val results: List<StockInfo.StockInfoDto> = if(profitability) {
            StockInfo.queryProfitableStocks(
                start      = start,
                size       = applicationProperties.standardResultsPerPage + 1,
                timeWindow = timeWindow,
                min        = (-1.0)*abs(volatilityLimit),
                max        = 1.0*abs(volatilityLimit)
            )
        } else {
            StockInfo.queryAllStocks(
                start      = start,
                size       = applicationProperties.standardResultsPerPage + 1,
                timeWindow = timeWindow,
                min        = (-1.0)*abs(volatilityLimit),
                max        = 1.0*abs(volatilityLimit)
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
        model["profitability"]   = profitability

        return "index"
    }
}