package com.enfotrix.life_changer_user_2_0.Models

import com.google.firebase.Timestamp

class ModelAnnouncement @JvmOverloads constructor(
    var announcement: String = "",
    var status: String = "",
    val createdAt: Timestamp = Timestamp.now() // Creation timestamp
)