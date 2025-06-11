package com.ioreum.app_jurnal_pkl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SiswaAdapter(private val siswaList: List<Siswa>) :
    RecyclerView.Adapter<SiswaAdapter.SiswaViewHolder>() {

    class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNis: TextView = itemView.findViewById(R.id.tvNis)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvJK: TextView = itemView.findViewById(R.id.tvJK)
        val tvAsal: TextView = itemView.findViewById(R.id.tvAsal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_siswa, parent, false)
        return SiswaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
        val siswa = siswaList[position]
        holder.tvNis.text = siswa.nis
        holder.tvNama.text = siswa.nama_siswa
        holder.tvJK.text = siswa.jenis_kelamin
        holder.tvAsal.text = siswa.asal_sekolah
    }

    override fun getItemCount(): Int = siswaList.size
}
