package com.example.master.bricklist

import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

class Inventory{

    var id:Int? = null
    var name:String? = null
    var active:Int? = null
    var lastAccessed:Int ? = null
    var parts:ArrayList<Part>? = null

    constructor(){}

    constructor(id:Int?, name:String?, active:Int?, lastAccessed:Int?){
        this.id = id
        this.name = name
        this.active = active
        this.lastAccessed = lastAccessed
    }

    constructor(name:String?, active:Int?, lastAccessed:Int){
        this.name = name
        this.active = active
        this.lastAccessed = lastAccessed
    }

}

class MainActivity : AppCompatActivity() {

    var list = ArrayList<Inventory>()
    var names = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null
    var dbHandler: MyDBHandler?=null
    var sorted = FALSE



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        copyDB()
        dbHandler = MyDBHandler(this,null,null,1)
        sort_button_last.setTextColor(Color.WHITE)
        sort_button_date.setTextColor(Color.GRAY)
        list = dbHandler?.getInventories(sorted)!!
        list.forEach {
            names.add(it.name!!)
        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,names)
        project_list_view.adapter = adapter

        project_list_view.onItemClickListener = AdapterView.OnItemClickListener{
            adapterView, view, position, id ->

            val i = Intent(this,PartsActivity::class.java)
            i.putExtra("id", list[position].id)
            i.putExtra("name", list[position].name)
            startActivity(i)
        }

        project_list_view.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Czy usunąć ten projekt?")
            builder.setPositiveButton("Tak"){dialog, which ->

                dbHandler?.archive(list[position].id!!)
                names.clear()
                list = dbHandler?.getInventories(sorted)!!
                list.forEach {
                    names.add(it.name!!)
                }
                adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,names)
                project_list_view.adapter = adapter

            }
            builder.setNegativeButton("Nie"){dialog,which ->}
            val dialog: AlertDialog = builder.create()
            dialog.show()
            true
        }

    }

    private fun copyDB() {

        val cw = ContextWrapper(applicationContext)
        val db_name = "BrickList.db"
        val db_path = cw.dataDir.absolutePath
        val outDir = File(db_path, "databases")
        outDir.mkdir()
        val file = File(db_path + "/databases/", db_name)
        if(!file.exists()){
            val input =applicationContext.getAssets().open("BrickList.db");
            val mOutput = FileOutputStream(file)
            val mBuffer = ByteArray(1024)
            var mLength = input.read(mBuffer)
            while (mLength > 0) {
                mOutput.write(mBuffer, 0, mLength)
                mLength = input.read(mBuffer)
            }
            mOutput.flush()
            mOutput.close()
            input.close()
        }

    }

    fun sortujOstatnio(v: View){
        sorted = TRUE
        sort_button_date.setTextColor(Color.WHITE)
        sort_button_last.setTextColor(Color.GRAY)
        list = dbHandler?.getInventories(sorted)!!
        names.clear()
        list.forEach {
            names.add(it.name!!)
        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,names)
        project_list_view.adapter = adapter

    }

    fun sortujData(v: View){
        sorted = FALSE
        sort_button_last.setTextColor(Color.WHITE)
        sort_button_date.setTextColor(Color.GRAY)
        list = dbHandler?.getInventories(sorted)!!
        names.clear()
        list.forEach {
            names.add(it.name!!)
        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,names)
        project_list_view.adapter = adapter

    }
    override fun onRestart() {
        super.onRestart()
        sorted = FALSE
        sort_button_last.setTextColor(Color.WHITE)
        sort_button_date.setTextColor(Color.GRAY)
        list = dbHandler?.getInventories(sorted)!!
        names.clear()
        list.forEach {
            names.add(it.name!!)
        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,names)
        project_list_view.adapter = adapter
    }

    fun newProject(v: View){
        val i = Intent(this,NewProjectActivity::class.java)
        i.putStringArrayListExtra("Nazwy",names)
        startActivity(i)
    }
}
