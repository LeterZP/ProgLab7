package core

import commands.*
import exceptions.*
import elements.CityBuilder
import io.IOManager
import java.util.Stack

/**
 * Класс вызова команд.
 *
 * Позволяет вызывать инициализированные в нём команды для работы с коллекцией посредством [CollectionManager].
 *
 * @param io [IOManager], откуда читаются команды.
 *
 * @property commands [HashMap] команд, содержащий имя команды типа [String] и саму команду типа [Command].
 *
 * @constructor Принимает все описанные выше параметры, создавая объект, уже содержащий в себе стандартный набор команд.
 *
 * @since 1.0
 */
class CommandInvoker(val io: IOManager, val cm: ConnectionManager): CommandInvokerInterface {
    private val nextArgument: Stack<String> = Stack<String>()
    val commands: HashMap<String, CommandWrapper> = HashMap()
    val executionHistory = Stack<String>()

    init {
        try {
            getCommands()
        } catch (e: ConnectionException) {
            io.write(e.message + "\n")
            throw ProgramExitException()
        }
    }

    /**
     * Инициализирует команду, добавляя её в список возможных к использованию.
     *
     * @param command Команда для инициализации, типа [Command].
     *
     * @since 1.0
     */
    fun initializeCommand(command: CommandWrapper) {
        val name: String = command.name
        commands[name] = command
    }

    /**
     * Вызывает команду, читая её либо из очереди команд, либо из консоли в случае, если очередь пуста.
     *
     * @since 1.0
     */
    override fun executeCommand(cw: CommandWrapper): CommandWrapper {
        if (cw.name in getStandardCommandsWrapped().map { c -> c.name }) {
            val command: Command = getStandardCommands().stream()
                .filter { c -> c.getName() == cw.name }
                .toList()[0]
            command.execute(cw.arguments)
            cw.result = command.result
            return cw
        } else {
            getCommands()
            val result = cm.sendAndReceive(cw)
            return result
        }
    }

    fun readCommand() {
        var instruction: List<String> = try {
            readNext().trim().split(" ")
        } catch (_: NoNextCommandException) {
            io.read().trim().split(" ")
        }
        if (instruction.size == 1 && instruction[0] == "") return
        try {
            if (instruction[0] !in run {
                    val list1 = mutableListOf<String>()
                    for (i in this@CommandInvoker.getStandardCommandsWrapped()) {
                        list1.add(i.name)
                    }
                    list1
                }) {
                if (instruction[0] !in commands.keys) throw CommandNotFoundException(instruction[0])
            }
            val command = commands[instruction[0]]!!
            instruction = validateCommand(command, instruction)
            command.arguments = instruction.minus(instruction[0])
            io.write(executeCommand(command).result)
        } catch (e: CommandNotFoundException) {
            io.write(e.message + "\n")
        } catch (e: InvalidElementValueException) {
            io.write(e.message + "\n")
        }
    }

    /**
     * Читает первую в очереди команду.
     *
     * @return Команду с аргументами типа [String].
     *
     * @throws NoNextCommandException В случае, если очередь из команд пуста.
     *
     * @since 1.0
     */
    fun readNext(): String {
        val result: String
        if (nextArgument.isEmpty()) {
            executionHistory.clear()
            throw NoNextCommandException()
        }
        else {
            result = nextArgument.pop()
        }
        return result
    }

    /**
     * Добавляет одну или несколько команд в очередь.
     *
     * @param instructions Одна или несколько команд с аргументами типа [String].
     *
     * @since 1.0
     */
    fun addNext(instructions: String) {
        val values: List<String> = instructions.lines().reversed()
        for (instruction in values) {
            nextArgument.push(instruction)
        }
    }

    /**
     * Выбирает следующее значение для ввода.
     *
     * @see [IOManager.askForValue].
     */
    fun nextValue(output: String): String {
        val input: String
        if (nextArgument.isEmpty()) {
            input = io.askForValue(output)
        } else {
            input =  nextArgument.pop()
        }
        return input
    }

    fun getCommands() {
        val list = cm.askCommandList()
        commands.clear()
        for (command in list) {
            initializeCommand(command)
        }
        for (st in getStandardCommandsWrapped()) {
            initializeCommand(st)
        }
    }

    fun validateCommand(command: CommandWrapper, instruction: List<String>): List<String> {
        if (command.argumentsAmount == CityBuilder().size) {
            val args = mutableListOf<String>()
            args.add(instruction[0])
            if (instruction.size > 1) args.add(instruction[1])
            val creator = CityBuilder()
            if (!command.validation) creator.strictCheck = false
            var count = 0
            while (true) {
                val value: String = nextValue(creator.getField(count))
                try {
                    creator.setField(value, count)
                    args.add(value)
                } catch (e: InvalidElementValueException) {
                    io.write(e.message + "\n")
                    continue
                }
                if (count == creator.size - 1) break
                count++
            }
            return args.toList()
        } else if (command.argumentsAmount > 1) {
            val args = instruction.toMutableList()
            while (args.size <= command.argumentsAmount) {
                args.add("")
            }
            return args.toList()
        } else if (!commands[instruction[0]]!!.validate(instruction.minus(instruction[0])))
                throw InvalidElementValueException(instruction.minus(instruction[0]))
        return instruction
    }

    fun getStandardCommandsWrapped(): List<CommandWrapper> {
        val result = ArrayList<CommandWrapper>()
        val list = getStandardCommands()
        for (command in list) {
            val wrapper = CommandWrapper()
            wrapper.wrapCommand(command)
            result.add(wrapper)
        }
        return result.toList()
    }

    fun getStandardCommands(): List<Command> {
        val commands = ArrayList<Command>()
        commands.add(HelpCommand(this))
        commands.add(ExitCommand(this))
        commands.add(ExecuteScriptCommand(this))
        commands.add(RegisterCommand(this))
        commands.add(LoginCommand(this))
        return commands.toList()
    }
}