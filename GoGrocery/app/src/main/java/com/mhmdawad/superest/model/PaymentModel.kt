package com.mhmdawad.superest.model

data class PaymentModel(


    val amount: String ="0.0",
    val currency: String = "usd",
    val paymentMethodType: String = "card"
)