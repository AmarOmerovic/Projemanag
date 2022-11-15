package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaromerovic.projemanag.databinding.CardItemBinding
import com.amaromerovic.projemanag.models.Card

class CardListAdapter(private val context: Context, private val cards: ArrayList<Card>) :
    RecyclerView.Adapter<CardListAdapter.ViewHolder>() {

    private var onCardClickListener: OnCardClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = cards[position]
        holder.binding.cardName.text = model.name

        holder.itemView.setOnClickListener {
            onCardClickListener?.onCardClick(position, model)
        }

        val color: String = model.labelColor
        if (color.isNotEmpty()) {
            holder.binding.labelColor.visibility = View.VISIBLE
            holder.binding.labelColor.setBackgroundColor(Color.parseColor(color))
        }
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    fun setOnCardClick(onCardClickListener: OnCardClickListener) {
        this.onCardClickListener = onCardClickListener
    }

    class ViewHolder(val binding: CardItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnCardClickListener {
        fun onCardClick(cardPosition: Int, card: Card)
    }

}