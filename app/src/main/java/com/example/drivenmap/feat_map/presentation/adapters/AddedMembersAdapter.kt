package com.example.drivenmap.feat_map.presentation.adapters

import android.location.GnssAntennaInfo.SphericalCorrections
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.drivenmap.R
import com.example.drivenmap.databinding.AddedMemberItemBinding
import com.example.drivenmap.feat_map.domain.models.AddedUser
import com.google.maps.android.SphericalUtil

class AddedMembersAdapter:RecyclerView.Adapter<AddedMembersAdapter.ViewHolder>() {
    private val callback = object: DiffUtil.ItemCallback<AddedUser>(){
        override fun areItemsTheSame(oldItem: AddedUser, newItem: AddedUser): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AddedUser, newItem: AddedUser): Boolean {
            return oldItem.toString() == newItem.toString()
        }
    }
    private val differ = AsyncListDiffer(this,callback)

    fun setData(list:List<AddedUser>){
        differ.submitList(list)
    }
    inner class ViewHolder(private val binding:AddedMemberItemBinding):RecyclerView.ViewHolder(binding.root) {
        fun setData(item: AddedUser, position: Int) {
            binding.apply {
                item.apply {
                    addedMemberItemIvDisplayPhoto.load(R.drawable.baseline_person_pin_24){
                        placeholder(R.drawable.baseline_person_pin_24)
                        transformations(CircleCropTransformation())
                    }
                    addedMemberItemTvDistanceAway.text = distanceAway
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AddedMemberItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.setData(item,position)
    }
}