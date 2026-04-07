package com.example.auditoriaaplicaciones.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
// Deleted Parcelize import
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

import java.io.Serializable

// Para soportar guardado de estado ante rotaciones, los usamos como Serializable
data class NozzleData(val id: Int, var volumen: String = "", var presion: String = "") : Serializable

data class InsumoData(
    val codigo: String,
    val descripcion: String,
    val numero: Int,
    val insumo: String,
    val cantidad: String,
    val unidad: String
)

fun parseCsv(context: android.content.Context): List<InsumoData> {
    val list = mutableListOf<InsumoData>()
    try {
        val inputStream = context.resources.openRawResource(com.example.auditoriaaplicaciones.R.raw.datos)
        val reader = inputStream.bufferedReader()
        reader.readLine() // drop header
        reader.forEachLine { line ->
            val tokens = line.split(";")
            if (tokens.size >= 6) {
                list.add(InsumoData(
                    codigo = tokens[0],
                    descripcion = tokens[1],
                    numero = tokens[2].toIntOrNull() ?: 0,
                    insumo = tokens[3],
                    cantidad = tokens[4],
                    unidad = tokens[5]
                ))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

data class AuditoriaInfo(
    var id: String = UUID.randomUUID().toString(),
    var tipoAuditoria: String = "",
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
    
    var mezclador: String = "",
    var formulaMezclar: String = "",
    
    // Boquillas
    var nozzlesIzquierdo: List<NozzleData> = emptyList(),
    var nozzlesDerecho: List<NozzleData> = emptyList(),
    
    // GPS y Desplazamiento
    var tiempoDesplazamientoSegundos: Long = 0,
    var distanciaMetros: Float = 0f,
    var velocidadKmh: Float = 0f,

    // Cuestionario Final
    var boquillasTapadas: Boolean? = null,
    var boquillasTapadasNum: String = "",
    var presenciaPersonal: Boolean? = null,
    var alturaUniforme: Boolean? = null,
    var estadoVia: String = "", // "BUENO", "REGULAR", "MALO"
    var papelHidrosensible: Boolean = false,
    var papelGotas1cm: String = "",
    var papelGotasCuarto: String = ""
) : Serializable

@Composable
fun blackTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedBorderColor = Color.Black,
    unfocusedBorderColor = Color.Black,
    focusedLabelColor = Color.Black,
    unfocusedLabelColor = Color.Black,
    disabledTextColor = Color.Black,
    disabledBorderColor = Color.Black,
    disabledLabelColor = Color.Black,
    cursorColor = Color.Black
)

@Composable
fun BackgroundVideo() {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/raw/bg_video")
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun InitialScreen(
    modifier: Modifier = Modifier,
    onAuditoriaClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onDescargarExcelClick: () -> Unit
) {
    var showSelectionDialog by rememberSaveable { mutableStateOf(false) }
    var currentScreen by rememberSaveable { mutableStateOf("Menu") }
    var auditoriaInfo by rememberSaveable { mutableStateOf(AuditoriaInfo()) }
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        BackgroundVideo()
        
        Column(modifier = Modifier.fillMaxSize()) {
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
                        onHistorialClick = { currentScreen = "Historial" },
                        onDescargarExcelClick = { ExportManager.exportToExcel(context) }
                    )

                    if (showSelectionDialog) {
                        SelectionDialog(
                            onDismiss = { showSelectionDialog = false },
                            onOptionSelected = { option ->
                                showSelectionDialog = false
                                auditoriaInfo = AuditoriaInfo(tipoAuditoria = option)
                                if (option == "Spray Boom") {
                                    currentScreen = "SprayBoom"
                                } else if (option == "Mezclas") {
                                    currentScreen = "DatosGenerales"
                                }
                            }
                        )
                    }
                }
                "Historial" -> {
                    HistorialScreen(
                        onBack = { currentScreen = "Menu" }
                    )
                }
                "SprayBoom" -> {
                    SprayBoomChecklist(
                        onBack = { currentScreen = "Menu" },
                        onContinue = { currentScreen = "DatosGenerales" }
                    )
                }
                "DatosGenerales" -> {
                    DatosGeneralesScreen(
                        infoInicial = auditoriaInfo,
                        onBack = { 
                            if (auditoriaInfo.tipoAuditoria == "Mezclas") currentScreen = "Menu"
                            else currentScreen = "SprayBoom"
                        },
                        onContinue = { info ->
                            Toast.makeText(context, "Navegando a Formulario...", Toast.LENGTH_SHORT).show()
                            auditoriaInfo = info
                            if (info.tipoAuditoria == "Mezclas") {
                                currentScreen = "FormularioMezclas"
                            } else {
                                currentScreen = "FormularioAuditoria"
                            }
                        }
                    )
                }
                "FormularioAuditoria" -> {
                    FormularioAuditoriaScreen(
                        info = auditoriaInfo,
                        onBack = { currentScreen = "DatosGenerales" },
                        onContinue = { finalInfo ->
                            auditoriaInfo = finalInfo
                            StorageManager.saveAuditoria(context, finalInfo)
                            Toast.makeText(context, "¡Auditoría Guardada!", Toast.LENGTH_LONG).show()
                            currentScreen = "Menu"
                        }
                    )
                }
                "FormularioMezclas" -> {
                    FormularioMezclasScreen(
                        info = auditoriaInfo,
                        onBack = { currentScreen = "DatosGenerales" },
                        onContinue = { finalInfo ->
                            auditoriaInfo = finalInfo
                            StorageManager.saveAuditoria(context, finalInfo)
                            Toast.makeText(context, "¡Auditoría de Mezclas Guardada!", Toast.LENGTH_LONG).show()
                            currentScreen = "Menu"
                        }
                    )
                }
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
        Spacer(modifier = Modifier.weight(1f))
        
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Auditoria APLICACIONES",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                text = "auditorias generales",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        MenuButton(
            text = "Auditoría Aplicaciones",
            icon = Icons.Default.Checklist,
            onClick = onAuditoriaClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Ver Historial",
            icon = Icons.Default.History,
            onClick = onHistorialClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Descargar Excel",
            icon = Icons.Default.Download,
            onClick = onDescargarExcelClick
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "CT&A 2026",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5E1C8), contentColor = Color.Black)
                ) {
                    Text("AUDITORÍA MEZCLAS", fontSize = 16.sp)
                }
                Button(
                    onClick = { onOptionSelected("Spray Boom") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5E1C8), contentColor = Color.Black)
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
    
    // Saveable Custom Saver or simpler approach: ArrayList
    var checkedStatesValues by rememberSaveable { mutableStateOf(BooleanArray(items.size) { false }) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E1C8), contentColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "¿Cuenta con los siguientes materiales?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    items.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = checkedStatesValues[index],
                                onCheckedChange = { isChecked ->
                                    val nArr = checkedStatesValues.copyOf()
                                    nArr[index] = isChecked
                                    checkedStatesValues = nArr
                                },
                                colors = CheckboxDefaults.colors(checkmarkColor = Color.White, checkedColor = Color.Black, uncheckedColor = Color.Black)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = item, fontSize = 18.sp, color = Color.Black)
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        Text("Volver")
                    }
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text("Continuar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosGeneralesScreen(
    infoInicial: AuditoriaInfo,
    onBack: () -> Unit,
    onContinue: (AuditoriaInfo) -> Unit
) {
    var evaluador by rememberSaveable { mutableStateOf(infoInicial.evaluador) }
    var fecha by rememberSaveable { mutableStateOf(infoInicial.fecha) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    
    val cal = Calendar.getInstance().apply { timeInMillis = infoInicial.fecha }
    var hour by rememberSaveable { mutableStateOf(if (infoInicial.hora.isNotEmpty()) infoInicial.hora.split(":")[0].toInt() else cal.get(Calendar.HOUR_OF_DAY)) }
    var minute by rememberSaveable { mutableStateOf(if (infoInicial.hora.isNotEmpty()) infoInicial.hora.split(":")[1].toInt() else cal.get(Calendar.MINUTE)) }
    
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var lote by rememberSaveable { mutableStateOf(infoInicial.lote) }
    var finca by rememberSaveable { mutableStateOf(infoInicial.finca) }
    var debugErrorMsg by rememberSaveable { mutableStateOf("") }

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

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E1C8), contentColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Datos Generales",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

        OutlinedTextField(
            value = evaluador,
            onValueChange = { evaluador = it },
            label = { Text("Evaluador") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
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
            shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
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
            shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
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
            shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
        )

        OutlinedTextField(
            value = finca,
            onValueChange = { finca = it },
            label = { Text("Finca") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
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
                                
                                val updatedInfo = infoInicial.copy(
                                    evaluador = evaluador,
                                    fecha = fecha,
                                    hora = String.format("%02d:%02d", hour, minute),
                                    finca = finca,
                                    lote = lote
                                )
                                onContinue(updatedInfo)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text("Continuar")
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FormularioAuditoriaScreen(
    info: AuditoriaInfo,
    onBack: () -> Unit,
    onContinue: (AuditoriaInfo) -> Unit
) {
    var operador by rememberSaveable { mutableStateOf(info.operador) }
    var codTractor by rememberSaveable { mutableStateOf(info.codTractor) }
    var codImplemento by rememberSaveable { mutableStateOf(info.codImplemento) }
    var potenciaTractor by rememberSaveable { mutableStateOf(info.potenciaTractor) }
    var potenciaTdf by rememberSaveable { mutableStateOf(info.potenciaTdf) }
    var formula by rememberSaveable { mutableStateOf(info.formula) }
    var presion by rememberSaveable { mutableStateOf(info.presion) }
    var volumen by rememberSaveable { mutableStateOf(info.volumen) }
    
    val context = LocalContext.current
    val insumosList = remember { parseCsv(context) }
    val codigosUnicos = remember(insumosList) { insumosList.map { it.codigo }.distinct() }
    var expandedFormula by rememberSaveable { mutableStateOf(false) }
    // Initialize description if formula is passed from before
    var selectedDescripcion by rememberSaveable { mutableStateOf(
        insumosList.firstOrNull { it.codigo == info.formula }?.descripcion ?: ""
    ) }

    // --- Lógica Boquillas Aleatorias ---
    var leftNozzles by rememberSaveable { mutableStateOf(info.nozzlesIzquierdo) }
    var rightNozzles by rememberSaveable { mutableStateOf(info.nozzlesDerecho) }

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
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isMeasuring by rememberSaveable { mutableStateOf(false) }
    var startTime by rememberSaveable { mutableStateOf(0L) }
    
    var startLat by rememberSaveable { mutableStateOf(0.0) }
    var startLng by rememberSaveable { mutableStateOf(0.0) }
    var startAcc by rememberSaveable { mutableStateOf(0f) }
    
    var calcDistance by rememberSaveable { mutableStateOf(info.distanciaMetros) }
    var calcTimeSec by rememberSaveable { mutableStateOf(info.tiempoDesplazamientoSegundos) }
    var calcSpeed by rememberSaveable { mutableStateOf(info.velocidadKmh) }
    var calcError by rememberSaveable { mutableStateOf(0f) }
    var gpsStatus by rememberSaveable { mutableStateOf(if (calcDistance > 0) "Medición completada." else "Listo para medir") }

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

    // --- Final Cuestionario ---
    var boquillasTapadas by rememberSaveable { mutableStateOf(info.boquillasTapadas) }
    var boquillasTapadasNum by rememberSaveable { mutableStateOf(info.boquillasTapadasNum) }
    
    var presenciaPersonal by rememberSaveable { mutableStateOf(info.presenciaPersonal) }
    var alturaUniforme by rememberSaveable { mutableStateOf(info.alturaUniforme) }
    var estadoVia by rememberSaveable { mutableStateOf(info.estadoVia) }
    
    var papelHidro by rememberSaveable { mutableStateOf(info.papelHidrosensible) }
    var papelGotas1cm by rememberSaveable { mutableStateOf(info.papelGotas1cm) }
    var papelGotasCuarto by rememberSaveable { mutableStateOf(info.papelGotasCuarto) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E1C8), contentColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Header Section ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f), contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Resumen de Auditoría", fontWeight = FontWeight.Bold, color = Color.Black)
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

                Text(text = "Detalles de Auditoría", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)

                // --- Form Fields ---
                OutlinedTextField(value = operador, onValueChange = { operador = it }, label = { Text("Nombre operador") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())
                OutlinedTextField(value = codTractor, onValueChange = { codTractor = it }, label = { Text("Cód. Tractor") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())
                OutlinedTextField(value = codImplemento, onValueChange = { codImplemento = it }, label = { Text("Cód. Implemento") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())
                OutlinedTextField(value = potenciaTractor, onValueChange = { potenciaTractor = it }, label = { Text("Potencia Tractor (HP)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())
                OutlinedTextField(value = potenciaTdf, onValueChange = { potenciaTdf = it }, label = { Text("Potencia TDF/PPO (HP)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())
                
                ExposedDropdownMenuBox(
                    expanded = expandedFormula,
                    onExpandedChange = { expandedFormula = !expandedFormula },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = formula,
                        onValueChange = { 
                            formula = it
                            expandedFormula = true
                        },
                        label = { Text("Fórmula a aplicar") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )
                    
                    val filteredCodigos = codigosUnicos.filter { it.contains(formula, ignoreCase = true) }.take(10)
                    if (filteredCodigos.isNotEmpty() && expandedFormula) {
                        DropdownMenu(
                            expanded = expandedFormula,
                            onDismissRequest = { expandedFormula = false },
                            modifier = Modifier.exposedDropdownSize()
                        ) {
                            filteredCodigos.forEach { cod ->
                                DropdownMenuItem(
                                    text = { Text(cod, color = Color.Black) },
                                    onClick = {
                                        formula = cod
                                        expandedFormula = false
                                        val match = insumosList.firstOrNull { it.codigo == cod }
                                        selectedDescripcion = match?.descripcion ?: ""
                                        
                                        val aguaMatch = insumosList.firstOrNull { it.codigo == cod && it.insumo.equals("AGUA", ignoreCase = true) }
                                        if (aguaMatch != null) {
                                            volumen = aguaMatch.cantidad
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                if (selectedDescripcion.isNotEmpty()) {
                    Text(text = "Descripción: $selectedDescripcion", color = Color.DarkGray, fontSize = 14.sp)
                }

                OutlinedTextField(value = presion, onValueChange = { presion = it }, label = { Text("Presión (PSI)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())
                OutlinedTextField(value = volumen, onValueChange = { volumen = it }, label = { Text("Volumen aplicar") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.2f))

                // --- Botón de Video Demostrativo ---
                var showDemoVideo by rememberSaveable { mutableStateOf(false) }
                Button(
                    onClick = { showDemoVideo = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4169E1), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = "Video", modifier = Modifier.padding(end = 8.dp))
                    Text("VIDEO DEMOSTRATIVO", fontWeight = FontWeight.Bold)
                }

                if (showDemoVideo) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = { showDemoVideo = false }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp)
                        ) {
                            val exoPlayer = remember {
                                ExoPlayer.Builder(context).build().apply {
                                    val uri = Uri.parse("android.resource://${context.packageName}/raw/demo_video")
                                    setMediaItem(MediaItem.fromUri(uri))
                                    playWhenReady = true
                                    prepare()
                                }
                            }
                            DisposableEffect(Unit) {
                                onDispose { exoPlayer.release() }
                            }
                            AndroidView(
                                factory = {
                                    PlayerView(it).apply {
                                        player = exoPlayer
                                        useController = true
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                },
                                modifier = Modifier.fillMaxSize().background(Color.Black)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.2f))

                // --- Boquillas Aleatorias ---
                Text(text = "Brazo Izquierdo (Aleatorio 1-30)", fontWeight = FontWeight.Bold, color = Color.Black)
                leftNozzles.forEachIndexed { index, nozzle ->
                    Text(text = "Boquilla #${nozzle.id}", modifier = Modifier.padding(top = 8.dp))
                    OutlinedTextField(
                        value = nozzle.volumen,
                        onValueChange = { newValue -> leftNozzles = leftNozzles.toMutableList().apply { this[index] = nozzle.copy(volumen = newValue) } },
                        label = { Text("Volumen (ml)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
                    )
                    OutlinedTextField(
                        value = nozzle.presion,
                        onValueChange = { newValue -> leftNozzles = leftNozzles.toMutableList().apply { this[index] = nozzle.copy(presion = newValue) } },
                        label = { Text("Presión (PSI)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
                    )
                }

                Text(text = "Brazo Derecho (Aleatorio 31-60)", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 16.dp))
                rightNozzles.forEachIndexed { index, nozzle ->
                    Text(text = "Boquilla #${nozzle.id}", modifier = Modifier.padding(top = 8.dp))
                    OutlinedTextField(
                        value = nozzle.volumen,
                        onValueChange = { newValue -> rightNozzles = rightNozzles.toMutableList().apply { this[index] = nozzle.copy(volumen = newValue) } },
                        label = { Text("Volumen (ml)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
                    )
                    OutlinedTextField(
                        value = nozzle.presion,
                        onValueChange = { newValue -> rightNozzles = rightNozzles.toMutableList().apply { this[index] = nozzle.copy(presion = newValue) } },
                        label = { Text("Presión (PSI)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.2f))

                // --- Medición de Desplazamiento ---
                Text(text = "Medición de Desplazamiento (GPS)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.5f), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = gpsStatus, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp))

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
                                        startLat = location.latitude
                                        startLng = location.longitude
                                        startAcc = location.accuracy
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
                                        if (location != null && startLat != 0.0) {
                                            
                                            // Recreate startLocation
                                            val startLocationObj = Location("").apply {
                                                latitude = startLat
                                                longitude = startLng
                                            }

                                            val eTime = System.currentTimeMillis()
                                            
                                            calcTimeSec = (eTime - startTime) / 1000
                                            calcDistance = startLocationObj.distanceTo(location)
                                            
                                            // v = d(m) / t(s) * 3.6 = km/h
                                            if (calcTimeSec > 0) {
                                                calcSpeed = (calcDistance / calcTimeSec) * 3.6f
                                            }
                                            
                                            calcError = (startAcc + location.accuracy) / 2f
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
                    Text(text = "Tiempo: ${calcTimeSec}s", color = Color.Black)
                    Text(text = "Distancia: ${String.format(Locale.US, "%.2f", calcDistance)}m", color = Color.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Velocidad: ${String.format(Locale.US, "%.2f", calcSpeed)} km/h", color = Color.Black)
                    Text(text = "Margen Error: ±${String.format(Locale.US, "%.1f", calcError)}m", color = Color.DarkGray)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.2f))

        // --- Cuestionario Final ---
        Text(text = "¿Boquillas tapadas?", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = boquillasTapadas == true, onClick = { boquillasTapadas = true })
            Text("Sí")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = boquillasTapadas == false, onClick = { 
                boquillasTapadas = false 
                boquillasTapadasNum = "" // Reset if changing answer
            })
            Text("No")
        }
        if (boquillasTapadas == true) {
            OutlinedTextField(
                value = boquillasTapadasNum,
                onValueChange = { boquillasTapadasNum = it },
                label = { Text("¿Cuántas boquillas?") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "¿Presencia de personal en el bloque aplicado?", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = presenciaPersonal == true, onClick = { presenciaPersonal = true })
            Text("Sí")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = presenciaPersonal == false, onClick = { presenciaPersonal = false })
            Text("No")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "¿Operador mantuvo brazos a altura uniforme?", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = alturaUniforme == true, onClick = { alturaUniforme = true })
            Text("Sí")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = alturaUniforme == false, onClick = { alturaUniforme = false })
            Text("No")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Estado vía", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = estadoVia == "BUENO", onClick = { estadoVia = "BUENO" })
            Text("Bueno")
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(selected = estadoVia == "REGULAR", onClick = { estadoVia = "REGULAR" })
            Text("Regular")
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(selected = estadoVia == "MALO", onClick = { estadoVia = "MALO" })
            Text("Malo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = papelHidro, onCheckedChange = { papelHidro = it })
            Text(text = "Papel Hidrosensible", fontWeight = FontWeight.Bold)
        }

        if (papelHidro) {
            Column(modifier = Modifier.padding(start = 32.dp)) {
                Text("Ingresa la cantidad de gotas según el área seleccionada:")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top=8.dp)) {
                    OutlinedTextField(
                        value = papelGotas1cm,
                        onValueChange = { papelGotas1cm = it },
                        label = { Text("Gotas (1cm²)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
                    )
                    OutlinedTextField(
                        value = papelGotasCuarto,
                        onValueChange = { papelGotasCuarto = it },
                        label = { Text("Gotas (1/4 cm²)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) {
                        Text("Volver")
                    }
                    Button(
                        onClick = {
                            val updatedInfo = info.copy(
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
                                velocidadKmh = calcSpeed,
                                
                                boquillasTapadas = boquillasTapadas,
                                boquillasTapadasNum = boquillasTapadasNum,
                                presenciaPersonal = presenciaPersonal,
                                alturaUniforme = alturaUniforme,
                                estadoVia = estadoVia,
                                papelHidrosensible = papelHidro,
                                papelGotas1cm = papelGotas1cm,
                                papelGotasCuarto = papelGotasCuarto
                            )
                            onContinue(updatedInfo)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF5E1C8), // Beige Claro
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
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

// --- DataManager ---
object StorageManager {
    private const val PREFS_NAME = "auditoria_prefs"
    private const val KEY_AUDITS = "saved_audits"

    fun saveAuditoria(context: android.content.Context, auditoria: AuditoriaInfo) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val audits = getAuditorias(context).toMutableList()
        // If updating an existing one, remove it first
        audits.removeAll { it.id == auditoria.id }
        audits.add(auditoria)
        
        prefs.edit().putString(KEY_AUDITS, Gson().toJson(audits)).apply()
    }

    fun getAuditorias(context: android.content.Context): List<AuditoriaInfo> {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_AUDITS, null)
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<AuditoriaInfo>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun deleteAuditoria(context: android.content.Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val audits = getAuditorias(context).toMutableList()
        audits.removeAll { it.id == id }
        prefs.edit().putString(KEY_AUDITS, Gson().toJson(audits)).apply()
    }
}

// --- HistorialScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var audits by remember { mutableStateOf(StorageManager.getAuditorias(context)) }
    var filterMode by remember { mutableStateOf("TODOS") } // "TODOS", "Spray Boom", "Mezclas"

    val displayedAudits = if (filterMode == "TODOS") {
        audits
    } else {
        audits.filter { it.tipoAuditoria == filterMode }
    }

    var auditToDelete by remember { mutableStateOf<String?>(null) }

    if (auditToDelete != null) {
        AlertDialog(
            onDismissRequest = { auditToDelete = null },
            title = { Text("Eliminar Muestreo") },
            text = { Text("¿Estás seguro de que deseas eliminar este muestreo permanentemente?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        StorageManager.deleteAuditoria(context, auditToDelete!!)
                        audits = StorageManager.getAuditorias(context)
                        Toast.makeText(context, "Muestreo Eliminado", Toast.LENGTH_SHORT).show()
                        auditToDelete = null
                    }
                ) {
                    Text("Eliminar", color = androidx.compose.ui.graphics.Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { auditToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E1C8), contentColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                    }
                    Text("Historial de Muestreos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(
                        selected = filterMode == "TODOS",
                        onClick = { filterMode = "TODOS" },
                        label = { Text("Todos", color = Color.Black) }
                    )
                    FilterChip(
                        selected = filterMode == "Spray Boom",
                        onClick = { filterMode = "Spray Boom" },
                        label = { Text("Spray Boom", color = Color.Black) }
                    )
                    FilterChip(
                        selected = filterMode == "Mezclas",
                        onClick = { filterMode = "Mezclas" },
                        label = { Text("Mezclas", color = Color.Black) }
                    )
                }

                if (displayedAudits.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No hay muestreos guardados.", color = Color.DarkGray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(displayedAudits.size) { index ->
                            val audit = displayedAudits[index]
                            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(audit.fecha))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.5f), contentColor = Color.Black),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${audit.tipoAuditoria} - Finca: ${audit.finca}", fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text(text = "Fecha: $dateStr - Lote: ${audit.lote}", fontSize = 14.sp, color = Color.DarkGray)
                                        Text(text = "Evaluador: ${audit.evaluador}", fontSize = 14.sp, color = Color.DarkGray)
                                    }
                                    IconButton(onClick = { auditToDelete = audit.id }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = androidx.compose.ui.graphics.Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Excel Manager ---
object ExportManager {
    fun exportToExcel(context: android.content.Context) {
        val audits = StorageManager.getAuditorias(context)
        if (audits.isEmpty()) {
            Toast.makeText(context, "No hay datos para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
            
            // Sheet 1: Spray Boom
            val sbSheet = workbook.createSheet("Spray Boom")
            val mezclasSheet = workbook.createSheet("Mezclas")

            val headers = arrayOf(
                "ID", "Fecha", "Hora", "Evaluador", "Finca", "Lote", "Operador", 
                "CodTractor", "CodImplemento", "PotTractor", "PotTDF", "Formula", 
                "Presion", "Volumen", "Velocidad GPS", "Distancia GPS",
                "BoquillasTapadas", "NumTapadas", "PresenciaPersonal", "AlturaUniforme", 
                "EstadoVia", "PapelHidro", "Gotas1cm2", "Gotas1/4cm2"
            )

            // Setup Spray Boom Sheet
            var row0 = sbSheet.createRow(0)
            headers.forEachIndexed { i, h -> row0.createCell(i).setCellValue(h) }
            
            // Setup Mezclas Sheet
            var mRow0 = mezclasSheet.createRow(0)
            headers.forEachIndexed { i, h -> mRow0.createCell(i).setCellValue(h) }

            var sbRowIdx = 1
            var mRowIdx = 1

            for (audit in audits) {
                val sheet = if (audit.tipoAuditoria == "Mezclas") mezclasSheet else sbSheet
                val rowIdx = if (audit.tipoAuditoria == "Mezclas") mRowIdx++ else sbRowIdx++
                val row = sheet.createRow(rowIdx)
                
                val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(audit.fecha))
                
                row.createCell(0).setCellValue(audit.id)
                row.createCell(1).setCellValue(dateStr)
                row.createCell(2).setCellValue(audit.hora)
                row.createCell(3).setCellValue(audit.evaluador)
                row.createCell(4).setCellValue(audit.finca)
                row.createCell(5).setCellValue(audit.lote)
                row.createCell(6).setCellValue(audit.operador)
                row.createCell(7).setCellValue(audit.codTractor)
                row.createCell(8).setCellValue(audit.codImplemento)
                row.createCell(9).setCellValue(audit.potenciaTractor)
                row.createCell(10).setCellValue(audit.potenciaTdf)
                row.createCell(11).setCellValue(audit.formula)
                row.createCell(12).setCellValue(audit.presion)
                row.createCell(13).setCellValue(audit.volumen)
                row.createCell(14).setCellValue(audit.velocidadKmh.toString())
                row.createCell(15).setCellValue(audit.distanciaMetros.toString())
                
                row.createCell(16).setCellValue(if (audit.boquillasTapadas == true) "SI" else if (audit.boquillasTapadas == false) "NO" else "")
                row.createCell(17).setCellValue(audit.boquillasTapadasNum)
                row.createCell(18).setCellValue(if (audit.presenciaPersonal == true) "SI" else if (audit.presenciaPersonal == false) "NO" else "")
                row.createCell(19).setCellValue(if (audit.alturaUniforme == true) "SI" else if (audit.alturaUniforme == false) "NO" else "")
                row.createCell(20).setCellValue(audit.estadoVia)
                row.createCell(21).setCellValue(if (audit.papelHidrosensible) "SI" else "NO")
                row.createCell(22).setCellValue(audit.papelGotas1cm)
                row.createCell(23).setCellValue(audit.papelGotasCuarto)
            }

            // Save to Downloads directory
            val fileName = "Auditorias_${System.currentTimeMillis()}.xlsx"
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )
                Uri.fromFile(file)
            }

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
                Toast.makeText(context, "Excel guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error: No se pudo crear el archivo en Descargas.", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exportando Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FormularioMezclasScreen(
    info: AuditoriaInfo,
    onBack: () -> Unit,
    onContinue: (AuditoriaInfo) -> Unit
) {
    var mezclador by rememberSaveable { mutableStateOf(info.mezclador) }
    var formula by rememberSaveable { mutableStateOf(info.formulaMezclar) }

    val context = LocalContext.current
    val insumosList = remember { parseCsv(context) }
    val codigosUnicos = remember(insumosList) { insumosList.map { it.codigo }.distinct() }
    var expandedFormula by rememberSaveable { mutableStateOf(false) }

    val selectedInsumos = remember(formula, insumosList) {
        insumosList.filter { it.codigo == formula }.sortedBy { it.numero }
    }
    
    val dateFormatter = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E1C8), contentColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (General Info)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f), contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Información General", fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Eval: ${info.evaluador}", fontSize = 14.sp)
                            Text("Finca: ${info.finca}", fontSize = 14.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fecha: ${dateFormatter.format(java.util.Date(info.fecha))}", fontSize = 14.sp)
                            Text("Hora: ${info.hora}", fontSize = 14.sp)
                        }
                        Text("Lote: ${info.lote}", fontSize = 14.sp)
                    }
                }

                // Inputs
                OutlinedTextField(
                    value = mezclador,
                    onValueChange = { mezclador = it },
                    label = { Text("Nombre Mezclador") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedFormula,
                    onExpandedChange = { expandedFormula = !expandedFormula },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = formula,
                        onValueChange = { 
                            formula = it
                            expandedFormula = true
                        },
                        label = { Text("Fórmula a mezclar") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )
                    
                    val filteredCodigos = codigosUnicos.filter { it.contains(formula, ignoreCase = true) }.take(10)
                    if (filteredCodigos.isNotEmpty() && expandedFormula) {
                        DropdownMenu(
                            expanded = expandedFormula,
                            onDismissRequest = { expandedFormula = false },
                            modifier = Modifier.exposedDropdownSize()
                        ) {
                            filteredCodigos.forEach { cod ->
                                DropdownMenuItem(
                                    text = { Text(cod, color = Color.Black) },
                                    onClick = {
                                        formula = cod
                                        expandedFormula = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Table of components
                if (selectedInsumos.isNotEmpty()) {
                    Text(text = "Componentes de la Fórmula:", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f), contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Text("Insumo", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                                Text("Cantidad", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Unidad", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.5f))
                            
                            selectedInsumos.forEach { insumo ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Text(insumo.insumo, modifier = Modifier.weight(2f), fontSize = 12.sp)
                                    Text(insumo.cantidad, modifier = Modifier.weight(1f), fontSize = 12.sp)
                                    Text(insumo.unidad, modifier = Modifier.weight(1f), fontSize = 12.sp)
                                }
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.2f))
                            }
                        }
                    }
                }

                // Footers: Volver y Guardar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) {
                        Text("Volver")
                    }
                    Button(
                        onClick = {
                            val updatedInfo = info.copy(
                                mezclador = mezclador,
                                formulaMezclar = formula
                            )
                            onContinue(updatedInfo)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

