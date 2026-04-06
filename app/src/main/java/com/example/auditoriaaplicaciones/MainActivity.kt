package com.example.auditoriaaplicaciones

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.auditoriaaplicaciones.ui.InitialScreen
import com.example.auditoriaaplicaciones.ui.theme.AuditoriaAplicacionesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuditoriaAplicacionesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InitialScreen(
                        modifier = Modifier.padding(innerPadding),
                        onAuditoriaClick = { 
                            Toast.makeText(this, "Auditoría Aplicaciones", Toast.LENGTH_SHORT).show()
                        },
                        onHistorialClick = { 
                            Toast.makeText(this, "Ver Historial", Toast.LENGTH_SHORT).show()
                        },
                        onDescargarExcelClick = { 
                            Toast.makeText(this, "Descargar Excel", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}