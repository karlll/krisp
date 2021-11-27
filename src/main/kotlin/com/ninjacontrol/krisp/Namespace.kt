package com.ninjacontrol.krisp

import java.io.File
import java.nio.charset.Charset

val namespace: EnvironmentMap = mutableMapOf(
    symbol("+") to arithmeticFunction(ArithmeticOperation.Add),
    symbol("-") to arithmeticFunction(ArithmeticOperation.Subtract),
    symbol("*") to arithmeticFunction(ArithmeticOperation.Multiply),
    symbol("/") to arithmeticFunction(ArithmeticOperation.Divide),
    symbol("%") to arithmeticFunction(ArithmeticOperation.Modulo),
    symbol("prn") to prn(),
    symbol("pr-str") to pr_str(),
    symbol("str") to str(),
    symbol("println") to println(),
    symbol("list") to list(),
    symbol("list?") to `list?`(),
    symbol("empty?") to `empty?`(),
    symbol("first") to first(),
    symbol("rest") to rest(),
    symbol("nth") to nth(),
    symbol("count") to count(),
    symbol("=") to eq(),
    symbol(">") to gt(),
    symbol(">=") to gte(),
    symbol("<") to lt(),
    symbol("<=") to lte(),
    symbol("not") to not(),
    symbol("read-string") to `read-string`(),
    symbol("slurp") to slurp(),
    symbol("atom") to atom(),
    symbol("deref") to deref(),
    symbol("atom?") to `atom?`(),
    symbol("reset!") to reset(),
    symbol("swap!") to swap(),
    symbol("cons") to cons(),
    symbol("concat") to concat(),
    symbol("vec") to vec(),
    symbol("throw") to `throw`(),
    symbol("apply") to apply(),
    symbol("map") to map(),
    symbol("true?") to `true?`(),
    symbol("false?") to `false?`(),
    symbol("nil?") to `nil?`(),
    symbol("symbol?") to `symbol?`(),
    symbol("symbol") to symbol(),
    symbol("keyword") to keyword(),
    symbol("vector?") to `vector?`(),
    symbol("number?") to `number?`(),
    symbol("map?") to `map?`(),
    symbol("string?") to `string?`(),
    symbol("fn?") to `fn?`(),
    symbol("macro?") to `macro?`(),
    symbol("keyword?") to `keyword?`(),
    symbol("sequential?") to `sequential?`(),
    symbol("vector") to vector(),
    symbol("hash-map") to `hash-map`(),
    symbol("assoc") to assoc(),
    symbol("dissoc") to dissoc(),
    symbol("get") to get(),
    symbol("vals") to vals(),
    symbol("keys") to keys(),
    symbol("contains?") to `contains?`(),
    symbol("readline") to readLineWithPrompt(),
    symbol("meta") to meta(),
    symbol("with-meta") to `with-meta`(),
    symbol("time-ms") to `time-ms`(),
    symbol("seq") to seq(),
    symbol("conj") to conj()

)

fun func(precondition: ((Arguments) -> Unit)? = null, function: FunctionBody): MalFunction =
    MalFunction { args ->
        precondition?.invoke(args)
        function.invoke(args)
    }

inline fun <reified T> isArgumentType(args: Arguments) = args.all { arg -> arg is T }
inline fun <reified T, reified U> isArgumentEitherType(args: Arguments) =
    args.all { arg -> arg is T || arg is U }

inline fun <reified T> isArgumentNotType(args: Arguments) = args.none { arg -> arg is T }
fun assertNumberOfArguments(args: Arguments, amount: Int) = args.size == amount
fun assertNumberOfArgumentsOrMore(args: Arguments, amount: Int) = args.size >= amount

fun functionOfArity(n: Int, function: FunctionBody): MalFunction =
    func(
        precondition = { args ->
            if (!assertNumberOfArguments(args, n)) {
                throw InvalidArgumentException("Invalid number of arguments, expected $n instead of ${args.size}.")
            }
        }
    ) { args ->
        function.invoke(args)
    }

fun functionOfAtLeastArity(n: Int, function: FunctionBody): MalFunction =
    func(
        precondition = { args ->
            if (!assertNumberOfArgumentsOrMore(args, n)) {
                throw InvalidArgumentException("Invalid number of arguments, expected at least $n arguments, got ${args.size}.")
            }
        }
    ) { args ->
        function.invoke(args)
    }

