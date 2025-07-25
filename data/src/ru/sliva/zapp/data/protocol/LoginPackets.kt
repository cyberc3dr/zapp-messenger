@file:UseSerializers(PublicKeySerializer::class)

package ru.sliva.zapp.data.protocol

import korlibs.encoding.base64
import korlibs.encoding.fromBase64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.sliva.zapp.data.Credentials
import ru.sliva.zapp.data.Entity
import ru.sliva.zapp.data.UserType
import ru.sliva.zapp.data.utils.RSA.toPublicKey
import java.security.PublicKey

// Serverbound packets

@Serializable
@SerialName("handshake_c2s")
data class HandshakeC2S (
    var rsaKey: PublicKey
) : ServerBoundPacket

@Serializable
@SerialName("login_c2s")
data class LoginC2S (
    var userType: UserType,
    var credentials: Credentials
) : ServerBoundPacket

// Clientbound packets

@Serializable
@SerialName("handshake_s2c")
data class HandshakeS2C (
    var aesKey: String
) : ClientBoundPacket

@Serializable
@SerialName("login_s2c")
data class SuccessLogin(
    var entity: Entity
) : ClientBoundPacket

object PublicKeySerializer : KSerializer<PublicKey> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("PublicKey", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PublicKey) {
        encoder.encodeString(value.encoded.base64)
    }

    override fun deserialize(decoder: Decoder): PublicKey {
        return decoder.decodeString().fromBase64().toPublicKey()
    }
}