package com.uce.floracare.api_ingreso.data

import com.google.firebase.firestore.PropertyName

data class PlantEntity(
    val id: Int = 0,
    @get:PropertyName("nombre_comun")
    @field:PropertyName("nombre_comun")
    val nombreComun: String = "",
    @get:PropertyName("nombre_cientifico")
    @field:PropertyName("nombre_cientifico")
    val nombreCientifico: String = "",
    val imagen: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    @get:PropertyName("ciclo_vida")
    @field:PropertyName("ciclo_vida")
    val cicloVida: String = "",
    @get:PropertyName("nivel_cuidado")
    @field:PropertyName("nivel_cuidado")
    val nivelCuidado: String = "",
    val caracteristicas: Caracteristicas = Caracteristicas(),
    val riego: Riego = Riego(),
    @get:PropertyName("luz_solar")
    @field:PropertyName("luz_solar")
    val luzSolar: List<String> = emptyList(),
    @get:PropertyName("Temperatura")
    @field:PropertyName("Temperatura")
    val temperatura: Temperatura = Temperatura()
)

data class Caracteristicas(
    val medicinal: Boolean = false,
    val indoor: Boolean = false,
    val tropical: Boolean = false,
    @get:PropertyName("resistente_sequia")
    @field:PropertyName("resistente_sequia")
    val resistenteSequia: Boolean = false,
    @get:PropertyName("toxica_humanos")
    @field:PropertyName("toxica_humanos")
    val toxicaHumanos: Boolean = false,
    @get:PropertyName("toxica_mascotas")
    @field:PropertyName("toxica_mascotas")
    val toxicaMascotas: Boolean = false
)

data class Riego(
    val frecuencia: String = "",
    @get:PropertyName("cadaValor")
    @field:PropertyName("cadaValor")
    val cadaValor: String = ""
)

data class Temperatura(
    val min: Int = 0,
    val max: Int = 0,
    val descripcion: String = ""
)

fun PerenualResponse.toPlantEntity(): PlantEntity {
    val cadaValor = this.watering_general_benchmark?.value
        ?.trim('"')
        .orEmpty()

    val tempDesc = when {
        this.hardiness?.min != null && this.hardiness?.max != null -> {
            val avg = ((this.hardiness.min.toIntOrNull() ?: 0) + (this.hardiness.max.toIntOrNull() ?: 0)) / 2
            when {
                avg <= 5 -> "Muy Frío"
                avg <= 7 -> "Frío"
                avg <= 9 -> "Templado"
                avg <= 11 -> "Cálido"
                else -> "Muy Cálido"
            }
        }
        else -> "Temperatura ambiente estándar"
    }

    return PlantEntity(
        id = this.id,
        nombreComun = this.common_name.orEmpty(),
        nombreCientifico = this.scientific_name?.firstOrNull().orEmpty(),
        imagen = this.default_image?.original_url.orEmpty(),
        tipo = this.type.orEmpty(),
        descripcion = this.description.orEmpty(),
        cicloVida = this.cycle.orEmpty(),
        nivelCuidado = this.care_level.orEmpty(),
        caracteristicas = Caracteristicas(
            medicinal = this.medicinal ?: false,
            indoor = this.indoor ?: false,
            tropical = this.tropical ?: false,
            resistenteSequia = this.drought_tolerant ?: false,
            toxicaHumanos = this.poisonous_to_humans ?: false,
            toxicaMascotas = this.poisonous_to_pets ?: false
        ),
        riego = Riego(
            frecuencia = this.watering.orEmpty(),
            cadaValor = cadaValor
        ),
        luzSolar = this.sunlight.orEmpty(),
        temperatura = Temperatura(
            min = this.hardiness?.min?.toIntOrNull() ?: 0,
            max = this.hardiness?.max?.toIntOrNull() ?: 0,
            descripcion = tempDesc
        )
    )
}
