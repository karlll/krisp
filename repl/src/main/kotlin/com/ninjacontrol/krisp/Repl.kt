package com.ninjacontrol.krisp

import org.aesh.readline.Readline
import org.aesh.readline.ReadlineBuilder
import org.aesh.readline.editing.EditMode
import org.aesh.readline.editing.EditModeBuilder
import org.aesh.readline.terminal.TerminalBuilder
import org.aesh.readline.tty.terminal.TerminalConnection
import org.aesh.terminal.Terminal
import org.aesh.terminal.tty.Signal
import kotlin.system.exitProcess
import org.aesh.terminal.utils.Config as TerminalConfig

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
    options: Options,
    args: Array<String>?
) {

    args?.let {
        replExecutionEnv.set(
            symbol("*ARGV*"),
            // remove file argument from ARGV (args[0]) if we're going to evaluate a file
            if (options.file == null) it.toMalList() else it.sliceArray(1 until it.size).toMalList()
        )
    }
    if (options.withInit) {
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
        options.file != null -> evaluateFileAndExit(options.file)
        else -> {
            banner()
            mainLoop()
        }
    }
}

fun banner() {

    out(
        """
        ❄️  krisp v${BuildVersion.version}️
        """.trimIndent()
    )
}

fun printHelp() {
    val msg = """
    Usage: krisp <file> <options>
    
    options:
        --debug|-d              Debug mode
        --version|-v            Print version and quit
        --skipInit              Do not run init
        --help|-h               Print help and quit
        --input-mode=[emacs|vi] Set input mode, 'emacs' or 'vi' (default)
    """
    out(msg)
}

fun printVersion() {
    out(BuildVersion.version)
}

fun main(args: Array<String>) {
    val options = Options(
        withInit = !args.contains("--skipInit"),
        debug = args.contains("--debug") || args.contains("-d"),
        help = args.contains("--help") || args.contains("-h"),
        file = args.getOrNull(0)?.let { if (it.startsWith("-")) null else it },
        inputMode = if (args.contains("--input-mode=emacs")) InputMode.Emacs else InputMode.Vi,
        version = args.contains("--version") || args.contains("-v")
    )
    val terminalConnection = getTerminalConnection()
    terminalConnection.setSignalHandler { signal ->
        when (signal) {
            Signal.INT -> {
                out("bye!"); exitProcess(0)
            }
            else -> terminalConnection.write(signal.name)
        }
    }

    Output.target = TerminalOutput(terminalConnection)
    Input.source = TerminalInput(terminalConnection, history = true, inputMode = options.inputMode)
    when {
        options.help -> {
            printHelp()
        }
        options.version -> {
            printVersion()
        }
        else -> start(options, args)
    }
    exitProcess(0)
}

enum class InputMode {
    Vi, Emacs
}

data class Options(
    val withInit: Boolean = true,
    val debug: Boolean = false,
    val inputMode: InputMode = InputMode.Vi,
    val help: Boolean = false,
    val file: String? = null,
    val version: Boolean = false
)

fun getTerminalConnection() = TerminalConnection(getDefaultTerminal())
fun getDefaultTerminal(): Terminal = TerminalBuilder.builder()
    .name("Krisp REPL")
    .input(System.`in`)
    .output(System.out)
    .nativeSignals(true)
    .build()

class TerminalOutput(private val terminalConnection: TerminalConnection) : Out {
    override fun put(string: String) {
        terminalConnection.write(string)
    }

    override fun putLine(string: String) {
        terminalConnection.write(string + TerminalConfig.getLineSeparator())
    }
}

class TerminalInput(
    private val terminalConnection: TerminalConnection,
    history: Boolean,
    inputMode: InputMode
) : In {
    private val mode = EditModeBuilder.builder(
        if (inputMode == InputMode.Vi) EditMode.Mode.VI else EditMode.Mode.EMACS
    ).create()
    private val readline: Readline =
        ReadlineBuilder.builder()
            .enableHistory(history)
            .editMode(mode)
            .build()

    override fun readLine(): String? {
        var result: String? = null
        readline.readline(terminalConnection, prompt()) { input ->
            result = input

            terminalConnection.stopReading()
        }
        terminalConnection.openBlocking()
        return result
    }
}

object BuildVersion {
    val version: String
        get() = BuildVersion::class.java.getPackage().implementationVersion ?: "???"
}
