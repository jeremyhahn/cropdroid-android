package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.databinding.FragmentCartCheckoutBinding

class CheckoutFragment : Fragment() {

    lateinit private var binding: FragmentCartCheckoutBinding
    lateinit private var cropDroidAPI: CropDroidAPI
    lateinit private var productViewModel: ProductViewModel
    private var products = ArrayList<Product>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cart_checkout, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}