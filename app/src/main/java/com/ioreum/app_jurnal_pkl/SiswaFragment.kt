package com.ioreum.app_jurnal_pkl

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class SiswaFragment : Fragment() {

    private lateinit var tableSiswa: TableLayout
    private lateinit var etCariSiswa: EditText
    private lateinit var btnTambah: Button

    private val urlTampil = "http://192.168.36.139/jurnal_pkl/siswa/tampil_siswa.php"
    private val urlHapus = "http://192.168.36.139/jurnal_pkl/siswa/hapus_siswa.php"

    private val originalList = mutableListOf<JSONObject>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_siswa, container, false)
        tableSiswa = view.findViewById(R.id.tableSiswa)
        etCariSiswa = view.findViewById(R.id.etCariSiswa)
        btnTambah = view.findViewById(R.id.btnTambahSiswa)

        btnTambah.setOnClickListener {
            val fragment = Tambah_siswaFragment()
            parentFragmentManager.beginTransaction()
                .add(R.id.overlay_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            requireActivity().findViewById<View>(R.id.home).visibility = View.GONE
            requireActivity().findViewById<View>(R.id.overlay_fragment_container).visibility = View.VISIBLE
        }

        parentFragmentManager.setFragmentResultListener("refreshSiswa", viewLifecycleOwner) { _, _ ->
            loadDataSiswa()
        }

        etCariSiswa.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterDataSiswa(s.toString())
            }
        })

        tampilkanHeader()
        loadDataSiswa()
        return view
    }

    private fun tampilkanHeader() {
        tableSiswa.removeAllViews()
        val row = TableRow(requireContext())
        val headers = listOf(
            "No", "NIS", "Nama", "Jenis kelamin", "Asal Sekolah", "Mulai PKL",
            "Selesai PKL", "No HP", "Alamat", "Aksi"
        )
        headers.forEach {
            val tv = TextView(requireContext()).apply {
                text = it
                setPadding(20, 18, 20, 18)
                setTextColor(0xFF000000.toInt())
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            row.addView(tv)
        }
        tableSiswa.addView(row)
    }

    private fun loadDataSiswa() {
        val queue = Volley.newRequestQueue(requireContext())
        val request = StringRequest(urlTampil, { response ->
            try {
                val array = JSONArray(response)
                originalList.clear()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    originalList.add(obj)
                }

                if (originalList.isEmpty()) {
                    tampilkanHeader()
                    tampilkanPesan("📭 DATA MASIH KOSONG")
                } else {
                    filterDataSiswa(etCariSiswa.text.toString())
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal parsing data", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }, {
            Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
        })
        queue.add(request)
    }

    private fun tampilkanPesan(pesan: String) {
        val row = TableRow(requireContext())

        val tv = TextView(requireContext()).apply {
            text = pesan
            setPadding(32, 24, 32, 24)
            gravity = Gravity.CENTER
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFF44336.toInt())
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            ).apply {
                span = 10
            }
        }

        row.addView(tv)
        tableSiswa.addView(row)
    }

    private fun filterDataSiswa(keyword: String) {
        tampilkanHeader()
        val lower = keyword.lowercase().trim()
        var found = false

        originalList.forEachIndexed { i, obj ->
            val dataToSearch = listOf(
                obj.getString("nis"),
                obj.getString("nama_siswa"),
                obj.getString("jenis_kelamin"),
                obj.getString("asal_sekolah"),
                obj.getString("tanggal_mulai"),
                obj.getString("tanggal_selesai"),
                obj.getString("no_hp"),
                obj.getString("alamat")
            ).joinToString(" ").lowercase()

            if (dataToSearch.contains(lower)) {
                tampilkanBaris(obj, i)
                found = true
            }
        }

        if (!found) {
            val row = TableRow(requireContext())

            val tv = TextView(requireContext()).apply {
                text = "🔍 Data yang dicari tidak ditemukan"
                setPadding(32, 24, 32, 24)
                gravity = Gravity.CENTER
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFFF44336.toInt())
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                ).apply {
                    span = 10
                }
            }

            row.addView(tv)
            tableSiswa.addView(row)
        }
    }

    private fun tampilkanBaris(obj: JSONObject, index: Int) {
        val row = TableRow(requireContext())
        val data = listOf(
            (index + 1).toString(),
            obj.getString("nis"),
            obj.getString("nama_siswa"),
            obj.getString("jenis_kelamin"),
            obj.getString("asal_sekolah"),
            obj.getString("tanggal_mulai"),
            obj.getString("tanggal_selesai"),
            obj.getString("no_hp"),
            obj.getString("alamat")
        )

        data.forEach {
            val tv = TextView(requireContext()).apply {
                text = it
                setPadding(16, 52, 16, 12)
                gravity = Gravity.CENTER
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTextColor(0xFF000000.toInt())
            }
            row.addView(tv)
        }

        val aksiLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 0, 8, 0)
            gravity = Gravity.CENTER
        }

        val btnUbah = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.edit) // ikon pensil
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_ubah)
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(8, 8, 8, 8)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(16, 16, 16, 16)
            setOnClickListener {
                val fragment = UbahSiswaFragment().apply {
                    arguments = Bundle().apply {
                        putString("nis", obj.getString("nis"))
                        putString("nama_siswa", obj.getString("nama_siswa"))
                        putString("jenis_kelamin", obj.getString("jenis_kelamin"))
                        putString("asal_sekolah", obj.getString("asal_sekolah"))
                        putString("tanggal_mulai", obj.getString("tanggal_mulai"))
                        putString("tanggal_selesai", obj.getString("tanggal_selesai"))
                        putString("no_hp", obj.getString("no_hp"))
                        putString("alamat", obj.getString("alamat"))
                    }
                }
                parentFragmentManager.beginTransaction()
                    .add(R.id.overlay_fragment_container, fragment, "UbahSiswa")
                    .addToBackStack(null)
                    .commit()
                requireActivity().findViewById<View>(R.id.home).visibility = View.GONE
                requireActivity().findViewById<View>(R.id.overlay_fragment_container).visibility = View.VISIBLE
            }
        }

        val btnHapus = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.delete) // ikon tong sampah
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_hapus)
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(8, 8, 8, 8)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(16, 16, 16, 16)
            setOnClickListener {
                val alertDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Yakin ingin menghapus siswa ${obj.getString("nama_siswa")} (${obj.getString("nis")})?")
                    .setPositiveButton("Ya, Hapus") { _, _ -> hapusData(obj.getString("nis")) }
                    .setNegativeButton("Tidak", null)
                    .create()

                alertDialog.setOnShowListener {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        ?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        ?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                }
                alertDialog.show()
            }
        }


        aksiLayout.addView(btnUbah)
        aksiLayout.addView(btnHapus)
        row.addView(aksiLayout)
        tableSiswa.addView(row)
    }

    private fun hapusData(nis: String) {
        val queue = Volley.newRequestQueue(requireContext())
        val request = object : StringRequest(Method.POST, urlHapus,
            {
                Toast.makeText(requireContext(), "Data dihapus", Toast.LENGTH_SHORT).show()
                loadDataSiswa()
            },
            {
                Toast.makeText(requireContext(), "Gagal hapus data", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> = hashMapOf("nis" to nis)
        }
        queue.add(request)
    }
}
