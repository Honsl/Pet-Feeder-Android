package com.honsl.petfeeder

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FoodLevelFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FoodLevelFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val labels = listOf<String>("Left","Right")

        val chart = view.findViewById<BarChart>(R.id.chart)


        val formatter = IndexAxisValueFormatter(labels)
        chart.xAxis.valueFormatter = formatter
        chart.xAxis.granularity = 1f
        chart.xAxis.setGranularityEnabled(true)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.textColor = Color.WHITE


        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.setDrawGridLines(true)
        chart.xAxis.setDrawGridLines(false)
        chart.axisRight.setDrawLabels(false)
        chart.xAxis.setDrawLabels(true)
        chart.axisLeft.textColor = Color.WHITE
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 100f
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.xAxis.setDrawAxisLine(false)
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisRight.setDrawAxisLine(false)
        
        val entries = listOf<BarEntry>(BarEntry(0f,10f))
        val entries2 = listOf<BarEntry>(BarEntry(1f,50f))
        val dataSet = BarDataSet(entries,"Left")
        val dataSet2 = BarDataSet(entries2,"Right")
        dataSet2.setColor(ColorTemplate.rgb("#8191DC"))
        dataSet.setColor(ColorTemplate.rgb("#EEEE56"))
        dataSet.setDrawValues(false)
        dataSet2.setDrawValues(false)
        val dataSets = listOf<BarDataSet>(dataSet,dataSet2)
        val barData = BarData(dataSets)
        chart.data = barData
        //chart.description.text = "My Chart"
        //chart.animateXY(1000, 1000)
        chart.animateY(1000, Easing.EasingOption.EaseOutBounce)
        chart.invalidate()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_food_level, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FoodLevelFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FoodLevelFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}

