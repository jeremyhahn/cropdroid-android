package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.BR
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.Prompt
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.databinding.FragmentCartCheckoutBinding
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Customer
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.ShippingAddress
import com.jeremyhahn.cropdroid.ui.shoppingcart.parser.CustomerParser
import com.jeremyhahn.cropdroid.ui.shoppingcart.parser.PaymentIntentResponseParser
import com.jeremyhahn.cropdroid.ui.shoppingcart.parser.SetupIntentResponseParser
import com.jeremyhahn.cropdroid.ui.shoppingcart.rest.CreateInvoiceRequest
import com.jeremyhahn.cropdroid.ui.shoppingcart.rest.PaymentIntentResponse
import com.jeremyhahn.cropdroid.ui.shoppingcart.rest.SetDefaultPaymentMethodRequest
import com.jeremyhahn.cropdroid.ui.shoppingcart.viewmodel.CartViewModel
import com.jeremyhahn.cropdroid.ui.shoppingcart.viewmodel.CheckoutViewModel
import com.jeremyhahn.cropdroid.utils.Preferences
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmSetupIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.Address
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.addresselement.AddressDetails
import com.stripe.android.paymentsheet.addresselement.AddressLauncher
import com.stripe.android.paymentsheet.addresselement.AddressLauncherResult
import com.stripe.android.view.CardInputWidget
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException

enum class AddressType {
    BILLING,
    SHIPPING
}

class CheckoutFragment : Fragment(), CartListener {

    companion object{
        const private val TAG = "CheckoutFragment"
        const val ErrMissingPaymentMethod = "missing payment method"
        const val ErrMissingPaymentMethodUserMessage = "Unable to set payment method due to an " +
                "unexpected backend failure. Please try again later or oontact support for assistance if the problem continues."
        const val ErrInvoiceOustanding = "outstanding invoice"
        const val ErrInvoiceOustandingUserMessage = "You already have an outstanding invoice due. Please " +
                " satisfy your existing invoice and then complete your checkout."
    }

    private lateinit var binding: FragmentCartCheckoutBinding
    private lateinit var cropDroidAPI: CropDroidAPI
    private lateinit var fragmentActivity: FragmentActivity
    private lateinit var fragmentContext: Context
    private lateinit var mainActivity: MainActivity
    private lateinit var preferences: Preferences
    //private lateinit var customerSheetFacade: CustomerSheetFacade
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var paymentSheetCustomerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private lateinit var customerEphemeralKey: String
    private lateinit var setupIntentClientSecret: String
    private lateinit var shippingAddressLauncher: AddressLauncher
    private lateinit var billingDetailsAddressLauncher: AddressLauncher
    private lateinit var cartListAdapter: CartListAdapter
    private lateinit var publishableKey: String
    private lateinit var stripe: Stripe
    private lateinit var toolbarMenu: Menu

    private val creditCardWidgetFragment = CreditCardWidgetFragment()
    private var shippingDetails: AddressDetails? = null
    private var defaultBillingDetails: PaymentSheet.BillingDetails? = null
    private var customer: Customer? = null
    private var billingAddress: Address? = null
    private var shippingAddress: Address? = null
    private var paymentIntentResponse: PaymentIntentResponse? = null
    private var checkoutFlag = false
    private var pendingInvoiceId: String = ""

    private val cartViewModel: CartViewModel by activityViewModels()
    private val checkoutViewModel: CheckoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentActivity = requireActivity()
        fragmentContext = requireContext()
        mainActivity = (fragmentActivity as MainActivity)
        preferences = Preferences(fragmentActivity)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        shippingAddressLauncher = AddressLauncher(this, ::onShippingAddressLauncherResult)
        billingDetailsAddressLauncher = AddressLauncher(this, ::onBillingDetailsAddressLauncherResult)
        val hostname = preferences.currentController()
        val connection = EdgeDeviceRepository(fragmentActivity).get(hostname)
        if(connection == null) { // user is not logged in
            mainActivity.navigateToHome()
            return
        }
        cropDroidAPI = CropDroidAPI(connection, preferences.getControllerPreferences())
        loadPublishableKey()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cart_checkout, container, false)

        //customerSheetFacade = CustomerSheetFacade(fragmentActivity, fragmentContext,this, cropDroidAPI)

        fragmentActivity.addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.emptyCart -> {
                        Prompt(fragmentContext).show(
                            resources.getString(R.string.store_empty_cart),
                            resources.getString(R.string.store_prompt_empty_cart),
                            DialogInterface.OnClickListener { dialog, which ->
                                cartViewModel.clear()
                            },
                            null
                        )
                        true
                    }
                    R.id.pay -> {
                        checkout()
                        true
                    }
                    else -> false
                }
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.shoppingcart_checkout, menu)
                toolbarMenu = menu
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.cart = cartViewModel

        cartListAdapter = CartListAdapter(cartViewModel)
        binding.cartRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.cartRecyclerView.adapter = cartListAdapter
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(fragmentActivity.applicationContext, RecyclerView.VERTICAL, false)

