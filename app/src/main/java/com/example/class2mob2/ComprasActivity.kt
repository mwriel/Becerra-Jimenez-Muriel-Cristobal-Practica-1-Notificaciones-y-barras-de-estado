package com.example.class2mob2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mov2class.datamodel.Sale
import com.example.mov2class.datamodel.User
import com.example.mov2class.datamodel.Ticket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ComprasActivity : AppCompatActivity() {

    private lateinit var infoCompras: TextView
    private lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compras)
        infoCompras = findViewById(R.id.tvCompras)

        setSupportActionBar(findViewById(R.id.tbTicket))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        gson = Gson()

        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val currentTicketId = sharedPreferences.getInt("thisTicket", -1)
        val status = sharedPreferences.getInt("status", 0)
        if (status==1){
            if (currentTicketId != -1) {
                val salesJson = sharedPreferences.getString("sales", null)
                if (salesJson != null) {
                    val saleType = object : TypeToken<MutableList<Sale>>() {}.type
                    val sales = gson.fromJson<MutableList<Sale>>(salesJson, saleType)
                    val relatedSales = sales.filter { it.ticketid == currentTicketId }

                    if (relatedSales.isNotEmpty()) {
                        infoCompras.text = relatedSales.joinToString("\n") {
                            "Producto: ${it.product.name}, Cantidad: ${it.amount}, Precio: ${it.product.price}"
                        }
                    } else {
                        infoCompras.text = "No hay productos asociados a este ticket."
                    }
                } else {
                    infoCompras.text = "No hay registros de ventas."
                }
            } else {
                infoCompras.text = "No hay ticket en curso."
            }
        }else {
            // Obtener el usuario actual desde SharedPreferences
            val currentUser = sharedPreferences.getString("user", "user1") ?: "user1"

            // Obtener la lista de tickets
            val ticketsJson = sharedPreferences.getString("tickets", null)
            val tickets: MutableList<Ticket> = if (ticketsJson != null) {
                gson.fromJson(ticketsJson, object : TypeToken<MutableList<Ticket>>() {}.type)
            } else {
                mutableListOf()
            }

            // Filtrar tickets para el usuario actual
            val userTickets = tickets.filter { it.user == currentUser }

            if (userTickets.isNotEmpty()) {
                // Obtener la lista de ventas
                val salesJson = sharedPreferences.getString("sales", null)
                val sales: MutableList<Sale> = if (salesJson != null) {
                    gson.fromJson(salesJson, object : TypeToken<MutableList<Sale>>() {}.type)
                } else {
                    mutableListOf()
                }

                // Filtrar ventas para los tickets del usuario actual
                val userSales = userTickets.flatMap { ticket ->
                    sales.filter { it.ticketid == ticket.id }
                }

                if (userSales.isNotEmpty()) {
                    infoCompras.text = userSales.joinToString("\n") {
                        "Producto: ${it.product.name}, Cantidad: ${it.amount}, Precio: ${it.product.price}"
                    }
                } else {
                    infoCompras.text = "No hay ventas asociadas a los tickets del usuario actual."
                }
            } else {
                infoCompras.text = "No hay tickets para el usuario actual."
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        }
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }
}
