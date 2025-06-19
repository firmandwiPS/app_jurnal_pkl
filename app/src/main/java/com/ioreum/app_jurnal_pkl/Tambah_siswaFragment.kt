package com.ioreum.app_jurnal_pkl

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class Tambah_siswaFragment : Fragment() {

    private lateinit var etNis: EditText
    private lateinit var etNama: EditText
    private lateinit var spinnerJK: Spinner
    private lateinit var etAsal: EditText
    private lateinit var etMulai: EditText
    private lateinit var etSelesai: EditText
    private lateinit var etHP: EditText
    private lateinit var etAlamat: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnKembali: ImageButton

    private var selectedJK: String = "LAKI-LAKI"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tambah_siswa, container, false)

        // Inisialisasi view
        etNis = view.findViewById(R.id.etNis)
        etNama = view.findViewById(R.id.etNama)
        spinnerJK = view.findViewById(R.id.spinnerJK)
        etAsal = view.findViewById(R.id.etAsal)
        etMulai = view.findViewById(R.id.etMulai)
        etSelesai = view.findViewById(R.id.etSelesai)
        etHP = view.findViewById(R.id.etHP)
        etAlamat = view.findViewById(R.id.etAlamat)
        btnSimpan = view.findViewById(R.id.btnSimpan)
        btnKembali = view.findViewById(R.id.btnKembali)

        // Setup
        setupSpinner()
        setupDatePicker(etMulai)
        setupDatePicker(etSelesai)

        btnSimpan.setOnClickListener {
            if (validasiInput()) {
                simpanData()
            }
        }

        btnKembali.setOnClickListener {
            closeThisFragment()
        }

        return view
    }

    private fun setupSpinner() {
        val jenisKelamin = listOf("LAKI-LAKI", "PEREMPUAN")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisKelamin)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerJK.adapter = adapter

        spinnerJK.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedJK = jenisKelamin[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, y, m, d ->
                val date = "$y-${String.format("%02d", m + 1)}-${String.format("%02d", d)}"
                editText.setText(date)
            }, year, month, day).show()
        }
    }


    private fun validasiInput(): Boolean {
        val inputs = listOf(
            Pair(etNis, "NIS wajib diisi"),
            Pair(etNama, "Nama wajib diisi"),
            Pair(etAsal, "Asal sekolah wajib diisi"),
            Pair(etMulai, "Tanggal mulai wajib diisi"),
            Pair(etSelesai, "Tanggal selesai wajib diisi"),
            Pair(etHP, "No HP wajib diisi"),
            Pair(etAlamat, "Alamat wajib diisi")
        )

        for ((field, message) in inputs) {
            if (field.text.isNullOrEmpty()) {
                field.error = message
                field.requestFocus()
                return false
            }
        }
        return true
    }

    private fun simpanData() {
        val url = "http://172.16.100.91/jurnal_pkl/siswa/tambah_siswa.php"

        val stringRequest = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getString("status") == "success") {
                        Toast.makeText(requireContext(), "✅ Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        setFragmentResult("refreshSiswa", Bundle())
                        closeThisFragment()
                    } else {
                        val msg = obj.getString("message")
                        if (msg.contains("NIS sudah ada", true)) {
                            etNis.error = "NIS sudah digunakan"
                            etNis.requestFocus()
                        }
                        Toast.makeText(requireContext(), "❌ $msg", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("TambahSiswa", "Parsing error: ${e.message}")
                    Toast.makeText(requireContext(), "❌ Gagal parsing respon", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("TambahSiswa", "Volley Error: ${error.message}")
                Toast.makeText(requireContext(), "❌ Gagal koneksi: ${error.message}", Toast.LENGTH_LONG).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "nis" to etNis.text.toString(),
                    "nama_siswa" to etNama.text.toString(),
                    "jenis_kelamin" to selectedJK,
                    "asal_sekolah" to etAsal.text.toString(),
                    "tanggal_mulai" to etMulai.text.toString(),
                    "tanggal_selesai" to etSelesai.text.toString(),
                    "no_hp" to etHP.text.toString(),
                    "alamat" to etAlamat.text.toString()
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }

    private fun closeThisFragment() {
        // Sembunyikan overlay agar tidak menutupi fragment di bawahnya
        requireActivity().findViewById<View>(R.id.home).visibility = View.VISIBLE
        requireActivity().findViewById<View>(R.id.overlay_fragment_container).visibility = View.GONE
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Jaga-jaga jika user tekan tombol back manual
        requireActivity().findViewById<View>(R.id.home).visibility = View.VISIBLE
        requireActivity().findViewById<View>(R.id.overlay_fragment_container).visibility = View.GONE
    }

}