//        customerSheetFacade.error.observe(viewLifecycleOwner, Observer {
//            fragmentActivity.runOnUiThread {
//                AppError(fragmentContext).error(customerSheetFacade.error.value!!)
//            }
//        })

        checkoutViewModel.creditCardLast4.observe(viewLifecycleOwner, Observer {
            applyCheckoutViewModelBindings()
        })

        cartViewModel.total.observe(viewLifecycleOwner, Observer {
            applyCartViewModelBindings()
        })

        cartViewModel.size.observe(viewLifecycleOwner, Observer {
            toggleToolbarVisibility()
            applyCartViewModelBindings()
        })

        cartViewModel.shippingAddress.observe(viewLifecycleOwner, Observer {
            toggleToolbarVisibility()
            applyCartViewModelBindings()
        })

        binding.cartSwipeRefresh.setOnRefreshListener {
            toggleToolbarVisibility()
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

    private fun applyCartViewModelBindings() {
        binding.apply {
            binding.setVariable(BR.cart, cartViewModel)
            binding.executePendingBindings()
        }
    }

    private fun applyCheckoutViewModelBindings() {
        binding.apply {
            binding.setVariable(BR.checkout, checkoutViewModel)
            binding.executePendingBindings()
        }
    }

    override fun editAddress() {
        if(this.customer == null) {
            presentBillingDetailsAddressLauncher()
        } else {
            presentShippingDetailsAddressLauncher()
        }
    }

    override fun clear() {
        applyCartViewModelBindings()
        cartListAdapter.notifyDataSetChanged()
    }

    override fun checkout() {
        if (this.customer == null) {
            checkoutFlag = true
            toolbarMenu.findItem(R.id.pay).setEnabled(false)
            createSetupIntent()
        } else {
            createInvoice()
        }
    }

    override fun cancelDefaultPaymentMethod() {
        val creditCardLayout = fragmentActivity.supportFragmentManager.findFragmentById(R.id.creditCardLayout)
        if(cartViewModel.creditCardLast4.value == null) {
            binding.btnSavePaymentMethod.visibility = View.VISIBLE
            binding.btnEditPaymentMethod.visibility = View.GONE
            binding.creditCardLast4.visibility = View.GONE
            if(creditCardLayout != null) {
                binding.btnCancelPaymentMethod.visibility = View.GONE
            }
        } else {
            binding.btnSavePaymentMethod.visibility = View.GONE
            binding.btnEditPaymentMethod.visibility = View.VISIBLE
            binding.creditCardLast4.visibility = View.VISIBLE
            if(creditCardLayout != null) {
                binding.btnCancelPaymentMethod.visibility = View.VISIBLE
            } else {
                binding.btnCancelPaymentMethod.visibility = View.GONE
            }
        }
    }

    override fun saveDefaultPaymentMethod() {
        val creditCardLayout = fragmentActivity.supportFragmentManager.findFragmentById(R.id.creditCardLayout)
        if (creditCardLayout != null) {
            val cardInputWidget: CardInputWidget? = creditCardLayout.requireView().findViewById(R.id.cardInputWidget)
            if(cardInputWidget != null) {
                cardInputWidget.paymentMethodCreateParams?.let { params ->
                    showLoadingOverlay()
                    if(::setupIntentClientSecret.isInitialized && setupIntentClientSecret != null) {
                        // This is a new customer
                        val setupParams = ConfirmSetupIntentParams.create(params, setupIntentClientSecret)
                        stripe.confirmSetupIntent(this@CheckoutFragment, setupParams)
                        hideCreditCardWidget()
                        setDefaultPaymentMethod()
                        setupIntentClientSecret = ""
                    } else {
                        // This is a returning customer updating their payment method
                        stripe.createPaymentMethod(params, null, null, object:
                            ApiResultCallback<PaymentMethod> {
                            override fun onError(e: Exception) {
                                AppError(fragmentContext).error(e)
                            }
                            override fun onSuccess(paymentMethod: PaymentMethod) {
                                attachAndSetDefaultPaymentMethod(paymentMethod.id!!)
                            }
                        })
                    }
                }
            }
        }
    }

    fun attachAndSetDefaultPaymentMethod(paymentMethodId: String) {
        val setDefaultPaymentMethodRequest = SetDefaultPaymentMethodRequest(
            customer!!.id,
            customer!!.processorId,
            paymentMethodId
        )
        cropDroidAPI.attachAndSetDefaultPaymentMethod(setDefaultPaymentMethodRequest, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                fragmentActivity.runOnUiThread {
                    AppError(fragmentContext).exception(e)
                    hideLoadingOverlay()
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    fragmentActivity.runOnUiThread {
                        AppError(fragmentContext).apiAlert(apiResponse)
                        hideLoadingOverlay()
                    }
                    return
                }
                if(apiResponse.payload != null) {
                    val response = apiResponse.payload as JSONObject
                    customer = CustomerParser.parse(response)
                    fragmentActivity.runOnUiThread {
                        hideCreditCardWidget()
                    }
                    hideLoadingOverlay()
                    updateCreditCardLast4()
                }
            }
        })
    }

    private fun setDefaultPaymentMethod(paymentMethodId: String = "", requestCounter: Int = 0) {
        val setDefaultPaymentMethodRequest = SetDefaultPaymentMethodRequest(
            customer!!.id,
            customer!!.processorId,
            paymentMethodId
        )
        cropDroidAPI.setDefaultPaymentMethod(setDefaultPaymentMethodRequest, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                fragmentActivity.runOnUiThread {
                    AppError(fragmentContext).exception(e)
                    hideLoadingOverlay()
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    if(apiResponse.error == ErrMissingPaymentMethod) {
                        // A consistent, reproducible race condition is happening here...
                        // Retry this operation up to 5 more times, waiting 1 second between
                        // each request to give Stripe time to create the payment method on their side.
                        if(requestCounter >= 6) {
                            Log.d(TAG, "request counter: ${requestCounter}")
                            fragmentActivity.runOnUiThread {
                                AppError(fragmentContext).error(ErrMissingPaymentMethodUserMessage)
                                hideLoadingOverlay()
                            }
                            return
                        }
                        Thread.sleep(1000)
                        setDefaultPaymentMethod(paymentMethodId, requestCounter + 1)
                    }
                    return
                }
                hideLoadingOverlay()
                if(apiResponse.payload != null) {
                    val response = apiResponse.payload as JSONObject
                    customer = CustomerParser.parse(response)
                    updateCreditCardLast4()
                }
            }
        })
    }

    override fun editDefaultPaymentMethod() {
        binding.creditCardLast4.visibility = View.GONE
        showCreditCardWidget()
    }

    private fun showCreditCardWidget() {
        fragmentActivity.runOnUiThread {
            binding.btnSavePaymentMethod.visibility = View.VISIBLE
            binding.btnEditPaymentMethod.visibility = View.GONE
            val creditCardLayout =
                fragmentActivity.supportFragmentManager.findFragmentById(R.id.creditCardLayout)
            if (creditCardLayout == null) {
                fragmentActivity.supportFragmentManager.beginTransaction()
                    .add(R.id.creditCardLayout, creditCardWidgetFragment).commit()
            }
        }
    }

    private fun hideCreditCardWidget() {
        binding.btnSavePaymentMethod.visibility = View.GONE
        if(customer!!.paymentMethodId.isNotEmpty()) {
            binding.btnEditPaymentMethod.visibility = View.VISIBLE
            binding.creditCardLast4.visibility = View.VISIBLE
        } else {
            binding.btnEditPaymentMethod.visibility = View.GONE
            binding.creditCardLast4.visibility = View.GONE
        }
        binding.btnSavePaymentMethod.visibility = View.GONE
        fragmentActivity.supportFragmentManager.beginTransaction().remove(creditCardWidgetFragment).commit()
    }

    fun doAnimateView(view: View, toVisibility: Int, toAlpha: Float, duration: Int) {
        val show = toVisibility == View.VISIBLE
        if (show) {
            view.alpha = 0f
        }
        view.visibility = View.VISIBLE
        view.animate()
            .setDuration(duration.toLong())
            .alpha(if (show) toAlpha else 0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = toVisibility
                }
            })
    }

    private fun showLoadingOverlay() {
        fragmentActivity.runOnUiThread {
            val progressOverlay: View? = fragmentActivity.findViewById(R.id.progress_overlay)
            doAnimateView(progressOverlay!!, View.VISIBLE, 0.4f, 200)
            progressOverlay.bringToFront()
        }
    }

    private fun hideLoadingOverlay() {
        fragmentActivity.runOnUiThread {
            val progressOverlay: View? = fragmentActivity.findViewById(R.id.progress_overlay)
            doAnimateView(progressOverlay!!, View.GONE, 0f, 200)
            progressOverlay.bringToFront()
        }
    }

    private fun toggleToolbarVisibility() {
        fragmentActivity.runOnUiThread {
            if (cartViewModel.size.value!! > 0) {
                if (::toolbarMenu.isInitialized) {
                    toolbarMenu.findItem(R.id.emptyCart)!!.setVisible(true)
                    toolbarMenu.findItem(R.id.pay)!!.setVisible(true)
                }
                binding.cartEmptyText.visibility = View.GONE
            } else {
                if (::toolbarMenu.isInitialized) {
                    toolbarMenu.findItem(R.id.emptyCart)?.setVisible(false)
                    toolbarMenu.findItem(R.id.pay)?.setVisible(false)
                }
                binding.cartEmptyText.visibility = View.VISIBLE
            }
        }
    }

    private fun loadPublishableKey() {
        cropDroidAPI.getPublishableKey(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure response: " + e!!.message)
                fragmentActivity.runOnUiThread {
                    AppError(fragmentContext).exception(e)
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    fragmentActivity.runOnUiThread {
                        AppError(fragmentContext).apiAlert(apiResponse)
                    }
                    return
                }
                if(apiResponse.payload == null) {
                    fragmentActivity.runOnUiThread {
                        AppError(fragmentContext).alert(
                            "null publishable key returned from the server",
                            null,
                            null)
                    }
                    return
                }
                publishableKey = apiResponse.payload as String
                if(!publishableKey.isNullOrEmpty()) {
                    PaymentConfiguration.init(fragmentContext, publishableKey)
                    stripe = Stripe(fragmentContext, publishableKey)
                    getCustomer()
                }
            }
        })
    }

    private fun getCustomer() {
        cropDroidAPI.getCustomer(preferences.currentUserId(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                fragmentActivity.runOnUiThread {
                    AppError(fragmentContext).exception(e)
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    fragmentActivity.runOnUiThread {
                        AppError(fragmentContext).apiAlert(apiResponse)
                    }
                    return
                }
                if(apiResponse.payload != null) {
                    val response = apiResponse.payload as JSONObject
                    customer = CustomerParser.parse(response)
                    cartViewModel.setShippingAddress(customer!!.shipping!!)
                    if(!updateCreditCardLast4()) {
                        showCreditCardWidget()
                    }
                } else {
                    //createPaymentSheetWithSetupIntent()
                    createSetupIntent()
                }
            }
        })
    }

    private fun updateCreditCardLast4(): Boolean {
        if(customer!!.paymentMethodLast4.isNotEmpty()) {
            checkoutViewModel.setCreditCardLast4("**** **** **** %s".format(customer!!.paymentMethodLast4))
            return true
        }
        return false
    }

    private fun createSetupIntent() {
        cropDroidAPI.createSetupIntent(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                fragmentActivity.runOnUiThread {
                    AppError(fragmentContext).exception(e)
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    fragmentActivity.runOnUiThread {
                        AppError(fragmentContext).apiAlert(apiResponse)
                    }
                    return
                }
                if(apiResponse.payload != null) {
                    val response = apiResponse.payload as JSONObject
                    val setupIntentResponse = SetupIntentResponseParser.parse(response)
                    customer = setupIntentResponse.customer
                    setupIntentClientSecret = setupIntentResponse.clientSecret
                    publishableKey = setupIntentResponse.publishableKey
                    customerEphemeralKey = setupIntentResponse.ephemeralKey
                    paymentSheetCustomerConfig = PaymentSheet.CustomerConfiguration(
                        setupIntentResponse.customer.processorId,
                        setupIntentResponse.ephemeralKey
                    )
                    presentBillingDetailsAddressLauncher()
                    showCreditCardWidget()
                } else {
                    AppError(fragmentContext).error("unexpected CreateSetupIntent response from server")
                }
            }
        })
    }

    private fun createInvoice() {
        if(pendingInvoiceId.isNotEmpty()) {
            presentPaymentSheet()
            return
        }
        val createInvoiceRequest = CreateInvoiceRequest("", cartViewModel.getProducts())
        cropDroidAPI.createInvoice(createInvoiceRequest, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure response: " + e!!.message)
                fragmentActivity.runOnUiThread {
                    AppError(fragmentContext).exception(e)
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    if(apiResponse.error == ErrInvoiceOustanding) {
                        AppError(fragmentContext).error(ErrInvoiceOustandingUserMessage)
                    }
                    fragmentActivity.runOnUiThread {
                        AppError(fragmentContext).apiAlert(apiResponse)
                    }
                    return
                }
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

                    pendingInvoiceId = paymentIntentResponse!!.invoiceId
                    presentPaymentSheet()
                }
                catch(e: Exception) {
                    fragmentActivity.runOnUiThread {
                        AppError(fragmentActivity).error(e)
                    }
                }

            }
        })
    }

    private fun presentBillingDetailsAddressLauncher() {
        billingDetailsAddressLauncher.present(
            publishableKey = publishableKey,
            configuration = getAddressLauncherConfig(AddressType.BILLING)
        )
    }

    private fun presentShippingDetailsAddressLauncher() {
        shippingAddressLauncher.present(
            publishableKey = publishableKey,
            configuration = getAddressLauncherConfig(AddressType.SHIPPING)
        )
    }

    private fun presentPaymentSheet() {
        // val googlePayConfiguration = PaymentSheet.GooglePayConfiguration(
        //    environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
        //    countryCode = "US",
        //    currencyCode = "USD" // Required for Setup Intents, optional for Payment Intents
        //)
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = resources.getString(R.string.app_name),
                customer = paymentSheetCustomerConfig,
                // Setting this value hides the "Shipping address is same as billing" checkbox on the payment sheet
                // defaultBillingDetails = defaultBillingDetails,
                shippingDetails = shippingDetails,
                // Set `allowsDelayedPaymentMethods` to true if your business handles
                // delayed notification payment methods like US bank accounts.
                // allowsDelayedPaymentMethods = true
                billingDetailsCollectionConfiguration = PaymentSheet.BillingDetailsCollectionConfiguration(
                    attachDefaultsToPaymentMethod = true
                ),
                // googlePay = googlePayConfiguration
            )
        )
    }

    private fun getAddressLauncherConfig(addressType: AddressType): AddressLauncher.Configuration {
        var addressDetails: AddressDetails? = null
        var title = resources.getString(R.string.store_billing_details)
        if(addressType == AddressType.BILLING && customer != null && customer!!.address != null) {
            title = resources.getString(R.string.store_billing_details)
            addressDetails = AddressDetails(
                name = customer!!.name,
                phoneNumber = customer!!.phone,
                address = billingAddress
            )
        }
        else if(customer != null && customer!!.shipping != null) {
            title = resources.getString(R.string.store_shipping_details)
            addressDetails = AddressDetails(
                name = customer!!.shipping!!.name,
                phoneNumber = customer!!.shipping!!.phone,
                address = Address(
                    line1 = customer!!.shipping!!.address.line1,
                    line2 = customer!!.shipping!!.address.line2,
                    city = customer!!.shipping!!.address.city,
                    state = customer!!.shipping!!.address.state,
                    postalCode = customer!!.shipping!!.address.postalCode,
                    country = customer!!.shipping!!.address.country))
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

                val billingDetails = result.address
                val address = com.jeremyhahn.cropdroid.ui.shoppingcart.model.Address(
                    id = 0L,
                    line1 = billingDetails.address?.line1!!,
                    line2 = billingDetails.address?.line2!!,
                    city = billingDetails.address?.city!!,
                    state = billingDetails.address?.state!!,
                    postalCode = billingDetails.address?.postalCode!!,
                    country = billingDetails.address?.country!!)

                if(customer == null) {
                    customer = Customer(
                        mainActivity.user!!.id.toLong(),
                        "",
                        "",
                        billingDetails.name!!,
                        mainActivity.user!!.username,  // email address
                        billingDetails.phoneNumber!!,
                        null,
                        null,
                        "",
                        ""
                    )
                }

                customer!!.name = billingDetails.name!!
                customer!!.address = address
                customer!!.shipping = ShippingAddress(
                    0L,
                    billingDetails.name!!,
                    billingDetails.phoneNumber!!,
                    address
                )

                cropDroidAPI.updateCustomer(customer!!, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d(TAG, "onFailure response: " + e!!.message)
                        fragmentActivity.runOnUiThread {
                            AppError(fragmentContext).exception(e)
                        }
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val apiResponse = APIResponseParser.parse(response)
                        if (!apiResponse.success) {
                            fragmentActivity.runOnUiThread {
                                AppError(fragmentContext).apiAlert(apiResponse)
                            }
                            return
                        }
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
                    id = customer!!.shipping!!.address.id,
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

                cropDroidAPI.updateCustomer(customer!!, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d(TAG, "onFailure response: " + e!!.message)
                        fragmentActivity.runOnUiThread {
                            AppError(fragmentContext).exception(e)
                        }
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val apiResponse = APIResponseParser.parse(response)
                        if (!apiResponse.success) {
                            fragmentActivity.runOnUiThread {
                                AppError(fragmentContext).apiAlert(apiResponse)
                            }
                            return
                        }
                        val jsonCustomer = apiResponse.payload as JSONObject
                        customer = CustomerParser.parse(jsonCustomer)
                        cartViewModel.setShippingAddress(customer!!.shipping!!)
                        if(checkoutFlag) createInvoice()
                    }
                })
            }
            is AddressLauncherResult.Canceled -> {
                // Do nothing
            }
        }
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        Log.d(TAG, "payment result: " + paymentSheetResult.toString())
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Log.i(TAG,  "Payment canceled")
                // Reload the user because they may have deleted their payment method in the payment sheet
                getCustomer()
            }
            is PaymentSheetResult.Failed -> {
                Log.e(TAG,  "Payment error: ${paymentSheetResult.error}")

                AppError(fragmentContext).error("Payment failed. Please update your payment method and try again.")
            }
            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                Log.i(TAG,  "Completed")
                checkoutFlag = false
                pendingInvoiceId = ""
                cartViewModel.clear()
                //mainActivity.navigateToShoppingCart()
            }
        }
    }

