package com.example.drivenmap.feat_map.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.drivenmap.databinding.MemberCardItemBinding
import com.example.drivenmap.feat_map.domain.models.AddedUser

class AddMemberBottomSheetAdapter: RecyclerView.Adapter<AddMemberBottomSheetAdapter.ViewHolder>() {

    private var clickListener: ((AddedUser) -> Unit)? = null
    private val callback = object : DiffUtil.ItemCallback<AddedUser>() {
        override fun areItemsTheSame(oldItem: AddedUser, newItem: AddedUser): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AddedUser, newItem: AddedUser): Boolean {
            return oldItem.toString() == newItem.toString()
        }
    }
    private val differ = AsyncListDiffer(this, callback)

    fun setData(list: List<AddedUser>) {
        differ.submitList(list)
    }

    inner class ViewHolder(private val binding: MemberCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(item: AddedUser, position: Int) {
            if(position==0){
                binding.root.isEnabled = false
            }
            binding.apply {
                item.apply {
                    memberCardItemTvName.text = name
                    root.setOnClickListener {
                        differ.submitList(differ.currentList.filter { it != item })
                    }
                }
            }
        }
    }

    fun setClickListener(onClickItem: (AddedUser) -> Unit) {
        this.clickListener = onClickItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MemberCardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.setData(item, position)
    }
}