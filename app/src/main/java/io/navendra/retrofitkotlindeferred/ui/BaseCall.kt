package io.navendra.retrofitkotlindeferred.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import cio.navendra.retrofitkotlindeferred.utils.rx.AppSchedulerProvider
import cio.navendra.retrofitkotlindeferred.utils.rx.SchedulerProvider
import com.avaya.ocs.Exceptions.AuthorizationTokenException
import com.avaya.ocs.Services.Work.Enums.AudioDeviceError
import com.avaya.ocs.Services.Work.Enums.AudioDeviceType
import com.avaya.ocs.Services.Work.Enums.DTMFTone
import com.avaya.ocs.Services.Work.Enums.InteractionError
import com.avaya.ocs.Services.Work.Interactions.AbstractInteraction
import com.avaya.ocs.Services.Work.Interactions.AudioInteraction
import com.avaya.ocs.Services.Work.Interactions.Interaction
import com.avaya.ocs.Services.Work.Interactions.Listeners.AudioInteractionListener
import com.avaya.ocs.Services.Work.Interactions.Listeners.ConnectionListener
import com.avaya.ocs.Services.Work.Interactions.Listeners.OnAudioDeviceChangeListener
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.material.snackbar.Snackbar
import io.navendra.retrofitkotlindeferred.R
import io.navendra.retrofitkotlindeferred.utils.CallAgentService
import io.navendra.retrofitkotlindeferred.utils.Constants
import io.navendra.retrofitkotlindeferred.utils.ForegroundService
import io.navendra.retrofitkotlindeferred.utils.InteractionService
import io.navendra.retrofitkotlindeferred.utils.LocalStorageHelper
import io.navendra.retrofitkotlindeferred.utils.Logger
import io.navendra.retrofitkotlindeferred.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_call.call_elapsed_time
import kotlinx.android.synthetic.main.activity_call.dialpad_holder
import kotlinx.android.synthetic.main.activity_call.txtcallstate
import kotlinx.android.synthetic.main.dialpad_frame.view.dial_input
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_0
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_1
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_2
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_3
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_4
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_5
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_6
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_7
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_8
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_9
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_asterisk
import kotlinx.android.synthetic.main.dialpad_frame.view.dialpad_key_hashtag
import kotlinx.android.synthetic.main.dialpad_frame.view.secondary_erase_button
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseCall : AppCompatActivity() , AudioInteractionListener,
    /*ConnectionListener,*/ OnAudioDeviceChangeListener {
    //---------------------------------Dev AWG Start---------------------------------
    val INTERACTION_AUDIO = 1
    val INTERACTION_VIDEO = 2
    private var activePopupWindow: PopupWindow? = null

    private var errorState=""
    private var hasConnected = false
    private var isEndCallTriggered = false
    public var numberToDial: String? = ""
    var mCallDuration = 0L
    var callState = Constants.CALL_STATE_DIALING
    var callStateInt =0

    public var uui=""
    private var speakerEnabled: Boolean? = null
    private var screenLocked = false
    private var isActivityReady = false
    private var isWarningDismissed = true
    private var hasOnGoingToggleSpeakerRequest = false
    private var isServiceHasBeenCalled = false
    private var anyErrorMessages = ""
    private var actionStatus = true

    private lateinit var mPowerManager : PowerManager
    private lateinit var mWakeLock: PowerManager.WakeLock

    private var mDtmfPopupWindow: PopupWindow? = null
    private var dialText = ""
    private lateinit var layoutHandler : Handler




    private val mLogger = Logger.getLogger(ContentValues.TAG)

    // reference common application elements

    // reference common application elements
    // reference common UI elements
    private var callProgressState: TextView? = null
    private var btn_end_call: AppCompatImageView? = null
    private var imgTitleApp: AppCompatImageView? = null
    private var btn_dtmf: TextView? = null
    private var btn_speaker: TextView? = null
    private var btn_mute: TextView? = null

    private var dtmfPopupWindow: PopupWindow? = null

    protected var token: String? = null
    protected var displayname: String? = null
    protected var username: String? = null

    // Call timer implementation
    private val mTimerHandler: Handler? = null

    private var canHangup = true

    private var currentAudioDevice: AudioDeviceType? = null

    private val holdButton: ImageButton? = null

    protected var callTimeTextView: TextView? = null
    protected var audioDeviceButton: ImageButton? = null

    protected var textViewCallQuality: TextView? = null
    protected var ivCallQualityRating: ImageView? = null

    protected var mInteraction: Interaction? = null

    //service
    private val notificationChannelId = "Call Agent Service"
    private val notificationId = Constants.CALL_NOTIFICATION_ID
    private lateinit var notificationManager: NotificationManager

    private lateinit var mTimeHandler: Handler
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var postHandler: Handler
    private lateinit var errorHandler: Handler

//    private var mDevice: AudioOnlyDevice? = null
//    private var mUser: AudioOnlyUser? = null
//    private var mPlatform: AudioOnlyClientPlatform? = null
//    private var mSession: AudioOnlySession? = null

    private var mContextId: String? = null
    private var mToken: String? = ""

    private var mBluetootOnConnected = false


    var notificationText = ""
    private lateinit var notification: NotificationCompat.Builder

    private lateinit var mSensorManager: SensorManager
    private var mSensors: Sensor? = null

    private lateinit var audioManager: AudioManager
    private var showSessionWarning = false
    private var sessionWarningMessage = ""
    private var showSessionInterrupt = false
    private var sessionInterruptMessage = ""
    private var remoteAlertingStarted = false
    private val connectionAttemptTimeout = 4100
    private var isSessionStarted = false
    private var tickCounter = 0
    private var stopCounter = false
    private var isPaused=false
    var localStorageHelper :LocalStorageHelper? = null

    private var internetDisposable: Disposable? = null
    lateinit var scheduler: SchedulerProvider
    private  var isWaitNetworkTimerRunning = false;
    private val consTimerWait = 10000;

    val waitNetworkTimer = object: CountDownTimer(consTimerWait.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            isWaitNetworkTimerRunning = true;
        }

        override fun onFinish() {
            isWaitNetworkTimerRunning = false;
            if (!screenLocked and !hasOnGoingToggleSpeakerRequest
                and (activePopupWindow == null)) {
                hasOnGoingToggleSpeakerRequest = true
                endCall()
                callEnded()
            }
        }

    }




