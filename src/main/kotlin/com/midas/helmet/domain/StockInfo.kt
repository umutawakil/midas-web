package com.midas.helmet.domain

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.midas.helmet.services.LoggingService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.io.Serializable
import java.util.*
import kotlin.math.abs


class StockInfo {
    class StockInfoId : Serializable {
        var ticker: String? = null
        var timeWindow: Int? = null

        constructor(ticker: String?, timeWindow: Int?) {
            this.ticker     = ticker
            this.timeWindow = timeWindow
        }

        constructor()
    }

    var id: StockInfoId? = null
    var name: String? = null
    var windowDelta: Double? = null
    var minDelta: Double? = null
    var maxDelta: Double? = null
    var volumeDelta: Double? = null
    var profitMargin: Double? = null
    var debtPercentage: Double? = null
    var cashBurnRate: Double? = null
    var secSectorCode: Int? = null
    var otc: Boolean? = null

    class StockInfoDto (
        val ticker: String?,
        val name: String?,
        val windowDelta: Double?,
        val minDelta: Double?,
        val maxDelta: Double?,
        val volumeDelta: Double?,
        val profitMargin: Double?,
        val debtRatio: Double?,
        val flagDebtRatio: Boolean?,
        val cashBurnRate: Double?,
        val cashBurnRateMag: Double?,
        val showBurnRate: Boolean?,
        val flagBurnRate: Boolean?,
        val timeWindow: Int?
    )

    constructor() {}

    constructor(
        ticker: String,
        name: String?,
        windowDelta: Double,
        minDelta: Double,
        maxDelta: Double,
        volumeDelta: Double,
        profitMargin: Double?,
        debtRatio: Double?,
        cashBurnRate: Double?,
        timeWindow: Int,
        secSectorCode: Int?,
        otc: Boolean?
    ) {
        this.name           = name
        this.windowDelta    = windowDelta
        this.minDelta       = minDelta
        this.maxDelta       = maxDelta
        this.volumeDelta    = volumeDelta
        this.profitMargin   = profitMargin
        this.debtPercentage = debtRatio
        this.cashBurnRate   = cashBurnRate
        this.id             = StockInfoId(ticker = ticker, timeWindow = timeWindow)
        this.secSectorCode  = secSectorCode
        this.otc            = otc
    }


    @Suppress("UNCHECKED_CAST")
    @Component
    class SpringAdapter(
        @Autowired private val loggingService: LoggingService
    ) {
        @PostConstruct
        fun init() {
            val file = File("stock-info.json")
            val objectMapper = ObjectMapper()
            val results: List<StockInfo> = objectMapper.readValue(file, object : TypeReference<List<StockInfo>>() {}) as List<StockInfo>

            loggingService.log("Loading stock info for ${results.size}")
            for(r: StockInfo in results) {
                /** Populate the map for querying individual tickers **/
                val stockByWindow = stocksByTicker[r.id!!.ticker!!.lowercase()] ?: HashMap()
                stockByWindow[r.id!!.timeWindow!!]          = r
                stocksByTicker[r.id!!.ticker!!.lowercase()] = stockByWindow

                /** Populate the maps for browsing profitable stocks as well as profitable + unprofitable **/
                if((r.profitMargin != null) && r.profitMargin!! > 0) {
                    profitableStocksOnly.add(r)
                }
                allStocks.add(r)

            }
            loggingService.log("Stock info loaded.")
            loggingService.log("Profitable stock records: " + profitableStocksOnly.size)
            loggingService.log("All records: " + allStocks.size)
        }

    }


