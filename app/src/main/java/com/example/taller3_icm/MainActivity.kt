package com.example.taller3_icm

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3_icm.databinding.ActivityMainBinding
import com.example.taller3_icm.databinding.ActivityRegistrarUsuarioBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val intent = Intent(this, RegistrarUsuarioActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
          iniciarSesion()
        }

        auth = Firebase.auth


    }

    private fun iniciarSesion(){
        if (validarCampos())
            auth.signInWithEmailAndPassword(
                binding.emailInput.editText?.text.toString(),
                binding.passwordInput.editText?.text.toString()
            ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (checkLocationPermission()) {
                        println("Se encontro el permiso")
                        val intent = Intent(this, MapaActivity::class.java)
                        startActivity(intent)
                    } else {
                        println("No se encontro el permiso")
                        requestLocationPermission()
                    }
                } else {
                    val snackbar = task.exception?.localizedMessage?.let {
                        Snackbar.make(
                            binding.root,
                            it, Snackbar.LENGTH_INDEFINITE
                        )
                    }
                    snackbar?.setAction("Error al Iniciar Sesión") { snackbar.dismiss() }
                    snackbar?.show()
                }
            }
    }

    private fun validarCampos(): Boolean {
        var isValid = true


        // Validar correo electrónico
        if (binding.emailInput.editText?.text.toString().isEmpty() ||
            !Patterns.EMAIL_ADDRESS.matcher(binding.emailInput.editText?.text.toString()).matches()
        ) {
            binding.emailInput.error = "Correo electrónico inválido"
            isValid = false
        } else {
            binding.emailInput.error = null
        }

        // Validar contraseña
        if (binding.passwordInput.editText?.text.toString().isEmpty()) {
            binding.passwordInput.error = "Falta ingresar contraseña"
            isValid = false
        } else {
            binding.passwordInput.error = null
        }

        return isValid
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Datos.MY_PERMISSION_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, MapaActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Permiso de ubicación requerido",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, MapaActivity::class.java)
                startActivity(intent)
            }
        }
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


}