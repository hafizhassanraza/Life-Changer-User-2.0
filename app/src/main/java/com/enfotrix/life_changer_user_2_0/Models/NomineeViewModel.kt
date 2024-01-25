package com.enfotrix.life_changer_user_2_0.Models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class NomineeViewModel (context: Application) : AndroidViewModel(context)  {


    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)


    suspend fun addNominee(nominee: ModelNominee): LiveData<Boolean> {
        return userRepo.registerNominee(nominee)
    }
    suspend fun updateNominee(nominee: ModelNominee): LiveData<Boolean> {
        return userRepo.updateNominee(nominee) // set(update) model
    }
    suspend fun getNominee(nominator: String): Task<DocumentSnapshot> {
        return userRepo.getNominee(nominator)
    }




}