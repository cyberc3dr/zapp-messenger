package ru.sliva.zapp.data.database.user

import ru.sliva.zapp.data.Credentials

// todo сделать чет покруче чем это
interface AuthManager {
    suspend fun isValidCredentials(credentials: Credentials) : Boolean

}