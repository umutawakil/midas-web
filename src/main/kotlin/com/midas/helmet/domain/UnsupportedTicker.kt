package com.midas.helmet.domain

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.midas.helmet.services.LoggingService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.util.HashMap

class UnsupportedTicker {
    var name: String? = null //TODO: The need to use variables instead of vals is because of how the objectMapper deserializes json
    constructor()

    @Component
    class SpringAdapter(
        @Autowired private val loggingService: LoggingService
    ) {
        @PostConstruct
        fun init() {
            val file = File("unsupported-tickers.json")
            val objectMapper = ObjectMapper()
            val results: List<UnsupportedTicker> = objectMapper.readValue(file, object : TypeReference<List<UnsupportedTicker>>() {}) as List<UnsupportedTicker>

            loggingService.log("Loading unsupported tickers for ${results.size}")
            for(r: UnsupportedTicker in results) {
                tickers.add(r.name!!.lowercase())
            }
        }

    }

    companion object {
        private val tickers: MutableSet<String> = HashSet()

        fun isNotSupported(ticker: String) : Boolean {
            if(tickers.isEmpty()) {
                val file = File("unsupported-tickers.json")
                val objectMapper = ObjectMapper()
                val results: List<UnsupportedTicker> = objectMapper.readValue(file, object : TypeReference<List<UnsupportedTicker>>() {}) as List<UnsupportedTicker>
                for(r in results) {
                    tickers.add(r.name!!)
                }
            }
            return tickers.contains(ticker.lowercase())
        }
    }
}