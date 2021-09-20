package com.example.steps.ui.addeditgoal

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
import com.example.steps.databinding.FragmentAddEditGoalBinding
import com.example.steps.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditGoalFragment : Fragment(R.layout.fragment_add_edit_goal) {

    private val viewModel: AddEditGoalViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditGoalBinding.bind(view)
        binding.apply {
            editTextGoalName.setText(viewModel.goalName)
            if (viewModel.goalStepGoal == "null") {
                editTextGoalSteps.setText("")
                fabDeleteGoal.isVisible = false
            } else {
                editTextGoalSteps.setText((viewModel.goalStepGoal))
                fabDeleteGoal.isVisible = true
            }
            textViewDateCreated.isVisible = viewModel.goal != null
            textViewDateCreated.text = "Created: ${viewModel.goal?.createdDateFormated}"

            editTextGoalName.addTextChangedListener {
                viewModel.goalName = it.toString()
            }

            editTextGoalSteps.addTextChangedListener {
                viewModel.goalStepGoal = it.toString()
            }

            fabSavGoal.setOnClickListener {
                viewModel.loadSharedPref(context)
                viewModel.onSaveClick()
            }

            fabDeleteGoal.setOnClickListener {
                viewModel.onDeleteClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditGoalEvent.collect { event ->
                when (event) {
                    is AddEditGoalViewModel.AddEditGoalEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditGoalViewModel.AddEditGoalEvent.NavigateBackWithResult -> {
                        binding.editTextGoalName.clearFocus()
                        binding.editTextGoalSteps.clearFocus()

                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result
                                , "goal" to event.goal)

                        )

                        findNavController().popBackStack()
                    }
                }.exhaustive

            }
        }
    }
}