package ru.alzhanov.drinkkollect

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button

class DialogAddPost(context: Activity) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.new_post_dialog_layout)

        val buttonDone = findViewById<Button>(R.id.button_new_post_done)
        buttonDone.setOnClickListener {
            dismiss()
        }
    }
}