//    private val mCallTimeChecker = object : Runnable {
//        override fun run() {
//            if (!showSessionWarning and !stopCounter) {
//                updateTime()
//                mTimeHandler.postDelayed(this, Constants.TIMER_INTERVAL)
//            }
//        }
//    }


    // abstract methods that implementing interaction classes should implement.
    protected abstract fun view(): Int

    abstract fun getInteraction(): AbstractInteraction?

    abstract fun getInteractionType(): Int

    protected abstract fun updateCallState()

    protected abstract fun sendDtmf(tone: DTMFTone, mAudioInteraction: AudioInteraction?)
    protected abstract fun configureAudioMuteStatus(state: Boolean)

    abstract fun hangup()



    protected open fun canHungup(): Boolean {
        return canHangup
    }

    protected open fun setCanHungup(canHangup: Boolean) {
        this.canHangup = canHangup
    }
    //---------------------------------Dev AWG End---------------------------------


    private fun endCall() {
        if (!isEndCallTriggered) {
            Log.d("CallAgent", "End Call Triggered")
            isEndCallTriggered = true
            if (CallAgentService.isServiceRunning()) {
                CallAgentService.endCall(this)
            }
        }
        call_elapsed_time.text = "Disconnecting..."
        //        deleteSession()
    }
    private fun endCallFailed() {
        if (!isEndCallTriggered) {
            Log.d("CallAgent", "Failed call")
            isEndCallTriggered = true
            if (CallAgentService.isServiceRunning()) {
                CallAgentService.endCallFailed(this)
            }
        }
        call_elapsed_time.text = "Disconnecting..."
        //        deleteSession()
    }

    companion object {
        private lateinit var agentIntent: Intent
        private lateinit var dtmfIntent: Intent
        private var isActivityPaused = false
        public var isRunning = false
        private var hasPendingHangup = false
        private var isEstablished = false


    }

    //---------------------------------Dev AWG Start---------------------------------
    protected open fun handleClickToCall()   {
        try {
            mInteraction = createInteraction(this)
            mInteraction!!.setAuthorizationToken(token)
            registerListener()
            mInteraction!!.registerConnectionListener(object  : ConnectionListener{
                override fun onInteractionServiceConnecting() {
                    mLogger.d("onInteractionServiceConnecting : called")
                }

                override fun onInteractionServiceConnected() {
                    mLogger.d("onInteractionServiceConnected : called")

                }

                override fun onInteractionServiceDisconnected(p0: InteractionError?) {
                    mLogger.d("onInteractionServiceDisconnected : called")

                }

            })
            try {
                mInteraction!!.start()
                setCanHungup(true)
                attachEventHandlers()
            } catch (e: AuthorizationTokenException) {
                mLogger.e("Exception starting Audio Interaction: " + e.message, e)
                //                showToast(this, "Error with Authorization token!", Toast.LENGTH_LONG);
            }
        } catch (ex: Exception) {
            mLogger.e("Exception in handleClickToCall " + ex.message, ex)
        }
    }


    protected abstract fun attachEventHandlers()

    protected abstract fun registerListener()

    protected abstract fun createInteraction(listener: OnAudioDeviceChangeListener?): Interaction?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view())

        //---------------------------------Dev AWG Start---------------------------------
        btn_end_call = findViewById(R.id.btn_end_call)
        imgTitleApp = findViewById(R.id.img_title)
        btn_dtmf = findViewById(R.id.btn_dtmf)
        btn_speaker = findViewById(R.id.btn_speaker)
        btn_mute = findViewById(R.id.btn_mute)
        localStorageHelper = LocalStorageHelper(this)
        token = intent.extras?.getString(Constants.DATA_KEY_TOKEN) ?: localStorageHelper?.loadDataStorage(Constants.DATA_KEY_TOKEN)
        displayname=intent.extras!!.getString(Constants.DATA_KEY_DISPLAYNAME) ?: localStorageHelper?.loadDataStorage(Constants.DATA_KEY_DISPLAYNAME)
        username=intent.extras!!.getString(Constants.DATA_KEY_USERNAME)?: localStorageHelper?.loadDataStorage(Constants.DATA_KEY_USERNAME)
        numberToDial = intent.getStringExtra(Constants.KEY_NUMBER_TO_DIAL)?: localStorageHelper?.loadDataStorage(Constants.KEY_NUMBER_TO_DIAL)
        uui = intent.getStringExtra(Constants.KEY_CONTEXT).toString()?: localStorageHelper!!.loadDataStorage(Constants.KEY_CONTEXT)

        scheduler = AppSchedulerProvider()


