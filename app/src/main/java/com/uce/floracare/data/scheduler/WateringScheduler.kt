package com.uce.floracare.data.scheduler

import com.uce.floracare.data.remote.dto.PlantEntity

interface WateringScheduler {

    fun schedule(
        plant: PlantEntity
    )

    fun cancel(
        plant: PlantEntity
    )
}