package com.ninjacontrol.krisp

fun read(input: String) = readStr(input)

object Symbols {
    val def = MalSymbol("def!")
    val let = MalSymbol("let*")
    val `do` = MalSymbol("do")
    val `if` = MalSymbol("if")
    val fn = MalSymbol("fn*")
    val quote = MalSymbol("quote")
    val quasiquote = MalSymbol("quasiquote")
    val quasiquoteexpand = MalSymbol("quasiquoteexpand")
    val unquote = MalSymbol("unquote")
    val eval = MalSymbol("eval")
    val `splice-unquote` = MalSymbol("splice-unquote")
    val concat = MalSymbol("concat")
    val cons = MalSymbol("cons")
    val vec = MalSymbol("vec")
    val defMacro = MalSymbol("defmacro!")
    val macroExpand = MalSymbol("macroexpand")
    val `try` = MalSymbol("try*")
    val `catch` = MalSymbol("catch*")
}

fun eval(ast: MalType, env: Environment): MalType {
    var currentAst = ast
    var nextAst = ast
    var currentEnv = env
    var nextEnv = currentEnv

    /**
     * Returns null when a new environment and ast has been set, MalType otherwise
     */
    val apply: () -> MalType? = {
        when (val evaluatedList = evalAst(currentAst, currentEnv)) {
            is MalList -> {
                when (val f = evaluatedList.head) {
                    is MalFunctionContainer -> {
                        val params = when (f.params) {
                            is MalList -> f.params
                            is MalVector -> f.params.asList()
                            else -> null
                        }
                        if (params == null) throw InvalidArgumentException("Invalid parameter type")
                        else {
                            val newEnv = Environment.withBindings(
                                outer = f.environment,
                                bindings = params.items,
                                expressions = evaluatedList.tail.items
                            )
                            nextEnv = newEnv
                            nextAst = f.ast
                            null
                        }
                    }
                    is MalFunction ->
                        f.apply(evaluatedList.tail)
                    else ->
                        throw EvaluationException("Not a function")
                }
            }
            else -> throw EvaluationException("Cannot apply")
        }
    }
    while (true) {
        if (currentAst !is MalList) return evalAst(currentAst, currentEnv)
        currentAst = macroExpand(currentAst, currentEnv)
        // check resulting AST again after macro expansion
        if (currentAst !is MalList) return evalAst(currentAst, currentEnv)
        if (currentAst.isEmpty()) return currentAst
        val head = currentAst.head

        when {
            head eq Symbols.def -> return define(currentAst.tail, currentEnv)
            head eq Symbols.let -> {
                val (resultAst, newEnv) = let(currentAst.tail, currentEnv)
                if (resultAst is MalError) {
                    return resultAst
                }
                nextAst = resultAst
                newEnv?.let { nextEnv = newEnv }
            }
            head eq Symbols.`do` -> nextAst = `do`(currentAst.tail, currentEnv)
            head eq Symbols.`if` -> nextAst = `if`(currentAst.tail, currentEnv)
            head eq Symbols.fn -> nextAst = fn(currentAst.tail, currentEnv)
            head eq Symbols.quote -> return quote(currentAst.tail)
            head eq Symbols.quasiquoteexpand -> return quasiquote(
                unwrapSingle(currentAst.tail),
                currentEnv
            )
            head eq Symbols.quasiquote ->
                nextAst =
                    quasiquote(unwrapSingle(currentAst.tail), currentEnv)
            head eq Symbols.defMacro -> return defMacro(currentAst.tail, currentEnv)
            head eq Symbols.macroExpand -> return macroExpand(
                unwrapSingle(currentAst.tail),
                currentEnv
            )
            head eq Symbols.`try` -> {
                val nextAstEnv = tryCatch(currentAst.tail, currentEnv)
                nextAst = nextAstEnv.first
                nextEnv = nextAstEnv.second
            }
            else -> apply()?.let {
                return it
            }
        }
        currentAst = nextAst
        currentEnv = nextEnv
    }
}

fun tryCatch(ast: MalList, env: Environment): Pair<MalType, Environment> {
    if (ast.size < 1) throw InvalidArgumentException("Invalid number of arguments")
    val tryForm = ast.get(0)
    if (ast.size == 1) { // accept a try expression without a catch
        return eval(tryForm, env) to env
    }

    when (val catchForm = ast.get(1)) {
        is MalList -> {
            when {
                catchForm.size != 3 -> throw InvalidArgumentException("Expected a catch expression with two arguments")
                catchForm.head neq Symbols.catch -> throw InvalidArgumentException("Expected a 'catch' expression following 'try'.")
                catchForm.get(1) !is MalSymbol -> throw InvalidArgumentException("Expected symbol")
                else -> {
                    return try {
                        val tryResult = eval(tryForm, env)
                        tryResult to env
                    } catch (e: Throwable) {
                        val exception =
                            if (e is UserException) e.toMap() else e.toMap()
                        val bindSymbol = catchForm.get(1) as MalSymbol
                        val newEnv = Environment(outer = env)
                        val catchExpression = catchForm.get(2)
                        newEnv.set(bindSymbol, exception)
                        catchExpression to newEnv
                    }
                }
            }
        }
        else -> throw InvalidArgumentException("Expected a list")
    }
}

fun unwrapSingle(ast: MalList) = when (ast.size) {
    1 -> ast.head
    else -> ast
}

