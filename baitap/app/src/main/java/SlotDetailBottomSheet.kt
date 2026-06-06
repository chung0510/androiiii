package com.example.timhieu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SlotDetailBottomSheet : BottomSheetDialogFragment() {

    private lateinit var slot: LockerSlot

    companion object {

        fun newInstance(
            slot: LockerSlot
        ): SlotDetailBottomSheet {

            return SlotDetailBottomSheet().apply {
                this.slot = slot
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //----------------------------------
        // EMPTY
        //----------------------------------

        if (slot.status == "EMPTY") {

            val view = inflater.inflate(
                R.layout.bottom_sheet_empty,
                container,
                false
            )

            val txtSlotCode =
                view.findViewById<TextView>(
                    R.id.txtSlotCode
                )

            txtSlotCode.text =
                "Ngăn ${slot.code}"

            return view
        }

        //----------------------------------
        // ERROR
        //----------------------------------

        if (slot.status == "ERROR") {

            val view = inflater.inflate(
                R.layout.bottom_sheet_error,
                container,
                false
            )

            val txtSlotCode =
                view.findViewById<TextView>(
                    R.id.txtSlotCode
                )

            txtSlotCode.text =
                "Ngăn ${slot.code}"

            return view
        }

        //----------------------------------
        // USED + WARNING
        //----------------------------------

        val view = inflater.inflate(
            R.layout.bottom_sheet_used,
            container,
            false
        )

        val txtSlotCode =
            view.findViewById<TextView>(
                R.id.txtSlotCode
            )

        val txtBadge =
            view.findViewById<TextView>(
                R.id.txtBadge
            )

        val txtCustomer =
            view.findViewById<TextView>(
                R.id.txtCustomer
            )

        val txtRemaining =
            view.findViewById<TextView>(
                R.id.txtRemaining
            )

        txtSlotCode.text =
            "Ngăn ${slot.code}"

        txtCustomer.text =
            slot.customerName

        txtRemaining.text =
            slot.remainingTime

        //----------------------------------
        // USED
        //----------------------------------

        if (slot.status == "USED") {

            txtBadge.text =
                "Đang sử dụng"

            txtBadge.setTextColor(
                0xFF295EA8.toInt()
            )
        }

        //----------------------------------
        // WARNING
        //----------------------------------

        else {

            txtBadge.text =
                "Sắp hết hạn"

            txtBadge.setTextColor(
                0xFFA36B00.toInt()
            )
        }

        return view
    }
}