package com.example.auditoriaaplicaciones.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.auditoriaaplicaciones.ui.theme.AuditoriaAplicacionesTheme

@Composable
fun InitialScreen(
    modifier: Modifier = Modifier,
    onAuditoriaClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onDescargarExcelClick: () -> Unit
) {
    Column(
        modifier = modifier
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
