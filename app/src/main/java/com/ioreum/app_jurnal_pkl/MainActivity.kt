package com.ioreum.app_jurnal_pkl

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ioreum.app_jurnal_pkl.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private var currentIndex = 2 // Langsung mulai dari HomeFragment
    private var isFromFabClick = true // Untuk trigger awal Home dari FAB

    private val fragmentList = listOf(
        SiswaFragment(),     // index 0
        JurnalFragment(),    // index 1
        HomeFragment(),      // index 2 (FAB)
        PresensiFragment(),  // index 3
        laporan_pklFragment()// index 4
    )

    private val navIds = listOf(
        R.id.siswa,
        R.id.jurnal,
        R.id.fab,
        R.id.presensi,
        R.id.laporan
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil ViewPager dari CustomViewPager2 (yang bisa disable index tertentu)
        viewPager = binding.viewPager.findViewById(R.id.innerViewPager)

        // Styling background nav
        binding.bottomNavigationView.setBackgroundColor(Color.TRANSPARENT)
        binding.bottomNavigationView.setBackgroundResource(R.drawable.nav_background_with_touch_block)

        // Adapter ViewPager
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragmentList.size
            override fun createFragment(position: Int) = fragmentList[position]
        }
        viewPager.adapter = adapter

        // Nonaktifkan swipe ke HomeFragment (index 2 = FAB)
        binding.viewPager.disabledIndex = 2

        // ViewPager ke BottomNavigation
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 2 && !isFromFabClick && currentIndex != 2) {
                    // Cegah swipe ke HomeFragment
                    val target = if (currentIndex < position) 3 else 1
                    viewPager.setCurrentItem(target, true)
                } else {
                    binding.bottomNavigationView.selectedItemId = navIds[position]
                    currentIndex = position
                }
                isFromFabClick = false
            }
        })

        // BottomNavigation -> ViewPager
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val index = navIds.indexOf(item.itemId)
            if (index != -1 && index != 2 && index != currentIndex) {
                viewPager.setCurrentItem(index, true)
                currentIndex = index
            }
            true
        }

        // FAB -> HomeFragment
        binding.home.setOnClickListener {
            isFromFabClick = true
            viewPager.setCurrentItem(2, true)
            binding.bottomNavigationView.selectedItemId = R.id.fab
            currentIndex = 2
        }

        // 👇 Buka langsung HomeFragment saat pertama kali aplikasi dibuka
        viewPager.setCurrentItem(2, false)
        binding.bottomNavigationView.selectedItemId = R.id.fab
        currentIndex = 2
    }
}