    companion object {
        private val profitableStocksOnly: MutableSet<StockInfo> = HashSet()
        private val allStocks: MutableSet<StockInfo>            = HashSet()
        private val stocksByTicker: MutableMap<String, MutableMap<Int, StockInfo>> = HashMap()
        private val timeWindows: List<Int> = listOf(3, 5, 10, 20, 40, 60)

        //How do you paginate this since you have to start and stop?
        fun queryProfitableStocks(start: Int,size: Int, timeWindow: Int, min: Double, max: Double, orderDescending: Boolean): List<StockInfoDto> {
            return query(
                start           = start,
                size            = size,
                timeWindow      = timeWindow,
                set             = profitableStocksOnly,
                min             = min,
                max             = max,
                orderDescending = orderDescending
            )
        }
        fun queryAllStocks(start: Int,size: Int, timeWindow: Int, min: Double, max: Double, orderDescending: Boolean): List<StockInfoDto> {
            return query(
                start           = start,
                size            = size,
                timeWindow      = timeWindow,
                set             = allStocks,
                min             = min,
                max             = max,
                orderDescending = orderDescending
            )
        }

        private fun getStockInfo(ticker: String, timeWindow: Int) : StockInfo? {
            return stocksByTicker[ticker.lowercase()]?.get(timeWindow)
        }

        /** TODO: Given the way things are structured all filtering should probably be done here against one
         * data structure instead of one for profitable stocks and one for all stocks so we can easily
         * toggle in one place as an OTC and medical button are eventually added to the UI.
         */

        private fun filterStandardQuery(timeWindow: Int,
                                        set: Set<StockInfo>,
                                        min: Double,
                                        max: Double,
                                        orderDescending: Boolean) : List<StockInfo> {
            val results = set.filter {
                (it.id!!.timeWindow == timeWindow) &&
                (it.maxDelta!! <= max) &&
                (it.minDelta!! >= min) &&
                (it.secSectorCode != 283) &&
                (!("$it.secSectorCode".startsWith("38"))) &&
                (!("$it.secSectorCode".startsWith("80"))) &&
                (it.otc == false)
            }
            if(orderDescending) {
                return results.sortedByDescending { it.windowDelta }
            }
            return results.sortedBy { it.windowDelta }
        }

        private fun query(
                start: Int,
                size: Int,
                timeWindow: Int,
                set: Set<StockInfo>,
                min: Double,
                max: Double,
                orderDescending: Boolean
        ) : List<StockInfoDto> {
            val results: List<StockInfo> = filterStandardQuery(
                timeWindow      = timeWindow,
                min             = min,
                max             = max,
                set             = set,
                orderDescending = orderDescending
            )

            var endIndex = start + size
            if(endIndex >= results.size) {
                endIndex = results.size
            }
            //Todo: The DTOafication of the domain data needs to be abstracted because its utilized in two methods
            return results.subList(start, endIndex).map { x ->
                StockInfoDto(
                    ticker          = x.id!!.ticker,
                    name            = x.name,
                    windowDelta     = x.windowDelta,
                    minDelta        = x.minDelta,
                    maxDelta        = x.maxDelta,
                    volumeDelta     = x.volumeDelta,
                    profitMargin    = x.profitMargin,
                    debtRatio       = x.debtPercentage,
                    flagDebtRatio   = x.debtPercentage != null && x.debtPercentage!! > 50.0,
                    cashBurnRate    = x.cashBurnRate,
                    cashBurnRateMag = if( x.cashBurnRate != null && x.cashBurnRate!! <0) { abs(x.cashBurnRate!!) } else { null },
                    showBurnRate    = x.cashBurnRate != null && x.cashBurnRate!! < 0,
                    flagBurnRate    = x.cashBurnRate != null && x.cashBurnRate!! <= -100,
                    timeWindow      = timeWindow
               )
            }
        }

        fun queryTickerWindows(ticker: String) : List<StockInfoDto> {
            return timeWindows.map {
                queryTicker(ticker = ticker, timeWindow = it)[0]
            }.sortedBy {it.timeWindow}
        }

        fun queryTicker(
            ticker: String,
            timeWindow: Int
        ) : List<StockInfoDto> {
            val resultList: MutableList<StockInfoDto> = mutableListOf()
            val x: StockInfo = getStockInfo(ticker = ticker, timeWindow = timeWindow) ?: return emptyList()

             resultList.add(
                StockInfoDto(
                    ticker          = x.id!!.ticker,
                    name            = x.name,
                    windowDelta     = x.windowDelta,
                    minDelta        = x.minDelta,
                    maxDelta        = x.maxDelta,
                    volumeDelta     = x.volumeDelta,
                    profitMargin    = x.profitMargin,
                    debtRatio       = x.debtPercentage,
                    flagDebtRatio   = x.debtPercentage != null && x.debtPercentage!! > 50.0,
                    cashBurnRate    = x.cashBurnRate,
                    cashBurnRateMag = if( x.cashBurnRate != null && x.cashBurnRate!! <0) { abs(x.cashBurnRate!!) } else { null },
                    showBurnRate    = x.cashBurnRate != null && x.cashBurnRate!! < 0,
                    flagBurnRate    = x.cashBurnRate != null && x.cashBurnRate!! <= -100,
                    timeWindow      = timeWindow
                )
            )
            return resultList
        }
    }
}