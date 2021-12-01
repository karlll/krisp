package com.ninjacontrol.krisp

import kotlin.system.exitProcess

val replExecutionEnv = Environment().apply {
    add(namespace)
    set(
        Symbols.eval,
        func { args ->
            eval(args[0], this)
        }
    )
    set(symbol("*host-language*"), string("kotlin"))
}
val init = listOf(
    """(def! load-file (fn* (f) (eval (read-string (str "(do " (slurp f) "\nnil)")))))""",
    """(defmacro! cond (fn* (& xs) (if (> (count xs) 0) (list 'if (first xs) (if (> (count xs) 1) (nth xs 1) (throw "odd number of forms to cond")) (cons 'cond (rest (rest xs)))))))""",
)

tailrec fun mainLoop() {
    out(prompt(), newLine = false)
    readLine()?.let { input ->
        try {
            rep(input, replExecutionEnv)
        } catch (e: MalException) {
            out("*** ${e.message}")
        } catch (e2: Throwable) {
            out("*** Error: $e2")
            out("*** Aborting")
            exitProcess(1)
        }
    } ?: run {
        out("** Exiting.")
        exitProcess(0)
    }
    mainLoop()
}

fun re(input: String, env: Environment) = eval(read(input), env = env)
fun rep(input: String, env: Environment) = print(re(input, env = env))

fun evaluateFileAndExit(file: String) {
    val expression = "(load-file \"$file\")"
    try {
        val result = re(expression, replExecutionEnv)
        printString(result)
        exitProcess(0)
    } catch (e: Throwable) {
        out("*** Error: ${e.message ?: "unknown error"}")
        exitProcess(1)
    }
}

fun start(
    file: String? = null,
    withInit: Boolean = true,
    args: Array<String>?
) {

    args?.let {
        replExecutionEnv.set(
            symbol("*ARGV*"),
            // remove file argument from ARGV (args[0]) if we're going to evaluate a file
            if (file == null) it.toMalList() else it.sliceArray(1 until it.size).toMalList()
        )
    }
    if (withInit) {
        init.forEach { expression ->
            try {
                re(expression, replExecutionEnv)
            } catch (e: Throwable) {
                out("*** Init failed (${e.message})")
                exitProcess(1)
            }
        }
    }
    when {
        file != null -> evaluateFileAndExit(file)
        else -> {
            banner()
            mainLoop()
        }
    }
}

fun banner() {

    val versionString = "0.0.1"
    val bannerExpression = "(println \"krisp v$versionString \")"

    try {
        rep(bannerExpression, replExecutionEnv)
    } catch (e: Throwable) {
        out("*** (${e.message})")
        exitProcess(1)
    }
}

fun printHelp() {
    out("Usage: krisp <file> <options>")
    out("")
    out("If present, load and evaluate file then quit, otherwise starts REPL.")
    out("")
    out("options:")
    out("--skipInit\t\t\tDo not run init")
    out("--help|-h\t\t\t\tPrint help and quit")
}

fun main(args: Array<String>) {
    val file = args.getOrNull(0)?.let { if (it.startsWith("-")) null else it }
    val withInit = !args.contains("--skipInit")
    val printHelp = args.contains("--help") || args.contains("-h")
    Output.target = StandardOut
    Input.source = StandardIn
    when {
        printHelp -> printHelp()
        else -> start(file, withInit, args)
    }
}
