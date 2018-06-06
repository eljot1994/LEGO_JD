package com.example.master.bricklist

import android.app.AlertDialog
import android.app.Activity
import android.content.ContentValues
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_new_project.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList

fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

class NewProjectActivity : AppCompatActivity() {

    var prefix = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
    var postfix = ".xml"
    var names = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        names = intent.getStringArrayListExtra("Nazwy")
        setContentView(R.layout.activity_new_project)

    }

    fun getDataFromURL(v: View){

        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog,null)
        val message = dialogView.findViewById<TextView>(R.id.message)
        message.text = "Pobieranie..."
        builder.setView(dialogView)
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()

        create_button.isEnabled = false

        val xd = XmlDownloader()
        xd.execute()
        if (xd.get().toString() != "Sukces!" ) {
            dialog.cancel()
            create_button.isEnabled = true
        }
    }

    fun getImage(url: String, partID:Int, colorID:Int, dbHandler: MyDBHandler): Boolean{
        try{
            BufferedInputStream(URL(url).content as InputStream).use {
                val baf = ArrayList<Byte>()
                var current = 0
                while(true){
                    current = it.read()
                    if(current == -1)
                        break
                    baf.add(current.toByte())
                }
                val blob = baf.toByteArray()
                val blobValues = ContentValues()
                blobValues.put("Image", blob)
                dbHandler.updateImage(partID, colorID, blobValues)
                return true
            }
        }
        catch (e: Exception){
            return false
        }

    }

    fun loadData(){
        val filename = "downloaded.xml"
        val path = filesDir
        val inDir = File(path,"XML")


        if(inDir.exists()){

            val file = File(inDir, filename)
            if(file.exists()){

                val dbHandler = MyDBHandler(this,null,null,1)

                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

                xmlDoc.documentElement.normalize()

                val date = Date()
                val new_inventory = Inventory(editName.text.toString(),1, date.time.toInt())

                dbHandler.addInventory(new_inventory)

                val items: NodeList = xmlDoc.getElementsByTagName("ITEM")

                for (i in 0..items.length -1){

                    val itemNode: Node = items.item(i)

                    if(itemNode.getNodeType() == Node.ELEMENT_NODE){

                        val elem = itemNode as Element
                        val children = elem.childNodes

                        val part = Part()
                        var alternate:String? = null

                        for(j in 0..children.length - 1){
                            val node = children.item(j)
                            if(node is Element){
                                when(node.nodeName){
                                    "ITEMTYPE" -> part.itemType = node.textContent
                                    "ITEMID" -> part.itemID = node.textContent
                                    "QTY" -> part.quantityInSet = node.textContent.toInt()
                                    "COLOR" -> part.color = node.textContent.toInt()
                                    "EXTRA" -> part.extra = node.textContent
                                    "ALTERNATE" -> alternate = node.textContent
                                }
                            }
                        }
                        if(alternate.equals("N") && part.extra.equals("N") && dbHandler.checkForPart(part.itemID!!)){
                            part.inventoryID = dbHandler.getIDforNewInventory()
                            part.typeID = dbHandler.getTypeID(part.itemType!!)
                            part.colorID = dbHandler.getColorID(part.color!!)
                            part.partID = dbHandler.getPartID(part.itemID!!)
                            part.designID = dbHandler.getDesignID(part)

                            if(part.designID == 0){
                                part.designID = dbHandler.getSizeOfCodes() + 1
                                dbHandler.insertDesignID(part)
                            }

                            var url:String

                            if(!dbHandler.checkIfImageIsDownloaded(part.designID!!)){
                                url = "https://www.lego.com/service/bricks/5/2/" + part.designID
                                if(!getImage(url ,part.partID!!, part.colorID!!, dbHandler)){
                                    url = "http://img.bricklink.com/P/" + part.colorID + "/" + part.itemID + ".gif"
                                    if(!getImage(url ,part.partID!!, part.colorID!!, dbHandler)){
                                        url = "https://www.bricklink.com/PL/" + part.itemID + ".jpg"
                                        getImage(url ,part.partID!!, part.colorID!!,dbHandler)
                                    }else{
                                        url = "https://www.lego.com/service/bricks/5/2/300126"
                                        getImage(url ,part.partID!!, part.colorID!!, dbHandler)
                                    }
                                }
                            }
                            dbHandler.addPart(part);
                        }
                    }
                }
            }
        }
    }

    private inner class XmlDownloader: AsyncTask<String, Int, String>(){

        override fun doInBackground(vararg params: String?): String {

            try {
                prefix = prefix_edit.text.toString()
                postfix = postfix_edit.text.toString()
                if (editName.text.toString() in names){
                    return "Taka nazwa już istnieje"
                }

                val url = URL(prefix + editID.text + postfix)
                val connection = url.openConnection()
                connection.connect()
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/downloaded.xml")
                val data = ByteArray(1024)
                var count = 0
                count = isStream.read(data)
                while (count != -1) {
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
            }
            catch(e: MalformedURLException){

                return "Błąd z URL!"
            }
            catch (e: FileNotFoundException){

                return "Pliku nie znaleziono!"
            }
            catch (e: IOException){

                return "Wyjątek IO!"
            }

            loadData()
            finish()
            return "Sukces!"
        }

        override fun onPostExecute(result: String?) {
            toast(result.toString())
        }
    }
}
