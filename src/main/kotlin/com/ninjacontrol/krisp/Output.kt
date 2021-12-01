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
    var target: Out? = StandardOut
    override fun put(string: String) {
        target?.put(string)
    }

    override fun putLine(string: String) {
        target?.putLine(string)
    }
}
