package com.example.bookbuddy.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Sha
import com.example.bookbuddy.adapters.LanguageSpinnerAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.api.logging
import com.example.bookbuddy.databinding.ActivityMainBinding
import com.example.bookbuddy.models.Test.User
import com.example.bookbuddy.ui.navdrawer.NavDrawerActivity
import com.example.bookbuddy.utils.*
import com.example.bookbuddy.utils.Tools.Companion.responseToFile
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MainActivity : AppCompatActivity(), ApiErrorListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPrefs: UserPreferences
    private lateinit var savedUser: String
    private lateinit var savedPassword: String
    val api = CrudApi(this@MainActivity)

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
        if (finalCode == "null"){
            finalCode = applicationContext.resources.configuration.locales.get(0).language.toString()
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

    private fun saveLanguageCode(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("language_code", languageCode)
        editor.apply()
    }

    fun getStoredLanguage(): String {
        var sharedPreferences = applicationContext.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        var code = sharedPreferences.getString("language_code", "") ?: ""
        if (code.isNullOrEmpty()){
            code = applicationContext.resources.configuration.locales.get(0).language.toString()
        }
        return code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(binding.root)

        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        var currentLanguageCode = getStoredLanguage()
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
                            saveLanguageCode(applicationContext,"en")
                        }
                        "catalan_flag" -> {
                            setLocal(this@MainActivity, "ca")
                            saveLanguageCode(applicationContext,"ca")
                        }
                        else -> {
                            setLocal(this@MainActivity, "es")
                            saveLanguageCode(applicationContext,"es")
                        }
                    }
                    //recreate()
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finish()
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acciones a realizar cuando no se selecciona ningún elemento
            }
        }

        if (!currentLanguageChanged){
            currentLanguageChanged = true
            setLocal(this@MainActivity, currentLanguageCode)
            //recreate()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        userPrefs = UserPreferences(this)
        lifecycleScope.launch {
            savedUser = userPrefs.userCredentialsFlow.first().first
            savedPassword = userPrefs.userCredentialsFlow.first().second

            if (savedUser.isNotBlank() && savedPassword.isNotBlank()) {
                loadingEndedHome()
            } else {
                loadingEndedLogin()
            }
        }
        var userName = intent.getStringExtra("userName")
        binding.MAEditUser.setText(userName)

        binding.passwordForgor.setOnClickListener {
            /*
            val builder = AlertDialog.Builder(applicationContext)
            val editText = EditText(applicationContext)
            editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            builder.setTitle("Email")
            editText.hint = "Email"

            builder.setView(editText)

            builder.setPositiveButton("Ok") { dialog, which ->
                // Handle "Buscar" button click here
                var email = editText.text.toString()
                if (Tools.isEmailValid(email)){
                    println("TRUE")
                    /*
                    var password = generateRandomPassword(10)
                    var sha = Sha.calculateSHA(password)
                    println(password)
                    sendEmail("fenix6rafa@gmail.com")
                    */
                }


            }

            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            builder.setOnCancelListener(DialogInterface.OnCancelListener {
                // Handle cancel action here
                // This will be triggered when the dialog is canceled
            })

            val dialog = builder.create()
            dialog.show()

            editText.postDelayed({
                editText.requestFocus()
                val imm = applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 200)
            */
            val builder = MaterialAlertDialogBuilder(this)
            /*
            val dialogView = layoutInflater.inflate(R.layout.dialog_layout, binding.activityMain)
            val editText = dialogView.findViewById<EditText>(R.id.editText)
             */
            val editText = EditText(applicationContext)
            editText.hint = "Type here"
            builder.setTitle("Recover password")
                .setView(editText) // Aquí asignamos el diseño personalizado del diálogo que contiene el EditText
                .setPositiveButton("Accept") { dialog, _ ->
                    // Acciones a realizar al hacer clic en "Aceptar"
                    val inputValue = editText.text.toString()
                    var email = inputValue
                    if (Tools.isEmailValid(email)){
                        var emailAviable : Boolean? = isEmailAviable(email)
                        if (emailAviable != null){
                            var password = generateRandomPassword(10)
                            var shaPassword = Sha.calculateSHA(password)
                            sendEmail(email, shaPassword, password)
                        } else {
                            Tools.showSnackBar(applicationContext, binding.activityMain, "Email not exist")
                        }
                    } else {
                        Tools.showSnackBar(applicationContext, binding.activityMain, "Email is not correct")
                    }

                    // Realizar acciones con el valor ingresado en el EditText
                    //dialog.dismiss()
                }
                .show()
        }

        binding.MAButtonLogin.setOnClickListener {
            // Hide keyboard to change fragment
            val inputMethodManager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.MAButtonLogin.windowToken, 0)

            var userName = binding.MAEditUser.text.toString()
            var userPassword = binding.MAEditPassword.text.toString()
            if (userName.isNotBlank() && userPassword.isNotBlank()) {
                getUsers(userName, Sha.calculateSHA(userPassword))
                if (currentUser.userId != -1 || currentUser.userId == null) {
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
                    finish()
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

        val mainLayout = binding.activityMain
        mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                mainLayout.getWindowVisibleDisplayFrame(rect)
                val screenHeight = mainLayout.rootView.height
                val keyboardHeight = screenHeight - rect.bottom
                // Verifica si el teclado está oculto
                if (keyboardHeight < keyboardValue) {
                    binding.MAImage.visibility = View.VISIBLE
                }else{
                    binding.MAImage.visibility = View.GONE
                }
            }
        })
    }

    fun isEmailAviable(email: String): Boolean? {
        var response : Boolean? = false
        runBlocking {
            val crudApi = CrudApi(this@MainActivity)
            val corrutina = launch {
                response = crudApi.getEmailExists(email)
            }
            corrutina.join()
        }
        return response!!
    }

    fun generateRandomPassword(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun sendEmail(email: String, shaPassword: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var api = CrudApi(this@MainActivity)
            var result = api.updateUserPassword(email, shaPassword)

            if (result != null && result){
                val properties = Properties()
                properties["mail.smtp.host"] = "smtp.gmail.com"
                properties["mail.smtp.port"] = "587"
                properties["mail.smtp.auth"] = "true"
                properties["mail.smtp.starttls.enable"] = "true"

                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication("bookbuddyinfo2023@gmail.com", "iqqvymaokpfdukci")
                    }
                })

                try {
                    val message = MimeMessage(session)
                    message.setFrom(InternetAddress("bookbuddyinfo2023@gmail.com"))
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    message.subject = "BookBuddy Password recovery"
                    message.setText("New password: " + password + ". When u enter in the app change immediately the password in profile")
                    Transport.send(message)
                    Tools.showSnackBar(applicationContext, binding.activityMain, "Email sended")
                } catch (e: MessagingException) {
                    Tools.showSnackBar(applicationContext, binding.activityMain, "Error sending email, try again")
                }
            } else {
                Tools.showSnackBar(applicationContext, binding.activityMain, "Error password")
            }
        }
    }

    override fun onApiError() {
        Tools.showSnackBar(this, binding.activityMain, "Can't reach the server. Try again!")
    }

    fun getUsers(userName: String, password: String) {
        currentUser = User()
        runBlocking {            
            val corrutina = launch {                
                var tempData = api.getUserLogin(userName, password)
                if (tempData != null){
                    currentUser = tempData
                }
            }
            corrutina.join()
        }
        if (currentUser.userId > 0) {
            runBlocking {                
                val corrutina = launch {
                    var tempData = api.getProfileUser(currentUser.userId)
                    if(tempData != null){
                        currentProfile = tempData
                    }
                    if (currentUser.haspicture) {
                        responseToFile(applicationContext, api.getUserImage(currentUser.userId))
                    }
                }
                corrutina.join()
            }
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