package com.ninjacontrol.krisp

class StringTest : TestSuite {

    override val name = "Strings"

    private val tests = listOf(

        test {
            description = "unescaping: transform strings w. escape sequences"
            verify = {
                val escaped = "foobar\\n\\r\\t"
                val unescaped = unescape(escaped).first
                assertNotNull(unescaped)
                assertEqual(unescaped!!.length, 9)
                assertEqual(unescaped[6], '\n')
                assertEqual(unescaped[7], '\r')
                assertEqual(unescaped[8], '\t')
            }
        },
        test {
            description = "unescaping: reports invalid escape sequence & position"
            verify = {
                val escaped = "foobar\\n\\m\\t"
                val (unescaped, error) = unescape(escaped)
                val (errorMessage, charPos) = error!!
                assertNull(unescaped)
                assertNotNull(error)
                assertEqual(errorMessage, "Invalid char.")
                assertEqual(charPos!!, 9)
            }
        },
        test {
            description = "escaping: produces an escaped string"
            verify = {
                val unescaped = "foobar\n\t\""
                val escaped = escape(unescaped)
                assertEqual(escaped, "foobar\\n\\t\\\"")
            }
        },
        test {
            description = "unquote: trim quotes"
            verify = {
                val quoted = "\"foobar\""
                val unquoted = "foobar"
                val result = unquote(quoted)
                assertNotNull(result)
                assertEqual(result!!, unquoted)
                assertEqual(unquote("\"\"")!!, "")
            }
        },
        test {
            description = "quotes: verify quotes"
            verify = {
                val unbalancedError =
                    "String is unbalanced, first and last character must be a '\"'."
                verifyQuotes(null)?.let { (error, pos) ->
                    assertEqual(error, "String is null.")
                    assertNull(pos)
                } ?: fail("No error returned.")
                verifyQuotes("")?.let { (error, pos) ->
                    assertEqual(error, unbalancedError)
                    assertNull(pos)
                } ?: fail("No error returned.")
                verifyQuotes("\"foobar")?.let { (error, pos) ->
                    assertEqual(error, unbalancedError)
                    assertEqual(pos!!, 6)
                } ?: fail("No error returned.")
                verifyQuotes("\"foobar\\\"")?.let { (error, pos) ->
                    assertEqual(error, unbalancedError)
                    assertEqual(pos!!, 8)
                } ?: fail("No error returned.")
                verifyQuotes("\"dpp\"djklj\"")?.let { (error, pos) ->
                    assertEqual(error, "Unexpected end of string.")
                    assertEqual(pos!!, 4)
                } ?: fail("No error returned.")

                assertNull(verifyQuotes("\"foo\""))
                assertNull(verifyQuotes("\"fo\\\"o\""))
                assertNull(verifyQuotes("\"foo=\\\"bar\\\"\""))
            }
        }

    )

    override fun getTests(): List<TestCase> = tests
    override fun run(): Boolean =
        verifyTests(tests)
}
