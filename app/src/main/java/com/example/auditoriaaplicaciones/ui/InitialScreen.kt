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
import androidx.compose.foundation.horizontalScroll
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
import com.example.auditoriaaplicaciones.network.SyncManager
import kotlinx.coroutines.launch

import java.io.Serializable

// Para soportar guardado de estado ante rotaciones, los usamos como Serializable
data class NozzleData(val id: Int, var volumen: String = "", var presion: String = "", var tiempoSegundos: Int = 0) : Serializable
data class ProductoEvaluado(
    val producto: String, 
    var cumple: Boolean = true, 
    var reemplazo: String = "",
    var cantidad: String = "",
    var unidad: String = "",
    var orden: String = ""
) : Serializable

data class CalculoRecorrido(
    val id: String = UUID.randomUUID().toString(),
    var distancia: Double = 0.0,
    var tiempo: Double = 0.0,
    var volumenAplicado: Double = 0.0
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
    
    // Boquillas y Brazos
    var longitudBrazoIzquierdo: String = "",
    var longitudBrazoDerecho: String = "",
    var cantidadBoquillasIzquierdas: String = "",
    var cantidadBoquillasDerechas: String = "",
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
    var tamanoGotas: String = "",
    var ubicacion: String = "",
    var observaciones: String = "",
    var velocidadOptima: Float = 0f,
    var isSynced: Boolean = false,

    // Calibración Spray Boom
    var operarioCalib: String = "",
    var tractorCalib: String = "",
    var volumenTanque: Double = 0.0,
    var implementoCalib: String = "",
    var numBoquillas: Int = 0,
    var tipoBoquillas: String = "",
    var referenciaBoquillas: String = "",
    var tiempoCalib: Double = 0.0,
    var descargaCalib: Double = 0.0,
    var calculosRecorrido: List<CalculoRecorrido> = emptyList()
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
                                } else if (option == "Calibracion Spray Boom") {
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
                        else if (auditoriaInfo.tipoAuditoria == "Calibracion Spray Boom") currentScreen = "Menu"
                        else currentScreen = "SprayBoom"
                    }
                    DatosGeneralesScreen(
                        infoInicial = auditoriaInfo,
                        onBack = { 
                            if (auditoriaInfo.tipoAuditoria == "Mezclas") currentScreen = "Menu"
                            else if (auditoriaInfo.tipoAuditoria == "Calibracion Spray Boom") currentScreen = "Menu"
                            else currentScreen = "SprayBoom"
                        },
                        onContinue = { info ->
                            Toast.makeText(context, "Navegando a Formulario...", Toast.LENGTH_SHORT).show()
                            auditoriaInfo = info
                            if (info.tipoAuditoria == "Mezclas") {
                                currentScreen = "FormularioMezclas"
                            } else if (info.tipoAuditoria == "Calibracion Spray Boom") {
                                currentScreen = "FormularioCalibracion"
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
                "FormularioCalibracion" -> {
                    BackHandler { currentScreen = "DatosGenerales" }
                    FormularioCalibracionScreen(
                        info = auditoriaInfo,
                        onBack = { currentScreen = "DatosGenerales" },
                        onContinue = { finalInfo ->
                            auditoriaInfo = finalInfo
                            StorageManager.saveAuditoria(context, finalInfo)
                            Toast.makeText(context, "¡Calibración Guardada!", Toast.LENGTH_LONG).show()
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

                Button(
                    onClick = { onOptionSelected("Calibracion Spray Boom") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = beigeClaro,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "CALIBRACIÓN SPRAY BOOM",
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

        if (infoInicial.tipoAuditoria != "Calibracion Spray Boom") {
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
        }

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
                            val isCalibracion = infoInicial.tipoAuditoria == "Calibracion Spray Boom"
                            val loteNum = if (isCalibracion) 0 else (lote.toIntOrNull() ?: 0)
                            
                            if (evaluador.isBlank() || (!isCalibracion && (finca.isBlank() || lote.isBlank()))) {
                                debugErrorMsg = if (isCalibracion) {
                                    "ERROR: Faltan campos básicos.\nEvaluador='${evaluador}'"
                                } else {
                                    "ERROR: Faltan campos básicos.\nEvaluador='${evaluador}'\nFinca='${finca}'\nLote='${lote}'"
                                }
                            } else if (!isCalibracion && loteNum !in 1..87) {
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
    var codImplemento by rememberSaveable { mutableStateOf(info.codImplemento) }
    
    var codTractor by rememberSaveable { 
        mutableStateOf(
            if (info.codTractor.isNotEmpty()) info.codTractor
            else if (info.codImplemento.isNotEmpty()) "TA-12"
            else "TA-12"
        ) 
    }

    var potenciaTractor by rememberSaveable { mutableStateOf(info.potenciaTractor) }
    var potenciaTdf by rememberSaveable { mutableStateOf(info.potenciaTdf) }
    var formula by rememberSaveable { mutableStateOf(info.formula) }
    var manualFormulaName by rememberSaveable { mutableStateOf("") }
    var presion by rememberSaveable { mutableStateOf(info.presion) }
    var volumen by rememberSaveable { mutableStateOf(info.volumen) }
    
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var capturedLocation by remember { mutableStateOf("") }
    val insumosList = remember { parseCsv(context) }
    val codigosUnicos = remember(insumosList) { insumosList.map { it.codigo }.distinct() }
    var expandedFormula by rememberSaveable { mutableStateOf(false) }
    // Initialize description if formula is passed from before
    var selectedDescripcion by rememberSaveable { mutableStateOf(
        insumosList.firstOrNull { it.codigo == info.formula }?.descripcion ?: ""
    ) }

    val implementList = remember { listOf("IA - 14", "IA - 28", "IA - 53", "IA - 64", "IA - 67", "IA - 81", "IA - 82") }
    var expandedImplement by rememberSaveable { mutableStateOf(false) }

    // --- Lógica Boquillas Aleatorias ---
    var longitudBrazoIzquierdo by rememberSaveable {
        mutableStateOf(
            if (info.longitudBrazoIzquierdo.isNotEmpty()) info.longitudBrazoIzquierdo
            else when (info.codImplemento) {
                "IA - 14", "IA - 53" -> "15.3"
                "IA - 28", "IA - 81" -> "15.8"
                "IA - 64" -> "14.65"
                "IA - 67", "IA - 82" -> "14.24"
                else -> ""
            }
        )
    }
    var longitudBrazoDerecho by rememberSaveable {
        mutableStateOf(
            if (info.longitudBrazoDerecho.isNotEmpty()) info.longitudBrazoDerecho
            else when (info.codImplemento) {
                "IA - 14", "IA - 53" -> "15.3"
                "IA - 28", "IA - 81" -> "15.8"
                "IA - 64" -> "14.65"
                "IA - 67", "IA - 82" -> "14.24"
                else -> ""
            }
        )
    }
    var cantidadBoquillasIzquierdas by rememberSaveable {
        mutableStateOf(
            if (info.cantidadBoquillasIzquierdas.isNotEmpty()) info.cantidadBoquillasIzquierdas
            else when (info.codImplemento) {
                "IA - 14", "IA - 53" -> "40"
                "IA - 28", "IA - 81", "IA - 64" -> "45"
                "IA - 67", "IA - 82" -> "50"
                else -> ""
            }
        )
    }
    var cantidadBoquillasDerechas by rememberSaveable {
        mutableStateOf(
            if (info.cantidadBoquillasDerechas.isNotEmpty()) info.cantidadBoquillasDerechas
            else when (info.codImplemento) {
                "IA - 14", "IA - 53" -> "40"
                "IA - 28", "IA - 81", "IA - 64" -> "45"
                "IA - 67", "IA - 82" -> "50"
                else -> ""
            }
        )
    }

    var leftNozzles by rememberSaveable { mutableStateOf(info.nozzlesIzquierdo) }
    var rightNozzles by rememberSaveable { mutableStateOf(info.nozzlesDerecho) }

    // --- Lógica Medición Manual ---
    var isMeasuring by rememberSaveable { mutableStateOf(false) }
    var startTime by rememberSaveable { mutableStateOf(0L) }
    var calcDistance by rememberSaveable { mutableStateOf(info.distanciaMetros) }
    var calcTimeSec by rememberSaveable { mutableStateOf(info.tiempoDesplazamientoSegundos) }
    var calcSpeed by rememberSaveable { mutableStateOf(info.velocidadKmh) }
    var manualDistance by rememberSaveable { mutableStateOf(if (info.distanciaMetros > 0f) info.distanciaMetros.toString() else "") }
    var displayVelocityTime by remember { mutableStateOf(info.tiempoDesplazamientoSegundos.toInt()) }

    LaunchedEffect(isMeasuring) {
        if (isMeasuring) {
            while (isMeasuring) {
                kotlinx.coroutines.delay(100L)
                displayVelocityTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
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
    var tamanoGotas by rememberSaveable { mutableStateOf(info.tamanoGotas) }
    var observaciones by rememberSaveable { mutableStateOf(info.observaciones) }

    var showRecommendationDialog by remember { mutableStateOf(false) }
    var recommendationMessage by remember { mutableStateOf("") }

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
                
                // Implement selector
                ExposedDropdownMenuBox(
                    expanded = expandedImplement,
                    onExpandedChange = { expandedImplement = !expandedImplement },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = codImplemento,
                        onValueChange = {},
                        label = { Text("Cód. Implemento") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedImplement)
                        }
                    )
                    DropdownMenu(
                        expanded = expandedImplement,
                        onDismissRequest = { expandedImplement = false },
                        modifier = Modifier.exposedDropdownSize().background(Color(0xFFEAD7BC))
                    ) {
                        implementList.forEach { cod ->
                            DropdownMenuItem(
                                text = { Text(cod, color = Color.Black) },
                                onClick = {
                                    codImplemento = cod
                                    expandedImplement = false
                                    codTractor = "TA-12"
                                    
                                    val longVal = when (cod) {
                                        "IA - 14", "IA - 53" -> "15.3"
                                        "IA - 28", "IA - 81" -> "15.8"
                                        "IA - 64" -> "14.65"
                                        "IA - 67", "IA - 82" -> "14.24"
                                        else -> ""
                                    }
                                    val boqVal = when (cod) {
                                        "IA - 14", "IA - 53" -> "40"
                                        "IA - 28", "IA - 81", "IA - 64" -> "45"
                                        "IA - 67", "IA - 82" -> "50"
                                        else -> ""
                                    }
                                    longitudBrazoIzquierdo = longVal
                                    longitudBrazoDerecho = longVal
                                    cantidadBoquillasIzquierdas = boqVal
                                    cantidadBoquillasDerechas = boqVal
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = codTractor,
                    onValueChange = {},
                    label = { Text("Cód. Tractor") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors(),
                    readOnly = true
                )
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
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contentScale = ContentScale.Fit
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

                OutlinedTextField(
                    value = longitudBrazoIzquierdo,
                    onValueChange = {},
                    label = { Text("Longitud Brazo Izquierdo (m)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors(),
                    readOnly = true
                )
                OutlinedTextField(
                    value = longitudBrazoDerecho,
                    onValueChange = {},
                    label = { Text("Longitud Brazo Derecho (m)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors(),
                    readOnly = true
                )
                OutlinedTextField(
                    value = cantidadBoquillasDerechas,
                    onValueChange = {},
                    label = { Text("Cantidad Boquillas Brazo Derecho") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors(),
                    readOnly = true
                )
                OutlinedTextField(
                    value = cantidadBoquillasIzquierdas,
                    onValueChange = {},
                    label = { Text("Cantidad Boquillas Brazo Izquierdo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors(),
                    readOnly = true
                )

                Button(
                    onClick = {
                        val n = cantidadBoquillasDerechas.toIntOrNull() ?: 0
                        val m = cantidadBoquillasIzquierdas.toIntOrNull() ?: 0
                        if (n > 0) {
                            val r1 = Random.nextInt(1, n + 1)
                            var r2 = Random.nextInt(1, n + 1)
                            if (n >= 2) {
                                while (r2 == r1) { r2 = Random.nextInt(1, n + 1) }
                                rightNozzles = listOf(NozzleData(r1), NozzleData(r2))
                            } else {
                                rightNozzles = listOf(NozzleData(r1))
                            }
                        } else {
                            rightNozzles = emptyList()
                        }
                        
                        if (m > 0) {
                            val r1 = Random.nextInt(n + 1, n + m + 1)
                            var r2 = Random.nextInt(n + 1, n + m + 1)
                            if (m >= 2) {
                                while (r2 == r1) { r2 = Random.nextInt(n + 1, n + m + 1) }
                                leftNozzles = listOf(NozzleData(r1), NozzleData(r2))
                            } else {
                                leftNozzles = listOf(NozzleData(r1))
                            }
                        } else {
                            leftNozzles = emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Generar Boquillas Aleatorias")
                }

                // --- Boquillas Aleatorias ---
                Text(text = "Brazo Derecho", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 16.dp))
                rightNozzles.forEachIndexed { index, nozzle ->
                    key(nozzle.id) {
                        BoquillaItem(nozzle = nozzle, onUpdate = { updated -> 
                            rightNozzles = rightNozzles.toMutableList().apply { this[index] = updated }
                        })
                    }
                }

                Text(text = "Brazo Izquierdo", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 16.dp))
                leftNozzles.forEachIndexed { index, nozzle ->
                    key(nozzle.id) {
                        BoquillaItem(nozzle = nozzle, onUpdate = { updated -> 
                            leftNozzles = leftNozzles.toMutableList().apply { this[index] = updated }
                        })
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.2f))

                // --- Medición de Desplazamiento ---
                Text(
                    text = "Medición Manual de Velocidad",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.5f), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        
                        Text(text = "Modo Manual: Use el cronómetro y digite la distancia.", fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                            FilledTonalButton(
                                modifier = Modifier.weight(1f),
                                enabled = !isMeasuring,
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)),
                                onClick = {
                                    startTime = System.currentTimeMillis()
                                    isMeasuring = true
                                }
                            ) {
                                Text("Iniciar", color = androidx.compose.ui.graphics.Color.White)
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = isMeasuring,
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE53935)),
                                onClick = {
                                    val eTime = System.currentTimeMillis()
                                    calcTimeSec = (eTime - startTime) / 1000
                                    displayVelocityTime = calcTimeSec.toInt()
                                    isMeasuring = false
                                    
                                    val dist = manualDistance.toFloatOrNull() ?: 0f
                                    if (dist > 0 && calcTimeSec > 0) {
                                        calcSpeed = (dist / calcTimeSec) * 3.6f
                                        calcDistance = dist
                                    }
                                }
                            ) {
                                Text("Detener", color = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                        
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

                // Resultados
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Tiempo: ${displayVelocityTime}s", color = Color.Black)
                    Text(text = "Distancia: ${String.format(Locale.US, "%.2f", calcDistance)}m", color = Color.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Velocidad: ${String.format(Locale.US, "%.2f", calcSpeed)} km/h", color = Color.Black)
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
                        value = tamanoGotas,
                        onValueChange = { tamanoGotas = it },
                        label = { Text("Tamaño de gotas") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

                if (showRecommendationDialog) {
                    val nTotal = (cantidadBoquillasIzquierdas.toIntOrNull() ?: 0) + (cantidadBoquillasDerechas.toIntOrNull() ?: 0)
                    val numClogged = if (boquillasTapadas == true) (boquillasTapadasNum.toIntOrNull() ?: 0) else 0
                    
                    val allEvaluated = leftNozzles + rightNozzles
                    val flowsMLs = mutableListOf<Float>()
                    val lhaResults = mutableListOf<Float>()
                    
                    val dist = calcDistance
                    val vTime = calcTimeSec
                    val longIzqVal = longitudBrazoIzquierdo.toFloatOrNull() ?: 0f
                    val longDerVal = longitudBrazoDerecho.toFloatOrNull() ?: 0f
                    
                    allEvaluated.forEach { nozzle ->
                        val nTime = nozzle.tiempoSegundos.toFloat()
                        val nVol = nozzle.volumen.toFloatOrNull() ?: 0f
                        if (nTime > 0) {
                            val flow = nVol / nTime
                            flowsMLs.add(flow)
                            val totalVolTripL = (flow * vTime) / 1000f
                            val isLeft = leftNozzles.any { it.id == nozzle.id }
                            val armLen = if (isLeft) longIzqVal else longDerVal
                            if (armLen > 0) {
                                val areaHa = (armLen * dist) / 10000f
                                if (areaHa > 0) {
                                    val lhaIndividual = (totalVolTripL / areaHa) * nTotal
                                    lhaResults.add(lhaIndividual)
                                }
                            }
                        }
                    }
                    
                    val avgLHa = if (lhaResults.isNotEmpty()) lhaResults.average().toFloat() else 0f
                    val volumenPercent = if (avgLHa > 0f) (avgLHa / 2000f) * 100f else 0f
                    val alturaPercent = if (alturaUniforme == true) 100f else 0f
                    val boquillasPercent = if (nTotal > 0) ((nTotal - numClogged).toFloat() / nTotal.toFloat()) * 100f else 100f
                    
                    val meanFlow = if (flowsMLs.isNotEmpty()) flowsMLs.average() else 0.0
                    val avgDevFlow = if (flowsMLs.isNotEmpty()) flowsMLs.map { Math.abs(it - meanFlow) }.average() else 0.0
                    val uniformidadPercent = if (meanFlow > 0.0) (1.0 - (avgDevFlow / meanFlow)) * 100.0 else 0.0

                    val volCumple = volumenPercent in 90f..100f
                    val altCumple = alturaPercent in 70f..100f
                    val boqCumple = boquillasPercent in 90f..100f
                    val uniCumple = uniformidadPercent in 90.0..100.0

                    AlertDialog(
                        onDismissRequest = { showRecommendationDialog = false },
                        title = { Text("Resultados y Recomendaciones", fontWeight = FontWeight.Bold, color = Color.Black) },
                        text = {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Header Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth().background(Color(0xFFEAD7BC)).padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Variable", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.5f), fontSize = 11.sp)
                                            Text(text = "Unid.", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, fontSize = 11.sp)
                                            Text(text = "Margen", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp)
                                            Text(text = "Valor", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center, fontSize = 11.sp)
                                            Text(text = "Est.", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center, fontSize = 11.sp)
                                        }

                                        val itemsList = listOf(
                                            Triple("VOLUMEN/Ha", "90 - 100%", Pair(volumenPercent, volCumple)),
                                            Triple("ALTURA UNIFORME", "70 - 100%", Pair(alturaPercent, altCumple)),
                                            Triple("BOQUILLAS TAPADAS", "90 - 100%", Pair(boquillasPercent, boqCumple)),
                                            Triple("UNIFORMIDAD", "90 - 100%", Pair(uniformidadPercent.toFloat(), uniCumple))
                                        )

                                        itemsList.forEach { (name, margin, res) ->
                                            val (percent, cumple) = res
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = name, color = Color.Black, modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                                Text(text = "%", color = Color.Black, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, fontSize = 10.sp)
                                                Text(text = margin, color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 10.sp)
                                                Text(
                                                    text = String.format(Locale.US, "%.1f%%", percent),
                                                    color = Color.Black,
                                                    modifier = Modifier.weight(0.8f),
                                                    textAlign = TextAlign.Center,
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = if (cumple) "✓" else "✗",
                                                    color = if (cumple) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(0.4f),
                                                    textAlign = TextAlign.Center,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
                                        }
                                    }
                                }

                                Text(
                                    text = recommendationMessage,
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { 
                                showRecommendationDialog = false
                                val updatedInfo = info.copy(
                                    operador = operador,
                                    codTractor = codTractor,
                                    codImplemento = codImplemento,
                                    potenciaTractor = potenciaTractor,
                                    potenciaTdf = potenciaTdf,
                                    formula = if (formula == "OTRO") manualFormulaName else formula,
                                    presion = presion,
                                    volumen = volumen,
                                    longitudBrazoIzquierdo = longitudBrazoIzquierdo,
                                    longitudBrazoDerecho = longitudBrazoDerecho,
                                    cantidadBoquillasIzquierdas = cantidadBoquillasIzquierdas,
                                    cantidadBoquillasDerechas = cantidadBoquillasDerechas,
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
                                    tamanoGotas = tamanoGotas,
                                    ubicacion = if (capturedLocation.isEmpty()) "0,0" else capturedLocation,
                                    velocidadOptima = info.velocidadOptima
                                )
                                onContinue(updatedInfo)
                            }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = blackTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) {
                        Text("Volver")
                    }
                    Button(
                        onClick = {
                            // --- Lógica de Recomendaciones ---
                            val sb = StringBuilder()
                            
                            // 1. Alerta de Presión
                            val highPressureNozzles = (leftNozzles + rightNozzles).filter { (it.presion.toFloatOrNull() ?: 0f) > 20f || (it.presion.toFloatOrNull() ?: 100f) < 15f }
                            if (highPressureNozzles.isNotEmpty()) {
                                sb.append("⚠️ ALERTA DE PRESIÓN:\n")
                                highPressureNozzles.forEach { n ->
                                    sb.append("- Boquilla #${n.id}: ${n.presion} PSI (Fuera de rango 15-20 PSI)\n")
                                }
                                sb.append("\n")
                            }

                            // 2. Cálculo de L/Ha y Velocidad Optima
                            val nTotal = (cantidadBoquillasIzquierdas.toIntOrNull() ?: 0) + (cantidadBoquillasDerechas.toIntOrNull() ?: 0)
                            val dist = calcDistance
                            val vTime = calcTimeSec
                            val longIzqVal = longitudBrazoIzquierdo.toFloatOrNull() ?: 0f
                            val longDerVal = longitudBrazoDerecho.toFloatOrNull() ?: 0f
                            val totalWidth = longIzqVal + longDerVal
                            
                            var finalVelOptima = 0f

                            if (nTotal > 0 && dist > 0 && vTime > 0) {
                                val allEvaluated = leftNozzles + rightNozzles
                                val lhaResults = mutableListOf<Float>()
                                val flowsMLs = mutableListOf<Float>()
                                
                                allEvaluated.forEach { nozzle ->
                                    val nTime = nozzle.tiempoSegundos.toFloat()
                                    val nVol = nozzle.volumen.toFloatOrNull() ?: 0f
                                    
                                    if (nTime > 0) {
                                        val flow = nVol / nTime
                                        flowsMLs.add(flow)
                                        val totalVolTripL = (flow * vTime) / 1000f
                                        
                                        // Determinar longitud del brazo
                                        val isLeft = leftNozzles.any { it.id == nozzle.id }
                                        val armLen = if (isLeft) longIzqVal else longDerVal
                                        
                                        if (armLen > 0) {
                                            val areaHa = (armLen * dist) / 10000f
                                            if (areaHa > 0) {
                                                val lhaIndividual = (totalVolTripL / areaHa) * nTotal
                                                lhaResults.add(lhaIndividual)
                                            }
                                        }
                                    }
                                }
                                
                                if (lhaResults.isNotEmpty()) {
                                    val avgLHa = lhaResults.average().toFloat()
                                    sb.append("📊 RESULTADO DE CAUDAL:\n")
                                    sb.append("- Promedio calculado: ${String.format(Locale.US, "%.2f", avgLHa)} L/Ha\n")
                                    if (avgLHa < 2000f) {
                                        sb.append("❌ ALERTA: El caudal es menor a 2000 L/Ha. Se recomienda revisar calibración.\n")
                                    } else {
                                        sb.append("✅ El caudal es igual o mayor a 2000 L/Ha.\n")
                                    }

                                    // Cálculo de velocidad óptima para 2000 L/Ha
                                    if (totalWidth > 0 && flowsMLs.isNotEmpty()) {
                                        val avgFlow = flowsMLs.average().toFloat()
                                        finalVelOptima = (avgFlow * nTotal * 36f) / (totalWidth * 2000f)
                                        sb.append("\n💡 RECOMENDACIÓN:\n")
                                        sb.append("- Para alcanzar 2000 L/Ha exactos, la velocidad óptima es: ${String.format(Locale.US, "%.2f", finalVelOptima)} km/h\n")
                                    }
                                } else {
                                    sb.append("ℹ️ No se pudo calcular L/Ha. Verifique longitudes de brazo y volumen de boquillas.\n")
                                }
                            } else {
                                sb.append("ℹ️ Faltan datos (distancia, tiempo o cantidad boquillas) para el cálculo de L/Ha.\n")
                            }

                            // 3. Captura GPS
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { loc: Location? ->
                                        if (loc != null) {
                                            capturedLocation = "${loc.latitude}, ${loc.longitude}"
                                        }
                                    }
                            } else {
                                sb.append("\n⚠️ Sin permiso de GPS: Las coordenadas no serán guardadas.\n")
                            }

                            if (sb.isEmpty()) sb.append("Auditoría guardada exitosamente.")
                            recommendationMessage = sb.toString()
                            
                            // Guardamos temporalmente en el objeto info que recibimos
                            info.velocidadOptima = finalVelOptima
                            info.observaciones = observaciones
                            // La ubicación se captura asíncronamente en capturedLocation y será usada en el confirmButton
                            
                            showRecommendationDialog = true
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
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    val unsyncedCount = audits.count { !it.isSynced && it.tipoAuditoria == "Spray Boom" }
                    if (unsyncedCount > 0) {
                        IconButton(
                            onClick = {
                                val sprayBoomAudits = audits.filter { it.tipoAuditoria == "Spray Boom" && !it.isSynced }
                                Toast.makeText(context, "Iniciando carga de $unsyncedCount registros...", Toast.LENGTH_SHORT).show()
                                
                                sprayBoomAudits.forEach { audit ->
                                    SyncManager.syncAudit(context, audit) { success, msg ->
                                        // Regresar al hilo principal para mostrar el Toast
                                        (context as? android.app.Activity)?.runOnUiThread {
                                            if (success) {
                                                audits = StorageManager.getAuditorias(context)
                                            }
                                            Toast.makeText(context, "Registro ${audit.lote}: $msg", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Subir Todo", tint = Color.Black)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    FilterChip(
                        selected = filterMode == "Calibracion Spray Boom",
                        onClick = { filterMode = "Calibracion Spray Boom" },
                        label = { Text("Calibración", color = Color.Black) }
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
                                        if (audit.isSynced) {
                                            Text(text = "✓ Sincronizado", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (audit.tipoAuditoria == "Calibracion Spray Boom") {
                                        IconButton(onClick = {
                                            PdfExportManager.exportToPdf(context, audit)
                                        }) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Descargar PDF", tint = Color.Black)
                                        }
                                    }

                                    if (!audit.isSynced && audit.tipoAuditoria == "Spray Boom") {
                                        IconButton(onClick = {
                                            SyncManager.syncAudit(context, audit) { success, msg ->
                                                (context as? android.app.Activity)?.runOnUiThread {
                                                    if (success) {
                                                        audits = StorageManager.getAuditorias(context)
                                                    }
                                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = "Sincronizar", tint = Color.Black)
                                        }
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
            val calibSheet = workbook.createSheet("Calibración Spray Boom")

            val headers = arrayOf(
                "ID", "Fecha", "Hora", "Evaluador", "Finca", "Lote", "Operador", 
                "CodTractor", "CodImplemento", "PotTractor", "PotTDF", "Formula", 
                "Presion", "Volumen", "Velocidad km/h", "Distancia m", "Tiempo s",
                "LongBrazoIzq", "LongBrazoDer", "CantBoqIzq", "CantBoqDer",
                "BoquillasTapadas", "NumTapadas", "PresenciaPersonal", "AlturaUniforme", 
                "EstadoVia", "PapelHidro", "Gotas 1cm2", "Tamaño Gotas",
                "B1_ID", "B1_Pres", "B1_Vol", "B1_Tiempo", "B2_ID", "B2_Pres", "B2_Vol", "B2_Tiempo",
                "B3_ID", "B3_Pres", "B3_Vol", "B3_Tiempo", "B4_ID", "B4_Pres", "B4_Vol", "B4_Tiempo",
                "Ubicacion", "Vel_Optima", "Sincronizado", "Observaciones"
            )
            
            val mezclasHeaders = arrayOf(
                "ID", "Fecha", "Hora", "Evaluador", "Finca", "Lote", 
                "pH Inicial", "Dureza Agua", "CE Agua mS/cm",
                "pH Final", "CE Final mS/cm",
                "Mezclador", "Formula",
                "Productos Evaluados (JSON)", "Incompatibilidad", "Respeta Orden", "Obs Orden",
                "Usa EPP", "Obs EPP", "Tanque Limpio", "Obs Tanque", "Observaciones"
            )

            val calibHeaders = arrayOf(
                "ID", "Fecha", "Hora", "Evaluador", "Finca", "Lote", "Operario", 
                "CodTractor", "VolumenTanque", "CodImplemento", "NumBoquillas", 
                "TipoBoquillas", "ReferenciaBoquillas", "TiempoDescarga", "Descarga", 
                "CaudalBoquilla", "CaudalTotal", "PromedioDistancia", "PromedioTiempo", 
                "PromedioVelocidadKmh", "AreaRecorridaHa", "AreaTotalTanqueHa", 
                "VelocidadRequeridaKmh", "Observaciones", "CalculosRecorrido (JSON)"
            )

            // Setup Spray Boom Sheet
            var row0 = sbSheet.createRow(0)
            headers.forEachIndexed { i, h -> row0.createCell(i).setCellValue(h) }
            
            // Setup Mezclas Sheet
            var mRow0 = mezclasSheet.createRow(0)
            mezclasHeaders.forEachIndexed { i, h -> mRow0.createCell(i).setCellValue(h) }

            // Setup Calibración Sheet
            var calRow0 = calibSheet.createRow(0)
            calibHeaders.forEachIndexed { i, h -> calRow0.createCell(i).setCellValue(h) }

            var sbRowIdx = 1
            var mRowIdx = 1
            var calRowIdx = 1

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
                    row.createCell(21).setCellValue(audit.observaciones)
                } else if (audit.tipoAuditoria == "Calibracion Spray Boom") {
                    val row = calibSheet.createRow(calRowIdx++)
                    row.createCell(0).setCellValue(audit.id)
                    row.createCell(1).setCellValue(dateStr)
                    row.createCell(2).setCellValue(audit.hora)
                    row.createCell(3).setCellValue(audit.evaluador)
                    row.createCell(4).setCellValue(audit.finca)
                    row.createCell(5).setCellValue(audit.lote)
                    row.createCell(6).setCellValue(audit.operarioCalib)
                    row.createCell(7).setCellValue(audit.tractorCalib)
                    row.createCell(8).setCellValue(audit.volumenTanque)
                    row.createCell(9).setCellValue(audit.implementoCalib)
                    row.createCell(10).setCellValue(audit.numBoquillas.toDouble())
                    row.createCell(11).setCellValue(audit.tipoBoquillas)
                    row.createCell(12).setCellValue(audit.referenciaBoquillas)
                    row.createCell(13).setCellValue(audit.tiempoCalib)
                    row.createCell(14).setCellValue(audit.descargaCalib)
                    
                    val q = if (audit.tiempoCalib > 0.0) (audit.descargaCalib / audit.tiempoCalib) * 0.06 else 0.0
                    val qTot = audit.numBoquillas * q
                    row.createCell(15).setCellValue(q)
                    row.createCell(16).setCellValue(qTot)
                    
                    val validRuns = audit.calculosRecorrido.filter { it.distancia > 0.0 && it.tiempo > 0.0 }
                    val avgDist = if (validRuns.isNotEmpty()) validRuns.map { it.distancia }.average() else 0.0
                    val avgTime = if (validRuns.isNotEmpty()) validRuns.map { it.tiempo }.average() else 0.0
                    val avgVol = if (validRuns.isNotEmpty()) validRuns.map { it.volumenAplicado }.average() else 0.0
                    val avgVelKmh = if (avgTime > 0.0) (avgDist / avgTime) * 3.6 else 0.0
                    
                    val distBoquillas = when (audit.implementoCalib) {
                        "IA - 14", "IA - 28", "IA - 53", "IA - 81" -> 0.4
                        "IA - 64", "IA - 67", "IA - 82" -> 0.3
                        else -> 0.5
                    }
                    val w = audit.numBoquillas * distBoquillas
                    val areaRecHa = (w * avgDist) / 10000.0
                    val dosReal = if (areaRecHa > 0.0) avgVol / areaRecHa else 0.0
                    val areaTotTanq = if (dosReal > 0.0) audit.volumenTanque / dosReal else 0.0
                    val velReq = q * 0.6
                    
                    row.createCell(17).setCellValue(avgDist)
                    row.createCell(18).setCellValue(avgTime)
                    row.createCell(19).setCellValue(avgVelKmh)
                    row.createCell(20).setCellValue(areaRecHa)
                    row.createCell(21).setCellValue(areaTotTanq)
                    row.createCell(22).setCellValue(velReq)
                    row.createCell(23).setCellValue(audit.observaciones)
                    
                    val jsonRecorridos = com.google.gson.Gson().toJson(audit.calculosRecorrido)
                    row.createCell(24).setCellValue(jsonRecorridos)
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
                row.createCell(16).setCellValue(audit.tiempoDesplazamientoSegundos.toString())
                row.createCell(17).setCellValue(audit.longitudBrazoIzquierdo)
                row.createCell(18).setCellValue(audit.longitudBrazoDerecho)
                row.createCell(19).setCellValue(audit.cantidadBoquillasIzquierdas)
                row.createCell(20).setCellValue(audit.cantidadBoquillasDerechas)
                
                row.createCell(21).setCellValue(if (audit.boquillasTapadas == true) "SI" else if (audit.boquillasTapadas == false) "NO" else "")
                row.createCell(22).setCellValue(audit.boquillasTapadasNum)
                row.createCell(23).setCellValue(if (audit.presenciaPersonal == true) "SI" else if (audit.presenciaPersonal == false) "NO" else "")
                row.createCell(24).setCellValue(if (audit.alturaUniforme == true) "SI" else if (audit.alturaUniforme == false) "NO" else "")
                row.createCell(25).setCellValue(audit.estadoVia)
                row.createCell(26).setCellValue(if (audit.papelHidrosensible) "SI" else "NO")
                row.createCell(27).setCellValue(audit.papelGotas1cm)
                row.createCell(28).setCellValue(audit.tamanoGotas)
                
                // --- Nozzles Serialized ---
                val allNozzles = audit.nozzlesIzquierdo + audit.nozzlesDerecho
                for (i in 0 until 4) {
                    if (i < allNozzles.size) {
                        val n = allNozzles[i]
                        row.createCell(29 + (i * 4)).setCellValue(n.id.toDouble())
                        row.createCell(30 + (i * 4)).setCellValue(n.presion)
                        row.createCell(31 + (i * 4)).setCellValue(n.volumen)
                        row.createCell(32 + (i * 4)).setCellValue(n.tiempoSegundos.toString())
                    }
                }
                row.createCell(45).setCellValue(audit.ubicacion)
                row.createCell(46).setCellValue(audit.velocidadOptima.toDouble())
                row.createCell(47).setCellValue(if (audit.isSynced) "SI" else "NO")
                row.createCell(48).setCellValue(audit.observaciones)
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
    var observaciones by rememberSaveable { mutableStateOf(info.observaciones) }

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

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = blackTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

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
                                ceFinal = ceFinal,
                                observaciones = observaciones
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
fun BoquillaItem(nozzle: NozzleData, onUpdate: (NozzleData) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Boquilla #${nozzle.id}", fontWeight = FontWeight.Bold, color = Color.Black)
            
            var isRunning by remember { mutableStateOf(false) }
            var startTime by remember { mutableStateOf(0L) }
            var acumTime by remember { mutableStateOf(nozzle.tiempoSegundos) }
            var displayTime by remember { mutableStateOf(nozzle.tiempoSegundos) }

            LaunchedEffect(isRunning) {
                while (isRunning) {
                    kotlinx.coroutines.delay(100L)
                    val current = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                    displayTime = acumTime + current
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tiempo cronometrado: ${displayTime} s", fontWeight = FontWeight.Medium, color = Color.Black)
                Button(
                    onClick = {
                        if (isRunning) {
                            isRunning = false
                            acumTime = displayTime
                            onUpdate(nozzle.copy(tiempoSegundos = acumTime))
                        } else {
                            startTime = System.currentTimeMillis()
                            isRunning = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color(0xFFE53935) else Color(0xFF4CAF50))
                ) {
                    Text(if (isRunning) "Detener" else "Iniciar")
                }
            }

            OutlinedTextField(
                value = nozzle.volumen,
                onValueChange = { onUpdate(nozzle.copy(volumen = it)) },
                label = { Text("Volumen (ml)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
            )
            OutlinedTextField(
                value = nozzle.presion,
                onValueChange = { onUpdate(nozzle.copy(presion = it)) },
                label = { Text("Presión (PSI)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = blackTextFieldColors()
            )
        }
    }
}

@Composable
fun CalculoRecorridoCard(
    index: Int,
    calculo: CalculoRecorrido,
    onUpdate: (CalculoRecorrido) -> Unit,
    onDelete: () -> Unit
) {
    var distStr by rememberSaveable(calculo.id) { mutableStateOf(if (calculo.distancia > 0.0) calculo.distancia.toString() else "") }
    var tiempoStr by rememberSaveable(calculo.id) { mutableStateOf(if (calculo.tiempo > 0.0) calculo.tiempo.toString() else "") }
    var volStr by rememberSaveable(calculo.id) { mutableStateOf(if (calculo.volumenAplicado > 0.0) calculo.volumenAplicado.toString() else "") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cálculo #${index + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar cálculo", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = distStr,
                onValueChange = {
                    distStr = it
                    val dVal = it.toDoubleOrNull() ?: 0.0
                    onUpdate(calculo.copy(distancia = dVal))
                },
                label = { Text("Distancia recorrida (metros)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = blackTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tiempoStr,
                onValueChange = {
                    tiempoStr = it
                    val tVal = it.toDoubleOrNull() ?: 0.0
                    onUpdate(calculo.copy(tiempo = tVal))
                },
                label = { Text("Tiempo recorrido (segundos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = blackTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = volStr,
                onValueChange = {
                    volStr = it
                    val vVal = it.toDoubleOrNull() ?: 0.0
                    onUpdate(calculo.copy(volumenAplicado = vVal))
                },
                label = { Text("Volumen aplicado (litros)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = blackTextFieldColors()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioCalibracionScreen(
    info: AuditoriaInfo,
    onBack: () -> Unit,
    onContinue: (AuditoriaInfo) -> Unit
) {
    var operario by rememberSaveable { mutableStateOf(info.operarioCalib) }
    var codImplemento by rememberSaveable { mutableStateOf(info.implementoCalib) }
    
    var codTractor by rememberSaveable { 
        mutableStateOf(
            if (info.tractorCalib.isNotEmpty()) info.tractorCalib
            else if (info.implementoCalib.isNotEmpty()) "TA-12"
            else "TA-12"
        ) 
    }
    
    var numBoquillasStr by rememberSaveable {
        mutableStateOf(
            if (info.numBoquillas > 0) info.numBoquillas.toString()
            else when (info.implementoCalib) {
                "IA - 14", "IA - 53" -> "80"
                "IA - 28", "IA - 81", "IA - 64" -> "90"
                "IA - 67", "IA - 82" -> "100"
                else -> ""
            }
        )
    }

    var volumenTanqueStr by rememberSaveable { mutableStateOf(if (info.volumenTanque > 0.0) info.volumenTanque.toString() else "") }
    var tipoBoquillas by rememberSaveable { mutableStateOf(info.tipoBoquillas) }
    var tiempoStr by rememberSaveable { mutableStateOf(if (info.tiempoCalib > 0.0) info.tiempoCalib.toString() else "") }
    var descargaStr by rememberSaveable { mutableStateOf(if (info.descargaCalib > 0.0) info.descargaCalib.toString() else "") }
    var listRecorridos by remember { mutableStateOf(info.calculosRecorrido) }
    var observaciones by rememberSaveable { mutableStateOf(info.observaciones) }

    val context = LocalContext.current
    val implementList = remember { listOf("IA - 14", "IA - 28", "IA - 53", "IA - 64", "IA - 67", "IA - 81", "IA - 82") }
    var expandedImplement by rememberSaveable { mutableStateOf(false) }
    var expandedTipoBoquillas by rememberSaveable { mutableStateOf(false) }

    val distBoquillas = when (codImplemento) {
        "IA - 14", "IA - 28", "IA - 53", "IA - 81" -> 0.4
        "IA - 64", "IA - 67", "IA - 82" -> 0.3
        else -> 0.5
    }

    val longitudBrazo = when (codImplemento) {
        "IA - 14", "IA - 53" -> 15.3
        "IA - 28", "IA - 81" -> 15.8
        "IA - 64" -> 14.65
        "IA - 67", "IA - 82" -> 14.24
        else -> 0.0
    }

    val numBoquillas = numBoquillasStr.toIntOrNull() ?: 0
    val tiempoVal = tiempoStr.toDoubleOrNull() ?: 0.0
    val descargaVal = descargaStr.toDoubleOrNull() ?: 0.0
    val volTanqueVal = volumenTanqueStr.toDoubleOrNull() ?: 0.0

    // Calculations
    val caudalBoquilla = if (tiempoVal > 0.0) (descargaVal / tiempoVal) * 0.06 else 0.0 // L/min per nozzle
    val caudalTotal = numBoquillas * caudalBoquilla // L/min total

    // Travel run averages
    val validRecorridos = listRecorridos.filter { it.distancia > 0.0 && it.tiempo > 0.0 }
    val avgDistancia = if (validRecorridos.isNotEmpty()) validRecorridos.map { it.distancia }.average() else 0.0
    val avgTiempo = if (validRecorridos.isNotEmpty()) validRecorridos.map { it.tiempo }.average() else 0.0
    val avgVolumen = if (validRecorridos.isNotEmpty()) validRecorridos.map { it.volumenAplicado }.average() else 0.0

    val avgVelocidad = if (avgTiempo > 0.0) avgDistancia / avgTiempo else 0.0 // m/s
    val avgVelocidadKmh = avgVelocidad * 3.6

    // Ancho de trabajo (m) = numBoquillas * distBoquillas
    val anchoTrabajo = numBoquillas * distBoquillas
    val areaRecorridaHa = (anchoTrabajo * avgDistancia) / 10000.0
    val dosisRealLHa = if (areaRecorridaHa > 0.0) avgVolumen / areaRecorridaHa else 0.0
    val areaTotalTanqueHa = if (dosisRealLHa > 0.0) volTanqueVal / dosisRealLHa else 0.0

    // Velocidad requerida para 2000 L/Ha
    val velRequeridaKmh = caudalBoquilla * 0.6

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
                    text = "Calibración Spray Boom",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Header Info Summary
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
                            text = "Información General",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Evaluador: ${info.evaluador}", fontSize = 14.sp)
                        if (info.finca.isNotEmpty()) {
                            Text(text = "Finca: ${info.finca}", fontSize = 14.sp)
                        }
                        if (info.lote.isNotEmpty()) {
                            Text(text = "Lote: ${info.lote}", fontSize = 14.sp)
                        }
                        Text(text = "Fecha: ${dateFormatter.format(Date(info.fecha))}", fontSize = 14.sp)
                        Text(text = "Hora: ${info.hora}", fontSize = 14.sp)
                    }
                }

                Text(
                    text = "Formulario de Calibración",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = operario,
                    onValueChange = { operario = it },
                    label = { Text("Operario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors()
                )

                // Implement selector
                ExposedDropdownMenuBox(
                    expanded = expandedImplement,
                    onExpandedChange = { expandedImplement = !expandedImplement },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = codImplemento,
                        onValueChange = {},
                        label = { Text("Código de implemento") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedImplement)
                        }
                    )
                    DropdownMenu(
                        expanded = expandedImplement,
                        onDismissRequest = { expandedImplement = false },
                        modifier = Modifier.exposedDropdownSize().background(Color(0xFFEAD7BC))
                    ) {
                        implementList.forEach { cod ->
                            DropdownMenuItem(
                                text = { Text(cod, color = Color.Black) },
                                onClick = {
                                    codImplemento = cod
                                    expandedImplement = false
                                    codTractor = "TA-12"
                                    numBoquillasStr = when (cod) {
                                        "IA - 14", "IA - 53" -> "80"
                                        "IA - 28", "IA - 81", "IA - 64" -> "90"
                                        "IA - 67", "IA - 82" -> "100"
                                        else -> ""
                                    }
                                }
                            )
                        }
                    }
                }

                if (codImplemento.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Datos Básicos del Implemento",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Código de Tractor:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                                Text(text = codTractor, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Número de boquillas:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                                Text(text = numBoquillasStr, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Dist. entre Boquillas:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                                Text(text = "$distBoquillas m", color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Longitud Brazo:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                                Text(text = "$longitudBrazo m", color = Color.Black)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = volumenTanqueStr,
                        onValueChange = { volumenTanqueStr = it },
                        label = { Text("Volumen del tanque (Litros)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedTipoBoquillas,
                        onExpandedChange = { expandedTipoBoquillas = !expandedTipoBoquillas },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tipoBoquillas,
                            onValueChange = {},
                            label = { Text("Tipo de boquilla") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = blackTextFieldColors(),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoBoquillas)
                            }
                        )
                        DropdownMenu(
                            expanded = expandedTipoBoquillas,
                            onDismissRequest = { expandedTipoBoquillas = false },
                            modifier = Modifier.exposedDropdownSize().background(Color(0xFFEAD7BC))
                        ) {
                            listOf("Cono", "Abanico").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, color = Color.Black) },
                                    onClick = {
                                        tipoBoquillas = type
                                        expandedTipoBoquillas = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tiempoStr,
                        onValueChange = { tiempoStr = it },
                        label = { Text("Tiempo de descarga (segundos)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )

                    OutlinedTextField(
                        value = descargaStr,
                        onValueChange = { descargaStr = it },
                        label = { Text("Descarga (cc)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = blackTextFieldColors()
                    )
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Cálculos de Recorrido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // List of travel calculation cards
                listRecorridos.forEachIndexed { idx, item ->
                    CalculoRecorridoCard(
                        index = idx,
                        calculo = item,
                        onUpdate = { updated ->
                            listRecorridos = listRecorridos.toMutableList().apply { this[idx] = updated }
                        },
                        onDelete = {
                            listRecorridos = listRecorridos.toMutableList().apply { removeAt(idx) }
                        }
                    )
                }

                Button(
                    onClick = {
                        listRecorridos = listRecorridos + CalculoRecorrido()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar nuevo cálculo", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar nuevo cálculo", fontWeight = FontWeight.SemiBold)
                }

                // Averages Summary Card
                if (validRecorridos.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Averages (Promedios)",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(text = "Distancia: ${String.format(Locale.US, "%.2f", avgDistancia)} m", color = Color.Black)
                            Text(text = "Tiempo: ${String.format(Locale.US, "%.2f", avgTiempo)} s", color = Color.Black)
                            Text(text = "Volumen Aplicado: ${String.format(Locale.US, "%.2f", avgVolumen)} L", color = Color.Black)
                        }
                    }
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                // Calculated Results Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Resultados Calculados",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Caudal boquilla:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(text = "${String.format(Locale.US, "%.3f", caudalBoquilla)} L/min", color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Caudal total barra:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(text = "${String.format(Locale.US, "%.3f", caudalTotal)} L/min", color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Velocidad promedio:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(text = "${String.format(Locale.US, "%.2f", avgVelocidad)} m/s", color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Velocidad promedio km/h:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(text = "${String.format(Locale.US, "%.2f", avgVelocidadKmh)} km/h", color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Ancho trabajo (barra):", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(text = "${String.format(Locale.US, "%.2f", anchoTrabajo)} m (boquillas * ${distBoquillas}m)", color = Color.DarkGray, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Área recorrida prom:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(text = "${String.format(Locale.US, "%.4f", areaRecorridaHa)} Ha", color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Dosis real prom:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(text = "${String.format(Locale.US, "%.1f", dosisRealLHa)} L/Ha", color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Área total tanque:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(text = "${String.format(Locale.US, "%.2f", areaTotalTanqueHa)} Ha", color = Color.Black)
                        }
                        
                        HorizontalDivider(color = Color.Black.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Velocidad requerida para 2000 L/Ha:", color = Color.Black, fontWeight = FontWeight.Bold)
                            Text(text = "${String.format(Locale.US, "%.2f", velRequeridaKmh)} km/h", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = blackTextFieldColors()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
                            val finalInfo = info.copy(
                                operarioCalib = operario,
                                tractorCalib = codTractor,
                                volumenTanque = volTanqueVal,
                                implementoCalib = codImplemento,
                                numBoquillas = numBoquillas,
                                tipoBoquillas = tipoBoquillas,
                                referenciaBoquillas = "",
                                tiempoCalib = tiempoVal,
                                descargaCalib = descargaVal,
                                calculosRecorrido = listRecorridos,
                                observaciones = observaciones,
                                
                                // Re-use general fields
                                operador = operario,
                                codTractor = codTractor,
                                codImplemento = codImplemento,
                                volumen = volumenTanqueStr,
                                velocidadKmh = avgVelocidadKmh.toFloat(),
                                distanciaMetros = avgDistancia.toFloat(),
                                tiempoDesplazamientoSegundos = avgTiempo.toLong(),
                                velocidadOptima = velRequeridaKmh.toFloat()
                            )
                            onContinue(finalInfo)
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

object PdfExportManager {
    fun exportToPdf(context: android.content.Context, audit: AuditoriaInfo) {
        try {
            val pdfDocument = android.graphics.pdf.PdfDocument()
            // A4 dimensions: 595 x 842 points
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paintText = android.graphics.Paint()
            val paintLine = android.graphics.Paint()
            
            val margin = 40f
            var currentY = 50f

            // 1. HEADER BANNER
            paintLine.color = android.graphics.Color.rgb(0, 100, 80) // Dark green
            paintLine.style = android.graphics.Paint.Style.FILL
            canvas.drawRect(margin, currentY, 595f - margin, currentY + 60f, paintLine)

            paintText.color = android.graphics.Color.WHITE
            paintText.textSize = 18f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            paintText.textAlign = android.graphics.Paint.Align.CENTER
            canvas.drawText("REPORTE DE CALIBRACIÓN - SPRAY BOOM", 595f / 2f, currentY + 36f, paintText)
            
            currentY += 80f

            // 2. GENERAL INFORMATION SECTION
            paintText.color = android.graphics.Color.rgb(0, 100, 80)
            paintText.textSize = 14f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            paintText.textAlign = android.graphics.Paint.Align.LEFT
            canvas.drawText("INFORMACIÓN GENERAL", margin, currentY, paintText)
            
            paintLine.color = android.graphics.Color.rgb(0, 100, 80)
            paintLine.strokeWidth = 2f
            paintLine.style = android.graphics.Paint.Style.STROKE
            canvas.drawLine(margin, currentY + 4f, 595f - margin, currentY + 4f, paintLine)
            
            currentY += 24f

            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(audit.fecha))
            
            paintText.color = android.graphics.Color.BLACK
            paintText.textSize = 10f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            
            val col1X = margin + 10f
            val col2X = 300f
            
            canvas.drawText("Evaluador: ${audit.evaluador}", col1X, currentY, paintText)
            canvas.drawText("Operario: ${audit.operarioCalib}", col2X, currentY, paintText)
            currentY += 16f
            
            if (audit.finca.isNotEmpty()) {
                canvas.drawText("Finca: ${audit.finca}", col1X, currentY, paintText)
            }
            canvas.drawText("Tractor: ${audit.tractorCalib}", col2X, currentY, paintText)
            currentY += 16f
            
            if (audit.lote.isNotEmpty()) {
                canvas.drawText("Lote: ${audit.lote}", col1X, currentY, paintText)
            }
            canvas.drawText("Implemento: ${audit.implementoCalib}", col2X, currentY, paintText)
            currentY += 16f
            
            canvas.drawText("Fecha: $dateStr", col1X, currentY, paintText)
            canvas.drawText("Volumen Tanque: ${audit.volumenTanque} L", col2X, currentY, paintText)
            currentY += 16f
            
            canvas.drawText("Hora: ${audit.hora}", col1X, currentY, paintText)
            currentY += 24f

            // 3. TECHNICAL SPECIFICATIONS SECTION
            paintText.color = android.graphics.Color.rgb(0, 100, 80)
            paintText.textSize = 14f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            canvas.drawText("ESPECIFICACIONES TÉCNICAS", margin, currentY, paintText)
            canvas.drawLine(margin, currentY + 4f, 595f - margin, currentY + 4f, paintLine)
            currentY += 24f

            paintText.color = android.graphics.Color.BLACK
            paintText.textSize = 10f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            
            val distBoquillas = when (audit.implementoCalib) {
                "IA - 14", "IA - 28", "IA - 53", "IA - 81" -> 0.4
                "IA - 64", "IA - 67", "IA - 82" -> 0.3
                else -> 0.5
            }
            val longitudBrazo = when (audit.implementoCalib) {
                "IA - 14", "IA - 53" -> 15.3
                "IA - 28", "IA - 81" -> 15.8
                "IA - 64" -> 14.65
                "IA - 67", "IA - 82" -> 14.24
                else -> 0.0
            }
            
            canvas.drawText("Número de boquillas: ${audit.numBoquillas}", col1X, currentY, paintText)
            canvas.drawText("Tiempo de descarga: ${audit.tiempoCalib} s", col2X, currentY, paintText)
            currentY += 16f
            
            canvas.drawText("Tipo de boquillas: ${audit.tipoBoquillas}", col1X, currentY, paintText)
            canvas.drawText("Descarga promedio: ${audit.descargaCalib} cc", col2X, currentY, paintText)
            currentY += 16f
            
            canvas.drawText("Dist. entre Boquillas: ${distBoquillas} m", col1X, currentY, paintText)
            if (longitudBrazo > 0.0) {
                canvas.drawText("Longitud Brazo: ${longitudBrazo} m", col2X, currentY, paintText)
            }
            currentY += 24f

            // 4. RUNS TABLE (CÁLCULOS DE RECORRIDO)
            paintText.color = android.graphics.Color.rgb(0, 100, 80)
            paintText.textSize = 14f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            canvas.drawText("CÁLCULOS DE RECORRIDO", margin, currentY, paintText)
            canvas.drawLine(margin, currentY + 4f, 595f - margin, currentY + 4f, paintLine)
            currentY += 24f

            // Table Header Background
            paintLine.color = android.graphics.Color.rgb(235, 245, 240)
            paintLine.style = android.graphics.Paint.Style.FILL
            canvas.drawRect(margin, currentY - 12f, 595f - margin, currentY + 12f, paintLine)

            paintText.color = android.graphics.Color.rgb(0, 100, 80)
            paintText.textSize = 9f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            
            val tCol1 = margin + 10f
            val tCol2 = margin + 100f
            val tCol3 = margin + 200f
            val tCol4 = margin + 300f
            val tCol5 = margin + 400f
            
            canvas.drawText("Corrida #", tCol1, currentY, paintText)
            canvas.drawText("Distancia (m)", tCol2, currentY, paintText)
            canvas.drawText("Tiempo (s)", tCol3, currentY, paintText)
            canvas.drawText("Volumen (L)", tCol4, currentY, paintText)
            canvas.drawText("Velocidad (km/h)", tCol5, currentY, paintText)
            
            paintLine.color = android.graphics.Color.rgb(200, 200, 200)
            paintLine.style = android.graphics.Paint.Style.STROKE
            paintLine.strokeWidth = 1f
            canvas.drawLine(margin, currentY + 12f, 595f - margin, currentY + 12f, paintLine)
            
            currentY += 24f

            paintText.color = android.graphics.Color.BLACK
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            
            val validRuns = audit.calculosRecorrido.filter { it.distancia > 0.0 && it.tiempo > 0.0 }
            
            validRuns.forEachIndexed { i, run ->
                val vKmh = (run.distancia / run.tiempo) * 3.6
                canvas.drawText("Corrida #${i + 1}", tCol1, currentY, paintText)
                canvas.drawText(String.format(java.util.Locale.US, "%.2f", run.distancia), tCol2, currentY, paintText)
                canvas.drawText(String.format(java.util.Locale.US, "%.2f", run.tiempo), tCol3, currentY, paintText)
                canvas.drawText(String.format(java.util.Locale.US, "%.2f", run.volumenAplicado), tCol4, currentY, paintText)
                canvas.drawText(String.format(java.util.Locale.US, "%.2f", vKmh), tCol5, currentY, paintText)
                
                canvas.drawLine(margin, currentY + 4f, 595f - margin, currentY + 4f, paintLine)
                currentY += 18f
            }

            if (validRuns.isNotEmpty()) {
                val avgDist = validRuns.map { it.distancia }.average()
                val avgTime = validRuns.map { it.tiempo }.average()
                val avgVol = validRuns.map { it.volumenAplicado }.average()
                val avgVel = if (avgTime > 0.0) avgDist / avgTime else 0.0
                val avgVelKmh = avgVel * 3.6

                paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                canvas.drawText("PROMEDIO", tCol1, currentY, paintText)
                canvas.drawText(String.format(java.util.Locale.US, "%.2f", avgDist), tCol2, currentY, paintText)
                canvas.drawText(String.format(java.util.Locale.US, "%.2f", avgTime), tCol3, currentY, paintText)
                canvas.drawText(String.format(java.util.Locale.US, "%.2f", avgVol), tCol4, currentY, paintText)
                canvas.drawText(String.format(java.util.Locale.US, "%.2f", avgVelKmh), tCol5, currentY, paintText)

                paintLine.color = android.graphics.Color.rgb(0, 100, 80)
                paintLine.strokeWidth = 1.5f
                canvas.drawLine(margin, currentY + 4f, 595f - margin, currentY + 4f, paintLine)
                currentY += 28f
            } else {
                canvas.drawText("No se registraron corridas de recorrido válidas.", margin + 10f, currentY, paintText)
                currentY += 28f
            }

            // 5. CALCULATED RESULTS SECTION
            paintText.color = android.graphics.Color.rgb(0, 100, 80)
            paintText.textSize = 14f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            canvas.drawText("RESULTADOS FINALES Y CÁLCULOS", margin, currentY, paintText)
            canvas.drawLine(margin, currentY + 4f, 595f - margin, currentY + 4f, paintLine)
            currentY += 24f

            paintLine.color = android.graphics.Color.rgb(0, 100, 80)
            paintLine.style = android.graphics.Paint.Style.STROKE
            paintLine.strokeWidth = 1f
            canvas.drawRect(margin, currentY - 12f, 595f - margin, currentY + 130f, paintLine)

            paintText.color = android.graphics.Color.BLACK
            paintText.textSize = 10f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)

            val rCol1 = margin + 15f
            val rCol2 = 300f

            val q = if (audit.tiempoCalib > 0.0) (audit.descargaCalib / audit.tiempoCalib) * 0.06 else 0.0
            val qTot = audit.numBoquillas * q
            val avgDist = if (validRuns.isNotEmpty()) validRuns.map { it.distancia }.average() else 0.0
            val avgTime = if (validRuns.isNotEmpty()) validRuns.map { it.tiempo }.average() else 0.0
            val avgVol = if (validRuns.isNotEmpty()) validRuns.map { it.volumenAplicado }.average() else 0.0
            val avgVel = if (avgTime > 0.0) avgDist / avgTime else 0.0
            val avgVelKmh = avgVel * 3.6
            
            val distBoquillasVal = when (audit.implementoCalib) {
                "IA - 14", "IA - 28", "IA - 53", "IA - 81" -> 0.4
                "IA - 64", "IA - 67", "IA - 82" -> 0.3
                else -> 0.5
            }
            val w = audit.numBoquillas * distBoquillasVal
            val areaRecHa = (w * avgDist) / 10000.0
            val dosReal = if (areaRecHa > 0.0) avgVol / areaRecHa else 0.0
            val areaTotTanq = if (dosReal > 0.0) audit.volumenTanque / dosReal else 0.0
            val velReq = q * 0.6

            canvas.drawText("Caudal por Boquilla: ${String.format(java.util.Locale.US, "%.3f", q)} L/min", rCol1, currentY, paintText)
            canvas.drawText("Velocidad Promedio: ${String.format(java.util.Locale.US, "%.2f", avgVelKmh)} km/h", rCol2, currentY, paintText)
            currentY += 18f

            canvas.drawText("Caudal Total Barra: ${String.format(java.util.Locale.US, "%.3f", qTot)} L/min", rCol1, currentY, paintText)
            canvas.drawText("Área Recorrida Promedio: ${String.format(java.util.Locale.US, "%.4f", areaRecHa)} Ha", rCol2, currentY, paintText)
            currentY += 18f

            canvas.drawText("Ancho de Barra (${String.format(java.util.Locale.US, "%.0f", distBoquillasVal * 100)}cm esp.): ${String.format(java.util.Locale.US, "%.2f", w)} m", rCol1, currentY, paintText)
            canvas.drawText("Dosis Real Aplicada: ${String.format(java.util.Locale.US, "%.1f", dosReal)} L/Ha", rCol2, currentY, paintText)
            currentY += 18f

            canvas.drawText("Área Total por Tanque: ${String.format(java.util.Locale.US, "%.2f", areaTotTanq)} Ha", rCol1, currentY, paintText)
            currentY += 24f

            paintText.textSize = 11f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            paintText.color = android.graphics.Color.rgb(0, 100, 80)
            canvas.drawText("Velocidad requerida para llegar a 2000 L/Ha: ${String.format(java.util.Locale.US, "%.2f", velReq)} km/h", rCol1, currentY, paintText)
            currentY += 40f

            // Observaciones
            paintText.color = android.graphics.Color.BLACK
            paintText.textSize = 10f
            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            canvas.drawText("Observaciones:", margin, currentY, paintText)
            currentY += 14f

            paintText.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            val obsText = if (audit.observaciones.isNotEmpty()) audit.observaciones else "Ninguna"
            
            val limit = 85
            if (obsText.length > limit) {
                val line1 = obsText.substring(0, limit)
                val line2 = obsText.substring(limit)
                canvas.drawText(line1, margin + 5f, currentY, paintText)
                currentY += 14f
                canvas.drawText(line2, margin + 5f, currentY, paintText)
            } else {
                canvas.drawText(obsText, margin + 5f, currentY, paintText)
            }

            // Footer
            currentY = 800f
            paintText.textSize = 8f
            paintText.color = android.graphics.Color.GRAY
            paintText.textAlign = android.graphics.Paint.Align.CENTER
            canvas.drawText("Reporte generado automáticamente por la aplicación Auditorías Aplicaciones. CT&A 2026.", 595f / 2f, currentY, paintText)

            pdfDocument.finishPage(page)

            val fileName = if (audit.lote.isNotEmpty()) {
                "Reporte_Calibracion_${audit.lote}_${System.currentTimeMillis()}.pdf"
            } else {
                "Reporte_Calibracion_${System.currentTimeMillis()}.pdf"
            }
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
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
                    pdfDocument.writeTo(outputStream)
                }
                pdfDocument.close()
                Toast.makeText(context, "PDF guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error: No se pudo crear el PDF en Descargas.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generando PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
