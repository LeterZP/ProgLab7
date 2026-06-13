package core

import io.IOManager
import java.security.SecureRandom
import java.util.concurrent.locks.ReentrantLock

class UserManager(val io: IOManager) {
    private val db = DatabaseManager()
    private val lock = ReentrantLock()
    val tokens = HashMap<String, String>()

    fun getAllUsers(): ArrayList<String> {
        val users = db.getAllUsers()
        io.logger.info("Поиск всех пользователей...")
        return users
    }

    fun addUser(args: List<String>) {
        io.logger.info("Добавление пользователя...")
        db.addUser(args)
    }

    fun getSalfFromUser(login: String): String {
        io.logger.info("Поиск соли пользователя...")
        var result = ""
        if (login in getAllUsers()) {
            result = db.getSaltFromUser(login)
        }
        return result
    }

    fun getHashFromUser(login: String): String {
        io.logger.info("Поиск хэша пользователя...")
        var result = ""
        if (login in getAllUsers()) {
            result = db.getHashFromUser(login)
        }
        return result
    }

    fun addValidToken(user: String): String {
        var token: String
        lock.lock()
        try {
            do {
                token = SecureRandom.getSeed(20).decodeToString()
            } while (token in tokens.values)
            tokens[user] = token
        } finally {
            lock.unlock()
        }
        io.logger.info("Создан токен для пользователя $user.")
        return token
    }

    fun removeValidToken(token: String) {
        val user = getUserFromToken(token)
        lock.lock()
        try {
            if (tokens[user] == token) {
                tokens.remove(user)
            }
        } finally {
            lock.unlock()
        }
        io.logger.info("Удален токен для пользователя $user.")
    }

    fun getUserFromToken(token: String): String {
        lock.lock()
        try {
            return if (token in tokens.values) {
                tokens.keys.elementAt(tokens.values.indexOf(token))
            } else ""
        } finally {
            lock.unlock()
        }
    }
}