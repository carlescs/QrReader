package cat.company.qrreader.events

class SharedEvents {
    companion object SharedEvents {
        var openSideBar: (() -> Unit)? = null

        var onShareClick: (() -> Unit)? = null
        var onShareIsDisabled: ((disabled: Boolean) -> Unit)? = null

        var onPrintClick: (() -> Unit)? = null
        var onPrintIsDisabled: ((disabled: Boolean) -> Unit)? = null
    }
}
