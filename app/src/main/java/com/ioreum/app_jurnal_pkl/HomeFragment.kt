package com.ioreum.app_jurnal_pkl

import android.app.AlertDialog
import android.graphics.Color
import android.os.*
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.ioreum.app_jurnal_pkl.databinding.FragmentHomeBinding
import com.ioreum.app_jurnal_pkl.viewmodel.SharedPresensiViewModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import androidx.fragment.app.activityViewModels

class HomeFragment : Fragment() {

    // =====================[BAGIAN PRESENSI]=====================
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var barChart: BarChart

    private val bulanList = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    private val urlTampilPresensi = "http://172.16.100.11/jurnal_pkl/tampil_presensi.php"
    private var dataPresensiArray = JSONArray()

    private var selectedJenis = "Masuk"
    private var selectedRange = "Januari - Juni"

    private val sharedViewModel: SharedPresensiViewModel by activityViewModels()

    // =====================[BAGIAN INFORMASI]=====================
    private lateinit var textDate: TextView
    private lateinit var textTime: TextView
    private lateinit var scrollView: HorizontalScrollView

    private lateinit var imageViewTentang: ImageView
    private lateinit var imageViewProgram: ImageView
    private lateinit var imageViewFasilitas: ImageView
    private lateinit var imageViewRPL: ImageView

    private lateinit var textDeskripsiRPL: TextView
    private lateinit var btnToggleDeskripsiRPL: TextView

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

    // Image slider resources
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi bagian presensi
        barChart = binding.barChart
        fetchPresensiData()
        inisialisasiChartAwal()

        binding.btnFilterJenis.text = "$selectedJenis ▼"
        binding.tvDataJudul.text = "Data $selectedJenis"
        binding.btnFilterJenis.setOnClickListener { showJenisFilterMenu() }

        binding.btnFilterBulan.text = "$selectedRange ▼"
        binding.btnFilterBulan.setOnClickListener { showRangeFilterMenu() }

