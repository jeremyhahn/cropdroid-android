package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.databinding.FragmentCartProductListBinding
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.utils.Preferences

class ProductListFragment : Fragment() {

    lateinit private var binding: FragmentCartProductListBinding
    lateinit private var cropDroidAPI: CropDroidAPI
    lateinit private var productViewModel: ProductViewModel
    private var products = ArrayList<Product>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cart_product_list, container, false)

        var fragmentActivity = requireActivity()

        fragmentActivity.addMenuProvider(object : MenuProvider {  // 2
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.cart_checkout, menu)  // 3
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) { // 4
                    R.id.settings -> {
                        // navigate to settings
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)



        val preferences = Preferences(fragmentActivity)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        val hostname = preferences.currentController()

        val connection = EdgeDeviceRepository(fragmentActivity).get(hostname)!!
        cropDroidAPI = CropDroidAPI(connection, controllerSharedPrefs)

        productViewModel = ViewModelProviders.of(this,
            ProductViewModelFactory(fragmentActivity, cropDroidAPI))[ProductViewModel::class.java]

        binding.cartRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.cartRecyclerView.adapter = CartAdapter(products)
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(fragmentActivity.applicationContext, RecyclerView.VERTICAL, false)

        binding.cartSwipeRefresh.setOnRefreshListener {
            productViewModel.getProducts()
            binding.cartSwipeRefresh.isRefreshing = false
        }
        binding.cartSwipeRefresh.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        productViewModel.products.observe(viewLifecycleOwner, Observer {
            binding.cartSwipeRefresh.isRefreshing = false
            products = productViewModel.products.value!!

            binding.cartRecyclerView.itemAnimator = DefaultItemAnimator()
            binding.cartRecyclerView.adapter = CartAdapter(products)
            binding.cartRecyclerView.adapter!!.notifyDataSetChanged()

            if(products.size <= 0) {
                binding.cartEmptyText.visibility = View.VISIBLE
            } else {
                binding.cartEmptyText.visibility = View.GONE
            }
        })

        productViewModel.getProducts()

        return binding.root

//        var fragmentView = inflater.inflate(R.layout.fragment_product_list, container, false)
//        return fragmentView
    }

    // Post view initialization logic
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //binding = FragmentProductListBinding.bind(view)
//        // Connect adapters
//        productAdapter = ProductAdapter(productClickCallback)
//        binding.productsList.setAdapter(productAdapter)
//
//        // Initialize view properties, set click listeners, etc.
//        binding.productsSearchBtn.setOnClickListener {}
//
//        // Subscribe to state
//        viewModel.products.observe(this, Observer { myProducts ->
//        })

    }

    // Provided to ProductAdapter
//    private val productClickCallback = ProductClickCallback { product ->
//        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
//            (requireActivity() as ProductListActivity).show(product)
//        }
//    }
}