package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Customer
import com.jeremyhahn.cropdroid.ui.shoppingcart.parser.CustomerEphemeralKeyResponseParser
import com.jeremyhahn.cropdroid.ui.shoppingcart.parser.PaymentMethodsParser
import com.stripe.android.PaymentConfiguration
import com.stripe.android.customersheet.CustomerAdapter
import com.stripe.android.customersheet.CustomerEphemeralKey
import com.stripe.android.customersheet.CustomerEphemeralKeyProvider
import com.stripe.android.customersheet.CustomerSheet
import com.stripe.android.customersheet.CustomerSheetResult
import com.stripe.android.customersheet.CustomerSheetResultCallback
import com.stripe.android.customersheet.ExperimentalCustomerSheetApi
import com.stripe.android.customersheet.SetupIntentClientSecretProvider
import com.stripe.android.view.CardInputWidget
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


@OptIn(ExperimentalCustomerSheetApi::class)
class CustomerSheetFacade(
    val activity: Activity,
    val context: Context,
    val fragment: Fragment,
    val cropDroidAPI: CropDroidAPI):
        CustomerEphemeralKeyProvider,
        SetupIntentClientSecretProvider,
        CustomerSheetResultCallback {

    private var TAG = "CustomerSheetFacade"
    private var paymentMethods = ArrayList<String>()

    private lateinit var customerSheet: CustomerSheet
    private lateinit var customer: Customer
    private lateinit var clientSetupSecret: String
    private lateinit var customerEphemeralKey: String
    private lateinit var publishableKey: String

    var error: MutableLiveData<String> = MutableLiveData()

    fun init() {
        if(!::customer.isInitialized) {
            displayAlert("test", "test2", true)

            //getOrCreateCustomerWithEphemeralKey()
            //val stripe = Stripe(requireContext(), PaymentConfiguration.getInstance(requireActivity().applicationContext).publishableKey)
            return
        }
//        } else {
//            getPaymentMethods(::createSetupIntent)
//        }
    }

    private fun displayAlert(
        title: String,
        message: String,
        restartDemo: Boolean
    ) {
        activity.runOnUiThread {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
            if (restartDemo) {
                builder.setPositiveButton(
                    "Restart demo"
                ) { dialog: DialogInterface?, index: Int ->
                    val cardInputWidget: CardInputWidget = activity.findViewById(com.jeremyhahn.cropdroid.R.id.cardInputWidget)
                    cardInputWidget.clear()
                }
            } else {
                builder.setPositiveButton("Ok", null)
            }
            builder.create().show()
        }
    }

    private fun getOrCreateCustomerWithEphemeralKey() {
        cropDroidAPI.getCustomerWithEphemeralKey(0L, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                error.postValue(e.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    error.postValue(apiResponse.error)
                    return
                }
                if(apiResponse.payload != null) {
                    val response = apiResponse.payload as JSONObject
                    val customerWithEphemeralKeyResponse = CustomerEphemeralKeyResponseParser.parse(response)
                    customer = customerWithEphemeralKeyResponse.customer
                    customerEphemeralKey = customerWithEphemeralKeyResponse.ephemeralKey
                    //getPaymentMethods(::createSetupIntent)
                }
            }
        })
    }

    private fun getPaymentMethods(nextMethod: () -> (Unit)) {
        cropDroidAPI.getPaymentMethods(customer.processorId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                error.postValue(e.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    error.postValue(apiResponse.error)
                    return
                }
                if(apiResponse.payload != null) {
                    val response = apiResponse.payload as JSONArray
                    val paymentMethodTypes = PaymentMethodsParser.parse(response)
                    if(!paymentMethods.isNullOrEmpty()) {
                        paymentMethods = ArrayList()
                        for (paymentMethodType in paymentMethodTypes) {
                            if (paymentMethodType.card != null) {
                                paymentMethods.add(paymentMethodType.type)
                            }
                        }
                    }
                    nextMethod()
                }
            }
        })
    }

//    private fun createSetupIntent() {
//        cropDroidAPI.createSetupIntent(customer.processorId, object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                error.postValue(e.message)
//                return
//            }
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val apiResponse = APIResponseParser.parse(response)
//                if (!apiResponse.success) {
//                    error.postValue(apiResponse.error)
//                    return
//                }
//                if(apiResponse.payload != null) {
//                    val response = apiResponse.payload as JSONObject
//                    customerEphemeralKey = response.getString("client_secret")
//                    publishableKey = response.getString("publishable_key")
//                    getPaymentMethods(::present)
//                }
//            }
//        })
//    }

    private fun present() {
        activity.runOnUiThread {
            PaymentConfiguration.init(context, publishableKey)
            val customerSheetConfig = CustomerSheet.Configuration.builder(context.resources.getString(R.string.app_name)).build()
            val customerAdapeter = CustomerAdapter.create(
                context,
                this@CustomerSheetFacade,
                this@CustomerSheetFacade,
                paymentMethods)
            customerSheet = CustomerSheet.create(fragment, customerSheetConfig, customerAdapeter, ::onCustomerSheetResult)
            customerSheet.present()
        }
    }

    override fun onCustomerSheetResult(result: CustomerSheetResult) {
        when(result) {
            is CustomerSheetResult.Selected -> {
                Log.i(TAG,  "CustomerSheetResult.Selected")
            }
            is CustomerSheetResult.Canceled -> {
                Log.i(TAG,  "CustomerSheetResult.Canceled")
            }
            is CustomerSheetResult.Failed -> {
                Log.i(TAG,  "CustomerSheetResult.Failed")
            }
        }
    }

    override suspend fun provideCustomerEphemeralKey(): CustomerAdapter.Result<CustomerEphemeralKey> {
        return CustomerAdapter.Result.success(CustomerEphemeralKey.create(customer.processorId, customerEphemeralKey))
    }

    override suspend fun provideSetupIntentClientSecret(customerId: String): CustomerAdapter.Result<String> {
        return if(clientSetupSecret.isNullOrEmpty()) {
            CustomerAdapter.Result.failure(Exception(error.value), error.value)
        } else {
            CustomerAdapter.Result.success(clientSetupSecret)
        }
    }
}