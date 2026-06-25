package com.uce.floracare.data.local.dto

import com.google.gson.annotations.SerializedName

data class PerenualResponse(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("common_name") val common_name: String? = null,
    @SerializedName("scientific_name") val scientific_name: List<String>? = null,
    @SerializedName("other_name") val other_name: List<String>? = null,
    @SerializedName("family") val family: String? = null,
    @SerializedName("origin") val origin: List<String>? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("cycle") val cycle: String? = null,
    @SerializedName("care_level") val care_level: String? = null,
    @SerializedName("genus") val genus: String? = null,
    @SerializedName("species_epithet") val species_epithet: String? = null,
    @SerializedName("cultivar") val cultivar: String? = null,
    @SerializedName("hybrid") val hybrid: String? = null,
    @SerializedName("subspecies") val subspecies: String? = null,
    @SerializedName("authority") val authority: String? = null,
    @SerializedName("growth_rate") val growth_rate: String? = null,
    @SerializedName("maintenance") val maintenance: String? = null,
    @SerializedName("watering") val watering: String? = null,
    @SerializedName("watering_general_benchmark") val watering_general_benchmark: WateringBenchmark? = null,
    @SerializedName("sunlight") val sunlight: List<String>? = null,
    @SerializedName("pruning_month") val pruning_month: List<String>? = null,
    @SerializedName("hardiness") val hardiness: PerenualHardiness? = null,
    @SerializedName("dimensions") val dimensions: List<PerenualDimension>? = null,
    @SerializedName("drought_tolerant") val drought_tolerant: Boolean? = null,
    @SerializedName("tropical") val tropical: Boolean? = null,
    @SerializedName("indoor") val indoor: Boolean? = null,
    @SerializedName("medicinal") val medicinal: Boolean? = null,
    @SerializedName("poisonous_to_humans") val poisonous_to_humans: Boolean? = null,
    @SerializedName("poisonous_to_pets") val poisonous_to_pets: Boolean? = null,
    @SerializedName("invasive") val invasive: Boolean? = null,
    @SerializedName("thorny") val thorny: Boolean? = null,
    @SerializedName("salt_tolerant") val salt_tolerant: Boolean? = null,
    @SerializedName("seeds") val seeds: Boolean? = null,
    @SerializedName("flowers") val flowers: Boolean? = null,
    @SerializedName("flowering_season") val flowering_season: String? = null,
    @SerializedName("cones") val cones: Boolean? = null,
    @SerializedName("fruits") val fruits: Boolean? = null,
    @SerializedName("edible_fruit") val edible_fruit: Boolean? = null,
    @SerializedName("edible_leaf") val edible_leaf: Boolean? = null,
    @SerializedName("leaf") val leaf: Boolean? = null,
    @SerializedName("cuisine") val cuisine: Boolean? = null,
    @SerializedName("harvest_season") val harvest_season: String? = null,
    @SerializedName("attracts") val attracts: List<String>? = null,
    @SerializedName("propagation") val propagation: List<String>? = null,
    @SerializedName("soil") val soil: List<String>? = null,
    @SerializedName("pest_susceptibility") val pest_susceptibility: List<String>? = null,
    @SerializedName("plant_anatomy") val plant_anatomy: List<PlantAnatomy>? = null,
    @SerializedName("default_image") val default_image: PerenualImage? = null,
    @SerializedName("care_guides") val care_guides: String? = null
)

data class PerenualImage(
    @SerializedName("original_url") val original_url: String? = null
)

data class PerenualHardiness(
    @SerializedName("min") val min: String? = null,
    @SerializedName("max") val max: String? = null
)

data class PerenualDimension(
    @SerializedName("type") val type: String? = null,
    @SerializedName("min_value") val min_value: Int? = null,
    @SerializedName("max_value") val max_value: Int? = null,
    @SerializedName("unit") val unit: String? = null
)

data class WateringBenchmark(
    @SerializedName("value") val value: String? = null,
    @SerializedName("unit") val unit: String? = null
)

data class PlantAnatomy(
    @SerializedName("part") val part: String? = null,
    @SerializedName("color") val color: List<String>? = null
)