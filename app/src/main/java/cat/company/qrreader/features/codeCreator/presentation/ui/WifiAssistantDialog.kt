package cat.company.qrreader.features.codeCreator.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cat.company.qrreader.R
import cat.company.qrreader.utils.formatWifiQrText

/**
 * WiFi security types for QR code generation.
 *
 * @property qrValue The value used in the WIFI: QR code format string.
 * @property labelRes String resource ID for the display label.
 */
internal enum class WifiSecurityType(val qrValue: String, val labelRes: Int) {
    WPA("WPA", R.string.wifi_security_wpa),
    WEP("WEP", R.string.wifi_security_wep),
    OPEN("nopass", R.string.wifi_security_open)
}

/**
 * Dialog that assists the user in creating a WiFi QR code.
 *
 * Provides structured input fields for the network name (SSID), security type,
 * and password, then generates the properly formatted `WIFI:` text string.
 *
 * @param onDismiss Called when the dialog is dismissed without generating.
 * @param onGenerate Called with the generated WiFi QR code text when the user confirms.
 */
@Composable
fun WifiAssistantDialog(
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit
) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf(WifiSecurityType.WPA) }
    var securityDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.wifi_qr_assistant),
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        value = ssid,
                        onValueChange = { ssid = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.wifi_ssid_label)) }
                    )
                    ExposedDropdownMenuBox(
                        expanded = securityDropdownExpanded,
                        onExpandedChange = { securityDropdownExpanded = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            readOnly = true,
                            value = stringResource(securityType.labelRes),
                            onValueChange = {},
                            label = { Text(stringResource(R.string.wifi_security_label)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = securityDropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = securityDropdownExpanded,
                            onDismissRequest = { securityDropdownExpanded = false }
                        ) {
                            WifiSecurityType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(type.labelRes)) },
                                    onClick = {
                                        securityType = type
                                        securityDropdownExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                    if (securityType != WifiSecurityType.OPEN) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            value = password,
                            onValueChange = { password = it },
                            singleLine = true,
                            label = { Text(stringResource(R.string.wifi_network_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(
                            onClick = {
                                onGenerate(
                                    formatWifiQrText(
                                        ssid = ssid,
                                        password = password,
                                        securityType = securityType.qrValue
                                    )
                                )
                            },
                            enabled = ssid.isNotBlank() &&
                                (securityType == WifiSecurityType.OPEN || password.isNotBlank())
                        ) {
                            Text(stringResource(R.string.wifi_generate))
                        }
                    }
                }
            }
        }
    }
}
