package com.ninjacontrol.krisp

class QuoteTest : TestSuite {

    override val name = "Quoting"

    private val tests = listOf(
        testReadEval {
            description = "quote: return list"
            input = """(quote (1 2 3))"""
            expectedAst = list(int(1), int(2), int(3))
        },
        testReadEval {
            description = "quote: shorthand"
            input = """'(1 2 3)"""
            expectedAst = list(int(1), int(2), int(3))
        },
        testReadEval {
            description = "quote: as parameter"
            input = """(str 'abc)"""
            expectedAst = string("abc")
        },
        testReadEval {
            description = "quote: in `def!`"
            input = """(do (def! u '(1 2 3)) u)"""
            expectedAst = list(int(1), int(2), int(3))
        },
        testReadEval {
            description = "quasiquote: quoted"
            input = """(do (def! l '(i j)) (quasiquote (x l y)))"""
            expectedAst = list(symbol("x"), symbol("l"), symbol("y"))
        },
        testReadEval {
            description = "quasiquote: unquoted"
            input = """(do (def! ulon '(i j)) (quasiquote (x (unquote ulon) y)))"""
            expectedAst = list(symbol("x"), list(symbol("i"), symbol("j")), symbol("y"))
        },
        testReadEval {
            description = "quasiquote: splice-unquoted"
            input = """(do (def! ulon '(i j)) (quasiquote (x (splice-unquote ulon) y)))"""
            expectedAst = list(symbol("x"), symbol("i"), symbol("j"), symbol("y"))
        },
        testReadEval {
            description = "quasiquote: splice-unquoted, shorthand"
            input = """(do (def! ulon '(i j)) `(x ~@ulon y))"""
            expectedAst = list(symbol("x"), symbol("i"), symbol("j"), symbol("y"))
        },
        testReadEval {
            description = "quasiquote: shorthand"
            input = """`7"""
            expectedAst = int(7)
        },
        testReadEval {
            description = "quasiquote: nested list"
            input = """(quasiquote (1 2 (3 4)))"""
            expectedAst = list(int(1), int(2), list(int(3), int(4)))
        }

    )

    override fun getTests(): List<TestCase> = tests
    override fun run(): Boolean =
        verifyTests(tests)
}
