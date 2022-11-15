package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amaromerovic.projemanag.activities.TaskListActivity
import com.amaromerovic.projemanag.databinding.CardItemBinding
import com.amaromerovic.projemanag.models.Card
import com.amaromerovic.projemanag.models.SelectedMembers

class CardListAdapter(
    private val context: Context,
    private val cards: ArrayList<Card>
) :
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
        val model = cards[holder.adapterPosition]
        holder.binding.cardName.text = model.name

        holder.itemView.setOnClickListener {
            onCardClickListener?.onCardClick(holder.adapterPosition, model)
        }

        val color: String = model.labelColor
        if (color.isNotEmpty()) {
            holder.binding.labelColor.visibility = View.VISIBLE
            holder.binding.labelColor.setBackgroundColor(Color.parseColor(color))
        } else {
            holder.binding.labelColor.visibility = View.GONE
        }

        if ((context as TaskListActivity).assignedMemberDetailList.size > 0) {
            val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

            for (i in context.assignedMemberDetailList.indices) {
                for (j in model.assignedTo) {
                    if (context.assignedMemberDetailList[i].id == j) {
                        val selectedMembers = context.assignedMemberDetailList[i].id?.let {
                            context.assignedMemberDetailList[i].image?.let { it1 ->
                                SelectedMembers(
                                    it, it1
                                )
                            }
                        }
                        if (selectedMembers != null) {
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
            }

            if (selectedMembersList.size > 0) {
                if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                    holder.binding.recyclerViewSelectedMembersList.visibility = View.GONE
                } else {
                    holder.binding.recyclerViewSelectedMembersList.visibility = View.VISIBLE
                    holder.binding.recyclerViewSelectedMembersList.layoutManager =
                        GridLayoutManager(context, 5)
                    val adapter = CardMemberListAdapter(context, selectedMembersList, false)
                    holder.binding.recyclerViewSelectedMembersList.adapter = adapter
                    adapter.onCardMemberClick(object :
                        CardMemberListAdapter.OnCardMemberClickListener {
                        override fun onCardMemberClick() {
                            onCardClickListener?.onCardClick(holder.adapterPosition, model)
                        }
                    })
                }
            } else {
                holder.binding.recyclerViewSelectedMembersList.visibility = View.GONE
            }
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