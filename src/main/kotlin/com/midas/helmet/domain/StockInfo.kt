package com.midas.helmet.domain

import com.midas.helmet.repositories.StockInfoRepository
import jakarta.annotation.PostConstruct
import jakarta.persistence.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

@Entity
@Table(name="v_stock_info", schema = "helmet")
class StockInfo {
    @Embeddable
    private class StockInfoId(val ticker: String, val timeWindow: Int) : Serializable

    @EmbeddedId
    private val id: StockInfoId
    private val name: String?
    private val windowDelta: Double
    private val minDelta: Double
    private val maxDelta: Double
    private val volumeDelta: Double
    private val profitMargin: Double?
    @Column(name = "debt_percentage")
    private val debtPercentage: Double?
    @Column(name = "cfo_working_capital")
    private val cashBurnRate: Double?
    private val secSectorCode: Int?
    private val otc: Boolean?

    class StockInfoDto (
        val ticker: String,
        val name: String?,
        val windowDelta: Double,
        val minDelta: Double,
        val maxDelta: Double,
        val volumeDelta: Double,
        val profitMargin: Double?,
        val debtRatio: Double?,
        val flagDebtRatio: Boolean,
        val cashBurnRate: Double?,
        val cashBurnRateMag: Double?,
        val showBurnRate: Boolean,
        val flagBurnRate: Boolean,
        val timeWindow: Int
    )

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


    @Component
    class SpringAdapter(
        @Autowired private val stockInfoRepository: StockInfoRepository
    ) {
        @PostConstruct
        fun init() {
            StockInfo.stockInfoRepository = stockInfoRepository

            val results = stockInfoRepository.findAll().toList()
            println("Loading stock info for ${results.size}")
            for(r in results) {
                /** Populate the map for querying individual tickers **/
                val stockByWindow = stocksByTicker[r.id.ticker.lowercase()] ?: HashMap()
                stockByWindow[r.id.timeWindow] = r
                stocksByTicker[r.id.ticker.lowercase()] = stockByWindow

                /** Populate the maps for browsing profitable stocks as well as profitable + unprofitable **/
                if((r.profitMargin != null) && r.profitMargin > 0) {
                    profitableStocksOnly.add(r)
                    //println("A: ${r.id.ticker}")
                } else {
                    allStocks.add(r)
                    //println("B: ${r.id.ticker}")
                }
            }
            println("Stock info loaded.")
            println("Profitable stock records: " + profitableStocksOnly.size)
            println("All records: " + allStocks.size)
        }

    }


    companion object {
        private lateinit var stockInfoRepository: StockInfoRepository
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
                (it.id.timeWindow == timeWindow) &&
                (it.maxDelta <= max) &&
                (it.minDelta >= min) &&
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
                    ticker          = x.id.ticker,
                    name            = x.name,
                    windowDelta     = x.windowDelta,
                    minDelta        = x.minDelta,
                    maxDelta        = x.maxDelta,
                    volumeDelta     = x.volumeDelta,
                    profitMargin    = x.profitMargin,
                    debtRatio       = x.debtPercentage,
                    flagDebtRatio   = x.debtPercentage != null && x.debtPercentage > 50.0,
                    cashBurnRate    = x.cashBurnRate,
                    cashBurnRateMag = if( x.cashBurnRate != null && x.cashBurnRate <0) { abs(x.cashBurnRate) } else { null },
                    showBurnRate    = x.cashBurnRate != null && x.cashBurnRate < 0,
                    flagBurnRate    = x.cashBurnRate != null && x.cashBurnRate <= -100,
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
                    ticker          = x.id.ticker,
                    name            = x.name,
                    windowDelta     = x.windowDelta,
                    minDelta        = x.minDelta,
                    maxDelta        = x.maxDelta,
                    volumeDelta     = x.volumeDelta,
                    profitMargin    = x.profitMargin,
                    debtRatio       = x.debtPercentage,
                    flagDebtRatio   = x.debtPercentage != null && x.debtPercentage > 50.0,
                    cashBurnRate    = x.cashBurnRate,
                    cashBurnRateMag = if( x.cashBurnRate != null && x.cashBurnRate <0) { abs(x.cashBurnRate) } else { null },
                    showBurnRate    = x.cashBurnRate != null && x.cashBurnRate < 0,
                    flagBurnRate    = x.cashBurnRate != null && x.cashBurnRate <= -100,
                    timeWindow      = timeWindow
                )
            )
            return resultList
        }
    }
}