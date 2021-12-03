package com.ninjacontrol.krisp

typealias StringParseError = Pair<String, Int?>
typealias StringParseResult = Pair<String?, StringParseError?>

fun validateAndUnescape(string: String?): StringParseResult {

    verifyQuotes(string)?.let { error ->
        return StringParseResult(null, error)
    }
    unescape(string).let { (result, error) ->
        return when (result) {
            null -> StringParseResult(null, error)
            else -> StringParseResult(result, null)
        }
    }
}

fun validate(string: String?): StringParseError? {
    verifyQuotes(string)?.let { error ->
        return error
    }
    unescape(string).second?.let { error ->
        return error
    }
    return null
}

fun verifyQuotes(string: String?): StringParseError? {
    val unbalanceMessage = "String is unbalanced, first and last character must be a '\"'."
    fun checkUnbalanced(string: String): StringParseError? {
        var escaped = false
        string.forEachIndexed { pos, char ->
            when (char) {
                '\\' -> escaped = !escaped
                '"' -> when (escaped) {
                    true -> {
                        if (pos == string.length - 1) {
                            return StringParseError(unbalanceMessage, pos)
                        } else {
                            escaped = false
                        }
                    }
                    false -> {
                        if (pos > 0 && pos < string.length - 1) {
                            return StringParseError("Unexpected end of string.", pos)
                        }
                    }
                }
                else -> escaped = false
            }
        }
        return null
    }
    return when {
        string == null -> StringParseError("String is null.", null)
        string.length <= 1 -> StringParseError(unbalanceMessage, null)
        string.first() != '\"' -> StringParseError(unbalanceMessage, 0)
        string.last() != '\"' -> StringParseError(unbalanceMessage, string.length - 1)
        else -> checkUnbalanced(string)
    }
}

fun unescape(string: String?): StringParseResult {
    if (string == null) return StringParseResult(null, StringParseError("String is null.", null))
    val sb = StringBuilder()
    var escaped = false
    fun append(char: Char) {
        sb.append(char)
    }
    string.forEachIndexed { pos, char ->
        when (char) {
            '\\' -> when (escaped) {
                true -> {
                    append(char); escaped = false
                }
                false -> {
                    escaped = true
                }
            }
            else -> when (val transformed = unescapeAndCheck(char, escaped)) {
                null -> return StringParseResult(null, StringParseError("Invalid char.", pos))
                else -> {
                    append(transformed); escaped = false
                }
            }
        }
    }
    return StringParseResult(sb.toString(), null)
}

fun unescapeAndCheck(char: Char, escaped: Boolean): Char? {
    return when (escaped) {
        true -> when (char) {
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            '\'' -> '\''
            '\"' -> '\"'
            '\\' -> '\\'
            ' ' -> ' '
            else -> null
        }
        false -> char
    }
}

fun escape(string: String): String =
    string.map { escapeChar(it) }.joinToString(separator = "") { it }

fun escapeChar(char: Char): String {
    return when (char) {
        '\n' -> "\\n"
        '\r' -> "\\r"
        '\t' -> "\\t"
        '\"' -> "\\\""
        '\\' -> "\\\\"
        else -> char.toString()
    }
}

fun unquote(string: String?): String? = string?.removeSurrounding("\"")
