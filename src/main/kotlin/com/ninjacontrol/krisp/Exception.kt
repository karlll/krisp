package com.ninjacontrol.krisp

sealed class MalException(override val message: String) : Exception(message)

class ParseException(override val message: String) : MalException(message)
class InvalidArgumentException(override val message: String) : MalException(message)
class NotFoundException(override val message: String) : MalException(message)
class EvaluationException(override val message: String) : MalException(message)
class IOException(override val message: String) : MalException(message)
class OutOfBoundsException(override val message: String) : MalException(message)
class ArithmeticException(override val message: String) : MalException(message)

class UserException(val value: MalType) : MalException(printString(value))

fun Throwable.toMap() = map(
    key("type") to string(this.javaClass.simpleName),
    key("message") to (this.message?.let { string(it) } ?: MalNil)
)

fun UserException.toMap() = map(
    key("type") to string(this.javaClass.simpleName),
    key("value") to this.value
)
