package com.midas.helmet.domain

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

class UnsupportedTicker {
    var name: String? = null
    constructor()

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