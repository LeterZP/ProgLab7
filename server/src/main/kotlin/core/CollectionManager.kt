package core

import elements.City
import elements.Government
import exceptions.CollectionHasNoElementException
import io.IOManager
import java.io.IOException
import java.time.LocalDate
import java.util.Stack
import java.util.stream.Collectors

/**
 * Класс для управления коллекцией, содержащей элементы типа [City].
 *
 * @param io [IOManager], с которым взаимодействует коллекция.
 *
 * @property initializationTime Время инициализации коллекции типа [LocalDate].
 *
 * @constructor Принимает все указанные выше параметры, создавая готовый к использованию объект
 *              и сразу загружая элементы из файла.
 *
 * @since 1.0
 */
class CollectionManager(val io: IOManager) {
    private val db: DatabaseManager = DatabaseManager()
    private var collection: Stack<City> = Stack<City>()
    val initializationTime: LocalDate = LocalDate.now()

    init {
        try {
            collection = db.getAllElements()
            io.logger.info("Коллекция успешно загружена.")
        } catch (e: IOException) {
            io.logger.warning("Файл сохранения не найден.")
        }
    }

    /**
     * Считает количество элементов коллекции.
     *
     * @return Количество элементов коллекции типа [Int].
     *
     * @since 1.0
     */
    fun size(): Int {
        syncCollection()
        io.logger.info("Поиск длины коллекции...")
        return collection.stream().count().toInt()
    }

    /**
     * Добавляет элемент в коллекцию.
     *
     * @param city Город типа [City], который нужно добавить в коллекцию.
     *
     * @since 1.0
     */
    fun addElement(city: City) {
        io.logger.info("Добавление элемента...")
        db.addElement(city)
        syncCollection()
    }

    fun updateElement(city: City) {
        db.updateElement(city)
        syncCollection()
    }

    /**
     * Переворачивает коллекцию.
     *
     * @since 1.0
     */
    fun reorderElements() {
        syncCollection()
        io.logger.info("Переворачивание коллекции...")
        val newCollection: Stack<City> = Stack<City>()
        for (element in collection.asReversed()) {
            newCollection.push(element)
        }
        collection = newCollection
    }

    /**
     * Считает количество элементов коллекции, высота над уровнем моря которого выше заданного.
     *
     * @param metersAboveSeaLevel Высота над уровнем моря типа [Long].
     *
     * @return Количество элементов коллекции, подходящих по условию, типа [Int].
     *
     * @since 1.0
     */
    fun countHigherThen(metersAboveSeaLevel: Long): Int {
        syncCollection()
        io.logger.info("Поиск элементов выше заданного значения...")
        return collection.stream()
            .filter { x -> x.metersAboveSeaLevel > metersAboveSeaLevel }
            .count()
            .toInt()
    }

    /**
     * Выдаёт элемент из коллекции по [id][City.id].
     *
     * @param id [id][City.id] города [City].
     *
     * @return Элемент коллекции типа [City].
     *
     * @throws exceptions.CollectionHasNoElementException В случае, если в коллекции нет элемента с таким [id].
     *
     * @since 1.0
     */
    fun getElement(id: Long): City {
        syncCollection()
        io.logger.info("Поиск элемента...")
        try {
            return collection.stream()
                .filter { x -> x.id == id }
                .collect(Collectors.toList())[0]
        } catch (e: IndexOutOfBoundsException) {
            throw CollectionHasNoElementException(id)
        }
    }

    /**
     * Выдаёт все элементы коллекции в строковом представлении.
     *
     * @return Все элементы коллекции типа [String].
     *
     * @since 1.0
     */
    fun getAllElementsToString(): String {
        syncCollection()
        io.logger.info("Поиск всех элементов...")
        return collection.stream()
            .map { city -> city.toString() }
            .collect(Collectors.toList())
            .joinToString("\n")
    }

    /**
     * Выдаёт все виды правительств элементов коллекции в сортированном виде.
     *
     * @return [ArrayList], содержащий все виды правительств типа [Government].
     *
     * @since 1.0
     */
    fun getSortedGovernments(): List<Government?> {
        syncCollection()
        io.logger.info("Поиск всех правительств...")
        return collection.stream()
            .sorted { city1, city2 -> compareValues(city1.government, city2.government) }
            .map { city ->  city.government }
            .collect(Collectors.toList())
    }

    /**
     * Выдаёт сгруппированные имена всех городов и количество городов с одинаковым именем.
     *
     * @return [HashMap] с парами элементов имя типа [String] и количество типа [Int].
     *
     * @since 1.0
     */
    fun groupElements(): HashMap<String, Int> {
        syncCollection()
        io.logger.info("Группировка элементов по именам...")
        val names: HashMap<String, Int> = HashMap()
        val list = collection.stream()
            .map { city -> city.name }
            .collect(Collectors.toList())
        for (name in list) {
            names[name] = names[name] ?: 0
            names[name] = names[name]!! + 1
        }
        return names
    }

    /**
     * Удаляет элемент из коллекции по [id][City.id].
     *
     * @param id [id][City.id] города [City].
     *
     * @throws exceptions.CollectionHasNoElementException В случае, если в коллекции нет элемента с таким [id].
     *
     * @since 1.0
     */
    fun removeElement(id: Long, owner: String) {
        io.logger.info("Удаление элемента...")
        if (db.removeElement(id, owner) == 0) throw CollectionHasNoElementException(id)
        syncCollection()
    }

    /**
     * Удаляет последний элемент коллекции.
     *
     * @throws exceptions.CollectionHasNoElementException В случае, если коллекция пуста.
     *
     * @since 1.0
     */
    fun removeLast(owner: String) {
        syncCollection()
        val id: Long = collection.stream()
            .map { city -> city.id }
            .max { id1, id2 -> id1.compareTo(id2) }
            .get()
        io.logger.info("Удаление последнего элемента...")
        if (db.removeElement(id, owner) == 0) throw CollectionHasNoElementException(-1)
    }

    /**
     * Удаляет элементы, [id][City.id] которых больше заданного.
     *
     * @param id [id][City.id] города [City].
     *
     * @return Количество типа [Int] удалённых элементов.
     *
     * @since 1.0
     */
    fun removeGreater(id: Long, owner: String): Int {
        io.logger.info("Удаление всех элементов больше заданного...")
        val result = db.removeGreaterElements(id, owner)
        syncCollection()
        return result
    }

    /**
     * Удаляет все элементы коллекции.
     *
     * @since 1.0
     */
    fun clearCollection(owner: String) {
        io.logger.info("Отчистка коллекции...")
        db.removeGreaterElements(-1, owner)
        syncCollection()
    }

    fun syncCollection() {
        io.logger.info("Синхронизация коллекции...")
        collection = db.getAllElements()
    }
}