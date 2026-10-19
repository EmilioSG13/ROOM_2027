package com.example.room2027

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.room2027.data.AppDatabase
import com.example.room2027.data.Mascota
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var listView: ListView
    private lateinit var tvVacio: TextView
    private lateinit var adapter: MascotaAdapter
    private var todas: List<Mascota> = emptyList()
    private var usuarioId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usuarioId = Session.usuarioId(this)
        if (usuarioId == -1) {
            cerrarSesion()
            return
        }

        setContentView(R.layout.activity_main)
        db = AppDatabase.get(this)

        listView = findViewById(R.id.listMascotas)
        tvVacio = findViewById(R.id.tvVacio)
        adapter = MascotaAdapter(mutableListOf())
        listView.adapter = adapter

        val tvUsuario = findViewById<TextView>(R.id.tvUsuario)
        lifecycleScope.launch {
            val u = db.usuarioDao().porId(usuarioId)
            if (u == null) cerrarSesion() else tvUsuario.text = u.nombre
        }

        findViewById<TextView>(R.id.btnAgregar).setOnClickListener {
            startActivity(Intent(this, PetFormActivity::class.java))
        }

        // Click: actualizar en otra actividad
        listView.setOnItemClickListener { _, _, pos, _ ->
            val m = adapter.getItem(pos) ?: return@setOnItemClickListener
            startActivity(Intent(this, PetFormActivity::class.java).putExtra(PetFormActivity.EXTRA_ID, m.id))
        }

        // Long click: eliminar con confirmación
        listView.setOnItemLongClickListener { _, _, pos, _ ->
            val m = adapter.getItem(pos) ?: return@setOnItemLongClickListener true
            AlertDialog.Builder(this)
                .setTitle("Eliminar mascota")
                .setMessage("¿Está seguro que quiere eliminar a la mascota ${m.nombre}?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar") { _, _ ->
                    lifecycleScope.launch {
                        db.mascotaDao().eliminar(m)
                        Toast.makeText(this@MainActivity, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                        cargar()
                    }
                }
                .show()
            true
        }

        findViewById<EditText>(R.id.etBuscar).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) { filtrar(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<Button>(R.id.btnSalir).setOnClickListener { cerrarSesion() }
    }

    override fun onResume() {
        super.onResume()
        if (usuarioId != -1) cargar()
    }

    private fun cargar() {
        lifecycleScope.launch {
            todas = db.mascotaDao().listarPorUsuario(usuarioId)
            filtrar(findViewById<EditText>(R.id.etBuscar).text.toString())
        }
    }

    private fun filtrar(q: String) {
        val lista = if (q.isBlank()) todas
        else todas.filter { it.nombre.contains(q.trim(), true) || it.raza.contains(q.trim(), true) }
        adapter.clear()
        adapter.addAll(lista)
        tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun cerrarSesion() {
        Session.cerrar(this)
        val i = Intent(this, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private inner class MascotaAdapter(items: MutableList<Mascota>) :
        ArrayAdapter<Mascota>(this@MainActivity, 0, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: layoutInflater.inflate(R.layout.item_mascota, parent, false)
            val m = getItem(position)!!
            v.findViewById<TextView>(R.id.tvNombre).text = m.nombre
            v.findViewById<TextView>(R.id.tvDetalle).text = "${m.raza} · ${m.edad} años"
            return v
        }
    }
}
