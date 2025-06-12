package com.ioreum.app_jurnal_pkl

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class SiswaFragment : Fragment() {

    private lateinit var tableSiswa: TableLayout
    private val urlTampil = "http://192.168.36.139/jurnal_pkl/tampil_siswa.php"
    private val urlHapus = "http://192.168.36.139/jurnal_pkl/hapus_siswa.php"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_siswa, container, false)
        tableSiswa = view.findViewById(R.id.tableSiswa)

        val btnTambah = view.findViewById<Button>(R.id.btnTambahSiswa)
        btnTambah.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, Tambah_siswaFragment())
                addToBackStack(null)
            }
        }

        tampilkanHeader()
        loadDataSiswa()
        return view
    }

    private fun tampilkanHeader() {
        val row = TableRow(requireContext())
        val headers = listOf("No", "NIS", "Nama", "Jenis Kelamain", "Asal Sekolah", "Mulai PKL", "Selesai PKL", "Nomer HP", "Alamat", "Aksi")
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
        tableSiswa.addView(row)
    }

    private fun loadDataSiswa() {
        val request = StringRequest(urlTampil, { response ->
            val array = JSONArray(response)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val row = TableRow(requireContext())

                val data = listOf(
                    (i + 1).toString(),
                    obj.getString("nis"),
                    obj.getString("nama_siswa"),
                    obj.getString("jenis_kelamin"),
                    obj.getString("asal_sekolah"),
                    obj.getString("tanggal_mulai"),
                    obj.getString("tanggal_selesai"),
                    obj.getString("no_hp"),
                    obj.getString("alamat")
                )

                data.forEach {
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

                val aksiLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(8, 0, 8, 0)
                }

                val btnUbah = Button(requireContext()).apply {
                    text = "Ubah"
                    textSize = 12f
                    setOnClickListener {
                        val fragment = UbahSiswaFragment().apply {
                            arguments = Bundle().apply {
                                putString("nis", obj.getString("nis"))
                                putString("nama_siswa", obj.getString("nama_siswa"))
                                putString("jenis_kelamin", obj.getString("jenis_kelamin"))
                                putString("asal_sekolah", obj.getString("asal_sekolah"))
                                putString("tanggal_mulai", obj.getString("tanggal_mulai"))
                                putString("tanggal_selesai", obj.getString("tanggal_selesai"))
                                putString("no_hp", obj.getString("no_hp"))
                                putString("alamat", obj.getString("alamat"))
                            }
                        }
                        parentFragmentManager.commit {
                            replace(R.id.fragment_container, fragment)
                            addToBackStack(null)
                        }
                    }
                }

                val btnHapus = Button(requireContext()).apply {
                    text = "Hapus"
                    textSize = 12f
                    setOnClickListener {
                        val alertDialog = android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Konfirmasi Hapus")
                            .setMessage("Yakin ingin menghapus data siswa ${obj.getString("nama_siswa")} dengan NIS: ${obj.getString("nis")}?")
                            .setPositiveButton("Ya") { _, _ ->
                                hapusData(obj.getString("nis"))
                            }
                            .setNegativeButton("Tidak") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()

                        // Setelah dialog muncul, ubah warna tombol
                        alertDialog.setOnShowListener {
                            alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                            alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                        }

                        alertDialog.show()
                    }
                }


                aksiLayout.addView(btnUbah)
                aksiLayout.addView(btnHapus)
                row.addView(aksiLayout)

                tableSiswa.addView(row)
            }
        }, {
            Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun hapusData(nis: String) {
        val request = object : StringRequest(Method.POST, urlHapus, {
            Toast.makeText(requireContext(), "Data dihapus", Toast.LENGTH_SHORT).show()
            tableSiswa.removeAllViews()
            tampilkanHeader()
            loadDataSiswa()
        }, {
            Toast.makeText(requireContext(), "Gagal hapus data", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("nis" to nis)
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }
}
