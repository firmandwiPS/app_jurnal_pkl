package com.ioreum.app_jurnal_pkl

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class UbahJurnalFragment : Fragment() {

    private lateinit var spinnerNis: Spinner
    private lateinit var etTanggal: EditText
    private lateinit var etUraian: EditText
    private lateinit var etCatatan: EditText
    private lateinit var ivParaf: ImageView
    private lateinit var btnPilihParaf: Button
    private lateinit var btnSimpan: Button
    private lateinit var btnKembali: ImageButton

    private var idJurnal: String = ""
    private var oldFileName: String = ""
    private var selectedUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it
            ivParaf.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ubah_jurnal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        spinnerNis = view.findViewById(R.id.spinnerNis)
        etTanggal = view.findViewById(R.id.etTanggal)
        etUraian = view.findViewById(R.id.etUraian)
        etCatatan = view.findViewById(R.id.etCatatan)
        ivParaf = view.findViewById(R.id.ivParaf)
        btnPilihParaf = view.findViewById(R.id.btnPilihParaf)
        btnSimpan = view.findViewById(R.id.btnSimpan)
        btnKembali = view.findViewById(R.id.btnKembali)

        idJurnal = requireArguments().getString("id_jurnal") ?: ""

        btnKembali.setOnClickListener { parentFragmentManager.popBackStack() }
        btnPilihParaf.setOnClickListener { pickImage.launch("image/*") }
        etTanggal.setOnClickListener { showDatePicker(etTanggal) }

        loadSiswa()
        loadJurnal()

        btnSimpan.setOnClickListener {
            if (etTanggal.text.isBlank() || etUraian.text.isBlank()) {
                Toast.makeText(requireContext(), "Tanggal dan uraian wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadUpdate()
        }
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

    private fun loadSiswa() {
        val url = "http://192.168.36.139/jurnal_pkl/ambil_siswa.php"
        val request = StringRequest(Request.Method.GET, url,
            { response ->
                val jsonArray = JSONArray(response)
                val siswaList = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    siswaList.add("${obj.getString("nis")} - ${obj.getString("nama_siswa")}")
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, siswaList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNis.adapter = adapter
            },
            { error ->
                Toast.makeText(requireContext(), "Gagal memuat data siswa", Toast.LENGTH_SHORT).show()
            })
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun loadJurnal() {
        val url = "http://192.168.36.139/jurnal_pkl/ambil_jurnal_by_id.php"
        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                val json = JSONObject(response)
                val data = json.getJSONObject("jurnal")
                etTanggal.setText(data.getString("tanggal_kegiatan"))
                etUraian.setText(data.getString("uraian_kegiatan"))
                etCatatan.setText(data.getString("catatan_pembimbing"))

                oldFileName = data.getString("paraf_pembimbing")
                Glide.with(this)
                    .load("http://192.168.36.139/jurnal_pkl/foto/$oldFileName")
                    .placeholder(R.drawable.placeholder)
                    .into(ivParaf)

                val nis = data.getString("nis")
                for (i in 0 until spinnerNis.count) {
                    if (spinnerNis.getItemAtPosition(i).toString().startsWith(nis)) {
                        spinnerNis.setSelection(i)
                        break
                    }
                }
            },
            {
                Toast.makeText(requireContext(), "Gagal memuat data jurnal", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_jurnal" to idJurnal)
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun uploadUpdate() {
        val url = "http://192.168.36.139/jurnal_pkl/ubah_jurnal.php"
        val multipartRequest = object : VolleyMultipartRequest(Method.POST, url,
            { response ->
                val json = JSONObject(String(response.data))
                if (json.getString("status") == "success") {
                    Toast.makeText(requireContext(), "Berhasil mengubah data", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengubah data", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(requireContext(), "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "id_jurnal" to idJurnal,
                    "nis" to spinnerNis.selectedItem.toString().split(" - ")[0],
                    "tanggal_kegiatan" to etTanggal.text.toString(),
                    "uraian_kegiatan" to etUraian.text.toString(),
                    "catatan_pembimbing" to etCatatan.text.toString(),
                    "old_file" to oldFileName
                )
            }

            override fun getByteData(): Map<String, DataPart> {
                return selectedUri?.let {
                    val inputStream = requireContext().contentResolver.openInputStream(it)
                    val bytes = inputStream?.readBytes() ?: return emptyMap()
                    mapOf("paraf_pembimbing" to DataPart("paraf_${System.currentTimeMillis()}.jpg", bytes))
                } ?: emptyMap()
            }
        }
        Volley.newRequestQueue(requireContext()).add(multipartRequest)
    }
}
