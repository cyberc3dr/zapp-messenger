package ru.sliva.zapp.server

import ch.qos.logback.core.net.server.Client
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import korlibs.crypto.SecureRandom
import korlibs.encoding.base64
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import ru.sliva.zapp.data.User
import ru.sliva.zapp.data.UserCredentials
import ru.sliva.zapp.data.protocol.*
import ru.sliva.zapp.data.utils.AES.decryptAes
import ru.sliva.zapp.data.utils.AES.encryptAes
import ru.sliva.zapp.data.utils.RSA.encrypt
import java.security.PublicKey
import kotlin.coroutines.CoroutineContext

class ServerSession(val socket: Socket) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    var rsaKey: PublicKey? = null
    val aesKey = SecureRandom.nextBytes(32)

    var authorized = false

    private var activeReadChannel: ByteReadChannel? = null

    val readChannel: ByteReadChannel
        get() = activeReadChannel ?: socket.openReadChannel().also { activeReadChannel = it }

    private var activeWriteChannel: ByteWriteChannel? = null

    val writeChannel: ByteWriteChannel
        get() = activeWriteChannel ?: socket.openWriteChannel(autoFlush = true).also { activeWriteChannel = it }

    init {
        launch {
            while(true) try {
                handlePacket(readPacket())
            } catch (e: Throwable) {
                e.printStackTrace()
                break
            }
        }
    }

    suspend fun handlePacket(packet: ServerBoundPacket) = when(packet) {
        is HandshakeC2S -> {
            println("Received RSA key: ${packet.rsaKey}")

            rsaKey = packet.rsaKey

            sendPacket(HandshakeS2C(aesKey.base64))

            authorized = true
        }
        is LoginC2S -> {
            println("Received login packet: ${packet.userType} ${packet.credentials}")

            (packet.credentials as? UserCredentials)?.let {
                println("Login: ${it.login} Password: ${it.password}")
            }

            sendPacket(SuccessLogin(
                User(
                    1,
                    "Test",
                    "test",
                    "Bio",
                    0
                )
            ))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun sendPacket(packet: ClientBoundPacket) {
        var bytes = Cbor.encodeToByteArray<ClientBoundPacket>(packet)

        bytes = bytes.compress(GZIP)

        if(authorized) {
            bytes = bytes.encryptAes(aesKey)
        } else if(rsaKey != null) {
            bytes = rsaKey!!.encrypt(bytes)
        }

        writeChannel.writeInt(bytes.size)
        writeChannel.writeFully(bytes)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun readPacket() : ServerBoundPacket {
        val dataLength = readChannel.readInt()

        var bytes = readChannel.readByteArray(dataLength)

        if(authorized) {
            bytes = bytes.decryptAes(aesKey)
        }

        bytes = bytes.uncompress(GZIP)

        return Cbor.decodeFromByteArray(bytes)
    }
}