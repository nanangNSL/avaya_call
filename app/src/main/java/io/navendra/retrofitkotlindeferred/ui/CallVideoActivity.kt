package io.navendra.retrofitkotlindeferred.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.avaya.ocs.Services.Device.Video.Enums.CallQuality
import com.avaya.ocs.Services.Work.Enums.DTMFTone
import com.avaya.ocs.Services.Work.Interactions.AbstractInteraction
import com.avaya.ocs.Services.Work.Interactions.AudioInteraction
import com.avaya.ocs.Services.Work.Interactions.Interaction
import com.avaya.ocs.Services.Work.Interactions.Listeners.OnAudioDeviceChangeListener
import io.navendra.retrofitkotlindeferred.R

class CallVideoActivity : BaseCall() {
    override fun view(): Int {
        TODO("Not yet implemented")
    }

    override fun getInteraction(): AbstractInteraction? {
        TODO("Not yet implemented")
    }

    override fun getInteractionType(): Int {
        TODO("Not yet implemented")
    }

    override fun updateCallState() {
        TODO("Not yet implemented")
    }

    override fun sendDtmf(tone: DTMFTone, mAudioInteraction: AudioInteraction?) {
        TODO("Not yet implemented")
    }

    override fun configureAudioMuteStatus(state: Boolean) {
        TODO("Not yet implemented")
    }

    override fun hangup() {
        TODO("Not yet implemented")
    }

    override fun attachEventHandlers() {
        TODO("Not yet implemented")
    }

    override fun registerListener() {
        TODO("Not yet implemented")
    }

    override fun createInteraction(listener: OnAudioDeviceChangeListener?): Interaction? {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_video)
    }

    override fun getInteractionTimeElapsed(): Long {
        TODO("Not yet implemented")
    }

    override fun getElementReferences() {
        TODO("Not yet implemented")
    }

    override fun onInteractionAudioMuteStatusChanged(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onInteractionQualityChanged(p0: CallQuality?) {
        TODO("Not yet implemented")
    }
}