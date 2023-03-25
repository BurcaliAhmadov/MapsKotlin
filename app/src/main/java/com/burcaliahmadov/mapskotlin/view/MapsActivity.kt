package com.burcaliahmadov.mapskotlin.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.burcaliahmadov.mapskotlin.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.burcaliahmadov.mapskotlin.databinding.ActivityMapsBinding
import com.burcaliahmadov.mapskotlin.model.Place
import com.burcaliahmadov.mapskotlin.roomdb.PlaceDao
import com.burcaliahmadov.mapskotlin.roomdb.PlaceDatabase
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var permission :ActivityResultLauncher<String>
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener:LocationListener
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao
    var trackBoolean: Boolean?=null
    var selectedLatitude:Double?=null
    var selectedLongitude :Double?=null
    private val mDisposable = CompositeDisposable()
    var placeFromMain: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()
        selectedLatitude=0.00
        selectedLongitude=0.00
        binding.save.isEnabled=false
        sharedPreferences=this.getSharedPreferences("com.burcaliahmadov.mapskotlin", MODE_PRIVATE)
        trackBoolean=false
        db= Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Place").build()
        placeDao=db.placeDao()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this@MapsActivity)

        val intent=intent
        val info=intent.getStringExtra("info")
        if(info=="new"){
            binding.delete.visibility=View.GONE
            binding.save.visibility=View.VISIBLE

            trackBoolean=sharedPreferences.getBoolean("trackBoolean",false)
            locationManager=this.getSystemService(LOCATION_SERVICE) as LocationManager
            locationListener=object : LocationListener{
                override fun onLocationChanged(p0: Location) {
                    if(trackBoolean==false){
                        val userLoc=LatLng(p0.latitude,p0.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc,15f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                    }

                }

            }



            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.root,"Permission needed for location",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        //request Permission
                        permission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()

                }
                else {
                    //Request Permission
                    permission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else{
                //
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation =locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))

                }
                mMap.isMyLocationEnabled=true

            }
        }
        else{
            mMap.clear()
            placeFromMain=intent.getSerializableExtra("place") as Place
            placeFromMain?.let {
                val latlng=LatLng(it.pLatitude,it.pLongitude)
                mMap.addMarker(MarkerOptions().position(latlng).title(it.pName))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))
                binding.placeName.setText(it.pName)
                binding.save.visibility = View.GONE
                binding.delete.visibility = View.VISIBLE
            }



        }


    }



    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))
        selectedLatitude=p0.latitude
        selectedLongitude=p0.longitude
        binding.save.isEnabled=true
    }

    fun save(view:View){
        val place= Place(binding.placeName.text.toString(),selectedLatitude!!,selectedLongitude!!)
        mDisposable.add(placeDao.insertAll(place).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))

    }
    fun delete(view:View){
        placeFromMain?.let {
            mDisposable.add(placeDao.deleteAll(it).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))
        }
    }
    fun handleResponse(){
        val intent=Intent(this@MapsActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }



    fun registerLauncher(){
        permission=registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            if(result){
                if  (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation =locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))

                    }
                    mMap.isMyLocationEnabled=true


                }
                else{
                    Toast.makeText(this@MapsActivity, "Permisson needed!", Toast.LENGTH_LONG).show()
                }

            }
        }

    }

    override fun onStop() {
        super.onStop()
        mDisposable.clear()
    }


}