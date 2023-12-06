package com.enfotrix.lifechanger.Data

import com.enfotrix.lifechanger.SharedPrefManager
import User
import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentModel
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.TransactionModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage

class Repo(val context: Context) {






    private var constants= Constants()
    private val sharedPrefManager = SharedPrefManager(context)
    private val token = sharedPrefManager.getToken()



    ///////////////////////////   FIREBASE    //////////////////////////////////
    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    private var InvestorsCollection = db.collection(constants.INVESTOR_COLLECTION)
    private val NomineesCollection = db.collection(constants.NOMINEE_COLLECTION)
    private val AccountsCollection = db.collection(constants.ACCOUNTS_COLLECTION)
    private val InvestmentCollection = db.collection(constants.INVESTMENT_COLLECTION)
    private val TransactionsReqCollection = db.collection(constants.TRANSACTION_REQ_COLLECTION)
    private val ProfitTaxCollection = db.collection(constants.PROFIT_TAX_COLLECTION)
    private val WithdrawCollection = db.collection(constants.WITHDRAW_COLLECTION)
    private val NotificationCollection = db.collection(constants.NOTIFICATION_COLLECTION)


    ///////////////////////////////////////////////////////////////////////////////



    /*class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            FirebaseApp.initializeApp(this)
        }
    }*/

    /*suspend fun isInvestorExist(CNIC: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()

        InvestorsCollection.whereEqualTo(INVESTOR_CNIC, CNIC).get()
            .addOnSuccessListener {documents ->
            *//*    val user: User
                 user = it.documents.firstOrNull()?.toObject(User::class.java)!!*//*


                //for (document in documents) user = document.toObject<User>()

                var user:User?=null
                for (document in documents) user= document.toObject(User::class.java)

                if(user?.status.equals(INVESTOR_STATUS_ACTIVE)){
                    result.value?.first ?: true
                    result.value?.second ?: INVESTOR_CNIC_EXIST
                }
                else if(user?.status.equals(INVESTOR_STATUS_BLOCKED)){
                    result.value?.first ?: false
                    result.value?.second ?: INVESTOR_CNIC_BLOCKED
                }
                else if(documents.size()==0){
                    result.value?.first ?: false
                    result.value?.second ?: INVESTOR_CNIC_NOT_EXIST
                }
            }
            .addOnFailureListener{


                result.value?.first ?: false
                result.value?.second ?: INVESTOR_CNIC_NOT_EXIST
            }

        return result
    }*/


