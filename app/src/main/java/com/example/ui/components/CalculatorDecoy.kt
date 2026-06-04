package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorDecoy(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var activeOperator by remember { mutableStateOf<String?>(null) }
    var isWaitingForSecondNumber by remember { mutableStateOf(false) }

    fun processDigit(digit: String) {
        if (display == "0" || isWaitingForSecondNumber) {
            display = digit
            isWaitingForSecondNumber = false
        } else {
            if (display.length < 12) {
                display += digit
            }
        }
    }

    fun processOperator(op: String) {
        val currentVal = display.toDoubleOrNull() ?: 0.0
        operand1 = currentVal
        activeOperator = op
        isWaitingForSecondNumber = true
    }

    fun calculateResult() {
        val op = activeOperator ?: return
        val num1 = operand1 ?: return
        val num2 = display.toDoubleOrNull() ?: return

        val result = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "×" -> num1 * num2
            "÷" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            else -> num2
        }

        display = if (result.isNaN()) {
            "Error"
        } else if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            val formatted = String.format("%.6f", result).trimEnd('0')
            if (formatted.endsWith(".")) formatted.substring(0, formatted.length - 1) else formatted
        }

        operand1 = null
        activeOperator = null
        isWaitingForSecondNumber = false
    }

    fun clearCalculator() {
        display = "0"
        operand1 = null
        activeOperator = null
        isWaitingForSecondNumber = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Invisible prompt hint for safety check
            Text(
                text = "SYSTEM ACTIVE",
                color = Color.White.copy(alpha = 0.15f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 12.dp)
            )

            // Dynamic Display Box
            Text(
                text = display,
                color = Color.White,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                maxLines = 1
            )

            // Rows of Circular Apple Keys
            val grid = listOf(
                listOf("C", "±", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            grid.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { char ->
                        val isOp = char in listOf("÷", "×", "-", "+", "=")
                        val isFn = char in listOf("C", "±", "%")
                        
                        val btnBg = when {
                            isOp -> Color(0xFFFF9F0A) // Orange
                            isFn -> Color(0xFFA5A5A5) // Light Gray
                            else -> Color(0xFF333333) // Dark Gray
                        }
                        
                        val btnTextCol = when {
                            isFn -> Color.Black
                            else -> Color.White
                        }

                        // Handle zero button double width
                        val weightMultiplier = if (char == "0") 2f else 1f

                        Box(
                            modifier = Modifier
                                .weight(weightMultiplier)
                                .aspectRatio(if (char == "0") 2.1f else 1f)
                                .clip(if (char == "0") RoundedCornerShape(40.dp) else CircleShape)
                                .background(btnBg)
                                .combinedClickable(
                                    onClick = {
                                        when (char) {
                                            "C" -> clearCalculator()
                                            "±" -> {
                                                if (display != "0" && !display.startsWith("-")) {
                                                    display = "-$display"
                                                } else if (display.startsWith("-")) {
                                                    display = display.substring(1)
                                                }
                                            }
                                            "%" -> {
                                                val dv = display.toDoubleOrNull() ?: 0.0
                                                display = (dv / 100.0).toString()
                                            }
                                            "÷", "×", "-", "+" -> processOperator(char)
                                            "=" -> {
                                                if (display == "1337") {
                                                    viewModel.activeCamouflageMode = false
                                                    viewModel.logDevConsole("Secured custom session exit triggered via code.")
                                                    Toast.makeText(context, "Session Unlocked!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    calculateResult()
                                                }
                                            }
                                            else -> processDigit(char)
                                        }
                                    },
                                    onLongClick = {
                                        if (char == "C") {
                                            viewModel.activeCamouflageMode = false
                                            viewModel.logDevConsole("Secured custom session exit triggered via emergency hold.")
                                            Toast.makeText(context, "Session Unlocked!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                color = btnTextCol,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
