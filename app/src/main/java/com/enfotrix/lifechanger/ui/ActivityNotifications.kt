package com.enfotrix.lifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.lifechanger.Adapters.AdapterNotifications
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.NotificationModel

import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityNotificationsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

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
        supportActionBar?.title = "Investment Request"

        mContext = this@ActivityNotifications
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvNoti.layoutManager = LinearLayoutManager(mContext)
        notificationsList.sortBy { it.createdAt}
        adapter = AdapterNotifications(notificationsList)
        binding.rvNoti.adapter = adapter

        setTitle("")
        getNotificationsList() // Initialize the list when the activity is created
    }


    private fun getNotificationsList() {
        val notificationCollection = FirebaseFirestore.getInstance().collection(constants.NOTIFICATION_COLLECTION)

        // Fetch the documents
        notificationCollection.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Create a batched write to update the "read" field for all documents
                val batch = FirebaseFirestore.getInstance().batch()

                for (document in task.result!!) {
                    val notification = document.toObject(NotificationModel::class.java)

                    // Update the "read" field to true
                    batch.update(notificationCollection.document(document.id), "read", true)
                }

                // Commit the batched write
                batch.commit().addOnCompleteListener { commitTask ->
                    if (commitTask.isSuccessful) {
                        // Handle the success
                        Toast.makeText(mContext, "All notifications marked as read", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle the failure
                        Toast.makeText(mContext, "Error updating notifications", Toast.LENGTH_SHORT).show()
                    }
                }

                // Clear the previous list to avoid duplicates
                notificationsList.clear()

                // Iterate through the documents and add them to the list
                for (document in task.result!!) {
                    val notification = document.toObject(NotificationModel::class.java)
                    notification.id = document.id
                    notificationsList.add(notification)
                }

                // Notify the adapter that the dataset has changed
                adapter.notifyDataSetChanged()
            } else {
                // Handle the error
                Toast.makeText(mContext, "Error fetching notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
