package io.navendra.retrofitkotlindeferred.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import io.navendra.retrofitkotlindeferred.R
import io.navendra.retrofitkotlindeferred.utils.CallAgentService.CallAgentServiceData.errorState
import io.netty.util.Constant
import java.util.concurrent.TimeUnit

class CallAgentService : Service() {
    private val notificationChannelId = "Call Agent Service"
    private val notificationId = Constants.CALL_NOTIFICATION_ID
    private lateinit var notificationManager: NotificationManager

    private lateinit var mTimeHandler: Handler
    private lateinit var postHandler: Handler
    private lateinit var errorHandler: Handler

//    private var mDevice: AudioOnlyDevice? = null
//    private var mUser: AudioOnlyUser? = null
//    private var mPlatform: AudioOnlyClientPlatform? = null
//    private var mSession: AudioOnlySession? = null

    private var mContextId: String? = null
    private var mToken: String? = ""
    private var mCallDuration: Long = 0
    var numberToDial = ""

    var notificationText = ""
    var hasConnected = false
    var actionStatus = true
    var anyErrorMessages = ""
    private lateinit var notification: NotificationCompat.Builder

    private lateinit var mSensorManager: SensorManager
    private var mSensors: Sensor? = null

    private lateinit var audioManager: AudioManager
    private var speakerEnabled = false
    private var showSessionWarning = false
    private var sessionWarningMessage = ""
    private var showSessionInterrupt = false
    private var sessionInterruptMessage = ""
    private var remoteAlertingStarted = false
    private val connectionAttemptTimeout = 4100
    private var isSessionStarted = false
    private var tickCounter = 0
    private var stopCounter = false

    private val mCallTimeChecker = object : Runnable {
        override fun run() {
            if (!showSessionWarning and !stopCounter) {
                updateTime()
                mTimeHandler.postDelayed(this, Constants.TIMER_INTERVAL)
            }
        }
    }





