package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.BR
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.databinding.CardviewProductBindingImpl

class CartAdapter(private val shoppingCart : List<Product>) :
    RecyclerView.Adapter<CartAdapter.ProductListViewHolder>() {

    class ProductListViewHolder(val binding: CardviewProductBindingImpl) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply { -> itemView
                binding.setVariable(BR.product, product)
                binding.executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.ProductListViewHolder {
        //val v = LayoutInflater.from(parent.context).inflate(R.layout.fragment_product_list, parent, false)
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: CardviewProductBindingImpl = DataBindingUtil.inflate(
            layoutInflater, R.layout.cardview_product, parent, false)
        return ProductListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        var currentItem: Product = shoppingCart[position]
        holder.bind(currentItem)
        holder.binding.btnIncrementQuantity.setOnClickListener {
            val newQuantity: Int = currentItem.quantity.toString().toInt() + 1
            currentItem.quantity = newQuantity
            notifyDataSetChanged()
            //notifyItemChanged(position)
        }
        holder.binding.btnDecrementQuantity.setOnClickListener {
            val newQuantity: Int = currentItem.quantity.toString().toInt() - 1
            if(newQuantity < 0) return@setOnClickListener
            currentItem.quantity = newQuantity
            //notifyItemChanged(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return shoppingCart.size
    }
}