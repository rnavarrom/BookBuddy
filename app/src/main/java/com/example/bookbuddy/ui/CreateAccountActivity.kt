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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    var checkValues: Boolean = false
    lateinit var user : UserItem
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        binding.CAButtonRegister.setOnClickListener {

            checkValues = CheckFields()
            if(checkValues){
                user = getValues()
                postUser(user)
                Toast.makeText(this, "Acount created!", Toast.LENGTH_LONG).show()
                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra("userName", user.name)
                startActivity(intent)
            }
        }
        setContentView(binding.root)
    }

    fun postUser(user: UserItem) {
        var response = false
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                crudApi.addUserToAPI(user) //insert(user) //addUser(user)
            }
            corrutina.join()
        }
    }

    fun getValues(): UserItem {
        var user = UserItem(
            emptyList(),
            emptyList(),
            "",
            emptyList(),
            emptyList(),
            false,
            binding.CAEditUser.text.toString(),
            Sha.calculateSHA(binding.CAEditPassword.text.toString()),
            emptyList(),
            emptyList(),
            0,
            binding.CAEditEmail.text.toString()
        )
        return user
    }

    fun CheckFields(): Boolean {
        if (!binding.CAEditUser.text.isBlank() &&
            !binding.CAEditPassword.text.isBlank() &&
            !binding.CAEditPassword2.text.isBlank() &&
            !binding.CAEditEmail.text.isBlank()
        ) {
            if (binding.CAEditPassword.text.toString().equals(binding.CAEditPassword2.text.toString())) {
                if (Tools.isEmailValid(binding.CAEditEmail.text.toString())) {
                    if(!Tools.isEmailAviable(binding.CAEditEmail.text.toString())){
                        if(!Tools.isNameAviable(binding.CAEditUser.text.toString())){
                            return true
                        }else{
                            Toast.makeText(this, "User name already in use!", Toast.LENGTH_LONG).show()
                            return false
                        }
                    }else{
                        Toast.makeText(this, "Email is already in use!", Toast.LENGTH_LONG).show()
                        return false
                    }
                } else {
                    Toast.makeText(this, "Incorrect email!", Toast.LENGTH_LONG).show()
                    return false
                }
            } else {
                Toast.makeText(this, "Passwords are not equal!", Toast.LENGTH_LONG).show()
                return false
            }
        } else {
            Toast.makeText(this, "There are empty fields!", Toast.LENGTH_LONG).show()
            return false
        }
    }
}