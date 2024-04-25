package com.example.taller3_icm

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val editTextTextEmailAddress = findViewById<EditText>(R.id.email_input)
        val editTextTextPassword = findViewById<EditText>(R.id.password_input)

        val email = editTextTextEmailAddress.text.toString()
        val password = editTextTextPassword.text.toString()

        val registerButton = findViewById<Button>(R.id.register_button)

        registerButton.setOnClickListener {
            startActivity(Intent(applicationContext, RegistrarUsuarioActivity::class.java).apply {
                putExtra("EMAIL", editTextTextEmailAddress.toString())
            })
        }

        if (email.isNotEmpty() && password.isNotEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email y contraseña correctos", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this, "Email y contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Mostrar mensaje de error al usuario
            Toast.makeText(this, "Email y contraseña son requeridos", Toast.LENGTH_SHORT).show()
        }



    }
}