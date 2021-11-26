package com.ninjacontrol.krisp

typealias EnvironmentMap = MutableMap<MalSymbol, MalType>

class Environment(
    private val outer: Environment? = null,

) {

    companion object {

        private fun zipWithVariadicParams(
            bindings: List<MalType>,
            expressions: List<MalType>,
            env: Environment
        ): Environment {
            val markerPos = bindings.indexOfFirst { it is MalSymbol && it.name == "&" }
            if (markerPos != (bindings.size - 1) - 1) {
                // sanity check: we only understand a '&' marker if it is in the next to last position
                throw InvalidArgumentException("Bad variadic argument")
            }
            val paramsFirst = bindings.subList(0, markerPos)
            val paramRest = bindings[markerPos + 1]
            val expressionsFirst = expressions.subList(0, markerPos)
            val expressionsRest = expressions.subList(markerPos, expressions.size)
            paramsFirst.zip(expressionsFirst).forEach { (symbol, expression) ->
                env.set(symbol as MalSymbol, expression)
            }
            env.set(paramRest as MalSymbol, MalList(items = expressionsRest.toMutableList()))
            return env
        }

        private fun zipParams(
            bindings: List<MalType>,
            expressions: List<MalType>,
            env: Environment
        ): Environment {
            bindings.zip(expressions).forEach { (symbol, expression) ->
                env.set(symbol as MalSymbol, expression)
            }
            return env
        }

        fun withBindings(
            outer: Environment? = null,
            bindings: List<MalType>,
            expressions: List<MalType>
        ): Environment {
            val env = Environment(outer)
            return when {
                bindings.any { it !is MalSymbol } -> throw InvalidArgumentException("Bindings should be symbols")
                bindings.any { it is MalSymbol && it.name == "&" } -> zipWithVariadicParams(
                    bindings,
                    expressions,
                    env
                )
                (bindings.size == expressions.size) -> zipParams(bindings, expressions, env)
                else -> throw InvalidArgumentException("Bindings and expressions mismatch")
            }
        }
    }

    private val data: EnvironmentMap = mutableMapOf()
    fun set(symbol: MalSymbol, value: MalType): MalType {
        data[symbol] = value
        return value
    }

    fun find(symbol: MalSymbol): Environment? {
        return when (data.containsKey(symbol)) {
            true -> this
            false -> outer?.find(symbol)
        }
    }

    fun get(symbol: MalSymbol) = find(symbol)?.let { env ->
        env.data[symbol]
    } ?: throw NotFoundException("Symbol '${symbol.name}' not found")

    fun getOrError(symbol: MalSymbol) = find(symbol)?.let { env ->
        env.data[symbol]
    } ?: MalError("Symbol '${symbol.name}' not found")

    fun add(environment: EnvironmentMap) {
        data.putAll(from = environment)
    }
}
