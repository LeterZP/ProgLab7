package core

import commands.CommandWrapper
import exceptions.InvalidTokenException
import exceptions.ProgramExitException
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class ConnectionReceiver(private val ci: CommandInvoker, private val port: Int) {
    private var isWorking = true
    private var waitingForCommand = false
    private val socket = DatagramSocket(port, InetAddress.getLocalHost())
    private var bytes = ByteArray(32768)
    private val io = ci.io

    fun checkConnection() {
        if (!isWorking) throw ProgramExitException()
        bytes = ByteArray(32768)
        val packet = DatagramPacket(bytes, bytes.size)
        waitingForCommand = true
        socket.receive(packet)
        waitingForCommand = false
        if (isWorking) receive(packet.address, packet.port)
    }

    private fun receive(host: InetAddress, port: Int) {
        io.logger.info("Запрос получен.")
        var cw: CommandWrapper = Json.decodeFromString(bytes.decodeToString().replace("\u0000", ""))
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
        send(host, port, result)
    }

    private fun send(host: InetAddress, port: Int, response: String) {
        bytes = response.toByteArray()
        val packet = DatagramPacket(bytes, bytes.size, host, port)
        socket.send(packet)
        io.logger.info("Результат отправлен.")
    }

    fun saveInterrupt() {
        isWorking = false
        if (waitingForCommand) {
            send(socket.localAddress, port, "exit")
        }
    }
}