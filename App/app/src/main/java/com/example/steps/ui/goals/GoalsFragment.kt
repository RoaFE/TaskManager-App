package com.example.steps.ui.goals

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
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
import com.example.steps.databinding.FragmentGoalsBinding
import com.example.steps.util.exhaustive
import com.example.steps.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class GoalsFragment : Fragment(R.layout.fragment_goals), GoalsAdapter.OnItemClickListener {

    private val viewModel: GoalsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val binding = FragmentGoalsBinding.bind(view)

        val goalsAdapter = GoalsAdapter(this)

        binding.apply {
            recyclerViewGoals.apply {
                adapter = goalsAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            fabAddGoal.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }


        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            val goal = bundle.get("goal") as Goal
            viewModel.onAddEditResult(result, goal)
        }

        viewModel.goals.observe(viewLifecycleOwner) {
            goalsAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.goalsEvent.collect { event ->
                when (event) {
                    is GoalsViewModel.GoalsEvent.ShowUndoDeleteGoalMessage -> {
                        Snackbar.make(requireView(), "Goal deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.goal)
                            }.show()
                    }
                    is GoalsViewModel.GoalsEvent.NavigateToAddTaskScreen -> {
                        val action =
                            GoalsFragmentDirections.actionNavigationGoalsToAddEditGoalFragment(
                                null,
                                "New Goal"
                            )
                        findNavController().navigate(action)
                    }
                    is GoalsViewModel.GoalsEvent.NavigateToEditGoalScreen -> {
                        val action =
                            GoalsFragmentDirections.actionNavigationGoalsToAddEditGoalFragment(
                                event.goal,
                                "Edit Goal"
                            )
                        findNavController().navigate(action)
                    }
                    is GoalsViewModel.GoalsEvent.ShowGoalSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is GoalsViewModel.GoalsEvent.ShowCannotSelectCurrentGoalMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }
}