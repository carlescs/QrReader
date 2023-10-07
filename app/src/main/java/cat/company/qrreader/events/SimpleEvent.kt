package cat.company.qrreader.events

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SimpleEvent(val message: String)

class SimpleEventHandler {
    fun subscribeSimpleEvent(lifecycleOwner: LifecycleOwner, handler: (event: SimpleEvent) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            EventBus.subscribe<SimpleEvent> {
                handler(it)
            }
        }
    }
}