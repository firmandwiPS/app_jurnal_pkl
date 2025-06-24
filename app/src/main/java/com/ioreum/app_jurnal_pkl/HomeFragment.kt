package com.ioreum.app_jurnal_pkl

import android.app.AlertDialog
import android.graphics.Color
import android.os.*
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    // =====================[UI COMPONENTS]=====================
    private lateinit var textDate: TextView
    private lateinit var textTime: TextView
    private lateinit var scrollView: HorizontalScrollView

    private lateinit var imageViewTentang: ImageView
    private lateinit var imageViewProgram: ImageView
    private lateinit var imageViewFasilitas: ImageView
    private lateinit var imageViewRPL: ImageView

    private lateinit var textDeskripsiRPL: TextView
    private lateinit var btnToggleDeskripsiRPL: TextView

    // =====================[HANDLER & SCROLL CONFIG]=====================
    private val handler = Handler(Looper.getMainLooper())

    private var autoScrollSpeed = 4
    private val autoScrollSpeedNormal = 4
    private val autoScrollSpeedFast = 4

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (!::scrollView.isInitialized || scrollView.childCount == 0) return

            val maxScroll = scrollView.getChildAt(0).width - scrollView.width
            val currentX = scrollView.scrollX

            if (currentX >= maxScroll - 5) {
                scrollView.smoothScrollTo(0, 0)
            } else {
                scrollView.smoothScrollBy(autoScrollSpeed, 0)
            }

            handler.postDelayed(this, 30)
        }
    }

    // =====================[TIME UPDATE CONFIG]=====================
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val now = Date()
            val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            textDate.text = dateFormat.format(now)
            textTime.text = timeFormat.format(now)

            handler.postDelayed(this, 1000)
        }
    }

    // =====================[DESKRIPSI RPL]=====================
    private var isDeskripsiFullVisible = false

    private val deskripsiPendek = "Lab RPL adalah tempat praktik siswa dalam mengembangkan perangkat lunak secara profesional."

    private val deskripsiLengkap = """
        Lab RPL (Rekayasa Perangkat Lunak) adalah fasilitas utama bagi siswa jurusan RPL untuk belajar dan berlatih secara langsung dalam pengembangan software.

        Di dalam lab ini, siswa mengerjakan berbagai proyek seperti:
        • Pembuatan aplikasi mobile dan web  
        • Pengujian perangkat lunak  
        • Simulasi analisis dan desain sistem  
        • Pemrograman dengan berbagai bahasa (Java, Kotlin, Python, dll.)

        Lab RPL dilengkapi dengan komputer berperforma tinggi, koneksi internet cepat, dan lingkungan kerja kolaboratif yang mendukung pembelajaran praktis dan inovatif. Di sinilah siswa mengasah keterampilan mereka menjadi calon programmer profesional masa depan.
    """.trimIndent()

    // =====================[IMAGE SLIDER]=====================
    private val imageResourcesTentang = arrayOf(R.drawable.gedung, R.drawable.gedung2, R.drawable.gedung3)
    private val imageResourcesProgram = arrayOf(R.drawable.pelatihan, R.drawable.program2, R.drawable.program3)
    private val imageResourcesFasilitas = arrayOf(R.drawable.lokasi)

    private var currentImageIndexTentang = 0
    private var currentImageIndexProgram = 0
    private var currentImageIndexFasilitas = 0

    private val imageSlideHandlerTentang = Handler(Looper.getMainLooper())
    private val imageSlideHandlerProgram = Handler(Looper.getMainLooper())
    private val imageSlideHandlerFasilitas = Handler(Looper.getMainLooper())

    private val imageSlideRunnableTentang = object : Runnable {
        override fun run() {
            imageViewTentang.setImageResource(imageResourcesTentang[currentImageIndexTentang])
            currentImageIndexTentang = (currentImageIndexTentang + 1) % imageResourcesTentang.size
            imageSlideHandlerTentang.postDelayed(this, 2000)
        }
    }

    private val imageSlideRunnableProgram = object : Runnable {
        override fun run() {
            imageViewProgram.setImageResource(imageResourcesProgram[currentImageIndexProgram])
            currentImageIndexProgram = (currentImageIndexProgram + 1) % imageResourcesProgram.size
            imageSlideHandlerProgram.postDelayed(this, 2000)
        }
    }

    private val imageSlideRunnableFasilitas = object : Runnable {
        override fun run() {
            imageViewFasilitas.setImageResource(imageResourcesFasilitas[currentImageIndexFasilitas])
            currentImageIndexFasilitas = (currentImageIndexFasilitas + 1) % imageResourcesFasilitas.size
            imageSlideHandlerFasilitas.postDelayed(this, 2000)
        }
    }

    // =====================[FRAGMENT LIFECYCLE]=====================
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize UI
        textDate = view.findViewById(R.id.textDate)
        textTime = view.findViewById(R.id.textTime)
        scrollView = view.findViewById(R.id.horizontalScroll)

        imageViewTentang = view.findViewById(R.id.imageSlideTentang)
        imageViewProgram = view.findViewById(R.id.imageSlideProgram)
        imageViewFasilitas = view.findViewById(R.id.imageSlideFasilitas)
        imageViewRPL = view.findViewById(R.id.imageSlideRPL)

        textDeskripsiRPL = view.findViewById(R.id.textDeskripsiRPL)
        btnToggleDeskripsiRPL = view.findViewById(R.id.btnToggleDeskripsiRPL)

        val btnLihatTentang = view.findViewById<View>(R.id.btnLihat1)
        val btnLihatProgram = view.findViewById<View>(R.id.btnLihatProgram)
        val btnLihatLokasi = view.findViewById<View>(R.id.btnLihatLokasi)

        // Tombol popup info
        btnLihatTentang.setOnClickListener { showAlert("Tentang BBPPMPV BMTI", getTentangText()) }
        btnLihatProgram.setOnClickListener { showAlert("Program Unggulan", getProgramText()) }
        btnLihatLokasi.setOnClickListener { showAlert("Lokasi & Fasilitas", getLokasiText()) }

        // Tombol toggle deskripsi RPL
        btnToggleDeskripsiRPL.setOnClickListener {
            isDeskripsiFullVisible = !isDeskripsiFullVisible
            if (isDeskripsiFullVisible) {
                textDeskripsiRPL.text = deskripsiLengkap
                btnToggleDeskripsiRPL.text = "Tutup"
                btnToggleDeskripsiRPL.setTextColor(Color.RED)
            } else {
                textDeskripsiRPL.text = deskripsiPendek
                btnToggleDeskripsiRPL.text = "Lihat Selengkapnya"
                btnToggleDeskripsiRPL.setTextColor(Color.parseColor("#3F51B5"))
            }
        }

        // Deteksi geser untuk pause/resume auto scroll
        scrollView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> handler.removeCallbacks(scrollRunnable)
                MotionEvent.ACTION_UP -> {
                    handler.postDelayed({
                        autoScrollSpeed = autoScrollSpeedFast
                        handler.post(scrollRunnable)
                        handler.postDelayed({ autoScrollSpeed = autoScrollSpeedNormal }, 3000)
                    }, 2000)
                }
            }
            false
        }

        // Mulai semua handler
        handler.post(updateTimeRunnable)
        handler.postDelayed(scrollRunnable, 1000)

        imageSlideHandlerTentang.post(imageSlideRunnableTentang)
        imageSlideHandlerProgram.post(imageSlideRunnableProgram)
        imageSlideHandlerFasilitas.post(imageSlideRunnableFasilitas)

        return view
    }

    // =====================[Animasi ]=====================
    override fun onResume() {
        super.onResume()

        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)

        val textViewsToAnimate = listOf(
            R.id.btnToggleDeskripsiRPL,
            R.id.btnLihat1,
            R.id.btnLihatProgram,
            R.id.btnLihatLokasi
        )

        textViewsToAnimate.forEach { id ->
            view?.findViewById<View>(id)?.startAnimation(fadeIn)
        }

        view?.findViewById<View>(R.id.cardTentang)?.startAnimation(slideUp)
        view?.findViewById<View>(R.id.cardProgram)?.startAnimation(slideUp)
        view?.findViewById<View>(R.id.cardLokasi)?.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hentikan semua handler saat view dihancurkan
        handler.removeCallbacks(updateTimeRunnable)
        handler.removeCallbacks(scrollRunnable)
        imageSlideHandlerTentang.removeCallbacks(imageSlideRunnableTentang)
        imageSlideHandlerProgram.removeCallbacks(imageSlideRunnableProgram)
        imageSlideHandlerFasilitas.removeCallbacks(imageSlideRunnableFasilitas)
    }

    // =====================[POPUP TEXTS]=====================
    private fun getTentangText() = "BBPPMPV BMTI adalah pusat pelatihan pengembangan dan pelatihan vokasi di Indonesia."
    private fun getProgramText() = "Program unggulan mencakup pelatihan, magang industri, dan pengembangan kompetensi vokasi."
    private fun getLokasiText() = "BBPPMPV BMTI berlokasi di Cimahi, Jawa Barat dan memiliki fasilitas lengkap untuk pelatihan."

    // =====================[ALERT DIALOG BUILDER]=====================
    private fun showAlert(title: String, message: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Tutup") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        }

        dialog.show()
    }
}
