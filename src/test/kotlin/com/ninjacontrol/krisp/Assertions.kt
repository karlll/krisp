package com.ninjacontrol.krisp

class AssertionException(message: String, context: String? = null) :
    Throwable(context?.let { "$context: $message" } ?: message)

fun assertNeverExecuted() {
    throw AssertionException("assertion failed, should never execute!")
}

fun fail(message: String) {
    throw AssertionException("test failed: $message")
}

fun assertTrue(a: Boolean, context: String? = null) {
    if (!a) throw AssertionException(
        "assertion failed, value is false",
        context
    )
}

fun assertFalse(a: Boolean, context: String? = null) {
    if (a) throw AssertionException(
        "assertion failed, value is true",
        context
    )
}

fun assertNotNull(a: Any?, context: String? = null) {
    if (a == null) throw AssertionException("assertion failed, value is null", context)
}

fun assertNull(a: Any?, context: String? = null) {
    if (a != null) throw AssertionException("assertion failed, value is not null", context)
}

fun assertNil(a: MalType, context: String? = null) {
    if (a !is MalNil) throw AssertionException("assertion failed, $a is not NIL", context)
}

fun assertError(a: MalType, context: String? = null) {
    if (a !is MalError) throw AssertionException("assertion failed, $a is not an error", context)
}

fun assertNonError(a: MalType, context: String? = null) {
    if (a is MalError) throw AssertionException("assertion failed, $a is an error", context)
}

fun assertEqual(a: String, b: String, context: String? = null) {
    if (a != b) throw AssertionException("assertion failed, \"$a\" is not equal to \"$b\"", context)
}

fun assertEqual(a: Int, b: Int, context: String? = null) {
    if (a != b) throw AssertionException("assertion failed, $a is not equal to $b", context)
}

fun assertEqual(a: Char, b: Char, context: String? = null) {
    if (a != b) throw AssertionException("assertion failed, '$a' is not equal to '$b'", context)
}

fun assertEqual(a: MalType, b: MalType, context: String? = null) {
    if (!isEqual(a, b)) throw AssertionException("assertion failed, $a is not equal to $b", context)
}

fun assertReadEval(
    input: String,
    result: MalType,
    env: Environment = replExecutionEnv,
    context: String? = null
) {
    val ast = re(input, env)
    if (!isEqual(
            ast,
            result
        )
    ) throw AssertionException(
        "assertion failed, expected '$input' to result in $result, but was $ast instead",
        context
    )
}
