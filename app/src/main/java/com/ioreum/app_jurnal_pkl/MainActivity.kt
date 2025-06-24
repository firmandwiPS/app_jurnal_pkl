package com.ioreum.app_jurnal_pkl

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ioreum.app_jurnal_pkl.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private var currentIndex = 2
    private var isFromFabClick = true

    private val fragmentList = listOf(
        SiswaFragment(),     // 0
        JurnalFragment(),    // 1
        HomeFragment(),      // 2 (FAB)
        PresensiFragment(),  // 3
        laporan_pklFragment()// 4
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

        // 🔔 Minta izin notifikasi untuk Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            } else {
                ReminderUtil.scheduleDailyReminder(this)
            }
        } else {
            ReminderUtil.scheduleDailyReminder(this)
        }

        setupViewPagerAndNavigation()
    }

    // 🔄 Jika user memberikan izin
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ReminderUtil.scheduleDailyReminder(this)
        }
    }

    private fun setupViewPagerAndNavigation() {
        viewPager = binding.viewPager.findViewById(R.id.innerViewPager)

        binding.bottomNavigationView.setBackgroundColor(Color.TRANSPARENT)
        binding.bottomNavigationView.setBackgroundResource(R.drawable.nav_background_with_touch_block)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragmentList.size
            override fun createFragment(position: Int) = fragmentList[position]
        }
        viewPager.adapter = adapter

        // Nonaktifkan swipe ke HomeFragment (index 2 = FAB)
        binding.viewPager.disabledIndex = 2

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 2 && !isFromFabClick && currentIndex != 2) {
                    val target = if (currentIndex < position) 3 else 1
                    viewPager.setCurrentItem(target, true)
                } else {
                    binding.bottomNavigationView.selectedItemId = navIds[position]
                    currentIndex = position
                }
                isFromFabClick = false
            }
        })

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val index = navIds.indexOf(item.itemId)
            if (index != -1 && index != 2 && index != currentIndex) {
                viewPager.setCurrentItem(index, true)
                currentIndex = index
            }
            true
        }

        binding.home.setOnClickListener {
            isFromFabClick = true
            viewPager.setCurrentItem(2, true)
            binding.bottomNavigationView.selectedItemId = R.id.fab
            currentIndex = 2
        }

        // Mulai dari HomeFragment
        viewPager.setCurrentItem(2, false)
        binding.bottomNavigationView.selectedItemId = R.id.fab
        currentIndex = 2
    }
}
