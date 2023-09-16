package io.navendra.retrofitkotlindeferred.utils

import android.content.Context
import io.netty.util.Constant

class Utils {
    companion object{
        fun isOnCallInterface(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences(Constants.SP_GLOBAL, 0) ?: return false
            return sharedPref.getBoolean(Constants.ON_CALL_INTERFACE, false)
        }

        fun setOnCallInterface(context: Context, value: Boolean) {
            val prefs = context.getSharedPreferences(Constants.SP_GLOBAL, 0)
            val editor = prefs.edit()
            with(editor) {
                putBoolean(Constants.ON_CALL_INTERFACE, value)
            }
            editor.commit()
        }
    }
}