package com.midas.helmet.configuration.repositories

import com.midas.helmet.domain.StockInfo
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface StockInfoRepository : CrudRepository<StockInfo, Long> {
}