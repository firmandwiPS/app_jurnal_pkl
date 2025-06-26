package com.ioreum.app_jurnal_pkl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class Mulai1Fragment : Fragment() {

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mulai1, container, false)

        val btnLanjut = view.findViewById<Button>(R.id.btnLanjut)

        // Ambil ViewPager2 dari activity
        viewPager = requireActivity().findViewById(R.id.viewPager)

        // Saat tombol diklik, geser ke fragment ke-2 (index = 1)
        btnLanjut.setOnClickListener {
            viewPager.currentItem = 1
        }

        return view
    }
}
