package com.iftm.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BackgroundColor) {
                    CalculatorScreen()
                }
            }
        }
    }
}

// ---- Paleta de cores personalizada ----
val BackgroundColor = Color(0xFF1B1B1F)
val DisplayBackground = Color(0xFF101014)
val DigitButtonColor = Color(0xFF2C2C34)
val OperatorButtonColor = Color(0xFFFF9500)
val ScientificButtonColor = Color(0xFF3A3A42)
val ClearButtonColor = Color(0xFFD32F2F)
val EqualsButtonColor = Color(0xFF34C759)
val TextColorLight = Color(0xFFF5F5F5)

private enum class ButtonKind { DIGIT, OPERATOR, CLEAR, EQUALS, SCIENTIFIC }

@Composable
fun CalculatorScreen() {
    var expression by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scientificMode by remember { mutableStateOf(false) }

    // Retorna o segmento numérico atual (após o último operador)
    fun currentNumberSegment(): String {
        val lastOpIndex = expression.indexOfLast { it in "+-*/^" }
        return if (lastOpIndex == -1) expression else expression.substring(lastOpIndex + 1)
    }

    fun onDigit(d: String) {
        errorMessage = null
        val seg = currentNumberSegment()
        // Trava: evita zeros à esquerda desnecessários (ex.: "01" -> "1")
        expression = if (seg == "0") expression.dropLast(1) + d else expression + d
    }

    fun onDecimal() {
        errorMessage = null
        val seg = currentNumberSegment()
        // Trava: impede múltiplos pontos decimais no mesmo número
        if (seg.contains(".")) return
        expression += if (seg.isEmpty()) "0." else "."
    }

    fun onOperator(op: String) {
        errorMessage = null
        if (expression.isEmpty()) return // não permite iniciar com operador
        val lastChar = expression.last()
        // Trava: impede operadores matemáticos consecutivos
        if (lastChar in "+-*/^") return
        if (lastChar == '.') return // não permite operador logo após um ponto solto
        expression += op
    }

    fun onFunction(fnName: String) {
        errorMessage = null
        val seg = currentNumberSegment()
        val value = seg.toDoubleOrNull() ?: return
        try {
            val result = when (fnName) {
                "sin" -> CalculatorEngine.sinDeg(value)
                "cos" -> CalculatorEngine.cosDeg(value)
                "tan" -> CalculatorEngine.tanDeg(value)
                "log" -> CalculatorEngine.log10Safe(value)
                else -> value
            }
            val prefixLen = expression.length - seg.length
            expression = expression.substring(0, prefixLen) + CalculatorEngine.formatResult(result)
        } catch (e: CalculatorEngine.CalculatorException) {
            errorMessage = e.message
            expression = ""
        }
    }

    fun onClear() {
        expression = ""
        errorMessage = null
    }

    fun onBackspace() {
        if (expression.isNotEmpty()) expression = expression.dropLast(1)
        errorMessage = null
    }

    fun onEquals() {
        if (expression.isEmpty()) return
        if (expression.last() in "+-*/^") return // expressão incompleta
        try {
            val result = CalculatorEngine.evaluate(expression)
            expression = CalculatorEngine.formatResult(result)
        } catch (e: CalculatorEngine.CalculatorException) {
            // Trata cenários de exceção (ex.: divisão por zero) sem derrubar o app
            errorMessage = e.message ?: "Erro"
            expression = ""
        } catch (e: Exception) {
            errorMessage = "Erro"
            expression = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // ---- Visor ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DisplayBackground, RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = errorMessage ?: expression.ifEmpty { "0" },
                color = if (errorMessage != null) OperatorButtonColor else TextColorLight,
                fontSize = 44.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---- Alternador do modo científico (teclado expansível) ----
        Button(
            onClick = { scientificMode = !scientificMode },
            colors = ButtonDefaults.buttonColors(containerColor = ScientificButtonColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (scientificMode) "Ocultar funções científicas  ▲" else "Funções científicas (sen, cos, tan, log, xʸ)  ▼",
                color = TextColorLight,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (scientificMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("sin", ButtonKind.SCIENTIFIC, Modifier.weight(1f)) { onFunction("sin") }
                CalculatorButton("cos", ButtonKind.SCIENTIFIC, Modifier.weight(1f)) { onFunction("cos") }
                CalculatorButton("tan", ButtonKind.SCIENTIFIC, Modifier.weight(1f)) { onFunction("tan") }
                CalculatorButton("log", ButtonKind.SCIENTIFIC, Modifier.weight(1f)) { onFunction("log") }
                CalculatorButton("xʸ", ButtonKind.SCIENTIFIC, Modifier.weight(1f)) { onOperator("^") }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ---- Grade principal ----
        val row1 = listOf(Triple("C", ButtonKind.CLEAR) { onClear() }, Triple("⌫", ButtonKind.CLEAR) { onBackspace() }, Triple("/", ButtonKind.OPERATOR) { onOperator("/") })
        val row2 = listOf(Triple("7", ButtonKind.DIGIT) { onDigit("7") }, Triple("8", ButtonKind.DIGIT) { onDigit("8") }, Triple("9", ButtonKind.DIGIT) { onDigit("9") }, Triple("*", ButtonKind.OPERATOR) { onOperator("*") })
        val row3 = listOf(Triple("4", ButtonKind.DIGIT) { onDigit("4") }, Triple("5", ButtonKind.DIGIT) { onDigit("5") }, Triple("6", ButtonKind.DIGIT) { onDigit("6") }, Triple("-", ButtonKind.OPERATOR) { onOperator("-") })
        val row4 = listOf(Triple("1", ButtonKind.DIGIT) { onDigit("1") }, Triple("2", ButtonKind.DIGIT) { onDigit("2") }, Triple("3", ButtonKind.DIGIT) { onDigit("3") }, Triple("+", ButtonKind.OPERATOR) { onOperator("+") })
        val row5 = listOf(Triple(".", ButtonKind.DIGIT) { onDecimal() }, Triple("0", ButtonKind.DIGIT) { onDigit("0") }, Triple("=", ButtonKind.EQUALS) { onEquals() })

        listOf(row1, row2, row3, row4, row5).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, kind, action) ->
                    CalculatorButton(label, kind, Modifier.weight(1f), action)
                }
            }
        }
    }
}

@Composable
private fun CalculatorButton(
    label: String,
    kind: ButtonKind,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when (kind) {
        ButtonKind.DIGIT -> DigitButtonColor
        ButtonKind.OPERATOR -> OperatorButtonColor
        ButtonKind.CLEAR -> ClearButtonColor
        ButtonKind.EQUALS -> EqualsButtonColor
        ButtonKind.SCIENTIFIC -> ScientificButtonColor
    }
    Button(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.1f),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(
            text = label,
            color = TextColorLight,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
