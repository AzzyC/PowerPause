package com.azzy.powerpause

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Reference UI elements
        val bypassSwitch = findViewById<SwitchMaterial>(R.id.switch_bypass_charge)
        val limitEdit = findViewById<EditText>(R.id.edit_battery_limit)
        val startButton = findViewById<Button>(R.id.button_start_service)

        // Grey out battery limit input if Bypass Charge Mode is ON
        bypassSwitch.setOnCheckedChangeListener { _, isChecked ->
            limitEdit.isEnabled = !isChecked
        }

        // Start the foreground service when button is clicked
        startButton.setOnClickListener {
            val intent = Intent(this, ChargeControlService::class.java)
            // Pass the current Bypass Charge Mode state
            intent.putExtra("bypass_mode", bypassSwitch.isChecked)
            // Pass the battery limit (0 if not set or invalid)
            intent.putExtra("battery_limit", limitEdit.text.toString().toIntOrNull() ?: 0)
            startForegroundService(intent)
        }
    }
}
