package com.enfotrix.lifechanger.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.enfotrix.lifechanger.R

class DialogAddAccount (val context: Context){

    private var dialog= Dialog(context)


    fun show() {

        dialog = Dialog (context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_account)
        dialog.setCancelable(false)

        //val etPin1 = dialog.findViewById<EditText>(R.id.etPin1)
        val btnAddAccount = dialog.findViewById<Button>(R.id.btnAddAccount)

        btnAddAccount.setOnClickListener {

        }

        dialog.show()
    }


}