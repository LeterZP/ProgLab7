package commands

import core.CommandInvoker
import exceptions.InvalidLoginException
import exceptions.InvalidTokenException

class ServerRegisterCommand(override val ci: CommandInvoker): Command(ci) {
    override val argumentsAmount: Int = 3

    override fun execute(arguments: List<String>) {
        super.execute(arguments)
        try {
            if (arguments[0] in ci.um.getAllUsers()) {
                throw InvalidLoginException("register")
            }
            ci.um.addUser(arguments)
            result = "Пользователь ${arguments[0]} успешно зарегистрирован.\n"
            throw InvalidTokenException(arguments[0])
        } catch (e: InvalidLoginException) {
            result = e.message + "\n"
        }
    }

    override fun getSyntax(): String {
        return "[login] [hash] [salt]"
    }

    override fun getName(): String {
        return "serverRegister"
    }

    override fun describe(): String {
        return "Серверная версия команды для регистрации новых пользователей."
    }
}