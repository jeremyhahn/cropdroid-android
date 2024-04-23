package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.BR
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.databinding.BadgeShoppingCartBinding
import com.jeremyhahn.cropdroid.databinding.FragmentCartProductListBinding
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Product
import com.jeremyhahn.cropdroid.utils.Preferences

class ProductListFragment : Fragment() {

    private val TAG = "ProductListFragment"
    private var products = ArrayList<Product>()
    lateinit private var productListBinding: FragmentCartProductListBinding
    lateinit private var cartBinding: BadgeShoppingCartBinding
    lateinit private var cropDroidAPI: CropDroidAPI
    lateinit private var productViewModel: ProductViewModel
    lateinit private var cartTextView: TextView
    lateinit private var fragmentActivity: FragmentActivity
    lateinit private var mainActivity: MainActivity
    private lateinit var productListAdapter: ProductListAdapter

    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentActivity = requireActivity()
        mainActivity = (fragmentActivity as MainActivity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        productListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cart_product_list, container, false)

        val preferences = Preferences(fragmentActivity)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        val hostname = preferences.currentController()

        val connection = EdgeDeviceRepository(fragmentActivity).get(hostname)
        if(connection == null) { // user is not logged in
            mainActivity.navigateToHome()
            return productListBinding.root
        }

        cropDroidAPI = CropDroidAPI(connection, controllerSharedPrefs)

        fragmentActivity.addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                Log.d(TAG, "onMenuItemSelected: " + menuItem.toString())
                return true
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.cart_checkout, menu)

                val badgeLayout = menu.findItem(R.id.item_cart).actionView as RelativeLayout?
                cartTextView = badgeLayout!!.findViewById<View>(R.id.actionbar_cart_textview) as TextView

                cartBinding = DataBindingUtil.bind(badgeLayout)!!

                cartTextView.setOnClickListener(View.OnClickListener {
                    mainActivity.navigateToShoppingCartCheckout()
                })
                val imageView = badgeLayout!!.findViewById<View>(R.id.actionbar_cart_imageview) as ImageView
                imageView.setBackgroundResource(R.drawable.badge_selector) // Apply pressed state selector
                imageView.setOnClickListener(View.OnClickListener {
                    mainActivity.navigateToShoppingCartCheckout()
                })
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        productViewModel = ViewModelProviders.of(this,
            ProductViewModelFactory(fragmentActivity, cropDroidAPI))[ProductViewModel::class.java]

        productListAdapter = ProductListAdapter(products, cartViewModel)

        productListBinding.productRecyclerView.itemAnimator = DefaultItemAnimator()
        productListBinding.productRecyclerView.adapter = productListAdapter
        productListBinding.productRecyclerView.layoutManager = LinearLayoutManager(fragmentActivity.applicationContext, RecyclerView.VERTICAL, false)

        productListBinding.productSwipeRefresh.setOnRefreshListener {
            productListBinding.productSwipeRefresh.isRefreshing = true
            productViewModel.getProducts()
            applyProductListViewModelBindings()
            applyCartViewModelBindings()
            productListBinding.productSwipeRefresh.isRefreshing = false
        }

        productListBinding.productSwipeRefresh.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        productViewModel.products.observe(viewLifecycleOwner, Observer {
            products = productViewModel.products.value!!
            productListAdapter.updateProducts(products)
            hideProgressBar()
            toggleViewVisibility()
        })

        cartViewModel.size.observe(viewLifecycleOwner, Observer {
            if(::cartTextView.isInitialized) {
                cartTextView.text = cartViewModel.size.value.toString()
            }
            if(::cartBinding.isInitialized) {
                applyCartViewModelBindings()
            }
        })

        productViewModel.error.observe(viewLifecycleOwner, Observer {
            hideProgressBar()
            AppError(requireContext()).error(productViewModel.error.value.toString())
            toggleViewVisibility()
        })

        showProgressBar()
        productViewModel.getProducts()
        applyProductListViewModelBindings()

        return productListBinding.root
    }

    fun showProgressBar() {
        productListBinding.productEmptyText.visibility = View.GONE
        productListBinding.progressLoader.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        productListBinding.progressLoader.visibility = View.GONE
    }

    fun toggleViewVisibility() {
        if(products.size <= 0 && productListBinding.progressLoader.visibility != View.VISIBLE) {
            productListBinding.productEmptyText.visibility = View.VISIBLE
        } else {
            productListBinding.productEmptyText.visibility = View.GONE
        }
    }

    private fun applyCartViewModelBindings() {
        cartBinding.apply {
            cartBinding.setVariable(BR.cart, cartViewModel)
            cartBinding.executePendingBindings()
        }
    }

    private fun applyProductListViewModelBindings() {
        productListBinding.apply {
            productListBinding.setVariable(BR.cart, cartViewModel)
            productListBinding.executePendingBindings()
        }
    }
}