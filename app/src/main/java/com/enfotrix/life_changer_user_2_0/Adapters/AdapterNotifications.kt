package com.enfotrix.life_changer_user_2_0.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.life_changer_user_2_0.Constants

import com.enfotrix.life_changer_user_2_0.Models.NotificationModel
import com.enfotrix.life_changer_user_2_0.databinding.ItemAccountsBinding


class AdapterNotifications( val data: List<NotificationModel>) : RecyclerView.Adapter<AdapterNotifications.ViewHolder>() {


    var constant= Constants()

//    interface OnItemClickListener {
//        fun onItemClick(modelBankAccount: ModelBankAccount)
//        fun onDeleteClick(modelBankAccount: ModelBankAccount)
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAccountsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { holder.bind(data[position]) }
    override fun getItemCount(): Int { return data.size }
    inner class ViewHolder(val itemBinding: ItemAccountsBinding) : RecyclerView.ViewHolder(itemBinding.root){

        fun bind(modelBankAccount: NotificationModel) {

            itemBinding.notificationTitle.text=modelBankAccount.notiTitle
            itemBinding.notificationData.text=modelBankAccount.notiData
            itemBinding.date.text=modelBankAccount.date
//            itemBinding.layItem.setOnClickListener{ listener.onItemClick(modelBankAccount)}
        }

    }



}
