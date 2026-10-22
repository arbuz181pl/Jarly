package com.jarly

import android.app.Application
import com.jarly.data.Alerts
import com.jarly.data.JarlyStore
import com.jarly.ui.widget.JarlyWidget

class JarlyApp : Application() {

    lateinit var store: JarlyStore

    override fun onCreate() {
        super.onCreate()
        Alerts.createChannel(this)
        store = JarlyStore(this)
        store.onDataChanged = {
            Alerts.refresh(this)
            JarlyWidget.updateAll(this)
        }
        Alerts.refresh(this)
        JarlyWidget.updateAll(this)
    }
}
