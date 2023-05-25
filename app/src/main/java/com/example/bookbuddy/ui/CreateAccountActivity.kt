package com.example.bookbuddy.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.Utils.Sha
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.ActivityCreateAccountBinding
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUserCreate
import com.example.bookbuddy.utils.keyboardValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CreateAccountActivity : AppCompatActivity(), ApiErrorListener {
    private lateinit var binding: ActivityCreateAccountBinding
    private val api = CrudApi(this@CreateAccountActivity)
    var checkValues: Boolean = false
    lateinit var user: UserItem
    private var conditions = false
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val mainLayout = binding.createAcountLayout
        mainLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            mainLayout.getWindowVisibleDisplayFrame(rect)
            val screenHeight = mainLayout.rootView.height
            val keyboardHeight = screenHeight - rect.bottom
            // Verifica si el teclado está oculto
            if (keyboardHeight < keyboardValue) {
                binding.CAImage.visibility = View.VISIBLE
            } else {
                binding.CAImage.visibility = View.GONE
            }
        }

        binding.CAButtonRegister.setOnClickListener {
            //checkValues = CheckFields()
            if (checkFields()) {
                currentUserCreate = UserItem()
                getValues()

                val success = postUser(currentUserCreate)
                if (success) {
                    Tools.showSnackBar(
                        this,
                        binding.createAcountLayout,
                        getString(R.string.SB_AccountCreated)
                    )
                    //Toast.makeText(this, , Toast.LENGTH_LONG).show()
                    var intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("userName", currentUserCreate.name)
                    startActivity(intent)
                    finish()
                } else {
                    Tools.showSnackBar(
                        this,
                        binding.createAcountLayout,
                        getString(R.string.SB_AccountNotCreated)
                    )
                    //Toast.makeText(this, "Acount not created!", Toast.LENGTH_LONG).show()
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
            userConditions()
        }
        setContentView(binding.root)
    }

    private fun postUser(user: UserItem): Boolean {
        var response: Boolean? = false
        runBlocking {
            val corrutina = launch {
                response = api.addUserToAPI(user)
            }
            corrutina.join()
        }
        return if (response != null) {
            response!!
        } else {
            false
        }
    }

    private fun getValues() {
        currentUserCreate.name = binding.CAEditUser.text.toString()
        currentUserCreate.password = Sha.calculateSHA(binding.CAEditPassword.text.toString())
        currentUserCreate.email = binding.CAEditEmail.text.toString()
    }

    @SuppressLint("ResourceAsColor")
    private fun checkFields(): Boolean {

        resetColors()
        //Check if all the fields are not blank
        if (binding.CAEditUser.text.isBlank() ||
            binding.CAEditPassword.text.isBlank() ||
            binding.CAEditPassword2.text.isBlank() ||
            binding.CAEditEmail.text.isBlank()
        ) {
            Tools.showSnackBar(
                this,
                binding.createAcountLayout,
                getString(R.string.SB_NoBlankFields)
            )
            //Toast.makeText(this, , Toast.LENGTH_LONG).show()
            return false
        }
        //Check if the terms and conditions are accepted
        if (!conditions) {
            binding.userConditions.setTextColor(getColor(R.color.red_error))
            Tools.showSnackBar(
                this,
                binding.createAcountLayout,
                getString(R.string.SB_UserConditions)
            )
            //Toast.makeText(this, , Toast.LENGTH_LONG).show()
            return false
        }
        //Check if the input text is valid
        if (!Tools.isValidText(binding.CAEditUser.text.toString())) {
            binding.CAEditUser.setTextColor(getColor(R.color.red_error))
            Tools.showSnackBar(
                this,
                binding.createAcountLayout,
                getString(R.string.SB_InvalidUserName)
            )
            //Toast.makeText(this, "Invalid user name!", Toast.LENGTH_LONG).show()
            return false
        }
        //Check if the two passwrod fields are equal
        if (binding.CAEditPassword.text.toString() != binding.CAEditPassword2.text.toString()
        ) {
            binding.CAEditPassword.setTextColor(getColor(R.color.red_error))
            Tools.showSnackBar(
                this,
                binding.createAcountLayout,
                getString(R.string.SB_PasswordNoMatch)
            )
            //Toast.makeText(this, , Toast.LENGTH_LONG).show()
            return false
        }
        //check if the email has a valid formation
        if (!Tools.isEmailValid(binding.CAEditEmail.text.toString())) {
            binding.CAEditEmail.setTextColor(getColor(R.color.red_error))
            Tools.showSnackBar(this, binding.createAcountLayout, getString(R.string.InvalidEmail))
            //Toast.makeText(this, , Toast.LENGTH_LONG).show()
            return false
        }
        //Check if the username is not repited in the DB
        val userNameAviable: Boolean? = isNameAviable(binding.CAEditUser.text.toString())

        if (userNameAviable == null) {
            return false
        } else if (!userNameAviable) {
            binding.CAEditUser.setTextColor(getColor(R.color.red_error))
            Tools.showSnackBar(
                this,
                binding.createAcountLayout,
                getString(R.string.SB_UserNameInUse)
            )
            return false
        }
        //Chgeck if the email is not repeated in the DB
        val emailAviable: Boolean? = isEmailAviable(binding.CAEditEmail.text.toString())
        if (emailAviable == null) {
            return false
        } else if (!emailAviable) {
            binding.CAEditEmail.setTextColor(getColor(R.color.red_error))
            Tools.showSnackBar(this, binding.createAcountLayout, getString(R.string.SB_EmailUsed))
            // Toast.makeText(this, , Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun isNameAviable(userName: String): Boolean? {
        var response: Boolean? = false
        runBlocking {
            val corrutina = launch {
                response = api.getUserExists(userName)
            }
            corrutina.join()
        }
        return response
    }

    private fun isEmailAviable(email: String): Boolean? {
        var response: Boolean? = false
        runBlocking {
            val corrutina = launch {
                response = api.getEmailExists(email)
            }
            corrutina.join()
        }
        return response!!
    }

    private fun userConditions() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_user_conditions, null)
        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.DG_Accept)) { dialogInterface, i ->
            conditions = true
            resetColors()
        }
        builder.setNegativeButton(getString(R.string.BT_Cancel)) { dialogInterface, i ->
            conditions = false
            resetColors()
        }
        builder.show()
    }

    private fun resetColors() {
        binding.CAEditUser.setTextColor(getColor(R.color.black))
        binding.CAEditEmail.setTextColor(getColor(R.color.black))
        binding.CAEditPassword.setTextColor(getColor(R.color.black))
        binding.CAEditPassword2.setTextColor(getColor(R.color.black))
        binding.userConditions.setTextColor(getColor(R.color.black))
    }

    override fun onApiError() {
        Tools.showSnackBar(this, binding.createAcountLayout, Constants.ErrrorMessage)
    }
}
