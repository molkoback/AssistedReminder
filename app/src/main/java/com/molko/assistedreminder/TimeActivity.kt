package com.molko.assistedreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_time.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.GregorianCalendar

class TimeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time)
        
        timeCreate.setOnClickListener {
            val calendar = GregorianCalendar(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.currentHour,
                timePicker.currentMinute
            )
            val text = editTimeMessage.text.toString()
            if (text.isEmpty()) {
                toast("Please provide reminder text")
                return@setOnClickListener
            }
            val millis = calendar.timeInMillis
            if (millis <= System.currentTimeMillis()) {
                toast("Invalid time")
                return@setOnClickListener
            }
            
            val reminder = Reminder(
                uid = null,
                time = millis,
                location = null,
                message = text
            )
            doAsync {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "reminders"
                ).build()
                val uid = db.reminderDao().insert(reminder).toInt()
                reminder.uid = uid
                db.close()
                setAlarm(reminder)
                finish()
            }
        }
    }
    
    private fun setAlarm(reminder: Reminder) {
        val intent = Intent(this, ReminderReceiver::class.java)
            .putExtra("uid", reminder.uid)
            .putExtra("message", reminder.message)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_ONE_SHOT)
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setExact(AlarmManager.RTC, reminder.time!!, pendingIntent)
        runOnUiThread{toast("Reminder created")}
    }
}