//        val callinformation=findViewById<TextView>(R.id.call_information)

//        volumeControlStream = AudioManager.STREAM_VOICE_CALL
        actionStatus = true
        call_elapsed_time.text = "Calling..."


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.black)
        }

//        setInitialSpeakerMode()
        mPowerManager = getSystemService(POWER_SERVICE) as PowerManager
        mWakeLock = mPowerManager.newWakeLock(
            (PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP),
            "WakeLock:TAG"
        )
        Log.i("CallAgent", "ECT : ${isEndCallTriggered}")
        Log.i("CallAgent", "Finished onCreate")
        Utils.setOnCallInterface(this, true)
        layoutHandler = Handler(Looper.getMainLooper())

        callProgressState = findViewById(R.id.txtcallstate)


        btn_end_call!!.setOnClickListener(View.OnClickListener { view: View? ->
            if (!screenLocked and !hasOnGoingToggleSpeakerRequest
                and (activePopupWindow == null)) {
                Utils.setOnCallInterface(this, false)
                hasOnGoingToggleSpeakerRequest = true
                endCall()
                callEnded()
            }

        })

        btn_dtmf!!.setOnClickListener(View.OnClickListener { view: View? ->
            if (!screenLocked and (activePopupWindow == null)) {
                toggleDtmf(call_elapsed_time)
            }
        })

        btn_speaker!!.setOnClickListener(View.OnClickListener { view: View? ->
            if(currentAudioDevice.toString()=="HANDSET") {
                btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    getDrawable(R.drawable.baseline_volume_down_24),
                    null,
                    null
                )
                btn_speaker?.text="Handset"
            }
            if(currentAudioDevice.toString()=="SPEAKER") {
                btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    getDrawable(R.drawable.baseline_volume_up_72),
                    null,
                    null
                )
                btn_speaker?.text="Speaker"
            }
            if(currentAudioDevice.toString()=="BLUETOOTH_HEADSET") {
                btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    getDrawable(R.drawable.baseline_bluetooth_audio_24),
                    null,
                    null
                )
                btn_speaker?.text="Bluetooth"
            }
            if(currentAudioDevice.toString()=="WIRED_HEADSET") {
                btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    getDrawable(R.drawable.baseline_headphones_24),
                    null,
                    null
                )
                btn_speaker?.text="Wired Headset"
            }
            handleChangeAudioDevice()

//            if(mInteraction!!.isHeldRemotely)
//            {
//                mInteraction?.unhold()
//            }
//            else
//            {
//                mInteraction!!.hold()
//            }


        })
        btn_mute!!.setOnClickListener(View.OnClickListener { view: View? ->
            mLogger.d("Mute pressed")
            Log.d("mute","Kepencet mute diluar")
            if (mInteraction == null||mInteraction!=null){
                val mAudioInteraction = InteractionService.getInstance().audioInteraction
//                mInteraction = mAudioInteraction
                val isMuted = mAudioInteraction!!.isAudioMuted
                mLogger.i("Is audio muted: $isMuted")
                val muteStatus=!isMuted
                configureAudioMuteStatus(!isMuted)
                Log.d("mute","Kepencet mute")
            }
            else {
                mLogger.w("Audio Interaction is null")
            }
        })
        if(CallAgentService.isEstablished==true)
        {
            callStateInt=2
            txtcallstate.text="ESTABLISHED"
        }
        if(CallAgentService.isActivityPaused==false )
        {
            handleClickToCall()
            startService(Intent(this, ForegroundService::class.java))
           configureAudioMuteStatus(false)
            localStorageHelper?.saveDataStorage(Constants.CURRENT_MUTE_STATUS,false.toString())
        }
        else
        {
            if(localStorageHelper?.loadDataStorage(Constants.CURRENT_AUDIO_DEVICE)=="HANDSET") {
                btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    getDrawable(R.drawable.baseline_volume_down_24),
                    null,
                    null
                )
                btn_speaker?.text="Handset"
            }
            else if(localStorageHelper?.loadDataStorage(Constants.CURRENT_AUDIO_DEVICE)=="SPEAKER") {
                btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    getDrawable(R.drawable.baseline_volume_up_72),
                    null,
                    null
                )
                btn_speaker?.text="Speaker"
            }
            else if(localStorageHelper?.loadDataStorage(Constants.CURRENT_AUDIO_DEVICE)=="BLUETOOTH_HEADSET") {
                btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    getDrawable(R.drawable.baseline_bluetooth_audio_24),
                    null,
                    null
                )
                btn_speaker?.text="Bluetooth"
            }
            else if(localStorageHelper?.loadDataStorage(Constants.CURRENT_AUDIO_DEVICE)=="WIRED_HEADSET") {
                btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    getDrawable(R.drawable.baseline_headphones_24),
                    null,
                    null
                )
                btn_speaker?.text="Wired Headset"
            }
        }
        muteLogo()