inline fun <reified T> typedArgumentFunction(
    arity: Int = -1,
    minArity: Int = -1,
    crossinline function: FunctionBody
): MalFunction = func(
    precondition = { args ->
        when {
            (
                arity > 0 && !assertNumberOfArguments(
                    args,
                    arity
                )
                ) -> throw InvalidArgumentException("Invalid number of arguments, expected $arity arguments, got ${args.size}.")
            (
                minArity > 0 && !assertNumberOfArgumentsOrMore(
                    args,
                    minArity
                )
                ) -> throw InvalidArgumentException("Invalid number of arguments, expected at least $minArity arguments, got ${args.size}.")
            !isArgumentType<T>(args) -> throw InvalidArgumentException("Invalid argument type, ${T::class} expected")
        }
    }
) { args ->
    function.invoke(args)
}

fun integerFunction(function: FunctionBody): MalFunction = func(
    precondition = { args ->
        if (!isArgumentType<MalInteger>(args)) {
            throw InvalidArgumentException("Invalid argument type, expected an integer")
        }
    }
) { args ->
    function.invoke(args)
}

fun integerFunctionOfArity(n: Int, function: FunctionBody): MalFunction = func(
    precondition = { args ->
        when {
            args.size != n -> throw InvalidArgumentException("Invalid number of arguments, expected $n instead of ${args.size}.")
            !isArgumentType<MalInteger>(args) -> throw InvalidArgumentException("Invalid argument type, expected an integer")
        }
    }
) { args ->
    function.invoke(args)
}

fun stringFunctionOfArity(n: Int, function: FunctionBody): MalFunction = func(
    precondition = { args ->
        when {
            args.size != n -> throw InvalidArgumentException("Invalid number of arguments, expected $n instead of ${args.size}.")
            !isArgumentType<MalString>(args) -> throw InvalidArgumentException("Invalid argument type, expected a string")
        }
    }
) { args ->
    function.invoke(args)
}

/* Printing */

fun prn() = func { args ->
    val string = args.joinToString(separator = " ") {
        printString(
            it,
            printReadably = true
        )
    }
    out(string)
    MalNil
}

fun pr_str() = func { args ->
    val string =
        args.joinToString(separator = " ") {
            printString(
                it,
                printReadably = true,
                quoted = true
            )
        }
    MalString(value = string)
}

fun str() = func { args ->
    val string =
        args.joinToString(separator = "") {
            printString(
                it,
                printReadably = false,
                quoted = false
            )
        }
    MalString(value = string)
}

fun println() = func { args ->
    val string = args.joinToString(separator = " ") {
        printString(
            it,
            printReadably = false,
            quoted = false
        )
    }
    out(string)
    MalNil
}

/* Comparison */

enum class ComparisonOperation {
    GreaterThan,
    GreaterThanOrEqual,
    LessThan,
    LessThanOrEqual
}

fun eq() = functionOfArity(2) { MalBoolean(isEqual(it[0], it[1])) }
fun gt() = integerFunctionOfArity(2) {
    compare(it[0] as MalInteger, it[1] as MalInteger, ComparisonOperation.GreaterThan)
}

fun gte() = integerFunctionOfArity(2) {
    compare(
        it[0] as MalInteger,
        it[1] as MalInteger,
        ComparisonOperation.GreaterThanOrEqual
    )
}

fun lt() = integerFunctionOfArity(2) {
    compare(it[0] as MalInteger, it[1] as MalInteger, ComparisonOperation.LessThan)
}

fun lte() = integerFunctionOfArity(2) {
    compare(it[0] as MalInteger, it[1] as MalInteger, ComparisonOperation.LessThanOrEqual)
}

fun compare(a: MalInteger, b: MalInteger, operation: ComparisonOperation): MalBoolean {
    return when (operation) {
        ComparisonOperation.GreaterThan -> MalBoolean((a > b))
        ComparisonOperation.GreaterThanOrEqual -> MalBoolean((a >= b))
        ComparisonOperation.LessThan -> MalBoolean((a < b))
        ComparisonOperation.LessThanOrEqual -> MalBoolean((a <= b))
    }
}

fun not() = functionOfArity(1) {
    when (val arg = it[0]) {
        is MalNil -> True
        is MalBoolean -> MalBoolean(!arg.value)
        else -> False
    }
}

/* Lists */

fun list() = func { MalList(it.toMutableList()) }
fun `list?`() = functionOfArity(1) { args ->
    when (args[0]) {
        is MalList -> True
        else -> False
    }
}

fun `empty?`() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalList -> if (arg.isEmpty()) True else False
        is MalVector -> if (arg.isEmpty()) True else False
        else -> throw InvalidArgumentException("Argument is not a list nor a vector")
    }
}

