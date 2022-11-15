package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.MemberItemBinding
import com.amaromerovic.projemanag.models.User
import com.bumptech.glide.Glide

class MemberListAdapter(private val context: Context, private val list: ArrayList<User>) :
    RecyclerView.Adapter<MemberListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MemberItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.binding.memberName.text = model.name
        holder.binding.memberEmail.text = model.email

        Glide
            .with(context)
            .load(model.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.binding.memberImage)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: MemberItemBinding) : RecyclerView.ViewHolder(binding.root)
}