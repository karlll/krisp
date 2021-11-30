package com.ninjacontrol.krisp

fun print(input: MalType, printReadably: Boolean = true) =
    out(printString(input, printReadably = printReadably))

fun printString(
    form: MalType,
    printReadably: Boolean = false,
    quoted: Boolean = true,
    debug: Boolean = false
): String {

    val dbg = fun(str: () -> String) =
        when (debug) {
            true -> form.toString()
            false -> str()
        }

    return when (form) {
        is MalInteger -> dbg { form.value.toString() }
        is MalEOF -> dbg { "<EOF>" }
        is MalSymbol -> dbg { form.name }
        is MalError -> dbg { "*** ${form.message}" }
        is MalList -> dbg {
            form.items.joinToString(
                " ",
                "(",
                ")"
            ) { item -> printString(item, printReadably = printReadably, quoted = quoted) }
        }
        is MalBoolean -> dbg { form.value.toString() }
        is MalNil -> dbg { "nil" }
        is MalString -> dbg {
            when (printReadably) {
                true -> envelope(withQuotes = quoted, escape(form.value))
                false -> envelope(withQuotes = quoted, form.value)
            }
        }
        is MalKeyword -> dbg { ":${form.name}" }
        is MalVector -> dbg {
            form.items.joinToString(
                " ",
                "[",
                "]"
            ) { item -> printString(item, printReadably = printReadably, quoted = quoted) }
        }
        is MalMap -> dbg {
            form.items.entries.joinToString(
                " ", "{", "}"
            ) { (key, value) ->
                "${printString(key)} ${
                printString(
                    value,
                    printReadably = printReadably,
                    quoted = quoted
                )
                }"
            }
        }
        is MalFunction -> dbg {
            "#<fun>"
        }
        is MalFunctionContainer -> dbg {
            "#<function container>"
        }
        is MalAtom -> dbg {
            "(atom ${printString(form.value, printReadably, quoted)})"
        }
    }
}

fun envelope(withQuotes: Boolean, string: String) = when (withQuotes) {
    true -> "\"$string\""
    else -> string
}