    suspend fun isInvestorExist(CNIC: String): Task<QuerySnapshot> {
        return InvestorsCollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).get()
    }
    suspend fun loginUser(CNIC: String, PIN: String): Task<QuerySnapshot> {
        return InvestorsCollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).whereEqualTo(constants.INVESTOR_PIN,PIN).get()
    }

    suspend fun getNominee( nominator: String): Task<DocumentSnapshot> {
        return NomineesCollection.document(nominator).get()
       // return NomineesCollection.whereEqualTo("nominator", nominator).get()
    }
    suspend fun getUser( docID: String): Task<DocumentSnapshot> {
        return InvestorsCollection.document(docID).get()
       // return NomineesCollection.whereEqualTo("nominator", nominator).get()
    }

    suspend fun registerUser(user: User): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        user.status = constants.INVESTOR_STATUS_INCOMPLETE

        InvestorsCollection.add(user)
            .addOnSuccessListener { documentReference ->
                val documentId = documentReference.id

                // Update the user in Firestore with the document ID
                val userWithId = user.copy(id = documentId)
                InvestorsCollection.document(documentId)
                    .set(userWithId)
                    .addOnSuccessListener {
                        result.value = true
                    }
                    .addOnFailureListener {
                        result.value = false
                    }
            }
            .addOnFailureListener {
                result.value = false
            }

        return result
    }






    suspend fun updateDeviceToken(documentId: String, newDeviceToken: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        // Update the user's deviceToken in Firestore
        InvestorsCollection.document(documentId)
            .update("userdevicetoken", newDeviceToken)
            .addOnSuccessListener {
                result.value = true
            }
            .addOnFailureListener {
                result.value = false
            }

        return result
    }






    suspend fun updateUser(user: User): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        InvestorsCollection.document(user.id).set(user).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    suspend fun setUser(user: User): Task<Void> {
        return InvestorsCollection.document(sharedPrefManager.getToken()).set(user)
    }
    suspend fun registerBankAccount(bankAccount: ModelBankAccount): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        bankAccount.account_holder=sharedPrefManager.getToken()
        var documentRef= AccountsCollection.document()
        bankAccount.docID=documentRef.id

        documentRef.set(bankAccount).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    suspend fun userAccounts(token: String ): Task<QuerySnapshot> {
        return AccountsCollection.whereEqualTo(constants.ACCOUNT_HOLDER, token).get()
    }
    suspend fun adminAccounts(): Task<QuerySnapshot> {
        return AccountsCollection.whereEqualTo(constants.ACCOUNT_HOLDER, constants.ADMIN).get()
    }
    suspend fun getAccounts(): Task<QuerySnapshot> {
        return AccountsCollection.get()
    }
    suspend fun registerNominee(nominee: ModelNominee): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()


        //nominee.docID= NomineesCollection.document().id
        nominee.docID= nominee.nominator
        NomineesCollection.document(nominee.docID).set(nominee).addOnSuccessListener { documents ->
            sharedPrefManager.saveNominee(nominee)// id overwrite
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    suspend fun updateNominee(nominee: ModelNominee): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        NomineesCollection.document(nominee.docID).set(nominee).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }


    suspend fun uploadPhoto(imageUri: Uri, type:String): UploadTask {
        return storageRef.child(    type+"/"+sharedPrefManager.getToken()).putFile(imageUri)
    }

    suspend fun uploadPhotoRefrence(imageUri:Uri,type:String): StorageReference {

        return storageRef.child(    type+"/"+sharedPrefManager.getToken())
    }


    suspend fun uploadTransactionReceipt(imageUri: Uri, type:String, transactionId:String): UploadTask {
        return storageRef.child(    type+"/"+transactionId).putFile(imageUri)
    }





    suspend fun getProfitTax(token: String  ): Task<QuerySnapshot> {
        return ProfitTaxCollection.whereEqualTo(constants.INVESTOR_ID, token).get()
    }


    suspend fun getTransactionReq(token: String , type:String ): Task<QuerySnapshot> {
        return TransactionsReqCollection.whereEqualTo(constants.INVESTOR_ID, token).whereEqualTo(constants.TRANSACTION_TYPE,type).get()
    }

    suspend fun getTransactionReqByDates(token: String , type:String, startDate:String, endDate:String ): Task<QuerySnapshot> {
        return TransactionsReqCollection.whereEqualTo(constants.INVESTOR_ID, token)
            .whereEqualTo(constants.TRANSACTION_TYPE,type)
            .whereGreaterThanOrEqualTo("transactionAt", startDate)
            .whereLessThanOrEqualTo("transactionAt", endDate)
            .get()
    }





    suspend fun addTransactionReq(transactionModel: TransactionModel): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        TransactionsReqCollection.add(transactionModel)
            .addOnSuccessListener { documents ->
                result.value = true
            }.addOnFailureListener {
                result.value = false
            }
        return result
    }

    suspend fun addTransactionReqWithImage(transactionModel: TransactionModel, imageUri: Uri, type:String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        TransactionsReqCollection.add(transactionModel)
            .addOnSuccessListener { documents ->

                var id=documents.id
                transactionModel.id=id
                storageRef.child(type).child(id).putFile(imageUri).addOnSuccessListener {
                    result.value =true

                    TransactionsReqCollection.document(id).set(transactionModel)

                }
                    .addOnFailureListener {e->
                        Toast.makeText(context, ""+e.message, Toast.LENGTH_SHORT).show()
                    }
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    suspend fun addInvestment(investment: InvestmentModel): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        InvestmentCollection.document(investment.investorID).set(investment)
            .addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }



//    suspend fun uploadProfilePic(imageUri: Uri): LiveData<Uri> {
//        val uri = MutableLiveData<Uri>()
//        storageRef.child(sharedPrefManager.getDocID()).putFile(imageUri).addOnSuccessListener {
//            uri.value = it.uploadSessionUri
//        }.addOnFailureListener {
//            Toast.makeText(context, "Failed to upload profile pic", Toast.LENGTH_SHORT).show()
//        }
//
//        return uri
//    }

   /* suspend fun isInvestorExist(CNIC: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()

        InvestorsCollection.whereEqualTo(INVESTOR_CNIC, CNIC).get()
            .addOnSuccessListener {

                var user:User?=null
                for (document in it) user = document.toObject<User>()
                if(user?.status.equals(INVESTOR_STATUS_ACTIVE)) result.postValue(Pair(true, INVESTOR_CNIC_EXIST))
                else if(user?.status.equals(INVESTOR_STATUS_BLOCKED))result.postValue(Pair(false, INVESTOR_CNIC_BLOCKED))
                else if(it==null)result.postValue(Pair(false, INVESTOR_CNIC_NOT_EXIST))


            }
            .addOnFailureListener{
                result.postValue(Pair(false, INVESTOR_CNIC_NOT_EXIST))
            }
        return result
    }*/





/*

    suspend fun loginUser(CNIC: String, PIN: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()
        InvestorsCollection.whereEqualTo(INVESTOR_CNIC, CNIC).whereEqualTo(INVESTOR_PIN,PIN).get()
            .addOnSuccessListener { documents ->

                var user:User?= null
                for (document in documents) user = document.toObject<User>()
                result.value?.first ?: true
                result.value?.second ?: INVESTOR_LOGIN_MESSAGE
                if (user != null) sharedPrefManager.saveUser(user)


            }
            .addOnFailureListener{
                result.value?.first ?: false
                result.value?.second ?: INVESTOR_LOGIN_FAILURE_MESSAGE
            }
        return result
    }
*/










    /*private lateinit var constants: Constants()
    private val sharedPrefManager = SharedPrefManager(context)
    private val token = sharedPrefManager.getToken()



    ///////////////////////////   FIREBASE    //////////////////////////////////
    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    private var InvestorsCollection = db.collection(constants.INVESTOR_COLLECTION)
    private val NomineesCollection = db.collection(constants.NOMINEE_COLLECTION)
    private val InvestmentCollection = db.collection(constants.INVESTMENT_COLLECTION)
    private val ProfitCollection = db.collection(constants.PROFIT_COLLECTION)
    private val WithdrawCollection = db.collection(constants.WITHDRAW_COLLECTION)
    private val NotificationCollection = db.collection(constants.NOTIFICATION_COLLECTION)


    ///////////////////////////////////////////////////////////////////////////////

    init {
        constants = Constants()
    }

    fun initializeConstants() {
        constants = Constants() // Initialize the constants property
    }


    suspend fun isInvestorExist(CNIC: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()

        InvestorsCollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).get()
            .addOnSuccessListener {
                val user: User? = it.documents.firstOrNull()?.toObject(User::class.java)
                if(user?.status.equals(constants.INVESTOR_STATUS_ACTIVE)){
                    result.value?.first ?: true
                    result.value?.second ?: constants.INVESTOR_CNIC_EXIST
                }
                else if(user?.status.equals(constants.INVESTOR_STATUS_BLOCKED)){
                    result.value?.first ?: false
                    result.value?.second ?: constants.INVESTOR_CNIC_BLOCKED
                }
                else if(it==null){
                    result.value?.first ?: false
                    result.value?.second ?: constants.INVESTOR_CNIC_NOT_EXIST
                }

            }
            .addOnFailureListener{
                result.value?.first ?: false
                result.value?.second ?: constants.INVESTOR_CNIC_NOT_EXIST
            }

        return result
    }
    suspend fun registerUser(user: User): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        user.status=constants.INVESTOR_STATUS_ACTIVE
        InvestorsCollection.add(user).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }

    suspend fun loginUser(CNIC: String, PIN: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()
        InvestorsCollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).whereEqualTo(constants.INVESTOR_PIN,PIN).get()
            .addOnSuccessListener {
                val user: User? = it.documents.firstOrNull()?.toObject(User::class.java)
                if (user != null) {
                    result.value?.first ?: true
                    result.value?.second ?: constants.INVESTOR_LOGIN_MESSAGE
                    sharedPrefManager.saveUser(user)
                }
            }
            .addOnFailureListener{
                result.value?.first ?: false
                result.value?.second ?: constants.INVESTOR_LOGIN_FAILURE_MESSAGE
            }
        return result
    }*/




}