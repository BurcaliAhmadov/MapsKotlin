package com.burcaliahmadov.mapskotlin.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.burcaliahmadov.mapskotlin.databinding.RecyclerRowBinding
import com.burcaliahmadov.mapskotlin.model.Place
import com.burcaliahmadov.mapskotlin.view.MapsActivity

class PlaceAdapter(var placeList: List<Place>): RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {
    class PlaceHolder (val rowBinding: RecyclerRowBinding): RecyclerView.ViewHolder(rowBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val rowBinding:RecyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.rowBinding.recyclerViewTextView.text = placeList[position].pName
        holder.itemView.setOnClickListener {
            val intent:Intent=Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("place",placeList[position])
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return placeList.size
    }
}

