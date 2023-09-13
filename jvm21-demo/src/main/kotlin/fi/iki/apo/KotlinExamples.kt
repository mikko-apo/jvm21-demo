package fi.iki.apo

// type inference
// immutability as first class feature
// val = immutable
// destructuring
fun main() {
    val name = nullabilityAsFirstClassFeature("Apo").replaceNull("null")
    val myData = MyData(name, 123)
    val updatedMyData = myData.copy(age = 25)
    updatedMyData.save();
    println(updatedMyData.formatMyData())
}

// function as expression, functions don't need body
// null safety as first class feature
// safe call operator .? executes the function only if caller is non null
// elvis operator ?: returns the right side if the caller is null
fun nullabilityAsFirstClassFeature(possiblyNull: String?, defaultValue: String? = null): String? =
    possiblyNull?.plus(" pow")?.plus(" POW!") ?: defaultValue

// data class automatically defines .equals()/.hashCode(), .toString(), .copy() and .componentN() for destructuring
// var = mutable variable
// default parameters
// string templates
data class MyData(val name: String, var age: Int = 21) {
    fun formatMyData() = "$name age: $age"
}

// Collection types with Immutable types as default and Mutable versions available but needing "mutable" prefix

val immutableList = listOf(1, 2, 3)
val immutableListForcedWithType: List<Int> = mutableListOf(1, 2, 3)
val immutableSet: Set<Int> = mutableSetOf(1, 2, 3)
val immutableMap: Map<String, Int> = mutableMapOf(
    "1" to 1, // to = infix function shorthand to Pair()
    "2" to 2,
    Pair("3", 3))

// extension function can be defined in a separate file, module, project
fun MyData.save() {
    println("Saving $name $age")
}

// extension function with generics, for replacing possibly null values
// elvis operator ?:
fun <O> O?.replaceNull(o: O): O {
    return this ?: o
}

// destructuring
fun destructuring(person: MyData, people: List<MyData>, dataMap: Map<String, Int>) {
    // uses componentN() functions to get values
    val (personName, personAge) = person
    println("$personName $personAge")

    // list iteration+ componentN()
    for ((name, age) in people) {
        println("$name $age")
    }

    // map iteration
    for ((key, value) in dataMap) {
        println("$key $value")
    }
}

// super-charged when
fun whenAsSwitch(name: String?) =
    when (name) {
        "Apo", "Mikko" -> 1
        null -> 2
        else -> 3
    }

fun whenWithTypeMatching(x: Any) = when (x) {
    is String -> x.startsWith("prefix")
    else -> false
}

val validNumbers = listOf(55)
fun whenWithRanges(x: Int) =
    when (x) {
        in 1..10 -> print("x is in the range")
        in validNumbers -> print("x is valid")
        !in 10..20 -> print("x is outside the range")
        else -> print("none of the above")
    }

fun Int.isEven() = this % 2 == 0
fun Int.isOdd() = !this.isEven()
fun whenWithFunctionCalls(x: Int, y: Int) =
    when {
        x.isOdd() -> print("x is odd")
        y.isEven() -> print("y is even")
        else -> print("x+y is odd")
    }

//

// Hot or not

// Lambda syntax uses just braces { } and parameter list is inside the braces
fun mapUppercase(list: List<String>) = list.map { it.uppercase() }

fun appendToName(list: List<MyData>, append: String) =
    list
        // destructuring MyData
        .map { (name, age) ->
            MyData(name + append, age)
        }
        // using copy()
        .map { person ->
            person.copy(name = person.name + append)
        }

// Only runtime Exceptions, no checked exceptions
fun throwError(): Nothing = throw Exception("Hi There!")

