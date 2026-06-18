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
        val tipoAud = audit.tipoAuditoria.lowercase(Locale.getDefault())

        if (tipoAud == "mezclas") {
            jsonMap["tipo_auditoria"] = "mezclas"

            // Basic Info
            jsonMap["evaluador"] = audit.evaluador
            jsonMap["fecha"] = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(audit.fecha))
            jsonMap["hora"] = if (audit.hora.length == 5) "${audit.hora}:00" else audit.hora
            jsonMap["finca"] = audit.finca
            jsonMap["lote"] = audit.lote
            jsonMap["mezclador"] = audit.mezclador
            jsonMap["formula"] = audit.formulaMezclar

            // Water Metrics
            jsonMap["ph_agua"] = audit.phAgua
            jsonMap["dureza_agua"] = audit.durezaAgua
            jsonMap["ce_agua"] = audit.ceAgua
            jsonMap["ph_final"] = audit.phFinal
            jsonMap["ce_final"] = audit.ceFinal

            // Checklist & Observations
            jsonMap["incompatibilidad"] = audit.incompatibilidad
            jsonMap["orden_mezclado"] = audit.ordenMezclado
            jsonMap["obs_orden_mezclado"] = audit.obsOrdenMezclado
            jsonMap["usa_epp"] = audit.usaEpp
            jsonMap["obs_epp"] = audit.obsEpp
            jsonMap["tanque_limpio"] = audit.tanqueLimpio
            jsonMap["obs_tanque_limpio"] = audit.obsTanqueLimpio
            jsonMap["observaciones"] = audit.observaciones
            jsonMap["agua_percent"] = audit.getMezclasAguaPercent()
            jsonMap["insumos_percent"] = audit.getMezclasInsumosPercent()
            jsonMap["datos_generales_percent"] = audit.getMezclasDatosGeneralesPercent()

            // Products List
            val productsList = audit.productosEvaluados.map { prod ->
                mapOf(
                    "producto" to prod.producto,
                    "cumple" to prod.cumple,
                    "reemplazo" to prod.reemplazo,
                    "cantidad" to prod.cantidad,
                    "unidad" to prod.unidad,
                    "orden" to prod.orden
                )
            }
            jsonMap["productos_evaluados"] = productsList
        } else if (tipoAud.contains("calib")) {
            jsonMap["tipo_auditoria"] = "calibracion"

            // Basic Info
            jsonMap["evaluador"] = audit.evaluador
            jsonMap["fecha"] = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(audit.fecha))
            jsonMap["hora"] = if (audit.hora.length == 5) "${audit.hora}:00" else audit.hora
            jsonMap["finca"] = audit.finca
            jsonMap["lote"] = audit.lote
            jsonMap["operario"] = audit.operarioCalib
            jsonMap["tractor"] = audit.tractorCalib
            jsonMap["volumen_tanque"] = audit.volumenTanque
            jsonMap["implemento"] = audit.implementoCalib
            jsonMap["num_boquillas"] = audit.numBoquillas
            jsonMap["tipo_boquillas"] = audit.tipoBoquillas
            jsonMap["referencia_boquillas"] = audit.referenciaBoquillas
            jsonMap["tiempo_descarga"] = audit.tiempoCalib
            jsonMap["descarga"] = audit.descargaCalib

            // Calculated Metrics
            val q = if (audit.tiempoCalib > 0.0) (audit.descargaCalib / audit.tiempoCalib) * 0.06 else 0.0
            val qTot = audit.numBoquillas * q
            jsonMap["caudal_boquilla"] = q
            jsonMap["caudal_total"] = qTot

            val validRuns = audit.calculosRecorrido.filter { it.distancia > 0.0 && it.tiempo > 0.0 }
            val avgDist = if (validRuns.isNotEmpty()) validRuns.map { it.distancia }.average() else 0.0
            val avgTime = if (validRuns.isNotEmpty()) validRuns.map { it.tiempo }.average() else 0.0
            val avgVol = if (validRuns.isNotEmpty()) validRuns.map { it.volumenAplicado }.average() else 0.0
            val avgVelKmh = if (avgTime > 0.0) (avgDist / avgTime) * 3.6 else 0.0

            val distBoquillas = audit.distBoquillasCalib.toDoubleOrNull() ?: when (audit.implementoCalib) {
                "IA - 14", "IA - 28", "IA - 53", "IA - 81" -> 0.4
                "IA - 64", "IA - 67", "IA - 82" -> 0.3
                else -> 0.5
            }
            val w = audit.numBoquillas * distBoquillas
            val areaRecHa = (w * avgDist) / 10000.0
            val dosReal = if (areaRecHa > 0.0) avgVol / areaRecHa else 0.0
            val areaTotTanq = if (dosReal > 0.0) audit.volumenTanque / dosReal else 0.0
            val velReq = q * 0.6

            jsonMap["promedio_distancia"] = avgDist
            jsonMap["promedio_tiempo"] = avgTime
            jsonMap["promedio_velocidad_kmh"] = avgVelKmh
            jsonMap["area_recorrida_ha"] = areaRecHa
            jsonMap["area_total_tanque_ha"] = areaTotTanq
            jsonMap["velocidad_requerida_kmh"] = velReq
            jsonMap["observaciones"] = audit.observaciones
            jsonMap["dist_boquillas"] = audit.distBoquillasCalib
            jsonMap["longitud_brazo"] = audit.longitudBrazoCalib

            // Runs List
            val runsList = audit.calculosRecorrido.map { run ->
                mapOf(
                    "distancia" to run.distancia,
                    "tiempo" to run.tiempo,
                    "volumen_aplicado" to run.volumenAplicado
                )
            }
            jsonMap["calculos_recorrido"] = runsList
        } else {
            jsonMap["tipo_auditoria"] = "sprayboom"

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

            // 4 Puntajes
            jsonMap["volumen_percent"] = audit.getVolumenPercent()
            jsonMap["altura_percent"] = audit.getAlturaPercent()
            jsonMap["boquillas_percent"] = audit.getBoquillasTapadasPercent()
            jsonMap["uniformidad_percent"] = audit.getUniformidadPercent()

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
            jsonMap["gotas_1_4_cm2"] = audit.tamanoGotas
        }

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
