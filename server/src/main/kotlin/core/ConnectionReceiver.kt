package core

import commands.CommandWrapper
import exceptions.InvalidTokenException
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class ConnectionReceiver(private val ci: CommandInvoker, private val socket: DatagramSocket) {
    private var bytes = ByteArray(32768)
    private val io = ci.io

    fun receive(): TaskManager.Packet {
        bytes = ByteArray(32768)
        val packet = DatagramPacket(bytes, bytes.size)
        socket.receive(packet)
        io.logger.info("Запрос получен.")
        return TaskManager.Packet(packet.address, packet.port, bytes.decodeToString().replace("\u0000", ""))
    }

    fun process(message: String): String {
        var cw: CommandWrapper = Json.decodeFromString(message)
        val result: String
        when (cw.name) {
            "help" -> {
                val list = if (cw.token in ci.um.tokens.values) ci.getAllCommandsWrapped() else listOf()
                cw.result = Json.encodeToString(list)
            }
            "exit" -> {
                ci.um.removeValidToken(cw.token)
                cw.result = "Токен доступа инвалидирован.\n"
                io.logger.info("Пользователь вышел.")
            }
            else -> {
                try {
                    cw = ci.executeCommand(cw)
                } catch (e: InvalidTokenException) {
                    cw.result = e.message + "\n"
                }
            }
        }
        result = Json.encodeToString(cw)
        io.logger.info("Результат загружен.")
        return result
    }

    fun send(host: InetAddress, port: Int, response: String) {
        bytes = response.toByteArray()
        val packet = DatagramPacket(bytes, bytes.size, host, port)
        socket.send(packet)
        io.logger.info("Результат отправлен.")
    }
}