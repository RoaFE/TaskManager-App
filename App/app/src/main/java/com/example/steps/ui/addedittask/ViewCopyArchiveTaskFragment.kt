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
import com.example.steps.databinding.FragmentViewCopyArchiveTaskBinding
import com.example.steps.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ViewCopyArchiveTaskFragment : Fragment(R.layout.fragment_view_copy_archive_task){
    private val viewModel: ViewCopyArchiveTaskViewModel by viewModels()

    fun calculateScore(feasability : Int, priority : Int) : Float
    {
        return priority.toFloat() / feasability.toFloat();
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentViewCopyArchiveTaskBinding.bind(view)
        binding.apply {
            textTaskName.setText(viewModel.taskName)
            if (viewModel.taskDescription == "null") {
                textDescription.setText("")
                textFeasability.setText("")
                textFeasability.setText("")
                editTextScore.setText("")
                deleteTaskButton.isVisible = false
            } else {
                textDescription.setText((viewModel.taskDescription))
                textFeasability.setText(viewModel.taskFeasibility.toString())
                textPriority.setText(viewModel.taskPriority.toString())
                editTextScore.setText(
                    calculateScore(
                        viewModel.taskFeasibility,
                        viewModel.taskPriority
                    ).toString()
                )
                deleteTaskButton.isVisible = true
            }
            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.text = "Created: ${viewModel.task?.createdDateFormated}"

            deleteTaskButton.setOnClickListener {
                viewModel.onDeleteClick()
            }

            copyButton.setOnClickListener {
                viewModel.onCopyClick()
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.copyArchiveTaskEvent.collect { event ->
                when (event) {
                    is ViewCopyArchiveTaskViewModel.CopyArchiveTaskEvent.NavigateBackWithResult ->
                    {
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