fun count() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalNil -> MalInteger(0)
        is MalList -> MalInteger(arg.size)
        is MalVector -> MalInteger(arg.size)
        else -> throw InvalidArgumentException("Argument is not a list nor a vector")
    }
}

fun first() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalNil -> MalNil
        is MalList, is MalVector -> {
            when (arg) {
                is MalVector -> if (arg.isEmpty()) MalNil else arg.items[0]
                else -> if ((arg as MalList).isEmpty()) MalNil else arg.head
            }
        }
        else -> throw InvalidArgumentException("Argument is not a list")
    }
}

fun rest() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalNil -> emptyList()
        is MalList, is MalVector -> {
            when (arg) {
                is MalVector -> if (arg.isEmpty()) emptyList() else MalList(
                    items = arg.items.drop(1).toMutableList()
                )
                else -> if ((arg as MalList).isEmpty()) emptyList() else arg.tail
            }
        }
        else -> throw InvalidArgumentException("Argument is not a list nor a vector")
    }
}

fun nth() = functionOfArity(2) { args ->
    when {
        (args[0] !is MalList && args[0] !is MalVector) -> throw InvalidArgumentException("Argument is not a list nor a vector")
        (args[1] !is MalInteger) -> throw InvalidArgumentException("Argument is not an integer")

        else -> {
            val index = (args[1] as MalInteger).value
            when {
                args[0] is MalList -> {
                    (args[0] as MalList).items.getOrNull(index)
                        ?: throw OutOfBoundsException("Index out of bounds")
                }
                else -> {
                    (args[0] as MalVector).items.getOrNull(index)
                        ?: throw OutOfBoundsException("Index out of bounds")
                }
            }
        }
    }
}

fun cons() = functionOfArity(2) { args ->
    when (val arg = args[1]) {
        is MalList -> arg.cons(args[0])
        is MalVector -> MalList(arg.items).run { cons(args[0]) }
        else -> throw InvalidArgumentException("Argument is not a list nor a vector")
    }
}

fun concat() = func { args ->
    when (isArgumentEitherType<MalList, MalVector>(args)) {
        true -> MalList(
            args.flatMap {
                when (it) {
                    is MalVector -> it.items
                    else -> (it as MalList).items
                }
            }.toMutableList()
        )
        else -> throw InvalidArgumentException("Argument is not a list nor a vector")
    }
}

fun apply() = functionOfAtLeastArity(2) { args ->
    if (args[0] !is MalFunctionContainer && args[0] !is MalFunction) throw InvalidArgumentException(
        "Expected a function"
    )
    if ((args.last() !is MalList) && (args.last() !is MalVector)) throw InvalidArgumentException("Argument is not a list nor a vector")
    val function = when (args[0]) {
        is MalFunctionContainer -> (args[0] as MalFunctionContainer).fn
        else -> (args[0] as MalFunction)
    }
    val argList = when (val last = args.last()) {
        is MalList -> last.items
        else -> (last as MalVector).items
    }
    val otherArgs = args.slice(1..args.size - 2)
    val functionArgs = MalList((otherArgs + argList).toMutableList())
    function.apply(functionArgs)
}

fun map() = functionOfArity(2) { args ->
    if (args[0] !is MalFunctionContainer && args[0] !is MalFunction) throw InvalidArgumentException(
        "Expected a function"
    )
    if (args[1] !is MalList && args[1] !is MalVector) throw InvalidArgumentException("Argument is not a list nor a vector")
    val function = when (args[0]) {
        is MalFunctionContainer -> (args[0] as MalFunctionContainer).fn
        else -> (args[0] as MalFunction)
    }
    val argList = when (args[1]) {
        is MalList -> (args[1] as MalList).items
        else -> (args[1] as MalVector).items
    }
    MalList(items = argList.map { item -> function.apply(list(item)) }.toMutableList())
}

/* Vectors */

fun vec() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalList -> MalVector(items = arg.items)
        is MalVector -> arg
        else -> throw InvalidArgumentException("Argument is not a list nor a vector")
    }
}

/* Arithmetic functions */

enum class ArithmeticOperation {
    Add,
    Subtract,
    Multiply,
    Divide,
    Modulo
}

fun arithmeticFunction(operation: ArithmeticOperation) = integerFunction { args ->
    if (operation == ArithmeticOperation.Divide && args.drop(1)
        .any { (it as MalInteger).isZero }
    ) {
        throw ArithmeticException("Division by zero")
    }
    args.reduce { acc, arg ->
        val op1 = acc as MalInteger
        val op2 = arg as MalInteger
        when (operation) {
            ArithmeticOperation.Add -> op1 + op2
            ArithmeticOperation.Subtract -> op1 - op2
            ArithmeticOperation.Multiply -> op1 * op2
            ArithmeticOperation.Divide -> op1 / op2
            ArithmeticOperation.Modulo -> op1 % op2
        }
    }
}