    fun updateTime() {
        mCallDuration += 1000
        tickCounter += 1
        var notificationContentText = "Calling... "
        when (CallAgentServiceData.callState.value) {
            Constants.CALL_STATE_DIALING -> notificationContentText = "Calling... "
            Constants.CALL_STATE_CONNECTING -> {
                notificationContentText = "Calling... "
                onSessionRemoteAlerting()
            }
            Constants.CALL_STATE_ESTABLISHED ->{
                onSessionEstablished()
                notificationContentText = "Ongoing Call "  + notificationText + " (" + formatTime(mCallDuration) + ") "
                }
            Constants.CALL_STATE_FINISH->{

            }
            Constants.CALL_STATE_FAILED->{

            }
        }
        notification.setContentText(notificationContentText)
        notificationManager.notify(notificationId, notification.build())
        broadcastDuration(mCallDuration, formatTime(mCallDuration))
//        if (tickCounter == 7) {
//            onNetworkError(null) //log+snackbar
//            onCallError(null, null, "", "") //log+popup
//        }
//        if (tickCounter == 4) {
//            onSessionRemoteDisplayNameChanged(null, "") //log
//        }
//        if (tickCounter == 1) {
//            onSessionRemoteAlerting(null, true) //log
//        }
//        Log.i("tick", "tok $tickCounter")
    }

    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return resources.getString(
            R.string.time_hours_minutes_seconds_formatter,
            hours,
            minutes,
            seconds
        )
    }


    companion object {

        fun startService(context: Context, intent: Intent) {
            val startIntent = Intent(context, CallAgentService::class.java)
            startIntent.putExtra(Constants.KEY_CALL_STATE, Constants.CALL_STATE_DIALING)
            agentIntent = intent
            ContextCompat.startForegroundService(context, startIntent)
        }
        fun toggleSpeaker(context: Context) {
            val triggerIntent = Intent(context, CallAgentService::class.java)
            triggerIntent.putExtra(Constants.KEY_CALL_STATE, Constants.CALL_STATE_TOGGLE_SPEAKER)
            try {
                ContextCompat.startForegroundService(context, triggerIntent)
            } catch (e: Exception) {
                //pass
            }
        }
        fun endCall(context: Context) {
            val triggerIntent = Intent(context, CallAgentService::class.java)
            triggerIntent.putExtra(Constants.KEY_CALL_STATE, Constants.CALL_STATE_FINISH)
            try {
                ContextCompat.startForegroundService(context, triggerIntent)
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
        fun endCallFailed(context: Context) {
            val triggerIntent = Intent(context, CallAgentService::class.java)
            triggerIntent.putExtra(Constants.KEY_CALL_STATE, Constants.CALL_STATE_FAILED)
            try {
                ContextCompat.startForegroundService(context, triggerIntent)
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
        fun broadcastCallState(state: Int) {
            CallAgentServiceData.callState.postValue(state)
        }

        @JvmStatic
        fun broadcastCallStateError(error: String) {
            CallAgentServiceData.errorState.postValue(error)
        }
        fun pushDTMF(context: Context, intent: Intent) {
            val triggerIntent = Intent(context, CallAgentService::class.java)
            triggerIntent.putExtra(Constants.KEY_CALL_STATE, Constants.CALL_STATE_PUSH_DTMF)
            dtmfIntent = intent
            try {
                ContextCompat.startForegroundService(context, triggerIntent)
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }

        private lateinit var agentIntent: Intent
        private lateinit var dtmfIntent: Intent
        public var isActivityPaused = false
        private var isRunning = false
        private var hasPendingHangup = false
        public var isEstablished = false
        fun isServiceRunning() : Boolean {
            return isRunning
        }

        fun getAgentIntent() : Intent {
            return agentIntent
        }
        fun setIsActivityPaused(state: Boolean) {
            isActivityPaused = state
        }
        fun getPendingHangup() : Boolean {
            return hasPendingHangup
        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getIntExtra(Constants.KEY_CALL_STATE, Constants.CALL_STATE_DIALING)) {
//        when ((CallAgentServiceData.callState.value)) {
            Constants.CALL_STATE_DIALING -> {
                isRunning = true
                isEstablished = false
                remoteAlertingStarted = false
                try {
                    val input = "test call"
                    numberToDial = "${agentIntent.getStringExtra(Constants.KEY_NUMBER_TO_DIAL)}"
                    mContextId = agentIntent.getStringExtra(Constants.KEY_CONTEXT)
                    if (!mContextId.isNullOrEmpty()) {
                        mContextId = mContextId!!.trim()
                    }
                    mToken = agentIntent.getStringExtra(Constants.DATA_SESSION_KEY)
                    notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    mTimeHandler = Handler()
                    postHandler = Handler()
                    errorHandler = Handler()
                    notificationText = input
                    createNotificationChannel()
                    agentIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    val pendingIntent = if (Build.VERSION.SDK_INT < 31) {
                        PendingIntent.getActivity(
                            this,
                            0, agentIntent, PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    } else {
                        PendingIntent.getActivity(
                            this,
                            0, agentIntent, PendingIntent.FLAG_MUTABLE
                        ) }
                    notification = NotificationCompat.Builder(this, notificationChannelId)
                        .setContentTitle("Halo BCA")
                        .setContentText("Calling $input")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setOnlyAlertOnce(true)
                    startForeground(notificationId, notification.build())
//                        if (!this::audioManager.isInitialized) {
//                            initAudioManager()
//                        }

                    mCallDuration = 0
                    mCallTimeChecker.run()
                    mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    mSensors = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                    if (mSensors != null) {
                        mSensorManager.registerListener(
                            proximitySensorEventListener,
                            mSensors,
                            SensorManager.SENSOR_DELAY_NORMAL
                        )
                    }
//                    connect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }Constants.CALL_STATE_FINISH -> {
            if (hasPendingHangup) {
                hasPendingHangup = false
                continuePendingHangup()
            } else {

                hangup()

            }

        }
            Constants.CALL_STATE_FAILED->{
                if(errorState.value.toString()!="REJECTED"&& errorState.value!=null)
                {
                    broadcastCallInterruptedSignal(errorState.value.toString())
                }
                else
                {
                    hangup()
                }
//                hangup()
//                isActivityPaused=false
            }
            Constants.CALL_STATE_TOGGLE_SPEAKER -> {
//                if (!showSessionWarning && !showSessionInterrupt && !stopCounter) {
////                    toggleSpeakerSvc()
//                }
            }
            Constants.CALL_STATE_PUSH_DTMF -> {
//                if (mSession != null) {
//                    when (dtmfIntent.getIntExtra(Constant.DIAL_KEY_IDENTIFIER, 12)) {
//                        0 -> mSession!!.sendDTMF(DTMFType.ZERO)
//                        1 -> mSession!!.sendDTMF(DTMFType.ONE)
//                        2 -> mSession!!.sendDTMF(DTMFType.TWO)
//                        3 -> mSession!!.sendDTMF(DTMFType.THREE)
//                        4 -> mSession!!.sendDTMF(DTMFType.FOUR)
//                        5 -> mSession!!.sendDTMF(DTMFType.FIVE)
//                        6 -> mSession!!.sendDTMF(DTMFType.SIX)
//                        7 -> mSession!!.sendDTMF(DTMFType.SEVEN)
//                        8 -> mSession!!.sendDTMF(DTMFType.EIGHT)
//                        9 -> mSession!!.sendDTMF(DTMFType.NINE)
//                        10 -> mSession!!.sendDTMF(DTMFType.STAR)
//                        11 -> mSession!!.sendDTMF(DTMFType.POUND)
//                    }
//                }
            }

        }
        return START_STICKY
    }
    public fun broadcastCallInterruptedSignal(message: String) {
        actionStatus = false
        anyErrorMessages = if (anyErrorMessages.isNotEmpty()) {
            "$anyErrorMessages,$message"
        } else {
            message
        }
        sessionInterruptMessage = message
        if (!showSessionWarning) {
            showSessionInterrupt = true
            errorHandler.postDelayed({
                hangup()
            }, 1000)
        }
    }

    private fun continuePendingHangup() {
        if (this::notificationManager.isInitialized) {
            notificationManager.cancel(notificationId)
        }
        if (showSessionWarning) {
            CallAgentServiceData.callWarningMessage.postValue(sessionWarningMessage)
        }
        if (showSessionInterrupt) {
            CallAgentServiceData.callInterruptedMessage.postValue(sessionInterruptMessage)
        }
        broadcastCallState(Constants.CALL_STATE_FINISH)
        stopForeground(true)
        stopSelf()
    }


    private var proximitySensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }

        override fun onSensorChanged(p0: SensorEvent?) {
            if (p0!!.sensor.type == Sensor.TYPE_PROXIMITY) {
                if (p0.values[0] == 0f) {
                    broadcastFakeLockState(true)
                } else {
                    broadcastFakeLockState(false)
                }
            }
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
//        if (this::postHandler.isInitialized) {
//            broadcastCallState(Constant.CALL_STATE_CONNECTING)
//
//        }
        super.onDestroy()
        try {
//            unregisterReceiver(earPhoneBroadcastReceiver)
//            unregisterReceiver(btBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        if (mSession != null) {
//            mSession!!.unregisterListener(this)
//            mUser?.unregisterListener(this)
//            mSession!!.end()
//            mUser?.terminate()
//        }
//        speakerEnabled = false
        isRunning = false
//        mDevice = null
//        mPlatform = null
//        mUser = null
//        leaveAudio()
        Log.i("CallEvent", "On Destroy")
        Log.i("CallEvent", "Sending Logs")
        Log.i("CallEvent", "Msg: $anyErrorMessages")
        if(errorState.value==null || errorState.value.toString()=="")
        {
            anyErrorMessages==""
        }
        else
        {
            anyErrorMessages= "[Interaction-Failed-"+errorState.value.toString()+"]"
            CallAgentService.CallAgentServiceData.errorState.postValue("")
        }


    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(notificationChannelId, "Call Agent Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun hangup() {
        if (this::mTimeHandler.isInitialized) {
            stopCounter = true
        }
        if (isActivityPaused) {
            hasPendingHangup = true
            notification.setContentText("Call Ended")
            notificationManager.notify(notificationId, notification.build())
        } else {
            if (this::notificationManager.isInitialized) {
                notificationManager.cancel(notificationId)
            }
            if (showSessionWarning) {
                CallAgentServiceData.callWarningMessage.postValue(sessionWarningMessage)
                showSessionWarning = false
            }
            if (showSessionInterrupt) {
                CallAgentServiceData.callInterruptedMessage.postValue(sessionInterruptMessage)
                showSessionInterrupt = false
            }
            broadcastCallState(Constants.CALL_STATE_FINISH)
            stopForeground(true)
            stopSelf()
        }
    }


     fun onSessionRemoteAlerting() {
        if(remoteAlertingStarted==false)
        {
            remoteAlertingStarted = true
        }
        else
        {

        }
    }

     fun onSessionEstablished() {
    hasConnected = true
    if (isEstablished == false) {
        isEstablished = true
        mCallDuration = 0
        broadcastHasConnected(true)

    } else {
        isEstablished = true

    }
}


    object CallAgentServiceData {
        val duration = MutableLiveData<String>()
        val fakeLock = MutableLiveData<Boolean>()
        val elapsed = MutableLiveData<Long>()
        var callState = MutableLiveData<Int>().apply { postValue(0) }
        val errorState = MutableLiveData<String>()
        val hasConnected = MutableLiveData<Boolean>()
        val callInterruptedMessage = MutableLiveData<String>()
        val callWarningMessage = MutableLiveData<String>()
        val callInfoMessage = MutableLiveData<String>()
        val loudSpeakerOn = MutableLiveData<Boolean>()
        val toastMessage = MutableLiveData<String>()
    }

    private fun broadcastDuration(elapsed: Long, duration: String){
        CallAgentServiceData.elapsed.postValue(elapsed)
        CallAgentServiceData.duration.postValue(duration)
    }

    private fun broadcastFakeLockState(isLocked: Boolean) {
        if (!showSessionInterrupt && !showSessionWarning) {
            CallAgentServiceData.fakeLock.postValue(isLocked)
        }
    }



    private fun broadcastHasConnected(hasConnected: Boolean) {
        CallAgentServiceData.hasConnected.postValue(hasConnected)
    }


     public fun broadcastCallState(state: Int) {
        CallAgentServiceData.callState.postValue(state)
    }


}
