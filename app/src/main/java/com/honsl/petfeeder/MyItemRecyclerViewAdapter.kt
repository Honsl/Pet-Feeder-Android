package com.honsl.petfeeder

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.TextView
import com.google.android.material.progressindicator.CircularProgressIndicator

import com.honsl.petfeeder.placeholder.PlaceholderContent.PlaceholderItem
import com.honsl.petfeeder.databinding.FragmentFeedersBinding
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(
    private val values: MutableList<Feeder>,
    private val onItemClick: (Feeder) -> Unit
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentFeedersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val viewHolder = ViewHolder(binding)

        binding.root.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(values[position])
            }
        }


        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.name

        if(item.status=="OK"){
            holder.statusGood.isVisible = true
            holder.statusBad.isVisible = false
        }else{
            holder.statusGood.isVisible = false
            holder.statusBad.isVisible = true
        }

        if(item.schedule.isNotEmpty()){
            val closest = findClosestFutureSchedule(item)
            holder.feedingTime.text = "Next Feeding: "+ closest!!.time
        }else{
            holder.feedingTime.text = "Next Feeding: NONE Scheduled"
        }

        holder.leftPercentText.text = item.levelLeft.toInt().toString()+"%"
        holder.leftMeter.progress = item.levelLeft.toInt()

        when(item.levelLeft.toInt()){
            in 0..30 -> {
                holder.leftMeter.setIndicatorColor(Color.RED)
                holder.leftMeter.trackColor = Color.parseColor("#80E4080A")
            }
            in 31..60 -> {
                holder.leftMeter.setIndicatorColor(Color.YELLOW)
                holder.leftMeter.trackColor = Color.parseColor("#80FFDE59")
            }
            else -> {
                holder.leftMeter.setIndicatorColor(Color.GREEN)
                holder.leftMeter.trackColor = "#807DDA58".toColorInt()
            }

        }
        val bounceAnimator = ValueAnimator.ofFloat(0f, item.levelLeft.toFloat())
        bounceAnimator.duration = 1000
        bounceAnimator.interpolator = BounceInterpolator()
        bounceAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            holder.leftMeter.setProgress(animatedValue.toInt())
        }
        bounceAnimator.start()

        holder.rightPercentText.text = item.levelRight.toInt().toString()+"%"
        holder.rightMeter.progress = item.levelRight.toInt()
        when(item.levelRight.toInt()){
            in 0..30 -> {
                holder.rightMeter.setIndicatorColor(Color.RED)
                holder.rightMeter.trackColor = Color.parseColor("#80E4080A")
            }
            in 31..60 -> {
                holder.rightMeter.setIndicatorColor(Color.YELLOW)
                holder.rightMeter.trackColor = Color.parseColor("#80FFDE59")
            }
            else -> {
                holder.rightMeter.setIndicatorColor(Color.GREEN)
                holder.rightMeter.trackColor = "#807DDA58".toColorInt()
            }

        }
        val bounceAnimatorRight = ValueAnimator.ofFloat(0f, item.levelRight.toFloat())
        bounceAnimatorRight.duration = 1000
        bounceAnimatorRight.interpolator = BounceInterpolator()
        bounceAnimatorRight.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            holder.rightMeter.setProgress(animatedValue.toInt())
        }
        bounceAnimatorRight.start()

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentFeedersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemName
        val feedingTime: TextView = binding.textViewNextFeeding
        val leftPercentText = binding.progressLeftText
        val leftMeter = binding.leftProgress
        val rightPercentText = binding.progressRightText
        val rightMeter = binding.rightProgress

        val statusGood = binding.imageViewGood
        val statusBad = binding.imageViewBad

        override fun toString(): String {
            return super.toString() + " '"
        }
    }
    fun updateData(newValues: List<Feeder>) {
        values.clear()
        values.addAll(newValues)
        notifyDataSetChanged()
    }
    fun findClosestFutureSchedule(item: Feeder): Schedule? {
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        val now = LocalDateTime.now()

        val futureSchedules = item.schedule.mapNotNull { schedule ->
            try {
                val time = LocalTime.parse(schedule.time, formatter)
                val todayTime = now.toLocalDate().atTime(time)
                val scheduledDateTime = if (todayTime.isAfter(now)) todayTime else todayTime.plusDays(1)
                schedule to scheduledDateTime
            } catch (e: Exception) {
                null
            }
        }

        return futureSchedules.minByOrNull { (_, dateTime) ->
            Duration.between(now, dateTime)
        }?.first
    }
}