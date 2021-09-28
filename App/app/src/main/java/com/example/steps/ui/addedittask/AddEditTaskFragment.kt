package com.example.steps.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.steps.R
import com.example.steps.databinding.FragmentAddEditTaskBinding
import com.example.steps.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)
        binding.apply {
            editTextTaskName.setText(viewModel.taskName)
            if (viewModel.taskDescription == "null") {
                editTextDescription.setText("")
                fabDeleteTask.isVisible = false
            } else {
                editTextDescription.setText((viewModel.taskDescription))
                fabDeleteTask.isVisible = true
            }
            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.text = "Created: ${viewModel.task?.createdDateFormated}"

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            editTextDescription.addTextChangedListener {
                viewModel.taskDescription = it.toString()
            }

            fabSavTask.setOnClickListener {
                viewModel.loadSharedPref(context)
                viewModel.onSaveClick()
            }

            fabDeleteTask.setOnClickListener {
                viewModel.onDeleteClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        binding.editTextDescription.clearFocus()

                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result
                                , "task" to event.task)

                        )

                        findNavController().popBackStack()
                    }
                }.exhaustive

            }
        }
    }
}