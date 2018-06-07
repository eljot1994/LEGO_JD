package com.example.master.bricklist

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class CustomAdapter(var activity: Activity, var items: ArrayList<Part>) : BaseAdapter(){

    var dbHandler = MyDBHandler(activity,null,null,1)

    class ViewHolder(row: View?){
        var linearLayout: LinearLayout ?= null
        var imageView: ImageView? = null
        var textView: TextView? = null
        var store: TextView? = null
        var button_plus: Button? = null
        var button_minus: Button? = null


        init{
            linearLayout = row?.findViewById(R.id.linear)
            imageView = row?.findViewById(R.id.imageView)
            textView = row?.findViewById(R.id.textView)
            store = row?.findViewById(R.id.store)
            button_plus = row?.findViewById(R.id.button_plus)
            button_minus = row?.findViewById(R.id.button_minus)
        }
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val viewHolder: ViewHolder

        if(convertView == null){
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.row_item, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        }else{
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        val part = items[position]
        if(part.image != null){
            val bitmap = BitmapFactory.decodeByteArray(part.image, 0, part.image!!.size)
            viewHolder.imageView?.setImageBitmap(bitmap)

        }
        viewHolder.textView?.text = part.name
        viewHolder.store?.text = part.quantityInStore.toString()+ '/' + part.quantityInSet.toString()
        viewHolder.linearLayout?.setBackgroundColor(Color.TRANSPARENT)

        if(part.quantityInStore == part.quantityInSet){
            viewHolder.linearLayout?.setBackgroundColor(Color.argb(50,51,204,51))
            part.pozycja = 0
            items.sortBy { it.pozycja }
            this?.notifyDataSetChanged()
        }

        viewHolder.button_plus?.setOnClickListener{
            if(part.quantityInStore < part.quantityInSet!!) {
                part.quantityInStore += 1
                dbHandler.setUpdate(items[position].inventoryID!!, items[position].partID!!, items[position].colorID!!, part.quantityInStore)
                viewHolder.store?.text = part.quantityInStore.toString() + '/' + part.quantityInSet.toString()
            }
            if(part.quantityInStore == part.quantityInSet){
                viewHolder.linearLayout?.setBackgroundColor(Color.argb(50,51,204,51))
                part.pozycja = 0
                items.sortBy { it.pozycja }
                this?.notifyDataSetChanged()


            }
        }
        viewHolder.button_minus?.setOnClickListener {
            if(part.quantityInStore > 0){
                part.quantityInStore -= 1
                dbHandler.setUpdate(items[position].inventoryID!!, items[position].partID!!, items[position].colorID!!, part.quantityInStore)

                viewHolder.store?.text = part.quantityInStore.toString() + '/' + part.quantityInSet.toString()
            }
            if(part.quantityInStore != part.quantityInSet){
                viewHolder.linearLayout?.setBackgroundColor(Color.TRANSPARENT)
                part.pozycja = -1
                items.sortBy { it.pozycja }
                this?.notifyDataSetChanged()
            }
        }
        items.sortBy { it.pozycja }
        return view as View
    }

    override fun getItem(position: Int): Any {
       return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

}