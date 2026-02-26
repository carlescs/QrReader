package cat.company.qrreader.features.codeCreator.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cat.company.qrreader.R
import cat.company.qrreader.utils.formatContactQrText

/**
 * Dialog that assists the user in creating a Contact QR code.
 *
 * Provides structured input fields for the full name, phone number, email address,
 * and organisation, then generates a properly formatted vCard 3.0 text string.
 *
 * @param onDismiss Called when the dialog is dismissed without generating.
 * @param onGenerate Called with the generated vCard text when the user confirms.
 */
@Composable
fun ContactAssistantDialog(
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.contact_qr_assistant),
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.contact_full_name)) }
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        value = phone,
                        onValueChange = { phone = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.contact_phone)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.contact_email)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        value = organization,
                        onValueChange = { organization = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.contact_organization)) }
                    )
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(
                            onClick = {
                                onGenerate(
                                    formatContactQrText(
                                        name = name,
                                        phone = phone.takeIf { it.isNotBlank() },
                                        email = email.takeIf { it.isNotBlank() },
                                        organization = organization.takeIf { it.isNotBlank() }
                                    )
                                )
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text(stringResource(R.string.contact_generate))
                        }
                    }
                }
            }
        }
    }
}
