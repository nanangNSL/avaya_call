package io.navendra.retrofitkotlindeferred.ui

import android.content.ContentValues
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.avaya.ocs.Services.Device.Video.Enums.CallQuality
import com.avaya.ocs.Services.Work.Enums.AudioDeviceType
import com.avaya.ocs.Services.Work.Enums.DTMFTone
import com.avaya.ocs.Services.Work.Interactions.AbstractInteraction
import com.avaya.ocs.Services.Work.Interactions.AudioInteraction
import com.avaya.ocs.Services.Work.Interactions.Interaction
import com.avaya.ocs.Services.Work.Interactions.Listeners.OnAudioDeviceChangeListener
import io.navendra.retrofitkotlindeferred.R
import io.navendra.retrofitkotlindeferred.utils.Constants
import io.navendra.retrofitkotlindeferred.utils.InteractionService
import io.navendra.retrofitkotlindeferred.utils.Logger
import io.navendra.retrofitkotlindeferred.utils.ViewHandler

class CallActivity: BaseCall(), ViewHandler {
    //---------------------------------Dev AWG Start---------------------------------
    private val mLogger = Logger.getLogger(ContentValues.TAG)

    private var mAudioInteraction: AudioInteraction? = null

    override fun view(): Int {
        return R.layout.activity_call
    }

    override fun getInteractionType(): Int {
        return INTERACTION_AUDIO
    }

    override fun getInteraction(): AbstractInteraction? {
        return mAudioInteraction
    }


    override fun registerListener() {
        mAudioInteraction!!.registerListener(this)
    }

    override fun createInteraction(listener: OnAudioDeviceChangeListener?): Interaction? {
        numberToDial = intent.getStringExtra(Constants.KEY_NUMBER_TO_DIAL)
        uui = intent.getStringExtra(Constants.KEY_CONTEXT).toString()
        mAudioInteraction = InteractionService.getInstance().createAudioInteraction(listener,numberToDial,uui)
        return mAudioInteraction
    }

    override fun updateCallState() {
        try {
            if (mAudioInteraction != null) {
                val state = mAudioInteraction!!.interactionState
                updateCallState(state.toString())
            }
        } catch (e: Exception) {
            mLogger.e("Exception getting interaction state, " + e.message, e)
        }
    }
    override fun sendDtmf(tone: DTMFTone, mAudioInteraction2:AudioInteraction?) {
        if(mAudioInteraction==null)
        {
            mAudioInteraction2!!.sendDtmf(tone)
        }
        if (mAudioInteraction != null) {
            mAudioInteraction!!.sendDtmf(tone)
        }

    }

    override fun hangup() {
        if (canHungup()) {
            mLogger.d("hanging up")
            setCanHungup(false)
            try {
                dismissDtmf()
                if (mAudioInteraction == null){
                    if(InteractionService.getInstance().audioInteraction != null)
                        mAudioInteraction = InteractionService.getInstance().audioInteraction
                }
                if (mAudioInteraction != null) {
                    mAudioInteraction!!.end()
                    mAudioInteraction!!.discard()
                    mAudioInteraction = null
                } else {
                    mLogger.w("Audio Interaction is null")
                }
            } catch (e: Exception) {
                mLogger.e("Exception in hangup " + e.message, e)
                //                showToast(this, "Incomplete call cleanup sequence", Toast.LENGTH_LONG);
            }
        }
    }


    override fun finish() {
        try {
            if (mAudioInteraction != null) {
                mAudioInteraction!!.unregisterListener(this)
                // mAudioInteraction!!.unregisterConnectionListener(this)
            } else {
                mLogger.w("Audio Interaction is null - cannot unregister listeners")
            }
        } catch (e: Exception) {
            mLogger.e("Exception in finish " + e.message, e)
            //            showToast(this, "Incomplete listener cleanup sequence", Toast.LENGTH_LONG);
        }
        super.finish()
    }

    override fun configureAudioMuteStatus(state: Boolean) {
        mLogger.d("entering configureAudioMuteStatus - state: $state")
        val btn_mute=findViewById<TextView>(R.id.btn_mute)
        if(mAudioInteraction==null)
        {
            val mAudioInteraction = InteractionService.getInstance().audioInteraction
            mAudioInteraction!!.muteAudio(state)
        }
        if (mAudioInteraction != null) {
            mAudioInteraction!!.muteAudio(state)
        }
        localStorageHelper?.saveDataStorage(Constants.CURRENT_MUTE_STATUS,state.toString())

        if(localStorageHelper?.loadDataStorage(Constants.CURRENT_MUTE_STATUS)=="true")
        {
            btn_mute.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_mic_off_24),
                null,
                null
            )
            btn_mute.text="Muted"
        }
        else
        {
            btn_mute.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_mic_24),
                null,
                null
            )
            btn_mute.text="Unmuted"
        }
    }

    override fun onInteractionAudioMuteStatusChanged(state: Boolean) {
        mLogger.d("AudioInteraction:onInteractionAudioMuteStatusChanged, state - $state")
        val mMicrophoneButton = findViewById<TextView>(R.id.btn_mute)
//        if (state) {
//            mMicrophoneButton.setCompoundDrawablesWithIntrinsicBounds(
//                null,
//                getDrawable(R.drawable.ic_speaker_active),
//                null,
//                null
//            )
//
//        } else {
//            mMicrophoneButton.setCompoundDrawablesWithIntrinsicBounds(
//                null,
//                getDrawable(R.drawable.ic_speaker_default),
//                null,
//                null
//            )
//        }
    }

    override fun onInteractionQualityChanged(callQuality: CallQuality?) {
        this@CallActivity.runOnUiThread(Runnable {
            //                renderCallQuality(callQuality);
        })
    }
    // End of interaction callbacks

    // End of interaction callbacks
    override fun getElementReferences() {
        val btn_mute = findViewById<TextView>(R.id.btn_mute)
        val btn_end_call = findViewById<AppCompatImageView>(R.id.btn_end_call)
//        audioDeviceButton = findViewById(R.id.audioDeviceButton);
//        textViewCallQuality = findViewById(R.id.textViewCallQuality);
//        ivCallQualityRating = findViewById(R.id.ivCallQualityRating);
    }

    override fun attachEventHandlers() {
        val mMicrophoneButton = findViewById<TextView>(R.id.btn_mute)

//        mMicrophoneButton.setOnClickListener { view: View? ->
//            mLogger.d("Mute pressed")
//            Log.d("mute","Kepencet mute diluar")
//            if (mAudioInteraction != null) {
//                val isMuted = mAudioInteraction!!.isAudioMuted
//                mLogger.i("Is audio muted: $isMuted")
//                configureAudioMuteStatus(!isMuted)
//                Log.d("mute","Kepencet mute")
//            } else {
//                mLogger.w("Audio Interaction is null")
//            }
//        }
//        val speaker_button=findViewById<TextView>(R.id.btn_speaker)
//        mMicrophoneButton.setOnClickListener { view: View? ->
//        }
    }

    override fun getInteractionTimeElapsed(): Long {
        return mAudioInteraction!!.interactionTimeElapsed
    }


    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
    }

    override fun onAudioDeviceListChanged(list: List<AudioDeviceType?>?) {}



//---------------------------------Dev AWG End---------------------------------
}