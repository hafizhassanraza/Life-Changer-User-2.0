package com.enfotrix.lifechanger.Models

import com.google.firebase.Timestamp

data class ModelProfitTax @JvmOverloads constructor(
    var investorID: String = "",
    var type: String = "", //Profit, Tax
    var status: String = "", // pending ,approved , reject
    var amount: String = "",
    var receiverAccountID: String = "", // account id
    var previousBalance: String = "", //

    var senderAccountID: String = "",// account id
    var id: String = "",
    var newBalance: String = "", //
    var transactionAt: Timestamp? = null, // Creation timestamp
    val createdAt: Timestamp = Timestamp.now() // Creation timestamp

)