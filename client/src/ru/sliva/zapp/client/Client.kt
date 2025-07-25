package ru.sliva.zapp.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import korlibs.encoding.fromBase64
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import ru.sliva.zapp.data.UserCredentials
import ru.sliva.zapp.data.UserType
import ru.sliva.zapp.data.protocol.*
import ru.sliva.zapp.data.utils.AES.decryptAes
import ru.sliva.zapp.data.utils.AES.encryptAes
import ru.sliva.zapp.data.utils.RSA
import ru.sliva.zapp.data.utils.RSA.decrypt

object Client {

    private val selectorManager = ActorSelectorManager(Dispatchers.IO)
    private val defaultPort = 9002
    lateinit var socket: Socket

    var rsaKeyPair = RSA.generateKeyPair()
    var aesKey: ByteArray? = null

    private var activeReadChannel: ByteReadChannel? = null

    val readChannel: ByteReadChannel
        get() = activeReadChannel ?: socket.openReadChannel().also { activeReadChannel = it }

    private var activeWriteChannel: ByteWriteChannel? = null

    val writeChannel: ByteWriteChannel
        get() = activeWriteChannel ?: socket.openWriteChannel(autoFlush = true).also { activeWriteChannel = it }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        socket = aSocket(selectorManager).tcp().connect("127.0.0.1", port = defaultPort)

        sendPacket(HandshakeC2S(rsaKeyPair.public))

        while(true) try {
            handlePacket(readPacket())
        } catch (e: Throwable) {
            e.printStackTrace()
            break
        }
    }

    suspend fun handlePacket(packet: ClientBoundPacket) = when(packet) {
        is HandshakeS2C -> {
            println("Received AES key: ${packet.aesKey}")

            aesKey = packet.aesKey.fromBase64()

            sendPacket(LoginC2S(
                userType = UserType.USER,
                credentials = UserCredentials("login", "password")
            ))
        }
        is SuccessLogin -> {
            println("Successfully logged in")
            println(packet.entity)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun sendPacket(packet: ServerBoundPacket) {
        var bytes = Cbor.encodeToByteArray<ServerBoundPacket>(packet)

        bytes = bytes.compress(GZIP)

        if(aesKey != null) {
            bytes = bytes.encryptAes(aesKey!!)
        }

        writeChannel.writeInt(bytes.size)
        writeChannel.writeFully(bytes)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun readPacket() : ClientBoundPacket {
        val dataLength = readChannel.readInt()

        var bytes = readChannel.readByteArray(dataLength)

        bytes = if(aesKey != null) {
            bytes.decryptAes(aesKey!!)
        } else {
            rsaKeyPair.private.decrypt(bytes)
        }

        bytes = bytes.uncompress(GZIP)

        return Cbor.decodeFromByteArray(bytes)
    }
}