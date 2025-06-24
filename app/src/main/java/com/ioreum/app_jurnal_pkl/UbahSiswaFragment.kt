package com.ioreum.app_jurnal_pkl

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class UbahSiswaFragment : Fragment() {

    private lateinit var etNis: EditText
    private lateinit var etNama: EditText
    private lateinit var spJenisKelamin: Spinner
    private lateinit var etAsalSekolah: EditText
    private lateinit var etTanggalMulai: EditText
    private lateinit var etTanggalSelesai: EditText
    private lateinit var etNoHp: EditText
    private lateinit var etAlamat: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnKembali: ImageButton

    private val urlUbah = "http://172.16.100.11/jurnal_pkl/siswa/ubah_siswa.php"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ubah_siswa, container, false)

        etNis = view.findViewById(R.id.etNis)
        etNama = view.findViewById(R.id.etNama)
        spJenisKelamin = view.findViewById(R.id.spJenisKelamin)
        etAsalSekolah = view.findViewById(R.id.etAsalSekolah)
        etTanggalMulai = view.findViewById(R.id.etTanggalMulai)
        etTanggalSelesai = view.findViewById(R.id.etTanggalSelesai)
        etNoHp = view.findViewById(R.id.etNoHp)
        etAlamat = view.findViewById(R.id.etAlamat)
        btnSimpan = view.findViewById(R.id.btnSimpan)
        btnKembali = view.findViewById(R.id.btnKembali)

        val bundle = arguments
        val nisLama = bundle?.getString("nis") ?: ""
        etNis.setText(nisLama)
        etNama.setText(bundle?.getString("nama_siswa"))
        etAsalSekolah.setText(bundle?.getString("asal_sekolah"))
        etTanggalMulai.setText(bundle?.getString("tanggal_mulai"))
        etTanggalSelesai.setText(bundle?.getString("tanggal_selesai"))
        etNoHp.setText(bundle?.getString("no_hp"))
        etAlamat.setText(bundle?.getString("alamat"))

        val jenisKelaminList = listOf("LAKI-LAKI", "PEREMPUAN")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisKelaminList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spJenisKelamin.adapter = adapter

        val jenisKelamin = bundle?.getString("jenis_kelamin") ?: ""
        val selectedIndex = jenisKelaminList.indexOf(jenisKelamin.uppercase())
        if (selectedIndex >= 0) {
            spJenisKelamin.setSelection(selectedIndex)
        }

        etTanggalMulai.setOnClickListener { showDatePicker(etTanggalMulai) }
        etTanggalSelesai.setOnClickListener { showDatePicker(etTanggalSelesai) }

        btnKembali.setOnClickListener {
            closeThisFragment()
        }

        btnSimpan.setOnClickListener {
            simpanPerubahan(nisLama)
        }

        return view
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, y, m, d ->
            val date = "$y-${String.format("%02d", m + 1)}-${String.format("%02d", d)}"
            editText.setText(date)
        }, year, month, day).show()
    }

    private fun simpanPerubahan(nisLama: String) {
        val request = object : StringRequest(Method.POST, urlUbah,
            { response ->
                try {
                    val json = JSONObject(response)
                    val status = json.getString("status")
                    val message = json.optString("message", "")

                    if (status == "success") {
                        Toast.makeText(requireContext(), "✅ Data berhasil diubah", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.setFragmentResult("refreshSiswa", Bundle())
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "⚠️ $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "❌ Terjadi kesalahan parsing", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(requireContext(), "❌ Gagal koneksi: ${it.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "nis_lama" to nisLama,
                    "nis" to etNis.text.toString(),
                    "nama_siswa" to etNama.text.toString(),
                    "jenis_kelamin" to spJenisKelamin.selectedItem.toString(),
                    "asal_sekolah" to etAsalSekolah.text.toString(),
                    "tanggal_mulai" to etTanggalMulai.text.toString(),
                    "tanggal_selesai" to etTanggalSelesai.text.toString(),
                    "no_hp" to etNoHp.text.toString(),
                    "alamat" to etAlamat.text.toString()
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
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