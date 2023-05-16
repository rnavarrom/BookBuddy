package com.example.bookbuddy.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.example.bookbuddy.Utils.Sha
import com.example.bookbuddy.adapters.LanguageSpinnerAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.api.logging
import com.example.bookbuddy.databinding.ActivityMainBinding
import com.example.bookbuddy.ui.navdrawer.NavDrawerActivity
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.UserPreferences
import com.example.bookbuddy.utils.Tools.Companion.responseToFile
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentProfile
import com.example.bookbuddy.utils.currentUser
//import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity(), ApiErrorListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPrefs: UserPreferences
    private lateinit var savedUser: String
    private lateinit var savedPassword: String
    companion object {
        val USERNAME = stringPreferencesKey("username")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val adapter = LanguageSpinnerAdapter(this, arrayOf("american_flag.xml","catalan_flag.xml","spanish_flag.xml"))
        binding.languageSpinner.adapter = adapter

        userPrefs = UserPreferences(this)
        lifecycleScope.launch {
            savedUser = userPrefs.userCredentialsFlow.first().first
            savedPassword = userPrefs.userCredentialsFlow.first().second

            if (!savedUser.isNullOrBlank() && !savedPassword.isNullOrBlank()) {
                loadingEndedHome()
            } else {
                loadingEndedLogin()
            }
        }
        var userName = intent.getStringExtra("userName")
        binding.MAEditUser.setText(userName)

        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        binding.MAButtonLogin.setOnClickListener {
            var userName = binding.MAEditUser.text.toString()
            var userPassword = binding.MAEditPassword.text.toString()
            if (userName.isNotBlank() && userPassword.isNotBlank()) {

                getUsers(userName, Sha.calculateSHA(userPassword))
                //print("---------------" + currentUser.userId)
                if (currentUser.userId != -1) {
                    if (binding.MACheckBox.isChecked) {
                        val username = "usuario"
                        val password = "contraseña"
                        lifecycleScope.launch {
                            userPrefs.saveCredentials(userName, Sha.calculateSHA(userPassword))
                        }
                    } else {
                        val username = "usuario"
                        val password = "contraseña"
                        lifecycleScope.launch {
                            userPrefs.saveCredentials("", "")
                        }
                    }
                    Toast.makeText(this, "loging in", Toast.LENGTH_LONG).show()
                    var intent = Intent(this, NavDrawerActivity::class.java)
                    startActivity(intent)

                    //}else{
                    //  Toast.makeText(this, "Incorrect user or password",Toast.LENGTH_LONG).show()
                    //}
                } else {
                    Toast.makeText(this, "Incorrect user or password", Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.MAButtonSignIn.setOnClickListener {
            var intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
        binding.passwordToggle.setOnClickListener {
            val editText = binding.MAEditPassword
            Tools.tooglePasswordVisible(editText)
        }
    }

    override fun onApiError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun getUsers(userName: String, password: String) {
        runBlocking {
            val crudApi = CrudApi(this@MainActivity)
            val corrutina = launch {
                currentUser = crudApi.getUserLogin(userName, password)!!
                //currentUser = crudApi.getUserLogin(userName, password, "EEEE")!!
            }
            /*
            val corrutina = launch {
                currentUser = crudApi.getUserLogin(userName, password)
            }
            */
            corrutina.join()
        }
        runBlocking {
            val crudApi = CrudApi(this@MainActivity)
            val corrutina = launch {
                currentProfile = crudApi.getProfileUser(currentUser.userId)!!
                if (currentUser.haspicture) {
                    responseToFile(applicationContext, crudApi.getUserImage(currentUser.userId))
                }
            }
            corrutina.join()
        }
    }

    fun loadingEndedHome() {
        getUsers(savedUser, savedPassword)
        var intent = Intent(this, NavDrawerActivity::class.java)
        startActivity(intent)
    }

    fun loadingEndedLogin() {
        binding.loadingMain.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        userPrefs = UserPreferences(this)
        lifecycleScope.launch {
            savedUser = userPrefs.userCredentialsFlow.first().first
            savedPassword = userPrefs.userCredentialsFlow.first().second

            if (savedUser.isNullOrBlank() && savedPassword.isNullOrBlank()) {
                loadingEndedLogin()
            }
        }
    }
}