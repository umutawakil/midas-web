package com.midas.helmet.repositories

import com.midas.helmet.domain.Subscriber
import org.springframework.data.repository.CrudRepository

interface SubscriberRepository : CrudRepository<Subscriber, Long> {
}