package com.example.taller3_icm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
            profileImage.setImageResource(usuario.image)
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
