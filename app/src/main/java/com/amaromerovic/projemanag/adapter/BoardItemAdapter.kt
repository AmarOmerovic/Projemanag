package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.BoardItemBinding
import com.amaromerovic.projemanag.models.Board
import com.bumptech.glide.Glide

class BoardItemAdapter(private val context: Context, private val boards: ArrayList<Board>) :
    RecyclerView.Adapter<BoardItemAdapter.ViewHolder>() {

    private var onBoardItemClick: OnBoardItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BoardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.boardName.text = boards[position].name
        holder.binding.createdBy.text = buildString {
            append("Created by: ")
            append(boards[position].createdBy)
        }

        Glide
            .with(context)
            .load(boards[position].image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.binding.itemImage)

        holder.itemView.setOnClickListener {
            onBoardItemClick?.onItemClick(position, boards[position])
        }
    }

    override fun getItemCount(): Int {
        return boards.size
    }

    class ViewHolder(val binding: BoardItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnBoardItemClick {
        fun onItemClick(position: Int, model: Board)
    }

    fun onBoardItemClickListener(onBoardItemClick: OnBoardItemClick) {
        this.onBoardItemClick = onBoardItemClick
    }

}