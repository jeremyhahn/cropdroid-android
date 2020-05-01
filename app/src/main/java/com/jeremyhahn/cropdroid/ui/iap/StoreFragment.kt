package com.jeremyhahn.cropdroid.ui.iap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.jeremyhahn.cropdroid.Error
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.utils.Preferences
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class StoreFragment : Fragment(), PurchasesUpdatedListener {

    lateinit private var billingClient: BillingClient
    lateinit private var recyclerView: RecyclerView
    lateinit private var cropDroidAPI: CropDroidAPI
    private val skuList = listOf("server_v0.1a", "doser_v0.5a", "reservoir_v0.6a", "room_v0.6a", "test")

    companion object {
        const val TAG = "StoreActivity"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val fragmentView = inflater.inflate(R.layout.activity_store, container, false)

        val preferences = Preferences(activity!!.applicationContext)
        val controller = MasterControllerRepository(activity!!.applicationContext).getController(preferences.currentControllerId())
        cropDroidAPI = CropDroidAPI(controller)

        recyclerView =  fragmentView.findViewById(R.id.products) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()

        billingClient = BillingClient
            .newBuilder(activity!!.applicationContext)
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
                    Error(activity!!.applicationContext)
                        .dialog("${billingResult.responseCode}: ${billingResult.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                println("BILLING | onBillingServiceDisconnected | DISCONNECTED")
            }

        })

        return fragmentView
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
                    Error(activity!!.applicationContext)
                        .dialog("${response.responseCode}: ${response.debugMessage}")
                }
            }
        }
        else {
            Error(activity!!.applicationContext).dialog("onLoadProductsClicked() Billing client not ready!")
        }
    }

    fun initProductAdapter(skuDetailsList: List<SkuDetails>) {
        val productsAdapter = ProductsAdapter(activity!!.applicationContext, skuDetailsList) {
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
        recyclerView.adapter = productsAdapter
        recyclerView.adapter!!.notifyDataSetChanged()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        println("onPurchasesUpdated: ${billingResult.debugMessage}")
        if(purchases != null && purchases.size > 0)
        for(purchase in purchases) {
            println("verifying purchased item: ${purchase}")
        }
        consume(purchases)
    }

    private fun consume(purchases: MutableList<Purchase>?) {
        Log.d("consume: ", purchases.toString())
        val purchase = purchases?.first()
        if(purchase != null) {
            val consumeParams =
                ConsumeParams.newBuilder().setPurchaseToken(purchase!!.purchaseToken).build()
            billingClient.consumeAsync(consumeParams, object : ConsumeResponseListener {
                override fun onConsumeResponse(
                    billingResult: BillingResult,
                    purchaseToken: String
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseToken != null) {
                        println("AllowMultiplePurchases success, responseCode: ${billingResult.responseCode}")

                        //Error(activity!!.applicationContext).show("${billingResult.responseCode}: ${billingResult.debugMessage}")

                        cropDroidAPI.verifyPurchase(purchase, object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d(TAG, "onFailure response: " + e!!.message)
                                return
                            }

                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                var responseBody = response.body().string()

                                Log.d(TAG, "responseBody: " + responseBody)

                                if (response.code() != 200) {
                                    return
                                }

                                if (responseBody.toBoolean()) {
                                    activity!!.runOnUiThread(Runnable {
                                        Toast.makeText(
                                            activity!!.applicationContext,
                                            "Purchase successful!",
                                            Toast.LENGTH_SHORT
                                        )
                                    })
                                } else {
                                    Error(activity!!.applicationContext)
                                        .alert(
                                            "Unable to verify purchase. Please contact support for further assistance",
                                            null,
                                            null
                                        )
                                }
                            }
                        })


                    } else {
                        println("Can't allowMultiplePurchases, responseCode: ${billingResult.responseCode}")
                        Error(activity!!.applicationContext)
                            .dialog("${billingResult.responseCode}: ${billingResult.debugMessage}")
                    }
                }
            })
        }
    }
}
