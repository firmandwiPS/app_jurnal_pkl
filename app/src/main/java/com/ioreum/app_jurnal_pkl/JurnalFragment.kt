package com.ioreum.app_jurnal_pkl

import android.app.AlertDialog
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
    private val urlHapusJurnal = "http://192.168.36.139/jurnal_pkl/hapus_jurnal.php"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_jurnal, container, false)
        tableJurnal = view.findViewById(R.id.tableJurnal)
        btnTambah = view.findViewById(R.id.btnTambahJurnal)

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
        val headers = listOf("No", "NIS", "Nama", "Tanggal", "Uraian", "Catatan", "Paraf", "Aksi")
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
        tableJurnal.removeViews(1, tableJurnal.childCount - 1) // Hapus data lama

        val request = StringRequest(urlTampilJurnal, { response ->
            try {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val row = TableRow(requireContext())

                    val idJurnal = obj.getString("id_jurnal") // Dapatkan id_jurnal
                    val nis = obj.getString("nis")
                    val nama = obj.getString("nama_siswa")
                    val tanggal = obj.getString("tanggal_kegiatan")
                    val uraian = obj.getString("uraian_kegiatan")
                    val catatan = obj.getString("catatan_pembimbing")
                    val fotoParaf = obj.getString("paraf_pembimbing")

                    val dataTeks = listOf((i + 1).toString(), nis, nama, tanggal, uraian, catatan)

                    dataTeks.forEach {
                        val tv = TextView(requireContext()).apply {
                            text = it
                            setPadding(16, 12, 16, 12)
                            gravity = Gravity.CENTER  // Isi teks di tengah (vertikal dan horizontal)
                            textAlignment = View.TEXT_ALIGNMENT_CENTER // Untuk dukungan tambahan
                            setTextColor(0xFF000000.toInt())

                            // Set layout parameter agar posisinya juga di tengah dalam TableRow
                            layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.CENTER
                            }
                        }
                        row.addView(tv)
                    }


                    val imageView = ImageView(requireContext()).apply {
                        layoutParams = TableRow.LayoutParams(200, 200)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    if (fotoParaf.isNotEmpty()) {
                        val urlFoto = "http://192.168.36.139/jurnal_pkl/foto/$fotoParaf"
                        Glide.with(this)
                            .load(urlFoto)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.error_image)
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.placeholder)
                    }

                    row.addView(imageView)

                    val btnHapus = Button(requireContext()).apply {
                        text = "Hapus"
                        textSize = 12f
                        setOnClickListener {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Konfirmasi")
                                .setMessage("Yakin ingin menghapus jurnal ID: $idJurnal?")
                                .setPositiveButton("Ya") { _, _ ->
                                    hapusJurnal(idJurnal)
                                }
                                .setNegativeButton("Batal", null)
                                .show()
                        }
                    }

                    val aksiLayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        addView(btnHapus)
                    }

                    row.addView(aksiLayout)
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

    private fun hapusJurnal(idJurnal: String) {
        val request = object : StringRequest(Method.POST, urlHapusJurnal,
            { response ->
                if (response == "sukses") {
                    Toast.makeText(requireContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadDataJurnal()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(requireContext(), "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_jurnal" to idJurnal)
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
