package com.ioreum.app_jurnal_pkl

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.GridLayout
import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inisialisasi DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Inisialisasi Toolbar dan atur sebagai ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Inisialisasi NavigationView dan atur listener
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Buat instance ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open, R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set fragment default saat pertama kali dibuka
        if (savedInstanceState == null) {
            replaceFragment(MenuFragment())
            navigationView.setCheckedItem(R.id.menu)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu -> replaceFragment(MenuFragment())
            R.id.siswa -> replaceFragment(SiswaFragment())
            R.id.jurnal -> replaceFragment(JurnalFragment())
            R.id.presensi -> replaceFragment(PresensiFragment())
            R.id.laporan_pkl -> replaceFragment(laporan_pklFragment())
            R.id.logout -> {
                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()
                finishAffinity() // Menutup semua aktivitas dan keluar dari aplikasi
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null) // Menambahkan transaksi ke back stack
        transaction.commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack() // Kembali ke fragment sebelumnya
            } else {
                super.onBackPressed() // Keluar dari aplikasi jika tidak ada fragment sebelumnya
            }
        }
    }
}