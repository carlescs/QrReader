package cat.company.qrreader.features.tags.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.company.qrreader.R
import androidx.compose.ui.window.Dialog
import cat.company.qrreader.utils.Utils
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor

/**
 * Dialog for picking a color
 */
@Composable
fun ColorPickerDialog(colorDialogVisible: MutableState<Boolean>, color:String, selectColor:(color:String)->Unit) {
    val selectedColor=remember{ mutableStateOf(HsvColor.from(
        Utils.parseColor(color) ?: Color.White
    ))}
    Dialog(onDismissRequest = { colorDialogVisible.value = false }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 10.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.choose_color),
                        fontSize = 24.sp,
                        modifier = Modifier.padding(PaddingValues(bottom = 16.dp))
                    )
                    ClassicColorPicker(
                        modifier =Modifier.height(200.dp).fillMaxWidth(),
                        color = selectedColor.value
                    ) {
                        selectedColor.value=it
                    }
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButton(onClick = {
                            colorDialogVisible.value = false
                        }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                        TextButton(onClick = {
                            selectColor(Utils.colorToString(selectedColor.value.toColor()))
                            colorDialogVisible.value = false
                        },) {
                            Text(text = stringResource(R.string.select))
                        }
                    }
                }
            }
        }
    }
}

