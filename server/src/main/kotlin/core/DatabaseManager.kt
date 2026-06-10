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
                "governonAge, governonHeight, climate, government) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::climate, ?::government)"
        val st = connect.prepareStatement(query)
        unparseCity(city, st)
        val result = st.executeUpdate()
        connect.close()
        return result
    }

    fun removeElement(id: Long): Int {
        val connect = getConnection()
        val query = "DELETE FROM city WHERE id = ?"
        val st = connect.prepareStatement(query)
        st.setLong(1, id)
        val result = st.executeUpdate()
        connect.close()
        return result
    }

    fun removeGreaterElements(id: Long): Int {
        val connect = getConnection()
        val query = "DELETE FROM city WHERE id > ?"
        val st = connect.prepareStatement(query)
        st.setLong(1, id)
        val result = st.executeUpdate()
        connect.close()
        return result
    }
}