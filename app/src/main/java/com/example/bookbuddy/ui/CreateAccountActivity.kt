package com.example.bookbuddy.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookbuddy.Utils.Sha
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.ActivityCreateAccountBinding
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.currentUserCreate
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    var checkValues: Boolean = false
    lateinit var user: UserItem
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        binding.CAButtonRegister.setOnClickListener {

            checkValues = CheckFields()
            if(checkValues){
            currentUserCreate = UserItem()
            getValues()

            var success = postUser(currentUserCreate)
            if (success) {
                Toast.makeText(this, "Acount created!", Toast.LENGTH_LONG).show()
                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra("userName", currentUser.name)
                startActivity(intent)
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
        setContentView(binding.root)
    }

    fun postUser(user: UserItem): Boolean {
        var response = false
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                response = crudApi.addUserToAPI(user)
            }
            corrutina.join()
        }
        return response
    }

    fun getValues() {
        currentUserCreate.name = binding.CAEditUser.text.toString()
        currentUserCreate.password = Sha.calculateSHA(binding.CAEditPassword.text.toString())
        currentUserCreate.email = binding.CAEditEmail.text.toString()
    }

    fun CheckFields(): Boolean {
        if (binding.CAEditUser.text.isBlank() ||
            binding.CAEditPassword.text.isBlank() ||
            binding.CAEditPassword2.text.isBlank() ||
            binding.CAEditEmail.text.isBlank()
        ) {
            Toast.makeText(this, "Can't be blank fields!", Toast.LENGTH_LONG).show()
            return false
        }
        if (binding.CAEditPassword.text.toString() != binding.CAEditPassword2.text.toString()
        ) {
            Toast.makeText(this, "Passwords need to be the same!", Toast.LENGTH_LONG).show()
            return false
        }
        if (!Tools.isEmailValid(binding.CAEditEmail.text.toString())) {
            Toast.makeText(this, "Invalid email!", Toast.LENGTH_LONG).show()
            return false
        }
        if (Tools.isNameAviable(binding.CAEditUser.text.toString())) {
            Toast.makeText(this, "User name already in use!", Toast.LENGTH_LONG).show()
            return false
        }
        if (Tools.isEmailAviable(binding.CAEditEmail.text.toString())) {
            Toast.makeText(this, "Email already used!", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}
