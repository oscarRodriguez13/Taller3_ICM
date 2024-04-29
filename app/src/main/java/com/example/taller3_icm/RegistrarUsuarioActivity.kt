package com.example.taller3_icm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3_icm.databinding.ActivityRegistrarUsuarioBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.osmdroid.util.GeoPoint

class RegistrarUsuarioActivity : AppCompatActivity() , LocationListener {

    private lateinit var binding: ActivityRegistrarUsuarioBinding
    private val locationPermissionRequestCode = 1001
    var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser? = null
    private var photoURI: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var locationManager: LocationManager
    private var currentLocation: GeoPoint? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrarUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager


        auth = Firebase.auth
        user = auth.currentUser

        binding.registrate.setOnClickListener() {
            registrarUsuario()
        }



        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                photoURI = uri
                binding.fotoUsuario.setImageURI(uri)
                println("URI CORRECTO")
            }


        }

        binding.fotoUsuario.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }


    }



    private fun validarCampos(): Boolean {
        var isValid = true

        // Validar nombre
        if (binding.Usuario.editText?.text.toString().isEmpty()) {
            binding.Usuario.error = "Falta ingresar nombre"
            isValid = false
        } else {
            binding.Usuario.error = null
        }

        // Validar correo electrónico
        if (binding.email.editText?.text.toString().isEmpty() ||
            !Patterns.EMAIL_ADDRESS.matcher(binding.email.editText?.text.toString()).matches()
        ) {
            binding.email.error = "Correo electrónico inválido"
            isValid = false
        } else {
            binding.email.error = null
        }

        // Validar apellido
        if (binding.apellido.editText?.text.toString().isEmpty()) {
            binding.apellido.error = "Falta ingresar apellido"
            isValid = false
        } else {
            binding.apellido.error = null
        }

        // Validar identificación
        if (binding.idPersonal.editText?.text.toString().isEmpty()) {
            binding.idPersonal.error = "Falta ingresar identificación"
            isValid = false
        } else {
            binding.idPersonal.error = null
        }

        // Validar contraseña
        if (binding.Contra.editText?.text.toString().isEmpty()) {
            binding.Contra.error = "Falta ingresar contraseña"
            isValid = false
        } else {
            binding.Contra.error = null
        }

        return isValid
    }


    private fun registrarUsuario(){
        if (validarCampos()) {
            val nombre = binding.Usuario.editText?.text.toString()
            val email = binding.email.editText?.text.toString()
            val contra = binding.Contra.editText?.text.toString()
            val apellido = binding.apellido.editText?.text.toString()
            val identificacion = binding.idPersonal.editText?.text.toString()

            auth.createUserWithEmailAndPassword(email, contra)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        user = auth.currentUser
                        val userId = user?.uid
                        // Guarda los datos del usuario en la base de datos
                        val database = Firebase.database
                        val ref = database.getReference("Usuarios").child(userId!!)
                        val userData = HashMap<String, Any>()
                        userData["nombre"] = nombre
                        userData["apellido"] = apellido
                        userData["identificacion"] = identificacion
                        userData["longitud"] = currentLocation?.longitude.toString()
                        userData["latitud"] = currentLocation?.latitude.toString()
                        userData["estado"] = "desconectado"

                        ref.setValue(userData)
                            .addOnSuccessListener {
                                cargarFotoPerfil(userId)

                                if (checkLocationPermission()) {

                                    val intent = Intent(this, MapaActivity::class.java)
                                    startActivity(intent)
                                    finish()

                                } else {
                                    requestLocationPermission()
                                }

                                }
                                .addOnFailureListener { e ->
                                    Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
                                }
                        } else {
                            Snackbar.make(binding.root, "Error: ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                        }
                    }

        }
    }

    private fun cargarFotoPerfil(userId: String) {
        photoURI?.let { uri ->
            val storageRef = Firebase.storage.reference.child("Usuarios/$userId/profile")

            storageRef.putFile(uri)
                .addOnSuccessListener {
                    limpiarCampos()
                }
                .addOnFailureListener { e ->
                    println("No funciono la carga de la foto del usuario")
                }
        } ?: run {
            println("URI de foto nula")
        }
    }

    private fun limpiarCampos() {
        binding.Usuario.editText?.setText("")
        binding.email.editText?.setText("")
        binding.apellido.editText?.setText("")
        binding.Contra.editText?.setText("")
        binding.fotoUsuario.setImageResource(R.drawable.blank_profile_picture)
    }

    override fun onResume() {
        super.onResume()
        if (checkLocationPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        } else {
            requestLocationPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
         currentLocation = GeoPoint(location.latitude, location.longitude)
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


}