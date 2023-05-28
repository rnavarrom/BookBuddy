package com.example.bookbuddy.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.LanguageSpinnerAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.api.logging
import com.example.bookbuddy.databinding.ActivityLoginBinding
import com.example.bookbuddy.utils.*
import com.example.bookbuddy.utils.Tools.Companion.generateRandomPassword
import com.example.bookbuddy.utils.Tools.Companion.responseToFile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Activity to log in the application
 */
class LoginActivity : AppCompatActivity(), ApiErrorListener {
    private val api = CrudApi(this@LoginActivity)
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPrefs: UserPreferences
    private lateinit var savedUser: String
    private lateinit var savedPassword: String
    private val guideLineMin = 0.23F
    private val guideLineMax = 0.35F

    private var connectionError = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(binding.root)

        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val currentLanguageCode = getStoredLanguage()
        val curr = getCurrentLanguageCode(currentLanguageCode)
        val languages = arrayOf("american_flag", "catalan_flag")
        val adapter = LanguageSpinnerAdapter(this, languages)
        binding.languageSpinner.adapter = adapter
        val position = languages.indexOf(curr)
        binding.languageSpinner.setSelection(position)
        val lastSelectedPosition = position

        binding.languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != lastSelectedPosition) {
                        val selectedImageName = parent.getItemAtPosition(position).toString()
                        when (selectedImageName) {
                            "american_flag" -> {
                                setLocal(this@LoginActivity, "en")
                                saveLanguageCode(applicationContext, "en")
                            }
                            "catalan_flag" -> {
                                setLocal(this@LoginActivity, "ca")
                                saveLanguageCode(applicationContext, "ca")
                            }
                        }

                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

        if (!currentLanguageChanged) {
            currentLanguageChanged = true
            setLocal(this@LoginActivity, currentLanguageCode)
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        //Check if there is values for auto login
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
        val userName = intent.getStringExtra("userName")
        binding.MAEditUser.setText(userName)

        //Make a password forgot request
        binding.passwordForgor.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            val editText = EditText(applicationContext)
            editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            editText.hint = getString(R.string.LAY_HintEnterAcountEmail)
            builder.setTitle(getString(R.string.MSG_RecoverPassword))
                .setView(editText)
                .setPositiveButton(getString(R.string.BT_Accept)) { _, _ ->
                    val inputValue = editText.text.toString()
                    if (Tools.isEmailValid(inputValue)) {
                        val emailAviable: Boolean? = isEmailAviable(inputValue)
                        if (emailAviable != null) {
                            val password = generateRandomPassword(10)
                            val shaPassword = Sha.calculateSHA(password)
                            sendEmail(inputValue, shaPassword, password)
                        } else {
                            Tools.showSnackBar(
                                applicationContext,
                                binding.activityMain,
                                getString(R.string.SB_EmailNotExist)
                            )
                        }
                    } else {
                        Tools.showSnackBar(
                            applicationContext,
                            binding.activityMain,
                            getString(R.string.SB_EmailNotCorrect)
                        )
                    }

                }
                .show()
        }

        binding.MAButtonLogin.setOnClickListener {
            // Hide keyboard to change fragment
            val inputMethodManager =
                applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.MAButtonLogin.windowToken, 0)

            val userName = binding.MAEditUser.text.toString().trim()
            val userPassword = binding.MAEditPassword.text.toString()
            if (userName.isNotBlank() && userPassword.isNotBlank()) {
                getUsers(userName, Sha.calculateSHA(userPassword))
                if (!checkConnectionFailed()) {
                    if (currentUser != null) {
                        if (binding.MACheckBox.isChecked) {
                            lifecycleScope.launch {
                                userPrefs.saveCredentials(userName, Sha.calculateSHA(userPassword))
                            }
                        } else {
                            lifecycleScope.launch {
                                userPrefs.saveCredentials("", "")
                            }
                        }
                        Tools.showSnackBar(
                            applicationContext,
                            binding.activityMain,
                            getString(R.string.SB_LogIn)
                        )

                        val intent = Intent(this, NavDrawerActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Tools.showSnackBar(
                            applicationContext,
                            binding.activityMain,
                            getString(R.string.SB_IncorrectUserPass)
                        )
                    }
                }
            }
        }
        binding.MAButtonSignIn.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
        binding.passwordToggle.setOnClickListener {
            val editText = binding.MAEditPassword
            Tools.tooglePasswordVisible(editText)
        }

        val mainLayout = binding.activityMain
        mainLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            mainLayout.getWindowVisibleDisplayFrame(rect)
            val screenHeight = mainLayout.rootView.height
            val keyboardHeight = screenHeight - rect.bottom
            // Checks if the keyboard is hidden
            if (keyboardHeight < keyboardValue) {
                binding.MAImage.visibility = View.VISIBLE
                binding.guideLine.visibility = View.VISIBLE
                binding.guideLine.setGuidelinePercent(guideLineMax)
            } else {
                binding.MAImage.visibility = View.GONE
                binding.guideLine.visibility = View.GONE
                binding.guideLine.setGuidelinePercent(guideLineMin)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        userPrefs = UserPreferences(this)
        lifecycleScope.launch {
            val credentials = userPrefs.userCredentialsFlow.first()
            savedUser = credentials.first
            savedPassword = credentials.second

            if (savedUser.isBlank() && savedPassword.isBlank()) {
                loadingEndedLogin()
            }
        }
    }

    private fun isEmailAviable(email: String): Boolean? {
        var response: Boolean? = false
        runBlocking {
            val coroutine = launch {
                response = api.getEmailExists(email)
            }
            coroutine.join()
        }
        return response!!
    }

    //Send email with new passwrd
    private fun sendEmail(email: String, shaPassword: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = api.updateUserPasswordMail(email, shaPassword)
            if (result != null && result) {
                val properties = Properties()
                properties["mail.smtp.host"] = "smtp.gmail.com"
                properties["mail.smtp.port"] = "587"
                properties["mail.smtp.auth"] = "true"
                properties["mail.smtp.starttls.enable"] = "true"
                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(
                            getString(R.string.AppEmail),
                            getString(R.string.AppEmailCode)
                        )
                    }
                })
                try {
                    val message = MimeMessage(session)
                    message.setFrom(InternetAddress(getString(R.string.AppEmail)))
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    message.subject = getString(R.string.MSG_PassRecovery)
                    message.setText(getString(R.string.MSG_NewPass) + password + getString(R.string.MSG_NewPassWarning))
                    Transport.send(message)
                    Tools.showSnackBar(
                        applicationContext,
                        binding.activityMain,
                        getString(R.string.SB_EmailSend)
                    )
                } catch (e: MessagingException) {
                    Tools.showSnackBar(
                        applicationContext,
                        binding.activityMain,
                        getString(R.string.SB_EmailSendError)
                    )
                }
            } else {
                Tools.showSnackBar(
                    applicationContext,
                    binding.activityMain,
                    getString(R.string.SB_PassError)
                )
            }
        }
    }

    private fun getUsers(userName: String, password: String) {
        runBlocking {
            val coroutine = launch {
                val tempData = api.getUserLogin(userName, password)
                if (tempData != null) {
                    currentUser = tempData
                }
            }
            coroutine.join()
        }
        if (currentUser != null) {
            runBlocking {
                val coroutine = launch {
                    val tempData = api.getProfileUser(currentUser!!.userId)
                    if (tempData != null) {
                        currentProfile = tempData
                    }
                    if (currentUser?.haspicture == true) {
                        responseToFile(applicationContext, api.getUserImage(currentUser!!.userId))
                    }
                }
                coroutine.join()
            }
        }
    }

    private fun setLocal(activity: Activity, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun getCurrentLanguageCode(code: String): String {
        var finalCode: String = code
        if (finalCode == "null") {
            finalCode =
                applicationContext.resources.configuration.locales.get(0).language.toString()
        }
        return when (finalCode) {
            "en" -> {
                "american_flag"
            }
            "ca" -> {
                "catalan_flag"
            }
            else -> {
                "american_flag"
            }
        }
    }

    private fun saveLanguageCode(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("language_code", languageCode)
        editor.apply()
    }

    private fun getStoredLanguage(): String {
        val sharedPreferences =
            applicationContext.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        var code = sharedPreferences.getString("language_code", "") ?: ""
        if (code.isEmpty()) {
            code = applicationContext.resources.configuration.locales.get(0).language.toString()
        }
        return code
    }

    private fun loadingEndedHome() {
        getUsers(savedUser, savedPassword)
        if (currentUser != null) {
            val intent = Intent(this, NavDrawerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadingEndedLogin() {
        binding.loadingMain.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
    }

    private fun checkConnectionFailed(): Boolean {
        if (connectionError) {
            connectionError = false
            return true
        }
        return false
    }

    override fun onApiError(connectionFailed: Boolean) {
        if (connectionFailed) {
            binding.loadingMain.visibility = View.GONE
            connectionError = true
            Tools.showSnackBar(this, binding.activityMain, Constants.ErrrorMessage)
        }
    }
}