package com.burcaliahmadov.mapskotlin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Place
    (
    @ColumnInfo(name="pName")
    var pName:String ,
    @ColumnInfo(name="pLatitude")
    var pLatitude:Double,
    @ColumnInfo(name="pLongitude")
    var pLongitude:Double
    ) :Serializable{
        @PrimaryKey(autoGenerate = true)
        var id=0


}