//    private fun getSetupIntent(nextMethod: (() -> Unit)?) {
//        cropDroidAPI.getSetupIntent(SetupIntentRequest(setupIntentClientSecret), object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                fragmentActivity.runOnUiThread {
//                    AppError(fragmentContext).exception(e)
//                }
//                return
//            }
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val apiResponse = APIResponseParser.parse(response)
//                if (!apiResponse.success) {
//                    fragmentActivity.runOnUiThread {
//                        AppError(fragmentContext).apiAlert(apiResponse)
//                    }
//                    return
//                }
//                if(apiResponse.payload == null) {
//                    AppError(fragmentContext).error("unable to retrieve SetupIntent")
//                }
//                val response = apiResponse.payload as JSONObject
//                setupIntentClientSecret = response.getString("client_secret")
//                if(nextMethod != null) nextMethod()
//            }
//        })
//    }

    //    fun attachPaymentMethod(paymentMethodId: String) {
//        val setDefaultPaymentMethodRequest = SetDefaultPaymentMethodRequest(
//            customer!!.id,
//            customer!!.processorId,
//            paymentMethodId
//        )
//        cropDroidAPI.attachPaymentMethod(setDefaultPaymentMethodRequest, object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                fragmentActivity.runOnUiThread {
//                    AppError(fragmentContext).exception(e)
//                }
//                return
//            }
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val apiResponse = APIResponseParser.parse(response)
//                if (!apiResponse.success) {
//                    fragmentActivity.runOnUiThread {
//                        AppError(fragmentContext).apiAlert(apiResponse)
//                    }
//                    return
//                }
//                if(apiResponse.payload != null) {
//                    //val response = apiResponse.payload as JSONArray
//                }
//                getCustomer()
//            }
//        })
//    }

    //    private fun handleCheckoutButtonPressed() {
