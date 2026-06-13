package core

import exceptions.ProgramExitException
import io.IOManager

class ServerInitializer(val port: Int, val io: IOManager) {
    val cm = CollectionManager(io)
    val um = UserManager(io)
    val tasks = TaskManager(cm, um, port)
    lateinit var server: Thread

    fun initialize() {
        server = Thread {
            var isWorking = true
            while (isWorking) {
                try {
                    val ci = CommandInvoker(cm, um)
                    ci.runOnServer(io.readLocalCommands())
                } catch (e: ProgramExitException) {
                    isWorking = false
                }
            }
        }
        server.start()
        io.logger.info("Сервер запущен.")
        tasks.start()
    }

    fun waitForExit() {
        if (server.isAlive) server.join()
        tasks.join()
        io.logger.info("Завершение работы.")
    }
}