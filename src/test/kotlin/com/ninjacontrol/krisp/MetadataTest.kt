package com.ninjacontrol.krisp

class MetadataTest : TestSuite {

    override val name = "Metadata"

    private val tests = listOf(
        testReadEval {
            description = "with-meta+meta: set metadata on user function"
            input = """(do (def! u (with-meta (fn* [x] (- 1 x)) "foo")) (meta u))"""
            expectedAst = string("foo")
        },
        testReadEval {
            description = "with-meta+meta: set metadata on list"
            input = """(do (def! u (with-meta (list 1 2 3) "foo")) (meta u))"""
            expectedAst = string("foo")
        },
        testReadEval {
            description = "with-meta+meta: duplicate function"
            input = """
                (do 
                    (def! u (with-meta (fn* [x] (- 1 x)) "foo"))
                    (meta (with-meta u "bar")) 
                    (meta u)
                )""".trimMargin()
            expectedAst = string("foo")
        }

    )

    override fun getTests(): List<TestCase> = tests
    override fun run(): Boolean =
        verifyTests(tests)
}
