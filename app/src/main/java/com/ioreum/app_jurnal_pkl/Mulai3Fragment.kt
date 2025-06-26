package com.ioreum.app_jurnal_pkl

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class Mulai3Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mulai3, container, false)

        val btnLanjut = view.findViewById<MaterialButton>(R.id.btnLanjut)

        btnLanjut.setOnClickListener {
            // Navigasi ke MainActivity
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Tutup activity agar tidak bisa balik pakai back
        }

        return view
    }
}