//        deviceLogo()
        //If this is not implemented then after phone is put on sleep mode, the call remains active however audio transmission to other side stops after a few seconds  until you wake up the screen again.

        //---------------------------------Dev AWG End---------------------------------

    }

    private fun muteLogo()
    {
        if(localStorageHelper?.loadDataStorage(Constants.CURRENT_MUTE_STATUS)=="true")
        {
            btn_mute!!.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_mic_off_24),
                null,
                null
            )
            btn_mute!!.text ="Muted"
        }
        else if(localStorageHelper?.loadDataStorage(Constants.CURRENT_MUTE_STATUS)=="false")
        {
            btn_mute!!.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_mic_24),
                null,
                null
            )
            btn_mute!!.text="Unmuted"
        }
        else
        {
            btn_mute!!.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_mic_off_24),
                null,
                null
            )
            btn_mute!!.text="Unmuted"
        }
    }




    private fun setCurrentSpeakerMode(mode: Boolean?) {
        speakerEnabled = mode
        if (speakerEnabled == true) {
            btn_speaker!!.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_volume_up_72),
                null,
                null
            )
        } else {
            btn_speaker!!.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_volume_down_24),
                null,
                null
            )
        }
        hasOnGoingToggleSpeakerRequest = false
    }


    private var isCallInterrupted = false

    private fun showErrorAndSendLog(additionalData: String) {
        isCallInterrupted = true
        Log.i("CallAgent", "Show snack error on callstate ${callState.toString()}")
        val snackback = object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                finish()
                toHome(additionalData)
            }
        }
        runOnUiThread {
            if (isActivityReady and !isEndCallTriggered) {
                Toast.makeText(this, "unknow error", Toast.LENGTH_SHORT).show()
            } else {
                finish()
                toHome(additionalData)
            }
        }

    }
    private val durationObserver = Observer<String> { it ->
        val textdurationcall=findViewById<TextView>(R.id.call_elapsed_time)
        if (callStateInt == Constants.CALL_STATE_ESTABLISHED) {
            textdurationcall.text = it
        }
        if (callStateInt == Constants.CALL_STATE_DIALING) {
            textdurationcall.text = "Calling..."
        }
    }
    private val elapsedObserver = Observer<Long> { mCallDuration = it }

    private val fakeLockObserver = Observer<Boolean> {
        val params = this@BaseCall.window.attributes
        if (it) {
            if (!mWakeLock.isHeld) {
                mWakeLock.acquire()
            }
            screenLocked = true
        } else {
            if (mWakeLock.isHeld) {
                mWakeLock.release()
            }
            screenLocked = false
        }
    }
    private var callStateObserver = Observer<Int> {
        callState = it
        when (it) {
            Constants.CALL_STATE_DIALING -> {
                btn_end_call?.setBackgroundDrawable(resources.getDrawable(R.drawable.circle_bg))
                Glide.with(this)
                    .load(R.raw.loading)
                    .into(btn_end_call);

            }
            Constants.CALL_STATE_CONNECTING -> {
            }
            Constants.CALL_STATE_ESTABLISHED -> {
                hasConnected = true
                btn_end_call?.setBackgroundDrawable(resources.getDrawable(R.drawable.circle_white))
                btn_end_call?.setImageDrawable(resources.getDrawable(R.drawable.ic_call_red))
            }
            Constants.CALL_STATE_FINISH -> {
                Log.d("CallAgentActivity", "updateCallState observe: $callStateInt")

                if (activePopupWindow == null) {
                    if (callStateInt == 3){
                        endCall()
                        callEnded()
                    }

                }
            }

        }
    }
    private val loudSpeakerOnObserver = Observer<Boolean> { setCurrentSpeakerMode(it) }
    private var errorHandledOnUI = false
    private val callWarningMessageObserver = Observer<String> {
        if (it != "") {
            isWarningDismissed = false

            Log.i("CallAgent", "Receive warning message: $it")
            actionStatus = false
            anyErrorMessages = if (anyErrorMessages.isNotEmpty()) {
                "${anyErrorMessages}, $it"
            } else {
                it
            }
            if (!errorHandledOnUI) {
                showCallWarningPopup(it)
                errorHandledOnUI = true
            }
            CallAgentService.CallAgentServiceData.callWarningMessage.postValue("")
        }
    }
    private val callInterruptedMessageObserver = Observer<String> {
        if (it != "") {
            Log.i("CallAgent", "Receive interrupt message: $it")
            actionStatus = false
            anyErrorMessages = if (anyErrorMessages.isNotEmpty()) {
                "${anyErrorMessages}, $it"
            } else {
                "${anyErrorMessages}, $it"
            }
            if (!errorHandledOnUI) {
                showErrorAndSendLog(it)
                errorHandledOnUI = true
            }
            CallAgentService.CallAgentServiceData.callInterruptedMessage.postValue("")
        }
    }
    private val callInfoMessageObserver = Observer<String> {
        if (it != "") {
            Log.i("CallAgent", "Receive info message: $it")
            actionStatus = false
            anyErrorMessages = if (anyErrorMessages.isNotEmpty()) {
                "${anyErrorMessages}, $it"
            } else {
                it
            }
            if (!errorHandledOnUI) {
//                sendCallStateLog(it)
                errorHandledOnUI = true
            }
            CallAgentService.CallAgentServiceData.callInfoMessage.postValue("")
        }
    }
    private val toastMessageObserver = Observer<String> { showToast(it) }

    override fun onResume() {
        Log.i("CallAgent", "On Resume")
        super.onResume()
        isRunning=true
//        registerBluetoothBrReceiver()
        CallAgentService.setIsActivityPaused(false)
        // update duration
        CallAgentService.CallAgentServiceData.duration.observe(this, durationObserver)
        CallAgentService.CallAgentServiceData.elapsed.observe(this, elapsedObserver)

        // update on proximity change
        CallAgentService.CallAgentServiceData.fakeLock.observe(this, fakeLockObserver)

        // update on state change
        CallAgentService.CallAgentServiceData.callState.observe(this, callStateObserver)

        // show error snack on call interrupted
        CallAgentService.CallAgentServiceData.callInterruptedMessage.observe(
            this,
            callInterruptedMessageObserver
        )

        // show warning popup on call interrupted
        isCallInterrupted = false
        CallAgentService.CallAgentServiceData.callWarningMessage.observe(
            this,
            callWarningMessageObserver
        )

        // send log info about call state
        CallAgentService.CallAgentServiceData.callInfoMessage.observe(this, callInfoMessageObserver)

        // update button based on real loud speaker status
        CallAgentService.CallAgentServiceData.loudSpeakerOn.observe(this, loudSpeakerOnObserver)
        isEndCallTriggered = false

        CallAgentService.CallAgentServiceData.toastMessage.observe(this, toastMessageObserver)
        if (CallAgentService.getPendingHangup()) {
            CallAgentService.endCall(this)
        }

        observeNetwork()


    }



    private fun observeNetwork() {

        internetDisposable = ReactiveNetwork.observeInternetConnectivity()
            .subscribeOn(scheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnectedToInternet ->
                val text = isConnectedToInternet.toString()
                Log.d( "internet dispose:"," $text")

                if (!isConnectedToInternet && !isWaitNetworkTimerRunning){
                    waitNetworkTimer.start()
                }else{
                    if (isWaitNetworkTimerRunning){
                        waitNetworkTimer.cancel()
                        isWaitNetworkTimerRunning = false;
                    }
                }
            }
    }

    override fun onPause() {
        Log.i("CallAgent", "On Pause")
        super.onPause()
        if (!isEndCallTriggered) {
            CallAgentService.setIsActivityPaused(true)
        }


        CallAgentService.CallAgentServiceData.duration.removeObserver(durationObserver)
        CallAgentService.CallAgentServiceData.elapsed.removeObserver(elapsedObserver)
        CallAgentService.CallAgentServiceData.fakeLock.removeObserver(fakeLockObserver)
        CallAgentService.CallAgentServiceData.callState.removeObserver(callStateObserver)
        CallAgentService.CallAgentServiceData.callWarningMessage.removeObserver(
            callWarningMessageObserver
        )
        CallAgentService.CallAgentServiceData.callInterruptedMessage.removeObserver(callInterruptedMessageObserver)
        CallAgentService.CallAgentServiceData.callInfoMessage.removeObserver(callInfoMessageObserver)
        CallAgentService.CallAgentServiceData.loudSpeakerOn.removeObserver(loudSpeakerOnObserver)
        CallAgentService.CallAgentServiceData.toastMessage.removeObserver(toastMessageObserver)
    }

    override fun onAttachedToWindow() {
        Log.i("CallAgent", "Attached to window")
        if (!CallAgentService.isServiceRunning() and !isActivityReady and !isServiceHasBeenCalled) {
            isServiceHasBeenCalled = true
            Log.i("CallAgent", "Starting Service")
            CallAgentService.startService(this, intent)
//            setInitialSpeakerMode()
            isEndCallTriggered = false
        }
        isActivityReady = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.i("CallAgent", "Detached from window")
    }



    private fun showCallWarningPopup(additionalData: String) {
        if (isActivityReady and !isEndCallTriggered) {
            //showFullPopup(rootLayout)
        }
    }

    private fun toHome(additionalData: String?) {
        val homeIntent = Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        finish()
        startActivity(homeIntent)
    }

    private fun toggleDtmf(rootView: View) {
        if (mDtmfPopupWindow != null) {
            mDtmfPopupWindow!!.dismiss()
            return
        }
        btn_dtmf!!.setCompoundDrawablesWithIntrinsicBounds(
            null,
            getDrawable(R.drawable.baseline_dialpad_24),
            null,
            null
        )

        dialpad_holder.visibility = View.VISIBLE
        btn_end_call?.visibility = View.GONE
        txtcallstate?.visibility = View.GONE
        imgTitleApp?.visibility = View.GONE

        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialpad_frame, null)
        mDtmfPopupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        mDtmfPopupWindow!!.isFocusable = false
        mDtmfPopupWindow!!.update()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.BOTTOM
            mDtmfPopupWindow!!.enterTransition = slideIn

            val slideOut = Slide()
            slideOut.slideEdge = Gravity.BOTTOM
            mDtmfPopupWindow!!.exitTransition = slideOut

        }
        mDtmfPopupWindow!!.setOnDismissListener {
            layoutHandler.postDelayed({
                restoreLayoutAfterDialpadDismiss()
            }, 400)
        }
        mDtmfPopupWindow!!.showAsDropDown(rootView)
