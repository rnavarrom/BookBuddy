package com.example.bookbuddy.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.example.bookbuddy.Utils.Sha
import com.example.bookbuddy.adapters.LanguageSpinnerAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.api.logging
import com.example.bookbuddy.databinding.ActivityMainBinding
import com.example.bookbuddy.ui.navdrawer.NavDrawerActivity
import com.example.bookbuddy.utils.*
import com.example.bookbuddy.utils.Tools.Companion.responseToFile
import com.example.bookbuddy.utils.base.ApiErrorListener
//import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.logging.HttpLoggingInterceptor
import java.util.Locale

class MainActivity : AppCompatActivity(), ApiErrorListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPrefs: UserPreferences
    private lateinit var savedUser: String
    private lateinit var savedPassword: String

    companion object {
        val USERNAME = stringPreferencesKey("username")
    }

    fun setLocal(activity: Activity, langCode: String){
        var locale: Locale = Locale(langCode)
        Locale.setDefault(locale)
        var resources = activity.resources
        var config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun getCurrentLanguageCode(code: String): String {
        var finalCode: String = code
        if (code == "null"){
            var curLocal = resources.configuration.locales.get(0)
            finalCode = curLocal.language
        }
        when (finalCode){
            "en" -> {
                return "american_flag"
            }
            "ca" -> {
                return "catalan_flag"
            }
            "es" -> {
                return "spanish_flag"
            }
            else -> {
                return "american_flag"
            }
        }
    }

    fun saveLanguageCode(languageCode: String) {
        lifecycleScope.launch {
            userPrefs.saveLanguageCredentials(languageCode)
        }
    }

    fun getCurrentLanguageCode2(): String {
        userPrefs = UserPreferences(this)
        var savedLanguageCode: String = ""
        lifecycleScope.launch {
            //savedLanguageCode = userPrefs.userLanguageCredentialsFlow
            println("AAAAAAAAAAAAAAAAAAAAA")
            println(savedLanguageCode)
        }
        return savedLanguageCode
        /*
        var sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        println("HOLAAA")
        println(sharedPreferences.getString("language_code", "en"))
        return sharedPreferences.getString("language_code", "null") ?: "null"

         */
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var currentLanguageCode = getCurrentLanguageCode2()

        var curr = getCurrentLanguageCode(currentLanguageCode)
        val languages = arrayOf("american_flag","catalan_flag","spanish_flag")
        val adapter = LanguageSpinnerAdapter(this, languages)
        binding.languageSpinner.adapter = adapter
        var position = languages.indexOf(curr)
        binding.languageSpinner.setSelection(position)
        var lastSelectedPosition = position

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != lastSelectedPosition) {
                    val selectedImageName = parent.getItemAtPosition(position).toString()
                    println(selectedImageName)
                    when (selectedImageName){
                        "american_flag" -> {
                            setLocal(this@MainActivity, "en")
                            saveLanguageCode("en")
                        }
                        "catalan_flag" -> {
                            setLocal(this@MainActivity, "ca")
                            saveLanguageCode("ca")
                        }
                        else -> {
                            setLocal(this@MainActivity, "es")
                            saveLanguageCode("es")
                        }
                    }
                    startActivity(intent)
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acciones a realizar cuando no se selecciona ningún elemento
            }
        }

        if (!currentLanguageChanged){
            currentLanguageChanged = true
            setLocal(this@MainActivity, currentLanguageCode)
            startActivity(intent)
            //recreate()
        }


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
            var credentials = userPrefs.userCredentialsFlow.first()
            savedUser = credentials.first
            savedPassword = credentials.second

            if (savedUser.isNullOrBlank() && savedPassword.isNullOrBlank()) {
                loadingEndedLogin()
            }
        }
    }
}