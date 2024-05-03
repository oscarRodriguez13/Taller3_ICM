package com.example.taller3_icm

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView

class UsuariosAdapter(
    private val usuarios: List<Usuario>,
    private val onButtonClick: (Usuario) -> Unit // Callback para el evento de clic del botón
) : RecyclerView.Adapter<UsuariosAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        private val nombre: TextView = itemView.findViewById(R.id.nombre)
        private val button: Button = itemView.findViewById(R.id.button)

        fun bind(usuario: Usuario) {

            val profileRef = Firebase.storage.reference.child("Usuarios").child(usuario.uid).child("profile")

            profileRef.downloadUrl.addOnSuccessListener { uri ->
                val profileImageUrl = uri.toString()

                val activityContext = (itemView.context as? Activity)
                activityContext?.let { activity ->
                    if (!activity.isDestroyed && !activity.isFinishing) {
                        // Cargar la imagen usando Glide
                        Glide.with(itemView.context) // Utiliza el contexto del itemView
                            .load(profileImageUrl) // Utiliza la URL de la imagen
                            .apply(RequestOptions().placeholder(R.drawable.icn_foto_perfil)) // Opcional: establece una imagen de marcador de posición mientras se carga la imagen
                            .into(profileImage)
                    }
                }

            }.addOnFailureListener {
                profileImage.setImageResource(R.drawable.icn_foto_perfil)
            }

            nombre.text = usuario.nombre

            // Configurar el evento de clic del botón
            button.setOnClickListener {
                onButtonClick(usuario)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.usuarios_adapter, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.bind(usuario)
    }

    override fun getItemCount(): Int {
        return usuarios.size
    }
}
