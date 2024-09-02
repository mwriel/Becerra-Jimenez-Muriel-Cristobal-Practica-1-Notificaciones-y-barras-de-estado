package com.example.class2mob2

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.core.content.getSystemService
import java.util.Calendar



class MainActivity : AppCompatActivity() {

    private lateinit var comprar: Button
    private lateinit var historial: Button
    private lateinit var alarm:Button


    private lateinit var intent: Intent
    private lateinit var pendingIntent: PendingIntent
    private lateinit var alarmMgr: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        comprar = findViewById(R.id.btnComprar)
        historial = findViewById(R.id.btnHistorial)
        alarm=findViewById(R.id.btnAlarm)

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

        intent= Intent(this,PendingActivity::class.java).apply{
            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        pendingIntent=PendingIntent.getActivity(this,0,intent,
            PendingIntent.FLAG_IMMUTABLE)

        alarmMgr=(applicationContext.getSystemService(Context.ALARM_SERVICE)as? AlarmManager)!!

        alarm.setOnClickListener{
            alarmTT2()
        }


    }

    private fun alarmTT2(){
        alarmMgr.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime()+10*500,
            pendingIntent
        )
        Toast.makeText(applicationContext,"enseguida se le avisara los informes.", Toast.LENGTH_SHORT)
            .show()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        }
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }
}
