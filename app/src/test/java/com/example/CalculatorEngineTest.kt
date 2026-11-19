package com.example

import com.example.domain.AngleUnit
import com.example.domain.CalculatorEngine
import com.example.domain.CalculatorError
import com.example.domain.EvaluationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    private fun evalSuccess(expr: String, angleUnit: AngleUnit = AngleUnit.DEG): String {
        val result = CalculatorEngine.evaluate(expr, angleUnit)
        assertTrue("Expected Success for '$expr', got $result", result is EvaluationResult.Success)
        return (result as EvaluationResult.Success).formatted
    }

    private fun evalError(expr: String, angleUnit: AngleUnit = AngleUnit.DEG): CalculatorError {
        val result = CalculatorEngine.evaluate(expr, angleUnit)
        assertTrue("Expected Error for '$expr', got $result", result is EvaluationResult.Error)
        return (result as EvaluationResult.Error).errorType
    }

    @Test
    fun `basic arithmetic and operator precedence`() {
        assertEquals("14", evalSuccess("2+3×4"))
        assertEquals("20", evalSuccess("(2+3)×4"))
        assertEquals("5", evalSuccess("15÷3"))
        assertEquals("7", evalSuccess("10−3"))
    }

    @Test
    fun `decimal floating point precision avoids binary drift`() {
        assertEquals("0.3", evalSuccess("0.1+0.2"))
        assertEquals("0.2", evalSuccess("0.3−0.1"))
        assertEquals("0.06", evalSuccess("0.2×0.3"))
        assertEquals("0.3", evalSuccess("0.9÷3"))
    }

    @Test
    fun `percentage calculations`() {
        assertEquals("0.5", evalSuccess("50%"))
        assertEquals("110", evalSuccess("100+10%"))
        assertEquals("75", evalSuccess("100−25%"))
        assertEquals("50", evalSuccess("200×25%"))
    }

    @Test
    fun `negative numbers and sign toggle`() {
        assertEquals("-5", evalSuccess("-5"))
        assertEquals("2", evalSuccess("-5+7"))
        assertEquals("-12", evalSuccess("3×(-4)"))
        assertEquals("-5", CalculatorEngine.toggleSign("5"))
        assertEquals("5", CalculatorEngine.toggleSign("-5"))
        assertEquals("10+(-5", CalculatorEngine.toggleSign("10+5"))
        assertEquals("10+5", CalculatorEngine.toggleSign("10+(-5"))
    }

    @Test
    fun `scientific functions deg and rad`() {
        assertEquals("0.5", evalSuccess("sin(30)", AngleUnit.DEG))
        assertEquals("0.5", evalSuccess("cos(60)", AngleUnit.DEG))
        assertEquals("1", evalSuccess("tan(45)", AngleUnit.DEG))
        assertEquals("0", evalSuccess("sin(π)", AngleUnit.RAD))
        assertEquals("-1", evalSuccess("cos(π)", AngleUnit.RAD))
        assertEquals("3", evalSuccess("√(9)"))
        assertEquals("16", evalSuccess("4²"))
        assertEquals("8", evalSuccess("2^3"))
        assertEquals("120", evalSuccess("5!"))
        assertEquals("2", evalSuccess("log(100)"))
        assertEquals("1", evalSuccess("ln(e)"))
    }

    @Test
    fun `implicit multiplication works cleanly`() {
        assertEquals("12", evalSuccess("2(6)"))
        assertEquals("15", evalSuccess("(2+1)(3+2)"))
    }

    @Test
    fun `error handling for divide by zero domain and syntax`() {
        assertEquals(CalculatorError.DIVIDE_BY_ZERO, evalError("10÷0"))
        assertEquals(CalculatorError.DOMAIN_ERROR, evalError("√(-9)"))
        assertEquals(CalculatorError.DOMAIN_ERROR, evalError("ln(0)"))
        assertEquals(CalculatorError.DOMAIN_ERROR, evalError("tan(90)", AngleUnit.DEG))
        assertEquals(CalculatorError.DOMAIN_ERROR, evalError("(-3)!"))
        assertEquals(CalculatorError.INVALID_EXPRESSION, evalError("5++"))
    }
}
