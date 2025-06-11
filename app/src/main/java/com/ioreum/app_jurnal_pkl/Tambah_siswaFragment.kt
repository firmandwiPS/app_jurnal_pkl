package com.ioreum.app_jurnal_pkl

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class Tambah_siswaFragment : Fragment() {

    private lateinit var etNis: EditText
    private lateinit var etNama: EditText
    private lateinit var etJK: EditText
    private lateinit var etAsal: EditText
    private lateinit var etMulai: EditText
    private lateinit var etSelesai: EditText
    private lateinit var etHP: EditText
    private lateinit var etAlamat: EditText
    private lateinit var btnSimpan: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tambah_siswa, container, false)

        etNis = view.findViewById(R.id.etNis)
        etNama = view.findViewById(R.id.etNama)
        etJK = view.findViewById(R.id.etJK)
        etAsal = view.findViewById(R.id.etAsal)
        etMulai = view.findViewById(R.id.etMulai)
        etSelesai = view.findViewById(R.id.etSelesai)
        etHP = view.findViewById(R.id.etHP)
        etAlamat = view.findViewById(R.id.etAlamat)
        btnSimpan = view.findViewById(R.id.btnSimpan)

        btnSimpan.setOnClickListener {
            if (validasiInput()) {
                simpanData()
            }
        }

        return view
    }

    private fun validasiInput(): Boolean {
        val inputs = listOf(
            Pair(etNis, "NIS wajib diisi"),
            Pair(etNama, "Nama wajib diisi"),
            Pair(etJK, "Jenis kelamin wajib diisi"),
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
        val url = "http://192.168.36.189/jurnal_pkl/tambah_siswa.php" // ganti IP jika perlu

        val stringRequest = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getString("status") == "success") {
                        Toast.makeText(requireContext(), "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        setFragmentResult("refreshSiswa", Bundle())
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Gagal tambah data: ${obj.optString("message")}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Gagal koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "nis" to etNis.text.toString(),
                    "nama_siswa" to etNama.text.toString(),
                    "jenis_kelamin" to etJK.text.toString(),
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
}
