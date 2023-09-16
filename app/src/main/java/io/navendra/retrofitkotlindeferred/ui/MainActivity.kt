package io.navendra.retrofitkotlindeferred.ui

import android.app.PendingIntent.getActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import io.navendra.retrofitkotlindeferred.R
import io.navendra.retrofitkotlindeferred.data.CallRequest
import io.navendra.retrofitkotlindeferred.service.ApiFactory
import io.navendra.retrofitkotlindeferred.utils.CallAgentService
import io.navendra.retrofitkotlindeferred.utils.Constants
import io.navendra.retrofitkotlindeferred.utils.LocalStorageHelper
import io.navendra.retrofitkotlindeferred.utils.Utils
import io.netty.util.Constant
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.UUID

class MainActivity : AppCompatActivity() {

    var MainActivity:AppCompatImageView? = null
    var btnCallVideo:AppCompatImageView? = null
    var btnCallAudio:AppCompatImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCallAudio = findViewById(R.id.btn_call)
        btnCallVideo = findViewById(R.id.btn_video_call)

        btnCallAudio?.setOnClickListener {  getEncryptedToken() }
        btnCallVideo?.setOnClickListener {
            Toast.makeText(this, "Layanan ini sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }

        if (Utils.isOnCallInterface(this)) { //(CallAgentService.isServiceRunning()) {
            try {
                val agentIntent = CallAgentService.getAgentIntent()
                ContextCompat.startActivity(this,agentIntent,null)
            } catch (e: Exception) {
                Utils.setOnCallInterface(this, false)

            }
        }



    }

    private fun getEncryptedToken() {
        val tokenService = ApiFactory.retrofitApi


        //Getting Posts from Jsonplaceholder API
        GlobalScope.launch(Dispatchers.Main) {
            val postRequest = tokenService.getAccesToken(
                CallRequest(
                    UUID.randomUUID().toString().subSequence(0,5).toString(),
                    "3213",
                    "08161305150",
                    "Testapp",
                    "120000"
                )
            )
            try {
                val response = postRequest.await()
                if (response.isSuccessful) {
                    val token = response.body()?.encryptedToken
                    Log.d("success ", "token : " + token.toString())

                    if (token != null){
                        moveToInCallActivity(token,"3234","Testapp")
                    }


                } else {
                    Log.d("MainActivity ", response.errorBody().toString())
                    Toast.makeText(this@MainActivity, "${response.errorBody()}", Toast.LENGTH_SHORT)
                        .show()
                }

            } catch (e: Exception) {
                Log.d("MainActivity ", e.message.toString())
                Toast.makeText(this@MainActivity, "${e.message.toString()}", Toast.LENGTH_SHORT)
                    .show()

            }
        }

    }

    private fun moveToInCallActivity(token: String, calle  : String, username: String) {
        try {
//            mLogger.d("Moving to interaction activity")
            val intent = Intent (this, CallActivity::class.java)
            intent.putExtra(Constants.DATA_KEY_TOKEN, token)
            intent.putExtra(Constants.DATA_KEY_DISPLAYNAME, username)
            intent.putExtra(Constants.DATA_KEY_USERNAME, calle)
            intent.putExtra(Constants.KEY_CONTEXT, UUID.randomUUID().toString().subSequence(0,7).toString())
            intent.putExtra(Constants.KEY_NUMBER_TO_DIAL, calle)

            //saved to preadolescence
            val localStorageHelper = LocalStorageHelper(this)
            localStorageHelper.saveDataStorage(Constants.DATA_KEY_TOKEN, token)
            localStorageHelper.saveDataStorage(Constants.DATA_KEY_DISPLAYNAME,username)
            localStorageHelper.saveDataStorage(Constants.DATA_KEY_USERNAME, username)
            localStorageHelper.saveDataStorage(Constants.KEY_CONTEXT, UUID.randomUUID().toString().subSequence(0,7).toString())
            localStorageHelper.saveDataStorage(Constants.KEY_NUMBER_TO_DIAL, calle)


//            addExtraIntent(intent)
            startActivity(intent)
        } catch (ex: Exception) {
            Log.d("intent error","Error moving to interaction activity " + ex.message, ex)
        }
    }


}
