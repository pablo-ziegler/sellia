// File: UserRepository.kt
package com.example.selliaapp.repository

import com.example.selliaapp.data.dao.UserDao
import com.example.selliaapp.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    fun observeUsers(): Flow<List<User>> = userDao.observeAll()
        .onStart { /* hook para loading si querés */ }

    suspend fun insert(user: User): Long = withContext(Dispatchers.IO) { userDao.insert(user) }

    suspend fun update(user: User): Int = withContext(Dispatchers.IO) { userDao.update(user) }

    suspend fun delete(user: User): Int = withContext(Dispatchers.IO) { userDao.delete(user) }
}
