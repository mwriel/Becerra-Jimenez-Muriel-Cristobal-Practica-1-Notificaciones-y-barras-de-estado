package com.example.class2mob2

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mov2class.datamodel.Product
import com.example.mov2class.datamodel.Sale
import com.example.mov2class.datamodel.Ticket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.BroadcastReceiver


class CatalogoActivity : AppCompatActivity() {
    private lateinit var product: Spinner
    private lateinit var amount: EditText
    private lateinit var info: TextView
    private lateinit var ticketView: TextView
    private lateinit var add: Button
    private lateinit var buy: Button
    private lateinit var cancel: Button
    private lateinit var get:Button
    private lateinit var gson: Gson
    private lateinit var sales: MutableList<Sale>
    private var currentTicketId: Int = -1
    private lateinit var products: List<Product>

    private val CHANNEL_ID = "Canal_notificacion"
    private val textTitle = "Notificación"
    private val textContentAdd = "Elemento agregado al ticket"
    private val textContentCancel = "¿Quieres cancelar la compra?"
    private val notificationIdAdd = 100
    private val notificationIdCancel = 101

    private val notificationId = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)
        get=findViewById(R.id.btnGet)
        product = findViewById(R.id.spnCatalog)
        amount = findViewById(R.id.etAmount)
        info = findViewById(R.id.tvInfo)
        ticketView = findViewById(R.id.tvTicket)
        add = findViewById(R.id.btnAdd)
        buy = findViewById(R.id.btnBuy)
        cancel = findViewById(R.id.btnOut)
        get.setOnClickListener { notificacionProgress() }
        gson = Gson()


        setSupportActionBar(findViewById(R.id.tbSale))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        createNotificationChannel()

        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Recuperar ID de ticket actual o asignar uno nuevo
        currentTicketId = sharedPreferences.getInt("ticketCurrent", -1)
        if (currentTicketId == -1) {
            currentTicketId = 0
            editor.putInt("ticketCurrent", currentTicketId)
            editor.apply()
        }

        val productsJson = sharedPreferences.getString("products", null)
        if (productsJson != null) {
            products = gson.fromJson(productsJson, object : TypeToken<List<Product>>() {}.type)
        } else {
            products = listOf(
                Product("Producto1", "Tipo1", "Marca1", 10.0),
                Product("Producto2", "Tipo2", "Marca2", 20.0),
                Product("Producto3", "Tipo3", "Marca3", 30.0),
                Product("Producto4", "Tipo4", "Marca4", 40.0),
                Product("Producto5", "Tipo5", "Marca5", 50.0)
            )
            editor.putString("products", gson.toJson(products))
            editor.apply()
        }

        // Inicializar el Spinner con los nombres de los productos
        val productNames = products.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, productNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        product.adapter = adapter


        product.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedProduct = products[position]
                info.text = "Producto: ${selectedProduct.name}\n" +
                        "Tipo: ${selectedProduct.type}\n" +
                        "Marca: ${selectedProduct.brand}\n" +
                        "Precio: ${selectedProduct.price}"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No se necesita hacer nada en este caso
            }
        }


        // Cargar las ventas actuales del ticket
        sales = mutableListOf()
        add.setOnClickListener {
            val selectedProduct = products[product.selectedItemPosition]
            val quantity = amount.text.toString().toIntOrNull()

            if (quantity != null && quantity > 0) {
                val sale = Sale(selectedProduct, quantity, currentTicketId)
                sales.add(sale)

                ticketView.text = sales.joinToString("\n") {
                    "Producto: ${it.product.name}, Cantidad: ${it.amount}, Precio: ${it.product.price}"
                }

                // Guardar las ventas en SharedPreferences
                // editor.putString("sales", gson.toJson(sales))
                // editor.apply()

                // Enviar notificación
                sendNotificationAdd()
            } else {
                // Mostrar un mensaje de error
                amount.error = "Cantidad no válida"
            }
        }


        buy.setOnClickListener {
            if (sales.isEmpty()) {

                Toast.makeText(this, "No hay ventas para procesar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar las ventas en SharedPreferences
            val savedSalesJson = sharedPreferences.getString("sales", null)
            val savedSales: MutableList<Sale> = if (savedSalesJson != null) {
                gson.fromJson(savedSalesJson, object : TypeToken<MutableList<Sale>>() {}.type)
            } else {
                mutableListOf()
            }

            savedSales.addAll(sales)
            editor.putString("sales", gson.toJson(savedSales))
            editor.apply()

            // Crear nuevo Ticket
            val ticketsJson = sharedPreferences.getString("tickets", null)
            val tickets: MutableList<Ticket> = if (ticketsJson != null) {
                gson.fromJson(ticketsJson, object : TypeToken<MutableList<Ticket>>() {}.type)
            } else {
                mutableListOf()
            }

            val newTicket = Ticket(
                user = sharedPreferences.getString("user", "user1") ?: "user1"
            ,
                id = currentTicketId
            )
            tickets.add(newTicket)
            editor.putString("tickets", gson.toJson(tickets))
            editor.putInt("thisTicket", currentTicketId) // Guardar el ticket actual en thisTicket

            editor.putInt("ticketCurrent", currentTicketId + 1)
            editor.putInt("status", 1)
            editor.apply()

            sendNotificationBuy()
            //finish()
        }

        cancel.setOnClickListener {
            sendNotificationCancel()
        }
    }

    private fun createNotificationChannel() {
        val name = "Mi Canal"
        val descriptionText = "Descripción del canal"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    @SuppressLint("MissingPermission")
    private fun sendNotificationAdd() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle(textTitle)
            .setContentText(textContentAdd)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationIdAdd, builder.build())
        }
    }
    @SuppressLint("MissingPermission")
    private fun sendNotificationCancel() {
        val intentYes = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val intentNo = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntentYes: PendingIntent = PendingIntent.getActivity(this, 0,
            intentYes, PendingIntent.FLAG_IMMUTABLE)
        val pendingIntentNo: PendingIntent = PendingIntent.getActivity(this, 0,
            intentYes, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle(textTitle)
            .setContentText(textContentCancel)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.baseline_notifications_active_24, "Sí", pendingIntentYes)
            .addAction(R.drawable.baseline_notifications_active_24, "No", pendingIntentNo/*.getActivity(this, 0,
                Intent(), PendingIntent.FLAG_IMMUTABLE)*/)
            .addAction(R.drawable.baseline_notifications_active_24, "nada", PendingIntent.getActivity(this, 0,
                Intent(), PendingIntent.FLAG_IMMUTABLE))
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationIdCancel, builder.build())
        }
    }
    @SuppressLint("MissingPermission")
    private fun sendNotificationBuy() {
        val intent = Intent(this, ComprasActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0,
            intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle(textTitle)
            .setContentText("Ver Ticket")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationIdAdd, builder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun notificacionProgress() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.baseline_notifications_active_24)
            setContentTitle(textTitle)
            setContentText("Descarga de ticket en progreso")
            setPriority(NotificationCompat.PRIORITY_LOW)
        }

        val PROGRESS_MAX = 100
        val PROGRESS_CURRENT = 0

        NotificationManagerCompat.from(this).apply {
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            notify(notificationId, builder.build())

            builder.setContentText("Descarga de ticket completa")
                .setProgress(PROGRESS_MAX, 20, false)
                .setTimeoutAfter(5000)

            notify(notificationId, builder.build())
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
