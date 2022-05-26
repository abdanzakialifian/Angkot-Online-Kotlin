package com.transportation.kotline.driver

import com.transportation.kotline.model.DriversHistory

interface IOnItemDriverCallback {
    fun onItemClicked(driversHistory: DriversHistory)
}