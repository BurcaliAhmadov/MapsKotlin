package com.burcaliahmadov.mapskotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.burcaliahmadov.mapskotlin.R
import com.burcaliahmadov.mapskotlin.adapter.PlaceAdapter
import com.burcaliahmadov.mapskotlin.databinding.ActivityMainBinding
import com.burcaliahmadov.mapskotlin.model.Place
import com.burcaliahmadov.mapskotlin.roomdb.PlaceDao
import com.burcaliahmadov.mapskotlin.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private lateinit var db : PlaceDatabase
    private lateinit var placeDao: PlaceDao
    private val mDisposable= CompositeDisposable()
    private lateinit var  binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        val view:View=binding.root
        setContentView(view)
        db= Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Place").build()
        placeDao=db.placeDao()
        mDisposable.add(placeDao.getAll().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.add_menu,menu)
        return super.onCreateOptionsMenu(menu)

    }
    private fun handleResponse(list:List<Place>){
        binding.recyeclerView.layoutManager=LinearLayoutManager(this)
        val placeAdapter:PlaceAdapter= PlaceAdapter(list)
        binding.recyeclerView.adapter=placeAdapter

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId== R.id.add_id){
            val intent= Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        mDisposable.clear()
    }


}