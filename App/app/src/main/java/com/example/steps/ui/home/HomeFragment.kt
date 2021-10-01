package com.example.steps.ui.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.widget.addTextChangedListener
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.item_task.*
import kotlinx.coroutines.flow.collect

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
        }




        viewModel.tasks.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
            viewModel.onTaskLoad()
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
            else -> super.onOptionsItemSelected(item)
        }

    }

}