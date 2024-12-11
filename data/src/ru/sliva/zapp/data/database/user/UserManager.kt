package ru.sliva.zapp.data.database.user

import ru.sliva.zapp.data.User

interface UserManager {

    suspend fun findUserByID(id: Int, fromDb: Boolean = false) : User?
    suspend fun findUserByUsername(username: String, fromDb: Boolean = false) : User?
    suspend fun findUsersByLastSeen(lastSeen: Long, fromDb: Boolean = false) : List<User>
    fun getAllUsers() : List<User>
}