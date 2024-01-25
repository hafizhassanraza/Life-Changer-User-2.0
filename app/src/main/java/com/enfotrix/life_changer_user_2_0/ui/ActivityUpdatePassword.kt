package com.enfotrix.life_changer_user_2_0.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityUpdatePasswordBinding
import com.google.firebase.firestore.FirebaseFirestore

class ActivityUpdatePassword : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatePasswordBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialogUpdateTaken: Dialog
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityUpdatePassword
        utils = Utils(mContext)
        sharedPrefManager = SharedPrefManager(mContext)
        showNewPasswordDialog()


    }
    private fun showNewPasswordDialog() {
        dialogUpdateTaken = Dialog(this)
        dialogUpdateTaken.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogUpdateTaken.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogUpdateTaken.setContentView(R.layout.dialog_update_taken_pin)
        dialogUpdateTaken.setCancelable(true)
        val pin1 = dialogUpdateTaken.findViewById<EditText>(R.id.etPin1)
        val pin2 = dialogUpdateTaken.findViewById<EditText>(R.id.etPin2)
        val pin3 = dialogUpdateTaken.findViewById<EditText>(R.id.etPin3)
        val pin4 = dialogUpdateTaken.findViewById<EditText>(R.id.etPin4)
        val pin5 = dialogUpdateTaken.findViewById<EditText>(R.id.etPin5)
        val pin6 = dialogUpdateTaken.findViewById<EditText>(R.id.etPin6)
        val btnSetPin = dialogUpdateTaken.findViewById<Button>(R.id.btnSetpin)

        pin1.requestFocus()
        utils.moveFocus(listOf(pin1, pin2, pin3, pin4, pin5, pin6))

        val tvClearAll = dialogUpdateTaken.findViewById<TextView>(R.id.tvClearAll)
        tvClearAll.setOnClickListener {
            utils.clearAll(listOf(pin1, pin2, pin3, pin4, pin5, pin6))
            pin1.requestFocus()
        }
        btnSetPin.setOnClickListener {

            val completePin = "${pin1.text}${pin2.text}${pin3.text}${pin4.text}${pin5.text}${pin6.text}"
            if(completePin.contains("-"))
            {
                Toast.makeText(this, "Please Enter Valid Password", Toast.LENGTH_SHORT).show()
            }
            else{
                storeInFireStore(completePin)
            }
        }
        dialogUpdateTaken.show()
    }


    private fun storeInFireStore(completePin: String) {
        var user1 = sharedPrefManager.getUser()
        user1.pin = completePin
        db.collection("Investors").document(sharedPrefManager.getToken()).set(user1)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    sharedPrefManager.saveUser(user1)
                    dialogUpdateTaken.dismiss()
                    startActivity(Intent(this, MainActivity::class.java))

                   }
            }
    }





}
