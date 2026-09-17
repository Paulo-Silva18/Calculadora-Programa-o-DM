package com.iftm.calculadora

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

/**
 * Motor de cálculo da calculadora.
 *
 * Responsável por:
 *  - Tokenizar a expressão digitada pelo usuário
 *  - Converter para notação polonesa reversa (RPN) respeitando a precedência
 *    matemática (^ > * / > + -) via algoritmo Shunting-Yard
 *  - Avaliar a RPN com tratamento de exceções (ex.: divisão por zero)
 *  - Calcular funções científicas (seno, cosseno, tangente, log) aplicadas
 *    imediatamente ao número corrente, como em uma calculadora física
 */
object CalculatorEngine {

    class CalculatorException(message: String) : Exception(message)

    private const val OPERATORS = "+-*/^"

    fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            when {
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                        i++
                    }
                    tokens.add(expression.substring(start, i))
                }
                OPERATORS.contains(c) -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> i++ // ignora caracteres inesperados
            }
        }
        return tokens
    }

    private fun precedence(op: String): Int = when (op) {
        "^" -> 3
        "*", "/" -> 2
        "+", "-" -> 1
        else -> 0
    }

    private fun isRightAssociative(op: String) = op == "^"

    fun toRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val opStack = ArrayDeque<String>()

        for (token in tokens) {
            if (token.toDoubleOrNull() != null) {
                output.add(token)
            } else {
                while (opStack.isNotEmpty() &&
                    precedence(opStack.last()) > 0 &&
                    (precedence(opStack.last()) > precedence(token) ||
                        (precedence(opStack.last()) == precedence(token) && !isRightAssociative(token)))
                ) {
                    output.add(opStack.removeLast())
                }
                opStack.addLast(token)
            }
        }
        while (opStack.isNotEmpty()) {
            output.add(opStack.removeLast())
        }
        return output
    }

    fun evalRPN(rpn: List<String>): Double {
        val stack = ArrayDeque<Double>()
        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addLast(num)
                continue
            }
            if (stack.size < 2) throw CalculatorException("Expressão inválida")
            val b = stack.removeLast()
            val a = stack.removeLast()
            val result = when (token) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> {
                    if (b == 0.0) throw CalculatorException("Divisão por zero")
                    a / b
                }
                "^" -> a.pow(b)
                else -> throw CalculatorException("Operador desconhecido: $token")
            }
            stack.addLast(result)
        }
        if (stack.size != 1) throw CalculatorException("Expressão inválida")
        return stack.last()
    }

    fun evaluate(expression: String): Double {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) throw CalculatorException("Expressão vazia")
        return evalRPN(toRPN(tokens))
    }

    // ---- Funções científicas (ângulos em graus) ----

    fun sinDeg(value: Double): Double = sin(Math.toRadians(value))

    fun cosDeg(value: Double): Double = cos(Math.toRadians(value))

    fun tanDeg(value: Double): Double {
        val cosVal = cos(Math.toRadians(value))
        if (abs(cosVal) < 1e-10) throw CalculatorException("Tangente indefinida")
        return tan(Math.toRadians(value))
    }

    fun log10Safe(value: Double): Double {
        if (value <= 0.0) throw CalculatorException("Log de número não positivo")
        return log10(value)
    }

    /** Formata o resultado removendo casas decimais desnecessárias. */
    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Erro"
        return if (value == value.toLong().toDouble() && abs(value) < 1e15) {
            value.toLong().toString()
        } else {
            String.format("%.8f", value).trimEnd('0').trimEnd('.')
        }
    }
}
