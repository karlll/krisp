package com.ninjacontrol.krisp

class Reader(private val tokens: Array<String>) {
    private var pos = 0
    fun next(): String? = tokens.getOrNull(pos++)
    fun peek(): String? = tokens.getOrNull(pos)
    fun skip() = pos++
    fun currentFirst() = peek()?.firstOrNull()
    fun isCurrentFirst(char: Char) = peek()?.firstOrNull()?.equals(char) ?: false
}

val tokenPattern =
    "[\\s,]*(~@|[\\[\\]{}()'`~^@]|\"(?:\\\\.|[^\\\\\"])*\"?|;.*|[^\\s\\[\\]{}('\"`,;)]*)".toRegex()

fun trimmer(char: Char) = when (char) {
    ' ', '\n', '\t', ',' -> true
    else -> false
}

fun readStr(input: String): MalType = readForm(reader = Reader(tokens = tokenize(input)))

fun tokenize(input: String) =
    tokenPattern.findAll(input).map { it.value.trim(::trimmer) }.toList().toTypedArray()

fun readForm(reader: Reader): MalType {
    return when (reader.currentFirst()) {
        '(' -> readList(reader)
        '[' -> readVector(reader)
        '{' -> readMap(reader)
        ';' -> {
            reader.skip()
            readForm(reader)
        }
        '\'' -> readQuote(reader)
        '`' -> readQuote(reader, symbol = symbol("quasiquote"))
        '~' ->
            if (reader.peek()?.let { it.getOrNull(1) == '@' } == true) {
                readQuote(reader, symbol = symbol("splice-unquote"))
            } else
                readQuote(reader, symbol = symbol("unquote"))
        '@' -> readDerefForm(reader)
        '^' -> readWithMetaForm(reader)
        null -> MalEOF
        else -> readAtom(reader)
    }
}

fun readWithMetaForm(reader: Reader): MalType {
    reader.skip()
    val metadata = readForm(reader)
    val function = readForm(reader)
    if (metadata == MalEOF || function == MalEOF) throw ParseException("Unexpected EOF")
    return list(symbol("with-meta"), function, metadata)
}

fun readDerefForm(reader: Reader): MalType {
    reader.skip()
    val list = list(symbol("deref"))
    return when (val form = readForm(reader)) {
        is MalError -> form
        is MalEOF -> throw ParseException("Unexpected EOF")
        else -> {
            list.items.add(form)
            list
        }
    }
}

fun readQuote(reader: Reader, symbol: MalSymbol = symbol("quote")): MalType {
    reader.skip()
    val list = list(symbol)
    return when (val form = readForm(reader)) {
        is MalError -> form
        is MalEOF -> throw ParseException("Unexpected EOF")
        else -> {
            list.items.add(form)
            list
        }
    }
}

fun readList(reader: Reader): MalType {
    reader.skip() // skip list start marker
    val list = MalList(mutableListOf())
    while (true) {
        if (reader.isCurrentFirst(')')) {
            reader.skip() // end marker
            return list
        } else when (val form = readForm(reader)) {
            is MalError -> return form
            is MalEOF -> throw ParseException("Unexpected EOF")
            else -> list.items.add(form)
        }
    }
}

fun readVector(reader: Reader): MalType {
    reader.skip() // skip vector start marker
    val vector = MalVector(mutableListOf())
    while (true) {
        if (reader.isCurrentFirst(']')) {
            reader.skip() // end marker
            return vector
        } else when (val form = readForm(reader)) {
            is MalError -> return form
            is MalEOF -> throw ParseException("Unexpected EOF")
            else -> vector.items.add(form)
        }
    }
}

fun readMap(reader: Reader): MalType {
    reader.skip() // skip map start marker
    val map = MalMap(mutableMapOf())
    var key: MalType? = null
    while (true) {
        if (reader.isCurrentFirst('}')) {
            return if (key == null) {
                reader.skip()
                map
            } else {
                throw NotFoundException("Missing value for key=$key")
            }
        } else when (val form = readForm(reader)) {
            is MalError -> return form
            is MalEOF -> throw ParseException("Unexpected EOF")
            else -> {
                if (key == null) {
                    key = form
                } else {
                    map.items[key] = form
                    key = null
                }
            }
        }
    }
}

fun readAtom(reader: Reader): MalType {
    return reader.next()?.let { atom ->
        when {
            atom == "nil" -> MalNil
            Atoms.integerPattern.matches(atom) -> MalInteger(atom.toInt())
            Atoms.stringPattern.matches(atom) -> {

                val (unescaped, error) = validateAndUnescape(atom)
                when (error) {
                    null -> MalString(unquote(unescaped) ?: "")
                    else -> MalError(error.first)
                }
            }
            Atoms.keywordPattern.matches(atom) -> MalKeyword(name = atom.trimStart(':'))
            Atoms.booleanPattern.matches(atom) -> MalBoolean(value = atom.toBoolean())
            else -> MalSymbol(name = atom)
        }
    } ?: MalEOF
}

class Atoms {
    companion object {
        val integerPattern = "-?\\d+".toRegex()
        val stringPattern = "\"[\\s\\S]*".toRegex()
        val keywordPattern = ":.+".toRegex()
        val booleanPattern = "true|false".toRegex()
    }
}
