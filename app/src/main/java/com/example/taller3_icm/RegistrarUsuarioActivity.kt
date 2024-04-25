package com.example.taller3_icm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrarUsuarioActivity : AppCompatActivity() {

    val PATH_USERS="users/"
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_usuario)

        val name = findViewById<EditText>(R.id.etName).toString()

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val usr1 = Usuario()
            usr1.nombre = name
            usr1.apellido = "name"
            usr1.edad = "name"
            usr1.numeroId = "name"
            val myRef = database.getReference(PATH_USERS + auth.currentUser!!.uid)
            myRef.setValue(usr1)

        }
    }
}