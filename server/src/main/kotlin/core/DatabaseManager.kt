package core

import elements.City
import elements.CityBuilder
import org.postgresql.Driver
import java.sql.Connection
import java.util.Properties
import java.io.FileInputStream
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.Stack

class DatabaseManager(val dburl: String? = "jdbc:postgresql://localhost:5432/db", var properties: Properties? = null) {
    val driver: Driver = Driver()

    init {
        if (properties == null) {
            properties = Properties()
            properties!!.load(FileInputStream("db.cfg"))
        }
    }

    private fun getConnection(): Connection {
        return driver.connect(dburl, properties)!!
    }

    private fun parseCity(row: ResultSet): City {
        val builder = CityBuilder()
        for (i in 0..<builder.size) {
            builder.setField( row.getString(i+2) ?: "", i)
        }
        val city = builder.create()
        city.id = row.getLong(1)
        city.owner = row.getString(14)
        return city
    }

    private fun unparseCity(city: City, st: PreparedStatement) {
        st.setString(1, city.name)
        st.setInt(2, city.coordinates.x)
        st.setDouble(3, city.coordinates.y)
        st.setDouble(4, city.area)
        st.setInt(5, city.population)
        st.setLong(6, city.metersAboveSeaLevel)
        st.setFloat(7, city.populationDensity)
        st.setString(8, city.governon.name)
        st.setLong(9, city.governon.age)
        st.setFloat(10, city.governon.height)
        st.setString(11, city.climate?.toString())
        st.setString(12, city.government?.toString())
        st.setString(13, city.owner)
    }

    fun getAllElements(): Stack<City> {
        val connect = getConnection()
        val query = "SELECT * FROM city"
        val st = connect.createStatement()
        val result = st.executeQuery(query)
        connect.close()
        val collection = Stack<City>()
        while (result.next()) {
            collection.push(parseCity(result))
        }
        collection.sort()
        return collection
    }

    fun addElement(city: City): Int {
        val connect = getConnection()
        val query = "INSERT INTO city(name, coordinateX, coordinateY, area, " +
                "population, metersAboveSeaLevel, populationDensity, governonName, " +
                "governonAge, governonHeight, climate, government, owner) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::climate, ?::government, ?)"
        val st = connect.prepareStatement(query)
        unparseCity(city, st)
        val result = st.executeUpdate()
        connect.close()
        return result
    }

    fun removeElement(id: Long, owner: String): Int {
        val connect = getConnection()
        val query = "DELETE FROM city WHERE id = ? AND owner = ?"
        val st = connect.prepareStatement(query)
        st.setLong(1, id)
        st.setString(2, owner)
        val result = st.executeUpdate()
        connect.close()
        return result
    }

    fun removeGreaterElements(id: Long, owner: String): Int {
        val connect = getConnection()
        val query = "DELETE FROM city WHERE id > ? AND owner = ?"
        val st = connect.prepareStatement(query)
        st.setLong(1, id)
        st.setString(2, owner)
        val result = st.executeUpdate()
        connect.close()
        return result
    }

    fun updateElement(city: City): Int {
        val connect = getConnection()
        val query = "UPDATE city " +
                "SET name = ?, coordinateX = ?, coordinateY = ?, area = ?, " +
                "population = ?, metersAboveSeaLevel = ?, populationDensity = ?, governonName = ?, " +
                "governonAge = ?, governonHeight = ?, climate = ?::climate, government = ?::government, owner = ? " +
                "WHERE id = ?"
        val st = connect.prepareStatement(query)
        unparseCity(city, st)
        st.setLong(14, city.id)
        val result = st.executeUpdate()
        connect.close()
        return result
    }

    fun getAllUsers(): ArrayList<String> {
        val connect = getConnection()
        val query = "SELECT login FROM \"user\""
        val st = connect.createStatement()
        val result = st.executeQuery(query)
        connect.close()
        val list = ArrayList<String>()
        while (result.next()) {
            list.add(result.getString(1))
        }
        list.sort()
        return list
    }

    fun addUser(args: List<String>): Int {
        val connect = getConnection()
        val query = "INSERT INTO \"user\" VALUES (?, ?, ?)"
        val st = connect.prepareStatement(query)
        st.setString(1, args[0])
        st.setString(2, args[1])
        st.setString(3, args[2])
        val result = st.executeUpdate()
        connect.close()
        return result
    }

    fun getSaltFromUser(login: String): String {
        val connect = getConnection()
        val query = "SELECT salt FROM \"user\" WHERE login = ?"
        val st = connect.prepareStatement(query)
        st.setString(1, login)
        val resultSet = st.executeQuery()
        val result: String
        if (resultSet.next()) result = resultSet.getString(1)
        else result = ""
        connect.close()
        return result
    }

    fun getHashFromUser(login: String): String {
        val connect = getConnection()
        val query = "SELECT password FROM \"user\" WHERE login = ?"
        val st = connect.prepareStatement(query)
        st.setString(1, login)
        val resultSet = st.executeQuery()
        val result: String
        if (resultSet.next()) result = resultSet.getString(1)
        else result = ""
        connect.close()
        return result
    }
}