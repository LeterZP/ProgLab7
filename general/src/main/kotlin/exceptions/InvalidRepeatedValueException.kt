package exceptions

class InvalidRepeatedValueException(private val arg: String): Exception() {
    override val message: String = "Аргументы $arg должны быть одинаковы."
}