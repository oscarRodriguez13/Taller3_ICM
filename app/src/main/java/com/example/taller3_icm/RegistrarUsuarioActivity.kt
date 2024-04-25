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
    private lateinit var  myRef:DatabaseReference
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

    fun loadUsers() {
        myRef = database.getReference(PATH_USERS)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    val myUser = singleSnapshot.getValue(MyUser::class.java)
                    Log.i("Firebase", "Encontró usuario: " + myUser?.name)
                    val name = myUser?.name
                    val age = myUser?.age
                    Toast.makeText(baseContext, "$name: $age", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "error en la consulta", databaseError.toException())
            }
        })
    }
}