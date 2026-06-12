package core

import io.IOManager
import java.security.SecureRandom

class UserManager(val io: IOManager) {
    private val db = DatabaseManager()
    private var users = ArrayList<String>()
    val tokens = HashMap<String, String>()

    fun getAllUsers(): ArrayList<String> {
        syncUsers()
        io.logger.info("Поиск всех пользователей...")
        return users
    }

    fun addUser(args: List<String>) {
        io.logger.info("Добавление пользователя...")
        db.addUser(args)
        syncUsers()
    }

    fun syncUsers() {
        io.logger.info("Синхронизация пользователей...")
        users = db.getAllUsers()
    }

    fun getSalfFromUser(login: String): String {
        syncUsers()
        io.logger.info("Поиск соли пользователя...")
        var result = ""
        if (login in getAllUsers()) {
            result = db.getSaltFromUser(login)
        }
        return result
    }

    fun getHashFromUser(login: String): String {
        syncUsers()
        io.logger.info("Поиск хэша пользователя...")
        var result = ""
        if (login in getAllUsers()) {
            result = db.getHashFromUser(login)
        }
        return result
    }

    fun addValidToken(user: String): String {
        var token: String
        do {
            token = SecureRandom.getSeed(20).decodeToString()
        } while (token in tokens.values)
        tokens[user] = token
        io.logger.info("Создан токен для пользователя $user.")
        return token
    }

    fun removeValidToken(token: String) {
        val user = getUserFromToken(token)
        if (tokens[user] == token) {
            tokens.remove(user)
        }
        io.logger.info("Удален токен для пользователя $user.")
    }

    fun getUserFromToken(token: String): String {
        return if (token in tokens.values) {
            tokens.keys.elementAt(tokens.values.indexOf(token))
        } else ""
    }
}