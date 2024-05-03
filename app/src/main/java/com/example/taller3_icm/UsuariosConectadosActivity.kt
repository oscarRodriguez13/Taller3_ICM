package com.example.taller3_icm

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.osmdroid.config.Configuration
import java.util.Arrays
import java.util.concurrent.CountDownLatch

class UsuariosConectadosActivity : AppCompatActivity(), LocationListener {
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var usuarios: MutableList<Usuario>
    private lateinit var adapter: UsuariosAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios_conectados)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")

        Configuration.getInstance().userAgentValue = applicationContext.packageName

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        handlePermissions()

        recyclerView = findViewById(R.id.recyclerView)
        usuarios = mutableListOf()
        adapter = UsuariosAdapter(usuarios) { usuario ->
            val intent = Intent(this, UbicacionUsuarioActivity::class.java)
            val bundle = Bundle().apply {
                putString("uid", usuario.uid)
                putString("image", usuario.image)
                putString("nombre", usuario.nombre)
                putDouble("latitud", usuario.latitud)
                putDouble("longitud", usuario.longitud)
            }
            intent.putExtras(bundle)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        cargarUsuariosDisponibles()

        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            configurarListenerEstadoUsuario(uid)
        } else {
            Toast.makeText(this, "UID del usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    onLocationChanged(location)
                }
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Datos.MY_PERMISSION_REQUEST_LOCATION
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Datos.MY_PERMISSION_REQUEST_LOCATION
                )
            }
        }
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
                        val latitud = dataSnapshot.child("latitud").getValue(Double::class.java)
                        val longitud = dataSnapshot.child("longitud").getValue(Double::class.java)

                        if (userId != currentUser?.uid && nombre != null && latitud != null && longitud != null) {
                            usuarios.add(Usuario(userId, null, nombre, latitud, longitud))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onLocationChanged(location: Location) {
        val currentUser = auth.currentUser
        currentUser?.let {
            val uidUsuario = it.uid
            val estadoUsuarioRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(uidUsuario).child("estado")
            estadoUsuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val estado = snapshot.getValue(String::class.java)
                    if (estado == "disponible") {
                        // Actualizar la ubicación del usuario en la base de datos
                        val ubicacionUsuarioRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(uidUsuario)
                        ubicacionUsuarioRef.child("latitud").setValue(location.latitude)
                        ubicacionUsuarioRef.child("longitud").setValue(location.longitude)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar errores de lectura de la base de datos
                    Toast.makeText(applicationContext, "Error al leer el estado del usuario: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun configurarListenerEstadoUsuario(uid: String) {
        val referenciaUbicacionUsuario = obtenerReferenciaUbicacionUsuario(uid)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Aquí puedes manejar los cambios en la ubicación del usuario
                // Por ejemplo, actualizar la ubicación en el mapa
                val estado = snapshot.child("estado").getValue(String::class.java)
                val nombre = snapshot.child("nombre").getValue(String::class.java)


                Toast.makeText(applicationContext, "El usuario ${nombre} acaba de conectarse", Toast.LENGTH_LONG).show()

            }

            override fun onCancelled(error: DatabaseError) {
                // Maneja los errores de lectura de la base de datos
                Toast.makeText(applicationContext, "Error al leer el estado del usuario: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Agrega el listener a la referencia de la ubicación del usuario
        referenciaUbicacionUsuario.addValueEventListener(valueEventListener)
    }

    private fun obtenerReferenciaUbicacionUsuario(uid: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
    }

}