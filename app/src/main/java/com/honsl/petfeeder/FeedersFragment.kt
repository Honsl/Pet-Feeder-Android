package com.honsl.petfeeder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.honsl.petfeeder.placeholder.PlaceholderContent
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class FeedersFragment : Fragment() {

    private var columnCount = 1
    private lateinit var feederAdapter: MyItemRecyclerViewAdapter
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
        val view = inflater.inflate(R.layout.fragment_feeders_list, container, false)
        val jsonManager = JsonManager(requireContext())
        val feeders: MutableList<Feeder> =jsonManager.getFeeders()
        feederAdapter = MyItemRecyclerViewAdapter(feeders){ clickedFeeder ->

            Toast.makeText(context, "Clicked: ${clickedFeeder.name}", Toast.LENGTH_SHORT).show()

            //set the selected feeder as a global variable
            val app = requireActivity().applicationContext as GlobalFeeder
            app.feeder = jsonManager.getFeederByName(clickedFeeder.name)


            val intent = Intent(context, FeederDetailsActivity::class.java)
            requireContext().startActivity(intent)
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.feedersList)
        recyclerView.layoutManager = if (columnCount <= 1) {
            LinearLayoutManager(context)
        } else {
            GridLayoutManager(context, columnCount)
        }
        recyclerView.adapter = feederAdapter
        view.findViewById<MaterialButton>(R.id.imageButton)
            .setOnClickListener { Log.d("BUTTONS", "User tapped the Supabutton" )

                parentFragmentManager.setFragmentResult("fragmentView", bundleOf("bundleKey" to "result"))

            }



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rootView = view.findViewById<View>(R.id.main)
        val jsonManager = JsonManager(requireContext())
        val feeders:List<Feeder> =jsonManager.getFeeders()
        for ( feeder in feeders){
            val wifi = WifiManager(requireContext(),"http://"+feeder.ipAddress)
            lifecycleScope.launch {
                try {
                    val info = wifi.apiService.getFeeder()
                    feeder.status="OK"
                    feeder.levelLeft = info.leftFeeder
                    feeder.levelRight = info.rightFeeder
                    jsonManager.updateFeeder(feeder)
                    feederAdapter.updateData(jsonManager.getFeeders())

                } catch (e: Exception) {
                    val message  = Snackbar.make(view, "Error Connecting to "+feeder.name+"'s feeder", Snackbar.LENGTH_LONG)
                    //message.setBackgroundTint(Color.parseColor(""))
                    message.show()
                    feeder.status="ERROR"
                    jsonManager.updateFeeder(feeder)
                    feederAdapter.updateData(jsonManager.getFeeders())
                }
            }
        }
    }
    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            FeedersFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}