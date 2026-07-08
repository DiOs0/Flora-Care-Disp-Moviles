package com.uce.floracare.domain.model

import java.io.Serializable


data class TaskEntity(

    var firestoreId:String="",

    val plantFirestoreId:String="",

    val plantName:String="",

    val title:String="",

    val description:String="",

    val createdAt:Long=
        System.currentTimeMillis(),

    val completed:Boolean=false

):Serializable