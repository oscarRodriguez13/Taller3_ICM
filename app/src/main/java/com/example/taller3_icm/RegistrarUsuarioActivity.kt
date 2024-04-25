package com.example.taller3_icm

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputBinding
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RegistrarUsuarioActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_usuario)


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