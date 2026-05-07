package com.maaser.app.ui.addincome

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeSheet(onDismiss: () -> Unit, viewModel: AddIncomeViewModel = hiltViewModel()) {
    var amount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val date = System.currentTimeMillis()
    val isSaving by viewModel.isSaving.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.add_income_title), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.field_amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text(stringResource(R.string.field_source)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.field_note)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(
                onClick = { val amountDouble = amount.toDoubleOrNull() ?: return@Button; viewModel.save(amountDouble, source.takeIf { it.isNotBlank() }, note.takeIf { it.isNotBlank() }, date, onDismiss) },
                enabled = !isSaving && amount.toDoubleOrNull() != null, modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_save)) }
        }
    }
}
