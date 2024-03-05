package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.UserConfig
import java.util.*

class UserAccountsRecyclerAdapter(val userListener: UserAccountsListener, var recyclerItems: ArrayList<UserConfig>)
    : RecyclerView.Adapter<UserAccountsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview_useraccount, parent, false)
        return ViewHolder(v, userListener)
    }

    override fun onBindViewHolder(holder: UserAccountsRecyclerAdapter.ViewHolder, position: Int) {
        holder.bind(recyclerItems[position], position)
    }

    override fun getItemCount(): Int {
        return recyclerItems.size
    }

    fun clear() {
        recyclerItems.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, val userListener: UserAccountsListener) :
        RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

        private val TAG = "UserAccountsRecyclerAdapter\$ViewHolder"
        private var selectedPosition: Int = 0

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(user: UserConfig, position: Int) {

            this.selectedPosition = position

            itemView.setTag(user)

            Log.d(TAG, "binding user account: " + user)

            val textView = itemView.findViewById(R.id.userEmail) as TextView
            textView.text = user.email
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            var user = itemView.getTag() as UserConfig
            var id = user.id.toBigDecimal().toInt()

            Log.d("onCreateContextMenu", "user: " + user + ", id: " + id)

            menu!!.setHeaderTitle(R.string.menu_header_user_options)

            menu!!.add(0, id, 0, "Set Password")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    userListener.showSetPasswordDialog(user)
                    true
                })

            menu!!.add(0, id, 0, "Set Role")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    userListener.showRoleDialog(user)
                    true
                })

            menu!!.add(0, id, 0, "Delete")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    userListener.deleteUser(user, selectedPosition)
                    true
                })
        }
    }
}
