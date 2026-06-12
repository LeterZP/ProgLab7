package commands

import core.CommandInvoker
import exceptions.InvalidAmountOfArgumentsException
import exceptions.InvalidLoginException
import exceptions.InvalidTokenException

class ServerLoginCommand(override val ci: CommandInvoker): Command(ci) {
    override val argumentsAmount: Int = 2

    override fun execute(arguments: List<String>) {
        try {
            when (arguments.size) {
                1 -> {
                    result = ci.um.getSalfFromUser(arguments[0])
                    if (result == "") throw InvalidLoginException("login")
                }
                2 -> {
                    val hash = ci.um.getHashFromUser(arguments[0])
                    if (hash != arguments[1]) throw InvalidLoginException("password")
                    result = "Авторизация успешна.\n"
                    throw InvalidTokenException(arguments[0])
                }
                else -> throw InvalidAmountOfArgumentsException(this, arguments.size)
            }
        } catch (e: InvalidLoginException) {
            result = e.message + "\n"
        } catch (e: InvalidAmountOfArgumentsException) {
            result = e.message + "\n"
        }
    }

    override fun getSyntax(): String {
        return "[login] [password]"
    }

    override fun getName(): String {
        return "serverLogin"
    }

    override fun describe(): String {
        return ""
    }
}