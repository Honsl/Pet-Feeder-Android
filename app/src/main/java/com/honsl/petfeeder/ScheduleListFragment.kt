package com.honsl.petfeeder

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.honsl.petfeeder.placeholder.PlaceholderContent
import java.time.LocalTime
import java.util.UUID

/**
 * A fragment representing a list of Items.
 */
class ScheduleListFragment : Fragment() {

    private var columnCount = 1
    private lateinit var scheduleAdapter: ScheduleRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_list, container, false)



        scheduleAdapter = ScheduleRecyclerViewAdapter(requireContext(), parentFragmentManager)
        val app = requireActivity().applicationContext as GlobalFeeder
        val feeder = app.feeder


        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = if (columnCount <= 1) {
            LinearLayoutManager(context)
        } else {
            GridLayoutManager(context, columnCount)
        }
        recyclerView.adapter = scheduleAdapter

        view.findViewById<ImageButton>(R.id.imageButtonAddSchedule).setOnClickListener {
            val newSchedule = Schedule(UUID.randomUUID(), "left", 1, "12:30")
            val app = requireActivity().applicationContext as GlobalFeeder
            val feeder = app.feeder
            feeder?.schedule?.add(newSchedule)
            app.feeder = feeder

            scheduleAdapter.notifyItemInserted(feeder?.schedule!!.size - 1)
        }

        return view
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            ScheduleListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}

