package com.midas.helmet.repositories

import com.midas.helmet.domain.UnsupportedTicker
import org.springframework.data.repository.CrudRepository

interface UnsupportedTickerRepository : CrudRepository<UnsupportedTicker, String> {
}