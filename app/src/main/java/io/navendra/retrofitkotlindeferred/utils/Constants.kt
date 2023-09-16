package io.navendra.retrofitkotlindeferred.utils

import com.avaya.ocs.Services.Work.Enums.PlatformType
import okhttp3.MediaType

object Constants{
    const val BASE_URL = "https://labdemo.phintraco.com:443"
    const val TIMER_INTERVAL = 1000L
    const val HTTP = "http://"
    const val HTTPS = "https://"
    const val SERVER_PLACEHOLDER = "labdemo.phintraco.com"

    const val AAWG_TOKEN_URL_PATH_PLACEHOLDER = "token-generation-service/token/getEncryptedToken"
    const val AAWG_TOKEN_URL_PATH_PLACEHOLDER2 = "csa/resources/tenants/default"
    const val TOKEN_URL_PATH_PLACEHOLDER = "token-generation-service/token/getEncryptedToken"

    const val PORT_PLACEHOLDER = "443"

    const val AAWG_RETRIEVE_TOKEN_URL =
        "$HTTPS$SERVER_PLACEHOLDER:$PORT_PLACEHOLDER/$AAWG_TOKEN_URL_PATH_PLACEHOLDER"
    const val AAWG_RETRIEVE_TOKEN_INSECURE_URL =
        "$HTTP$SERVER_PLACEHOLDER:$PORT_PLACEHOLDER/$AAWG_TOKEN_URL_PATH_PLACEHOLDER"

    const val DATA_KEY_TOKEN = "_token"
    const val DATA_KEY_DISPLAYNAME = "_displayName"
    const val DATA_KEY_USERNAME = "_username"

    const val DATA_KEY_VIDEO_RESOLUTION = "_videoResolution"
    const val DATA_KEY_VIDEO_ORIENTATION = "_videoOrientation"


    val AAWG_TOKEN_MEDIATYPE = MediaType.parse("application/vnd.avaya.csa.tokens.v1+json")
    const val STORAGE_NAME_KEY = "PhinCallKey"
    const val DIAL_KEY_IDENTIFIER = "dial_key"
    const val KEY_CALL_STATE = "call_state"
    const val CALL_STATE_PUSH_DTMF = 800
    const val CALL_STATE_DIALING = 0
    const val CALL_STATE_CONNECTING = 1
    const val CALL_STATE_ESTABLISHED = 2
    const val CALL_STATE_FINISH = 3
    const val CALL_STATE_FAILED=4
    const val CALL_STATE_TOGGLE_SPEAKER = 10
    const val CALL_STATE_DIALKEY_PRESSED = 11
    const val CALL_STATE_TOGGLE_VIDEO = 12
    const val CALL_NOTIFICATION_ID = 900
    const val CALL_INTERRUPT_DATA_HEADER = "interrupt"
    const val DATA_SESSION_KEY = "_session_key"
    const val KEY_NUMBER_TO_DIAL = "key_numberToDial"
    const val KEY_CONTEXT = "key_context"
    const val CURRENT_MUTE_STATUS = "current_mute_status"
    const val SP_GLOBAL = "com.phintech.avaya"
    const val ON_CALL_INTERFACE = "on_call_interface"
    const val CURRENT_AUDIO_DEVICE = "current_audio_device"
    const val CALL_DEST = "call_destination"
    const val DEST_NUMBER = "3234"
}