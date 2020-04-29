package com.jeremyhahn.cropdroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.jeremyhahn.cropdroid.ui.billing.ProductsAdapter

class StoreActivity : AppCompatActivity(), PurchasesUpdatedListener {

    lateinit private var billingClient: BillingClient
    lateinit private var recyclerView: RecyclerView
    private var recyclerItems = ArrayList<String>()
    private val skuList = listOf("server_v0.1a", "doser_v0.5a", "reservoir_v0.6a", "room_v0.6a", "test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        recyclerView =  findViewById(R.id.products) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()

        billingClient = BillingClient
            .newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object: BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    println("BILLING | startConnection | RESULT OK")
                    loadProducts()
                } else {
                    println("BILLING | startConnection | RESULT: ${billingResult.responseCode}")
                    Error(applicationContext).show("${billingResult.responseCode}: ${billingResult.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                println("BILLING | onBillingServiceDisconnected | DISCONNECTED")
            }

        })
    }

    fun loadProducts() {
        if(billingClient.isReady) {
            val params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build()
            billingClient.querySkuDetailsAsync(params) { response, skuDetailsList ->
                if (response.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    println("querySkuDetailsAsync, responseCode: ${response.responseCode}, skuList: " + skuDetailsList.toString())
                    initProductAdapter(skuDetailsList)
                } else {
                    Log.e("StoreActivity", "Can't querySkuDetailsAsync, responseCode: ${response.responseCode}, debugMessage: ${response.debugMessage}")
                    Error(applicationContext).show("${response.responseCode}: ${response.debugMessage}")
                }
            }
        }
        else {
            Error(applicationContext).show("onLoadProductsClicked() Billing client not ready!")
        }
    }

    fun initProductAdapter(skuDetailsList: List<SkuDetails>) {
        val productsAdapter = ProductsAdapter(this, skuDetailsList) {
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient.launchBillingFlow(this, billingFlowParams)
        }
        recyclerView.adapter = productsAdapter
        recyclerView.adapter!!.notifyDataSetChanged()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        println("onPurchasesUpdated: ${billingResult.debugMessage}")
        if(purchases != null && purchases.size > 0)
        for(purchase in purchases) {
            println("purchased item: ${purchase}")
        }
        consume(purchases)
    }

    private fun consume(purchases: MutableList<Purchase>?) {
        Log.d("consume: ", purchases.toString())
        val purchase = purchases?.first()
        if (purchase != null) {
            val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase!!.purchaseToken).build()
            billingClient.consumeAsync(consumeParams, object : ConsumeResponseListener {
                override fun onConsumeResponse(billingResult: BillingResult, purchaseToken: String) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseToken != null) {
                        println("AllowMultiplePurchases success, responseCode: ${billingResult.responseCode}")
                        Error(applicationContext).show("${billingResult.responseCode}: ${billingResult.debugMessage}")
                    } else {
                        println("Can't allowMultiplePurchases, responseCode: ${billingResult.responseCode}")
                        Error(applicationContext).show("${billingResult.responseCode}: ${billingResult.debugMessage}")
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("SettingsActivity", "onActivityResult($requestCode,$resultCode,$data")
        // Pass on the activity result to the helper for handling
        /*
        if (!inappBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        } else {
            Log.i(FragmentActivity.TAG, "onActivityResult handled by IABUtil.")
        }*/
    }

}
