package com.limaenaccion.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Dropdown genérico reutilizable. Lo usan RegisterStep3Screen (distrito,
 * vecindario), EditPersonalDataScreen (género) y UpdateAddressScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    selectedValue: String?,
    placeholder: String,
    options: List<String>,
    isError: Boolean = false,
    errorMessage: String? = null,
    icon: ImageVector? = null,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedValue ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder) },
            leadingIcon = icon?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = isError,
            supportingText = errorMessage?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
}