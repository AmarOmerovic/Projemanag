package com.amaromerovic.projemanag.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.activities.TaskListActivity
import com.amaromerovic.projemanag.databinding.TaskItemBinding
import com.amaromerovic.projemanag.models.Task
import com.google.android.material.snackbar.Snackbar

class TaskListAdapter(private val context: Context, private val taskList: ArrayList<Task>) :
    RecyclerView.Adapter<TaskListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = TaskItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.9).toInt(),
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

        holder.binding.taskListTitle.text = model.title
        holder.binding.addTaskList.setOnClickListener {
            holder.binding.addTaskList.visibility = View.GONE
            holder.binding.addTaskListName.visibility = View.VISIBLE
        }

        holder.binding.closeListName.setOnClickListener {
            holder.binding.addTaskList.visibility = View.VISIBLE
            holder.binding.addTaskListName.visibility = View.GONE
        }

        holder.binding.doneListName.setOnClickListener {
            val listName = holder.binding.taskListNameEditText.text.toString()
            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.createTaskList(listName)
                }
            } else {
                showSnackBar(holder.binding.root)
            }
        }

        holder.binding.editItem.setOnClickListener {
            holder.binding.taskListEditNameEditText.setText(model.title)
            holder.binding.editTaskListName.visibility = View.VISIBLE
            holder.binding.linearLayoutTitleView.visibility = View.GONE
        }

        holder.binding.closeEditView.setOnClickListener {
            holder.binding.linearLayoutTitleView.visibility = View.VISIBLE
            holder.binding.editTaskListName.visibility = View.GONE
        }

        holder.binding.doneEditListName.setOnClickListener {
            val listName = holder.binding.taskListEditNameEditText.text.toString()
            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.updateTaskList(position, listName, model)
                }
            } else {
                showSnackBar(holder.binding.root)
            }
        }

        holder.binding.deleteItem.setOnClickListener {
            alertDialogForDeleteList(position, model.title)
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title?")
        builder.setIcon(R.drawable.alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun showSnackBar(view: View) {
        val snackBar = Snackbar.make(
            view,
            "Please enter a list name!",
            Snackbar.LENGTH_LONG
        )
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.darkBlue
            )
        )
        snackBar.show()
    }

    class ViewHolder(val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root)

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPX(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}