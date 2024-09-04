package com.example.class2mob2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mov2class.datamodel.User
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity() {

    private lateinit var users: MutableList<User>
    private lateinit var user: EditText
    private lateinit var pass: EditText
    private lateinit var login: Button
    private lateinit var gson: Gson

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
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
            val usersJson = gson.toJson(users)
            with(sharedPreferences.edit()) {
                putString("users", usersJson)
                apply()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permiso ya concedido
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(this, "Se necesita permiso para mostrar notificaciones", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Token: $token")
        }

        login.setOnClickListener {
            val inputUser = user.text.toString()
            val inputPass = pass.text.toString()
            val loggedInUser = users.find { it.name == inputUser && it.pass == inputPass }

            if (loggedInUser != null) {
                with(sharedPreferences.edit()) {
                    putString("user", loggedInUser.name)
                    apply()
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
