package com.limaenaccion.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Modal BottomSheet oficial de Términos y Condiciones y Política de Privacidad de Lima en Acción.
 * Diseñado bajo directrices de Material 3 para ser reutilizado tanto en el registro de usuarios
 * como en la sección de configuración y soporte legal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsSheet(
    onDismissRequest: () -> Unit,
    onAcceptClicked: (() -> Unit)? = null,
    acceptButtonText: String = "Entendido y Conforme"
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
        ) {
            // Cabecera Principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Términos y Condiciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lima en Acción · Red Cívica y Comunitaria",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badge de jurisdicción legal
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = "🇵🇪 Lima Metropolitana · Ley N° 29733 de Protección de Datos Personales",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Contenido con Scroll de Términos
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TermsSection(
                    icon = Icons.Filled.Shield,
                    title = "1. Naturaleza Cívica y Deslinde de Emergencia",
                    content = "Lima en Acción es una plataforma comunitaria diseñada para fomentar la vigilancia ciudadana y la alerta temprana entre vecinos de Lima Metropolitana.\n\n" +
                            "⚠️ IMPORTANTE: Esta aplicación NO sustituye a los servicios públicos oficiales de respuesta del Estado. Ante emergencias graves con riesgo de vida, debes llamar inmediatamente a la Central 105 de la Policía Nacional del Perú, al 116 de los Bomberos o al 106 del SAMU.",
                    cardColor = Color(0xFFFEF3C7),
                    borderColor = Color(0xFFF59E0B),
                    textColor = Color(0xFF92400E)
                )

                TermsSection(
                    icon = Icons.Filled.NotificationsActive,
                    title = "2. Uso Responsable del Botón SOS y Reportes",
                    content = "El usuario se compromete a utilizar el botón SOS y el formulario de alertas de manera verídica, ética y responsable. Queda terminantemente prohibido:\n" +
                            "• Emitir reportes falsos o engañosos.\n" +
                            "• Activar alertas de pánico a modo de prueba no autorizada o juego.\n" +
                            "• Publicar acusaciones sin fundamento contra personas particulares.\n\n" +
                            "La comunidad cuenta con herramientas de moderación vecinal. Cuentas con reportes catalogados reiteradamente como falsos serán suspendidas de manera definitiva.",
                    cardColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                )

                TermsSection(
                    icon = Icons.Filled.LocationOn,
                    title = "3. Geolocalización y Privacidad (Ley N° 29733)",
                    content = "En estricto cumplimiento de la Ley N° 29733 (Ley de Protección de Datos Personales del Perú):\n" +
                            "• Tu ubicación GPS solo se consulta y transmite de forma activa cuando creas un reporte ciudadano o pulsas el botón SOS para enviar tu posición a tus contactos de auxilio.\n" +
                            "• Garantía de Anonimato: Al publicar un incidente, puedes activar el switch 'Reporte Anónimo' para que tu nombre y apellido no sean visibles ante la red de vecinos.\n" +
                            "• Tienes derecho en cualquier momento a solicitar la eliminación integral de tu cuenta y datos desde el menú de Ajustes.",
                    cardColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                )

                TermsSection(
                    icon = Icons.Filled.CameraAlt,
                    title = "4. Evidencia Fotográfica y Respeto a Menores",
                    content = "Las fotografías adjuntas a los incidentes deben servir exclusivamente como evidencia cívica del hecho (daños materiales, focos de riesgo o contexto del suceso).\n\n" +
                            "Está prohibido subir imágenes que atenten contra la intimidad, que muestren menores de edad o que contengan contenido explícito o denigrante. Todo material contrario a estas normas será depurado.",
                    cardColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                )

                TermsSection(
                    icon = Icons.Filled.WifiOff,
                    title = "5. Disponibilidad y Conectividad",
                    content = "La transmisión en tiempo real de los reportes y la difusión de mensajes SMS de auxilio dependen de la cobertura del operador de telefonía móvil y de la conectividad a internet de tu dispositivo. Lima en Acción no se responsabiliza por fallas ajenas a la aplicación derivadas de la red de telecomunicaciones.",
                    cardColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón de Aceptación / Confirmación
            PrimaryButton(
                text = acceptButtonText,
                onClick = {
                    onAcceptClicked?.invoke()
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            )
        }
    }
}

@Composable
private fun TermsSection(
    icon: ImageVector,
    title: String,
    content: String,
    cardColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}
