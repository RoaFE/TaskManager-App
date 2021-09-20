package com.example.steps.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.steps.R
import com.example.steps.data.Goal
import com.example.steps.data.SortOrder
import com.example.steps.databinding.FragmentHomeBinding
import com.example.steps.ui.goals.GoalsAdapter
import com.example.steps.ui.goals.GoalsFragmentDirections
import com.example.steps.ui.goals.GoalsViewModel
import com.example.steps.util.exhaustive
import com.example.steps.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.flow.collect
import kotlin.math.ceil
import kotlin.math.floor

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), HomeAdapter.OnItemClickListener {

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val binding = FragmentHomeBinding.bind(view)

        val homeAdapter = HomeAdapter(this)

        binding.apply {
            recyclerViewHome.apply {
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            editTextGoalSteps.addTextChangedListener {
                viewModel.stepsIncrement = it.toString()
            }

            stepInputScrollView.isEnabled = false

            addStepsButton.setOnClickListener {
                viewModel.onAddSteps()
            }
        }




        viewModel.goals.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
            viewModel.onGoalLoad()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect {event ->
                when(event)
                {
                    is HomeEvent.UpdateCurrentGoalInfo ->
                    {
                        binding.apply {
                            currentGoalLabelText.text = event.history.name
                            currentGoalText.text = "${event.history.stepGoal} steps"
                            currentGoalProgressText.text = event.history.stepsDone.toString()
                            val percentage : Int = floor(event.history.stepsDone.toFloat() / event.history.stepGoal.toFloat() * 100).toInt()
                            currentGoalProgressPercentage.text = "${(percentage)}%"
                            if(percentage > 100) {
                                currentGoalProgressBar.progress = 100
                            }
                            else
                            {
                                currentGoalProgressBar.progress = percentage
                            }

                            binding.editTextGoalSteps.clearFocus()
                        }
                    }
                    is HomeEvent.ShowInvalidInputMessage ->
                    {
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_SHORT).show()
                    }
                    is HomeEvent.UndoAddSteps ->
                    {

                        Snackbar.make(requireView(),"Steps Added",Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoSteps(event.steps)
                            }.show()
                    }
                }.exhaustive

            }
        }

        setHasOptionsMenu(true)
    }


    override fun onItemClick(goal: Goal) {
        viewModel.onGoalSelected(goal)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_goals, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_sort_by_goal -> {
                viewModel.onSortOrderSelected(SortOrder.BY_GOAL)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}