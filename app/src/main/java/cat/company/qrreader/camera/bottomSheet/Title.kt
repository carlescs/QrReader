package cat.company.qrreader.camera.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Title(title:String){
    Text(text = title, fontSize = 25.sp)
    Spacer(modifier = Modifier.height(10.dp))
}