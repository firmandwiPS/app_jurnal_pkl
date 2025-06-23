package com.ioreum.app_jurnal_pkl

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var textDate: TextView? = null
    private var textTime: TextView? = null

    private val handler = Handler()

    private val updateTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            val now = Date()
            val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            textDate?.text = dateFormat.format(now)
            textTime?.text = timeFormat.format(now)
            handler.postDelayed(this, 1000)
        }
    }

    private lateinit var imageViewTentang: ImageView
    private lateinit var imageViewProgram: ImageView
    private lateinit var imageViewFasilitas: ImageView
    private lateinit var imageViewRPL: ImageView
    private lateinit var textDeskripsiRPL: TextView
    private lateinit var btnToggleDeskripsiRPL: TextView

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

    private val imageResourcesTentang = arrayOf(
        R.drawable.gedung,
        R.drawable.gedung2,
        R.drawable.gedung3
    )
    private val imageResourcesProgram = arrayOf(
        R.drawable.pelatihan,
        R.drawable.program2,
        R.drawable.program3,
        R.drawable.program4,
        R.drawable.program5
    )
    private val imageResourcesFasilitas = arrayOf(
        R.drawable.lokasi
        // Tambahkan lebih banyak gambar jika ada
    )

    private var currentImageIndexTentang = 0
    private var currentImageIndexProgram = 0
    private var currentImageIndexFasilitas = 0

    private val imageSlideHandlerTentang = Handler()
    private val imageSlideHandlerProgram = Handler()
    private val imageSlideHandlerFasilitas = Handler()

    private val imageSlideRunnableTentang = object : Runnable {
        override fun run() {
            imageViewTentang.setImageResource(imageResourcesTentang[currentImageIndexTentang])
            currentImageIndexTentang = (currentImageIndexTentang + 1) % imageResourcesTentang.size
            imageSlideHandlerTentang.postDelayed(this, 1000)
        }
    }

    private val imageSlideRunnableProgram = object : Runnable {
        override fun run() {
            imageViewProgram.setImageResource(imageResourcesProgram[currentImageIndexProgram])
            currentImageIndexProgram = (currentImageIndexProgram + 1) % imageResourcesProgram.size
            imageSlideHandlerProgram.postDelayed(this, 1000)
        }
    }

    private val imageSlideRunnableFasilitas = object : Runnable {
        override fun run() {
            imageViewFasilitas.setImageResource(imageResourcesFasilitas[currentImageIndexFasilitas])
            currentImageIndexFasilitas = (currentImageIndexFasilitas + 1) % imageResourcesFasilitas.size
            imageSlideHandlerFasilitas.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        textDate = view.findViewById(R.id.textDate)
        textTime = view.findViewById(R.id.textTime)

        imageViewTentang = view.findViewById(R.id.imageSlideTentang)
        imageViewProgram = view.findViewById(R.id.imageSlideProgram)
        imageViewFasilitas = view.findViewById(R.id.imageSlideFasilitas)

        imageViewRPL = view.findViewById(R.id.imageSlideRPL)
        textDeskripsiRPL = view.findViewById(R.id.textDeskripsiRPL)
        btnToggleDeskripsiRPL = view.findViewById(R.id.btnToggleDeskripsiRPL)

        // Jalankan slideshow
        imageSlideHandlerTentang.post(imageSlideRunnableTentang)
        imageSlideHandlerProgram.post(imageSlideRunnableProgram)
        imageSlideHandlerFasilitas.post(imageSlideRunnableFasilitas)

        handler.post(updateTimeRunnable)

        val btnLihatSelengkapnya = view.findViewById<View>(R.id.btnLihat1)
        val btnLihatProgram = view.findViewById<View>(R.id.btnLihatProgram)
        val btnLihatLokasi = view.findViewById<View>(R.id.btnLihatLokasi)

        btnLihatSelengkapnya.setOnClickListener {
            showAlert(
                "Tentang BBPPMPV BMTI",
                "BBPPMPV BMTI (Balai Besar Pengembangan Penjaminan Mutu Pendidikan Vokasi Bidang Mesin dan Teknik Industri) " +
                        "merupakan unit pelaksana teknis di bawah Kementerian Pendidikan, Kebudayaan, Riset, dan Teknologi (Kemendikbudristek) Republik Indonesia. " +
                        "Lembaga ini berlokasi di Kota Cimahi, Jawa Barat, dan memiliki peran strategis dalam peningkatan mutu pendidikan vokasi di Indonesia.\n\n" +

                        "Sebagai pusat pengembangan dan pelatihan tenaga pendidik vokasi di bidang mesin dan teknik industri, BBPPMPV BMTI menyelenggarakan berbagai program unggulan, antara lain:\n" +
                        "• Pelatihan dan sertifikasi kompetensi bagi guru SMK.\n" +
                        "• Pengembangan kurikulum berbasis industri dan teknologi terkini.\n" +
                        "• Penerapan teknologi pembelajaran modern seperti e-learning, teaching factory, dan blended learning.\n" +
                        "• Fasilitasi kerjasama dengan dunia usaha dan dunia industri (DUDI) guna memperkuat link and match.\n\n" +

                        "Tujuan utama BBPPMPV BMTI adalah menciptakan tenaga pendidik vokasi yang kompeten, adaptif, dan siap menghadapi tantangan revolusi industri 4.0 serta era society 5.0. " +
                        "Dengan komitmen terhadap peningkatan mutu dan inovasi pendidikan, BBPPMPV BMTI berperan penting dalam mencetak generasi unggul yang siap bersaing secara global."
            )
        }

        btnLihatProgram.setOnClickListener {
            showAlert(
                "Program Unggulan",
                "BBPPMPV BMTI merancang dan menyelenggarakan berbagai program unggulan yang bertujuan untuk meningkatkan mutu pendidikan vokasi, khususnya di bidang mesin dan teknik industri. Program-program ini disusun berdasarkan kebutuhan dunia kerja yang terus berkembang dan mengedepankan pendekatan berbasis kompetensi.\n\n" +

                        "Berikut adalah beberapa program unggulan yang dijalankan oleh BBPPMPV BMTI:\n\n" +

                        "• Pelatihan dan Sertifikasi Guru Produktif:\n" +
                        "  Meningkatkan kompetensi guru-guru SMK melalui pelatihan intensif yang mengacu pada standar industri nasional dan internasional.\n\n" +

                        "• Pengembangan Kurikulum Adaptif:\n" +
                        "  Menyusun kurikulum pendidikan vokasi yang responsif terhadap perkembangan teknologi dan kebutuhan dunia usaha dan dunia industri (DUDI).\n\n" +

                        "• Inovasi Metode Pembelajaran:\n" +
                        "  Mendorong penggunaan metode belajar modern seperti e-learning, blended learning, dan teaching factory untuk memperkuat proses pembelajaran yang kontekstual.\n\n" +

                        "• Penguatan Kemitraan Strategis:\n" +
                        "  Menjalin kerja sama dengan mitra industri untuk mendukung program magang, sinkronisasi kurikulum, serta pemenuhan kebutuhan SDM vokasi yang kompeten.\n\n" +

                        "Program-program ini dirancang untuk mempercepat pencapaian tujuan utama pendidikan vokasi, yaitu menghasilkan lulusan yang siap kerja, kompeten, dan mampu beradaptasi di era transformasi industri digital."
            )
        }

        btnLihatLokasi.setOnClickListener {
            showAlert(
                "Lokasi & Fasilitas BBPPMPV BMTI",
                "BBPPMPV BMTI (Balai Besar Pengembangan Penjaminan Mutu Pendidikan Vokasi Bidang Mesin dan Teknik Industri) berlokasi di Jl. Pesantren Km. 2, Cimahi, Jawa Barat. Lokasinya sangat strategis dan mudah diakses dari berbagai wilayah, menjadikannya tempat ideal untuk kegiatan pelatihan dan pengembangan pendidikan vokasi.\n\n" +

                        "Fasilitas yang tersedia di BBPPMPV BMTI dirancang untuk mendukung proses belajar-mengajar yang efektif dan modern. Berikut adalah beberapa fasilitas utama:\n\n" +

                        "• Workshop dan Laboratorium Industri:\n" +
                        "  Dilengkapi dengan peralatan mutakhir di bidang otomasi, teknik mesin, manufaktur, CNC, mekatronika, dan lainnya untuk praktik berbasis kompetensi.\n\n" +

                        "• Ruang Kelas Interaktif:\n" +
                        "  Ruang kelas modern yang dilengkapi dengan proyektor, sistem multimedia, dan konektivitas internet untuk mendukung proses pembelajaran digital.\n\n" +

                        "• Asrama Pelatihan:\n" +
                        "  Menyediakan fasilitas penginapan yang nyaman bagi peserta pelatihan jangka menengah hingga panjang, lengkap dengan ruang makan dan area komunal.\n\n" +

                        "• Aula dan Ruang Pertemuan:\n" +
                        "  Digunakan untuk seminar, workshop, dan kegiatan massal lainnya dalam skala lokal maupun nasional.\n\n" +

                        "• Area Hijau dan Lingkungan Asri:\n" +
                        "  Suasana kampus yang hijau dan tertata rapi menciptakan lingkungan belajar yang nyaman, sehat, dan mendukung konsentrasi peserta.\n\n" +

                        "Dengan fasilitas yang lengkap dan berkualitas tinggi, BBPPMPV BMTI terus berkomitmen menjadi pusat unggulan pengembangan pendidikan vokasi di Indonesia."
            )
        }

        // Atur toggle deskripsi RPL
        textDeskripsiRPL.text = deskripsiPendek
        btnToggleDeskripsiRPL.setOnClickListener {
            isDeskripsiFullVisible = !isDeskripsiFullVisible
            if (isDeskripsiFullVisible) {
                textDeskripsiRPL.text = deskripsiLengkap
                btnToggleDeskripsiRPL.text = "Tutup"
                btnToggleDeskripsiRPL.setTextColor(Color.RED)
            }
            else {
                textDeskripsiRPL.text = deskripsiPendek
                btnToggleDeskripsiRPL.text = "Lihat Selengkapnya"
                btnToggleDeskripsiRPL.setTextColor(Color.parseColor("#3F51B5")) // warna biru
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)

        view?.findViewById<View>(R.id.btnLihat1)?.startAnimation(fadeIn)
        view?.findViewById<View>(R.id.btnLihatProgram)?.startAnimation(fadeIn)
        view?.findViewById<View>(R.id.btnLihatLokasi)?.startAnimation(fadeIn)
        view?.findViewById<View>(R.id.btnToggleDeskripsiRPL)?.startAnimation(fadeIn)

        view?.findViewById<View>(R.id.cardTentang)?.startAnimation(slideUp)
        view?.findViewById<View>(R.id.cardProgram)?.startAnimation(slideUp)
        view?.findViewById<View>(R.id.cardLokasi)?.startAnimation(slideUp)
        view?.findViewById<View>(R.id.cardTentangRPL)?.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTimeRunnable)
        imageSlideHandlerTentang.removeCallbacks(imageSlideRunnableTentang)
        imageSlideHandlerProgram.removeCallbacks(imageSlideRunnableProgram)
        imageSlideHandlerFasilitas.removeCallbacks(imageSlideRunnableFasilitas)
    }

    private fun showAlert(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        }
        alertDialog.show()
    }
}
