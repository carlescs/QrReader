package cat.company.qrreader.navigation

import androidx.annotation.StringRes
import cat.company.qrreader.R

sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: Int) {
    object Camera : Screen("camera", R.string.camera, R.drawable.ic_baseline_qr_code_scanner_24)
    object History : Screen("history", R.string.history,R.drawable.ic_baseline_history_24)
}