        sharedViewModel.dataUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated == true) {
                fetchPresensiData()
                sharedViewModel.clearUpdateFlag()
            }
        }

        // Inisialisasi bagian informasi
        textDate = binding.textDate
        textTime = binding.textTime
        scrollView = binding.horizontalScroll

        imageViewTentang = binding.imageSlideTentang
        imageViewProgram = binding.imageSlideProgram
        imageViewFasilitas = binding.imageSlideFasilitas
        imageViewRPL = binding.imageSlideRPL

        textDeskripsiRPL = binding.textDeskripsiRPL
        btnToggleDeskripsiRPL = binding.btnToggleDeskripsiRPL

        val btnLihatTentang = binding.btnLihat1
        val btnLihatProgram = binding.btnLihatProgram
        val btnLihatLokasi = binding.btnLihatLokasi

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
    }

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
        _binding = null

        // Hentikan semua handler saat view dihancurkan
        handler.removeCallbacks(updateTimeRunnable)
        handler.removeCallbacks(scrollRunnable)
        imageSlideHandlerTentang.removeCallbacks(imageSlideRunnableTentang)
        imageSlideHandlerProgram.removeCallbacks(imageSlideRunnableProgram)
        imageSlideHandlerFasilitas.removeCallbacks(imageSlideRunnableFasilitas)
    }

    // =====================[FUNGSI PRESENSI]=====================
    private fun showJenisFilterMenu() {
        val popup = PopupMenu(requireContext(), binding.btnFilterJenis)
        popup.menu.add("Masuk")
        popup.menu.add("Izin")
        popup.menu.add("Alfa")

        popup.setOnMenuItemClickListener { item ->
            selectedJenis = item.title.toString()
            binding.btnFilterJenis.text = "$selectedJenis ▼"
            binding.tvDataJudul.text = "Data $selectedJenis"
            setupChart()
            true
        }

        popup.show()
    }

    private fun showRangeFilterMenu() {
        val popup = PopupMenu(requireContext(), binding.btnFilterBulan)
        popup.menu.add("Januari - Juni")
        popup.menu.add("Juli - Desember")

        popup.setOnMenuItemClickListener { item ->
            selectedRange = item.title.toString()
            binding.btnFilterBulan.text = "$selectedRange ▼"
            setupChart()
            true
        }

        popup.show()
    }

    private fun fetchPresensiData() {
        val request = JsonArrayRequest(Request.Method.GET, urlTampilPresensi, null,
            { response ->
                dataPresensiArray = response
                setupChart()
            },
            {
                Toast.makeText(requireContext(), "Gagal memuat data presensi", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun inisialisasiChartAwal() {
        val entries = ArrayList<BarEntry>()
        val labels = arrayListOf("Januari", "Februari", "Maret", "April", "Mei", "Juni")

        val dataSet = BarDataSet(entries, "Jumlah $selectedJenis")
        dataSet.color = resources.getColor(R.color.green, null)

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        barChart.data = barData
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.setScaleEnabled(false)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -50f
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = labels.size.toFloat()

        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 20f
        leftAxis.granularity = 5f
        leftAxis.setLabelCount(5, true)
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = resources.getColor(R.color.black, null)
        leftAxis.gridLineWidth = 1f
        leftAxis.setDrawAxisLine(true)
        leftAxis.setDrawLabels(true)
        leftAxis.spaceTop = 0f
        leftAxis.spaceBottom = 0f

        barChart.axisRight.isEnabled = false

        val legend = barChart.legend
        legend.setDrawInside(false)
        legend.yOffset = 0f

        barChart.setExtraBottomOffset(15f)
        barChart.invalidate()
    }

    private fun setupChart() {
        val bulanMap = mutableMapOf<Int, Float>()
        val rangeIndices = if (selectedRange == "Januari - Juni") 0..5 else 6..11
        for (i in rangeIndices) {
            bulanMap[i] = 0f
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until dataPresensiArray.length()) {
            val obj = dataPresensiArray.getJSONObject(i)
            val tanggal = obj.getString("tanggal")
            val keterangan = obj.getString("keterangan")

            val date = sdf.parse(tanggal)
            val cal = Calendar.getInstance()
            cal.time = date!!
            val bulanIndex = cal.get(Calendar.MONTH)

            val cocokJenis = keterangan.equals(selectedJenis, ignoreCase = true)
            val cocokRange = bulanIndex in rangeIndices

            if (cocokJenis && cocokRange) {
                bulanMap[bulanIndex] = (bulanMap[bulanIndex] ?: 0f) + 1
            }
        }

        val filteredEntries = ArrayList<BarEntry>()
        val filteredBulanLabels = ArrayList<String>()
        var xIndex = 0f

        for (i in rangeIndices) {
            filteredEntries.add(BarEntry(xIndex, bulanMap[i] ?: 0f))
            filteredBulanLabels.add(bulanList[i])
            xIndex++
        }

        val dataSet = BarDataSet(filteredEntries, "Jumlah $selectedJenis")

        dataSet.color = when (selectedJenis.lowercase()) {
            "izin" -> resources.getColor(R.color.yellow, null)
            "alfa" -> resources.getColor(R.color.red, null)
            else -> resources.getColor(R.color.green, null)
        }

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        barChart.data = barData
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.setScaleEnabled(false)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(filteredBulanLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -50f
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = filteredEntries.size.toFloat()

        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 20f
        leftAxis.granularity = 5f
        leftAxis.setLabelCount(5, true)
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = resources.getColor(R.color.black, null)
        leftAxis.gridLineWidth = 1f

        barChart.axisRight.isEnabled = false
        barChart.invalidate()
    }

    // =====================[FUNGSI INFORMASI]=====================
    private fun getTentangText() = "BBPPMPV BMTI adalah pusat pelatihan pengembangan dan pelatihan vokasi di Indonesia."
    private fun getProgramText() = "Program unggulan mencakup pelatihan, magang industri, dan pengembangan kompetensi vokasi."
    private fun getLokasiText() = "BBPPMPV BMTI berlokasi di Cimahi, Jawa Barat dan memiliki fasilitas lengkap untuk pelatihan."

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