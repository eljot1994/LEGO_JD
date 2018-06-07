package com.example.master.bricklist

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class Part {

    var id:Int? = null
    var inventoryID:Int? = null
    var itemType:String? = null
    var typeID:Int? = null
    var itemID:String? = null
    var color:Int? = null
    var colorID:Int? = null
    var quantityInSet:Int? = null
    var quantityInStore:Int = 0
    var extra:String? = null
    var name:String? = null
    var partID:Int? = null
    var designID:Int? = null
    var image:ByteArray ?= null
    var pozycja:Int = -1

    override fun toString(): String {
        return id.toString() + " "+ inventoryID.toString()+ " " + itemType + " "+ typeID.toString()+ " "+
                itemID + " " + color.toString() + " "+ colorID.toString()+ " " +
                quantityInSet.toString()+ " " + quantityInStore.toString() + " "+
                extra + " "+ name + " "+ partID.toString() + " " + designID.toString()
    }
}

class PartsActivity : AppCompatActivity() {

    val dbHandler = MyDBHandler(this,null,null,1)
    var id:Int? = null
    var name:String? = null
    var items = ArrayList<Part>()



    var adapter : CustomAdapter ?= null
    var listView : ListView ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parts_inventory)

        val extras = intent.extras ?: return
        id = extras.getInt("id")
        name = extras.getString("name")

        val pg = PartsGetter()

        pg.execute(this)

        adapter?.notifyDataSetChanged()
    }

    override fun finish() {
        dbHandler.dateUpdate(id!!)
        super.finish()
    }

    fun export(v: View){


        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.newDocument()
        val rootElement = doc.createElement("INVENTORY")

        for(part in items){
            if(part.quantityInStore != part.quantityInSet){

                val itemRoot = doc.createElement("ITEM")

                val itemType = doc.createElement("ITEMTYPE")
                itemType.appendChild(doc.createTextNode(part.itemType))
                val itemID = doc.createElement("ITEMID")
                itemID.appendChild(doc.createTextNode(part.itemID))
                val color = doc.createElement("COLOR")
                color.appendChild(doc.createTextNode(part.color.toString()))
                val qtyFilled = doc.createElement("QTYFILLED")

                qtyFilled.appendChild(doc.createTextNode((part.quantityInSet!! - part.quantityInStore).toString()))
                itemRoot.appendChild(itemType)
                itemRoot.appendChild(itemID)
                itemRoot.appendChild(color)
                itemRoot.appendChild(qtyFilled)

                rootElement.appendChild(itemRoot)
            }
        }

        doc.appendChild(rootElement)

        val transformer = TransformerFactory.newInstance().newTransformer()

        transformer.setOutputProperty(OutputKeys.INDENT,"yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            var path: File ?= null

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Gdzie chcesz zapisać plik?")
            builder.setPositiveButton("Karta SD"){dialog, which ->
                path = Environment.getExternalStorageDirectory()
                val outDir = File(path, "LEGO")
                Log.i("TEST",outDir.toString())
                outDir.mkdir()

                val file = File(outDir, name + id.toString() + ".xml")
                Log.i("TEST",file.toString())
                transformer.transform(DOMSource(doc), StreamResult(file))

                Toast.makeText(this,"Plik zapisano na karcie SD",Toast.LENGTH_SHORT).show()

            }
            builder.setNegativeButton("Pamięć telefonu"){dialog, which ->
                path = this.filesDir
                val outDir = File(path, "LEGO")
                Log.i("TEST",outDir.toString())
                outDir.mkdir()

                val file = File(outDir, name + id.toString() + ".xml")
                Log.i("TEST",file.toString())
                transformer.transform(DOMSource(doc), StreamResult(file))

                Toast.makeText(this,"Plik zapisano w pamięci telefonu",Toast.LENGTH_SHORT).show()

            }
            val dialog: AlertDialog = builder.create()
            dialog.show()

        }else{
            Toast.makeText(this,"Brak zgody na zapis pliku",Toast.LENGTH_SHORT).show()
        }
    }

    private inner class PartsGetter: AsyncTask<Activity, Int, String>()  {

        override fun doInBackground(vararg params: Activity?): String{

            items = dbHandler.getParts(id!!)

            runOnUiThread(Runnable {
                listView = findViewById(R.id.listView)
                items.sortBy { it.pozycja }
                adapter = CustomAdapter(params[0]!!, items)
                listView?.adapter = adapter
                adapter?.notifyDataSetChanged()



            })

            return "Sukces!"
        }
    }
}