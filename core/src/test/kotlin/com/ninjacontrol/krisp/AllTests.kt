package com.ninjacontrol.krisp

class AllTests : TestSuite {

    override val name = "All tests"

    private val testSuites = listOf(
        EnvironmentTest(),
        NamespaceTest(),
        StringTest(),
        EvaluationTest(),
        FunctionsTest(),
        QuoteTest(),
        MacroTest(),
        MetadataTest()
    )

    override fun getTests() = testSuites.flatMap { testSuite -> testSuite.getTests() }

    override fun run(): Boolean {
        val only = getTests().filter { it.only }
        val suite = when {
            only.isNotEmpty() -> listOf(CustomSuite(testCases = only))
            else -> testSuites
        }
        return suite.map { testSuite ->
            log(testSuite.name)
            testSuite.run()
        }
            .reduce { acc, result -> acc && result }
    }
}
