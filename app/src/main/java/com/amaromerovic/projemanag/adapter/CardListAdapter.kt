package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.view.LayoutInflater
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
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    fun setOnCardClick(onCardClickListener: OnCardClickListener) {
        this.onCardClickListener = onCardClickListener
    }

    class ViewHolder(val binding: CardItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnCardClickListener {
        fun onCardClick(position: Int, card: Card)
    }

}