//        val intentConfig = PaymentSheet.IntentConfiguration(
//            mode = PaymentSheet.IntentConfiguration.Mode.Setup(
//                currency = "usd",
//            ),
//            // Other configuration options...
//        )
//
//        paymentSheet.presentWithIntentConfiguration(
//            intentConfiguration = intentConfig,
//            // Optional configuration - See the "Customize the sheet" section in this guide
//            configuration = PaymentSheet.Configuration(
//                merchantDisplayName = "Example Inc.",
//            )
//        )
//    }

//    private fun createPaymentSheetWithSetupIntent() {
//        paymentSheet = PaymentSheet(
//            activity = fragmentActivity,
//            createIntentCallback = { _, _ ->
//                val synchronousResponse = cropDroidAPI.createSetupIntentForNewCustomer()
//                if(synchronousResponse.error != null) {
//                    AppError(fragmentContext).exception(synchronousResponse.error)
//                    CreateIntentResult.Failure(cause = synchronousResponse.error, displayMessage = "Failed to initialize SetupIntent")
//                } else if(synchronousResponse.response != null) {
//                    val jsonResponse = synchronousResponse.response.body().string() as JSONObject
//                    val setupIntentResponse = SetupIntentResponseParser.parse(jsonResponse)
//                    CreateIntentResult.Success(setupIntentResponse.clientSecret)
//                } else {
//                    val invalidResponseException = Exception("invalid response from server")
//                    CreateIntentResult.Failure(
//                        cause = invalidResponseException,
//                        displayMessage = invalidResponseException.message
//                    )
//                }
//            },
//            paymentResultCallback = ::onPaymentSheetResult,
//        )
//    }

