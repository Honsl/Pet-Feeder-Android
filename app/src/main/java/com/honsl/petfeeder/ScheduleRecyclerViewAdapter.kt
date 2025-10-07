package com.honsl.petfeeder

import android.Manifest
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.honsl.petfeeder.databinding.FragmentScheduleItemBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.text.Editable
import android.text.TextWatcher
import android.util.Log

class ScheduleRecyclerViewAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ViewHolder>() {

    // This list will be populated externally

    private val app = context.applicationContext as GlobalFeeder
    private val feeder = app.feeder ?: throw IllegalStateException("Feeder not initialized")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentScheduleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = feeder.schedule[position]
        holder.setTime.text = feeder.schedule[position].time
        holder.portion.setText(item.amount.toString())
        when(item.side){
            "left"->{
                holder.radioPortion.check(R.id.btn_left)
            }
            "random"->{
                holder.radioPortion.check(R.id.btn_random)
            }
            "right"->{
                holder.radioPortion.check(R.id.btn_right)
            }
        }
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(30)
            .setTitleText("Select Time")
            .build()

        holder.portion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val value = s?.toString()?.toIntOrNull() ?: 0
                feeder.schedule[holder.adapterPosition].amount = value
                app.feeder = feeder
            }

        })
        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

            val formattedTime = SimpleDateFormat("h:mm a", Locale.US).format(calendar.time)
            holder.setTime.text = formattedTime
            feeder.schedule[holder.adapterPosition].time = formattedTime
            app.feeder = feeder
        }

        holder.foodTimeButton.setOnClickListener {
            picker.show(fragmentManager, "timePicker")
        }
        holder.radioPortion.addOnButtonCheckedListener { group, checkedID,isChecked->
            var side:String = "none"
            if(isChecked){
                when(checkedID){
                    R.id.btn_left->{
                        side = "left"
                    }
                    R.id.btn_random->{
                        side = "random"
                    }
                    R.id.btn_right->{
                        side = "right"
                    }
                }
                feeder.schedule[holder.adapterPosition].side = side
                app.feeder = feeder
            }

        }
        holder.deleteScheduleButton.setOnClickListener {
            val positionToRemove = holder.adapterPosition
            if (positionToRemove != RecyclerView.NO_POSITION) {
                feeder.schedule.removeAt(positionToRemove)
                notifyItemRemoved(positionToRemove)
                app.feeder = feeder
            }
        }
    }

    override fun getItemCount(): Int = feeder.schedule.size

    inner class ViewHolder(binding: FragmentScheduleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val foodTimeButton: ImageButton = binding.imageButtonTime
        val deleteScheduleButton: ImageButton = binding.imageButtonDelete
        val setTime: TextView = binding.textViewSetTime
        val portion: EditText = binding.portionAmount
        val radioPortion: MaterialButtonToggleGroup = binding.buttonToggleGroupPortion


    }
}
