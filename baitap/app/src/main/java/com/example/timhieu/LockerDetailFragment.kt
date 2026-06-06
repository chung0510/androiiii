package com.example.timhieu

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class LockerDetailFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var lockerCode: String? = null

    private val slotList = ArrayList<LockerSlot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParam1 = it.getString(ARG_PARAM1)
            mParam2 = it.getString(ARG_PARAM2)
            lockerCode = it.getString("lockerCode")
            Log.d("LOCKER_CODE", lockerCode ?: "null")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_locker_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridLocker: GridLayout = view.findViewById(R.id.gridLocker)

        //----------------------------------
        // LOAD DATA THEO TỪNG TỦ
        //----------------------------------
        when (lockerCode) {
            "HP-01" -> loadHP01()
            "HP-02" -> loadHP02()
        }

        //----------------------------------
        // GENERATE BUTTONS
        //----------------------------------
        for (slot in slotList) {
            val button = MaterialButton(requireContext())
            button.text = slot.code
            button.isAllCaps = false
            button.textSize = 14f
            button.cornerRadius = (8 * resources.displayMetrics.density).toInt()
            button.setSingleLine(true)
            button.maxLines = 1
            button.insetTop = 0
            button.insetBottom = 0
            button.textAlignment = View.TEXT_ALIGNMENT_CENTER
            button.setPadding(0, 0, 0, 0)

            //----------------------------------
            // KÍCH THƯỚC BUTTON
            //----------------------------------
            val density = resources.displayMetrics.density
            val params = GridLayout.LayoutParams()
            params.width = (55 * density).toInt()
            params.height = (40 * density).toInt()
            params.setMargins(
                (2 * density).toInt(),
                (2 * density).toInt(),
                (2 * density).toInt(),
                (2 * density).toInt()
            )
            button.layoutParams = params

            //----------------------------------
            // STYLE THEO TRẠNG THÁI
            //----------------------------------
            when (slot.status) {
                "USED" -> {
                    button.backgroundTintList = ColorStateList.valueOf(0xFFE7F0FD.toInt())
                    button.strokeColor = ColorStateList.valueOf(0xFFB6D0FF.toInt())
                    button.strokeWidth = 2
                    button.setTextColor(0xFF295EA8.toInt())
                }
                "EMPTY" -> {
                    button.backgroundTintList = ColorStateList.valueOf(0xFFEAF5E9.toInt())
                    button.strokeColor = ColorStateList.valueOf(0xFFB7DDB4.toInt())
                    button.strokeWidth = 2
                    button.setTextColor(0xFF2E7D32.toInt())
                }
                "WARNING" -> {
                    button.backgroundTintList = ColorStateList.valueOf(0xFFFFF4DF.toInt())
                    button.strokeColor = ColorStateList.valueOf(0xFFFFD56B.toInt())
                    button.strokeWidth = 2
                    button.setTextColor(0xFFA36B00.toInt())
                }
                "ERROR" -> {
                    button.backgroundTintList = ColorStateList.valueOf(0xFFFFECEC.toInt())
                    button.strokeColor = ColorStateList.valueOf(0xFFFF8A8A.toInt())
                    button.strokeWidth = 2
                    button.setTextColor(0xFFB71C1C.toInt())
                }
            }

            //----------------------------------
            // CLICK
            //----------------------------------
            button.setOnClickListener {
                val sheet = SlotDetailBottomSheet.newInstance(slot)
                sheet.show(parentFragmentManager, "slot_detail")
            }

            //----------------------------------
            // THÊM VÀO GRID
            //----------------------------------
            gridLocker.addView(button)
        }
    }

    private fun loadHP01() {
        slotList.clear()
        slotList.add(LockerSlot("A01", "S", "USED", "Nguyễn Văn An", "48 phút"))
        slotList.add(LockerSlot("A02", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("A03", "S", "WARNING", "Trần Văn Bình", "12 phút"))
        slotList.add(LockerSlot("A04", "S", "ERROR", "", ""))
        slotList.add(LockerSlot("A05", "S", "USED", "Lê Thị Mai", "2 giờ"))
        slotList.add(LockerSlot("A06", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("A07", "S", "WARNING", "Nguyễn Văn Nam", "15 phút"))
        slotList.add(LockerSlot("A08", "S", "USED", "Phạm Văn Long", "3 giờ"))
        slotList.add(LockerSlot("A09", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("A10", "S", "ERROR", "", ""))
        slotList.add(LockerSlot("A11", "S", "USED", "Nguyễn Đức Anh", "5 giờ"))
        slotList.add(LockerSlot("A12", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("A13", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("A14", "S", "USED", "Hoàng Văn Thái", "1 giờ"))
        slotList.add(LockerSlot("A15", "S", "EMPTY", "", ""))
    }

    private fun loadHP02() {
        slotList.clear()
        slotList.add(LockerSlot("B01", "S", "USED", "Đặng Văn Hậu", "20 phút"))
        slotList.add(LockerSlot("B02", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("B03", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("B04", "S", "USED", "Ngô Gia Tự", "4 giờ"))
        slotList.add(LockerSlot("B05", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("B06", "S", "ERROR", "", ""))
        slotList.add(LockerSlot("B07", "S", "USED", "Trần Duy Hưng", "10 phút"))
        slotList.add(LockerSlot("B08", "S", "EMPTY", "", ""))
        slotList.add(LockerSlot("B09", "S", "WARNING", "Phan Bội Châu", "5 phút"))
        slotList.add(LockerSlot("B10", "S", "EMPTY", "", ""))
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LockerDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
