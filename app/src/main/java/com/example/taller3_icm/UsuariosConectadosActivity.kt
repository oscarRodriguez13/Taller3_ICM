package com.example.taller3_icm

import android.annotation.SuppressLint
import android.content.Intent
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
        adapter = UsuariosAdapter(usuarios) { usuario ->
            val intent = Intent(this, UbicacionUsuarioActivity::class.java)
            val bundle = Bundle().apply {
                putString("uid", usuario.uid)
                putInt("image", usuario.image)
                putString("nombre", usuario.nombre)
                putDouble("latitud", usuario.latitud)
                putDouble("longitud", usuario.longitud)
            }
            intent.putExtras(bundle)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")
        cargarUsuariosDisponibles()
    }

    private fun cargarUsuariosDisponibles() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        databaseReference.orderByChild("estado").equalTo("disponible")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    usuarios.clear()
                    snapshot.children.forEach { dataSnapshot ->
                        val userId = dataSnapshot.key.toString() // Suponiendo que cada entrada tiene su UID como clave
                        val nombre = dataSnapshot.child("nombre").getValue(String::class.java)
                        val latitudString = dataSnapshot.child("latitud").getValue(String::class.java)
                        val longitudString = dataSnapshot.child("longitud").getValue(String::class.java)

                        val latitud = latitudString?.toDouble()
                        val longitud = longitudString?.toDouble()

                        if (userId != currentUser?.uid && nombre != null && latitud != null && longitud != null) {
                            usuarios.add(Usuario(userId, R.drawable.icn_foto_perfil, nombre, latitud, longitud))
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