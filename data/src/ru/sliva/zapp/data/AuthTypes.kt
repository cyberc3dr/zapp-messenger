package ru.sliva.zapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserType(val id: Byte) {
    @SerialName("user")
    USER(0x00),

    @SerialName("bot")
    BOT(0x01);

    companion object {
        fun Byte.toUserType() = entries.firstOrNull { it.id == this }
    }
}

@Serializable
sealed interface Credentials

@Serializable
@SerialName("user_credentials")
class UserCredentials(
    val login: String,
    val password: String
) : Credentials

@Serializable
@SerialName("bot_credentials")
class BotCredentials(
    val token: String
) : Credentials