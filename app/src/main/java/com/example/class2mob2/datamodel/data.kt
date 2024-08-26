package com.example.mov2class.datamodel


data class User(
    val name: String,
    val pass: String
)
data class Product(
    val name: String,
    val type: String,
    val brand: String,
    val price: Double
)
data class Sale(
    val product: Product,
    val amount: Int,
    val ticketid:Int
)
data class Ticket(
    val user: String,
    val id:Int
)