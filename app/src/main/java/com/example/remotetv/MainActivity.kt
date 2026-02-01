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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.KeyEvent

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
    private lateinit var playPauseButton: Button
    private lateinit var bothButton: Button
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
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
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
        val layout = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.VERTICAL
            setPadding(40, 40, 40, 40)
        }

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

        val buttonContainer = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(20, 40, 20, 20)
        }

        okButton = createButton("OK", android.R.color.holo_green_light) { sendKey(0x28) } // 0x28 is Enter
        buttonContainer.addView(okButton)

        playPauseButton = createButton("▶||", android.R.color.holo_blue_light) { sendConsumerKey(0x00CD) }
        buttonContainer.addView(playPauseButton)

        bothButton = createButton("OK+▶||", android.R.color.holo_orange_light) { sendCombo() }
        buttonContainer.addView(bothButton)

        layout.addView(buttonContainer)

        val volContainer = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }

        volUpButton = createButton("VOL +", android.R.color.holo_purple) { sendConsumerKey(0x00E9) }
        volContainer.addView(volUpButton)

        volDownButton = createButton("VOL -", android.R.color.background_dark) { sendConsumerKey(0x00EA) }
        volContainer.addView(volDownButton)

        layout.addView(volContainer)
        setContentView(layout)
    }

    private fun createButton(text: String, color: Int, onClick: () -> Unit): Button {
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
            setOnClickListener { onClick() }
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
            connectedDevice = device // Simpan target tapi belum tentu connect
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

    private fun sendKey(keyCode: Int) {
        // Send Key Down
        val report = ByteArray(8)
        report[2] = keyCode.toByte() // Key 1
        sendReport(1, report)
        
        // Wait briefly
        try { Thread.sleep(50) } catch (e: InterruptedException) { e.printStackTrace() }

        // Send Key Up
        sendReport(1, ByteArray(8))
    }

    private fun sendConsumerKey(usageCode: Int) {
        // Consumer Control Report Structure: 2 Bytes (Little Endian)
        val report = ByteArray(2)
        report[0] = (usageCode and 0xFF).toByte()
        report[1] = ((usageCode shr 8) and 0xFF).toByte()
        
        Log.d("HID", "Sending Consumer Key: $usageCode")
        val sent = sendReport(2, report) // Key Down
        
        // Wait briefly
        try { Thread.sleep(50) } catch (e: InterruptedException) { e.printStackTrace() }

        if (sent) sendReport(2, ByteArray(2)) // Key Up
    }

    private fun sendCombo() {
        // OK then Play/Pause
        Thread {
            sendKey(0x28) // Enter
            Thread.sleep(100)
            sendConsumerKey(0x00CD) // Play/Pause
        }.start()
    }

    private fun sendReport(id: Int, data: ByteArray): Boolean {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return false
        if (connectedDevice != null) {
            Log.d("HID", "Sending Report ID: $id, Data: ${data.joinToString { "%02x".format(it) }}")
            return hidDevice?.sendReport(connectedDevice, id, data) ?: false
        } else {
            runOnUiThread { Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show() }
            return false
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("HID", "Physical Key Down: $keyCode")
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                sendKey(0x28) // HID Enter
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                sendConsumerKey(0x00E9) // HID Vol+
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                sendConsumerKey(0x00EA) // HID Vol-
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                sendConsumerKey(0x00CD) // HID Play/Pause
                true
            }
            KeyEvent.KEYCODE_MUTE -> {
                sendConsumerKey(0x00E2) // HID Mute
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                super.onKeyDown(keyCode, event) // Biarkan tombol back default
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            permissions.add(android.Manifest.permission.BLUETOOTH)
            permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
        }
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
}
