package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

sealed class EvaluationResult {
    data class Success(val formatted: String, val raw: BigDecimal) : EvaluationResult()
    data class Error(val errorType: CalculatorError) : EvaluationResult()
}

enum class CalculatorError {
    DIVIDE_BY_ZERO,
    DOMAIN_ERROR,
    INVALID_EXPRESSION,
    OVERFLOW
}

enum class AngleUnit {
    DEG, RAD
}

/**
 * Precision expression parser and evaluator for Bong Calculator.
 * Uses BigDecimal (16-digit precision, HALF_UP) for exact decimal arithmetic
 * to eliminate IEEE-754 floating-point drift (e.g. 0.1 + 0.2 == 0.3).
 */
object CalculatorEngine {

    private val MC = MathContext(16, RoundingMode.HALF_UP)
    private val DISPLAY_MC = MathContext(12, RoundingMode.HALF_UP)
    private val PI_BD = BigDecimal("3.14159265358979323846")
    private val E_BD = BigDecimal("2.71828182845904523536")

    private sealed interface Token {
        data class Number(val value: BigDecimal) : Token
        data class Constant(val symbol: String, val value: BigDecimal) : Token
        data class Function(val name: String) : Token
        data class Operator(val symbol: Char) : Token
        data object LeftParen : Token
        data object RightParen : Token
        data object Percent : Token
        data object Factorial : Token
        data object Square : Token
    }

    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEG): EvaluationResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return EvaluationResult.Success("0", BigDecimal.ZERO)
        }

        return try {
            val normalized = autoCloseParentheses(trimmed)
            val tokens = tokenize(normalized)
            if (tokens.isEmpty()) {
                return EvaluationResult.Error(CalculatorError.INVALID_EXPRESSION)
            }
            val parser = Parser(tokens, angleUnit)
            val result = parser.parseExpression()
            if (parser.hasMore()) {
                return EvaluationResult.Error(CalculatorError.INVALID_EXPRESSION)
            }
            val cleaned = cleanResult(result)
            EvaluationResult.Success(formatBigDecimal(cleaned), cleaned)
        } catch (e: CalcEvalException) {
            EvaluationResult.Error(e.errorType)
        } catch (_: ArithmeticException) {
            EvaluationResult.Error(CalculatorError.OVERFLOW)
        } catch (_: NumberFormatException) {
            EvaluationResult.Error(CalculatorError.INVALID_EXPRESSION)
        } catch (_: Exception) {
            EvaluationResult.Error(CalculatorError.INVALID_EXPRESSION)
        }
    }

    private fun autoCloseParentheses(expr: String): String {
        var openCount = 0
        for (ch in expr) {
            if (ch == '(') openCount++
            else if (ch == ')' && openCount > 0) openCount--
        }
        return if (openCount > 0) expr + ")".repeat(openCount) else expr
    }

    private fun tokenize(input: String): List<Token> {
        val rawTokens = mutableListOf<Token>()
        var i = 0
        val len = input.length

        while (i < len) {
            val ch = input[i]
            when {
                ch.isWhitespace() -> i++
                ch.isDigit() || ch == '.' -> {
                    val sb = StringBuilder()
                    var dotCount = 0
                    while (i < len && (input[i].isDigit() || input[i] == '.')) {
                        if (input[i] == '.') dotCount++
                        if (dotCount > 1) throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
                        sb.append(input[i])
                        i++
                    }
                    // Handle scientific notation like 1.23E5 or 1.23E-4
                    if (i < len && (input[i] == 'E' || input[i] == 'e') &&
                        (i + 1 < len && (input[i + 1].isDigit() || input[i + 1] == '+' || input[i + 1] == '-' || input[i + 1] == '−'))
                    ) {
                        val nextAfterE = input[i + 1]
                        if (nextAfterE.isDigit() || ((nextAfterE == '+' || nextAfterE == '-' || nextAfterE == '−') && i + 2 < len && input[i + 2].isDigit())) {
                            sb.append('E')
                            i++
                            if (input[i] == '+' || input[i] == '-' || input[i] == '−') {
                                sb.append(if (input[i] == '−') '-' else input[i])
                                i++
                            }
                            while (i < len && input[i].isDigit()) {
                                sb.append(input[i])
                                i++
                            }
                        }
                    }
                    val numStr = sb.toString()
                    if (numStr == ".") throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
                    rawTokens.add(Token.Number(BigDecimal(numStr)))
                }
                ch == 'π' -> {
                    rawTokens.add(Token.Constant("π", PI_BD))
                    i++
                }
                ch == 'e' -> {
                    rawTokens.add(Token.Constant("e", E_BD))
                    i++
                }
                ch == '√' -> {
                    rawTokens.add(Token.Function("sqrt"))
                    i++
                }
                ch == '²' -> {
                    rawTokens.add(Token.Square)
                    i++
                }
                ch == '(' -> {
                    rawTokens.add(Token.LeftParen)
                    i++
                }
                ch == ')' -> {
                    rawTokens.add(Token.RightParen)
                    i++
                }
                ch == '%' -> {
                    rawTokens.add(Token.Percent)
                    i++
                }
                ch == '!' -> {
                    rawTokens.add(Token.Factorial)
                    i++
                }
                ch == '+' -> {
                    rawTokens.add(Token.Operator('+'))
                    i++
                }
                ch == '-' || ch == '−' -> {
                    rawTokens.add(Token.Operator('-'))
                    i++
                }
                ch == '*' || ch == '×' -> {
                    rawTokens.add(Token.Operator('*'))
                    i++
                }
                ch == '/' || ch == '÷' -> {
                    rawTokens.add(Token.Operator('/'))
                    i++
                }
                ch == '^' -> {
                    rawTokens.add(Token.Operator('^'))
                    i++
                }
                ch.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < len && input[i].isLetter()) {
                        sb.append(input[i])
                        i++
                    }
                    val word = sb.toString().lowercase()
                    when (word) {
                        "sin", "cos", "tan", "ln", "log", "sqrt" -> rawTokens.add(Token.Function(word))
                        "pi" -> rawTokens.add(Token.Constant("π", PI_BD))
                        else -> throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
                    }
                }
                else -> throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
            }
        }

        // Insert implicit multiplication where appropriate:
        // e.g. 2(3), (2)(3), 2π, π2, 2sin(30), 50%(2), 3!(2), 4²(2)
        val result = mutableListOf<Token>()
        for (idx in rawTokens.indices) {
            val curr = rawTokens[idx]
            if (idx > 0) {
                val prev = rawTokens[idx - 1]
                val prevIsValueEnd = prev is Token.Number ||
                    prev is Token.Constant ||
                    prev is Token.RightParen ||
                    prev is Token.Percent ||
                    prev is Token.Factorial ||
                    prev is Token.Square
                val currIsValueStart = curr is Token.Number ||
                    curr is Token.Constant ||
                    curr is Token.LeftParen ||
                    curr is Token.Function
                if (prevIsValueEnd && currIsValueStart) {
                    result.add(Token.Operator('*'))
                }
            }
            result.add(curr)
        }
        return result
    }

    private class CalcEvalException(val errorType: CalculatorError) : RuntimeException()

    private class Parser(
        private val tokens: List<Token>,
        private val angleUnit: AngleUnit
    ) {
        private var pos = 0

        fun hasMore(): Boolean = pos < tokens.size

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun consume(): Token = tokens[pos++]

        /**
         * Additive level (+, -). Also handles standard calculator percentage semantics:
         * `A + B%` -> `A + (A * B / 100)`
         * `A - B%` -> `A - (A * B / 100)`
         */
        fun parseExpression(): BigDecimal {
            var left = parseTerm()
            while (true) {
                val op = peek()
                if (op is Token.Operator && (op.symbol == '+' || op.symbol == '-')) {
                    consume()
                    val (rightVal, isDirectPercent) = parseTermWithPercentFlag()
                    left = if (isDirectPercent) {
                        val delta = left.multiply(rightVal, MC)
                        if (op.symbol == '+') left.add(delta, MC) else left.subtract(delta, MC)
                    } else {
                        if (op.symbol == '+') left.add(rightVal, MC) else left.subtract(rightVal, MC)
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun parseTermWithPercentFlag(): Pair<BigDecimal, Boolean> {
            // Check if the term is a single factor ending in `%` (like `10%` or `(5+5)%`)
            // and NOT followed by `*`, `/`, or `^`.
            val startPos = pos
            val factorVal = parsePower()
            val next = peek()
            val isMultiplyOrDivide = next is Token.Operator && (next.symbol == '*' || next.symbol == '/')
            if (!isMultiplyOrDivide) {
                // Check if the factor we just parsed ended directly with a Percent token
                val lastConsumed = tokens.getOrNull(pos - 1)
                if (lastConsumed is Token.Percent) {
                    return factorVal to true
                }
            }
            pos = startPos
            return parseTerm() to false
        }

        private fun parseTerm(): BigDecimal {
            var left = parsePower()
            while (true) {
                val op = peek()
                if (op is Token.Operator && (op.symbol == '*' || op.symbol == '/')) {
                    consume()
                    val right = parsePower()
                    left = if (op.symbol == '*') {
                        left.multiply(right, MC)
                    } else {
                        if (right.compareTo(BigDecimal.ZERO) == 0) {
                            throw CalcEvalException(CalculatorError.DIVIDE_BY_ZERO)
                        }
                        left.divide(right, MC)
                    }
                } else {
                    break
                }
            }
            return left
        }

        // Right-associative exponentiation: 2^3^2 = 2^(3^2)
        private fun parsePower(): BigDecimal {
            val base = parseUnary()
            val op = peek()
            return if (op is Token.Operator && op.symbol == '^') {
                consume()
                val exponent = parsePower()
                powerBigDecimal(base, exponent)
            } else {
                base
            }
        }

        private fun parseUnary(): BigDecimal {
            val op = peek()
            return if (op is Token.Operator && (op.symbol == '+' || op.symbol == '-')) {
                consume()
                val operand = parseUnary()
                if (op.symbol == '-') operand.negate() else operand
            } else {
                parsePostfix()
            }
        }

        private fun parsePostfix(): BigDecimal {
            var value = parsePrimary()
            while (true) {
                when (peek()) {
                    is Token.Percent -> {
                        consume()
                        value = value.divide(BigDecimal(100), MC)
                    }
                    is Token.Square -> {
                        consume()
                        value = value.multiply(value, MC)
                    }
                    is Token.Factorial -> {
                        consume()
                        value = factorialBigDecimal(value)
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parsePrimary(): BigDecimal {
            val token = peek() ?: throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
            return when (token) {
                is Token.Number -> {
                    consume()
                    token.value
                }
                is Token.Constant -> {
                    consume()
                    token.value
                }
                is Token.LeftParen -> {
                    consume()
                    val inner = parseExpression()
                    if (peek() !is Token.RightParen) {
                        throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
                    }
                    consume()
                    inner
                }
                is Token.Function -> {
                    consume()
                    val arg = if (peek() is Token.LeftParen) {
                        consume()
                        val inner = parseExpression()
                        if (peek() !is Token.RightParen) {
                            throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
                        }
                        consume()
                        inner
                    } else {
                        parseUnary()
                    }
                    applyFunction(token.name, arg)
                }
                else -> throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
            }
        }

        private fun applyFunction(name: String, arg: BigDecimal): BigDecimal {
            val d = arg.toDouble()
            if (d.isNaN() || d.isInfinite()) {
                throw CalcEvalException(CalculatorError.OVERFLOW)
            }

            return when (name) {
                "sqrt" -> {
                    if (arg < BigDecimal.ZERO) throw CalcEvalException(CalculatorError.DOMAIN_ERROR)
                    if (arg.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
                    val sq = sqrt(d)
                    toCleanBigDecimal(sq)
                }
                "ln" -> {
                    if (arg <= BigDecimal.ZERO) throw CalcEvalException(CalculatorError.DOMAIN_ERROR)
                    toCleanBigDecimal(ln(d))
                }
                "log" -> {
                    if (arg <= BigDecimal.ZERO) throw CalcEvalException(CalculatorError.DOMAIN_ERROR)
                    toCleanBigDecimal(log10(d))
                }
                "sin" -> {
                    if (angleUnit == AngleUnit.DEG) {
                        val mod = ((d % 360.0) + 360.0) % 360.0
                        if (abs(mod - 0.0) < 1e-12 || abs(mod - 180.0) < 1e-12) return BigDecimal.ZERO
                        if (abs(mod - 30.0) < 1e-12 || abs(mod - 150.0) < 1e-12) return BigDecimal("0.5")
                        if (abs(mod - 210.0) < 1e-12 || abs(mod - 330.0) < 1e-12) return BigDecimal("-0.5")
                        if (abs(mod - 90.0) < 1e-12) return BigDecimal.ONE
                        if (abs(mod - 270.0) < 1e-12) return BigDecimal("-1")
                        toCleanBigDecimal(sin(Math.toRadians(d)))
                    } else {
                        toCleanBigDecimal(sin(d))
                    }
                }
                "cos" -> {
                    if (angleUnit == AngleUnit.DEG) {
                        val mod = ((d % 360.0) + 360.0) % 360.0
                        if (abs(mod - 90.0) < 1e-12 || abs(mod - 270.0) < 1e-12) return BigDecimal.ZERO
                        if (abs(mod - 60.0) < 1e-12 || abs(mod - 300.0) < 1e-12) return BigDecimal("0.5")
                        if (abs(mod - 120.0) < 1e-12 || abs(mod - 240.0) < 1e-12) return BigDecimal("-0.5")
                        if (abs(mod - 0.0) < 1e-12) return BigDecimal.ONE
                        if (abs(mod - 180.0) < 1e-12) return BigDecimal("-1")
                        toCleanBigDecimal(cos(Math.toRadians(d)))
                    } else {
                        toCleanBigDecimal(cos(d))
                    }
                }
                "tan" -> {
                    if (angleUnit == AngleUnit.DEG) {
                        val mod = ((d % 180.0) + 180.0) % 180.0
                        if (abs(mod - 90.0) < 1e-10) throw CalcEvalException(CalculatorError.DOMAIN_ERROR)
                        if (abs(mod - 0.0) < 1e-12) return BigDecimal.ZERO
                        if (abs(mod - 45.0) < 1e-12) return BigDecimal.ONE
                        if (abs(mod - 135.0) < 1e-12) return BigDecimal("-1")
                        toCleanBigDecimal(tan(Math.toRadians(d)))
                    } else {
                        val c = cos(d)
                        if (abs(c) < 1e-14) throw CalcEvalException(CalculatorError.DOMAIN_ERROR)
                        toCleanBigDecimal(tan(d))
                    }
                }
                else -> throw CalcEvalException(CalculatorError.INVALID_EXPRESSION)
            }
        }

        private fun powerBigDecimal(base: BigDecimal, exponent: BigDecimal): BigDecimal {
            if (base.compareTo(BigDecimal.ZERO) == 0 && exponent < BigDecimal.ZERO) {
                throw CalcEvalException(CalculatorError.DIVIDE_BY_ZERO)
            }
            if (base.compareTo(BigDecimal.ZERO) == 0 && exponent.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ONE
            }
            // Exact integer power via BigDecimal when exponent is a small integer
            val expInt = try {
                exponent.stripTrailingZeros().intValueExact()
            } catch (_: ArithmeticException) {
                null
            }
            if (expInt != null && expInt in -999..999) {
                return base.pow(expInt, MC)
            }
            val b = base.toDouble()
            val e = exponent.toDouble()
            if (b < 0 && floor(e) != e) {
                throw CalcEvalException(CalculatorError.DOMAIN_ERROR)
            }
            val res = b.pow(e)
            return toCleanBigDecimal(res)
        }

        private fun factorialBigDecimal(value: BigDecimal): BigDecimal {
            val stripped = value.stripTrailingZeros()
            if (stripped < BigDecimal.ZERO || stripped.scale() > 0) {
                throw CalcEvalException(CalculatorError.DOMAIN_ERROR)
            }
            val n = try {
                stripped.intValueExact()
            } catch (_: ArithmeticException) {
                throw CalcEvalException(CalculatorError.OVERFLOW)
            }
            if (n > 170) throw CalcEvalException(CalculatorError.OVERFLOW)
            var acc = BigDecimal.ONE
            for (k in 2..n) {
                acc = acc.multiply(BigDecimal(k), MC)
            }
            return acc
        }

        private fun toCleanBigDecimal(value: Double): BigDecimal {
            if (value.isNaN()) throw CalcEvalException(CalculatorError.DOMAIN_ERROR)
            if (value.isInfinite()) throw CalcEvalException(CalculatorError.OVERFLOW)
            val roundedNearZero = if (abs(value) < 1e-14) 0.0 else value
            val bd = BigDecimal(roundedNearZero.toString()).round(DISPLAY_MC)
            return bd
        }
    }

    private fun cleanResult(value: BigDecimal): BigDecimal {
        val rounded = value.round(DISPLAY_MC)
        return if (rounded.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else rounded.stripTrailingZeros()
    }

    fun formatBigDecimal(value: BigDecimal): String {
        val cleaned = cleanResult(value)
        if (cleaned.compareTo(BigDecimal.ZERO) == 0) return "0"
        val absVal = cleaned.abs()
        val useScientific = absVal >= BigDecimal("1E12") ||
            (absVal > BigDecimal.ZERO && absVal < BigDecimal("1E-7"))
        return if (useScientific) {
            cleaned.round(MathContext(8, RoundingMode.HALF_UP)).toString()
        } else {
            cleaned.toPlainString()
        }
    }

    /**
     * Toggles sign of the trailing number or parenthesized expression in the input string.
     */
    fun toggleSign(expression: String): String {
        if (expression.isEmpty() || expression == "0") return "-"
        if (expression == "-") return ""

        // Find start of trailing number token
        var i = expression.length - 1
        while (i >= 0 && (expression[i].isDigit() || expression[i] == '.')) {
            i--
        }
        val numStart = i + 1
        if (numStart < expression.length) {
            val numberPart = expression.substring(numStart)
            // Check if preceded by `(-`
            if (numStart >= 2 && expression.substring(numStart - 2, numStart) == "(-") {
                return expression.substring(0, numStart - 2) + numberPart
            }
            // Check if the entire expression is `-number`
            if (numStart == 1 && expression[0] == '-') {
                return numberPart
            }
            val prefix = expression.substring(0, numStart)
            return if (prefix.isEmpty()) {
                "-$numberPart"
            } else {
                "$prefix(-$numberPart"
            }
        }
        return "$expression(-"
    }
}
