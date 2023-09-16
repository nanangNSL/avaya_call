package io.navendra.retrofitkotlindeferred.utils

import android.content.Context
import android.content.SharedPreferences

class LocalStorageHelper(context: Context) : IStorageHelper {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(Constants.STORAGE_NAME_KEY, 0)
    private lateinit var editor: SharedPreferences.Editor

    override fun loadDataStorage(p0: String?): String {
        return this.sharedPreferences.getString(p0, "")!!
    }

    override fun clearDataStorage() {
        this.editor = this.sharedPreferences.edit()
        with(this.editor) {
            clear()
            apply()
        }
    }

    override fun saveDataStorage(p0: String?, p1: String?) {
        this.editor = this.sharedPreferences.edit()
        with(this.editor) {
            putString(p0, p1)
            apply()
        }
    }
}