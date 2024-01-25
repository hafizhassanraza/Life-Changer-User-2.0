package com.enfotrix.life_changer_user_2_0.Models

import com.google.firebase.Timestamp
import com.google.gson.Gson

data class ModelFA  @JvmOverloads constructor(
    var cnic: String = "default",
    var firstName: String = "",
    var lastName: String = "",
    var address: String = "",
    var phone: String = "",
    var status: String = "",
    var photo: String = "",
    var cnic_front: String = "",
    var cnic_back: String = "",
    var pin: String = "",
    var id: String = "",
    var designantion: String = "",
    var profit: String = "0",
    val createdAt: Timestamp = Timestamp.now() // Creation timestamp
){

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromString(modelFA: String): ModelFA? {
            val gson = Gson()
            return try {
                gson.fromJson(modelFA, ModelFA::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}