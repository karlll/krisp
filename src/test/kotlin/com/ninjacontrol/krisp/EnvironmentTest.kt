package com.ninjacontrol.krisp

class EnvironmentTest : TestSuite {

    override val name = "Environment"

    private val tests = listOf(

        test {
            description = "Initialize environment"
            verify = {
                val env = Environment()
                assertNotNull(env)
            }
        },
        test {
            description = "Initialize environment with bindings"
            verify = {
                val env = Environment.withBindings(
                    outer = null,
                    bindings = listOf(
                        symbol("foo"),
                        symbol("bar"),
                        symbol("baz")
                    ),
                    expressions = listOf(
                        symbol("xux"),
                        symbol("bug"),
                        symbol("xug")
                    )
                )
                assertEqual(env.get(symbol("foo")), symbol("xux"))
                assertEqual(env.get(symbol("bar")), symbol("bug"))
                assertEqual(env.get(symbol("baz")), symbol("xug"))
            }
        },
        test {
            description =
                "Initialize environment with variadic parameters, 2 bindings, 3 expressions"
            verify = {
                val env = Environment.withBindings(
                    outer = null,
                    bindings = listOf(
                        symbol("foo"),
                        symbol("&"),
                        symbol("baz")
                    ),
                    expressions = listOf(
                        symbol("xux"),
                        symbol("bug"),
                        symbol("xug")
                    )
                )
                assertNotNull(env)
                assertEqual(env.get(symbol("foo")), symbol("xux"))
                assertEqual(env.get(symbol("baz")), list(symbol("bug"), symbol("xug")))
            }
        },
        test {
            description =
                "Initialize environment with variadic parameters, 2 bindings, 2 expressions"
            verify = {
                val env = Environment.withBindings(
                    outer = null,
                    bindings = listOf(
                        symbol("foo"),
                        symbol("&"),
                        symbol("baz")
                    ),
                    expressions = listOf(
                        symbol("xux"),
                        symbol("xug")
                    )
                )
                assertNotNull(env)
                assertEqual(env.get(symbol("foo")), symbol("xux"))
                assertEqual(env.get(symbol("baz")), list(symbol("xug")))
            }
        },
        test {
            description =
                "Initialize environment with variadic parameters, 5 bindings, 12 expressions"
            verify = {
                val env = Environment.withBindings(
                    outer = null,
                    bindings = listOf(
                        symbol("a"),
                        symbol("b"),
                        symbol("c"),
                        symbol("d"),
                        symbol("&"),
                        symbol("e"),
                    ),
                    expressions = listOf(
                        symbol("a1"),
                        symbol("b1"),
                        symbol("c1"),
                        symbol("d1"),
                        symbol("e1"),
                        symbol("e2"),
                        symbol("e3"),
                        symbol("e4"),
                        symbol("e5"),
                        symbol("e6"),
                        symbol("e7"),
                        symbol("e8"),
                    )
                )
                assertNotNull(env)
                assertEqual(env.get(symbol("a")), symbol("a1"))
                assertEqual(env.get(symbol("b")), symbol("b1"))
                assertEqual(env.get(symbol("c")), symbol("c1"))
                assertEqual(env.get(symbol("d")), symbol("d1"))
                assertEqual(
                    env.get(symbol("e")),
                    list(
                        symbol("e1"),
                        symbol("e2"),
                        symbol("e3"),
                        symbol("e4"),
                        symbol("e5"),
                        symbol("e6"),
                        symbol("e7"),
                        symbol("e8"),
                    )
                )
            }
        },
        test {
            description = "Add symbol"
            verify = {
                val env = Environment()
                val foo = symbol("foo")
                val bar = symbol("bar")
                env.set(foo, bar)
                assertEqual(env.get(foo), bar)
            }
        },
        test {
            description = "Find symbol in environment, symbol found"
            verify = {
                val env = Environment()
                val foo = symbol("foo")
                env.set(foo, foo)
                env.find(foo)
                assertTrue(env.find(foo) == env)
            }
        },
        test {
            description = "Find symbol in environment, symbol not found"
            verify = {
                val env = Environment()
                val foo = symbol("foo")
                env.find(foo)
                assertNull(env.find(foo))
            }
        },

    )

    override fun getTests(): List<TestCase> = tests
    override fun run(): Boolean =
        verifyTests(tests)
}