/* Input */

fun `read-string`() = stringFunctionOfArity(1) { args ->
    readStr((args[0] as MalString).value)
}

fun slurp() = stringFunctionOfArity(1) { args ->
    val fileName = args[0] as MalString
    readFileAsString(fileName.value, Charsets.UTF_8)
}

fun readFileAsString(fileName: String, charSet: Charset): MalType {
    val file = File(fileName)
    return when {
        !file.exists() -> throw IOException("File \"$fileName\" does not exist")
        !file.canRead() -> throw IOException("Can not read \"$fileName\"")
        file.length() > ((2L * 1024 * 1024 * 1024) - 1) -> throw IOException("File is too large")
        else -> MalString(file.readText(charSet))
    }
}

fun readLineWithPrompt() = typedArgumentFunction<MalString>(arity = 1) { args ->
    val prompt = args[0] as MalString
    out(prompt.value)
    when (val input = readLine()) {
        null -> MalNil
        else -> string(input)
    }
}

/* Atom */

fun atom() = functionOfArity(1) { args ->
    MalAtom(args[0])
}

fun deref() = typedArgumentFunction<MalAtom>(arity = 1) { args ->
    val atom = args[0] as MalAtom
    atom.value
}

fun reset() = functionOfArity(2) { args ->
    when {
        (args[0] !is MalAtom) -> throw InvalidArgumentException("Not an atom")
        else -> {
            val atom = args[0] as MalAtom
            atom.value = args[1]
            atom.value
        }
    }
}

fun swap() = functionOfAtLeastArity(2) { args ->
    when {
        (args[0] !is MalAtom) -> throw InvalidArgumentException("Argument is not an atom")
        ((args[1] !is MalFunctionContainer) && (args[1] !is MalFunction)) -> throw InvalidArgumentException(
            "Argument is not a function nor a function expression"
        )
        else -> {
            val atom = args[0] as MalAtom
            val swapFunction = when (args[1]) {
                is MalFunctionContainer -> (args[1] as MalFunctionContainer).fn
                else -> (args[1] as MalFunction)
            }
            val additionalArgs =
                if (args.size > 2) args.sliceArray(2 until args.size) else emptyArray()
            val swapFunctionArgs = list(atom.value, *additionalArgs)
            when (val newValue = swapFunction.apply(swapFunctionArgs)) {
                is MalError -> newValue
                else -> {
                    atom.value = newValue
                    atom.value
                }
            }
        }
    }
}

/* Exceptions */

fun `throw`() = functionOfArity(1) { args ->
    throw UserException(args[0])
}

/* Predicates */

fun `nil?`() = functionOfArity(1) { args -> if (args[0] eq MalNil) True else False }
fun `true?`() = functionOfArity(1) { args -> if (args[0] eq True) True else False }
fun `false?`() = functionOfArity(1) { args -> if (args[0] eq False) True else False }
fun `symbol?`() = functionOfArity(1) { args -> if (args[0] is MalSymbol) True else False }
fun `atom?`() = functionOfArity(1) { args -> if (args[0] is MalAtom) True else False }
fun `vector?`() = functionOfArity(1) { args -> if (args[0] is MalVector) True else False }
fun `string?`() = functionOfArity(1) { args -> if (args[0] is MalString) True else False }
fun `number?`() = functionOfArity(1) { args -> if (args[0] is MalInteger) True else False }
fun `map?`() = functionOfArity(1) { args -> if (args[0] is MalMap) True else False }
fun `fn?`() =
    functionOfArity(1) { args ->
        when (val arg = args[0]) {
            is MalFunctionContainer -> if (arg.isMacro) False else True
            else -> if (arg is MalFunction) True else False
        }
    }

fun `sequential?`() =
    functionOfArity(1) { args -> if (args[0] is MalList || args[0] is MalVector) True else False }

fun `keyword?`() = functionOfArity(1) { args -> if (args[0] is MalKeyword) True else False }

fun `macro?`() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalFunctionContainer -> if (arg.isMacro) True else False
        else -> False
    }
}

fun symbol() = typedArgumentFunction<MalString>(arity = 1) { args ->
    MalSymbol((args[0] as MalString).value)
}

fun keyword() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalString -> MalKeyword((arg as MalString).value)
        is MalKeyword -> arg
        else -> throw InvalidArgumentException("Expected a string or a keyword argument")
    }
}

