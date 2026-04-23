package com.example.auditoriaaplicaciones.network

import android.content.Context
import android.util.Log
import com.example.auditoriaaplicaciones.ui.AuditoriaInfo
import com.example.auditoriaaplicaciones.ui.StorageManager
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object SyncManager {
    private val client = OkHttpClient()
    private val gson = Gson()
    private const val API_URL = "https://interno.control.agricolaguapa.com/consultor/api/guardar_aplicacion"

    fun syncAudit(context: Context, audit: AuditoriaInfo, onResult: (Boolean, String) -> Unit) {
        if (audit.isSynced) {
            onResult(true, "Ya sincronizado")
            return
        }

        val jsonMap = mutableMapOf<String, Any?>()
        
        // Basic Info
        jsonMap["evaluador"] = audit.evaluador
        jsonMap["fecha"] = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(audit.fecha))
        jsonMap["hora"] = if (audit.hora.length == 5) "${audit.hora}:00" else audit.hora
        jsonMap["finca"] = audit.finca
        jsonMap["lote"] = audit.lote
        jsonMap["operador"] = audit.operador
        jsonMap["ubicacion"] = audit.ubicacion

        // Tractor & Equipment
        jsonMap["tractor"] = audit.codTractor
        jsonMap["spray"] = audit.codImplemento
        jsonMap["potencia_tractor"] = audit.potenciaTractor.toIntOrNull() ?: 0
        jsonMap["potencia_tdf"] = audit.potenciaTdf.toIntOrNull() ?: 0

        // Calibration
        jsonMap["formula"] = audit.formula
        jsonMap["presion_aplicacion"] = audit.presion.toFloatOrNull() ?: 0f
        jsonMap["volumen_aplicar"] = audit.volumen.toFloatOrNull() ?: 0f

        // Movement
        jsonMap["velocidad_kmh"] = audit.velocidadKmh
        jsonMap["metros_desplazamiento"] = audit.distanciaMetros
        
        // Send raw seconds for the numeric field
        jsonMap["tiempo_desplazamiento"] = audit.tiempoDesplazamientoSegundos.toDouble()

        // General Questions
        jsonMap["boquillas_tapadas"] = audit.boquillasTapadas ?: false
        jsonMap["cuantas_tapadas"] = audit.boquillasTapadasNum.toIntOrNull() ?: 0
        jsonMap["presencia_personal"] = audit.presenciaPersonal ?: false
        jsonMap["estado_via"] = audit.estadoVia
        jsonMap["altura_uniforme"] = audit.alturaUniforme ?: false
        jsonMap["papel_hidrosensible"] = audit.papelHidrosensible
        jsonMap["velocidad_optima"] = audit.velocidadOptima
        jsonMap["observaciones"] = audit.observaciones

        // Nozzles (B1, B2 from Left; B3, B4 from Right)
        val left = audit.nozzlesIzquierdo
        val right = audit.nozzlesDerecho

        // B1
        jsonMap["b1_id"] = left.getOrNull(0)?.id ?: 0
        jsonMap["b1_pres"] = left.getOrNull(0)?.presion?.toFloatOrNull() ?: 0f
        jsonMap["b1_vol"] = left.getOrNull(0)?.volumen?.toFloatOrNull() ?: 0f
        
        // B2
        jsonMap["b2_id"] = left.getOrNull(1)?.id ?: 0
        jsonMap["b2_pres"] = left.getOrNull(1)?.presion?.toFloatOrNull() ?: 0f
        jsonMap["b2_vol"] = left.getOrNull(1)?.volumen?.toFloatOrNull() ?: 0f

        // B3
        jsonMap["b3_id"] = right.getOrNull(0)?.id ?: 0
        jsonMap["b3_pres"] = right.getOrNull(0)?.presion?.toFloatOrNull() ?: 0f
        jsonMap["b3_vol"] = right.getOrNull(0)?.volumen?.toFloatOrNull() ?: 0f

        // B4
        jsonMap["b4_id"] = right.getOrNull(1)?.id ?: 0
        jsonMap["b4_pres"] = right.getOrNull(1)?.presion?.toFloatOrNull() ?: 0f
        jsonMap["b4_vol"] = right.getOrNull(1)?.volumen?.toFloatOrNull() ?: 0f

        // Arms
        jsonMap["cantidad_boquillas_brazo_izq"] = audit.cantidadBoquillasIzquierdas.toIntOrNull() ?: 0
        jsonMap["longitud_brazo_izq"] = audit.longitudBrazoIzquierdo.toFloatOrNull() ?: 0f
        jsonMap["cantidad_boquillas_brazo_der"] = audit.cantidadBoquillasDerechas.toIntOrNull() ?: 0
        jsonMap["longitud_brazo_der"] = audit.longitudBrazoDerecho.toFloatOrNull() ?: 0f

        // Paper results
        jsonMap["gotas_1_cm2"] = audit.papelGotas1cm.toIntOrNull() ?: 0
        jsonMap["gotas_1_4_cm2"] = audit.tamanoGotas // Used for size as requested

        val requestBody = gson.toJson(jsonMap).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, e.message ?: "Error de red")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful) {
                    // Update state to Synced in storage
                    val updatedAudit = audit.copy(isSynced = true)
                    StorageManager.saveAuditoria(context, updatedAudit)
                    onResult(true, "Sincronizado correctamente")
                } else {
                    onResult(false, "Error servidor: ${response.code}\n$body")
                }
            }
        })
    }
}
