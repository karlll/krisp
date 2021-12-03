package com.ninjacontrol.krisp

class TokenReader(private val tokens: Array<String>) {
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

fun readStr(input: String): MalType = readForm(tokenReader = TokenReader(tokens = tokenize(input)))

fun tokenize(input: String) =
    tokenPattern.findAll(input).map { it.value.trim(::trimmer) }.toList().toTypedArray()

fun readForm(tokenReader: TokenReader): MalType {
    return when (tokenReader.currentFirst()) {
        '(' -> readList(tokenReader)
        '[' -> readVector(tokenReader)
        '{' -> readMap(tokenReader)
        ';' -> {
            tokenReader.skip()
            readForm(tokenReader)
        }
        '\'' -> readQuote(tokenReader)
        '`' -> readQuote(tokenReader, Symbols.quasiquote)
        '~' ->
            if (tokenReader.peek()?.let { it.getOrNull(1) == '@' } == true) {
                readQuote(tokenReader, Symbols.`splice-unquote`)
            } else
                readQuote(tokenReader, Symbols.unquote)
        '@' -> readDerefForm(tokenReader)
        '^' -> readWithMetaForm(tokenReader)
        null -> MalEOF
        else -> readAtom(tokenReader)
    }
}

fun readWithMetaForm(tokenReader: TokenReader): MalType {
    tokenReader.skip()
    val metadata = readForm(tokenReader)
    val function = readForm(tokenReader)
    if (metadata == MalEOF || function == MalEOF) throw ParseException("Unexpected EOF")
    return list(Symbols.`with-meta`, function, metadata)
}

fun readDerefForm(tokenReader: TokenReader): MalType {
    tokenReader.skip()
    val list = list(Symbols.deref)
    return when (val form = readForm(tokenReader)) {
        is MalError -> form
        is MalEOF -> throw ParseException("Unexpected EOF")
        else -> {
            list.items.add(form)
            list
        }
    }
}

fun readQuote(tokenReader: TokenReader, symbol: MalSymbol = Symbols.quote): MalType {
    tokenReader.skip()
    val list = list(symbol)
    return when (val form = readForm(tokenReader)) {
        is MalError -> form
        is MalEOF -> throw ParseException("Unexpected EOF")
        else -> {
            list.items.add(form)
            list
        }
    }
}

fun readList(tokenReader: TokenReader): MalType {
    tokenReader.skip() // skip list start marker
    val list = MalList(mutableListOf())
    while (true) {
        if (tokenReader.isCurrentFirst(')')) {
            tokenReader.skip() // end marker
            return list
        } else when (val form = readForm(tokenReader)) {
            is MalError -> return form
            is MalEOF -> throw ParseException("Unexpected EOF")
            else -> list.items.add(form)
        }
    }
}

fun readVector(tokenReader: TokenReader): MalType {
    tokenReader.skip() // skip vector start marker
    val vector = MalVector(mutableListOf())
    while (true) {
        if (tokenReader.isCurrentFirst(']')) {
            tokenReader.skip() // end marker
            return vector
        } else when (val form = readForm(tokenReader)) {
            is MalError -> return form
            is MalEOF -> throw ParseException("Unexpected EOF")
            else -> vector.items.add(form)
        }
    }
}

fun readMap(tokenReader: TokenReader): MalType {
    tokenReader.skip() // skip map start marker
    val map = MalMap(mutableMapOf())
    var key: MalType? = null
    while (true) {
        if (tokenReader.isCurrentFirst('}')) {
            return if (key == null) {
                tokenReader.skip()
                map
            } else {
                throw NotFoundException("Missing value for key=$key")
            }
        } else when (val form = readForm(tokenReader)) {
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

fun readAtom(tokenReader: TokenReader): MalType {
    return tokenReader.next()?.let { atom ->
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
