package com.example.timhieu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.timhieu.network.Locker

class LockerAdapter(context: Context, private val lockers: List<Locker>) :
    ArrayAdapter<Locker>(context, R.layout.item_locker, lockers) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_locker, parent, false)
        val locker = lockers[position]

        val tvAddress = view.findViewById<TextView>(R.id.tvLockerAddress)
        val tvId = view.findViewById<TextView>(R.id.tvLockerId)
        val tvStatus = view.findViewById<TextView>(R.id.tvLockerStatus)

        tvAddress.text = context.getString(R.string.locker_address_label, locker.address)
        tvId.text = context.getString(R.string.locker_id_small_label, locker.lockerId)
        tvStatus.text = context.getString(R.string.locker_status_label, locker.status)

        return view
    }
}