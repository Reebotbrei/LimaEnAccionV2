package com.limaenaccion.feature_alerts.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limaenaccion.core.ui.components.UserAvatar
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncidentCard(
    incident: Incident,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasWitnessed: Boolean = false,
    onToggleWitness: (() -> Unit)? = null,
    isReportedAsFalse: Boolean = false,
    onReportFalsehood: (() -> Unit)? = null
) {
    val decodedBitmap = remember(incident.photoBase64) {
        incident.photoBase64?.let { base64Str ->
            try {
                val clean = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        }
    }

    val effectiveWitnesses = incident.witnessCount + if (hasWitnessed) 1 else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // 1. Avatar de usuario (iniciales o escudo oficial)
            UserAvatar(
                name = incident.reportedBy,
                isOfficial = incident.isOfficial,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Columna de contenido
            Column(modifier = Modifier.weight(1f)) {
                // Fila 1: Nombre, insignia oficial, @handle y tiempo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = incident.reportedBy,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (incident.isOfficial) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Cuenta Oficial Verificada",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = incident.handle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = " · ${incident.reportedAt}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Fila 2: Pill de Categoría + Distancia + BADGE DE ESTADO DEL INCIDENTE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Categoría
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = incident.type.color().copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = incident.type.label,
                            color = incident.type.color(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    // Distancia
                    Text(
                        text = "A ${incident.distanceMeters} m",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Badge de Estado del Incidente (🟡 Reportado | 🔵 En atención | 🟢 Resuelto)
                    val (statusBg, statusFg) = when (incident.incidentStatus) {
                        IncidentStatus.REPORTADO -> Color(0xFFD97706).copy(alpha = 0.16f) to Color(0xFFD97706)
                        IncidentStatus.EN_ATENCION -> Color(0xFF2563EB).copy(alpha = 0.16f) to Color(0xFF2563EB)
                        IncidentStatus.RESUELTO -> Color(0xFF16A34A).copy(alpha = 0.16f) to Color(0xFF16A34A)
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusBg
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${incident.incidentStatus.iconEmoji} ${incident.incidentStatus.label}",
                                color = statusFg,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila 3: Descripción completa del reporte cívico
                Text(
                    text = incident.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fila 4: Ubicación con icono de pin
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = incident.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Fila 5: Foto adjunta (si existe)
                if (decodedBitmap != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = "Foto de evidencia adjunta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila 6: Respaldos comunitarios / Validación oficial
                if (incident.falseReportsCount >= 3 || isReportedAsFalse) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bajo revisión · Reportado como posible hecho falso",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (incident.isVerifiedByAuthority) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Validado por Serenazgo SJL · Autoridad local",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1E40AF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (effectiveWitnesses > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(Icons.Filled.People, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$effectiveWitnesses vecinos presenciaron este hecho",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Fila 7: ACCIONES CÍVICAS (Diseño 100% Responsivo y Adaptativo a Temas con FlowRow)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Acción 1: "Lo presencié" (Interactivo)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (hasWitnessed) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (hasWitnessed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.clickable { onToggleWitness?.invoke() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = "Confirmar como testigo presencial",
                                tint = if (hasWitnessed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasWitnessed) "Presenciado ($effectiveWitnesses)" else if (effectiveWitnesses > 0) "Lo presencié ($effectiveWitnesses)" else "Lo presencié",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (hasWitnessed) FontWeight.Bold else FontWeight.Medium,
                                color = if (hasWitnessed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Acción 2: "Aportar información" (Abre el detalle para sumar testimonios)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable(onClick = onClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                contentDescription = "Aportar testimonios o información",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aportar información",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Acción 3: "Reportar falsedad" (Completamente adaptativo a temas y responsivo)
                    val reportContainerColor = if (isReportedAsFalse) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    }
                    val reportBorder = if (isReportedAsFalse) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    val reportContentColor = if (isReportedAsFalse) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = reportContainerColor,
                        border = reportBorder,
                        modifier = Modifier.clickable { onReportFalsehood?.invoke() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Flag,
                                contentDescription = "Reportar contenido falso o spam",
                                tint = reportContentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isReportedAsFalse) "Reportado" else "Reportar",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isReportedAsFalse) FontWeight.Bold else FontWeight.Medium,
                                color = reportContentColor
                            )
                        }
                    }
                }
            }
        }

        // Divisor suave entre tweets / alertas cívicas
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 0.6.dp
        )
    }
}
