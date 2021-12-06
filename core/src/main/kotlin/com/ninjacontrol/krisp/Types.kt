package com.ninjacontrol.krisp

sealed class MalType

class MalList(val items: MutableList<MalType>) : WithMetadata, MalType() {
    val head: MalType
        get() = items.firstOrNull() ?: MalNil
    val tail: MalList
        get() = MalList(items = items.drop(1).toMutableList())
    val last: MalType
        get() = items.lastOrNull() ?: MalNil
    val size: Int
        get() = items.size

    fun isEmpty() = items.isEmpty()
    fun getOrNull(index: Int) = items.getOrNull(index)
    fun get(index: Int) = items[index]
    fun forEach(reversed: Boolean = false, function: (MalType) -> Unit) {
        when (reversed) {
            true -> items.forEach(function)
            else -> items.reversed().forEach(function)
        }
    }

    operator fun iterator(): Iterator<MalType> {
        return items.iterator()
    }

    fun subList(fromIndex: Int, toIndex: Int) = MalList(items.subList(fromIndex, toIndex))
    fun cons(item: MalType): MalList = MalList(mutableListOf(item).apply { addAll(items) })

    override var metadata: MalType? = null
        get() = field ?: MalNil
}

class MalVector(val items: MutableList<MalType>) : WithMetadata, MalType() {
    val size: Int
        get() = items.size

    fun isEmpty() = items.isEmpty()
    override var metadata: MalType? = null
        get() = field ?: MalNil
}

interface WithMetadata {
    var metadata: MalType?
}

fun MalList.asTupleList(): List<List<MalType>> =
    items.windowed(size = 2, step = 2, partialWindows = false)

fun MalList.asVector(): MalVector = MalVector(items = items)
fun MalVector.asList(): MalList = MalList(items = items)

class MalMap(val items: MutableMap<MalType, MalType>) : WithMetadata, MalType() {
    override var metadata: MalType? = null
        get() = field ?: MalNil
}

data class MalError(val message: String) : MalType()
data class MalSymbol(val name: String) : MalType()
data class MalInteger(val value: Int) : MalType()
data class MalBoolean(val value: Boolean) : MalType()
data class MalString(val value: String) : MalType()
data class MalKeyword(val name: String) : MalType()
class MalAtom(var value: MalType) : MalType()
object MalEOF : MalType()
object MalNil : MalType()

val True = MalBoolean(value = true)
val False = MalBoolean(value = false)

fun symbol(name: String) = MalSymbol(name)
fun list(vararg items: MalType): MalList = MalList(items.toMutableList())
fun map(vararg kvPair: Pair<MalType, MalType>) = MalMap(mutableMapOf(*kvPair))
fun string(value: String) = MalString(value)
fun int(value: Int) = MalInteger(value)
fun emptyList() = MalList(items = mutableListOf())
fun atom(value: MalType) = MalAtom(value)
fun key(name: String) = MalKeyword(name)
fun Array<String>.toMalList() = MalList(this.map { str -> MalString(str) }.toMutableList())

typealias Arguments = Array<MalType>
typealias FunctionBody = (args: Arguments) -> MalType

class MalFunction(val functionBody: FunctionBody) : WithMetadata, MalType() {
    fun apply(args: MalList): MalType = functionBody.invoke(args.items.toTypedArray())
    override var metadata: MalType? = null
        get() = field ?: MalNil

    override fun hashCode() = functionBody.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MalFunction

        if (functionBody != other.functionBody) return false

        return true
    }
}

data class MalFunctionContainer(
    val ast: MalType,
    val params: MalType,
    val environment: Environment,
    val isMacro: Boolean = false,
    val fn: MalFunction,
) : MalType()

operator fun MalInteger.plus(other: MalInteger): MalInteger = MalInteger(value + other.value)
operator fun MalInteger.minus(other: MalInteger): MalInteger = MalInteger(value - other.value)
operator fun MalInteger.times(other: MalInteger): MalInteger = MalInteger(value * other.value)
operator fun MalInteger.div(other: MalInteger): MalInteger = MalInteger(value / other.value)
operator fun MalInteger.rem(other: MalInteger): MalInteger = MalInteger(value % other.value)
operator fun MalInteger.compareTo(other: MalInteger): Int = when {
    value < other.value -> -1
    value > other.value -> 1
    else -> 0
}

val MalInteger.isZero
    get() = value == 0

fun MalList.duplicate() = MalList(items = this.items).apply { metadata = this.metadata }
fun MalVector.duplicate() = MalVector(items = this.items).apply { metadata = this.metadata }
fun MalMap.duplicate() = MalMap(items = this.items).apply { metadata = this.metadata }
fun MalFunction.duplicate() =
    MalFunction(functionBody = this.functionBody).apply { metadata = this.metadata }

fun MalFunctionContainer.duplicate() = MalFunctionContainer(
    ast = this.ast,
    params = this.params,
    environment = this.environment,
    isMacro = this.isMacro,
    fn = this.fn.duplicate()
).apply { fn.metadata = this.fn.metadata }
