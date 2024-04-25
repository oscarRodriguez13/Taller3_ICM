package com.example.taller3_icm

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegistrarUsuarioActivity : AppCompatActivity() {

    val PATH_USERS="users/"
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var  myRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_usuario)

        val etName = findViewById<EditText>(R.id.etName)

        val user = "7JUFrrDo1ZgB9LC2fbb0k42lOQa2"

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val usr1 = Usuario()
            usr1.nombre = etName.text.toString()
            usr1.apellido = etName.text.toString()
            usr1.edad = etName.text.toString()
            usr1.numeroId = etName.text.toString()
            myRef = database.getReference(PATH_USERS + auth.currentUser!!.uid)
            myRef.setValue(usr1)

        }
    }

    fun loadUsers() {
        myRef = database.getReference(PATH_USERS)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    val myUser = singleSnapshot.getValue(Usuario::class.java)
                    Log.i("Firebase", "Encontró usuario: " + myUser?.nombre)
                    val name = myUser?.nombre
                    val age = myUser?.edad
                    Toast.makeText(baseContext, "$name: $age", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "error en la consulta", databaseError.toException())
            }
        })
    }
}