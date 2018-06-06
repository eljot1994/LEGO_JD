package com.example.master.bricklist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class MyDBHandler( var context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int):
        SQLiteOpenHelper(context,DATABASE_NAME, factory, DATABASE_VERSION){


    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "BrickList.db"
    }


    fun archive(inventoryID:Int){
        val db = writableDatabase
        db.execSQL("update Inventories set Active = 0" + " where id = " + inventoryID)
        db.close()
    }

    fun getDesignID(part:Part): Int{
        var designID = 0
        val query = "select code from codes where itemid=" + part.partID +
                " and colorid=" + part.colorID
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            designID = cursor.getString(0).toInt()
        cursor.close()
        db.close()
        return designID
    }

    fun getPartID(itemID:String): Int{
        var partID = 0
        val query = "select id from Parts where code = '" + itemID + "'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            partID = cursor.getString(0).toInt()
        cursor.close()
        db.close()
        return partID
    }

    fun getName(partID:Int):String{
        var name = ""
        val query = "select name from Parts where id = '" + partID + "'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            name = cursor.getString(0)
        cursor.close()
        db.close()
        return name
    }

    fun getColorID(color:Int): Int{
        var colorID = 0
        val query = "select id from Colors where code = '" + color + "'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            colorID = cursor.getString(0).toInt()
        cursor.close()
        db.close()
        return colorID
    }

    fun getTypeID(itemType:String): Int{
        var typeID = 0
        val query = "select id from ItemTypes where code = '" + itemType + "'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            typeID = cursor.getString(0).toInt()
        cursor.close()
        db.close()
        return typeID
    }

    fun getItemID(partID: Int): String{
        var itemID = ""
        val query = "select code from parts where id = " + partID
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            itemID = cursor.getString(0)
        cursor.close()
        db.close()
        return itemID
    }

    fun getType(typeID:Int): String{
        var type = ""
        val query = "select code from ItemTypes where id = " + typeID
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            type = cursor.getString(0)
        cursor.close()
        db.close()
        return type

    }

    fun insertDesignID(part:Part){
        val values = ContentValues()
        values.put("ItemID", part.partID)
        values.put("ColorID", part.colorID)
        values.put("Code", part.designID)
        val db = this.writableDatabase
        db.insert("Codes", null,values)
        db.close()
    }

    fun checkIfImageIsDownloaded(designID: Int):Boolean{
        val query = "select Image from Codes where code = " + designID
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            if(cursor.getBlob(0) == null){
                cursor.close()
                db.close()
                return false
            }else{
                cursor.close()
                db.close()
                return true
            }
        cursor.close()
        db.close()
        return false
    }

    fun getSizeOfCodes():Int{
        var size = 0
        val query = "select count(id) from codes"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            size = cursor.getInt(0)
        cursor.close()
        db.close()
        return size
    }

    fun getImage(designID:Int): ByteArray? {
        var image:ByteArray ?= null
        val query = "select Image from Codes where Code = " + designID
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst()) {
            image = cursor.getBlob(0)
        }
        cursor.close()
        db.close()
        return image
    }

    fun getIDforNewInventory():Int {
        var newID = 0
        val query = "select count(id) as ilosc from Inventories"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst())
            newID = cursor.getString(0).toInt()
        cursor.close()
        db.close()
        return newID
    }

    fun dateUpdate(id: Int){
        val db = writableDatabase
        val date = Date()
        db.execSQL("update Inventories set LastAccessed = " + date.time.toInt() + " where id = " + id)
        db.close()
    }

    fun setUpdate(inventoryID:Int, itemID:Int, colorID:Int, qua: Int){
        val db = writableDatabase
        db.execSQL("update InventoriesParts set QuantityInStore = " + qua + " where InventoryID = " + inventoryID+
        " and ColorID = " + colorID + " and ItemID = " + itemID)
        db.close()
    }

    fun updateImage(partID:Int, colorID:Int, image:ContentValues){
        val db = writableDatabase
        val selection = "ColorID = " + colorID + " and ItemID = " + partID
        db.update("CODES", image, selection, null)
        db.close()
    }

    fun addPart(part:Part){
        val values = ContentValues()
        values.put("InventoryID", part.inventoryID)
        values.put("TypeID", part.typeID)
        values.put("ItemID", part.partID)
        values.put("QuantityInSet", part.quantityInSet)
        values.put("QuantityInStore", part.quantityInStore)
        values.put("ColorID", part.colorID)
        val db = this.writableDatabase
        db.insert("InventoriesParts",null,values)
        db.close()
    }

    fun addInventory(inventory: Inventory){
        val values = ContentValues()
        values.put("Name",inventory.name)
        values.put("Active",inventory.active)
        values.put("LastAccessed", inventory.lastAccessed)
        val db = this.writableDatabase
        db.insert("Inventories",null,values)
        db.close()
    }

    fun getParts(inventoryID:Int): ArrayList<Part>{
        val list = ArrayList<Part>()
        val query = "SELECT * FROM InventoriesParts where InventoryID = " + inventoryID
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst()){
            val part = Part()
            part.inventoryID = cursor.getInt(1)
            part.typeID = cursor.getInt(2)
            part.partID = cursor.getInt(3)
            part.quantityInSet = cursor.getInt(4)
            part.quantityInStore = cursor.getInt(5)
            part.colorID = cursor.getInt(6)
            part.name = getName(part.partID!!)
            part.itemID = getItemID(part.partID!!)
            part.itemType = getType(part.typeID!!)
            part.designID = getDesignID(part)
            part.image = getImage(part.designID!!)
            list.add(part)
        }
        while(cursor.moveToNext()){
            val part = Part()
            part.inventoryID = cursor.getInt(1)
            part.typeID = cursor.getInt(2)
            part.partID = cursor.getInt(3)
            part.quantityInSet = cursor.getInt(4)
            part.quantityInStore = cursor.getInt(5)
            part.colorID = cursor.getInt(6)
            part.name = getName(part.partID!!)
            part.itemID = getItemID(part.partID!!)
            part.itemType = getType(part.typeID!!)
            part.designID = getDesignID(part)
            part.image = getImage(part.designID!!)
            list.add(part)
        }

        cursor.close()
        db.close()

        return list
    }

    fun checkForPart(itemID: String):Boolean{
        val query = "select * from Parts where code = '" + itemID + "'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst()){
            cursor.close()
            db.close()
            return true
        }
        cursor.close()
        db.close()
        return false
    }

    fun getInventories(sorted:Boolean): ArrayList<Inventory>{
        val list = ArrayList<Inventory>()
        var query = ""
        if(sorted){
            query = "SELECT * FROM INVENTORIES where Active = 1 order by LastAccessed desc"
        }else{
            query = "SELECT * FROM INVENTORIES where Active = 1"
        }

        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)

        if(cursor.moveToFirst()){
            val inventory = Inventory()
            inventory.id = cursor.getInt(0)
            inventory.name = cursor.getString(1)
            inventory.active = cursor.getInt(2)
            inventory.lastAccessed = cursor.getInt(3)
            list.add(inventory)
        }
        while(cursor.moveToNext()){
            val inventory = Inventory()
            inventory.id = cursor.getInt(0)
            inventory.name = cursor.getString(1)
            inventory.active = cursor.getInt(2)
            inventory.lastAccessed = cursor.getInt(3)
            list.add(inventory)
        }

        cursor.close()
        db.close()

        return list
    }


}