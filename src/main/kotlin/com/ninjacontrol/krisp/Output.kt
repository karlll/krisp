package com.ninjacontrol.krisp

fun out(string: String, newLine: Boolean = true) =
    if (newLine) Output.putLine(string) else Output.put(string)

interface Out {
    fun put(string: String)
    fun putLine(string: String)
}

object StandardOut : Out {
    override fun put(string: String) {
        print(string)
    }

    override fun putLine(string: String) {
        println(string)
    }
}

object Output : Out {
    var outputter: Out? = StandardOut
    override fun put(string: String) {
        outputter?.put(string)
    }

    override fun putLine(string: String) {
        outputter?.putLine(string)
    }
}
