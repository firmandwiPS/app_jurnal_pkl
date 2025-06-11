package com.ioreum.app_jurnal_pkl

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SiswaFragment : Fragment() {

    private lateinit var tableSiswa: TableLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_siswa, container, false)
        tableSiswa = view.findViewById(R.id.tableSiswa)

        view.findViewById<Button>(R.id.btnTambahSiswa).setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Tambah_siswaFragment())
                .addToBackStack(null)
                .commit()
        }

        tampilkanDataSiswa()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("refresh_siswa") { _, bundle ->
            if (bundle.getBoolean("siswa_added", false)) {
                Toast.makeText(requireContext(), "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                tableSiswa.removeAllViews()
                tampilkanDataSiswa()
            }
        }
    }

    private fun tampilkanDataSiswa() {
        val url = "http://192.168.36.189/jurnal_pkl/tampil_siswa.php"
        val request = JsonArrayRequest(url,
            { response ->
                tampilkanHeader()
                for (i in 0 until response.length()) {
                    tambahBarisSiswa(i + 1, response.getJSONObject(i))
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Gagal ambil data: $error", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun tampilkanHeader() {
        val row = TableRow(requireContext())
        val headers = listOf("No", "NIS", "Nama", "JK", "Asal", "Mulai", "Selesai", "No HP", "Alamat")
        for (header in headers) {
            val tv = TextView(requireContext())
            tv.text = header
            tv.setPadding(16, 12, 16, 12)
            tv.setTextColor(0xFF000000.toInt())
            tv.setTypeface(null, android.graphics.Typeface.BOLD)
            row.addView(tv)
        }
        tableSiswa.addView(row)
    }

    private fun tambahBarisSiswa(nomor: Int, obj: JSONObject) {
        val row = TableRow(requireContext())
        val listData = listOf(
            nomor.toString(),
            obj.optString("nis"),
            obj.optString("nama_siswa"),
            obj.optString("jenis_kelamin"),
            obj.optString("asal_sekolah"),
            obj.optString("tanggal_mulai"),
            obj.optString("tanggal_selesai"),
            obj.optString("no_hp"),
            obj.optString("alamat")
        )

        for (data in listData) {
            val tv = TextView(requireContext())
            tv.text = data
            tv.setPadding(16, 8, 16, 8)
            row.addView(tv)
        }
        tableSiswa.addView(row)
    }
}
