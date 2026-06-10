package elements

import exceptions.InvalidElementValueException
import kotlinx.serialization.Serializable

/**
 * Человек для города [City].
 *
 * @param name
 * @param age
 * @param height
 */
@Serializable
class Human(val name: String, val age: Long, val height: Float) {
    init{
        if (name == "") throw InvalidElementValueException(name)
        if (age <= 0) throw InvalidElementValueException(age)
        if (height <= 0) throw InvalidElementValueException(height)
    }

    override fun toString(): String {
        return "$name, возраст $age, рост $height"
    }
}