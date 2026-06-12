package exceptions

class InvalidTokenException(val user: String): Exception() {
    override val message: String = "Невалидный токен."
}