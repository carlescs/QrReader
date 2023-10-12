package cat.company.qrreader.events

class SharedEvents {
    companion object SharedEvents {
        var onShareClick: (() -> Unit)? = null
        var onShareIsDisabled: ((disabled: Boolean) -> Unit)? = null
    }
}
