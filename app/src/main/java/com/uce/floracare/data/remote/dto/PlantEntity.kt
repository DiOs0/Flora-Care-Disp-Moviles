package com.uce.floracare.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.uce.floracare.data.local.dto.PerenualResponse
import java.io.Serializable

/*
 * MODELO DEL JSON PARA FIREBASE
 */

data class PlantEntity(

    var firestoreId:String="",

    val id:Int=0,

    @get:PropertyName("nombre_comun")
    @field:PropertyName("nombre_comun")
    val nombreComun:String="",

    val nombreCientifico:String="",

    val imagen:String="",

    val tipo:String="",

    val descripcion:String="",

    val cicloVida:String="",

    val nivelCuidado:String="",

    val caracteristicas:Caracteristicas=
        Caracteristicas(),

    val riego:Riego=
        Riego(),

    val luzSolar:List<String> =
        emptyList(),

    val temperatura:Temperatura=
        Temperatura()

):Serializable


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

) : Serializable


data class Riego(

    val frecuencia: String = "",

    @get:PropertyName("cadaValor")
    @field:PropertyName("cadaValor")
    val cadaValor: String = ""

) : Serializable


data class Temperatura(

    val min: Int = 0,
    val max: Int = 0,
    val descripcion: String = ""

) : Serializable