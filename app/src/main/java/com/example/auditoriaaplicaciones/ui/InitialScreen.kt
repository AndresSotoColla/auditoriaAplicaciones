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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.auditoriaaplicaciones.R
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
data class ProductoEvaluado(
    val producto: String, 
    var cumple: Boolean = true, 
    var reemplazo: String = "",
    var cantidad: String = "",
    var unidad: String = "",
    var orden: String = ""
) : Serializable

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
    var productosEvaluados: List<ProductoEvaluado> = emptyList(),
    var incompatibilidad: Boolean = false,
    var ordenMezclado: Boolean = true,
    var obsOrdenMezclado: String = "",
    var usaEpp: Boolean = true,
    var obsEpp: String = "",
    var tanqueLimpio: Boolean = true,
    var obsTanqueLimpio: String = "",
    
    var phAgua: String = "",
    var durezaAgua: String = "",
    var ceAgua: String = "",
    var phFinal: String = "",
    var ceFinal: String = "",
    
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
                    BackHandler { currentScreen = "Menu" }
                    HistorialScreen(
                        onBack = { currentScreen = "Menu" }
                    )
                }
                "SprayBoom" -> {
                    BackHandler { currentScreen = "Menu" }
                    SprayBoomChecklist(
                        onBack = { currentScreen = "Menu" },
                        onContinue = { currentScreen = "DatosGenerales" }
                    )
                }
                "DatosGenerales" -> {
                    BackHandler {
                        if (auditoriaInfo.tipoAuditoria == "Mezclas") currentScreen = "Menu"
                        else currentScreen = "SprayBoom"
                    }
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
                    BackHandler { currentScreen = "DatosGenerales" }
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
                    BackHandler { currentScreen = "DatosGenerales" }
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

    val beigeClaro = Color(0xFFF9EBD7) // 🔥 más claro que el anterior

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔽 BAJAR UN POCO EL TÍTULO
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Auditoria Aplicaciones",
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

        Spacer(modifier = Modifier.weight(1f))

        // 🎯 BOTONES
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            MenuButton(
                text = "Auditoría Aplicaciones",
                icon = Icons.Default.Checklist,
                onClick = onAuditoriaClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuButton(
                text = "Ver Historial",
                icon = Icons.Default.History,
                onClick = onHistorialClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuButton(
                text = "Descargar Excel",
                icon = Icons.Default.Download,
                onClick = onDescargarExcelClick
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 📍 FOOTER
        Text(
            text = "CT&A 2026",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SelectionDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {

    val beigeOscuro = Color(0xFFEAD7BC) // 🔥 fondo
    val beigeClaro = Color(0xFFF5E1C8)  // 🔥 botones

    AlertDialog(
        onDismissRequest = onDismiss,

        // 🎨 FONDO DEL DIALOG
        containerColor = beigeOscuro,

        title = {
            Text(
                text = "Seleccione la Auditoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black // 🔥 título negro
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = { onOptionSelected("Mezclas") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = beigeClaro,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "AUDITORÍA MEZCLAS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { onOptionSelected("Spray Boom") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = beigeClaro,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "AUDITORÍA SPRAY BOOM",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },

        confirmButton = {},

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    color = Color.Black
                )
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
    var phAgua by rememberSaveable { mutableStateOf(infoInicial.phAgua) }
    var durezaAgua by rememberSaveable { mutableStateOf(infoInicial.durezaAgua) }
    var ceAgua by rememberSaveable { mutableStateOf(infoInicial.ceAgua) }
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
                if (it.isEmpty()) {
                    lote = it
                    finca = ""
                } else if (it.all { char -> char.isDigit() }) {
                    val loteInt = it.toIntOrNull() ?: 0
                    if (loteInt <= 87) {
                        lote = it 
                        finca = when (loteInt) {
                            in 1..20 -> "LA FE"
                            in 21..27 -> "SULTANA"
                            in 28..39 -> "JAMAICA"
                            in 40..55 -> "EGIPTO"
                            in 56..65 -> "AMÉRICAS"
                            in 66..76 -> "BRASIL"
                            in 77..87 -> "ARGENTINA"
                            else -> ""
                        }
                    }
                }
            },
            label = { Text("Lote (01 - 87)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
        )

        OutlinedTextField(
            value = finca,
            onValueChange = { finca = it },
            label = { Text("Finca") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
        )

        if (infoInicial.tipoAuditoria == "Mezclas") {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.1f))
            Text("Calidad del Agua", fontWeight = FontWeight.Bold, color = Color.Black)
            
            OutlinedTextField(
                value = phAgua,
                onValueChange = { phAgua = it },
                label = { Text("pH inicial del agua") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = durezaAgua,
                onValueChange = { durezaAgua = it },
                label = { Text("Dureza del agua") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = ceAgua,
                onValueChange = { ceAgua = it },
                label = { Text("CE agua (mS/cm)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

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
                                    lote = lote,
                                    phAgua = phAgua,
                                    durezaAgua = durezaAgua,
                                    ceAgua = ceAgua
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
    var manualFormulaName by rememberSaveable { mutableStateOf("") }
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

    val tractorList = remember { List(41) { "TA-${(it + 1).toString().padStart(2, '0')}" } }
    val implementList = remember { List(81) { "IA-${(it + 1).toString().padStart(2, '0')}" } }
    var expandedTractor by rememberSaveable { mutableStateOf(false) }
    var expandedImplement by rememberSaveable { mutableStateOf(false) }

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

    // --- Modo Manual ---
    var isManualMode by rememberSaveable { mutableStateOf(false) }
    var manualDistance by rememberSaveable { mutableStateOf(if (info.distanciaMetros > 0f) info.distanciaMetros.toString() else "") }


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
                Text(
                    text = "Aplicación con Spray Boom",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

// --- Header Section ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.5f),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Resumen de Auditoría",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Evaluador: ${info.evaluador}",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Finca: ${info.finca}",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Fecha: ${dateFormatter.format(Date(info.fecha))}",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Hora: ${info.hora}",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Lote: ${info.lote}",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text(text = "Detalles de Auditoría", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)

                // --- Form Fields ---
                OutlinedTextField(value = operador, onValueChange = { operador = it }, label = { Text("Nombre operador") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())
                
                ExposedDropdownMenuBox(
                    expanded = expandedTractor,
                    onExpandedChange = { expandedTractor = !expandedTractor },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = codTractor,
                        onValueChange = { 
                            codTractor = it
                            expandedTractor = true
                        },
                        label = { Text("Cód. Tractor") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )
                    val filteredTractors = tractorList.filter { it.contains(codTractor, ignoreCase = true) }
                    if (filteredTractors.isNotEmpty() && expandedTractor) {
                        DropdownMenu(
                            expanded = expandedTractor,
                            onDismissRequest = { expandedTractor = false },
                            modifier = Modifier.exposedDropdownSize().background(Color(0xFFEAD7BC))
                        ) {
                            filteredTractors.forEach { cod ->
                                DropdownMenuItem(
                                    text = { Text(cod, color = Color.Black) },
                                    onClick = {
                                        codTractor = cod
                                        expandedTractor = false
                                    }
                                )
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedImplement,
                    onExpandedChange = { expandedImplement = !expandedImplement },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = codImplemento,
                        onValueChange = { 
                            codImplemento = it
                            expandedImplement = true
                        },
                        label = { Text("Cód. Implemento") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )
                    val filteredImplements = implementList.filter { it.contains(codImplemento, ignoreCase = true) }
                    if (filteredImplements.isNotEmpty() && expandedImplement) {
                        DropdownMenu(
                            expanded = expandedImplement,
                            onDismissRequest = { expandedImplement = false },
                            modifier = Modifier.exposedDropdownSize().background(Color(0xFFEAD7BC))
                        ) {
                            filteredImplements.forEach { cod ->
                                DropdownMenuItem(
                                    text = { Text(cod, color = Color.Black) },
                                    onClick = {
                                        codImplemento = cod
                                        expandedImplement = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(value = potenciaTractor, onValueChange = { potenciaTractor = it }, label = { Text("Potencia Tractor (HP)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())
                OutlinedTextField(value = potenciaTdf, onValueChange = { potenciaTdf = it }, label = { Text("Potencia TDF/PPO (HP)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors())

                val focusRequester = remember { FocusRequester() }
                var isFocused by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedFormula,
                    onExpandedChange = {
                        expandedFormula = !expandedFormula
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedTextField(
                        value = formula,
                        onValueChange = { newValue ->
                            formula = newValue
                            // ❌ NO abrir dropdown aquí (evita que se cierre el teclado)
                        },
                        label = { Text("Fórmula a aplicar") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                isFocused = it.isFocused
                                expandedFormula = it.isFocused // ✅ solo abre con foco
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors(),
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFormula)
                        }
                    )

                    val filteredCodigos = codigosUnicos
                        .filter { it.contains(formula, ignoreCase = true) }
                        .sorted()
                        .toMutableList()

                    if (!filteredCodigos.contains("OTRO")) filteredCodigos.add("OTRO")

                    if (expandedFormula && filteredCodigos.isNotEmpty()) {

                        DropdownMenu(
                            expanded = expandedFormula,
                            onDismissRequest = { expandedFormula = false },
                            modifier = Modifier
                                .exposedDropdownSize()
                                .heightIn(max = 250.dp) // ✅ evita crecimiento infinito
                                .background(Color(0xFFEAD7BC))
                        ) {

                            filteredCodigos.forEach { cod ->

                                DropdownMenuItem(
                                    text = { Text(cod, color = Color.Black) },
                                    onClick = {
                                        formula = cod
                                        expandedFormula = false

                                        if (cod != "OTRO") {
                                            val match = insumosList.firstOrNull { it.codigo == cod }
                                            selectedDescripcion = match?.descripcion ?: ""

                                            val aguaMatch = insumosList.firstOrNull {
                                                it.codigo == cod && it.insumo.equals("AGUA", ignoreCase = true)
                                            }

                                            if (aguaMatch != null) {
                                                volumen = aguaMatch.cantidad
                                            }

                                            manualFormulaName = ""
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

// 🔽 CAMPOS ADICIONALES
                if (formula == "OTRO") {

                    OutlinedTextField(
                        value = manualFormulaName,
                        onValueChange = { manualFormulaName = it },
                        label = { Text("Nombre de la Fórmula (Manual)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )

                    OutlinedTextField(
                        value = selectedDescripcion,
                        onValueChange = { selectedDescripcion = it },
                        label = { Text("Descripción de la Fórmula") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )

                } else if (selectedDescripcion.isNotEmpty()) {

                    Text(
                        text = "Descripción: $selectedDescripcion",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
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

                // Nozzle Reference Image
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.referencia_boquillas),
                            contentDescription = "Referencia boquillas",
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Referencia frontal boquillas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp),
                            color = Color.Black
                        )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isManualMode) "Medición Manual" else "Medición GPS",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    
                    TextButton(onClick = { isManualMode = !isManualMode }) {
                        Text(if (isManualMode) "CAMBIAR A GPS" else "CAMBIAR A MANUAL")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.5f), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        
                        if (!isManualMode) {
                            Text(text = gpsStatus, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp))
                        } else {
                            Text(text = "Modo Manual: Use el cronómetro y digite la distancia.", fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                            FilledTonalButton(
                                modifier = Modifier.weight(1f),
                                enabled = !isMeasuring,
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)),
                                onClick = {
                                    if (!isManualMode) {
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
                                    } else {
                                        // Manual Mode Timer
                                        startTime = System.currentTimeMillis()
                                        isMeasuring = true
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
                                    if (!isManualMode) {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            gpsStatus = "Obteniendo ubicación final..."
                                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                                .addOnSuccessListener { location: Location? ->
                                                    if (location != null && startLat != 0.0) {
                                                        val startLocationObj = Location("").apply {
                                                            latitude = startLat
                                                            longitude = startLng
                                                        }
                                                        val eTime = System.currentTimeMillis()
                                                        calcTimeSec = (eTime - startTime) / 1000
                                                        calcDistance = startLocationObj.distanceTo(location)
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
                                    } else {
                                        // Manual Mode stop
                                        val eTime = System.currentTimeMillis()
                                        calcTimeSec = (eTime - startTime) / 1000
                                        isMeasuring = false
                                        
                                        val dist = manualDistance.toFloatOrNull() ?: 0f
                                        if (dist > 0 && calcTimeSec > 0) {
                                            calcSpeed = (dist / calcTimeSec) * 3.6f
                                            calcDistance = dist
                                        }
                                    }
                                }
                            ) {
                                Text("Detener", color = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                        
                        if (isManualMode) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = manualDistance,
                                onValueChange = { 
                                    manualDistance = it
                                    val dist = it.toFloatOrNull() ?: 0f
                                    if (dist > 0 && calcTimeSec > 0) {
                                        calcSpeed = (dist / calcTimeSec) * 3.6f
                                        calcDistance = dist
                                    }
                                },
                                label = { Text("Distancia Manual (metros)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = blackTextFieldColors(),
                                shape = RoundedCornerShape(12.dp)
                            )
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
                                formula = if (formula == "OTRO") manualFormulaName else formula,
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
            .height(56.dp), // 🔥 altura ideal (tipo Material)
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF5E1C8),
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp) // más sutil
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp) // 🔥 más proporcional
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
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
                "EstadoVia", "PapelHidro", "Gotas1cm2", "Gotas1/4cm2",
                "B1_ID", "B1_Pres", "B1_Vol", "B2_ID", "B2_Pres", "B2_Vol",
                "B3_ID", "B3_Pres", "B3_Vol", "B4_ID", "B4_Pres", "B4_Vol"
            )
            
            val mezclasHeaders = arrayOf(
                "ID", "Fecha", "Hora", "Evaluador", "Finca", "Lote", 
                "pH Inicial", "Dureza Agua", "CE Agua mS/cm",
                "pH Final", "CE Final mS/cm",
                "Mezclador", "Formula",
                "Productos Evaluados (JSON)", "Incompatibilidad", "Respeta Orden", "Obs Orden",
                "Usa EPP", "Obs EPP", "Tanque Limpio", "Obs Tanque"
            )

            // Setup Spray Boom Sheet
            var row0 = sbSheet.createRow(0)
            headers.forEachIndexed { i, h -> row0.createCell(i).setCellValue(h) }
            
            // Setup Mezclas Sheet
            var mRow0 = mezclasSheet.createRow(0)
            mezclasHeaders.forEachIndexed { i, h -> mRow0.createCell(i).setCellValue(h) }

            var sbRowIdx = 1
            var mRowIdx = 1

            for (audit in audits) {
                val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(audit.fecha))
                
                if (audit.tipoAuditoria == "Mezclas") {
                    val row = mezclasSheet.createRow(mRowIdx++)
                    row.createCell(0).setCellValue(audit.id)
                    row.createCell(1).setCellValue(dateStr)
                    row.createCell(2).setCellValue(audit.hora)
                    row.createCell(3).setCellValue(audit.evaluador)
                    row.createCell(4).setCellValue(audit.finca)
                    row.createCell(5).setCellValue(audit.lote)
                    row.createCell(6).setCellValue(audit.phAgua)
                    row.createCell(7).setCellValue(audit.durezaAgua)
                    row.createCell(8).setCellValue(audit.ceAgua)
                    row.createCell(9).setCellValue(audit.phFinal)
                    row.createCell(10).setCellValue(audit.ceFinal)
                    row.createCell(11).setCellValue(audit.mezclador)
                    row.createCell(12).setCellValue(audit.formulaMezclar)
                    
                    val jsonProductos = com.google.gson.Gson().toJson(audit.productosEvaluados)
                    row.createCell(13).setCellValue(jsonProductos)
                    row.createCell(14).setCellValue(if (audit.incompatibilidad) "SI" else "NO")
                    row.createCell(15).setCellValue(if (audit.ordenMezclado) "SI" else "NO")
                    row.createCell(16).setCellValue(audit.obsOrdenMezclado)
                    row.createCell(17).setCellValue(if (audit.usaEpp) "SI" else "NO")
                    row.createCell(18).setCellValue(audit.obsEpp)
                    row.createCell(19).setCellValue(if (audit.tanqueLimpio) "SI" else "NO")
                    row.createCell(20).setCellValue(audit.obsTanqueLimpio)
                } else {
                    val row = sbSheet.createRow(sbRowIdx++)
                
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
                
                // --- Nozzles Serialized ---
                val allNozzles = audit.nozzlesIzquierdo + audit.nozzlesDerecho
                for (i in 0 until 4) {
                    if (i < allNozzles.size) {
                        val n = allNozzles[i]
                        row.createCell(24 + (i * 3)).setCellValue(n.id.toDouble())
                        row.createCell(25 + (i * 3)).setCellValue(n.presion)
                        row.createCell(26 + (i * 3)).setCellValue(n.volumen)
                    }
                }
                }
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
    var manualFormulaName by rememberSaveable { mutableStateOf(if (info.formulaMezclar == "OTRO") "" else "") }

    val context = LocalContext.current
    val insumosList = remember { parseCsv(context) }
    val codigosUnicos = remember(insumosList) { insumosList.map { it.codigo }.distinct() }
    var expandedFormula by rememberSaveable { mutableStateOf(false) }

    val selectedInsumos = remember(formula, insumosList) {
        insumosList.filter { it.codigo == formula }.sortedBy { it.numero }
    }
    
    var productosEvaluados by remember { mutableStateOf(info.productosEvaluados) }
    var incompatibilidad by rememberSaveable { mutableStateOf(info.incompatibilidad) }
    var ordenMezclado by rememberSaveable { mutableStateOf(info.ordenMezclado) }
    var obsOrdenMezclado by rememberSaveable { mutableStateOf(info.obsOrdenMezclado) }
    var usaEpp by rememberSaveable { mutableStateOf(info.usaEpp) }
    var obsEpp by rememberSaveable { mutableStateOf(info.obsEpp) }
    var tanqueLimpio by rememberSaveable { mutableStateOf(info.tanqueLimpio) }
    var obsTanqueLimpio by rememberSaveable { mutableStateOf(info.obsTanqueLimpio) }
    
    var phFinal by rememberSaveable { mutableStateOf(info.phFinal) }
    var ceFinal by rememberSaveable { mutableStateOf(info.ceFinal) }

    LaunchedEffect(selectedInsumos) {
        if (productosEvaluados.isEmpty() || selectedInsumos.map { it.insumo } != productosEvaluados.map { it.producto }) {
            productosEvaluados = selectedInsumos.map { ProductoEvaluado(producto = it.insumo) }
        }
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
                Text(
                    text = "Preparación de Mezclas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

// Header (General Info)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.5f),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            "Información General",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Evaluador: ${info.evaluador}", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Text("Finca: ${info.finca}", fontSize = 14.sp, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Fecha: ${dateFormatter.format(java.util.Date(info.fecha))}", fontSize = 14.sp, textAlign = TextAlign.Center)
                        Text("Hora: ${info.hora}", fontSize = 14.sp, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Lote: ${info.lote}", fontSize = 14.sp, textAlign = TextAlign.Center)

                        if (info.phAgua.isNotEmpty() || info.durezaAgua.isNotEmpty() || info.ceAgua.isNotEmpty()) {
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color.Black.copy(alpha = 0.1f)
                            )

                            if (info.phAgua.isNotEmpty())
                                Text("pH Inicial: ${info.phAgua}", fontSize = 14.sp, textAlign = TextAlign.Center)

                            if (info.durezaAgua.isNotEmpty())
                                Text("Dureza Agua: ${info.durezaAgua}", fontSize = 14.sp, textAlign = TextAlign.Center)

                            if (info.ceAgua.isNotEmpty())
                                Text("CE Agua: ${info.ceAgua} mS/cm", fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
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

                val focusRequester = remember { FocusRequester() }
                var isFocused by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedFormula,
                    onExpandedChange = {
                        expandedFormula = !expandedFormula
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedTextField(
                        value = formula,
                        onValueChange = { newValue ->
                            formula = newValue
                            // ❌ NO tocar expanded aquí
                        },
                        label = { Text("Fórmula a mezclar") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                isFocused = it.isFocused
                                expandedFormula = it.isFocused // 🔥 abre solo con foco
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors(),
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFormula)
                        }
                    )

                    val filteredCodigos = codigosUnicos
                        .filter { it.contains(formula, ignoreCase = true) }
                        .sorted()
                        .toMutableList()

                    if (!filteredCodigos.contains("OTRO")) filteredCodigos.add("OTRO")

                    if (expandedFormula && filteredCodigos.isNotEmpty()) {

                        DropdownMenu(
                            expanded = expandedFormula,
                            onDismissRequest = { expandedFormula = false },
                            modifier = Modifier
                                .exposedDropdownSize()
                                .heightIn(max = 250.dp)
                                .background(Color(0xFFF5E1C8))
                        ) {

                            filteredCodigos.forEach { cod ->

                                DropdownMenuItem(
                                    text = { Text(cod, color = Color.Black) },
                                    onClick = {
                                        formula = cod
                                        expandedFormula = false

                                        if (cod != "OTRO") {
                                            productosEvaluados = insumosList
                                                .filter { it.codigo == cod }
                                                .sortedBy { it.numero }
                                                .map {
                                                    ProductoEvaluado(
                                                        producto = it.insumo,
                                                        cantidad = it.cantidad,
                                                        unidad = it.unidad,
                                                        orden = it.numero.toString()
                                                    )
                                                }
                                        } else {
                                            productosEvaluados = emptyList()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // New fields for final parameters
                OutlinedTextField(
                    value = phFinal,
                    onValueChange = { phFinal = it },
                    label = { Text("pH final") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors()
                )
                OutlinedTextField(
                    value = ceFinal,
                    onValueChange = { ceFinal = it },
                    label = { Text("CE final (mS/cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors()
                )
                // Table of components
                if (formula == "OTRO" || selectedInsumos.isNotEmpty()) {
                    Text(text = "Validación de Productos:", fontWeight = FontWeight.Bold, color = Color.Black)
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f), contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (formula != "OTRO") {
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                    Text("Insumo", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                                    Text("Vol.", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Aplicó?", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                }
                                androidx.compose.material3.HorizontalDivider(color = Color.Black.copy(alpha = 0.5f))
                            }
                            
                            productosEvaluados.forEachIndexed { index, pe ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    if (formula == "OTRO") {
                                        OutlinedTextField(
                                            value = pe.producto,
                                            onValueChange = { 
                                                val m = productosEvaluados.toMutableList()
                                                m[index] = pe.copy(producto = it)
                                                productosEvaluados = m
                                            },
                                            label = { Text("Nombre Producto") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = blackTextFieldColors()
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = pe.orden,
                                                onValueChange = { 
                                                    val m = productosEvaluados.toMutableList()
                                                    m[index] = pe.copy(orden = it)
                                                    productosEvaluados = m
                                                },
                                                label = { Text("Orden") },
                                                modifier = Modifier.weight(1f),
                                                colors = blackTextFieldColors()
                                            )
                                            OutlinedTextField(
                                                value = pe.cantidad,
                                                onValueChange = { 
                                                    val m = productosEvaluados.toMutableList()
                                                    m[index] = pe.copy(cantidad = it)
                                                    productosEvaluados = m
                                                },
                                                label = { Text("Volumen") },
                                                modifier = Modifier.weight(1.5f),
                                                colors = blackTextFieldColors()
                                            )
                                            OutlinedTextField(
                                                value = pe.unidad,
                                                onValueChange = { 
                                                    val m = productosEvaluados.toMutableList()
                                                    m[index] = pe.copy(unidad = it)
                                                    productosEvaluados = m
                                                },
                                                label = { Text("Unidad") },
                                                modifier = Modifier.weight(1f),
                                                colors = blackTextFieldColors()
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "${pe.producto} (${pe.cantidad} ${pe.unidad})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text("¿Aplicó?", fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                        RadioButton(selected = pe.cumple, onClick = {
                                            val m = productosEvaluados.toMutableList()
                                            m[index] = pe.copy(cumple = true)
                                            productosEvaluados = m
                                        }, modifier = Modifier.size(24.dp))
                                        Text("Sí", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        RadioButton(selected = !pe.cumple, onClick = {
                                            val m = productosEvaluados.toMutableList()
                                            m[index] = pe.copy(cumple = false)
                                            productosEvaluados = m
                                        }, modifier = Modifier.size(24.dp))
                                        Text("No", fontSize = 12.sp)
                                        
                                        if (formula == "OTRO") {
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(onClick = {
                                                val m = productosEvaluados.toMutableList()
                                                m.removeAt(index)
                                                productosEvaluados = m
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                            }
                                        }
                                    }
                                    
                                    if (!pe.cumple) {
                                        OutlinedTextField(
                                            value = pe.reemplazo,
                                            onValueChange = { 
                                                val m = productosEvaluados.toMutableList()
                                                m[index] = pe.copy(reemplazo = it)
                                                productosEvaluados = m
                                            },
                                            label = { Text("Reemplazo") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            colors = blackTextFieldColors(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    androidx.compose.material3.HorizontalDivider(color = Color.Black.copy(alpha = 0.2f))
                                }
                            }
                            
                            if (formula == "OTRO") {
                                Button(
                                    onClick = { 
                                        productosEvaluados = productosEvaluados + ProductoEvaluado(producto = "")
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Agregar Producto")
                                }
                            }
                        }
                    }
                } // End if Empty Formula

                Spacer(modifier = Modifier.height(8.dp))

                // Incompatibilidad
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.5f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("¿Se genera incompatibilidad?", fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = incompatibilidad, onClick = { incompatibilidad = true })
                            Text("Sí", color = Color.Black)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = !incompatibilidad, onClick = { incompatibilidad = false })
                            Text("No", color = Color.Black)
                        }
                    }
                }

                // Orden de Mezclado
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.5f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("¿Se respeta el orden de mezclado?", fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = ordenMezclado, onClick = { ordenMezclado = true })
                            Text("Sí", color = Color.Black)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = !ordenMezclado, onClick = { ordenMezclado = false })
                            Text("No", color = Color.Black)
                        }
                        if (!ordenMezclado) {
                            OutlinedTextField(
                                value = obsOrdenMezclado,
                                onValueChange = { obsOrdenMezclado = it },
                                label = { Text("Observación orden de mezclado") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = blackTextFieldColors()
                            )
                        }
                    }
                }

                // Uso de EPP
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.5f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("¿Operario usa EPP?", fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = usaEpp, onClick = { usaEpp = true })
                            Text("Sí", color = Color.Black)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = !usaEpp, onClick = { usaEpp = false })
                            Text("No", color = Color.Black)
                        }
                        if (!usaEpp) {
                            OutlinedTextField(
                                value = obsEpp,
                                onValueChange = { obsEpp = it },
                                label = { Text("Observación uso EPP") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = blackTextFieldColors()
                            )
                        }
                    }
                }

                // Tanque limpio
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.5f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("¿Tanque limpio?", fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = tanqueLimpio, onClick = { tanqueLimpio = true })
                            Text("Sí", color = Color.Black)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = !tanqueLimpio, onClick = { tanqueLimpio = false })
                            Text("No", color = Color.Black)
                        }
                        if (!tanqueLimpio) {
                            OutlinedTextField(
                                value = obsTanqueLimpio,
                                onValueChange = { obsTanqueLimpio = it },
                                label = { Text("Observación tanque limpio") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = blackTextFieldColors()
                            )
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
                                formulaMezclar = if (formula == "OTRO") manualFormulaName else formula,
                                productosEvaluados = productosEvaluados,
                                incompatibilidad = incompatibilidad,
                                ordenMezclado = ordenMezclado,
                                obsOrdenMezclado = obsOrdenMezclado,
                                usaEpp = usaEpp,
                                obsEpp = obsEpp,
                                tanqueLimpio = tanqueLimpio,
                                obsTanqueLimpio = obsTanqueLimpio,
                                phFinal = phFinal,
                                ceFinal = ceFinal
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

