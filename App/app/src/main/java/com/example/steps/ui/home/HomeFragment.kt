package com.example.steps.ui.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.steps.R
import com.example.steps.data.Task
import com.example.steps.data.SortOrder
import com.example.steps.databinding.FragmentHomeBinding
import com.example.steps.util.exhaustive
import com.example.steps.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), HomeAdapter.OnItemClickListener {

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val binding = FragmentHomeBinding.bind(view)

        val homeAdapter = HomeAdapter(this)
        val shortTermTasksAdapter = HomeAdapter (this)
        val longTermTasksAdapter = HomeAdapter (this)

        binding.apply {

            recyclerViewHome.isVisible = viewModel.curTab == 0
            recyclerViewShortTerm.isVisible = viewModel.curTab == 1
            recyclerViewLongTerm.isVisible = viewModel.curTab == 2

            recyclerViewHome.apply {
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            recyclerViewLongTerm.apply {
                adapter = longTermTasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            recyclerViewShortTerm.apply {
                adapter = shortTermTasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            floatingActionButtonTaskConfirm.setOnClickListener {
                viewModel.onTaskChecked()
            }

            tabTaskTerms.selectTab(tabTaskTerms.getTabAt(viewModel.curTab))

            tabTaskTerms.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        viewModel.updateTab(tab.position)
                        recyclerViewHome.isVisible = viewModel.curTab == 0
                        recyclerViewShortTerm.isVisible = viewModel.curTab == 1
                        recyclerViewLongTerm.isVisible = viewModel.curTab == 2
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // Handle tab unselect
                }
            })
        }




        viewModel.tasks.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
            viewModel.onTaskLoad()
        }

        viewModel.shortTermTasks.observe(viewLifecycleOwner) {
            shortTermTasksAdapter.submitList(it)
        }

        viewModel.longTermTasks.observe(viewLifecycleOwner) {
            longTermTasksAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect {event ->
                when(event)
                {
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
                    is HomeEvent.UpdateCurrentTaskInformation ->
                    {
                        binding.apply {
                            currentTaskLabelText.text = event.task.name
                            currentTaskDescription.text = event.task.taskDescription
                            currentTaskDateCreated.text = event.task.createdDateFormated
                            if(event.task.longTerm)
                            {
                                currentTaskGoalTerm.text = "Long Term Goal"
                            }
                            else
                            {
                                currentTaskGoalTerm.text = "Short Term Goal"
                            }
                            floatingActionButtonTaskConfirm.isVisible = true
                            if(event.task.completed) {
                                floatingActionButtonTaskConfirm.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_clear_24))
                            }
                            else
                            {
                                floatingActionButtonTaskConfirm.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_check_24))
                            }
                        }
                    }
                    is HomeEvent.TaskChecked ->
                    {
                        if(event.task.completed) {
                            Snackbar.make(requireView(), "Task Completed", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    viewModel.onTaskCompletedUndo(event.task)
                                }.show()
                        }
                        else
                        {
                            Snackbar.make(requireView(), "Task Reopened", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    viewModel.onTaskCompletedUndo(event.task)
                                }.show()
                        }
                    }
                    is HomeEvent.ClearCurrentTaskInformation ->
                    {
                        binding.apply {
                            currentTaskLabelText.text = "Create a task"
                            currentTaskDescription.text = "No current active tasks available, please go to the task tab and create a task"
                            currentTaskDateCreated.text = ""
                            floatingActionButtonTaskConfirm.isVisible = false
                        }
                    }
                    is HomeEvent.UpdateTabSelected ->
                    {
                        binding.apply {
                            tabTaskTerms.selectTab(tabTaskTerms.getTabAt(event.tab))
                            recyclerViewHome.isVisible = viewModel.curTab == 0
                            recyclerViewShortTerm.isVisible = viewModel.curTab == 1
                            recyclerViewLongTerm.isVisible = viewModel.curTab == 2
                        }
                    }
                }.exhaustive

            }
        }




        setHasOptionsMenu(true)
    }


    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_goals, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
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
            R.id.action_sort_by_score -> {
                viewModel.onSortOrderSelected(SortOrder.BY_SCORE)
                true
            }
            R.id.action_hide_completed -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedTask(item.isChecked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}