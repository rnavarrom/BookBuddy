package com.example.bookbuddy.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Sha
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.ActivityCreateAccountBinding
import com.example.bookbuddy.models.Response
import com.example.bookbuddy.models.Test.Pending
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.unaccent
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.currentUserCreate
import com.example.bookbuddy.utils.keyboardValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CreateAccountActivity : AppCompatActivity(), ApiErrorListener {
    private lateinit var binding: ActivityCreateAccountBinding
    var checkValues: Boolean = false
    lateinit var user: UserItem
    var conditions = false
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val mainLayout = binding.createAcountLayout
        mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                mainLayout.getWindowVisibleDisplayFrame(rect)
                val screenHeight = mainLayout.rootView.height
                val keyboardHeight = screenHeight - rect.bottom
                println(keyboardHeight.toString() + "------------------")
                // Verifica si el teclado está oculto
                if (keyboardHeight < keyboardValue) {
                    binding.CAImage.visibility = View.VISIBLE
                } else {
                    binding.CAImage.visibility = View.GONE
                }
            }
        })

        binding.CAButtonRegister.setOnClickListener {
            //checkValues = CheckFields()
            if (CheckFields()) {
                currentUserCreate = UserItem()
                getValues()

            var success = postUser(currentUserCreate)
            if (success) {
                Toast.makeText(this, "Acount created!", Toast.LENGTH_LONG).show()
                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra("userName", currentUser.name)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Acount not created!", Toast.LENGTH_LONG).show()
            }

            }
        }

        binding.passwordToggle1.setOnClickListener {
            Tools.tooglePasswordVisible(binding.CAEditPassword)
        }
        binding.passwordToggle2.setOnClickListener {
            Tools.tooglePasswordVisible(binding.CAEditPassword2)
        }
        binding.userConditions.setOnClickListener {
            UserConditions()
        }
        setContentView(binding.root)
    }

    fun postUser(user: UserItem): Boolean {
        var response : Boolean? = false
        runBlocking {
            val crudApi = CrudApi(this@CreateAccountActivity)
            val corrutina = launch {
                response = crudApi.addUserToAPI(user, "Server not working")
            }
            corrutina.join()
        }
        if(response != null){
            return response!!
        }else{
            return false
        }
    }

    fun getValues() {
        currentUserCreate.name = binding.CAEditUser.text.toString()
        currentUserCreate.password = Sha.calculateSHA(binding.CAEditPassword.text.toString())
        currentUserCreate.email = binding.CAEditEmail.text.toString()
    }

    @SuppressLint("ResourceAsColor")
    fun CheckFields(): Boolean {

        ResetColors()
        //Check if all the fields are not blank
        if (binding.CAEditUser.text.isBlank() ||
            binding.CAEditPassword.text.isBlank() ||
            binding.CAEditPassword2.text.isBlank() ||
            binding.CAEditEmail.text.isBlank()
        ) {
            Toast.makeText(this, "Can't be blank fields!", Toast.LENGTH_LONG).show()
            return false
        }
        //Check if the terms and conditions are accepted
        if (!conditions) {
            binding.userConditions.setTextColor(getColor(R.color.red_error))
            Toast.makeText(this, "You need to read and accept user conditions", Toast.LENGTH_LONG)
                .show()
            return false
        }
        //Check if the input text is valid
        if (!Tools.isValidText(binding.CAEditUser.text.toString())) {
            binding.CAEditUser.setTextColor(getColor(R.color.red_error))
            Toast.makeText(this, "Invalid user name!", Toast.LENGTH_LONG).show()
            return false
        }
        //Check if the two passwrod fields are equal
        if (binding.CAEditPassword.text.toString() != binding.CAEditPassword2.text.toString()
        ) {
            binding.CAEditPassword.setTextColor(getColor(R.color.red_error))
            Toast.makeText(this, "Passwords need to be the same!", Toast.LENGTH_LONG).show()
            return false
        }
        //check if the email has a valid formation
        if (!Tools.isEmailValid(binding.CAEditEmail.text.toString())) {
            binding.CAEditEmail.setTextColor(getColor(R.color.red_error))
            Toast.makeText(this, "Invalid email!", Toast.LENGTH_LONG).show()
            return false
        }
        //Check if the username is not repited in the DB
        var userNameAviable : Boolean? = isNameAviable(binding.CAEditUser.text.toString())
        if (userNameAviable == null) {
            return false
        }else if(!userNameAviable!!){
            binding.CAEditUser.setTextColor(getColor(R.color.red_error))
            Toast.makeText(this, "User name already in use!", Toast.LENGTH_LONG).show()
            return false
        }
        //Chgeck if the email is not repeated in the DB
        var emailAviable : Boolean? = isEmailAviable(binding.CAEditEmail.text.toString())
        if (emailAviable == null) {
            return false
        }else if(!emailAviable){
            binding.CAEditEmail.setTextColor(getColor(R.color.red_error))
            Toast.makeText(this, "Email already used!", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
    fun isNameAviable(userName: String): Boolean? {
        var response : Boolean? = false
        runBlocking {
            val crudApi = CrudApi(this@CreateAccountActivity)
            val corrutina = launch {
                response = crudApi.getUserExists(userName, "Pruena!")
            }
            corrutina.join()
        }
        return response
    }
    fun isEmailAviable(email: String): Boolean? {
        var response : Boolean? = false
        runBlocking {
            val crudApi = CrudApi(this@CreateAccountActivity)
            val corrutina = launch {
                response = crudApi.getEmailExists(email, "Email already exists")
            }
            corrutina.join()
        }
        return response!!
    }
    fun UserConditions() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_user_conditions, null)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Accept") { dialogInterface, i ->
            conditions = true
            ResetColors()
        }
        builder.setNegativeButton("Cancel") { dialogInterface, i ->
            conditions = false
            ResetColors()
        }
        builder.show()
    }

    fun ResetColors() {
        binding.CAEditUser.setTextColor(getColor(R.color.black))
        binding.CAEditEmail.setTextColor(getColor(R.color.black))
        binding.CAEditPassword.setTextColor(getColor(R.color.black))
        binding.CAEditPassword2.setTextColor(getColor(R.color.black))
        binding.userConditions.setTextColor(getColor(R.color.black))
    }

    override fun onApiError(errorMessage: String) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
