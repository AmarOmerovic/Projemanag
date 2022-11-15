package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.MemberItemDialogBinding
import com.amaromerovic.projemanag.models.User
import com.amaromerovic.projemanag.utils.Constants
import com.bumptech.glide.Glide

class MemberListAdapterDialog(private val context: Context, private val list: ArrayList<User>) :
    RecyclerView.Adapter<MemberListAdapterDialog.ViewHolder>() {

    private var onMemberClickListener: OnMemberClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MemberItemDialogBinding.inflate(
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

        if (model.selected) {
            holder.binding.selectedMember.visibility = View.VISIBLE
        } else {
            holder.binding.selectedMember.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (onMemberClickListener != null) {
                if (model.selected) {
                    onMemberClickListener!!.onMemberClick(position, model, Constants.UN_SELECT)
                } else {
                    onMemberClickListener!!.onMemberClick(position, model, Constants.SELECT)
                }
            }
        }
    }

    fun onMemberClick(onMemberClickListener: OnMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: MemberItemDialogBinding) : RecyclerView.ViewHolder(binding.root)


    interface OnMemberClickListener {
        fun onMemberClick(position: Int, user: User, action: String)
    }
}