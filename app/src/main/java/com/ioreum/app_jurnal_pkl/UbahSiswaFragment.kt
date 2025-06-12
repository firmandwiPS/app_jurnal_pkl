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

    private val urlUbah = "http://192.168.36.139/jurnal_pkl/ubah_siswa.php"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        // Ambil data dari argument
        val bundle = arguments
        val nisLama = bundle?.getString("nis") ?: ""
        etNis.setText(nisLama)
        etNama.setText(bundle?.getString("nama_siswa"))
        etAsalSekolah.setText(bundle?.getString("asal_sekolah"))
        etTanggalMulai.setText(bundle?.getString("tanggal_mulai"))
        etTanggalSelesai.setText(bundle?.getString("tanggal_selesai"))
        etNoHp.setText(bundle?.getString("no_hp"))
        etAlamat.setText(bundle?.getString("alamat"))

        // Spinner Jenis Kelamin
        val jenisKelaminList = listOf("LAKI-LAKI", "PEREMPUAN")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisKelaminList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spJenisKelamin.adapter = adapter

        val jenisKelamin = bundle?.getString("jenis_kelamin") ?: ""
        val selectedIndex = jenisKelaminList.indexOf(jenisKelamin.uppercase())
        if (selectedIndex >= 0) {
            spJenisKelamin.setSelection(selectedIndex)
        }

        // Date picker
        etTanggalMulai.setOnClickListener { showDatePicker(etTanggalMulai) }
        etTanggalSelesai.setOnClickListener { showDatePicker(etTanggalSelesai) }

        // Tombol kembali
        btnKembali.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Simpan
        btnSimpan.setOnClickListener {
            val request = object : StringRequest(Method.POST, urlUbah, {
                Toast.makeText(requireContext(), "Data berhasil diubah", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }, {
                Toast.makeText(requireContext(), "Gagal mengubah data", Toast.LENGTH_SHORT).show()
            }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["nis_lama"] = nisLama
                    params["nis"] = etNis.text.toString()
                    params["nama_siswa"] = etNama.text.toString()
                    params["jenis_kelamin"] = spJenisKelamin.selectedItem.toString()
                    params["asal_sekolah"] = etAsalSekolah.text.toString()
                    params["tanggal_mulai"] = etTanggalMulai.text.toString()
                    params["tanggal_selesai"] = etTanggalSelesai.text.toString()
                    params["no_hp"] = etNoHp.text.toString()
                    params["alamat"] = etAlamat.text.toString()
                    return params
                }
            }

            Volley.newRequestQueue(requireContext()).add(request)
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
}
