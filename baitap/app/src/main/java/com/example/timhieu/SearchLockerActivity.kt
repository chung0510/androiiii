    package com.example.timhieu

    import android.Manifest
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.location.Location
    import android.os.Bundle
    import android.view.View
    import android.widget.ArrayAdapter
    import android.widget.AutoCompleteTextView
    import android.widget.Button
    import android.widget.ProgressBar
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.app.ActivityCompat
    import com.example.timhieu.network.LocationRequest
    import com.example.timhieu.network.Locker
    import com.example.timhieu.network.RetrofitClient
    import com.google.android.gms.location.FusedLocationProviderClient
    import com.google.android.gms.location.LocationServices
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.SupportMapFragment
    import com.google.android.gms.maps.model.BitmapDescriptorFactory
    import com.google.android.gms.maps.model.LatLng
    import com.google.android.gms.maps.model.Marker
    import com.google.android.gms.maps.model.MarkerOptions
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response

    class SearchLockerActivity : AppCompatActivity(), OnMapReadyCallback {

        private lateinit var mMap: GoogleMap
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private lateinit var progressBar: ProgressBar
        private lateinit var lockerDropdown: AutoCompleteTextView

        private var lockerList: List<Locker> = emptyList()
        private var selectedLocker: Locker? = null

        private val markers = mutableMapOf<String, Marker>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_search_locker)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            progressBar = findViewById(R.id.progressBar)
            lockerDropdown = findViewById(R.id.spinnerLockerList)

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

            mapFragment.getMapAsync(this)

            findViewById<Button>(R.id.btnOrderNow).setOnClickListener {

                selectedLocker?.let { locker ->

                    if (locker.availableSlots <= 0) {
                        Toast.makeText(
                            this,
                            "Tủ này đã đầy, vui lòng chọn tủ khác",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val intent = Intent(this, RentLockerActivity::class.java)

                    intent.putExtra("LOCKER_ID", locker.lockerId)
                    intent.putExtra("AVAILABLE_SLOTS", locker.availableSlots)
                    intent.putExtra("LOCKER_ADDRESS", locker.address)

                    startActivity(intent)

                } ?: run {
                    Toast.makeText(
                        this,
                        "Vui lòng chọn một tủ trên bản đồ hoặc danh sách",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            loadNearbyLockers()
        }

        private fun loadNearbyLockers() {

            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )

                return
            }

            progressBar.visibility = View.VISIBLE

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->

                if (location != null) {

                    val request =
                        LocationRequest(location.latitude, location.longitude)

                    RetrofitClient.api.generateLockers(request)
                        .enqueue(object : Callback<List<Locker>> {

                            override fun onResponse(
                                call: Call<List<Locker>>,
                                response: Response<List<Locker>>
                            ) {

                                progressBar.visibility = View.GONE

                                if (response.isSuccessful) {
                                    lockerList = response.body() ?: emptyList()
                                    updateLockerUI()
                                } else {
                                    fetchLockers()
                                }
                            }

                            override fun onFailure(
                                call: Call<List<Locker>>,
                                t: Throwable
                            ) {
                                progressBar.visibility = View.GONE
                                fetchLockers()
                            }
                        })

                } else {

                    progressBar.visibility = View.GONE
                    fetchLockers()
                }
            }
        }

        private fun fetchLockers() {

            progressBar.visibility = View.VISIBLE

            RetrofitClient.api.getLockers()
                .enqueue(object : Callback<List<Locker>> {

                    override fun onResponse(
                        call: Call<List<Locker>>,
                        response: Response<List<Locker>>
                    ) {

                        progressBar.visibility = View.GONE

                        if (response.isSuccessful) {
                            lockerList = response.body() ?: emptyList()
                            updateLockerUI()
                        }
                    }

                    override fun onFailure(
                        call: Call<List<Locker>>,
                        t: Throwable
                    ) {
                        progressBar.visibility = View.GONE
                    }
                })
        }

        private fun updateLockerUI() {

            if (!::mMap.isInitialized) return

            mMap.clear()
            markers.clear()

            val lockerNames = lockerList.map { locker ->

                val statusText =
                    if (locker.availableSlots > 0)
                        " (Còn trống)"
                    else
                        " (Đã đầy)"

                "Tủ ${locker.address}$statusText"
            }

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                lockerNames
            )

            lockerDropdown.setAdapter(adapter)

            lockerDropdown.setOnItemClickListener { _, _, position, _ ->

                val locker = lockerList[position]

                selectedLocker = locker

                val marker = markers[locker.lockerId]

                marker?.showInfoWindow()

                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        marker?.position ?: LatLng(
                            locker.latitude,
                            locker.longitude
                        ),
                        17f
                    )
                )
            }

            for (locker in lockerList) {

                val pos = LatLng(
                    locker.latitude,
                    locker.longitude
                )

                val markerColor =
                    if (locker.availableSlots > 0)
                        BitmapDescriptorFactory.HUE_AZURE
                    else
                        BitmapDescriptorFactory.HUE_RED

                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("Tủ ${locker.address}")
                        .snippet("Còn: ${locker.availableSlots}/${locker.totalSlots}")
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                markerColor
                            )
                        )
                )

                if (marker != null) {
                    markers[locker.lockerId] = marker
                }
            }
        }

        override fun onMapReady(googleMap: GoogleMap) {

            mMap = googleMap

            mMap.setOnMarkerClickListener { marker ->

                val locker = lockerList.find {
                    marker.title == "Tủ ${it.address}"
                }

                locker?.let {

                    selectedLocker = it

                    val index = lockerList.indexOf(it)

                    if (index in lockerList.indices) {

                        val statusText =
                            if (it.availableSlots > 0)
                                " (Còn trống)"
                            else
                                " (Đã đầy)"

                        lockerDropdown.setText(
                            "Tủ ${it.address}$statusText",
                            false
                        )
                    }

                    marker.showInfoWindow()
                }

                true
            }

            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                mMap.isMyLocationEnabled = true

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->

                        location?.let {

                            val currentLatLng =
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                )

                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    15f
                                )
                            )
                        }
                    }
            }

            if (lockerList.isNotEmpty()) {
                updateLockerUI()
            }
        }
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            if (
                requestCode == 1001 &&
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                loadNearbyLockers()
            }
        }
    }