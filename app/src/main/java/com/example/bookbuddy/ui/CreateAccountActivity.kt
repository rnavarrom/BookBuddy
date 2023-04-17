package com.example.bookbuddy.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookbuddy.Utils.Sha
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.ActivityCreateAccountBinding
import com.example.bookbuddy.models.UserItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    var checkValues: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        binding.CAButtonRegister.setOnClickListener {
            var user = getValues()
            postUser(user)
            Toast.makeText(this, "Insert!!", Toast.LENGTH_LONG).show()
        }
        setContentView(binding.root)
    }

    fun postUser(user: UserItem){
        var newUser = UserItem()
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                crudApi.insert(user) //addUser(user)
            }
            corrutina.join()
        }
       // return newUser
    }

    fun getValues() : UserItem{
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

    /*
    fun CheckName(user : UserItem): Boolean {
        if (!binding.CAEditUser.text.isBlank() ||
            !binding.CAEditPassword.text.isBlank() ||
            !binding.CAEditPassword2.text.isBlank() ||
            !binding.CAEditEmail.text.isBlank()
        ) {
            Toast.makeText(this, "S'ha d'omplir tots els camps", Toast.LENGTH_LONG).show()
            if (binding.CAEditPassword.text.toString()
                    .equals(binding.CAEditPassword2.text.toString())
            ) {

            }
        }
    }

     */

}