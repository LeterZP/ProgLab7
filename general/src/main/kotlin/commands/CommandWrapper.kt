package commands

import kotlinx.serialization.Serializable

@Serializable
class CommandWrapper() {
    var token: String = ""
    var command: String = ""
    var argumentsAmount = 0
    var arguments: List<String> = listOf("")
    var string: String = ""
    var describe: String = ""
    var syntax: String = ""
    var name: String = ""
    var result: String = ""
    var validation: Boolean = true

    fun wrapCommand(c: Command) {
        command = c.getName()
        argumentsAmount = c.argumentsAmount
        string = c.toString()
        describe = c.describe()
        syntax = c.getSyntax()
        name = c.getName()
        validation = c.validation
    }

    fun validate(args: List<String>): Boolean {
        if (!validation) return true
        if (args.size == argumentsAmount) return true
        if (args.size == 1) {
            try {
                args[0].toLong()
                return true
            } catch (_: NumberFormatException) {
                return false
            }
        }
        return false
    }
}