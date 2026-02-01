package com.example.remotetv

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    // UI Components
    private lateinit var statusText: TextView
    private lateinit var deviceInfoText: TextView
    private lateinit var connectButton: Button
    private lateinit var okButton: Button
    // Removed combo-related buttons
    private lateinit var volUpButton: Button
    private lateinit var volDownButton: Button
    private lateinit var scanButton: Button

    private val PERMISSION_REQUEST_CODE = 100

    // HID Report Map for Keyboard and Consumer Control
    private val REPORT_MAP = byteArrayOf(
        0x05.toByte(), 0x01.toByte(),       // Usage Page (Generic Desktop)
        0x09.toByte(), 0x06.toByte(),       // Usage (Keyboard)
        0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
        0x85.toByte(), 0x01.toByte(),       //   Report ID (1)
        0x05.toByte(), 0x07.toByte(),       //   Usage Page (Key Codes)
        0x19.toByte(), 0xE0.toByte(),       //   Usage Minimum (224)
        0x29.toByte(), 0xE7.toByte(),       //   Usage Maximum (231)
        0x15.toByte(), 0x00.toByte(),       //   Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(),       //   Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(),       //   Report Size (1)
        0x95.toByte(), 0x08.toByte(),       //   Report Count (8)
        0x81.toByte(), 0x02.toByte(),       //   Input (Data, Variable, Absolute) - Modifier byte
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x75.toByte(), 0x08.toByte(),       //   Report Size (8)
        0x81.toByte(), 0x01.toByte(),       //   Input (Constant) - Reserved byte
        0x95.toByte(), 0x05.toByte(),       //   Report Count (5)
        0x75.toByte(), 0x01.toByte(),       //   Report Size (1)
        0x05.toByte(), 0x08.toByte(),       //   Usage Page (LEDs)
        0x19.toByte(), 0x01.toByte(),       //   Usage Minimum (1)
        0x29.toByte(), 0x05.toByte(),       //   Usage Maximum (5)
        0x91.toByte(), 0x02.toByte(),       //   Output (Data, Variable, Absolute) - LEDs
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x75.toByte(), 0x03.toByte(),       //   Report Size (3)
        0x91.toByte(), 0x01.toByte(),       //   Output (Constant) - Padding
        0x95.toByte(), 0x06.toByte(),       //   Report Count (6)
        0x75.toByte(), 0x08.toByte(),       //   Report Size (8)
        0x15.toByte(), 0x00.toByte(),       //   Logical Minimum (0)
        0x25.toByte(), 0x65.toByte(),       //   Logical Maximum (101)
        0x05.toByte(), 0x07.toByte(),       //   Usage Page (Key Codes)
        0x19.toByte(), 0x00.toByte(),       //   Usage Minimum (0)
        0x29.toByte(), 0x65.toByte(),       //   Usage Maximum (101)
        0x81.toByte(), 0x00.toByte(),       //   Input (Data, Array) - Key arrays (6 bytes)
        0xC0.toByte(),                      // End Collection

        0x05.toByte(), 0x0C.toByte(),       // Usage Page (Consumer Devices)
        0x09.toByte(), 0x01.toByte(),       // Usage (Consumer Control)
        0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
        0x85.toByte(), 0x02.toByte(),       //   Report ID (2)
        0x15.toByte(), 0x00.toByte(),       //   Logical Minimum (0)
        0x26.toByte(), 0xFF.toByte(), 0x03.toByte(), //   Logical Maximum (1023)
        0x19.toByte(), 0x00.toByte(),       //   Usage Minimum (0)
        0x2A.toByte(), 0xFF.toByte(), 0x03.toByte(), //   Usage Maximum (1023)
        0x75.toByte(), 0x10.toByte(),       //   Report Size (16)
        0x95.toByte(), 0x04.toByte(),       //   Report Count (4) - INCREASED to 4 to be safe
        0x81.toByte(), 0x00.toByte(),       //   Input (Data, Array, Absolute)
        0xC0.toByte()                       // End Collection
    )

    private val hidDeviceCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d("HID", "App Status Changed: registered=$registered")
            runOnUiThread {
                if (registered) {
                    statusText.text = "HID Registered. Ready to Connect."
                } else {
                    statusText.text = "HID Registration Failed."
                }
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d("HID", "Connection State: $state")
            runOnUiThread {
                when (state) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        connectedDevice = device
                        statusText.text = "✓ Terkoneksi ke ${device?.name}"
                        statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                        connectButton.text = "Terhubung"
                        connectButton.isEnabled = false
                        scanButton.isEnabled = false
                        Toast.makeText(this@MainActivity, "Connected to ${device?.name}", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        connectedDevice = null
                        statusText.text = "Terputus"
                        statusText.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        connectButton.isEnabled = true
                        scanButton.isEnabled = true
                    }
                }
            }
        }
    }

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerHidDevice()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        checkPermissions()
        
        if (bluetoothAdapter != null) {
            bluetoothAdapter.getProfileProxy(this, serviceListener, BluetoothProfile.HID_DEVICE)
        } else {
            statusText.text = "Bluetooth tidak tersedia"
        }
    }

    private fun registerHidDevice() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Unregister first to clear zombie states
        Log.d("HID", "Unregistering app before registration...")
        hidDevice?.unregisterApp()

        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Remote TV",
            "Android Remote",
            "Android",
            BluetoothHidDevice.SUBCLASS1_KEYBOARD,
            REPORT_MAP
        )

        val qosSettings = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800,
            9,
            0,
            11250,
            BluetoothHidDeviceAppQosSettings.MAX
        )

        // Delay registration to allow unregister to complete
        mainHandler.postDelayed({
            Log.d("HID", "Registering app...")
            val result = hidDevice?.registerApp(sdpSettings, null, qosSettings, executor, hidDeviceCallback)
            Log.d("HID", "RegisterApp call result: $result")
            
            if (result == false) {
                runOnUiThread { statusText.text = "Gagal memanggil registerApp (Internal Error)" }
            }
        }, 500)
    }

    private fun createUI() {
        val scrollView = androidx.core.widget.NestedScrollView(this).apply {
            layoutParams = androidx.appcompat.widget.LinearLayoutCompat.LayoutParams(
                androidx.appcompat.widget.LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                androidx.appcompat.widget.LinearLayoutCompat.LayoutParams.MATCH_PARENT
            )
        }

        val layout = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.VERTICAL
            setPadding(40, 40, 40, 40)
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }
        scrollView.addView(layout)

        val title = TextView(this).apply {
            text = "Remote TV HID"
            textSize = 24f
            setTextColor(resources.getColor(android.R.color.black))
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(title)

        statusText = TextView(this).apply {
            text = "Status: Menunggu HID Service..."
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.holo_red_dark))
            gravity = android.view.Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }
        layout.addView(statusText)

        deviceInfoText = TextView(this).apply {
            text = "Device: Belum terkoneksi"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(deviceInfoText)

        scanButton = Button(this).apply {
            text = "🔍 Scan Device"
            setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            setTextColor(resources.getColor(android.R.color.white))
            setPadding(30, 20, 30, 20)
            setOnClickListener { scanDevices() }
        }
        layout.addView(scanButton)

        val resetHidButton = Button(this).apply {
            text = "🔄 Reset HID Service"
            setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark))
            setTextColor(resources.getColor(android.R.color.white))
            setPadding(30, 20, 30, 20)
            setOnClickListener { 
                statusText.text = "Resetting HID..."
                registerHidDevice() 
            }
        }
        layout.addView(resetHidButton)

        val discoverButton = Button(this).apply {
            text = "📡 Make Discoverable"
            setBackgroundColor(resources.getColor(android.R.color.holo_purple))
            setTextColor(resources.getColor(android.R.color.white))
            setPadding(30, 20, 30, 20)
            setOnClickListener { makeDiscoverable() }
        }
        layout.addView(discoverButton)

        connectButton = Button(this).apply {
            text = "Hubungkan ke B860H"
            setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
            setTextColor(resources.getColor(android.R.color.white))
            setPadding(30, 20, 30, 20)
            setOnClickListener { connectToDevice() }
        }
        layout.addView(connectButton)

        // D-Pad Section
        val dpadTitle = TextView(this).apply {
            text = "D-Pad Navigasi"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 30, 0, 10)
        }
        layout.addView(dpadTitle)

        // Row Up
        val rowUp = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 10, 0, 10)
        }
        val btnUp = createStyledButton("↑", android.R.color.holo_blue_dark)
        setupRepeaterButton(btnUp, { sendKeyDown(0x52) }, { sendKeyUp() }) // Keyboard Up Arrow
        rowUp.addView(btnUp)
        layout.addView(rowUp)

        // Row Middle (Left - OK - Right)
        val rowMid = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 10, 0, 10)
        }
        val btnLeft = createStyledButton("←", android.R.color.holo_blue_dark)
        setupRepeaterButton(btnLeft, { sendKeyDown(0x50) }, { sendKeyUp() }) // Keyboard Left Arrow
        rowMid.addView(btnLeft)

        okButton = createStyledButton("OK", android.R.color.holo_green_light)
        setupButton(okButton, { sendKeyDown(0x28) }, { sendKeyUp() }) // Keyboard Enter
        rowMid.addView(okButton)

        val btnRight = createStyledButton("→", android.R.color.holo_blue_dark)
        setupRepeaterButton(btnRight, { sendKeyDown(0x4F) }, { sendKeyUp() }) // Keyboard Right Arrow
        rowMid.addView(btnRight)
        layout.addView(rowMid)

        // Row Down
        val rowDown = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 10, 0, 10)
        }
        val btnDown = createStyledButton("↓", android.R.color.holo_blue_dark)
        setupRepeaterButton(btnDown, { sendKeyDown(0x51) }, { sendKeyUp() }) // Keyboard Down Arrow
        rowDown.addView(btnDown)
        layout.addView(rowDown)

        val volContainer = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }

        // Volume Buttons with Repeater
        volUpButton = createStyledButton("VOL +", android.R.color.holo_purple)
        setupRepeaterButton(volUpButton, { sendConsumerKeyDown(0x00E9) }, { sendConsumerKeyUp() })
        volContainer.addView(volUpButton)

        volDownButton = createStyledButton("VOL -", android.R.color.background_dark)
        setupRepeaterButton(volDownButton, { sendConsumerKeyDown(0x00EA) }, { sendConsumerKeyUp() })
        volContainer.addView(volDownButton)

        layout.addView(volContainer)

        // Settings Button (Consumer Menu - 0x0040)
        val settingsButton = createStyledButton("SET", android.R.color.holo_orange_dark)
        setupButton(settingsButton, { sendConsumerKeyDown(0x0040) }, { sendConsumerKeyUp() })
        layout.addView(settingsButton)

        setContentView(scrollView)
    }

    private fun createStyledButton(text: String, color: Int): Button {
        return Button(this).apply {
            this.text = text
            setBackgroundColor(resources.getColor(color))
            setTextColor(resources.getColor(android.R.color.white))
            textSize = 18f
            setPadding(30, 25, 30, 25)
            layoutParams = androidx.appcompat.widget.LinearLayoutCompat.LayoutParams(
                200,
                androidx.appcompat.widget.LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(10, 10, 10, 10) }
        }
    }

    private fun setupButton(button: Button, onDown: () -> Unit, onUp: () -> Unit) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    onDown()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    onUp()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRepeaterButton(button: Button, onDown: () -> Unit, onUp: () -> Unit) {
        val repeaterHandler = Handler(Looper.getMainLooper())
        val repeaterRunnable = object : Runnable {
            override fun run() {
                onDown() // Resend Down Report
                repeaterHandler.postDelayed(this, 100) // Repeat every 100ms
            }
        }

        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    // Start Repeating
                    onDown() // First press
                    repeaterHandler.postDelayed(repeaterRunnable, 200) // Start repeating after 200ms
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    // Stop Repeating
                    repeaterHandler.removeCallbacks(repeaterRunnable)
                    onUp()
                    true
                }
                else -> false
            }
        }
    }

    private fun makeDiscoverable() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
             Toast.makeText(this, "Izin Advertise tidak diberikan", Toast.LENGTH_SHORT).show()
             return
        }
        val discoverableIntent = android.content.Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivity(discoverableIntent)
    }

    private fun scanDevices() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
        
        val pairedDevices = bluetoothAdapter?.bondedDevices
        val tvDevices = pairedDevices?.filter { 
            it.name.contains("B860H", ignoreCase = true) || it.name.contains("TV", ignoreCase = true) 
        }

        if (!tvDevices.isNullOrEmpty()) {
            val device = tvDevices.first()
            deviceInfoText.text = "Target: ${device.name} (Siap connect)"
            connectedDevice = device 
            statusText.text = "Target dipilih. Klik Hubungkan."
        } else {
            statusText.text = "TV B860H tidak ditemukan di paired devices"
            Toast.makeText(this, "Pair dulu di settings", Toast.LENGTH_LONG).show()
        }
    }

    private fun connectToDevice() {
        if (connectedDevice == null) {
            scanDevices()
            if (connectedDevice == null) return
        }
        
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
        
        statusText.text = "Mencoba connect ke ${connectedDevice?.name}..."
        hidDevice?.connect(connectedDevice)
    }

    // Keyboard Helpers
    private fun sendKeyDown(keyCode: Int) {
        val report = ByteArray(8)
        report[2] = keyCode.toByte() // Key 1
        sendReport(1, report)
    }

    private fun sendKeyUp() {
        sendReport(1, ByteArray(8))
    }

    // Consumer Helpers
    private fun sendConsumerKeyDown(usageCode: Int) {
        val report = ByteArray(8) // Updated to 8 bytes (4 keys support)
        report[0] = (usageCode and 0xFF).toByte()
        report[1] = ((usageCode shr 8) and 0xFF).toByte()
        // Rest are 0x00
        sendReport(2, report)
    }

    private fun sendComboDownMixed(keyboardCode: Int, consumerCode: Int) {
        // 1. Send Keyboard Down
        val reportK = ByteArray(8)
        reportK[2] = keyboardCode.toByte()
        sendReport(1, reportK)

        // 2. Send Consumer Down
        val reportC = ByteArray(8)
        reportC[0] = (consumerCode and 0xFF).toByte()
        reportC[1] = ((consumerCode shr 8) and 0xFF).toByte()
        sendReport(2, reportC)
        
        Log.d("HID", "Sent Combo: Key($keyboardCode) + Cons($consumerCode)")
    }

    private fun sendComboDownConsumer(code1: Int, code2: Int) {
        val report = ByteArray(8)
        // Key 1
        report[0] = (code1 and 0xFF).toByte()
        report[1] = ((code1 shr 8) and 0xFF).toByte()
        // Key 2
        report[2] = (code2 and 0xFF).toByte()
        report[3] = ((code2 shr 8) and 0xFF).toByte()
        
        sendReport(2, report)
        Log.d("HID", "Sent Combo Consumer: $code1 + $code2")
    }

    private fun sendComboUp() {
        sendReport(1, ByteArray(8)) // Release Keyboard
        sendReport(2, ByteArray(8)) // Release Consumer
        Log.d("HID", "Released Combo")
    }

    private fun sendConsumerKeyUp() {
        sendReport(2, ByteArray(8)) // Clear all consumer keys
    }

    private fun sendReport(id: Int, data: ByteArray): Boolean {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return false
        if (connectedDevice != null) {
            // Log.d("HID", "Sending Report ID: $id") 
            return hidDevice?.sendReport(connectedDevice, id, data) ?: false
        } else {
            runOnUiThread { Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show() }
            return false
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADVERTISE
            )
            val missing = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missing.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST_CODE)
            }
        } else {
            val permissions = arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
            }
        }
    }
}
