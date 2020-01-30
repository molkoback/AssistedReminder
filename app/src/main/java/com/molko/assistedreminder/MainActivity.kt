package com.molko.assistedreminder

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        var addActive = false
        fabAdd.setOnClickListener {
            if (!addActive) {
                addActive = true
                fabMap.animate().translationY(-1*resources.getDimension(R.dimen.add_animation))
                fabTime.animate().translationY(-2*resources.getDimension(R.dimen.add_animation))
            }
            else {
                addActive = false
                fabMap.animate().translationY(0f)
                fabTime.animate().translationY(0f)
            }
        }
        
        fabMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        fabTime.setOnClickListener {
            val intent = Intent(this, TimeActivity::class.java)
            startActivity(intent)
        }
    }
}
