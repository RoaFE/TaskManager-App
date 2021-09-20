package com.example.steps.ui.history

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.ColorInt
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.example.steps.R
import com.example.steps.databinding.FragmentHistoryBinding
import com.example.steps.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.PointsGraphSeries
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.text.DateFormat
import java.text.SimpleDateFormat


@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel: HistoryViewModel by viewModels()


    private var stepGoalSeries = BarGraphSeries<DataPoint>()
    private var stepsDoneSeries = PointsGraphSeries<DataPoint>()

    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val binding = FragmentHistoryBinding.bind(view)

        var goalNameArray: Array<String>
        lateinit var goalSpinnerAdapter: ArrayAdapter<String>


        viewModel.goals.observe(viewLifecycleOwner) {
            viewModel.populateGoalSpinner()
        }

        viewModel.goalList.observe(viewLifecycleOwner, Observer {
            if (it != null && it.isNotEmpty()) {
                goalNameArray = viewModel.goalList.value!!.toTypedArray()
                goalSpinnerAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_list,
                    goalNameArray
                )
                goalSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerViewGoals.adapter = goalSpinnerAdapter
            }
        })

        binding.spinnerViewGoals.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    pos: Int,
                    id: Long
                ) {
                    viewModel.onChangeGoal(pos)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // TODO
                }
            }


        binding.apply {
            currentHistoryStepsDoneText.addTextChangedListener {
                viewModel.goalStepsDone = it.toString()
            }

            buttonSaveHistory.setOnClickListener {
                viewModel.onButtonSaveClick()
            }

            buttonGoBackDate.setOnClickListener {
                viewModel.ChangeDateWindow(-viewModel.dateRange)
            }

            buttonGoForwardDate.setOnClickListener {
                viewModel.ChangeDateWindow(viewModel.dateRange)
            }

            lineGraph.addSeries(stepGoalSeries)
            lineGraph.addSeries(stepsDoneSeries)
            stepGoalSeries.setOnDataPointTapListener { series, dataPoint ->
                viewModel.onDataPointTap(dataPoint)
            }
            stepsDoneSeries.setOnDataPointTapListener { series, dataPoint ->
                viewModel.onDataPointTap(dataPoint)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.historiesEvent.collect { event ->
                when (event) {

                    //Reset data, make series actual variables outside of this scope, look at the screen on the left you idiot

                    HistoryEvent.UpdateGraph -> {
                        val stepsGraph = view.findViewById(R.id.line_graph) as GraphView
                        stepsGraph.title =
                            ("${viewModel.startDateString} - ${viewModel.endDateString}");
                        stepGoalSeries.resetData(viewModel.goalHistory.toTypedArray())
                        stepGoalSeries.spacing = 40
                        //stepGoalSeries.dataWidth = 1.0

                        val typedValue = TypedValue()
                        val theme: Resources.Theme = requireContext().theme
                        theme.resolveAttribute(R.attr.colorPrimaryVariant, typedValue, true)
                        @ColorInt var color = typedValue.data


                        stepGoalSeries.color = (color)


                        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
                        color = typedValue.data
                        stepsDoneSeries.resetData(viewModel.stepsHistory.toTypedArray())
                        stepsDoneSeries.color = (color)
                        stepsDoneSeries.shape = PointsGraphSeries.Shape.RECTANGLE

                        val dateformat: DateFormat = SimpleDateFormat("d/M/yy")

                        stepsGraph.gridLabelRenderer.labelFormatter =
                            DateAsXAxisLabelFormatter(activity, dateformat)
                        stepsGraph.gridLabelRenderer.numHorizontalLabels = 7


                        stepsGraph.viewport.isYAxisBoundsManual = true
                        stepsGraph.viewport.setMinY(0.0)
                        stepsGraph.gridLabelRenderer.padding = 5
                        stepsGraph.viewport.setMinX(viewModel.startDate.time.toDouble())
                        stepsGraph.viewport.setMaxX(viewModel.endDate.time.toDouble())
                        stepsGraph.viewport.isXAxisBoundsManual = true
                        //stepsGraph.gridLabelRenderer.setHumanRounding(false)

                    }
                    HistoryEvent.UpdateCurrentHistoryInfo -> {
                        @Suppress("IMPLICIT_CAST_TO_ANY")
                        binding.apply {
                            currentHistoryDateText.text = viewModel.currentSelectedDate
                            //currentHistoryText.text = viewModel.goalName
                            currentHistoryStepGoalText.text = viewModel.goalStepGoal
                            currentHistoryStepsDoneText.setText(viewModel.goalStepsDone)

                            for (i in 0..goalSpinnerAdapter.count - 1) {
                                if (viewModel.goalName == goalSpinnerAdapter.getItem(i)) {
                                    spinnerViewGoals.setSelection(i)
                                    break
                                } else {
                                    spinnerViewGoals.setSelection(0)
                                }
                            }


                        }
                    }
                    is HistoryEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    HistoryEvent.UpdateHistoryInfoNewGoalSelected -> {
                        binding.apply {
                            currentHistoryStepGoalText.text = viewModel.goalStepGoal
                        }

                    }
                }.exhaustive
            }
        }
        setHasOptionsMenu(true)


        // 1. Instantiate an AlertDialog.Builder
        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }

        // 2. Chain together various setter methods to set the dialog characteristics
        builder?.setMessage("Are you sure you want to clear all history")
        builder?.setTitle("Clear history")
        builder?.setPositiveButton("Delete", DialogInterface.OnClickListener { _, _ ->
            viewModel.deleteHistory()
        })

        builder?.setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ ->

        })

        dialog = builder?.create()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                true
            }
            R.id.action_clear_history -> {
                dialog?.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}