package com.transportation.kotline.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.transportation.kotline.customer.CustomersHistory
import com.transportation.kotline.customer.IOnItemCustomerCallback
import com.transportation.kotline.databinding.ItemListCustomersHistoryBinding
import java.util.*
import kotlin.collections.ArrayList

class CustomersHistoryAdapter(var onItemCustomerCallback: IOnItemCustomerCallback) :
    RecyclerView.Adapter<CustomersHistoryAdapter.ViewHolder>() {

    private val listHistory = ArrayList<CustomersHistory>()

    fun setListHistory(listCustomersHistories: List<CustomersHistory>) {
        this.listHistory.clear()
        this.listHistory.addAll(listCustomersHistories)
    }

    inner class ViewHolder(private val binding: ItemListCustomersHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(customersHistory: CustomersHistory) {
            binding.apply {
                tvTime.text = getDate(customersHistory.time)
                tvName.text = customersHistory.driverName
                tvDestination.text = customersHistory.destination
            }
            itemView.setOnClickListener { onItemCustomerCallback.onItemClicked(listHistory[bindingAdapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemListCustomersHistoryBinding.inflate(
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