//        mDtmfPopupWindow!!.dimBehind()
        setDialpadClickable()

    }
    private fun restoreLayoutAfterDialpadDismiss() {
        dialpad_holder.visibility = View.GONE
        btn_end_call?.visibility = View.VISIBLE
        txtcallstate?.visibility = View.VISIBLE
        imgTitleApp?.visibility = View.VISIBLE
        btn_dtmf!!.setCompoundDrawablesWithIntrinsicBounds(
            null,
            getDrawable(R.drawable.baseline_dialpad_24),
            null,
            null
        )
        mDtmfPopupWindow = null
    }

    private fun setDialpadClickable() {
        if (mDtmfPopupWindow != null) {
            mDtmfPopupWindow!!.contentView.dialpad_key_1.setOnClickListener {
                dialKeyPressed(1,"1")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_2.setOnClickListener {
                dialKeyPressed(2,"2")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_3.setOnClickListener {
                dialKeyPressed(3,"3")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_4.setOnClickListener {
                dialKeyPressed(4,"4")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_5.setOnClickListener {
                dialKeyPressed(5,"5")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_6.setOnClickListener {
                dialKeyPressed(6,"6")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_7.setOnClickListener {
                dialKeyPressed(7,"7")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_8.setOnClickListener {
                dialKeyPressed(8,"8")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_9.setOnClickListener {
                dialKeyPressed(9,"9")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_0.setOnClickListener {
                dialKeyPressed(0,"0")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_asterisk.setOnClickListener {
                dialKeyPressed(10,"*")
            }
            mDtmfPopupWindow!!.contentView.dialpad_key_hashtag.setOnClickListener {
                dialKeyPressed(11,"#")
            }
            mDtmfPopupWindow!!.contentView.secondary_erase_button.setOnClickListener {
                dialKeyPressed(12,"1")
            }
            mDtmfPopupWindow!!.contentView.dial_input.text = dialText
            if (dialText != "") {
//                mDtmfPopupWindow!!.contentView.secondary_erase_button.visibility = View.VISIBLE
                mDtmfPopupWindow!!.contentView.secondary_erase_button.visibility = View.GONE
            } else {
                mDtmfPopupWindow!!.contentView.secondary_erase_button.visibility = View.GONE
            }
        }
    }

    private fun dialKeyPressed(key: Int,identifier:String) {
        when {
            key<10 -> dialText = dialText.plus(key.toString())
            key == 10 -> dialText = dialText.plus("*")
            key == 11 -> dialText = dialText.plus("#")
            key == 12 -> dialText = dialText.dropLast(1)
        }
        if (key in 0..11) {
            val mAudioInteraction = InteractionService.getInstance().audioInteraction
            if(key==10)
            {
                val tone=identifier
                sendDtmf(DTMFTone.get(tone),mAudioInteraction)
            }
            else if(key==11)
            {
                val tone=identifier
                sendDtmf(DTMFTone.get(tone),mAudioInteraction)
            }
            else {
                val tone = key.toString()
                mLogger.d("DTMF: $tone")
                sendDtmf(DTMFTone.get(tone), mAudioInteraction)
                //---------------------------------Dev AWG Start---------------------------------

//            if (key==1)
//            {
//                sendDtmf(DTMFType.ONE,mAudioInteraction)
//            }
//            else if (key==2)
//            {
//                sendDtmf(DTMFType.TWO,mAudioInteraction)
//            }
//            else if (key==3)
//            {
//                sendDtmf(DTMFType.THREE,mAudioInteraction)
//            }
//            else if (key==4)
//            {
//                sendDtmf(DTMFType.FOUR,mAudioInteraction)
//            }
//            else if (key==5)
//            {
//                sendDtmf(DTMFType.FIVE,mAudioInteraction)
//            }
//            else if (key==6)
//            {
//                sendDtmf(DTMFType.SIX,mAudioInteraction)
//            }
//            else if (key==7)
//            {
//                sendDtmf(DTMFType.SEVEN,mAudioInteraction)
//            }
//            else if (key==8)
//            {
//                sendDtmf(DTMFType.EIGHT,mAudioInteraction)
//            }
//            else if (key==9)
//            {
//                sendDtmf(DTMFType.NINE,mAudioInteraction)
//            }
//            else if (key==0)
//            {
//                sendDtmf(DTMFType.ZERO,mAudioInteraction)
//            }
//            else if (key==10)
//            {
//                sendDtmf(DTMFType.STAR,mAudioInteraction)
//            }
//            else if (key==11)
//            {
//                sendDtmf(DTMFType.POUND,mAudioInteraction)
//            }
//            else
//            {
//                sendDtmf(DTMFTone.get(tone), mAudioInteraction)
//            }
            }
            //---------------------------------Dev AWG End---------------------------------
        }
        if (mDtmfPopupWindow != null) {
            mDtmfPopupWindow!!.contentView.dial_input.text = dialText
            if (dialText != "") {
//                mDtmfPopupWindow!!.contentView.secondary_erase_button.visibility = View.VISIBLE
                mDtmfPopupWindow!!.contentView.secondary_erase_button.visibility = View.GONE
            } else {
                mDtmfPopupWindow!!.contentView.secondary_erase_button.visibility = View.GONE
            }
        }
    }
    //---------------------------------Dev AWG Start---------------------------------
    override fun onInteractionInitiating() {
        mLogger.d("AbstractInteraction:onInteractionInitiating")
        updateCallState()
        hasConnected = false
    }

    override fun onInteractionRemoteAlerting() {
        mLogger.d("AbstractInteraction:onInteractionRemoteAlerting")
        runOnUiThread {  }
        hasConnected = false
        updateCallState()
    }


    override fun onInteractionActive() {
        mLogger.d("AbstractInteraction:onInteractionActive")
        hasConnected = true
        updateCallState()
    }

    override fun onInteractionEnded() {
        mLogger.d("AbstractInteraction:onInteractionEnded")
        updateCallState()
        hasConnected = false
//        hangup()
//        callEnded()

    }
    //---------------------------------Dev AWG End---------------------------------



//    @Override
//    public void onInteractionHeld() {
//        holdButton.setEnabled(!mInteraction.isHeldRemotely());
//        holdButton.setImageResource(R.drawable.ic_activecall_advctrl_hold_active);
//        updateCallState();
//    }
//
//    @Override
//    public void onInteractionUnheld() {
//        holdButton.setImageResource(R.drawable.ic_activecall_advctrl_hold);
//        updateCallState();
//    }


    //    @Override
    //    public void onInteractionHeld() {
    //        holdButton.setEnabled(!mInteraction.isHeldRemotely());
    //        holdButton.setImageResource(R.drawable.ic_activecall_advctrl_hold_active);
    //        updateCallState();
    //    }
    //
    //    @Override
    //    public void onInteractionUnheld() {
    //        holdButton.setImageResource(R.drawable.ic_activecall_advctrl_hold);
    //        updateCallState();
    //    }
    //---------------------------------Dev AWG Start---------------------------------
    override fun onInteractionHeldRemotely() {
        //Take appropriate action
    }

    override fun onInteractionUnheldRemotely() {
        //Take appropriate action
    }


    override fun onInteractionFailed(error: InteractionError) {
        mLogger.d("Interaction failed, error - $error")
        errorState=error.toString()
        updateCallState()

//        displayMessage(error.toString(), true);
    }


    override fun onDiscardComplete() {
        mLogger.d("AbstractInteraction:onInteractionEnded")
        finish()
    }
    //---------------------------------Dev AWG End---------------------------------


    protected abstract fun getInteractionTimeElapsed(): Long

    protected abstract fun getElementReferences()

    override fun onBackPressed() {
        super.onBackPressed()
        mLogger.d("onBackPressed()")
        hangup()
    }


//    override fun finish() {
//        super.finish()
////        onDestroy()
////        hangup()
////        callEnded()
//        runOnUiThread {
////            stopCallTimer()
//        }
//    }

    override fun onStop() {
        super.onStop()
        mLogger.d("onStop()")
        dismissDtmf()
    }

    //---------------------------------Dev AWG Start---------------------------------
    override fun onInteractionHeld() {
        holdButton!!.isEnabled = !mInteraction!!.isHeldRemotely
        updateCallState()
    }

    override fun onInteractionUnheld() {
        updateCallState()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        isRunning=false
//        mLogger.d("onDestroy()")
//        hangup()
//        stopService(Intent(this, ForegroundService::class.java))
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        mLogger.d("onDestroy()")
//        if (this::mediaPlayer.isInitialized) {
//            mediaPlayer.stop()
//        }
//        Log.i("CallEvent", "On Destroy")
//        Log.i("CallEvent", "Sending Logs")
//        Log.i("CallEvent", "Msg: $anyErrorMessages")
//
////        writeLog(
////            "CALL:" + getString(currentCall!!.text!!).replace("\n", " "),
////            true,
////            anyErrorMessages
////        )
//        Utils.setOnCallInterface(this, false)
////        hangup()
//        stopService(Intent(this, ForegroundService::class.java))
//    }


    // UI helper methods
    protected open fun updateCallState(state: String?) {
//        runOnUiThread(() -> callProgressState.setText(state));
        runOnUiThread { callProgressState!!.text = state.toString()
            if(state=="IDLE")
            {
                callStateInt=0
            }
            else if(state=="REMOTE_ALERTING")
            {
                callStateInt=1
            }
            else if(state=="ESTABLISHED")
            {
                callStateInt=2
            }
            else if(state=="ENDED")
            {
                callStateInt=3
//                hangup()
            }
            else if(state=="FAILED")
            {
                callStateInt=4

                if(errorState=="REJECTED")
                {
                    showCallWarningPopup(errorState)
                }
                CallAgentService.broadcastCallStateError(errorState)
                endCallFailed()


//                CallAgentService.broadcastCallState(callStateInt)
//                callEnded()


            }
            Log.d("CallAgentActivity", "updateCallState: $callStateInt")
            CallAgentService.broadcastCallState(callStateInt)

        }
    }
    //---------------------------------Dev AWG End---------------------------------

    protected open fun showToast(activity: Activity, message: String?, length: Int) {
        activity.runOnUiThread {
            val toast = Toast.makeText(activity.applicationContext, message, length)
            toast.show()
        }
    }
    private fun showToast(additionalData: String) {
//        Toast.makeText(this, additionalData, Toast.LENGTH_SHORT).show()
        //pass
    }
    private fun callEnded() {
        if (!isEndCallTriggered) {
            Log.d("CallAgent", "End Call Triggered")
            isEndCallTriggered = true
            if (!isCallInterrupted) {
                Log.i("CallAgent", "Hangup by system")
            } else {
                Log.i("CallAgent", "Call interrupted")
            }
            hangup()
        } else {
            Log.i("CallAgent", "Hangup by user")
            hangup()
        }
    }



    protected open fun dismissDtmf() {
        if (dtmfPopupWindow != null) {
            dtmfPopupWindow!!.dismiss()
        }
        dtmfPopupWindow = null
    }

    open fun dtmf(v: View) {
        try {
            val tone = v.tag as String
            mLogger.d("DTMF: $tone")
            val mAudioInteraction = InteractionService.getInstance().audioInteraction
//            sendDtmf(DTMFTone.get(tone),mAudioInteraction)
        } catch (e: Exception) {
            mLogger.e("Exception in dtmf", e)
            //            displayMessage("DTMF exception: " + e.getMessage());
        }
    }


    //---------------------------------Dev AWG Start---------------------------------
//    override fun onInteractionServiceConnecting() {
//        mLogger.d("onInteractionServiceConnecting - Call signalling interrupted")
//    }
//
//    override fun onInteractionServiceConnected() {
//        mLogger.d("onInteractionServiceConnected - Call signalling available")
//    }
//
//    override fun onInteractionServiceDisconnected(interactionError: InteractionError) {
//        mLogger.d("onInteractionServiceDisconnected with error [$interactionError] - Call signalling not recoverable")
//    }

    override fun onAudioDeviceListChanged(devices: List<AudioDeviceType?>?) {
        mLogger.d("onAudioDeviceListChanged")
    }

    override fun onAudioDeviceChanged(audioDeviceType: AudioDeviceType) {
        mLogger.d("onAudioDeviceChanged $audioDeviceType")
//        audioDeviceButton!!.setEnabled(true);
        currentAudioDevice = audioDeviceType
        localStorageHelper?.saveDataStorage(Constants.CURRENT_AUDIO_DEVICE,audioDeviceType.toString())
        Log.d("Audio Device Tyoe",currentAudioDevice.toString())
        if(currentAudioDevice.toString()=="HANDSET") {
            btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_volume_down_24),
                null,
                null
            )
            btn_speaker?.text="Handset"
        }
        else if(currentAudioDevice.toString()=="SPEAKER") {
            btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_volume_up_72),
                null,
                null
            )
            btn_speaker?.text="Speaker"
        }
        else if(currentAudioDevice.toString()=="BLUETOOTH_HEADSET") {
            btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_bluetooth_audio_24),
                null,
                null
            )
            btn_speaker?.text="Bluetooth"
        }
        else if(currentAudioDevice.toString()=="WIRED_HEADSET") {
            btn_speaker?.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.baseline_headphones_24),
                null,
                null
            )
            btn_speaker?.text="Wired Headset"
        }


//        audioDeviceButton!!.setImageResource(getAudioDeviceIcon(audioDeviceType));
    }

    override fun onAudioDeviceError(audioDeviceError: AudioDeviceError) {
        mLogger.d("onAudioDeviceError $audioDeviceError")
        Log.d("audio tapi error",audioDeviceError.toString())
    }
    open fun handleChangeAudioDevice() {
        if (mInteraction == null){
            val mAudioInteraction = InteractionService.getInstance().audioInteraction
            mInteraction = mAudioInteraction

        }
        currentAudioDevice = when(localStorageHelper?.loadDataStorage(Constants.CURRENT_AUDIO_DEVICE)){
            "WIRED_HEADSET" -> AudioDeviceType.WIRED_HEADSET
            "HANDSET" -> AudioDeviceType.HANDSET
            "BLUETOOTH_HEADSET" -> AudioDeviceType.BLUETOOTH_HEADSET
            else -> AudioDeviceType.SPEAKER
        }

        val switchList = getDevicesToSwitch(
            mInteraction!!.availableAudioDevices,
            currentAudioDevice!!
        )
        if (switchList != null && !switchList.isEmpty()) {
            if (switchList.size == 1) {
                mInteraction!!.setAudioDevice(switchList[0])
            } else {
                showSelectAudioDeviceDialog(switchList)
            }
        } else {
//            Toast.makeText(this, R.string.no_device_to_switch, Toast.LENGTH_SHORT).show()
        }
    }
//     open fun handleChangeAudioDevice() {
//        val switchList = getDevicesToSwitch(
//            mInteraction!!.availableAudioDevices,
//            currentAudioDevice!!
//        )
//        if (switchList != null && !switchList.isEmpty()) {
//            if (switchList.size == 1) {
//                mInteraction!!.setAudioDevice(switchList[0])
//            } else {
//                showSelectAudioDeviceDialog(switchList)
//            }
//        } else {
//            Toast.makeText(this, R.string.no_device_to_switch, Toast.LENGTH_SHORT).show()
//        }
//    }

    fun getDevicesToSwitch(
        availableDevices: List<AudioDeviceType>,
        currentDevice: AudioDeviceType
    ): List<AudioDeviceType>? {
        val toSwitch: MutableList<AudioDeviceType> = ArrayList()
        for (device in availableDevices) {
            if (device != currentDevice) toSwitch.add(device)
//            toSwitch.add(device)
        }
        return toSwitch
    }


    fun showSelectAudioDeviceDialog(devices: List<AudioDeviceType>) {
        val labels: MutableList<String> = ArrayList()
        for (device in devices) {
            labels.add(getAudioDeviceLabel(device))
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Audio Device")
        builder.setItems(
            labels.toTypedArray()
        ) { dialog: DialogInterface?, index: Int ->
//             bluetoothchange(index,devices)
            mInteraction!!.setAudioDevice(
                devices[index]
            )
        }
        builder.show()
        Log.d("Speaker", "Show select audio device dialog")
    }


    fun getAudioDeviceLabel(type: AudioDeviceType): String {
        return when (type) {
            AudioDeviceType.WIRED_HEADSET -> "Wired Headset"
            AudioDeviceType.BLUETOOTH_HEADSET -> "Bluetooth Headset"
            AudioDeviceType.HANDSET -> "Earpiece Speaker"
            AudioDeviceType.SPEAKER -> "Speaker"
            else -> "Earpiece speaker"

        }
        Log.d("Speaker", "getAudioDeviceLabel")
    }


}


