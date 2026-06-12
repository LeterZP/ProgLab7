package exceptions

class InvalidLoginException(private val commandName: String): Exception() {
    override val message: String? =
        when (commandName) {
            "register" -> "Пользователь с таким логином уже существует."
            "login" -> "Пользователя с таким логином не существует."
            "password" -> "Неверный пароль."
            else -> "Ошибка авторизации."
        }
}