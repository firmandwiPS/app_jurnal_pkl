package com.ioreum.app_jurnal_pkl

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import com.ioreum.app_jurnal_pkl.viewmodel.SharedPresensiViewModel
import org.json.JSONObject
import java.util.*

class PresensiFragment : Fragment() {

    private lateinit var tablePresensi: TableLayout
    private lateinit var etCariPresensi: EditText
    private lateinit var spinnerFilterKeterangan: Spinner
    private var dataPresensiArray: JSONArray = JSONArray()

    private val sharedViewModel: SharedPresensiViewModel by activityViewModels()

    private val urlTampil = "http://172.16.100.11/jurnal_pkl/tampil_presensi.php"
    private val urlTambahPresensi = "http://172.16.100.11/jurnal_pkl/tambah_presensi.php"
    private val urlUbahPresensi = "http://172.16.100.11/jurnal_pkl/ubah_presensi.php"
    private val urlGetNis = "http://172.16.100.11/jurnal_pkl/daftar_siswa.php"
    private val urlHapusPresensi = "http://172.16.100.11/jurnal_pkl/hapus_presensi.php"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_presensi, container, false)

        tablePresensi = view.findViewById(R.id.tablePresensi)
        val btnTambah = view.findViewById<Button>(R.id.btnTambahPresensi)
        etCariPresensi = view.findViewById(R.id.etCariPresensi)
        spinnerFilterKeterangan = view.findViewById(R.id.spinnerFilterKeterangan)

        val options = listOf("Semua", "Masuk", "Izin", "Alfa")
        spinnerFilterKeterangan.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)

        spinnerFilterKeterangan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterDataPresensi(etCariPresensi.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        etCariPresensi.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDataPresensi(s.toString().trim())
            }
        })

        btnTambah.setOnClickListener { showBottomSheetPresensi() }

        tampilkanHeader()
        loadDataPresensi()

        return view
    }

    private fun tampilkanHeader() {
        val row = TableRow(requireContext())
        val headers = listOf("No", "NIS", "Nama Siswa", "Tanggal", "Keterangan", "Aksi")
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
        tablePresensi.addView(row)
    }

    private fun loadDataPresensi() {
        val request = StringRequest(
            Request.Method.GET, urlTampil,
            { response ->
                try {
                    dataPresensiArray = JSONArray(response)
                    filterDataPresensi(etCariPresensi.text.toString())
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Data tidak valid", Toast.LENGTH_SHORT).show()
                }
            },
            {
                dataPresensiArray = JSONArray()
                filterDataPresensi(etCariPresensi.text.toString())
            }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun filterDataPresensi(keyword: String) {
        tablePresensi.removeAllViews()
        tampilkanHeader()

        val selectedKeterangan = spinnerFilterKeterangan.selectedItem?.toString()
        var count = 1
        var adaData = false

        for (i in 0 until dataPresensiArray.length()) {
            val obj = dataPresensiArray.getJSONObject(i)
            val nis = obj.getString("nis")
            val nama = obj.getString("nama_siswa")
            val keterangan = obj.getString("keterangan")
            val tanggal = obj.getString("tanggal")
            val cocokKeyword = nis.contains(keyword, true) || nama.contains(keyword, true)
            val cocokKeterangan = selectedKeterangan == "Semua" || keterangan == selectedKeterangan

            if (cocokKeyword && cocokKeterangan) {
                adaData = true
                val row = TableRow(requireContext())
                val idPresensi = obj.getString("id_presensi")
                val data = listOf((count++).toString(), nis, nama, tanggal, keterangan)

                data.forEach {
                    val tv = TextView(requireContext()).apply {
                        text = it
                        setPadding(16, 47, 16, 12)
                        gravity = Gravity.CENTER
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setTextColor(0xFF000000.toInt())
                    }
                    row.addView(tv)
                }

                val layoutAksi = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    setPadding(8, 0, 8, 0)
                }

                val btnEdit = Button(requireContext()).apply {
                    text = "Ubah"
                    textSize = 12f
                    setOnClickListener {
                        showBottomSheetUbahPresensi(idPresensi, nis, tanggal, keterangan)
                    }
                }

                val btnHapus = Button(requireContext()).apply {
                    text = "Hapus"
                    textSize = 12f
                    setOnClickListener {
                        val alertDialog = android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Konfirmasi Hapus")
                            .setMessage("Yakin ingin menghapus presensi siswa $nama dengan NIS: $nis?")
                            .setPositiveButton("Ya, Hapus") { _, _ ->
                                hapusPresensi(idPresensi)
                            }
                            .setNegativeButton("Tidak", null)
                            .create()

                        alertDialog.setOnShowListener {
                            alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                                ?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                            alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                                ?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                        }

                        alertDialog.show()
                    }
                }

                layoutAksi.addView(btnEdit)
                layoutAksi.addView(btnHapus)
                row.addView(layoutAksi)
                tablePresensi.addView(row)
            }
        }

        if (!adaData && dataPresensiArray.length() > 0) {
            tampilkanPesan("Data presensi yang dicari tidak ditemukan.")
        } else if (!adaData && dataPresensiArray.length() == 0) {
            tampilkanPesan("Data presensi masih kosong.")
        }
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
            ).apply { span = 10 }
        }

        row.addView(tv)
        tablePresensi.addView(row)
    }

    private fun hapusPresensi(idPresensi: String) {
        val request = object : StringRequest(Method.POST, urlHapusPresensi,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("status")) {
                    Toast.makeText(requireContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    tablePresensi.removeAllViews()
                    tampilkanHeader()
                    loadDataPresensi()
                    tablePresensi.postDelayed({ loadDataPresensi() }, 200)
                    sharedViewModel.notifyDataUpdated()
                } else {
                    Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(requireContext(), "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_presensi" to idPresensi)
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun showBottomSheetPresensi() {
        showBottomSheet(null, null, null, null)
    }

    private fun showBottomSheetUbahPresensi(idPresensi: String, nis: String, tanggal: String, keterangan: String) {
        showBottomSheet(idPresensi, nis, tanggal, keterangan)
    }

    private fun showBottomSheet(idPresensi: String?, nisLama: String?, tanggalLama: String?, keteranganLama: String?) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_tambah_presensi, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = if (idPresensi == null) "Tambah Presensi" else "Ubah Presensi"

        val spinnerNis = view.findViewById<Spinner>(R.id.spinnerNis)
        val etTanggal = view.findViewById<EditText>(R.id.etTanggal)
        val spinnerKeterangan = view.findViewById<Spinner>(R.id.spinnerKeterangan)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)


        val nisList = mutableListOf<String>()
        val nisMap = mutableMapOf<String, String>()
        val reverseMap = mutableMapOf<String, String>()

        val requestNis = JsonArrayRequest(urlGetNis, { response ->
            for (i in 0 until response.length()) {
                val obj = response.getJSONObject(i)
                val nis = obj.getString("nis")
                val nama = obj.getString("nama_siswa")
                val display = "$nis - $nama"
                nisList.add(display)
                nisMap[display] = nis
                reverseMap[nis] = display
            }
            spinnerNis.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nisList)
            if (nisLama != null) spinnerNis.setSelection(nisList.indexOf(reverseMap[nisLama]))
        }, {
            Toast.makeText(requireContext(), "Gagal memuat NIS", Toast.LENGTH_SHORT).show()
        })
        Volley.newRequestQueue(requireContext()).add(requestNis)

        val keteranganOptions = listOf("Masuk", "Izin", "Alfa")
        spinnerKeterangan.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, keteranganOptions)

        if (tanggalLama != null) etTanggal.setText(tanggalLama)
        if (keteranganLama != null) spinnerKeterangan.setSelection(keteranganOptions.indexOf(keteranganLama))

        etTanggal.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                etTanggal.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSimpan.setOnClickListener {
            val nisSelected = spinnerNis.selectedItem?.toString()
            val nis = nisMap[nisSelected]
            val tanggal = etTanggal.text.toString().trim()
            val keterangan = spinnerKeterangan.selectedItem?.toString()

            if (nis.isNullOrEmpty() || tanggal.isEmpty() || keterangan.isNullOrEmpty()) {
                Toast.makeText(context, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idPresensi == null) {
                simpanPresensi(nis, tanggal, keterangan) {
                    dialog.dismiss()
                    loadDataPresensi()
                }
            } else {
                ubahPresensi(idPresensi, nis, tanggal, keterangan) {
                    dialog.dismiss()
                    loadDataPresensi()
                }
            }
        }

        dialog.show()
    }

    private fun simpanPresensi(nis: String, tanggal: String, keterangan: String, onSuccess: () -> Unit) {
        val request = object : StringRequest(Method.POST, urlTambahPresensi,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("status")) {
                    sharedViewModel.notifyDataUpdated()
                    onSuccess()
                } else {
                    Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(context, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("nis" to nis, "tanggal" to tanggal, "keterangan" to keterangan)
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun ubahPresensi(idPresensi: String, nis: String, tanggal: String, keterangan: String, onSuccess: () -> Unit) {
        val request = object : StringRequest(Method.POST, urlUbahPresensi,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("status")) {
                    Toast.makeText(context, "Data berhasil diubah", Toast.LENGTH_SHORT).show()
                    sharedViewModel.notifyDataUpdated()
                    onSuccess()
                } else {
                    Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(context, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_presensi" to idPresensi, "nis" to nis, "tanggal" to tanggal, "keterangan" to keterangan)
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }
}
