package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.BR
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.databinding.FragmentCartCheckoutBinding
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Customer
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.ShippingAddress
import com.jeremyhahn.cropdroid.ui.shoppingcart.parser.CustomerParser
import com.jeremyhahn.cropdroid.ui.shoppingcart.parser.PaymentIntentResponseParser
import com.jeremyhahn.cropdroid.ui.shoppingcart.rest.CreateInvoiceRequest
import com.jeremyhahn.cropdroid.ui.shoppingcart.rest.PaymentIntentResponse
import com.jeremyhahn.cropdroid.utils.Preferences
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet.Address
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.addresselement.AddressDetails
import com.stripe.android.paymentsheet.addresselement.AddressLauncher
import com.stripe.android.paymentsheet.addresselement.AddressLauncherResult
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

class CheckoutFragment : Fragment(), CartListener {

    private val TAG = "CheckoutFragment"
    private lateinit var binding: FragmentCartCheckoutBinding
    private lateinit var cropDroidAPI: CropDroidAPI
    private lateinit var fragmentActivity: FragmentActivity
    private lateinit var mainActivity: MainActivity
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var paymentSheetCustomerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private lateinit var shippingAddressLauncher: AddressLauncher
    private lateinit var billingDetailsAddressLauncher: AddressLauncher
    private lateinit var cartListAdapter: CartListAdapter

