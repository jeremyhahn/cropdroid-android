package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.view.LayoutInflater
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.BR
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.databinding.CardviewProductBindingImpl
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Product

class ProductListAdapter(private var products : List<Product>, private val cart: CartViewModel) :
    RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder>() {

    class ProductListViewHolder(val binding: CardviewProductBindingImpl) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply { -> itemView
                binding.setVariable(BR.product, product)
                binding.executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListAdapter.ProductListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: CardviewProductBindingImpl = DataBindingUtil.inflate(
            layoutInflater, R.layout.cardview_product, parent, false)
        return ProductListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        var currentItem: Product = products[position]
        var itemInCart = cart.getProduct(currentItem.id)
        if(itemInCart != null) {
            currentItem.quantity = itemInCart.quantity
        }
        holder.bind(currentItem)
        holder.binding.btnIncrementQuantity.setOnClickListener {
            currentItem.quantity++
            notifyDataSetChanged()
            cart.set(currentItem)
        }
        holder.binding.btnDecrementQuantity.setOnClickListener {
            if(currentItem.quantity == 0) return@setOnClickListener
            currentItem.quantity--
            notifyDataSetChanged()
            cart.remove(currentItem)
        }
//        val productQuantityEditText = holder.binding.productQuantity as EditText
//        productQuantityEditText.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus) {
//                currentItem.quantity = productQuantityEditText.text.toString().toInt()
//                cart.set(currentItem)
//            }
//        })
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}