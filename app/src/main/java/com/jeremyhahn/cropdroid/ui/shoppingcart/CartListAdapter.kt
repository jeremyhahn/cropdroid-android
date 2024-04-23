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

class CartListAdapter(private val cart: CartViewModel) :
    RecyclerView.Adapter<CartListAdapter.CartListViewHolder>() {

    class CartListViewHolder(val binding: CardviewProductBindingImpl) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply { -> itemView
                binding.setVariable(BR.product, product)
                binding.executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartListAdapter.CartListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: CardviewProductBindingImpl = DataBindingUtil.inflate(
            layoutInflater, R.layout.cardview_product, parent, false)
        return CartListViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartListViewHolder, position: Int) {
        var products = cart.getProducts()
        var currentItem: Product = products[position]
        holder.bind(currentItem)
        holder.binding.btnIncrementQuantity.setOnClickListener {
            currentItem.quantity++
            cart.set(currentItem)
            notifyDataSetChanged()
        }
        holder.binding.btnDecrementQuantity.setOnClickListener {
            if(currentItem.quantity == 0) return@setOnClickListener
            currentItem.quantity--
            cart.remove(currentItem)
            notifyDataSetChanged()
        }
//        val productQuantityEditText = holder.binding.productQuantity as EditText
//        productQuantityEditText.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus) {
//                currentItem.quantity = productQuantityEditText.text.toString().toInt()
//                cart.set(currentItem)
//                notifyDataSetChanged()
//            }
//        })
    }

    override fun getItemCount(): Int {
        return cart.items.value!!.size
    }
}