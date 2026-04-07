package com.example.auditoriaaplicaciones.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.example.auditoriaaplicaciones.ui.theme.AuditoriaAplicacionesTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// Data class to store the audit information across screens
data class NozzleData(val id: Int, var volumen: String = "", var presion: String = "")

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
    var volumen: String = "",
    
    // Nuevos campos para boquillas
    var nozzlesIzquierdo: List<NozzleData> = emptyList(),
    var nozzlesDerecho: List<NozzleData> = emptyList(),
    
    // GPS y Desplazamiento
    var tiempoDesplazamientoSegundos: Long = 0,
    var distanciaMetros: Float = 0f,
    var velocidadKmh: Float = 0f
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

    Column(modifier = modifier.fillMaxSize()) {
        // DEBUG TEXT VISIBLE EN PANTALLA
        Text(
            text = "DEBUG INFO -> Pantalla Actual: $currentScreen",
            color = androidx.compose.ui.graphics.Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp).background(androidx.compose.ui.graphics.Color.Yellow)
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
    var debugErrorMsg by remember { mutableStateOf("") }

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

        Spacer(modifier = Modifier.height(16.dp))

        // MOSTRAR ERRORES EN PANTALLA
        if (debugErrorMsg.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = debugErrorMsg,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

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
                        debugErrorMsg = "ERROR: Faltan campos básicos.\nEvaluador='${evaluador}'\nFinca='${finca}'\nLote='${lote}'"
                    } else if (loteNum !in 1..87) {
                        debugErrorMsg = "ERROR: Lote '${lote}' (numeral=$loteNum) no está entre 1 y 87."
                    } else {
                        debugErrorMsg = "Navegando a FormularioAuditoria..."
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
    val context = LocalContext.current
    var operador by remember { mutableStateOf(info.operador) }
    var codTractor by remember { mutableStateOf(info.codTractor) }
    var codImplemento by remember { mutableStateOf(info.codImplemento) }
    var potenciaTractor by remember { mutableStateOf(info.potenciaTractor) }
    var potenciaTdf by remember { mutableStateOf(info.potenciaTdf) }
    var formula by remember { mutableStateOf(info.formula) }
    var presion by remember { mutableStateOf(info.presion) }
    var volumen by remember { mutableStateOf(info.volumen) }

    // --- Lógica Boquillas Aleatorias ---
    var leftNozzles by remember { mutableStateOf(info.nozzlesIzquierdo) }
    var rightNozzles by remember { mutableStateOf(info.nozzlesDerecho) }

    LaunchedEffect(Unit) {
        if (leftNozzles.isEmpty()) {
            val randomLeft1 = Random.nextInt(1, 31)
            var randomLeft2 = Random.nextInt(1, 31)
            while (randomLeft2 == randomLeft1) { randomLeft2 = Random.nextInt(1, 31) }
            leftNozzles = listOf(NozzleData(randomLeft1), NozzleData(randomLeft2))
        }
        if (rightNozzles.isEmpty()) {
            val randomRight1 = Random.nextInt(31, 61)
            var randomRight2 = Random.nextInt(31, 61)
            while (randomRight2 == randomRight1) { randomRight2 = Random.nextInt(31, 61) }
            rightNozzles = listOf(NozzleData(randomRight1), NozzleData(randomRight2))
        }
    }

    // --- Lógica Medición GPS ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isMeasuring by remember { mutableStateOf(false) }
    var startLoc by remember { mutableStateOf<Location?>(null) }
    var endLoc by remember { mutableStateOf<Location?>(null) }
    var startTime by remember { mutableStateOf(0L) }
    
    var calcDistance by remember { mutableStateOf(0f) }
    var calcTimeSec by remember { mutableStateOf(0L) }
    var calcSpeed by remember { mutableStateOf(0f) }
    var calcError by remember { mutableStateOf(0f) }
    var gpsStatus by remember { mutableStateOf("Listo para medir") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            Toast.makeText(context, "Permiso GPS concedido. Ya puede medir.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso GPS denegado.", Toast.LENGTH_SHORT).show()
        }
    }

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

        Text(text = "Detalles de Auditoría", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        // --- Form Fields ---
        OutlinedTextField(value = operador, onValueChange = { operador = it }, label = { Text("Nombre operador") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = codTractor, onValueChange = { codTractor = it }, label = { Text("Cód. Tractor") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = codImplemento, onValueChange = { codImplemento = it }, label = { Text("Cód. Implemento") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = potenciaTractor, onValueChange = { potenciaTractor = it }, label = { Text("Potencia Tractor (HP)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = potenciaTdf, onValueChange = { potenciaTdf = it }, label = { Text("Potencia TDF/PPO (HP)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
        }

        OutlinedTextField(value = formula, onValueChange = { formula = it }, label = { Text("Fórmula a aplicar") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = presion, onValueChange = { presion = it }, label = { Text("Presión (PSI)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = volumen, onValueChange = { volumen = it }, label = { Text("Volumen aplicar") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // --- Boquillas Aleatorias ---
        Text(text = "Brazo Izquierdo (Aleatorio 1-30)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        leftNozzles.forEachIndexed { index, nozzle ->
            Text(text = "Boquilla #${nozzle.id}", modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nozzle.volumen,
                    onValueChange = { newVal -> leftNozzles = leftNozzles.toMutableList().apply { this[index] = nozzle.copy(volumen = newVal) } },
                    label = { Text("Volumen 30 s") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = nozzle.presion,
                    onValueChange = { newVal -> leftNozzles = leftNozzles.toMutableList().apply { this[index] = nozzle.copy(presion = newVal) } },
                    label = { Text("Presión (PSI)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Text(text = "Brazo Derecho (Aleatorio 31-60)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 16.dp))
        rightNozzles.forEachIndexed { index, nozzle ->
            Text(text = "Boquilla #${nozzle.id}", modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nozzle.volumen,
                    onValueChange = { newVal -> rightNozzles = rightNozzles.toMutableList().apply { this[index] = nozzle.copy(volumen = newVal) } },
                    label = { Text("Volumen 30 s") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = nozzle.presion,
                    onValueChange = { newVal -> rightNozzles = rightNozzles.toMutableList().apply { this[index] = nozzle.copy(presion = newVal) } },
                    label = { Text("Presión (PSI)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // --- Medición de Desplazamiento ---
        Text(text = "Medición de Desplazamiento (GPS)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = gpsStatus, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(bottom = 16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        enabled = !isMeasuring,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)),
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                return@FilledTonalButton
                            }

                            gpsStatus = "Obteniendo ubicación inicial..."
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                .addOnSuccessListener { location: Location? ->
                                    if (location != null) {
                                        startLoc = location
                                        startTime = System.currentTimeMillis()
                                        isMeasuring = true
                                        gpsStatus = "Midiendo... Camine y luego detenga."
                                        calcError = location.accuracy
                                    } else {
                                        gpsStatus = "Error: Active GPS/Espere señal"
                                    }
                                }
                        }
                    ) {
                        Text("Iniciar", color = androidx.compose.ui.graphics.Color.White)
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = isMeasuring,
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE53935)),
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                gpsStatus = "Obteniendo ubicación final..."
                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                    .addOnSuccessListener { location: Location? ->
                                        if (location != null && startLoc != null) {
                                            endLoc = location
                                            val eTime = System.currentTimeMillis()
                                            
                                            // Cálculos
                                            calcTimeSec = (eTime - startTime) / 1000
                                            calcDistance = startLoc!!.distanceTo(location)
                                            
                                            // v = d(m) / t(s) * 3.6 = km/h
                                            if (calcTimeSec > 0) {
                                                calcSpeed = (calcDistance / calcTimeSec) * 3.6f
                                            }
                                            
                                            calcError = (startLoc!!.accuracy + location.accuracy) / 2f
                                            isMeasuring = false
                                            gpsStatus = "Medición completada."
                                        } else {
                                            gpsStatus = "Error obteniendo posición final."
                                            isMeasuring = false
                                        }
                                    }
                            }
                        }
                    ) {
                        Text("Detener", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }

        // Resultados
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Tiempo: ${calcTimeSec}s")
            Text(text = "Distancia: ${String.format(Locale.US, "%.2f", calcDistance)}m")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Velocidad: ${String.format(Locale.US, "%.2f", calcSpeed)} km/h")
            Text(text = "Margen Error: ±${String.format(Locale.US, "%.1f", calcError)}m", color = androidx.compose.ui.graphics.Color.Gray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Volver")
            }
            Button(
                onClick = {
                    onContinue(
                        info.copy(
                            operador = operador,
                            codTractor = codTractor,
                            codImplemento = codImplemento,
                            potenciaTractor = potenciaTractor,
                            potenciaTdf = potenciaTdf,
                            formula = formula,
                            presion = presion,
                            volumen = volumen,
                            nozzlesIzquierdo = leftNozzles,
                            nozzlesDerecho = rightNozzles,
                            tiempoDesplazamientoSegundos = calcTimeSec,
                            distanciaMetros = calcDistance,
                            velocidadKmh = calcSpeed
                        )
                    )
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Finalizar")
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
