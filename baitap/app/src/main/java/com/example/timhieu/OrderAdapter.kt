package com.example.timhieu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.timhieu.network.Order

class OrderAdapter(
    context: Context,
    private val orders: List<Order>
) : ArrayAdapter<Order>(
    context,
    android.R.layout.simple_list_item_2,
    orders
) {

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view = convertView
            ?: LayoutInflater.from(context)
                .inflate(
                    android.R.layout.simple_list_item_2,
                    parent,
                    false
                )

        val order = orders[position]

        val text1 =
            view.findViewById<TextView>(
                android.R.id.text1
            )

        val text2 =
            view.findViewById<TextView>(
                android.R.id.text2
            )

        text1.text =
            order.address ?: "Không có địa chỉ"

        text2.text =
            "Mã tủ: ${order.lockerCode}"

        return view
    }
}