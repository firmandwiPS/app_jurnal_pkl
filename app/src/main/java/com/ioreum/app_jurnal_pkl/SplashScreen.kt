// SplashScreen.kt
package com.ioreum.app_jurnal_pkl

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {

    private val POST_HANDLE_DELAY = 5500L
    private val LOGO_MOVE_UP_DELAY = 1500L
    private val TEXT_START_DELAY = 2000L
    private val WAVE_START_DELAY = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logo = findViewById<ImageView>(R.id.logoImage)
        val colorSlide = findViewById<View>(R.id.colorSlide)
        val textTyping = findViewById<TextView>(R.id.textTyping)
        val bgOverlay = findViewById<View>(R.id.bgOverlay)


        textTyping.visibility = View.INVISIBLE

        bgOverlay.visibility = View.INVISIBLE

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }
        logo.startAnimation(fadeIn)

        val slideUp = TranslateAnimation(0f, 0f, 200f, 0f).apply {
            duration = 1500
            fillAfter = true
            interpolator = AccelerateDecelerateInterpolator()
        }
        colorSlide.startAnimation(slideUp)

        Handler(Looper.getMainLooper()).postDelayed({
            val moveUp = TranslateAnimation(0f, 0f, 0f, -300f).apply {
                duration = 500
                fillAfter = true
            }
            logo.startAnimation(moveUp)
        }, LOGO_MOVE_UP_DELAY)

        Handler(Looper.getMainLooper()).postDelayed({
            textTyping.visibility = View.VISIBLE
            animateTyping(textTyping, "APLIKASI JURNAL PKL LAB RPL", 100) {
                bgOverlay.visibility = View.VISIBLE
                val overlayUp = TranslateAnimation(0f, 0f, bgOverlay.height.toFloat(), 0f).apply {
                    duration = 500
                    fillAfter = true
                    interpolator = AccelerateDecelerateInterpolator()
                }
                bgOverlay.startAnimation(overlayUp)
            }
        }, TEXT_START_DELAY)

        Handler(Looper.getMainLooper()).postDelayed({



        }, WAVE_START_DELAY)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, POST_HANDLE_DELAY)
    }

    private fun animateTyping(textView: TextView, text: String, delay: Long = 100, onTypingEnd: () -> Unit) {
        var index = 0
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (index <= text.length) {
                    textView.text = text.substring(0, index)
                    index++
                    handler.postDelayed(this, delay)
                } else {
                    onTypingEnd()
                }
            }
        }
        handler.post(runnable)
    }
}
