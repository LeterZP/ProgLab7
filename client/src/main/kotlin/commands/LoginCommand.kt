package commands

import core.CommandInvoker
import exceptions.InvalidLoginException
import java.security.MessageDigest

class LoginCommand(override val ci: CommandInvoker): Command(ci) {
    override val argumentsAmount: Int = 2

    override fun execute(arguments: List<String>) {
        try {
            val command = "serverLogin"
            var login = CommandWrapper()
            login.command = command
            login.name = command
            login.arguments = listOf(arguments[0])
            login.argumentsAmount = 1
            login = ci.executeCommand(login)
            if (login.result.length >= 11) result = login.result
            else {
                val salt = login.result
                val md = MessageDigest.getInstance("MD5")
                val pepper = "4b528400e376726c3f1b"
                val password = arguments[1]
                val hash = md.digest((password + salt + pepper).toByteArray()).decodeToString()
                login.arguments = listOf(arguments[0], hash)
                login.argumentsAmount = this.argumentsAmount
                login = ci.executeCommand(login)
                result = login.result
                ci.cm.token = login.token
                ci.getCommands()
            }
        } catch (e: InvalidLoginException) {
            result = e.message + "\n"
        }
    }

    override fun getSyntax(): String {
        return "[login] [password]"
    }

    override fun getName(): String {
        return "login"
    }

    override fun describe(): String {
        return "Авторизует пользователя"
    }
}