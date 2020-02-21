package com.molko.assistedreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import kotlin.random.Random

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
    
    companion object {
        val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
        var notificationID = 1567
        fun showNotification(context: Context, message: String) {
            val name = context.getString(R.string.app_name)
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(name)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = name
                }
                notificationManager.createNotificationChannel(channel)
            }
            val notification = notificationID + Random(notificationID).nextInt(1, 30)
            notificationManager.notify(notificationID, notificationBuilder.build())
        }
    }
}
