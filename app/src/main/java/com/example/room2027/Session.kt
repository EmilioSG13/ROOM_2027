package com.example.room2027

import android.content.Context
import java.security.MessageDigest

object Session {
    private const val PREFS = "session"
    private const val KEY_ID = "usuarioId"

    fun guardar(ctx: Context, usuarioId: Int) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_ID, usuarioId).apply()

    fun usuarioId(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_ID, -1)

    fun cerrar(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()

    fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
