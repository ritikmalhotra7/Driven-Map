package com.example.drivenmap.feat_map.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.drivenmap.databinding.LayoutAddedMemberItemBinding
import com.example.drivenmap.feat_map.data.dto.UserXDto

class AddedMembersAdapter:RecyclerView.Adapter<AddedMembersAdapter.ViewHolder>() {

    private var clickListener:((UserXDto)->Unit)? = null
    private val callback = object: DiffUtil.ItemCallback<UserXDto>(){
        override fun areItemsTheSame(oldItem: UserXDto, newItem: UserXDto): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: UserXDto, newItem: UserXDto): Boolean {
            return oldItem.toString() == newItem.toString()
        }
    }
    private val differ = AsyncListDiffer(this,callback)

    fun setData(list:List<UserXDto>){
        differ.submitList(list)
    }
    fun getList(): MutableList<UserXDto> = differ.currentList
    inner class ViewHolder(private val binding:LayoutAddedMemberItemBinding):RecyclerView.ViewHolder(binding.root) {
        fun setData(item: UserXDto, position: Int) {
            binding.apply {
                item.apply {
                    root.setOnClickListener {
                        clickListener?.let{
                            it(item)
                        }
                    }
                    tvEmail.text = item.email
                    ivProfile.load(item.profilePhoto){
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }
    }

    fun setClickListener(onClickItem:(UserXDto)->Unit){
        this.clickListener = onClickItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutAddedMemberItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.setData(item,position)
    }
}