package com.transportation.kotline.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.transportation.kotline.databinding.ItemListDriversHistoryBinding
import com.transportation.kotline.model.DriversHistory
import com.transportation.kotline.driver.IOnItemDriverCallback
import java.util.*
import kotlin.collections.ArrayList

class DriversHistoryAdapter(var onItemDriverCallback: IOnItemDriverCallback) :
    RecyclerView.Adapter<DriversHistoryAdapter.ViewHolder>() {
    private val listHistory = ArrayList<DriversHistory>()

    fun setListHistory(listDriversHistories: List<DriversHistory>) {
        this.listHistory.clear()
        this.listHistory.addAll(listDriversHistories)
    }

    inner class ViewHolder(private val binding: ItemListDriversHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(driversHistory: DriversHistory) {
            binding.apply {
                tvTime.text = getDate(driversHistory.time)
                tvName.text = driversHistory.customerName
                tvDestination.text = driversHistory.customerDestination
            }
            itemView.setOnClickListener { onItemDriverCallback.onItemClicked(listHistory[bindingAdapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemListDriversHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listHistory[position])
    }

    override fun getItemCount(): Int = listHistory.size

    // function to convert time
    private fun getDate(time: Long): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = time * 1000
        return DateFormat.format("dd-MM-yyyy HH:mm", calendar).toString()
    }
}