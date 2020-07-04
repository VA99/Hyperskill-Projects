package calculator

import java.util.Scanner
import java.math.BigInteger

fun main() {
    val scanner = Scanner(System.`in`)

    val operators = "+-*/^"
    val unaryOperators = "+-"
    val priorityOfOperator = mapOf("+-" to 1, "*/" to 2, "^" to 3)
    val variables = mutableMapOf<String, BigInteger>()

    do {
        var input = ""
        if (scanner.hasNextLine()) {
            input = scanner.nextLine().replace(" ", "")
            val validCommand = input.isNotEmpty() && input.first() == '/'

            when {
                validCommand -> {
                    when (input) {
                        "/exit" -> println("Bye!")
                        "/help" -> println("The program calculates the sum of numbers")
                        else -> println("Unknown command")
                    }
                }
                else -> {
                    if (inputValidation(input, variables, unaryOperators, operators)) {
                        val variableName = input.substringBefore('=', "")
                        input = input.substringAfter('=', input)

                        var i = 0
                        var expression = ""
                        val stack = mutableListOf<String>()
                        while (i < input.length) {
                            val value = input.substring(i)

                            var operator = firstStringOf(value) { !it.isLetterOrDigit() && it !in "()" }
                            val bracket = firstStringOf(value) { it in "()" }
                            val operand = firstStringOf(value) { it.isLetterOrDigit() }

                            i += operand.length + operator.length + bracket.length

                            if (operand.isNotEmpty()) expression += "$operand "
                            if (bracket.isNotEmpty()) {
                                if (bracket.all { it == '(' }) bracket.forEach { stack.add("$it") }
                                else if (bracket.all { it == ')' }) {
                                    var isLeftBracket = false
                                    do {
                                        expression += "${stack.last()} "
                                        stack.removeAt(stack.lastIndex)
                                        if (stack.isNotEmpty()) isLeftBracket = stack.last() == "("
                                    } while (stack.isNotEmpty() && !isLeftBracket)
                                    if (isLeftBracket) stack.removeAt(stack.lastIndex)
                                }
                            }
                            if (operator.isNotEmpty()) {
                                if (operator.all { it in unaryOperators }) operator = filterUnaryOperators(operator, unaryOperators)
                                if (stack.isEmpty() || stack.isNotEmpty() && stack.last() == "(" ) stack.add(operator)
                                else {
                                    val operatorPriority1 = priorityOf(operator, priorityOfOperator)
                                    var operatorPriority2 = priorityOf(stack.last(), priorityOfOperator)
                                    if (operatorPriority1 > operatorPriority2) stack.add(operator)
                                    else {
                                        do {
                                            expression += "${stack.last()} "
                                            stack.removeAt(stack.lastIndex)
                                            if (stack.isNotEmpty()) operatorPriority2 = priorityOf(stack.last(), priorityOfOperator)
                                        } while (stack.isNotEmpty() && operatorPriority1 <= operatorPriority2)
                                        stack.add(operator)
                                    }
                                }
                            }
                        }
                        while (stack.isNotEmpty()) {
                            expression += "${stack.last()} "
                            stack.removeAt(stack.lastIndex)
                        }

                        val result = mutableListOf<BigInteger>()
                        while (expression.isNotEmpty()) {
                            val value = expression.substringBefore(' ')
                            if (isNumber(value)) result.add(value.toBigInteger())
                            else if (isWord(value)) result.add(variables[value]!!)
                            else if (value in "+-*/^") {
                                val b = result.removeAt(result.lastIndex)
                                if (result.isNotEmpty()) {
                                    val a = result.removeAt(result.lastIndex)
                                    result.add(
                                            when (value) {
                                                "^" -> a.pow(b.toInt())
                                                "/" -> a / b
                                                "*" -> a * b
                                                "+" -> a + b
                                                "-" -> a - b
                                                else -> BigInteger.ZERO
                                            }
                                    )
                                } else result.add(BigInteger("$value$b"))
                            }
                            expression = expression.substringAfter(' ', "")
                        }
                        if (result.isNotEmpty()) {
                            if (variableName.isNotEmpty()) variables[variableName] = result.first()
                            else println(result.first())
                        }
                    }
                }
            }
        }

    } while (input != "/exit")
}

fun priorityOf(operator: String, priorityOfOperator: Map<String, Int>): Int {
    for ((operators, priority) in priorityOfOperator) if (operator in operators) return priority
    return -1
}

