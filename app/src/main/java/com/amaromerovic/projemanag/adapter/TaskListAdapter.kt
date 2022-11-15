package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.amaromerovic.projemanag.databinding.TaskItemBinding
import com.amaromerovic.projemanag.models.Task

class TaskListAdapter(context: Context, private val taskList: ArrayList<Task>) :
    RecyclerView.Adapter<TaskListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = TaskItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins(15.toDP().toPX(), 0, (40.toDP()).toPX(), 0)

        view.root.layoutParams = layoutParams

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = taskList[position]
        if (position == taskList.size - 1) {
            holder.binding.addTaskList.visibility = View.VISIBLE
            holder.binding.linearLayoutTaskItem.visibility = View.GONE
        } else {
            holder.binding.addTaskList.visibility = View.GONE
            holder.binding.linearLayoutTaskItem.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    class ViewHolder(val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPX(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}