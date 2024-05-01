package com.example.taller3_icm

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

class UbicacionUsuarioActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var sensorManager: SensorManager? = null
    private lateinit var locationManager: LocationManager
    private var lightSensor: Sensor? = null
    private var marker: Marker? = null
    private var mGeocoder: Geocoder? = null
    private var geoPoint: GeoPoint? = null
    private lateinit var osmMap: MapView
    private var isFirstLocationUpdate = true
    private var randomMarker: Marker? = null
    private var latitud: Double? = null
    private var longitud: Double? = null
    private var nombre: String? = null
    private var image: Int? = null
    private var uid: String? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion_usuario)

        val intentExtras = intent.extras
        if (intentExtras != null) {
            uid = intentExtras.getString("uid")
            image = intentExtras.getInt("image")
            nombre = intentExtras.getString("nombre")
            latitud = intentExtras.getDouble("latitud")
            longitud = intentExtras.getDouble("longitud")
        } else {
            Toast.makeText(this, "No se encontraron datos extras en el Intent", Toast.LENGTH_SHORT).show()
        }

        Configuration.getInstance().userAgentValue = applicationContext.packageName

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        handlePermissions()

        osmMap = findViewById(R.id.osmMap)
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)

        mGeocoder = Geocoder(baseContext)

        addMarkerUser()

        val centerButton = findViewById<ImageButton>(R.id.centerButton)

        centerButton.setOnClickListener {
            centerCameraOnUser()
        }

        if (uid != null) {
            configurarListenerUbicacionUsuario(uid!!)
        } else {
            Toast.makeText(this, "UID del usuario no encontrado", Toast.LENGTH_SHORT).show()
        }

    }

    private fun actualizarUbicacionEnMapa(latitud: Double?, longitud: Double?) {
        if (latitud != null && longitud != null) {
            val nuevaUbicacion = GeoPoint(latitud, longitud)
            randomMarker?.position = nuevaUbicacion
            osmMap.invalidate()
        } else {
            Toast.makeText(this, "No se encontró la ubicación del usuario", Toast.LENGTH_SHORT).show()
        }
    }


    private fun obtenerReferenciaUbicacionUsuario(uid: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
    }

    private fun configurarListenerUbicacionUsuario(uid: String) {
        val referenciaUbicacionUsuario = obtenerReferenciaUbicacionUsuario(uid)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Aquí puedes manejar los cambios en la ubicación del usuario
                // Por ejemplo, actualizar la ubicación en el mapa
                val latitudString = snapshot.child("latitud").getValue(String::class.java)
                val longitudString = snapshot.child("longitud").getValue(String::class.java)

                val latitud = latitudString?.toDouble()
                val longitud = longitudString?.toDouble()
                actualizarUbicacionEnMapa(latitud, longitud)
            }

            override fun onCancelled(error: DatabaseError) {
                // Maneja los errores de lectura de la base de datos
                Toast.makeText(applicationContext, "Error al leer la ubicación del usuario: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Agrega el listener a la referencia de la ubicación del usuario
        referenciaUbicacionUsuario.addValueEventListener(valueEventListener)
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

    @SuppressLint("MissingPermission")
    override fun onResume() {
        sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f, this)
        val latitude = 4.62
        val longitude = -74.07
        val startPoint = GeoPoint(latitude, longitude)
        super.onResume()
        osmMap.onResume()
        val mapController: IMapController = osmMap.controller
        mapController.setZoom(18.0)
        geoPoint?.let {
            mapController.setCenter(startPoint)
        }
    }

    override fun onPause() {
        super.onPause()
        osmMap.onPause()
        sensorManager?.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        geoPoint = GeoPoint(location.latitude, location.longitude)
        val mapController: IMapController = osmMap.controller
        mapController.setCenter(geoPoint)
        mapController.setZoom(18.0)

        if (marker == null) {
            marker = Marker(osmMap)
            osmMap.overlays.add(marker)
            marker?.position = geoPoint
            marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker?.title = "Tú"
        } else {
            marker?.position = geoPoint
        }
        osmMap.invalidate()

    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                val lux = event.values[0]
                if (lux < 30) {
                    osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                } else {
                    osmMap.overlayManager.tilesOverlay.setColorFilter(null)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

    private fun addMarkerUser() {

        val latitude = latitud ?: 0.0
        val longitude = longitud ?: 0.0

        val randomGeoPoint = GeoPoint(latitude, longitude)

        randomMarker = Marker(osmMap)
        randomMarker?.position = randomGeoPoint
        randomMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        randomMarker?.title = nombre
        val customMarkerDrawable = ContextCompat.getDrawable(this, R.drawable.icn_marcador_usuario)

        // Escalar la imagen al tamaño predeterminado (48x48 píxeles)
        val width = 48
        val height = 48
        val scaledDrawable = Bitmap.createScaledBitmap(
            (customMarkerDrawable as BitmapDrawable).bitmap,
            width,
            height,
            false
        )

        // Asignar la imagen escalada al marcador
        randomMarker?.icon = BitmapDrawable(resources, scaledDrawable)

        osmMap.overlays.add(randomMarker)
        osmMap.invalidate()
    }

    private fun centerCameraOnUser() {
        marker?.let {
            val mapController: IMapController = osmMap.controller
            mapController.setCenter(marker!!.position)
            mapController.setZoom(18.0)
        } ?: run {
            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
        }
    }


}
