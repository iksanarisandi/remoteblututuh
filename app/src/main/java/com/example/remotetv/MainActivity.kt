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

import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContracts

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
    private lateinit var scanButton: Button
    private lateinit var okButton: Button
    private lateinit var volUpButton: Button
    private lateinit var volDownButton: Button

    private val PERMISSION_REQUEST_CODE = 100

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Toast.makeText(this, "Sending: $text", Toast.LENGTH_SHORT).show()
                sendString(text)
            }
        }
    }

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
        0x26.toByte(), 0xFF.toByte(), 0x00.toByte(), //   Logical Maximum (255)
        0x05.toByte(), 0x07.toByte(),       //   Usage Page (Key Codes)
        0x19.toByte(), 0x00.toByte(),       //   Usage Minimum (0)
        0x2A.toByte(), 0xFF.toByte(), 0x00.toByte(), //   Usage Maximum (255)
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

        // Connection Buttons
        val connRow = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 10, 0, 10)
        }
        scanButton = Button(this).apply {
            text = "🔍 Scan"
            setOnClickListener { scanDevices() }
        }
        connRow.addView(scanButton)
        
        connectButton = Button(this).apply {
            text = "Connect"
            setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
            setTextColor(resources.getColor(android.R.color.white))
            setOnClickListener { connectToDevice() }
        }
        connRow.addView(connectButton)
        layout.addView(connRow)

        val advRow = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 10, 0, 20)
        }
        val resetHidButton = Button(this).apply {
            text = "Reset HID"
            setOnClickListener { 
                statusText.text = "Resetting HID..."
                registerHidDevice() 
            }
        }
        advRow.addView(resetHidButton)

        val discoverButton = Button(this).apply {
            text = "Discoverable"
            setOnClickListener { makeDiscoverable() }
        }
        advRow.addView(discoverButton)
        layout.addView(advRow)

        // --- Feature Row: Power | YouTube | Voice ---
        val featureRow = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }
        
        // Power Button (0x0030)
        val btnPower = createStyledButton("POWER", android.R.color.holo_red_dark)
        setupRepeaterButton(btnPower, { sendConsumerKeyDown(0x0030) }, { sendConsumerKeyUp() })
        featureRow.addView(btnPower)

        // YouTube Button (0x0221 Search / AL)
        val btnYoutube = createStyledButton("YouTube", android.R.color.holo_red_light)
        // Using Search (0x0221) as requested alternative
        btnYoutube.setOnClickListener { 
            sendConsumerKeyDown(0x0221)
            Handler(Looper.getMainLooper()).postDelayed({ sendConsumerKeyUp() }, 100)
        }
        featureRow.addView(btnYoutube)

        // Voice Button
        val btnVoice = createStyledButton("🎤 Auto", android.R.color.holo_blue_light)
        btnVoice.setOnClickListener { startVoiceRecognition(100) }
        featureRow.addView(btnVoice)

        // Direct Voice Button (Input Only)
        val btnDirectVoice = createStyledButton("🎤 Input", android.R.color.holo_green_dark)
        btnDirectVoice.setOnClickListener { startVoiceRecognition(101) }
        featureRow.addView(btnDirectVoice)

        layout.addView(featureRow)

        // --- D-Pad ---
        val dpadTitle = TextView(this).apply {
            text = "Navigation"
            textSize = 16f
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(dpadTitle)

        // Up
        val rowUp = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
        }
        val btnUp = createStyledButton("↑", android.R.color.holo_blue_dark)
        setupRepeaterButton(btnUp, { sendKeyDown(0x52) }, { sendKeyUp() })
        rowUp.addView(btnUp)
        layout.addView(rowUp)

        // Left - OK - Right
        val rowMid = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
        }
        val btnLeft = createStyledButton("←", android.R.color.holo_blue_dark)
        setupRepeaterButton(btnLeft, { sendKeyDown(0x50) }, { sendKeyUp() })
        rowMid.addView(btnLeft)

        okButton = createStyledButton("OK", android.R.color.holo_green_light)
        setupRepeaterButton(okButton, { sendKeyDown(0x28) }, { sendKeyUp() })
        rowMid.addView(okButton)

        val btnRight = createStyledButton("→", android.R.color.holo_blue_dark)
        setupRepeaterButton(btnRight, { sendKeyDown(0x4F) }, { sendKeyUp() })
        rowMid.addView(btnRight)
        layout.addView(rowMid)

        // Down
        val rowDown = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
        }
        val btnDown = createStyledButton("↓", android.R.color.holo_blue_dark)
        setupRepeaterButton(btnDown, { sendKeyDown(0x51) }, { sendKeyUp() })
        rowDown.addView(btnDown)
        layout.addView(rowDown)

        // --- Volume ---
        val volContainer = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }
        volDownButton = createStyledButton("VOL -", android.R.color.background_dark)
        setupRepeaterButton(volDownButton, { sendConsumerKeyDown(0x00EA) }, { sendConsumerKeyUp() })
        volContainer.addView(volDownButton)

        volUpButton = createStyledButton("VOL +", android.R.color.holo_purple)
        setupRepeaterButton(volUpButton, { sendConsumerKeyDown(0x00E9) }, { sendConsumerKeyUp() })
        volContainer.addView(volUpButton)
        layout.addView(volContainer)

        setContentView(scrollView)
    }

    private fun showKeyboardInput() {
        val input = android.widget.EditText(this)
        input.hint = "Ketik teks untuk dikirim..."
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Keyboard Remote")
            .setView(input)
            .setPositiveButton("Kirim") { _, _ ->
                val text = input.text.toString()
                if (text.isNotEmpty()) {
                    sendDirectInput(text)
                }
            }
            .setNegativeButton("Batal", null)
            .create()
            
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
        input.requestFocus()
    }

    private fun startVoiceRecognition(requestCode: Int) {
        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Bicara sekarang...")
        }
        try {
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            statusText.text = "Voice Error: ${e.message}"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val text = result?.get(0)
            if (!text.isNullOrEmpty()) {
                sendVoiceSearch(text)
            }
        }
    }

    private fun sendVoiceSearch(text: String) {
        Thread {
            // 1. Trigger Search Menu
            runOnUiThread { statusText.text = "Membuka Pencarian..." }
            sendConsumerKeyDown(0x0221) // AC Search
            try { Thread.sleep(100) } catch (e: Exception) {}
            sendConsumerKeyUp()
            
            // Tunggu UI Search muncul (Voice UI biasanya butuh waktu)
            try { Thread.sleep(2000) } catch (e: Exception) {}

            // 2. Kirim tombol RIGHT untuk keluar dari mode Mic
            // Banyak Android TV membuka search dalam mode "Listening". 
            // Tekan kanan untuk fokus ke text box.
            runOnUiThread { statusText.text = "Fokus ke text box..." }
            sendKeyDown(0x4F) // Right Arrow
            try { Thread.sleep(200) } catch (e: Exception) {}
            sendKeyUp()
            try { Thread.sleep(200) } catch (e: Exception) {}

            // Tambahan: Kirim ENTER untuk memastikan masuk mode ketik (jika perlu)
            sendKeyDown(0x28) // Enter
            try { Thread.sleep(100) } catch (e: Exception) {}
            sendKeyUp()
            try { Thread.sleep(500) } catch (e: Exception) {}

            // 3. Ketik Teks
            runOnUiThread { statusText.text = "Mengetik: $text" }
            text.forEach { char ->
                val keycode = charToHid(char)
                if (keycode != 0) {
                    val shift = if (char.isUpperCase() || "!@#$%^&*()_+{}|:\"<>?~".contains(char)) 0x02 else 0x00
                    sendModifierKeyDown(shift, keycode)
                    try { Thread.sleep(50) } catch (e: Exception) {} // Increased delay
                    sendKeyUp()
                    try { Thread.sleep(50) } catch (e: Exception) {}
                }
            }
            
            // 4. Kirim Enter untuk eksekusi pencarian
            try { Thread.sleep(500) } catch (e: Exception) {}
            runOnUiThread { statusText.text = "Mengirim Enter..." }
            sendKeyDown(0x28) // Keyboard Enter
            try { Thread.sleep(100) } catch (e: Exception) {}
            sendKeyUp()
            
            runOnUiThread { statusText.text = "Selesai: $text" }
        }.start()
    }

    private fun sendString(text: String) {
        Thread {
            text.forEach { char ->
                val keycode = charToHid(char)
                if (keycode != 0) {
                    val shift = if (char.isUpperCase() || "!@#$%^&*()_+{}|:\"<>?~".contains(char)) 0x02 else 0x00
                    sendModifierKeyDown(shift, keycode)
                    try { Thread.sleep(20) } catch (e: Exception) {}
                    sendKeyUp()
                    try { Thread.sleep(20) } catch (e: Exception) {}
                }
            }
        }.start()
    }

    private fun charToHid(c: Char): Int {
        val char = c.lowercaseChar()
        return when (char) {
            'a' -> 0x04; 'b' -> 0x05; 'c' -> 0x06; 'd' -> 0x07; 'e' -> 0x08
            'f' -> 0x09; 'g' -> 0x0A; 'h' -> 0x0B; 'i' -> 0x0C; 'j' -> 0x0D
            'k' -> 0x0E; 'l' -> 0x0F; 'm' -> 0x10; 'n' -> 0x11; 'o' -> 0x12
            'p' -> 0x13; 'q' -> 0x14; 'r' -> 0x15; 's' -> 0x16; 't' -> 0x17
            'u' -> 0x18; 'v' -> 0x19; 'w' -> 0x1A; 'x' -> 0x1B; 'y' -> 0x1C; 'z' -> 0x1D
            '1' -> 0x1E; '2' -> 0x1F; '3' -> 0x20; '4' -> 0x21; '5' -> 0x22
            '6' -> 0x23; '7' -> 0x24; '8' -> 0x25; '9' -> 0x26; '0' -> 0x27
            ' ' -> 0x2C; '\n' -> 0x28; '-' -> 0x2D; '=' -> 0x2E; '[' -> 0x2F
            ']' -> 0x30; '\\' -> 0x31; ';' -> 0x33; '\'' -> 0x34; '`' -> 0x35
            ',' -> 0x36; '.' -> 0x37; '/' -> 0x38
            else -> 0
        }
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
    private fun sendModifierKeyDown(modifier: Int, keyCode: Int) {
        val report = ByteArray(8)
        report[0] = modifier.toByte() // Modifier
        report[2] = keyCode.toByte() // Key 1
        sendReport(1, report)
    }

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
