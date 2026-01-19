package com.example.jagaduit.utils

import java.text.NumberFormat
import java.util.Locale

fun Double.toRupiah(): String {
    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    return numberFormat.format(this).replace("Rp", "Rp ")
}