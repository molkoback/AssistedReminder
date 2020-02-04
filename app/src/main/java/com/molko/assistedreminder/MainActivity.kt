package com.molko.assistedreminder

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

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
    
    override fun onResume() {
        super.onResume()
        refreshList()
    }
    
    private fun refreshList() {
        doAsync {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "reminders"
            ).build()
            val reminders = db.reminderDao().getReminders()
            db.close()

            uiThread {
                if (reminders.isNotEmpty()) {
                    list.adapter = ReminderAdapter(applicationContext, reminders)
                }
                else {
                    toast("No reminders")
                }
            }
        }
    }
}
