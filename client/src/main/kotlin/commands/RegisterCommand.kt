package commands

import core.CommandInvoker
import exceptions.InvalidRepeatedValueException
import java.security.MessageDigest
import java.security.SecureRandom

class RegisterCommand(override val ci: CommandInvoker): Command(ci) {
    override val argumentsAmount: Int = 3

    override fun execute(arguments: List<String>) {
        try {
            if (arguments[1] != arguments[2]) throw InvalidRepeatedValueException("password")
            val md = MessageDigest.getInstance("MD5")
            val password = arguments[1]
            val salt = SecureRandom.getSeed(10).decodeToString()
            val pepper = "4b528400e376726c3f1b"
            val hash = md.digest((password + salt + pepper).toByteArray()).decodeToString()
            val command = "serverRegister"
            val list = listOf(arguments[0], hash, salt)
            var register = CommandWrapper()
            register.command = command
            register.name = command
            register.argumentsAmount = this.argumentsAmount
            register.arguments = list
            register = ci.executeCommand(register)
            result = register.result
            ci.cm.token = register.token
            ci.getCommands()
        } catch (e: InvalidRepeatedValueException) {
            result = e.message + "\n"
        }
    }

    override fun getSyntax(): String {
        return "[login] [password] [password]"
    }

    override fun getName(): String {
        return "register"
    }

    override fun describe(): String {
        return "Регистрирует новых пользователей"
    }
}