package com.ioreum.app_jurnal_pkl

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject

class JurnalFragment : Fragment() {

    private lateinit var tableJurnal: TableLayout
    private lateinit var etCariJurnal: EditText
    private lateinit var btnTambah: Button

    private val urlTampilJurnal = "http://172.16.100.11/jurnal_pkl/tampil_jurnal.php"
    private val urlHapusJurnal = "http://172.16.100.11/jurnal_pkl/hapus_jurnal.php"

    private val originalList = mutableListOf<JSONObject>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jurnal, container, false)
        tableJurnal     = view.findViewById(R.id.tableJurnal)
        etCariJurnal    = view.findViewById(R.id.etCariJurnal)
        btnTambah       = view.findViewById(R.id.btnTambahJurnal)

        btnTambah.setOnClickListener {
            val fragment = TambahJurnalFragment()
            parentFragmentManager.beginTransaction()
                .add(R.id.overlay_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            requireActivity().findViewById<View>(R.id.home).visibility = View.GONE
            requireActivity().findViewById<View>(R.id.overlay_fragment_container).visibility = View.VISIBLE
        }

        // Dengarkan sinyal dari Tambah/Ubah
        parentFragmentManager.setFragmentResultListener("refreshJurnal", viewLifecycleOwner) { _, _ ->
            loadDataJurnal()
        }

        etCariJurnal.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterDataJurnal(s.toString())
            }
        })

        tampilkanHeader()
        loadDataJurnal()
        return view
    }

    private fun tampilkanHeader() {
        tableJurnal.removeAllViews()
        val row = TableRow(requireContext())
        listOf("No","NIS","Nama","Tanggal","Uraian","Catatan","Paraf","Aksi").forEach {
            val tv = TextView(requireContext()).apply {
                text = it
                setPadding(16, 12, 16, 12)
                gravity = Gravity.CENTER
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTextColor(0xFF000000.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            row.addView(tv)
        }
        tableJurnal.addView(row)
    }

    private fun loadDataJurnal() {
        val req = StringRequest(urlTampilJurnal, { resp ->
            try {
                val arr = JSONArray(resp)
                originalList.clear()
                for (i in 0 until arr.length()) originalList.add(arr.getJSONObject(i))
                if (originalList.isEmpty()) {
                    tampilkanHeader()
                    tampilkanPesan("📭 DATA JURNAL MASIH KOSONG")
                } else {
                    filterDataJurnal(etCariJurnal.text.toString())
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal parsing data", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }, {
            Toast.makeText(requireContext(), "Gagal memuat data jurnal", Toast.LENGTH_SHORT).show()
        })
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun tampilkanPesan(msg: String) {
        val row = TableRow(requireContext())
        val tv = TextView(requireContext()).apply {
            text = msg
            gravity = Gravity.CENTER
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFF44336.toInt())
            setPadding(24, 20, 24, 20)
            layoutParams = TableRow.LayoutParams().apply { span = 8 }
        }
        row.addView(tv)
        tableJurnal.addView(row)
    }

    private fun filterDataJurnal(keyword: String) {
        tampilkanHeader()
        val lower = keyword.lowercase().trim()
        var found = false
        originalList.forEachIndexed { idx, obj ->
            val combined = listOf(
                obj.getString("nis"),
                obj.getString("nama_siswa"),
                obj.getString("tanggal_kegiatan"),
                obj.getString("uraian_kegiatan"),
                obj.getString("catatan_pembimbing")
            ).joinToString(" ").lowercase()
            if (combined.contains(lower)) {
                tampilkanBaris(obj, idx, showDeleteButton = true)
                found = true
            }
        }
        if (!found) tampilkanPesan("🔍 Data tidak ditemukan")
    }

    private fun tampilkanBaris(obj: JSONObject, index: Int, showDeleteButton: Boolean = true) {
        val row = TableRow(requireContext())
        val idJ = obj.getString("id_jurnal")
        val nis = obj.getString("nis")
        val nama = obj.getString("nama_siswa")
        val tgl = obj.getString("tanggal_kegiatan")
        val uraian = obj.getString("uraian_kegiatan")
        val cat = obj.getString("catatan_pembimbing")
        val foto = obj.getString("paraf_pembimbing")

        listOf((index + 1).toString(), nis, nama, tgl, uraian, cat).forEach {
            val tv = TextView(requireContext()).apply {
                text = it
                setPadding(12, 40, 12, 40) // Memberi padding vertikal agar teks tampak di tengah bawah
                gravity = Gravity.CENTER
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTextColor(0xFF000000.toInt())
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT
                )
            }
            row.addView(tv)
        }

        val iv = ImageView(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(220, 220)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(8, 8, 8, 8)
        }

        if (foto.isNotEmpty()) {
            Glide.with(this).load("http://172.16.100.11/jurnal_pkl/foto/$foto")
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(iv)
        } else {
            iv.setImageResource(R.drawable.placeholder)
        }
        row.addView(iv)

        val layoutAksi = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 40) // Agar tombol Ubah & Hapus berada di tengah vertikal
        }

        val btnU = Button(requireContext()).apply {
            text = "Ubah"
            textSize = 12f
            setOnClickListener {
                // Cegah fragment dobel
                val existingFragment = parentFragmentManager.findFragmentByTag("UbahJurnal")
                if (existingFragment == null) {
                    val fragment = UbahJurnalFragment().apply {
                        arguments = Bundle().apply {
                            putString("id_jurnal", idJ)
                            putString("nis", nis)
                            putString("nama", nama)
                            putString("tanggal", tgl)
                            putString("uraian", uraian)
                            putString("catatan", cat)
                            putString("paraf", foto)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .add(R.id.overlay_fragment_container, fragment, "UbahJurnal")
                        .addToBackStack(null)
                        .commit()

                    requireActivity().findViewById<View>(R.id.overlay_fragment_container).visibility = View.VISIBLE
                    requireActivity().findViewById<View>(R.id.home).visibility = View.GONE
                }
            }
        }
        layoutAksi.addView(btnU)

        if (showDeleteButton) {
            val btnH = Button(requireContext()).apply {
                text = "Hapus"
                textSize = 12f
                setOnClickListener {
                    val alertDialog = AlertDialog.Builder(requireContext())
                        .setTitle("Hapus Jurnal")
                        .setMessage("Apakah Anda yakin ingin menghapus jurnal atas nama:\n\n$nama\nTanggal: $tgl ?")
                        .setCancelable(false)
                        .setPositiveButton("Ya, Hapus") { _, _ ->
                            hapusJurnal(idJ)
                        }
                        .setNegativeButton("Batal", null)
                        .create()

                    alertDialog.setOnShowListener {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                    }

                    alertDialog.show()
                }
            }
            layoutAksi.addView(btnH)
        }

        row.addView(layoutAksi)
        tableJurnal.addView(row)
    }


    private fun hapusJurnal(idJurnal: String) {
        val req = object : StringRequest(Method.POST, urlHapusJurnal,
            { resp ->
                if (resp == "sukses") {
                    Toast.makeText(requireContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadDataJurnal()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(requireContext(), "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf("id_jurnal" to idJurnal)
        }
        Volley.newRequestQueue(requireContext()).add(req)
    }
}