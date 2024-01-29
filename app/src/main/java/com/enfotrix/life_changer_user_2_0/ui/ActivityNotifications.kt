package com.enfotrix.life_changer_user_2_0.ui

import User
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.life_changer_user_2_0.Adapters.AdapterNotifications
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.NotificationModel

import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityNotificationsBinding
import com.google.firebase.firestore.FirebaseFirestore

class ActivityNotifications : AppCompatActivity() {
    //private val notificationViewModel: NotificationViewModel by viewModels()
    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog
    private var notificationsList: MutableList<NotificationModel> = mutableListOf()
    private lateinit var adapter: AdapterNotifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityNotifications
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvNoti.layoutManager = LinearLayoutManager(mContext)

        adapter = AdapterNotifications(notificationsList)
        binding.rvNoti.adapter = adapter

        setData()
    }


    private fun setData() {
        updateData()
        binding.rvNoti.adapter = AdapterNotifications(sharedPrefManager.getNotificationList().sortedByDescending {it.createdAt}) }
    private fun updateData() {

        val notificationsFromSharedPref = sharedPrefManager.getNotificationList()
        notificationsFromSharedPref.forEach { it.read = true }
        sharedPrefManager.putNotificationList(notificationsFromSharedPref)
        saveNotificationsToFirestore(notificationsFromSharedPref)


    }


    private fun saveNotificationsToFirestore(notificationsList: List<NotificationModel>) {
        val userNotificationsCollection =
            sharedPrefManager.getUser()?.id?.let {
                FirebaseFirestore.getInstance().collection(constants.NOTIFICATION_COLLECTION).document(
           sharedPrefManager.getToken()
                )

            }
        if (userNotificationsCollection != null) {
            userNotificationsCollection.set(mapOf(constants.NOTIFICATION_COLLECTION to notificationsList))
                .addOnSuccessListener {
                    Toast.makeText(mContext, "Notifications updated in Firestore", Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener {
                    Toast.makeText(mContext, "Failed to update notifications in Firestore", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
