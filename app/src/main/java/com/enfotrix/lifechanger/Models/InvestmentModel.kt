package com.enfotrix.lifechanger.Models




data class InvestmentModel @JvmOverloads constructor(
    var investorID: String = "",
    var investmentBalance: String = "0",
    var lastInvestment: String = "",
    var lastInvestmentReq: String = "",
    var lastWithdraw: String = "",
    var lastWithdrawReq: String = "",
    var lastProfit: String = "",
    var upcomingProfit: String = "",

    var lastInvestmentDate: String = "",
    var lastInvestmentReqDate: String = "",
    var lastWithdrawDate: String = "",
    var lastWithdrawReqDate: String = "",
    var lastProfitDate: String = "",
    var upcomingProfitDate: String = ""
)