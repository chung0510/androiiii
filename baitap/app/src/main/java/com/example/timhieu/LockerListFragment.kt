package com.example.timhieu

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LockerListFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerLocker: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_locker_list,
            container,
            false
        )

        recyclerLocker =
            view.findViewById(R.id.recyclerLocker)

        recyclerLocker.layoutManager =
            LinearLayoutManager(requireContext())

        // MOCK DATA

        val lockerList = arrayListOf(
            arrayOf("HP-01", "AEON Hải Phòng", "Online"),
            arrayOf("HP-02", "Vincom Plaza", "Offline"),
            arrayOf("HP-03", "BigC Hải Phòng", "Online"),
            arrayOf("HP-04", "Lạch Tray", "Online"),
            arrayOf("HP-05", "Đại học Hàng Hải", "Offline"),
            arrayOf("HP-06", "AEON Mall", "Online"),
            arrayOf("HP-07", "Ga Hải Phòng", "Online"),
            arrayOf("HP-08", "Vinmec Hải Phòng", "Offline")
        )

        val adapter = LockerAdapter(lockerList)

        recyclerLocker.adapter = adapter

        return view
    }

    inner class LockerAdapter(
        private val lockerList: ArrayList<Array<String>>
    ) : RecyclerView.Adapter<LockerAdapter.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {

            val view = LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_locker,
                    parent,
                    false
                )

            return ViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {

            val locker = lockerList[position]

            holder.txtLockerCode.text = locker[0]

            holder.txtLocation.text = locker[1]

            holder.txtStatus.text = locker[2]

            if (locker[2] == "Online") {

                holder.txtStatus.setBackgroundResource(
                    R.drawable.bg_status_green
                )

                holder.txtStatus.setTextColor(
                    Color.parseColor("#355E12")
                )

            } else {

                holder.txtStatus.setBackgroundResource(
                    R.drawable.bg_status_red
                )

                holder.txtStatus.setTextColor(
                    Color.parseColor("#8E1B1B")
                )
            }

            holder.itemView.setOnClickListener {

                val bundle = Bundle()

                bundle.putString(
                    "lockerCode",
                    locker[0]
                )

                val fragment =
                    LockerDetailFragment()

                fragment.arguments = bundle

                requireActivity()
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        fragment
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }

        override fun getItemCount(): Int {
            return lockerList.size
        }

        inner class ViewHolder(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView) {

            val txtLockerCode: TextView =
                itemView.findViewById(
                    R.id.txtLockerCode
                )

            val txtLocation: TextView =
                itemView.findViewById(
                    R.id.txtLocation
                )

            val txtStatus: TextView =
                itemView.findViewById(
                    R.id.txtStatus
                )
        }
    }

    companion object {

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(
            param1: String,
            param2: String
        ) =
            LockerListFragment().apply {

                arguments = Bundle().apply {

                    putString(
                        ARG_PARAM1,
                        param1
                    )

                    putString(
                        ARG_PARAM2,
                        param2
                    )
                }
            }
    }
}