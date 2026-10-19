package com.example.room2027

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.room2027.data.AppDatabase
import com.example.room2027.data.Usuario
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val cbTerminos = findViewById<CheckBox>(R.id.cbTerminos)
        val dao = AppDatabase.get(this).usuarioDao()

        findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase()
            val pass = etPassword.text.toString()
            val nombre = etNombre.text.toString().trim()

            when {
                email.isEmpty() || pass.isEmpty() || nombre.isEmpty() -> msg("Completa todos los campos")
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> etEmail.error = "Correo no válido"
                pass.length < 6 -> etPassword.error = "Mínimo 6 caracteres"
                !cbTerminos.isChecked -> msg("Debes aceptar los términos y la política de privacidad")
                else -> lifecycleScope.launch {
                    // Evitar duplicados
                    if (dao.buscarPorEmail(email) != null) {
                        etEmail.error = "Este correo ya está registrado"
                        return@launch
                    }
                    if (dao.buscarPorNombre(nombre) != null) {
                        etNombre.error = "Este nombre ya está en uso"
                        return@launch
                    }
                    try {
                        val id = dao.insertar(Usuario(email = email, password = Session.hash(pass), nombre = nombre))
                        Session.guardar(this@RegisterActivity, id.toInt())
                        val i = Intent(this@RegisterActivity, MainActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(i)
                    } catch (e: SQLiteConstraintException) {
                        msg("El usuario ya existe")
                    }
                }
            }
        }

        findViewById<TextView>(R.id.tvIngresar).setOnClickListener { finish() }
    }

    private fun msg(t: String) = Toast.makeText(this, t, Toast.LENGTH_SHORT).show()
}
