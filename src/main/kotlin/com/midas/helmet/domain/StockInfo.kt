package com.midas.helmet.domain

import jakarta.persistence.*

@Entity
@Table(name="v_stock_info")
class StockInfo {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = -1L
    private val ticker: String
    private val name: String
    private val windowDelta: Double
    private val minDelta: Double
    private val maxDelta: Double
    private val profitMargin: Double
    private val debtRatio: Double
    private val cashBurnRate: Double
    private val timeWindow: Int

    class StockInfoDto (
        val ticker: String,
        val name: String,
        val windowDelta: Double,
        val minDelta: Double,
        val maxDelta: Double,
        val profitMargin: Double,
        val debtRatio: Double,
        val cashBurnRate: Double,
        val timeWindow: Int
    )

    constructor(
        ticker: String,
        name: String,
        windowDelta: Double,
        minDelta: Double,
        maxDelta: Double,
        profitMargin: Double,
        debtRatio: Double,
        cashBurnRate: Double,
        timeWindow: Int
    ) {
        this.ticker       = ticker
        this.name         = name
        this.windowDelta  = windowDelta
        this.minDelta     = minDelta
        this.maxDelta     = maxDelta
        this.profitMargin = profitMargin
        this.debtRatio    = debtRatio
        this.cashBurnRate = cashBurnRate
        this.timeWindow   = timeWindow
    }

    companion object {
        fun find() : List<StockInfoDto> {
            val mutableList: MutableList<StockInfoDto> = mutableListOf()
            repeat(10) {
                mutableList.add(
                    StockInfoDto(
                        ticker = "ABC",
                        name = "ABC Corporation",
                        windowDelta = 88.0,
                        minDelta = -8.0,
                        maxDelta = 22.0,
                        profitMargin = 12.0,
                        debtRatio    = 90.0,
                        cashBurnRate = 120.0,
                        timeWindow = 20
                    )
                )
            }
            return mutableList
        }
    }
}