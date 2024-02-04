package com.midas.helmet.domain

import com.midas.helmet.repositories.UnsupportedTickerRepository
import jakarta.annotation.PostConstruct
import jakarta.persistence.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Entity
@Table(name="v_unsupported_ticker", schema = "helmet")
class UnsupportedTicker(
    @Id
    @Column(name="ticker")
    private val name: String
) {
    @Component
    private class SpringAdapter(
        @Autowired
        private val unsupportedTickerRepository: UnsupportedTickerRepository
    ) {
        @PostConstruct
        fun init() {
            UnsupportedTicker.unsupportedTickerRepository = unsupportedTickerRepository

            for(t in unsupportedTickerRepository.findAll()) {
                tickers.add(t.name.lowercase())
            }
        }
    }

    companion object {
        private lateinit var unsupportedTickerRepository: UnsupportedTickerRepository
        private val tickers: MutableSet<String> = HashSet()

        fun isNotSupported(ticker: String) : Boolean {
            return tickers.contains(ticker.lowercase())
        }
    }
}