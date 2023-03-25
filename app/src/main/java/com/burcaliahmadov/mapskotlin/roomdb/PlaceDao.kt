package com.burcaliahmadov.mapskotlin.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.burcaliahmadov.mapskotlin.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaceDao {
@Query("SELECT * FROM place")
fun getAll(): Flowable<List<Place>>
@Insert
fun insertAll(place:Place):Completable
@Delete
fun deleteAll(place:Place):Completable

}