package com.azzy.powerpause

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Switch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val limitSwitch = findViewById<Switch>(R.id.switch_indiscriminate)
        val limitEdit = findViewById<EditText>(R.id.edit_battery_limit)
        val startButton = findViewById<Button>(R.id.button_start_service)

        // Simple logic to grey out EditText when indiscriminate mode is on
        limitSwitch.setOnCheckedChangeListener { _, isChecked ->
            limitEdit.isEnabled = !isChecked
        }

        startButton.setOnClickListener {
            val intent = Intent(this, ChargeControlService::class.java)
            intent.putExtra("indiscriminate", limitSwitch.isChecked)
            intent.putExtra("limit", limitEdit.text.toString().toIntOrNull() ?: 0)
            startForegroundService(intent)
        }
    }
}