fun vector() = func { args ->
    MalVector(items = args.toMutableList())
}

/*  Maps */

fun `hash-map`() = func { args ->
    if (args.size.mod(2) != 0) throw InvalidArgumentException("Expected an even number of arguments")
    val kvMap = mutableMapOf<MalType, MalType>()
    args.asList().windowed(size = 2, step = 2, partialWindows = false).forEach { entry ->
        kvMap[entry[0]] = entry[1]
    }
    MalMap(items = kvMap)
}

fun assoc() = functionOfAtLeastArity(2) { args ->
    if (args[0] !is MalMap) throw InvalidArgumentException("Expected first argument to be a map")
    if ((args.size - 1).mod(2) != 0) throw InvalidArgumentException("Expected an even number of arguments following the first argument")
    val newMap = mutableMapOf<MalType, MalType>().apply { putAll(from = (args[0] as MalMap).items) }
    args.asList().drop(1).windowed(size = 2, step = 2, partialWindows = false).forEach { entry ->
        newMap[entry[0]] = entry[1]
    }
    MalMap(items = newMap)
}

fun dissoc() = functionOfAtLeastArity(2) { args ->
    if (args[0] !is MalMap) throw InvalidArgumentException("Expected first argument to be a map")
    val newMap = mutableMapOf<MalType, MalType>().apply { putAll(from = (args[0] as MalMap).items) }
    args.asList().drop(1).forEach { key ->
        newMap.remove(key)
    }
    MalMap(items = newMap)
}

fun get() = functionOfArity(2) { args ->
    when {
        (args[0] is MalNil) -> MalNil
        (args[0] !is MalMap) -> throw InvalidArgumentException("Expected first argument to be a map")
        else -> (args[0] as MalMap).items.getOrDefault(args[1], MalNil)
    }
}

fun `contains?`() = functionOfArity(2) { args ->
    if (args[0] !is MalMap) throw InvalidArgumentException("Expected first argument to be a map")
    (args[0] as MalMap).items.contains(args[1]).let { MalBoolean(it) }
}

fun keys() = typedArgumentFunction<MalMap>(arity = 1) { args ->
    MalList(items = (args[0] as MalMap).items.keys.toMutableList())
}

fun vals() = typedArgumentFunction<MalMap>(arity = 1) { args ->
    MalList(items = (args[0] as MalMap).items.values.toMutableList())
}

/*  Metadata */

fun meta() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalList -> arg.metadata ?: MalNil
        is MalMap -> arg.metadata ?: MalNil
        is MalVector -> arg.metadata ?: MalNil
        is MalFunction -> arg.metadata ?: MalNil
        is MalFunctionContainer -> arg.fn.metadata ?: MalNil
        else -> MalNil
    }
}

fun `with-meta`() = functionOfArity(2) { args ->
    when (val arg = args[0]) {
        is MalList -> arg.duplicate().apply { metadata = args[1] }
        is MalMap -> arg.duplicate().apply { metadata = args[1] }
        is MalVector -> arg.duplicate().apply { metadata = args[1] }
        is MalFunction -> arg.duplicate().apply { metadata = args[1] }
        is MalFunctionContainer -> {
            val cp = arg.duplicate()
            val ncp = cp.apply { fn.metadata = args[1] }
            ncp
        }
        else -> throw InvalidArgumentException("Invalid type, meta data is only supported for list, vector, map and function")
    }
}

fun `time-ms`() = functionOfArity(0) {
    val msSinceEpoch = System.currentTimeMillis()
    val truncated = msSinceEpoch.toInt() // will overflow in 2038, fix before!
    int(value = truncated)
}

/* Collections */

fun seq() = functionOfArity(1) { args ->
    when (val arg = args[0]) {
        is MalList -> if (arg.isEmpty()) MalNil else arg
        is MalVector -> if (arg.isEmpty()) MalNil else MalList(arg.items)
        is MalString -> if (arg.value == "") MalNil else MalList(
            arg.value.map { string(it.toString()) }.toMutableList()
        )
        is MalNil -> MalNil
        else -> throw InvalidArgumentException("Invalid type, expected list, vector or string")
    }
}

fun conj() = functionOfAtLeastArity(2) { args ->
    if (args[0] !is MalList && args[0] !is MalVector) throw InvalidArgumentException("Argument is not a list nor a vector")
    val items = args.drop(1)
    when (val collection = args[0]) {
        is MalList -> MalList(items = (items.reversed() + collection.items).toMutableList())
        else -> MalVector(items = ((collection as MalVector).items + items).toMutableList())
    }
}
