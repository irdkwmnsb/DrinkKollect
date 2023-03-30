package ru.alzhanov.drinkkollect

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomListViewAdapter(private val context: Activity, private val valuesList: ArrayList<String>) :
    ArrayAdapter<String>(context, R.layout.main_scroll_item_layout, valuesList) {

    override fun getView(idx: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val itemView = inflater.inflate(R.layout.main_scroll_item_layout, null, true)

        val viewText = itemView.findViewById(R.id.main_scroll_item_text) as TextView

        viewText.text = valuesList[idx]

        return itemView
    }
}