package com.ioreum.app_jurnal_pkl

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONArray

class JurnalFragment : Fragment() {

    private lateinit var tableJurnal: TableLayout
    private lateinit var btnTambah: Button
    private val urlTampilJurnal = "http://192.168.36.139/jurnal_pkl/tampil_jurnal.php"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_jurnal, container, false)
        tableJurnal = view.findViewById(R.id.tableJurnal)
        btnTambah = view.findViewById(R.id.btnTambahJurnal)

        // Tombol Tambah
        btnTambah.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TambahJurnalFragment())
                .addToBackStack(null)
                .commit()
        }

        tampilkanHeader()
        loadDataJurnal()

        return view
    }

    private fun tampilkanHeader() {
        val row = TableRow(requireContext())
        val headers = listOf("No", "NIS", "Nama", "Tanggal", "Uraian", "Catatan", "Paraf")
        headers.forEach {
            val tv = TextView(requireContext()).apply {
                text = it
                setPadding(16, 12, 16, 12)
                setTextColor(0xFF000000.toInt())
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            row.addView(tv)
        }
        tableJurnal.addView(row)
    }

    private fun loadDataJurnal() {
        val request = StringRequest(urlTampilJurnal, { response ->
            try {
                val array = JSONArray(response)

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val row = TableRow(requireContext())

                    val no = (i + 1).toString()
                    val nis = obj.getString("nis")
                    val nama = obj.getString("nama_siswa")
                    val tanggal = obj.getString("tanggal_kegiatan")
                    val uraian = obj.getString("uraian_kegiatan")
                    val catatan = obj.getString("catatan_pembimbing")
                    val fotoParaf = obj.getString("paraf_pembimbing")

                    val dataTeks = listOf(no, nis, nama, tanggal, uraian, catatan)

                    dataTeks.forEach {
                        val tv = TextView(requireContext()).apply {
                            text = it
                            setPadding(16, 12, 16, 12)
                            gravity = Gravity.CENTER  // Menengahkan isi teks
                            textAlignment = View.TEXT_ALIGNMENT_CENTER  // Tambahan (optional)
                            setTextColor(0xFF000000.toInt())
                            layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.CENTER  // Menengahkan TextView di dalam TableRow
                            }
                        }
                        row.addView(tv)
                    }

                    // ImageView untuk foto paraf
                    val imageView = ImageView(requireContext()).apply {
                        layoutParams = TableRow.LayoutParams(200, 200)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    if (fotoParaf.isNotEmpty()) {
                        val urlFoto = "http://192.168.36.139/jurnal_pkl/foto/$fotoParaf"
                        Log.d("JurnalFragment", "Load image URL: $urlFoto")
                        Glide.with(this)
                            .load(urlFoto)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.error_image)
                            .into(imageView)
                    } else {
                        // Jika tidak ada foto, tampilkan placeholder
                        imageView.setImageResource(R.drawable.placeholder)
                    }

                    row.addView(imageView)
                    tableJurnal.addView(row)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal parsing data", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }, {
            Toast.makeText(requireContext(), "Gagal memuat data jurnal", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