fun fn(expressions: MalList, env: Environment): MalType {

    if (expressions.size != 2) {
        throw InvalidArgumentException("Invalid number of arguments, expected 2")
    }
    val functionBindings: List<MalType> = when (val bindings = expressions.get(0)) {
        is MalList -> bindings.items
        is MalVector -> bindings.items
        else -> throw InvalidArgumentException("Error creating bindings, invalid type, expected list or vector")
    }
    val fn = MalFunction { functionArguments ->
        val newEnv = Environment.withBindings(
            env,
            bindings = functionBindings,
            expressions = functionArguments.toList()
        )
        return@MalFunction eval(expressions.get(1), newEnv)
    }
    return MalFunctionContainer(
        ast = expressions.get(1),
        params = expressions.get(0),
        environment = env,
        fn = fn
    )
}

fun `if`(expressions: MalList, env: Environment): MalType {
    if (expressions.size < 2) throw InvalidArgumentException("Invalid conditional expression")
    return when (val condition = eval(expressions.get(0), env)) {
        is MalBoolean, is MalNil -> when (condition) {
            False, MalNil -> expressions.getOrNull(2) ?: MalNil
            else -> expressions.get(1)
        }
        else -> expressions.get(1)
    }
}

fun `do`(expressions: MalList, env: Environment): MalType {
    if (expressions.isEmpty()) return MalNil
    evalAst(expressions.subList(0, expressions.size), env)
    return expressions.last
}

fun macroExpand(ast: MalType, env: Environment): MalType {
    var currentAst = ast
    while (isMacroCall(currentAst, env)) {
        // isMacroCall ensures that the ast has the following content
        val astList = ast as MalList
        val symbol = astList.head as MalSymbol
        val functionContainer = env.get(symbol)
        val function = (functionContainer as MalFunctionContainer).fn
        val arguments = astList.tail
        currentAst = function.apply(arguments)
    }
    return currentAst
}

fun isMacroCall(ast: MalType, env: Environment) = when {
    ast is MalList && ast.head is MalSymbol -> {
        val symbol = ast.head as MalSymbol
        when (val value = env.getOrError(symbol)) {
            is MalFunctionContainer -> value.isMacro
            else -> false
        }
    }
    else -> false
}

fun defMacro(bindingList: MalList, env: Environment): MalType {
    return when (bindingList.size) {
        2 -> {
            when (val name = bindingList.get(0)) {
                is MalSymbol -> {
                    when (val value = eval(bindingList.get(1), env)) {
                        is MalFunctionContainer -> {
                            val newValue = value.copy(isMacro = true)
                            env.set(name, newValue)
                        }
                        else -> env.set(name, value)
                    }
                }
                else -> {
                    throw InvalidArgumentException("Invalid argument (symbol)")
                }
            }
        }
        else -> throw InvalidArgumentException("Invalid number of arguments")
    }
}

fun define(bindingList: MalList, env: Environment): MalType {
    return when (bindingList.size) {
        2 -> {
            when (val name = bindingList.get(0)) {
                is MalSymbol -> {
                    val value = eval(bindingList.get(1), env)
                    env.set(name, value)
                }
                else -> {
                    throw InvalidArgumentException("Invalid argument (symbol)")
                }
            }
        }
        else -> throw InvalidArgumentException("Invalid number of arguments")
    }
}

fun let(expressions: MalList, env: Environment): Pair<MalType, Environment?> {

    fun evaluateWithBindings(
        expression: MalType,
        bindings: List<MalType>,
        env: Environment
    ): Pair<MalType, Environment?> =
        if (bindings.size % 2 == 0) {
            bindings.chunked(2).forEach {
                val key = it[0]
                val evaluated = eval(it[1], env)
                if (key !is MalSymbol) {
                    throw InvalidArgumentException("Error evaluating environment, key must be a symbol")
                } else {
                    env.set(key, evaluated)
                }
            }
            expression to env // TCO
        } else {
            throw InvalidArgumentException("Invalid binding list (odd number of items)")
        }

    return when (expressions.size) {
        2 -> {
            val newEnv = Environment(outer = env)
            val newBindings = expressions.get(0)
            val expression = expressions.get(1)
            when (newBindings) {
                is MalList -> evaluateWithBindings(expression, newBindings.items, newEnv)
                is MalVector -> evaluateWithBindings(expression, newBindings.items, newEnv)
                else -> throw InvalidArgumentException("Invalid binding (not a list or vector)")
            }
        }
        else -> throw InvalidArgumentException("Invalid number of arguments")
    }
}

fun quote(ast: MalList): MalType {
    return ast.getOrNull(0) ?: MalNil
}

fun quasiquote(ast: MalType, _env: Environment): MalType {

    return when {
        ast is MalList && ast.head eq Symbols.unquote -> ast.getOrNull(1)
            ?: throw InvalidArgumentException("Invalid arguments")
        ast is MalList || ast is MalVector -> {
            var result = emptyList()
            val elements =
                if (ast is MalList) ast.items.asReversed() else (ast as MalVector).items.asReversed()
            for (element in elements) {
                result = when {
                    element is MalList && element.head eq Symbols.`splice-unquote` -> {
                        if (element.size < 2) throw InvalidArgumentException("Invalid number of arguments")
                        list(Symbols.concat, element.get(1), result)
                    }
                    else -> {
                        list(Symbols.cons, quasiquote(element, _env), result)
                    }
                }
            }
            if (ast is MalVector) list(Symbols.vec, result) else result
        }
        ast is MalMap || ast is MalSymbol -> {
            list(Symbols.quote, ast)
        }
        else -> ast
    }
}

fun evalAst(ast: MalType, env: Environment): MalType = when (ast) {
    is MalSymbol -> env.get(ast)
    is MalList -> MalList(items = ast.items.map { item -> eval(item, env) }.toMutableList())
    is MalVector -> MalVector(items = ast.items.map { item -> eval(item, env) }.toMutableList())
    is MalMap -> MalMap(
        items = ast.items.mapValues { (_, value) -> eval(value, env) }
            .toMutableMap()
    )
    else -> ast
}
