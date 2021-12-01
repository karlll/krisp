package com.ninjacontrol.krisp

fun readLine(): String? = Input.readLine()

interface In {
    fun readLine(): String?
}

object StandardIn : In {
    override fun readLine(): String? = kotlin.io.readLine()
}

object Input : In {
    var source: In? = StandardIn
    override fun readLine(): String? = source?.readLine()
}