fun filterUnaryOperators(value: String, unaryOperators: String): String {
    val lengthOfMinus = value.filter { it == unaryOperators.last() }.length
    return if (lengthOfMinus % 2 == 0) "${unaryOperators.first()}" else "${unaryOperators.last()}"
}

fun firstStringOf(string: String, predicate: (Char) -> Boolean): String {
    var length = 0
    while (length < string.length && predicate(string[length])) length++
    return if (length == 0) "" else string.substring(0, length)
}

fun getStringsOf(string: String, predicate: (Char) -> Boolean): Array<String> {
    var i = 0
    var stringArray = arrayOf<String>()
    while (i < string.length) {
        val first = firstStringOf(string.substring(i), predicate)
        if (first.isNotEmpty()) stringArray += first
        i += first.length + 1
    }
    return stringArray
}

fun isWord(string: String) = string.all { it.isLetter() }
fun isNumber(string: String) = string.all { it.isDigit() }
fun areOperatorsUnary(string: String, unaryOperators: String) = string.all { it in unaryOperators }

fun inputValidation(input: String, variables: Map<String, BigInteger>, unaryOperators: String, allOperators: String): Boolean {
    val indexOfAssignOperator = input.indexOf('=')

    val isAssignment = indexOfAssignOperator in 1 until input.lastIndex
    val isNotAssignment = indexOfAssignOperator == -1

    var validVariableName = false
    var expression = ""
    if (isAssignment) {
        val variableName = input.substring(0, indexOfAssignOperator)
        validVariableName = isWord(variableName)
        expression = input.substring(indexOfAssignOperator + 1)
    } else if (isNotAssignment) expression = input

    var validExpression = false
    var areVariablesDeclared = true
    if (expression.isNotEmpty()) {
        var validTokens = true
        var validPair = true

        var previousOperand = ""
        var previousOperator = ""
        var previousBrackets = ""
        var previousValue = ""

        var operand = ""
        var operator = ""
        var brackets = ""

        var i = 0
        while (i < expression.length) {
            val startFrom = expression.substring(i)
            operand = startFrom.takeWhile { it.isLetterOrDigit() }
            operator = startFrom.takeWhile { it in allOperators }
            brackets = startFrom.takeWhile { it in "()" }

            val validOperand = isWord(operand) || isNumber(operand)
            val validOperator = operator.length > 1 && areOperatorsUnary(operator, unaryOperators) || operator.length <= 1
            val validBrackets = brackets.all { it == '(' } || brackets.all { it == ')' }

            validTokens = validOperand && validOperator && validBrackets
            if (!validTokens) break

            val value = operand + operator + brackets
            validPair = if (previousValue.isNotEmpty()) {
                when {
                    previousOperand.isNotEmpty() -> brackets.isNotEmpty() && brackets.all { it == ')' } || operator.isNotEmpty()
                    previousOperator.isNotEmpty() -> brackets.isNotEmpty() && brackets.all { it == '(' } || operand.isNotEmpty()
                    previousBrackets.isNotEmpty() -> operand.isNotEmpty() && previousBrackets.all { it == '(' } || operator.isNotEmpty()
                    else -> false
                }
            } else brackets.isNotEmpty() && brackets.all { it == '(' } || brackets.isEmpty()
            if (!validPair) break

            i += value.length
            if (i < expression.length) {
                previousOperand = operand
                previousOperator = operator
                previousBrackets = brackets
                previousValue = value
            }
        }

        val validLastPair = validTokens && if (previousValue.isNotEmpty()) {
            when {
                previousOperand.isNotEmpty() -> validPair
                previousOperator.isNotEmpty() -> brackets.isNotEmpty() && brackets.all { it == ')' } || operand.isNotEmpty()
                else -> previousBrackets.isEmpty()
            }
        } else validPair

        val leftBrackets = expression.filter { it == '(' }
        val rightBrackets = expression.filter { it == ')' }

        val areBracketsEqual = leftBrackets.length == rightBrackets.length
        validExpression = validTokens && validPair && validLastPair && areBracketsEqual && expression.none { it == '=' }

        val letters = getStringsOf(expression) { it.isLetter() }
        if (letters.isNotEmpty()) areVariablesDeclared = validExpression && letters.all { variables.containsKey(it) }
    }

    when {
        !validExpression -> {
            if (isAssignment) println("Invalid assignment")
            else if (input.isNotEmpty()) println("Invalid expression")
        }
        !validVariableName && isAssignment -> println("Invalid identifier")
        !areVariablesDeclared -> println("Unknown variable")
    }

    return validExpression && (validExpression || !isAssignment) && (validVariableName || !isAssignment) && areVariablesDeclared || input.isEmpty()
}
