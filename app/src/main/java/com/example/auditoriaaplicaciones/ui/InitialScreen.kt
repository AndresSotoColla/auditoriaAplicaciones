package com.example.auditoriaaplicaciones.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.auditoriaaplicaciones.ui.theme.AuditoriaAplicacionesTheme
import java.text.SimpleDateFormat
import java.util.*

// Data class to store the audit information across screens
data class AuditoriaInfo(
    var evaluador: String = "",
    var fecha: Long = System.currentTimeMillis(),
    var hora: String = "",
    var lote: String = "",
    var finca: String = "",
    var operador: String = "",
    var codTractor: String = "",
    var codImplemento: String = "",
    var potenciaTractor: String = "",
    var potenciaTdf: String = "",
    var formula: String = "",
    var presion: String = "",
    var volumen: String = ""
)

@Composable
fun InitialScreen(
    modifier: Modifier = Modifier,
    onAuditoriaClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onDescargarExcelClick: () -> Unit
) {
    var showSelectionDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("Menu") }
    var auditoriaInfo by remember { mutableStateOf(AuditoriaInfo()) }
    val context = LocalContext.current

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
                    onContinue = { info ->
                        Toast.makeText(context, "Navigating to Formulario...", Toast.LENGTH_SHORT).show()
                        auditoriaInfo = info
                        currentScreen = "FormularioAuditoria"
                    }
                )
            }
            "FormularioAuditoria" -> {
                FormularioAuditoriaScreen(
                    info = auditoriaInfo,
                    onBack = { currentScreen = "DatosGenerales" },
                    onContinue = { finalInfo ->
                        auditoriaInfo = finalInfo
                        /* Continuar a la siguiente lógica */
                    }
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
    onContinue: (AuditoriaInfo) -> Unit
) {
    var evaluador by remember { mutableStateOf("") }
    var fecha by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var hour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var lote by remember { mutableStateOf("") }
    var finca by remember { mutableStateOf("") }

    val context = LocalContext.current
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

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
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
            value = String.format("%02d:%02d", hour, minute),
            onValueChange = {},
            label = { Text("Hora") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.Schedule, contentDescription = "Seleccionar hora")
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
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    lote = it 
                }
            },
            label = { Text("Lote (01 - 87)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = lote.isNotEmpty() && (lote.toIntOrNull() ?: 0) !in 1..87,
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
                onClick = {
                    val loteNum = lote.toIntOrNull() ?: 0
                    if (evaluador.isBlank() || finca.isBlank() || lote.isBlank()) {
                        Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                    } else if (loteNum !in 1..87) {
                        Toast.makeText(context, "El lote debe estar entre 1 y 87", Toast.LENGTH_SHORT).show()
                    } else {
                        onContinue(
                            AuditoriaInfo(
                                evaluador = evaluador,
                                fecha = fecha,
                                hora = String.format("%02d:%02d", hour, minute),
                                finca = finca,
                                lote = lote
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Continuar")
            }
        }
    }
}

@Composable
fun FormularioAuditoriaScreen(
    info: AuditoriaInfo,
    onBack: () -> Unit,
    onContinue: (AuditoriaInfo) -> Unit
) {
    var operador by remember { mutableStateOf("") }
    var codTractor by remember { mutableStateOf("") }
    var codImplemento by remember { mutableStateOf("") }
    var potenciaTractor by remember { mutableStateOf("") }
    var potenciaTdf by remember { mutableStateOf("") }
    var formula by remember { mutableStateOf("") }
    var presion by remember { mutableStateOf("") }
    var volumen by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Resumen de Auditoría", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Eval: ${info.evaluador}", fontSize = 14.sp)
                    Text(text = "Finca: ${info.finca}", fontSize = 14.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Fecha: ${dateFormatter.format(Date(info.fecha))}", fontSize = 14.sp)
                    Text(text = "Hora: ${info.hora}", fontSize = 14.sp)
                }
                Text(text = "Lote: ${info.lote}", fontSize = 14.sp)
            }
        }

        Text(
            text = "Detalles de Auditoría",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // --- Form Fields ---
        OutlinedTextField(
            value = operador,
            onValueChange = { operador = it },
            label = { Text("Nombre operador") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = codTractor,
                onValueChange = { codTractor = it },
                label = { Text("Cód. Tractor") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = codImplemento,
                onValueChange = { codImplemento = it },
                label = { Text("Cód. Implemento") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = potenciaTractor,
                onValueChange = { potenciaTractor = it },
                label = { Text("Potencia Tractor (HP)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = potenciaTdf,
                onValueChange = { potenciaTdf = it },
                label = { Text("Potencia TDF/PPO (HP)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        OutlinedTextField(
            value = formula,
            onValueChange = { formula = it },
            label = { Text("Fórmula a aplicar") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = presion,
                onValueChange = { presion = it },
                label = { Text("Presión (PSI)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = volumen,
                onValueChange = { volumen = it },
                label = { Text("Volumen aplicar") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

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
                onClick = {
                    if (operador.isBlank() || formula.isBlank() || volumen.isBlank()) {
                        Toast.makeText(context, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
                    } else {
                        onContinue(
                            info.copy(
                                operador = operador,
                                codTractor = codTractor,
                                codImplemento = codImplemento,
                                potenciaTractor = potenciaTractor,
                                potenciaTdf = potenciaTdf,
                                formula = formula,
                                presion = presion,
                                volumen = volumen
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
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
