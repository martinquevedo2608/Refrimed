package com.example.refrimed.data

import java.util.Date

data class Values(
    val timestamp: Date = Date(),
    val temp1: Double = 0.0,
    val temp2: Double = 0.0,
    val temp3: Double = 0.0,
    val temp4: Double = 0.0,
    val current: Double = 0.0,
    val relay1: Boolean = false,
    val relay2: Boolean = false,
    val alarm1: Boolean = false,
    val alarm2: Boolean = false,
    val alarm3: Boolean = false
    )
