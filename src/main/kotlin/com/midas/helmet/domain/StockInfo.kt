package com.midas.helmet.domain

import com.midas.helmet.configuration.repositories.StockInfoRepository
import jakarta.annotation.PostConstruct
import jakarta.persistence.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap
import kotlin.math.abs

@Entity
@Table(name="v_stock_info")
class StockInfo {
    /*@Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = -1L*/
    @Embeddable
    private class StockInfoId(val ticker: String, val timeWindow: Int) : Serializable

    @EmbeddedId
    private val id: StockInfoId
    //private val ticker: String
    private val name: String?
    private val windowDelta: Double
    private val minDelta: Double
    private val maxDelta: Double
    private val profitMargin: Double?
    @Column(name = "current_asset_liability")
    private val debtRatio: Double?
    @Column(name = "cfo_working_capital")
    private val cashBurnRate: Double?
    //private val timeWindow: Int

    class StockInfoDto (
        val ticker: String,
        val name: String?,
        val windowDelta: Double,
        val minDelta: Double,
        val maxDelta: Double,
        val profitMargin: Double?,
        val debtRatio: Double?,
        val cashBurnRate: Double?
    )

    constructor(
        ticker: String,
        name: String?,
        windowDelta: Double,
        minDelta: Double,
        maxDelta: Double,
        profitMargin: Double?,
        debtRatio: Double?,
        cashBurnRate: Double?,
        timeWindow: Int
    ) {
        //this.ticker       = ticker
        this.name         = name
        this.windowDelta  = windowDelta
        this.minDelta     = minDelta
        this.maxDelta     = maxDelta
        this.profitMargin = profitMargin
        this.debtRatio    = debtRatio
        this.cashBurnRate = cashBurnRate
        //this.timeWindow   = timeWindow
        this.id           = StockInfoId(ticker = ticker, timeWindow = timeWindow)
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
                val stockByWindow = stocksByTicker[r.id.ticker] ?: HashMap()
                stockByWindow[r.id.timeWindow] = r
                stocksByTicker[r.id.ticker] = stockByWindow

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


        //How do you paginate this since you have to start and stop?
        fun queryProfitableStocks(start: Int,size: Int, timeWindow: Int, min: Double, max: Double): List<StockInfoDto> {
            return query(
                start      = start,
                size       = size,
                timeWindow = timeWindow,
                set        = profitableStocksOnly,
                min        = min,
                max        = max
            )
        }
        fun queryAllStocks(start: Int,size: Int, timeWindow: Int, min: Double, max: Double): List<StockInfoDto> {
            return query(
                start      = start,
                size       = size,
                timeWindow = timeWindow,
                set        = allStocks,
                min        = min,
                max        = max
            )
        }

        fun getStockInfo(ticker: String) : Map<Int, StockInfo>? {
            return stocksByTicker[ticker]
        }

        private fun query(
                start: Int,
                size: Int,
                timeWindow: Int,
                set: Set<StockInfo>,
                min: Double,
                max: Double
        ) : List<StockInfoDto> {
            val results: List<StockInfo> = set.filter {
                it.id.timeWindow == timeWindow && it.maxDelta <= max && it.minDelta >= min
            }.sortedByDescending { it.windowDelta }

            var endIndex = start + size
            if(endIndex >= results.size) {
                endIndex = results.size
            }
            return results.subList(start, endIndex).map { x ->
                StockInfoDto(
                    ticker       = x.id.ticker,
                    name         = x.name,
                    windowDelta  = x.windowDelta,
                    minDelta     = x.minDelta,
                    maxDelta     = x.maxDelta,
                    profitMargin = x.profitMargin,
                    debtRatio    = x.debtRatio,
                    cashBurnRate = x.cashBurnRate
               )
            }
        }
    }
}