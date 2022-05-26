package com.transportation.kotline.customer

import com.transportation.kotline.model.CustomersHistory

interface IOnItemCustomerCallback {
    fun onItemClicked(customersHistory: CustomersHistory)
}