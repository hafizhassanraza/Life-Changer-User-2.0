package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Adapters.AdapterNotifications
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.NotificationModel
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.databinding.ActivityNotificationsBinding
import com.google.firebase.firestore.FirebaseFirestore

class ActivityNotifications : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private var notificationsList: MutableList<NotificationModel> = mutableListOf()
    private lateinit var adapter: AdapterNotifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityNotifications
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvNoti.layoutManager = LinearLayoutManager(mContext)

        adapter = AdapterNotifications(notificationsList)
        binding.rvNoti.adapter = adapter

        setData()
        saveNotificationsToFirestore()
    }

    private fun setData() {
        val sortedList = sharedPrefManager.getNotificationList().sortedByDescending { it.createdAt }
        binding.rvNoti.adapter = AdapterNotifications(sortedList)
    }

    private fun saveNotificationsToFirestore() {
        val userId = sharedPrefManager.getUser()?.id

        userId?.let {
            val db = FirebaseFirestore.getInstance()
            val notificationCollection = db.collection(constants.NOTIFICATION_COLLECTION)

            sharedPrefManager.getNotificationList().forEach { notification ->
                if (!notification.read) {
                    notificationCollection.document(notification.id)
                        .update("read", true)
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener {
                        }
                }
            }
        }
    }
}
