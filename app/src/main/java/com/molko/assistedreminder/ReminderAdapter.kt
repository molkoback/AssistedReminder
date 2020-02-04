package com.molko.assistedreminder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.list_view_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(context: Context, private val list: List<Reminder>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    
    override fun getView(i: Int, view: View?, parent: ViewGroup?): View {
        val row = inflater.inflate(R.layout.list_view_item, parent, false)
        row.itemMessage.text = list[i].message
        if (list[i].time != null) {
            val sdf = SimpleDateFormat("HH:mm - dd.MM.yyyy")
            sdf.timeZone = TimeZone.getDefault()
            row.itemTrigger.text = sdf.format(list[i].time)
        }
        else {
            row.itemTrigger.text = "location"
        }
        return row
    }
    
    override fun getItem(i: Int): Any {
        return list[i]
    }
    
    override fun getItemId(i: Int): Long {
        return i.toLong()
    }
    
    override fun getCount(): Int {
        return list.size
    }
}
