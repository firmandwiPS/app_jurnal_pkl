package com.ioreum.app_jurnal_pkl

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class IntroActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        viewPager = findViewById(R.id.viewPager)
        val adapter = IntroPagerAdapter(this)
        viewPager.adapter = adapter

        // 🌟 Animasi Depth Page Transformer
        viewPager.setPageTransformer(DepthPageTransformer())
    }

    // 🎬 DepthPageTransformer Class
    inner class DepthPageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            view.apply {
                when {
                    position < -1 -> { // Halaman terlalu kiri
                        alpha = 0f
                    }
                    position <= 0 -> { // halaman yang masuk dari kiri
                        alpha = 1f
                        translationX = 0f
                        scaleX = 1f
                        scaleY = 1f
                    }
                    position <= 1 -> { // halaman keluar ke kanan
                        alpha = 1 - position
                        translationX = width * -position
                        val scaleFactor = 0.75f + (1 - Math.abs(position)) * 0.25f
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                    }
                    else -> { // terlalu kanan
                        alpha = 0f
                    }
                }
            }
        }
    }
}
