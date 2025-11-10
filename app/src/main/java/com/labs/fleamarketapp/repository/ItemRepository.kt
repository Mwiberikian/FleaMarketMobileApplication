package com.labs.fleamarketapp.repository

import com.labs.fleamarketapp.local.dao.CategoryDao
import com.labs.fleamarketapp.local.dao.ItemDao
import com.labs.fleamarketapp.local.entities.CategoryEntity
import com.labs.fleamarketapp.local.entities.ItemEntity
import com.labs.fleamarketapp.local.entities.ItemType
import com.labs.fleamarketapp.local.entities.Status
import kotlinx.coroutines.flow.Flow

class ItemRepository(
    private val itemDao: ItemDao,
    private val categoryDao: CategoryDao
) {
    suspend fun upsertItem(item: ItemEntity) = itemDao.upsert(item)
    fun getItem(id: String): Flow<ItemEntity?> = itemDao.getById(id)
    fun getActiveItems(): Flow<List<ItemEntity>> = itemDao.getByStatus(Status.ACTIVE)
    fun getItemsByCategory(categoryId: Long): Flow<List<ItemEntity>> = itemDao.getByCategoryAndStatus(categoryId, Status.ACTIVE)
    fun search(query: String): Flow<List<ItemEntity>> = itemDao.search(query)
    fun getByType(type: ItemType): Flow<List<ItemEntity>> = itemDao.getByType(type)

    suspend fun upsertCategory(category: CategoryEntity) = categoryDao.upsert(category)
    fun getCategories(): Flow<List<CategoryEntity>> = categoryDao.getAll()
}
