package exceptions

class NoAccessException(user: String, realUser: String): Exception() {
    override val message: String = "Пользователь $user не может редактировать элемент пользователя $realUser"
}