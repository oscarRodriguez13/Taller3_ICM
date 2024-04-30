package com.example.taller3_icm

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Arrays

class UsuariosConectadosActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var usuarios: MutableList<Usuario>
    private lateinit var adapter: UsuariosAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios_conectados)

        recyclerView = findViewById(R.id.recyclerView)
        usuarios = mutableListOf()
        adapter = UsuariosAdapter(usuarios)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")
        cargarUsuariosDisponibles()
    }

    private fun cargarUsuariosDisponibles() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        databaseReference.orderByChild("estado").equalTo("disponible")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usuarios.clear()
                    snapshot.children.forEach {
                        val userId = it.key // Suponiendo que cada entrada tiene su UID como clave
                        val nombre = it.child("nombre").getValue(String::class.java)
                        if (userId != currentUser?.uid && nombre != null) {
                            usuarios.add(Usuario(R.drawable.icn_foto_perfil, nombre))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}