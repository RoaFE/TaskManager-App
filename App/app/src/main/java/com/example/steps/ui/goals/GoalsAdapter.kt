package com.example.steps.ui.goals

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.steps.data.Goal
import com.example.steps.databinding.ItemGoalBinding

class GoalsAdapter(private val listener: OnItemClickListener) : ListAdapter<Goal, GoalsAdapter.GoalsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalsViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class GoalsViewHolder(private val binding: ItemGoalBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val goal = getItem(position)
                        listener.onItemClick(goal)
                    }
                }
            }
        }

        fun bind(goal: Goal) {
            Log.d("GoalsAdapter","ViewHolderbind")
            binding.apply {
                goalName.text = goal.name
                goalTarget.text = goal.stepGoal.toString()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(goal: Goal)
    }


    class DiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            Log.d("GoalAdapter", "are Items the same")
            return(oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean =
            oldItem == newItem
    }

}