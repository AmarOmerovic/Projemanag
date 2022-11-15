package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ItemCardSelectedMemberBinding
import com.amaromerovic.projemanag.models.SelectedMembers
import com.bumptech.glide.Glide

open class CardMemberListAdapter(
    private val context: Context,
    private val list: ArrayList<SelectedMembers>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<CardMemberListAdapter.ViewHolder>() {

    private var onCardMemberClickListener: OnCardMemberClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCardSelectedMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]


        if (position == list.size - 1 && assignMembers) {
            holder.binding.addMember.visibility = View.VISIBLE
            holder.binding.selectedMemberImage.visibility = View.GONE
        } else {
            holder.binding.addMember.visibility = View.GONE
            holder.binding.selectedMemberImage.visibility = View.VISIBLE

            Glide
                .with(context)
                .load(model.image)
                .fitCenter()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.binding.selectedMemberImage)

        }

        holder.itemView.setOnClickListener {
            onCardMemberClickListener?.onCardMemberClick()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun onCardMemberClick(onCardMemberClickListener: OnCardMemberClickListener) {
        this.onCardMemberClickListener = onCardMemberClickListener
    }

    class ViewHolder(val binding: ItemCardSelectedMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnCardMemberClickListener {
        fun onCardMemberClick()
    }
}