package com.ninjacontrol.krisp

class EvaluationTest : TestSuite {

    override val name = "Evaluation and special forms"

    private val tests = listOf(
        testReadEval {
            description = "if: evaluates condition, true"
            input = """(if true (+ 10 10) false)"""
            expectedAst = int(20)
        },

        testReadEval {
            description = "if: evaluates condition, false"
            input = """(if false true (str "foobar"))"""
            expectedAst = string("foobar")
        },

        testReadEval {
            description = "let*: bind symbol"
            input = """(let* (c 2) c)"""
            expectedAst = int(2)
        },
        testReadEval {
            description = "let*: functions in bindings"
            input = """(let* [foo (+ 20 13) bar (+ 2 foo)] (+ foo bar))"""
            expectedAst = int(68)
        },
        testReadEval {
            description = "let*+do+def!: using outer environment"
            input = """(do (def! X 24) (let* (Y 12) (let* (Z 39) X)))"""
            expectedAst = int(24)
        },
        testReadEvalThrows(NotFoundException("Symbol 'badSymbol' not found")) {
            description = "do: catch error while evaluating"
            input = """(do (+ 1 2) badSymbol true)"""
        },
        testReadEval {
            description = "def+let+fn: closures nests environments"
            input = """(do (def! f (let* (x 10) (fn* () x))) (def! x 11) (f))"""
            expectedAst = int(10)
        }

    )

    override fun getTests(): List<TestCase> = tests
    override fun run(): Boolean =
        verifyTests(tests)
}
