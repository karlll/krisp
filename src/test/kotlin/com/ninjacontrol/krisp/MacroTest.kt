package com.ninjacontrol.krisp

class MacroTest : TestSuite {

    override val name = "Macros"

    private val tests = listOf(
        testReadEval {
            description = "defmacro: define macro"
            input = """(do (defmacro! leet (fn* () 1337)) (leet))"""
            expectedAst = int(1337)
        },
        testReadEval {
            description = "macroexpand: expand macro"
            input = """(do (defmacro! leet (fn* () 1337)) (macroexpand (leet)))"""
            expectedAst = int(1337)
        },

    )

    override fun getTests(): List<TestCase> = tests
    override fun run(): Boolean =
        verifyTests(tests)
}
