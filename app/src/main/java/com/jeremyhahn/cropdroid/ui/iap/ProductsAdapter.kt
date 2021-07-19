package com.jeremyhahn.cropdroid.ui.iap

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.DownloadImageTask
import kotlinx.android.synthetic.main.product_cardview.view.*

class ProductsAdapter(private val context: Context, private val list: MutableList<SkuDetails>?,
        private val onProductClicked: (SkuDetails) -> Unit) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    override fun getItemCount(): Int = list!!.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_cardview, parent, false)
        val viewHolder = ViewHolder(view, context)
        view.setOnClickListener { onProductClicked(list!![viewHolder.adapterPosition]) }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list!![position])
    }

    fun setData(skuDetails: MutableList<SkuDetails>?) {
        list!!.clear()
        if(skuDetails != null && skuDetails.size > 0) {
            list!!.addAll(skuDetails!!)
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {

        private val context: Context

        init {
            this.context = context
        }

        fun bind(skuDetails: SkuDetails) {

            Log.d("skuDetails", skuDetails.toString())

            itemView.setTag(skuDetails)
            itemView.productName.text = skuDetails.title
            itemView.productDescription.text = skuDetails.description
            itemView.productPrice.text = skuDetails.price
            itemView.productImage.setImageDrawable(context.getDrawable(R.drawable.ic_reservoir_controller))
            //DownloadImageTask(itemView.productImage).execute("https://www.cropdroid.com/uploads/1/1/8/6/118684507/background-images/1150223307.png")
        }
    }
}