    private var shippingDetails: AddressDetails? = null
    private var defaultBillingDetails: PaymentSheet.BillingDetails? = null
    private var customer: Customer? = null
    private var billingAddress: Address? = null
    private var shippingAddress: Address? = null
    private var paymentIntentResponse: PaymentIntentResponse? = null

    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentActivity = requireActivity()
        mainActivity = (fragmentActivity as MainActivity)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        shippingAddressLauncher = AddressLauncher(this, ::onShippingAddressLauncherResult)
        billingDetailsAddressLauncher = AddressLauncher(this, ::onBillingDetailsAddressLauncherResult)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cart_checkout, container, false)

        val preferences = Preferences(fragmentActivity)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        val hostname = preferences.currentController()

        val connection = EdgeDeviceRepository(fragmentActivity).get(hostname)
        if(connection == null) { // user is not logged in
            mainActivity.navigateToHome()
            return binding.root
        }

        cropDroidAPI = CropDroidAPI(connection, controllerSharedPrefs)

        binding.cart = cartViewModel

        cartListAdapter = CartListAdapter(cartViewModel)
        binding.cartRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.cartRecyclerView.adapter = cartListAdapter
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(fragmentActivity.applicationContext, RecyclerView.VERTICAL, false)

        cartViewModel.total.observe(viewLifecycleOwner, Observer {
            applyCartViewModelBindings()
        })

        cartViewModel.size.observe(viewLifecycleOwner, Observer {
            toggleEmptyText()
            applyCartViewModelBindings()
        })

        binding.cartSwipeRefresh.setOnRefreshListener {
            toggleEmptyText()
            applyCartViewModelBindings()
            binding.cartSwipeRefresh.isRefreshing = false
        }

        binding.cartSwipeRefresh.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        cartViewModel.registerListener(this)
        applyCartViewModelBindings()

        return binding.root
    }

    private fun toggleEmptyText() {
        if (cartViewModel.size.value!! > 0) {
            binding.cartEmptyText.visibility = View.GONE
        } else {
            binding.cartEmptyText.visibility = View.VISIBLE
        }
    }

    private fun applyCartViewModelBindings() {
        binding.apply {
            binding.setVariable(BR.cart, cartViewModel)
            binding.executePendingBindings()
        }
    }

    override fun clear() {
        applyCartViewModelBindings()
        cartListAdapter.notifyDataSetChanged()
    }

    override fun checkout() {
        cropDroidAPI.getCustomer(mainActivity.user!!.id.toLong(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure response: " + e!!.message)
                activity!!.runOnUiThread {
                    AppError(requireContext()).exception(e)
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    activity!!.runOnUiThread {
                        AppError(requireContext()).apiAlert(apiResponse)
                    }
                    return
                }

                Log.d(TAG, "getCustomer payload: " + apiResponse.payload)

                if(apiResponse.payload == null) {
                    presentBillingDetailsAddressLauncher()
                } else {
                    val response = apiResponse.payload as JSONObject
                    customer = CustomerParser.parse(response)
                    createInvoice()
                }
            }
        })
    }

    fun createInvoice() {
        val createInvoiceRequest = CreateInvoiceRequest("", cartViewModel.getProducts())
        cropDroidAPI.createInvoice(createInvoiceRequest, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure response: " + e!!.message)
                activity!!.runOnUiThread {
                    AppError(requireContext()).exception(e)
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    activity!!.runOnUiThread {
                        AppError(requireContext()).apiAlert(apiResponse)
                    }
                    return
                }

                Log.d(TAG, "payload: " + apiResponse.payload)

                try {

                    val jsonPaymentIntent = apiResponse.payload as JSONObject
                    paymentIntentResponse = PaymentIntentResponseParser.parse(jsonPaymentIntent)

                    customer = paymentIntentResponse!!.customer
                    paymentIntentClientSecret = paymentIntentResponse!!.clientSecret
                    paymentSheetCustomerConfig = PaymentSheet.CustomerConfiguration(
                        paymentIntentResponse!!.customer.processorId,
                        paymentIntentResponse!!.ephemeralKey)

                    if(paymentIntentResponse!!.customer.address != null) {
                        billingAddress = Address(
                            line1 = paymentIntentResponse!!.customer.address?.line1,
                            line2 = paymentIntentResponse!!.customer.address?.line2,
                            city = paymentIntentResponse!!.customer.address?.city,
                            postalCode = paymentIntentResponse!!.customer.address?.postalCode,
                            state = paymentIntentResponse!!.customer.address?.state,
                            country = paymentIntentResponse!!.customer.address?.country)
                    }
                    if(paymentIntentResponse!!.customer.shipping != null) {
                        shippingAddress = Address(
                            line1 = paymentIntentResponse!!.customer.shipping?.address?.line1,
                            line2 = paymentIntentResponse!!.customer.shipping?.address?.line2,
                            city = paymentIntentResponse!!.customer.shipping?.address?.city,
                            postalCode = paymentIntentResponse!!.customer.shipping?.address?.postalCode,
                            state = paymentIntentResponse!!.customer.shipping?.address?.state,
                            country = paymentIntentResponse!!.customer.shipping?.address?.country)
                    }

                    defaultBillingDetails = PaymentSheet.BillingDetails(
                        name = paymentIntentResponse!!.customer.name,
                        email = paymentIntentResponse!!.customer.email,
                        phone = paymentIntentResponse!!.customer.phone,
                        address = billingAddress
                    )

                    PaymentConfiguration.init(
                        requireContext(),
                        paymentIntentResponse!!.publishableKey
                    )

                    presentPaymentSheet()
                }
                catch(e: Exception) {
                    activity!!.runOnUiThread {
                        AppError(fragmentActivity).error(e)
                    }
                }

            }
        })
    }

    private fun presentBillingDetailsAddressLauncher() {
        billingDetailsAddressLauncher.present(
            publishableKey = getPublishableKey(),
            configuration = getAddressLauncherConfig(paymentIntentResponse,
                resources.getString(R.string.store_billing_details))
        )
    }

    private fun presentShippingDetailsAddressLauncher() {
        shippingAddressLauncher.present(
            publishableKey = getPublishableKey(),
            configuration = getAddressLauncherConfig(
                paymentIntentResponse,
                resources.getString(R.string.store_shipping_details)
            )
        )
    }

    private fun getPublishableKey(): String {
        return if(paymentIntentResponse == null)
            ""
        else
            paymentIntentResponse!!.publishableKey
    }

    private fun getAddressLauncherConfig(paymentIntentResponse: PaymentIntentResponse?, title: String): AddressLauncher.Configuration {
        var addressDetails: AddressDetails? = null
        if(paymentIntentResponse != null && paymentIntentResponse.customer.address != null) {
            addressDetails = AddressDetails(
                name = paymentIntentResponse.customer.name,
                phoneNumber = paymentIntentResponse.customer.phone,
                address = billingAddress,
                isCheckboxSelected = true
            )
        }
        return AddressLauncher.Configuration(
            title = title,
            additionalFields = AddressLauncher.AdditionalFieldsConfiguration(
                phone = AddressLauncher.AdditionalFieldsConfiguration.FieldConfiguration.REQUIRED
            ),
            address = addressDetails,
            //allowedCountries = setOf("US", "CA", "GB"),
            googlePlacesApiKey = "(optional) YOUR KEY HERE"
        )
    }

    private fun onBillingDetailsAddressLauncherResult(result: AddressLauncherResult) {
        when (result) {
            is AddressLauncherResult.Succeeded -> {
                shippingDetails = result.address

                val address = com.jeremyhahn.cropdroid.ui.shoppingcart.model.Address(
                    id = 0L,
                    line1 = shippingDetails!!.address?.line1!!,
                    line2 = shippingDetails!!.address?.line2!!,
                    city = shippingDetails!!.address?.city!!,
                    state = shippingDetails!!.address?.state!!,
                    postalCode = shippingDetails!!.address?.postalCode!!,
                    country = shippingDetails!!.address?.country!!)

                customer = Customer(
                    mainActivity.user!!.id.toLong(),
                    "",
                    "",
                    result.address.name!!,
                    mainActivity.user!!.username,
                    result.address.phoneNumber!!,
                    address,
                    ShippingAddress(
                        0L,
                        "",
                        "",
                        address
                    ))

                cropDroidAPI.createCustomer(customer!!, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d(TAG, "onFailure response: " + e!!.message)
                        activity!!.runOnUiThread {
                            AppError(requireContext()).exception(e)
                        }
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val apiResponse = APIResponseParser.parse(response)
                        if (!apiResponse.success) {
                            activity!!.runOnUiThread {
                                AppError(requireContext()).apiAlert(apiResponse)
                            }
                            return
                        }
                        Log.d(TAG, "onBillingDetailsAddressLauncherResult payload: " + apiResponse.payload)
                        // Save the new customer with the newly created address ID and form field values
                        val jsonCustomer = apiResponse.payload as JSONObject
                        customer = CustomerParser.parse(jsonCustomer)

                        presentShippingDetailsAddressLauncher()
                    }
                })
            }
            is AddressLauncherResult.Canceled -> {
                // Do nothing
            }
        }
    }

    private fun onShippingAddressLauncherResult(result: AddressLauncherResult) {
        when (result) {
            is AddressLauncherResult.Succeeded -> {
                shippingDetails = result.address

                customer!!.shipping?.name = result.address.name!!
                customer!!.shipping?.phone = result.address.phoneNumber!!

                val address = com.jeremyhahn.cropdroid.ui.shoppingcart.model.Address(
                    id = customer!!.shipping!!.address!!.id,
                    line1 = shippingDetails!!.address?.line1!!,
                    line2 = shippingDetails!!.address?.line2!!,
                    city = shippingDetails!!.address?.city!!,
                    state = shippingDetails!!.address?.state!!,
                    postalCode = shippingDetails!!.address?.postalCode!!,
                    country = shippingDetails!!.address?.country!!
                )

                customer!!.shipping = ShippingAddress(
                    id = customer!!.shipping!!.id,
                    name = customer!!.shipping!!.name,
                    phone = customer!!.shipping!!.phone,
                    address = address
                )

                // Update the customers shipping address
                cropDroidAPI.updateCustomer(customer!!, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d(TAG, "onFailure response: " + e!!.message)
                        activity!!.runOnUiThread {
                            AppError(requireContext()).exception(e)
                        }
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val apiResponse = APIResponseParser.parse(response)
                        if (!apiResponse.success) {
                            activity!!.runOnUiThread {
                                AppError(requireContext()).apiAlert(apiResponse)
                            }
                            return
                        }
                        Log.d(TAG, "onShippingAddressLauncherResult payload: " + apiResponse.payload)
                        // Save the new customer with the newly created shipping address ID and form field values
                        val jsonCustomer = apiResponse.payload as JSONObject
                        customer = CustomerParser.parse(jsonCustomer)

                        // Create the invoice
                        createInvoice()
                        //presentPaymentSheet()
                    }
                })

            }
            is AddressLauncherResult.Canceled -> {
                // Do nothing
            }
        }
    }

    private fun presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = resources.getString(R.string.app_name),
                customer = paymentSheetCustomerConfig,
                //defaultBillingDetails = defaultBillingDetails,
                shippingDetails = shippingDetails,
                // Set `allowsDelayedPaymentMethods` to true if your business handles
                // delayed notification payment methods like US bank accounts.
                // allowsDelayedPaymentMethods = true
                billingDetailsCollectionConfiguration = PaymentSheet.BillingDetailsCollectionConfiguration(
                    attachDefaultsToPaymentMethod = true
                )
            )
        )
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        Log.d(TAG, "payment result: " + paymentSheetResult.toString())
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Log.i(TAG,  "Payment canceled")
            }
            is PaymentSheetResult.Failed -> {
                Log.e(TAG,  "Payment error: ${paymentSheetResult.error}")
            }
            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                Log.i(TAG,  "Completed")
                completePayment()
            }
        }
    }

    fun completePayment() {
        cartViewModel.clear()
        mainActivity.navigateToShoppingCart()
    }
}