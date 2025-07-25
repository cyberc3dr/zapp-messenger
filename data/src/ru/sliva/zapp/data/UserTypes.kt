package ru.sliva.zapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Entity {
    val id: Int
    val name: String
    val username: String?
    val description: String?
}

@Serializable
@SerialName("user")
data class User(
    override val id: Int,
    override val name: String,
    override val username: String?,
    override val description: String?,
    val lastSeen: Long?
) : Entity

@Serializable
@SerialName("bot")
data class Bot(
    override val id: Int,
    override val name: String,
    override val username: String?,
    override val description: String?,
    val commands: List<String>
) : Entity
