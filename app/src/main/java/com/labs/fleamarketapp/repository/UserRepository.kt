package com.labs.fleamarketapp.repository

import com.labs.fleamarketapp.local.dao.UserDao
import com.labs.fleamarketapp.local.entities.Status
import com.labs.fleamarketapp.local.entities.UserEntity
import com.labs.fleamarketapp.local.entities.UserRole
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun register(user: UserEntity) {
        userDao.upsert(user)
    }

    fun getUser(id: String): Flow<UserEntity?> = userDao.getById(id)

    suspend fun login(email: String): UserEntity? = userDao.getByEmail(email)

    fun getAll(): Flow<List<UserEntity>> = userDao.getAll()
}
