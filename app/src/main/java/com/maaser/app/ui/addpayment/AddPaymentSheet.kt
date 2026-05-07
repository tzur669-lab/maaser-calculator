package com.maaser.app.ui.addpayment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaser.app.R
import com.maaser.app.data.model.PaymentDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentSheet(onDismiss: () -> Unit, viewModel: AddPaymentViewModel = hiltViewModel()) {
    var amount by remember { mutableStateOf("") }
    var selectedDestination by remember { mutableStateOf<PaymentDestination?>(null) }
    var freeText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val date = System.currentTimeMillis()
    val destinations by viewModel.destinations.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.add_payment_title), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.field_payment_amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)
            ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                OutlinedTextField(
                    value = selectedDestination?.name ?: if (freeText.isNotBlank()) stringResource(R.string.destination_other) else "",
                    onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.field_destination)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    destinations.forEach { dest -> DropdownMenuItem(text = { Text(dest.name) }, onClick = { selectedDestination = dest; freeText = ""; dropdownExpanded = false }) }
                    DropdownMenuItem(text = { Text(stringResource(R.string.destination_other)) }, onClick = { selectedDestination = null; dropdownExpanded = false })
                }
            }
            if (selectedDestination == null) {
                OutlinedTextField(value = freeText, onValueChange = { freeText = it }, label = { Text(stringResource(R.string.field_destination_free_text)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.field_note)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(
                onClick = { val amountDouble = amount.toDoubleOrNull() ?: return@Button; viewModel.save(amountDouble, selectedDestination?.id, freeText.takeIf { it.isNotBlank() }, note.takeIf { it.isNotBlank() }, date, onDismiss) },
                enabled = !isSaving && amount.toDoubleOrNull() != null, modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_save)) }
        }
    }
}
