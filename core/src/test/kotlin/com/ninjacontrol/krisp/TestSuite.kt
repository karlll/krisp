package com.ninjacontrol.krisp

interface TestSuite {

    val name: String

    val pass: String
        get() = "[✅]"
    val fail: String
        get() = "[❌]"
    val passText: String
        get() = "PASS"
    val failText: String
        get() = "FAIL"

    fun log(message: String, newline: Boolean = true) {
        if (newline) println(message) else print(message)
    }

    fun getTests(): List<TestCase>
    fun verifyTests(testCases: List<TestCase>): Boolean {
        return testCases.map { testCase ->
            val context = testCase.description ?: "(no description)"
            try {

                testCase.verify()
                log("$pass $context")
                true
            } catch (e: AssertionException) {
                e.message?.let { log("$fail $context: $it") } ?: log("$fail $context")
                false
            }
        }.reduce { acc, result -> acc && result }
    }

    fun run(): Boolean
}

abstract class TestCase(var description: String? = null) {
    var verify: () -> Unit = { assertNeverExecuted() }
    var only = false
}

class ReadEvalTestCase : TestCase() {
    lateinit var input: String
    lateinit var expectedAst: MalType
}

class DefaultTestCase : TestCase()

class CustomSuite(private val testCases: List<TestCase>) : TestSuite {
    override val name = "Custom"
    override fun getTests(): List<TestCase> = testCases
    override fun run(): Boolean = verifyTests(testCases)
}

fun test(case: DefaultTestCase.() -> Unit): TestCase {
    val t = DefaultTestCase()
    t.case()
    return t
}

fun testReadEval(case: ReadEvalTestCase.() -> Unit): TestCase {
    val t = ReadEvalTestCase()
    t.case()
    t.verify = { assertReadEval(input = t.input, result = t.expectedAst) }
    return t
}

inline fun <reified T : MalException> testReadEvalThrows(
    exception: T,
    crossinline case: ReadEvalTestCase.() -> Unit
): TestCase {
    val t = ReadEvalTestCase()
    var caught: Throwable? = null
    t.case()
    t.verify = {
        try {
            re(t.input, replExecutionEnv)
        } catch (e: Throwable) {
            caught = e
        }
        when {
            caught == null -> throw AssertionException("No exception thrown")
            caught !is T -> throw AssertionException("Unexpected exception, $caught")
            (caught as T).message != exception.message -> throw AssertionException("Expected exception with message '${exception.message}' but got '${(caught as T).message}' instead")
        }
    }
    return t
}
