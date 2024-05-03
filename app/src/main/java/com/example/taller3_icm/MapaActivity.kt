package com.example.taller3_icm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3_icm.databinding.ActivityMapaBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.BufferedReader
import java.io.InputStreamReader

class MapaActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: ActivityMapaBinding
    private lateinit var locationManager: LocationManager
    private lateinit var auth: FirebaseAuth
    private var marker: Marker? = null
    private var markers: MutableList<Marker> = mutableListOf()
    private var uid1: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("ENTRANDO A MAPA ACTIVITY")
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Configuration.getInstance().userAgentValue = applicationContext.packageName
        binding.osmMap.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)

        auth = FirebaseAuth.getInstance()

        binding.osmMap.setMultiTouchControls(true)


        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        cargarLocalizaciones()
        configurarListenerEstadoUsuarios()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_mapa, menu)
        Log.d("Menu", "Menu inflated")
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuLogOut -> {
                cambiarEstadoUsuario("desconectado")
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                true
            }
            R.id.menuEstado -> {
                val currentUser = auth.currentUser
                currentUser?.let {
                    val uid = it.uid
                    val usuariosRef = FirebaseDatabase.getInstance().getReference("Usuarios")
                    usuariosRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val estado = dataSnapshot.child("estado").getValue(String::class.java)
                                if (estado == "disponible") {
                                    Toast.makeText(this@MapaActivity, "Estado cambiado a desconectado", Toast.LENGTH_SHORT).show()
                                    usuariosRef.child(uid).child("estado").setValue("desconectado")
                                } else if (estado == "desconectado") {
                                    Toast.makeText(this@MapaActivity, "Estado cambiado a disponible", Toast.LENGTH_SHORT).show()
                                    usuariosRef.child(uid).child("estado").setValue("disponible")
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejar errores de lectura de la base de datos si es necesario
                        }
                    })
                }
                true
            }
            R.id.menuPersonas -> {
                val intent = Intent(
                    this,
                    UsuariosConectadosActivity::class.java
                )
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)


        }
    }

    private fun cambiarEstadoUsuario(estadoNuevo: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val usuariosRef = FirebaseDatabase.getInstance().getReference("Usuarios")
            usuariosRef.child(uid).child("estado").setValue(estadoNuevo).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Estado cambiado a $estadoNuevo", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al cambiar estado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarLocalizaciones() {
        try {
            val inputStream = assets.open("locations.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonStringBuilder = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                jsonStringBuilder.append(line)
                line = reader.readLine()
            }
            reader.close()

            val jsonObject = JSONObject(jsonStringBuilder.toString())
            val locationsArray = jsonObject.getJSONArray("locationsArray")
            for (i in 0 until locationsArray.length()) {
                val locationObject = locationsArray.getJSONObject(i)
                val latitude = locationObject.getDouble("latitude")
                val longitude = locationObject.getDouble("longitude")
                val name = locationObject.getString("name")
                val point = GeoPoint(latitude, longitude)
                val marker = Marker(binding.osmMap)
                marker.position = point
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = name
                markers.add(marker)
                binding.osmMap.overlays.add(marker)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading locations", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("MapaActivity", "onResume()")
        if (checkLocationPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        } else {
            requestLocationPermission()
        }


    }

    override fun onPause() {
        super.onPause()
        Log.d("MapaActivity", "onPause()")
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        val currentLocation = GeoPoint(location.latitude, location.longitude)
        val mapController = binding.osmMap.controller

        if (marker == null) {
            marker = Marker(binding.osmMap)
            binding.osmMap.overlays.add(marker)
            mapController.setZoom(15.0)
            mapController.setCenter(currentLocation)
        }

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

        marker?.position = currentLocation
        marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker?.title = "Tú"
        val icon: Drawable? = resources.getDrawable(R.drawable.baseline_person_pin_circle_24, null)
        marker?.icon = icon
        binding.osmMap.invalidate()
    }


    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Datos.MY_PERMISSION_REQUEST_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Datos.MY_PERMISSION_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onResume() // Reiniciar la actualización de la ubicación si se otorgan los permisos
            } else {
                Toast.makeText(
                    this,
                    "Permiso de ubicación requerido",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    private fun configurarListenerEstadoUsuarios() {
        val referenciaUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        val estadosAnteriores: MutableMap<String, String> = mutableMapOf() // Almacena el estado anterior de cada usuario

        referenciaUsuarios.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { dataSnapshot ->
                    val uid = dataSnapshot.key
                    val estadoActual = dataSnapshot.child("estado").getValue(String::class.java)

                    // Verificar si el estado actual no es nulo y es diferente al estado anterior
                    if (uid != null && estadoActual != null && estadoActual != estadosAnteriores[uid]) {
                        val nombre = dataSnapshot.child("nombre").getValue(String::class.java)
                        Log.i("Cambio Estado", "El usuario $nombre cambió su estado a $estadoActual")

                        // Mostrar el toast correspondiente al cambio de estado
                        if (estadoActual == "disponible") {
                            Toast.makeText(applicationContext, "El usuario $nombre acaba de conectarse", Toast.LENGTH_LONG).show()
                        } else if (estadoActual == "desconectado") {
                            Toast.makeText(applicationContext, "El usuario $nombre acaba de desconectarse", Toast.LENGTH_LONG).show()
                        }

                        // Actualizar el estado anterior del usuario
                        estadosAnteriores[uid] = estadoActual
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar los errores de lectura de la base de datos
                Toast.makeText(applicationContext, "Error al leer los estados de los usuarios: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }




}