//    private fun presentPaymentSheetWithSetupIntent() {
//        paymentSheet.presentWithSetupIntent(
//            setupIntentClientSecret,
//            PaymentSheet.Configuration(
//                merchantDisplayName = resources.getString(R.string.app_name),
//                customer = paymentSheetCustomerConfig,
//                // Set `allowsDelayedPaymentMethods` to true if your business handles
//                // delayed notification payment methods like US bank accounts.
//                //allowsDelayedPaymentMethods = true
//                shippingDetails = shippingDetails,
//                // Set `allowsDelayedPaymentMethods` to true if your business handles
//                // delayed notification payment methods like US bank accounts.
//                // allowsDelayedPaymentMethods = true
////                billingDetailsCollectionConfiguration = PaymentSheet.BillingDetailsCollectionConfiguration(
////                    attachDefaultsToPaymentMethod = true
////                )
//            )
//        )
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        stripe.onPaymentResult(resultCode, data, object: ApiResultCallback<PaymentIntentResult> {
//            override fun onSuccess(result: PaymentIntentResult) {
//                val paymentIntent = result.intent
//                if(paymentIntent.status == StripeIntent.Status.Succeeded) {
//                    Log.d("TAG", "Payment intent status: ${paymentIntent.status}")
//                } else if(paymentIntent.status == StripeIntent.Status.RequiresPaymentMethod) {
//                    Log.d("TAG", "Payment failed: ${paymentIntent.lastPaymentError?.message.orEmpty()}")
//                } else {
//                    Log.d("TAG", "Payment intent status with error: ${paymentIntent.lastPaymentError?.message.orEmpty()}")
//                }
//            }
//            override fun onError(e: Exception) {
//                Log.e("TAG", e.message.orEmpty())
//            }
//        })
//
//        stripe.onSetupResult(resultCode, data, object: ApiResultCallback<SetupIntentResult> {
//            override fun onSuccess(result: SetupIntentResult) {
//                val setupIntent = result.intent
//                if(setupIntent.status == StripeIntent.Status.Succeeded) {
//                    Log.d("TAG", "Payment intent status: ${setupIntent.status}")
//                } else if(setupIntent.status == StripeIntent.Status.RequiresPaymentMethod) {
//                    Log.d("TAG", "Payment failed: ${setupIntent.lastSetupError?.message.orEmpty()}")
//                } else {
//                    Log.d("TAG", "Payment intent status with error: ${setupIntent.lastSetupError?.message.orEmpty()}")
//                }
//            }
//            override fun onError(e: Exception) {
//                Log.e("TAG", "setup intent error: ${e.message.orEmpty()}")
//            }
//        })
//    }
}