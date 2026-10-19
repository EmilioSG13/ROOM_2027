package com.example.room2027

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.room2027.data.AppDatabase
import com.example.room2027.data.Mascota
import kotlinx.coroutines.launch

/** Sirve para agregar (sin extra) o actualizar (con EXTRA_ID) una mascota. */
class PetFormActivity : AppCompatActivity() {

    companion object { const val EXTRA_ID = "mascotaId" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_form)

        val usuarioId = Session.usuarioId(this)
        val mascotaId = intent.getIntExtra(EXTRA_ID, -1)
        val editando = mascotaId != -1
        val dao = AppDatabase.get(this).mascotaDao()

        val tvTitulo = findViewById<TextView>(R.id.tvTitulo)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etRaza = findViewById<EditText>(R.id.etRaza)
        val etEdad = findViewById<EditText>(R.id.etEdad)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        var mascotaActual: Mascota? = null

        findViewById<TextView>(R.id.tvUsuario).text = ""
        if (editando) {
            tvTitulo.text = "Actualizar"
            btnGuardar.text = "Actualizar"
            btnEliminar.visibility = View.VISIBLE
            lifecycleScope.launch {
                val m = dao.porId(mascotaId, usuarioId)
                if (m == null) { finish(); return@launch }
                mascotaActual = m
                etNombre.setText(m.nombre)
                etRaza.setText(m.raza)
                etEdad.setText(m.edad.toString())
            }
        } else {
            tvTitulo.text = "Nueva"
            btnGuardar.text = "Guardar"
            btnEliminar.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val raza = etRaza.text.toString().trim()
            val edad = etEdad.text.toString().trim().toIntOrNull()
            if (nombre.isEmpty() || raza.isEmpty() || edad == null || edad < 0) {
                Toast.makeText(this, "Completa nombre, raza y una edad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                if (editando) dao.actualizar(Mascota(mascotaId, nombre, raza, edad, usuarioId))
                else dao.insertar(Mascota(nombre = nombre, raza = raza, edad = edad, usuarioId = usuarioId))
                Toast.makeText(this@PetFormActivity, "Mascota guardada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnEliminar.setOnClickListener {
            val m = mascotaActual ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Eliminar mascota")
                .setMessage("¿Está seguro que desea eliminar a la mascota ${m.nombre}?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar") { _, _ ->
                    lifecycleScope.launch {
                        dao.eliminar(m)
                        Toast.makeText(this@PetFormActivity, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .show()
        }

        findViewById<Button>(R.id.btnRegresar).setOnClickListener { finish() }
    }
}
