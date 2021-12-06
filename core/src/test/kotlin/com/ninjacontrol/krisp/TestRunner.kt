package com.ninjacontrol.krisp

import kotlin.system.exitProcess

fun main() {
    val tests = AllTests()
    runSuite(tests)
}

fun runSuite(tests: TestSuite) {
    var result = false
    try {
        result = tests.run()
    } catch (e: Throwable) {
        result = false
        tests.log("*** Got exception ${e.javaClass}, '${e.message}'")
    } finally {
        tests.log("--------------------")
        tests.log("Test finished: ", newline = false)
        when (result) {
            true -> {
                tests.log(tests.passText); exitProcess(0)
            }
            false -> {
                tests.log(tests.failText); exitProcess(1)
            }
        }
    }
}
