package com.labs.fleamarketapp.repository

import com.labs.fleamarketapp.local.dao.OrderDao
import com.labs.fleamarketapp.local.entities.OrderEntity
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {
    suspend fun upsert(order: OrderEntity) = orderDao.upsert(order)
    fun getOrder(id: String): Flow<OrderEntity?> = orderDao.getById(id)
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> = orderDao.getOrdersForUser(userId)
}
