package com.example.class2mob2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var comprar: Button
    private lateinit var historial: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        comprar = findViewById(R.id.btnComprar)
        historial = findViewById(R.id.btnHistorial)

        setSupportActionBar(findViewById(R.id.tbMain))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        comprar.setOnClickListener {
            val intent = Intent(this, CatalogoActivity::class.java)
            startActivity(intent)
        }

        historial.setOnClickListener {
            val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("status", 0)
            editor.apply()
            val intent = Intent(this, ComprasActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        }
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }
}
