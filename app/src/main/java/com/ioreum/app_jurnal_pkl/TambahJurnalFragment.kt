package com.ioreum.app_jurnal_pkl

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class TambahJurnalFragment : Fragment() {

    private lateinit var spinnerNis: Spinner
    private lateinit var editTanggal: EditText
    private lateinit var editUraian: EditText
    private lateinit var editCatatan: EditText
    private lateinit var imagePreview: ImageView
    private lateinit var btnSimpan: Button
    private lateinit var btnPilihFoto: Button
    private var selectedImageUri: Uri? = null
    private lateinit var btnKembali: ImageButton


    private val urlTambah = "http://192.168.36.139/jurnal_pkl/tambah_jurnal.php"
    private val urlDaftarSiswa = "http://192.168.36.139/jurnal_pkl/daftar_siswa.php"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_jurnal, container, false)

        spinnerNis = view.findViewById(R.id.spinnerNis)
        editTanggal = view.findViewById(R.id.editTanggal)
        editUraian = view.findViewById(R.id.editUraian)
        editCatatan = view.findViewById(R.id.editCatatan)
        imagePreview = view.findViewById(R.id.imagePreview)
        btnSimpan = view.findViewById(R.id.btnSimpan)
        btnPilihFoto = view.findViewById(R.id.btnPilihFoto)
        btnKembali = view.findViewById(R.id.btnKembali)

        // Tombol kembali
        btnKembali.setOnClickListener {
            closeThisFragment()
        }

        loadNis()

        editTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val date = "$y-${"%02d".format(m+1)}-${"%02d".format(d)}"
                editTanggal.setText(date)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnPilihFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        btnSimpan.setOnClickListener {
            uploadData()
        }

        return view
    }

    private fun loadNis() {
        val listNis = mutableListOf<String>()
        val queue = Volley.newRequestQueue(requireContext())
        val request = StringRequest(urlDaftarSiswa, { response ->
            val json = org.json.JSONArray(response)
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                listNis.add("${obj.getString("nis")} - ${obj.getString("nama_siswa")}")
            }
            spinnerNis.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listNis)
        }, {})
        queue.add(request)
    }

    private fun uploadData() {
        val nis = spinnerNis.selectedItem?.toString()?.split(" - ")?.get(0) ?: ""
        val tanggal = editTanggal.text.toString().trim()
        val uraian = editUraian.text.toString().trim()
        val catatan = editCatatan.text.toString().trim()

        if (nis.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih NIS terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        if (tanggal.isEmpty()) {
            editTanggal.error = "Tanggal harus diisi"
            editTanggal.requestFocus()
            return
        }

        if (uraian.isEmpty()) {
            editUraian.error = "Uraian kegiatan harus diisi"
            editUraian.requestFocus()
            return
        }

        if (catatan.isEmpty()) {
            editCatatan.error = "Catatan pembimbing harus diisi"
            editCatatan.requestFocus()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Pilih foto paraf terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        val inputStream: InputStream? = selectedImageUri?.let {
            requireActivity().contentResolver.openInputStream(it)
        }

        val byteArray = inputStream?.readBytes()

        val request = object : VolleyMultipartRequest(
            Method.POST, urlTambah,
            { response ->
                Toast.makeText(requireContext(), "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.setFragmentResult("refreshJurnal", Bundle())
                closeThisFragment()
            },
            {
                Toast.makeText(requireContext(), "Gagal upload", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getByteData(): Map<String, DataPart> {
                val map = HashMap<String, DataPart>()
                map["paraf_pembimbing"] = DataPart("paraf.jpg", byteArray!!, "image/jpeg")
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "nis" to nis,
                    "tanggal_kegiatan" to tanggal,
                    "uraian_kegiatan" to uraian,
                    "catatan_pembimbing" to catatan
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && data != null && data.data != null) {
            selectedImageUri = data.data
            val inputStream = requireActivity().contentResolver.openInputStream(selectedImageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imagePreview.setImageBitmap(bitmap)
        }
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