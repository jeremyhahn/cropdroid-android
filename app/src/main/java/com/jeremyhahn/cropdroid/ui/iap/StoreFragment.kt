//package com.jeremyhahn.cropdroid.ui.iap
//
//import android.content.Context
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.DefaultItemAnimator
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.android.billingclient.api.*
//import com.jeremyhahn.cropdroid.AppError
//import com.jeremyhahn.cropdroid.R
//import com.jeremyhahn.cropdroid.data.CropDroidAPI
//import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
//import com.jeremyhahn.cropdroid.utils.Preferences
//import okhttp3.Call
//import okhttp3.Callback
//import java.io.IOException
//
//class StoreFragment : Fragment(), PurchasesUpdatedListener {
//
//    lateinit private var billingClient: BillingClient
//    lateinit private var recyclerView: RecyclerView
//    lateinit private var cropDroidAPI: CropDroidAPI
//    lateinit private var ctx: Context
//
//    private val skuList = listOf("server_v0.1a", "doser_v0.5a", "reservoir_v0.6a", "room_v0.6a", "test")
//
//    companion object {
//        const val TAG = "StoreActivity"
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
//        super.onCreateView(inflater, container, savedInstanceState)
//
//        ctx = requireActivity().applicationContext
//
//        val fragmentView = inflater.inflate(R.layout.fragment_store, container, false)
//
//        val preferences = Preferences(ctx)
//        val currentController = preferences.currentController()
//        if (currentController == "") {
//            AppError(ctx).toast("Controller connection required to use store.")
//            return fragmentView
//        }
//
//        val repo =  EdgeDeviceRepository(ctx)
//        val controller = repo.get(currentController)!!
//        cropDroidAPI = CropDroidAPI(controller, preferences.getDefaultPreferences())
//        billingClient = BillingClient.newBuilder(ctx).setListener(this).enablePendingPurchases().build()
//
//        recyclerView =  fragmentView.findViewById(R.id.products) as RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(ctx, RecyclerView.VERTICAL, false)
//        recyclerView.adapter = ProductsAdapter(ctx, ArrayList<SkuDetails>()) {
//            val billingFlowParams = BillingFlowParams
//                .newBuilder()
//                .setSkuDetails(it)
//                .build()
//            val responseCode = billingClient.launchBillingFlow(requireActivity(), billingFlowParams).responseCode
//            println("initProductAdapter responseCode: ${responseCode}")
//            if(responseCode != BillingClient.BillingResponseCode.OK) {
//                val errmsg = "Failed to launch in-app purchase flow"
//                Log.e(TAG, errmsg)
//                AppError(ctx).toast(errmsg)
//            }
//        }
//        recyclerView.itemAnimator = DefaultItemAnimator()
//
//        billingClient.startConnection(object: BillingClientStateListener {
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//
//                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
//                    println("BILLING | startConnection | RESULT OK")
//                    loadProducts()
//                } else {
//                    println("BILLING | startConnection | RESULT: ${billingResult.responseCode}")
//                    AppError(ctx).toast("${billingResult.responseCode}: ${billingResult.debugMessage}")
//                }
//            }
//            override fun onBillingServiceDisconnected() {
//                println("BILLING | onBillingServiceDisconnected | DISCONNECTED")
//            }
//
//        })
//
//        return fragmentView
//    }
//
//    fun loadProducts() {
//        if(billingClient.isReady) {
//            val params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build()
//            billingClient.querySkuDetailsAsync(params) { response, skuDetailsList ->
//                if (response.responseCode ==  BillingClient.BillingResponseCode.OK) {
//                    println("querySkuDetailsAsync, responseCode: ${response.responseCode}, skuList: " + skuDetailsList.toString())
//                    initProductAdapter(skuDetailsList)
//                } else {
//                    Log.e("StoreActivity", "Can't querySkuDetailsAsync, responseCode: ${response.responseCode}, debugMessage: ${response.debugMessage}")
//                    AppError(ctx).toast("${response.responseCode}: ${response.debugMessage}")
//                }
//            }
//        }
//        else {
//            AppError(ctx).toast("onLoadProductsClicked() Billing client not ready!")
//        }
//    }
//
//    fun initProductAdapter(skuDetailsList: MutableList<SkuDetails>?) {
//        (recyclerView.adapter as ProductsAdapter).setData(skuDetailsList)
//    }
//
//    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
//        println("onPurchasesUpdated: ${billingResult.debugMessage}")
//        if(purchases != null && purchases.size > 0)
//        for(purchase in purchases) {
//            println("verifying purchased item: ${purchase}")
//        }
//        consume(purchases)
//    }
//
//    private fun consume(purchases: List<Purchase>?) {
//        Log.d("consume: ", purchases.toString())
//        val purchase = purchases?.first()
//        if(purchase != null) {
//            val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase!!.purchaseToken).build()
//            billingClient.consumeAsync(consumeParams, object : ConsumeResponseListener {
//                override fun onConsumeResponse(
//                    billingResult: BillingResult,
//                    purchaseToken: String
//                ) {
//                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseToken != null) {
//                        println("AllowMultiplePurchases success, responseCode: ${billingResult.responseCode}")
//
//                        //AppError(ctx).show("${billingResult.responseCode}: ${billingResult.debugMessage}")
//
//                        cropDroidAPI.verifyPurchase(purchase, object : Callback {
//                            override fun onFailure(call: Call, e: IOException) {
//                                Log.d(TAG, "onFailure response: " + e!!.message)
//                                return
//                            }
//
//                            override fun onResponse(call: Call, response: okhttp3.Response) {
//                                var responseBody = response.body().string()
//
//                                Log.d(TAG, "responseBody: " + responseBody)
//
//                                if (response.code() != 200) {
//                                    return
//                                }
//
//                                if (responseBody.toBoolean()) {
//                                    activity!!.runOnUiThread(Runnable {
//                                        Toast.makeText(
//                                            ctx,
//                                            "Purchase successful!",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                    })
//                                } else {
//                                    AppError(ctx)
//                                        .alert(
//                                            "Unable to verify purchase. Please contact support for further assistance",
//                                            null,
//                                            null
//                                        )
//                                }
//                            }
//                        })
//
//
//                    } else {
//                        println("Can't allowMultiplePurchases, responseCode: ${billingResult.responseCode}")
//                        AppError(ctx).toast("${billingResult.responseCode}: ${billingResult.debugMessage}")
//                    }
//                }
//            })
//        }
//    }
//}
