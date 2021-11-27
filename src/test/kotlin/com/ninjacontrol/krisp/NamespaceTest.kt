package com.ninjacontrol.krisp

class NamespaceTest : TestSuite {

    override val name = "Builtin functions in default namespace"

    private val tests = listOf(
        testReadEval {
            description = "list: no arguments returns empty list"
            input = """(list)"""
            expectedAst = com.ninjacontrol.krisp.emptyList()
        },
        testReadEval {
            description = "count: returns number of elements in list"
            input = """(count (list 1 2 3 4 5 6 7 8 9))"""
            expectedAst = int(9)
        },
        testReadEval {
            description = "count: empty list returns 0 elements"
            input = """(count (list))"""
            expectedAst = int(0)
        },
        testReadEval {
            description = "count: 'nil' counts as 0 elements"
            input = """(count nil)"""
            expectedAst = int(0)
        },
        testReadEval {
            description = "pr-str: 0 arguments returns empty string"
            input = """(pr-str)"""
            expectedAst = MalString("")
        },
        testReadEval {
            description = "read-string: eval string"
            input = """(read-string "(1 2 3)")"""
            expectedAst = list(int(1), int(2), int(3))
        },
        testReadEvalThrows(IOException("File \"this-does-not-exist\" does not exist")) {
            description = "slurp: invalid filename throws error"
            input = """(slurp "this-does-not-exist")"""
        },
        testReadEval {
            description = "atom: create atom"
            input = """(do (def! a (atom 43)) a)"""
            expectedAst = atom(int(43))
        },
        testReadEval {
            description = "atom: deref atom"
            input = """(do (def! a (atom 42)) (deref a))"""
            expectedAst = int(42)
        },
        testReadEval {
            description = "atom: deref atom, shorthand"
            input = """(do (def! a (atom 49)) @a)"""
            expectedAst = int(49)
        },
        testReadEval {
            description = "atom: is atom?"
            input = """(do (def! a (atom 49)) (atom? a))"""
            expectedAst = True
        },
        testReadEval {
            description = "atom: reset value"
            input = """(do (def! a (atom 49)) (reset! a 11))"""
            expectedAst = int(11)
        },
        testReadEval {
            description = "atom: swap atom"
            input = """(do (def! a (atom 49)) (swap! a (fn* [x y] (+ x y)) 10) @a)"""
            expectedAst = int(59)
        },
        testReadEval {
            description = "read-string: read string w. newline"
            input = """(read-string "\"\n\"")"""
            expectedAst = string("\n")
        },
        testReadEval {
            description = "atom: closures retains atoms"
            input = """(do (def! f (let* (a (atom 2)) (fn* () (deref a)))) (def! a (atom 3)) (f))"""
            expectedAst = int(2)
        },
        testReadEval {
            description = "cons: returns list with new element prepended"
            input = """(cons 0 '(1 2 3 4))"""
            expectedAst = list(int(0), int(1), int(2), int(3), int(4))
        },
        testReadEval {
            description = "cons: vector as second argument"
            input = """(cons 0 [1 2 3 4])"""
            expectedAst = list(int(0), int(1), int(2), int(3), int(4))
        },
        testReadEval {
            description = "concat: returns concatenated list"
            input = """(concat '(1 2) '(3 4) '(5 6) '(7 8))"""
            expectedAst = list(int(1), int(2), int(3), int(4), int(5), int(6), int(7), int(8))
        },
        testReadEval {
            description = "concat: vector parameter should return list"
            input = """(concat [99 98])"""
            expectedAst = list(int(99), int(98))
        },
        testReadEval {
            description = "concat: vector + list + vector"
            input = """(concat [99 98] (list 97 96) [95 94])"""
            expectedAst = list(int(99), int(98), int(97), int(96), int(95), int(94))
        },
        testReadEval {
            description = "try*/throw/catch*: throw and catch user exception"
            input = """(try* (throw '(1 2 3)) (catch* E (do E)))"""
            expectedAst = map(
                key("type") to string("UserException"),
                key("value") to list(int(1), int(2), int(3))
            )
        },
        testReadEval {
            description = "try*/catch*: catch built-in exception"
            input = """(try* unknown (catch* E (do E)))"""
            expectedAst = map(
                key("type") to string("NotFoundException"),
                key("message") to string("Symbol 'unknown' not found")
            )
        },
        testReadEval {
            description = "apply: call function with argument list"
            input = """(apply (fn* (l) (cons 10 l)) [[20 23]] )"""
            expectedAst = list(int(10), int(20), int(23))
        },
        testReadEval {
            description = "apply: call built function with argument list"
            input = """(apply + [20 23])"""
            expectedAst = int(43)
        },
        testReadEval {
            description = "apply: call function with concatenated argument list"
            input = """(apply (fn* (& l) (cons 10 l)) 12 33 [20 23] )"""
            expectedAst = list(int(10), int(12), int(33), int(20), int(23))
        },
        testReadEval {
            description = "map: list"
            input = """(map (fn* (o) (+ o 10)) [1 2 3 4 5])"""
            expectedAst = list(int(11), int(12), int(13), int(14), int(15))
        },
        testReadEval {
            description = "map: list, built-in function"
            input = """(map list [1 2 3])"""
            expectedAst = list(list(int(1)), list(int(2)), list(int(3)))
        },
        testReadEval {
            description = "true?+false?: predicates"
            input = """(list (true? true) (false? false) (true? false) (false? true))"""
            expectedAst = list(True, True, False, False)
        },
        testReadEval {
            description = "nil?: predicates"
            input = """(list (nil? true) (nil? nil))"""
            expectedAst = list(False, True)
        },
        testReadEval {
            description = "symbol?: predicates"
            input = """(list (symbol? true) (symbol? 'apskaft) (symbol? 12) (symbol? "foo"))"""
            expectedAst = list(False, True, False, False)
        },
        testReadEval {
            description = "hash-map: build map"
            input = """(hash-map :foo 1 :bar 2 :baz 3)"""
            expectedAst = map(key("foo") to int(1), key("bar") to int(2), key("baz") to int(3))
        },
        testReadEvalThrows(InvalidArgumentException("Expected an even number of arguments")) {
            description = "hash-map: odd number of arguments throws exception"
            input = """(hash-map :foo 1 :bar)"""
        },
        testReadEval {
            description = "assoc: add to map"
            input = """(assoc {:foo 1 :bar 2} :baz 3)"""
            expectedAst = map(key("foo") to int(1), key("bar") to int(2), key("baz") to int(3))
        },
        testReadEvalThrows(InvalidArgumentException("Expected an even number of arguments following the first argument")) {
            description = "assoc: odd number of arguments throws exception"
            input = """(assoc {:foo 1 :bar 2} :blurg)"""
        },
        testReadEval {
            description = "dissoc: remove from map"
            input = """(dissoc {:foo 1 :bar 2 :baz 3} :baz :bar)"""
            expectedAst = map(key("foo") to int(1))
        },
        testReadEval {
            description = "get: get value from map by key"
            input = """(get {:foo 1 :bar 2 :baz 3} :baz)"""
            expectedAst = int(3)
        },
        testReadEval {
            description = "get: not found returns nil"
            input = """(get {:foo 1 :bar 2 :baz 3} :ulon)"""
            expectedAst = MalNil
        },
        testReadEval {
            description = "keys: list of keys"
            input = """(keys {:foo 1 :bar 2 :baz 3})"""
            expectedAst = list(key("foo"), key("bar"), key("baz"))
        },
        testReadEval {
            description = "vals: list of values"
            input = """(vals {:foo 1 :bar 2 :baz 3})"""
            expectedAst = list(int(1), int(2), int(3))
        },
        testReadEval {
            description = "contains?: true if key exists"
            input = """(contains? {:foo 1 :bar 2 :baz 3} :foo)"""
            expectedAst = True
        },
        testReadEval {
            description = "contains?: false if key does not exist"
            input = """(contains? {:foo 1 :bar 2 :baz 3} :uklan)"""
            expectedAst = False
        },

    )

    override fun getTests(): List<TestCase> = tests
    override fun run(): Boolean =
        verifyTests(tests)
}
