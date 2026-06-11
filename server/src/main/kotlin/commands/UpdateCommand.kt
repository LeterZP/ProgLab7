package commands

import core.CommandInvoker
import elements.CityBuilder
import exceptions.CollectionHasNoElementException
import exceptions.InvalidAmountOfArgumentsException
import exceptions.InvalidElementValueException

/**
 * Команда для обновления элемента коллекции.
 *
 * @param ci [CommandInvoker] для [Command].
 *
 * @constructor Вызывает родительский конструктор класса [Command].
 *
 * @since 1.0
 */
class UpdateCommand(override val ci: CommandInvoker): Command(ci) {
    override val argumentsAmount: Int = CityBuilder().size
    override var validation: Boolean = false

    override fun execute(arguments: List<String>) {
        var arguments = arguments
        val id: Long
        try {
            id = arguments[0].toLong()
        } catch (_: NumberFormatException) {
            result = "${arguments[0]} не является id элемента.\n"
            ci.io.logger.warning("Элемент ${arguments[0]} не найден.")
            return
        }
        arguments = arguments.minus(arguments[0])
        val creator = CityBuilder()
        creator.strictCheck = false
        if (arguments.size != creator.size) throw InvalidAmountOfArgumentsException(this, arguments.size)
        var count = 0
        try {
            while (true) {
                creator.setField(arguments[count], count)
                if (count == creator.size-1) break
                count++
            }
            ci.cm.updateElement(creator.update(ci.cm.getElement(id)))
            result = "Элемент успешно обновлён.\n"
            ci.io.logger.info("Элемент обновлён.")
        } catch (e: InvalidElementValueException) {
            result = e.message + "\n"
            ci.io.logger.warning(e.message)
        } catch (e: CollectionHasNoElementException) {
            result = e.message + "\n"
            ci.io.logger.warning(e.message)
        }
    }

    override fun describe(): String {
        return "Обновляет значение элемента"
    }

    override fun getSyntax(): String {
        return "[id] {element}"
    }

    override fun getName(): String {
        return "update"
    }
}