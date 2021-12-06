package com.ninjacontrol.krisp

class FunctionsTest : TestSuite {

    override val name = "Functions"

    private val tests = listOf(
        testReadEval {
            description = "Invocation with simple parameters, return string"
            input = """( (fn* (a) (pr-str "hello" a)) "world" )"""
            expectedAst = string("\"hello\" \"world\"")
        },
        testReadEval {
            description = "Invocation with simple parameters, return integer"
            input = """( (fn* (a b) (+ a b)) 10 12 )"""
            expectedAst = int(22)
        },
        testReadEval {
            description = "Invocation with simple parameters, return list"
            input = """( (fn* (a b) (list a b)) 10 12 )"""
            expectedAst = list(int(10), int(12))
        },
        testReadEval {
            description = "Invocation with variadic parameters, return list"
            input = """( (fn* (a b c d & e) e) 1 2 3 4 5 6 7 8 9 10 11 12 13 14 )"""
            expectedAst = list(
                int(5),
                int(6),
                int(7),
                int(8),
                int(9),
                int(10),
                int(11),
                int(12),
                int(13),
                int(14)
            )
        }
    )

    override fun getTests(): List<TestCase> = tests
    override fun run(): Boolean =
        verifyTests(tests)
}
