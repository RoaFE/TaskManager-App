package com.example.steps.ui.tasks

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.steps.data.Task
import com.example.steps.databinding.ItemTaskBinding

class TasksAdapter(private val listener: OnItemClickListener) : ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
            }
        }

        fun bind(task: Task) {
            Log.d("Task","ViewHolderbind")
            binding.apply {
                taskName.text = task.name
                taskScore.text = task.taskScore.toString()
                taskScoreText.text = "Score:"
                taskFeasibility.text = task.taskDifficulty.toString()
                taskFeasibilityText.text = "Feasibility:"
                taskPriority.text = task.taskPriority.toString()
                taskPriorityText.text = "Priority:"
                taskDesc.text = task.taskDescription
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
    }


    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            Log.d("TaskAdapter", "are Items the same")
            return(oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem
    }

}