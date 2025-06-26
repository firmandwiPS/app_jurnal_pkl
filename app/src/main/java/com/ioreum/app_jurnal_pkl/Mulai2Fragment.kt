package com.ioreum.app_jurnal_pkl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class Mulai2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mulai2, container, false)

        val btnLanjut = view.findViewById<MaterialButton>(R.id.btnLanjut)

        btnLanjut.setOnClickListener {
            // Pindah ke halaman fragment ke-3 (index 2)
            val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
            viewPager?.currentItem = 2
        }

        return view
    }
}
