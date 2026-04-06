package com.example.auditoriaaplicaciones.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.auditoriaaplicaciones.ui.theme.AuditoriaAplicacionesTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InitialScreen(
    modifier: Modifier = Modifier,
    onAuditoriaClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onDescargarExcelClick: () -> Unit
) {
    var showSelectionDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("Menu") }

    Box(modifier = modifier.fillMaxSize()) {
        when (currentScreen) {
            "Menu" -> {
                MainMenu(
                    onAuditoriaClick = { showSelectionDialog = true },
                    onHistorialClick = onHistorialClick,
                    onDescargarExcelClick = onDescargarExcelClick
                )

                if (showSelectionDialog) {
                    SelectionDialog(
                        onDismiss = { showSelectionDialog = false },
                        onOptionSelected = { option ->
                            showSelectionDialog = false
                            if (option == "Spray Boom") {
                                currentScreen = "SprayBoom"
                            }
                        }
                    )
                }
            }
            "SprayBoom" -> {
                SprayBoomChecklist(
                    onBack = { currentScreen = "Menu" },
                    onContinue = { currentScreen = "DatosGenerales" }
                )
            }
            "DatosGenerales" -> {
                DatosGeneralesScreen(
                    onBack = { currentScreen = "SprayBoom" },
                    onContinue = { /* Lógica para continuar */ }
                )
            }
        }
    }
}

@Composable
fun MainMenu(
    onAuditoriaClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onDescargarExcelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Auditoría de Aplicaciones",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        MenuButton(
            text = "Auditoría Aplicaciones",
            icon = Icons.Default.Checklist,
            onClick = onAuditoriaClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Ver Historial",
            icon = Icons.Default.History,
            onClick = onHistorialClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Descargar Excel",
            icon = Icons.Default.Download,
            onClick = onDescargarExcelClick,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    }
}

@Composable
fun SelectionDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccione la Auditoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onOptionSelected("Mezclas") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("AUDITORÍA MEZCLAS", fontSize = 16.sp)
                }
                Button(
                    onClick = { onOptionSelected("Spray Boom") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("AUDITORÍA SPRAY BOOM", fontSize = 16.sp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SprayBoomChecklist(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val items = listOf(
        "Cronómetro",
        "Probeta Granulada",
        "Papel Hidrosensible",
        "Kit de Calibración",
        "Cinta Métrica"
    )
    val checkedStates = remember { mutableStateListOf(*Array(items.size) { false }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "¿Cuenta con los siguientes materiales?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(modifier = Modifier.weight(1.0f)) {
            items(items.size) { index ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = checkedStates[index],
                        onCheckedChange = { checkedStates[index] = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = items[index], fontSize = 18.sp)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Volver")
            }
            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continuar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosGeneralesScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    var evaluador by remember { mutableStateOf("") }
    var fecha by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var lote by remember { mutableStateOf("") }
    var finca by remember { mutableStateOf("") }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fecha)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { fecha = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Datos Generales",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = evaluador,
            onValueChange = { evaluador = it },
            label = { Text("Evaluador") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = dateFormatter.format(Date(fecha)),
            onValueChange = {},
            label = { Text("Fecha") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = lote,
            onValueChange = { 
                if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.toInt() in 1..87)) {
                    lote = it 
                }
            },
            label = { Text("Lote (01 - 87)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = finca,
            onValueChange = { finca = it },
            label = { Text("Finca") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Volver")
            }
            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f),
                enabled = evaluador.isNotBlank() && lote.isNotBlank() && finca.isNotBlank()
            ) {
                Text("Continuar")
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColorFor(containerColor)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InitialScreenPreview() {
    AuditoriaAplicacionesTheme {
        InitialScreen(
            onAuditoriaClick = {},
            onHistorialClick = {},
            onDescargarExcelClick = {}
        )
    }
}
