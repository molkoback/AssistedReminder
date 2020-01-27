package com.molko.assistedreminder

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val fabTime: View = findViewById(R.id.fabTime)
        fabTime.setOnClickListener { view ->
            val intent = Intent(this, TimeActivity::class.java)
            startActivity(intent)
        }
        val fabMap: View = findViewById(R.id.fabMap)
        fabMap.setOnClickListener { view ->
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        val fabHelp: View = findViewById(R.id.fabHelp)
        fabHelp.setOnClickListener { view ->
        }
    }
}
