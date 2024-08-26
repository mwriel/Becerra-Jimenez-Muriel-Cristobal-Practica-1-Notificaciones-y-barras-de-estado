package com.example.class2mob2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.mov2class.datamodel.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity() {

    private lateinit var users: MutableList<User>

    private lateinit var user: EditText
    private lateinit var pass: EditText
    private lateinit var login: Button
    private lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear() // Limpia todas las preferencias
        //editor.apply() // Guarda los cambios

        gson = Gson()
        user = findViewById(R.id.tfUser)
        pass = findViewById(R.id.tfPass)
        login = findViewById(R.id.btnLogin)


        val usersJson = sharedPreferences.getString("users", null)
        if (usersJson != null) {
            val userType = object : TypeToken<MutableList<User>>() {}.type
            users = gson.fromJson(usersJson, userType)
        } else {
            users = mutableListOf(
                User("user1", "pass1"),
                User("user2", "pass2"),
                User("user3", "pass3"),
                User("user4", "pass4"),
                User("user5", "pass5")
            )

            val editor = sharedPreferences.edit()
            val usersJson = gson.toJson(users)
            editor.putString("users", usersJson)
            editor.apply()
        }

        login.setOnClickListener {
            val inputUser = user.text.toString()
            val inputPass = pass.text.toString()
            val loggedInUser = users.find { it.name == inputUser && it.pass == inputPass }

            if (loggedInUser != null) {
                val editor = sharedPreferences.edit()
                editor.putString("user", loggedInUser.name)
                editor.apply()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
