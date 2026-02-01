package com.example.remotetv

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var connectedDevice: BluetoothDevice? = null

    // UUID SPP (Serial Port Profile) - standar untuk komunikasi Bluetooth
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create UI programmatically
        createUI()

        // Check permissions
        checkPermissions()

        // Check Bluetooth
        if (bluetoothAdapter == null) {
            statusText.text = "Bluetooth tidak tersedia"
            statusText.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }
    }

    private fun createUI() {
        val layout = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        // Title
        val title = TextView(this).apply {
            text = "Remote TV Bluetooth"
            textSize = 24f
            setTextColor(resources.getColor(android.R.color.black))
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(title)

        // Status
        statusText = TextView(this).apply {
            text = "Status: Tidak terkoneksi"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.holo_red_dark))
            gravity = android.view.Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }
        layout.addView(statusText)

        // Device Info
        deviceInfoText = TextView(this).apply {
            text = "Device: Belum terkoneksi"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(deviceInfoText)

        // Scan Button
        scanButton = Button(this).apply {
            text = "🔍 Scan Device"
            setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            setTextColor(resources.getColor(android.R.color.white))
            setPadding(30, 20, 30, 20)
            setOnClickListener {
                scanDevices()
            }
        }
        layout.addView(scanButton)

        // Connect Button
        connectButton = Button(this).apply {
            text = "Hubungkan ke B860H"
            setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
            setTextColor(resources.getColor(android.R.color.white))
            setPadding(30, 20, 30, 20)
            setOnClickListener {
                connectToDevice()
            }
        }
        layout.addView(connectButton)

        // Button container
        val buttonContainer = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(20, 40, 20, 20)
        }

        // OK Button
        okButton = createButton("OK", android.R.color.holo_green_light) {
            sendCommand("OK")
        }
        buttonContainer.addView(okButton)

        // Play/Pause Button
        playPauseButton = createButton("▶||", android.R.color.holo_blue_light) {
            sendCommand("PLAY_PAUSE")
        }
        buttonContainer.addView(playPauseButton)

        // Both Button
        bothButton = createButton("OK+▶||", android.R.color.holo_orange_light) {
            sendCommand("OK_PLAY_PAUSE")
        }
        buttonContainer.addView(bothButton)

        layout.addView(buttonContainer)

        // Volume Button container
        val volContainer = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }

        // Volume Up
        volUpButton = createButton("VOL +", android.R.color.holo_purple) {
            sendCommand("VOL_UP")
        }
        volContainer.addView(volUpButton)

        // Volume Down
        volDownButton = createButton("VOL -", android.R.color.background_dark) {
            sendCommand("VOL_DOWN")
        }
        volContainer.addView(volDownButton)

        layout.addView(volContainer)

        // Info
        val info = TextView(this).apply {
            text = "Pastikan TV B860H sudah terpair\nGunakan Scan untuk cari device"
            textSize = 12f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            gravity = android.view.Gravity.CENTER
            setPadding(20, 40, 20, 20)
        }
        layout.addView(info)

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
            ).apply {
                setMargins(10, 10, 10, 10)
            }
            setOnClickListener {
                onClick()
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scanDevices() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Aktifkan Bluetooth dulu", Toast.LENGTH_SHORT).show()
            return
        }

        statusText.text = "Scanning devices..."
        statusText.setTextColor(resources.getColor(android.R.color.holo_blue_dark))

        // Get paired devices
        val pairedDevices = bluetoothAdapter!!.bondedDevices

        val tvDevices = pairedDevices.filter { 
            it.name.contains("B860H", ignoreCase = true) ||
            it.name.contains("TV", ignoreCase = true) ||
            it.name.contains("JQVITEK", ignoreCase = true)
        }

        if (tvDevices.isNotEmpty()) {
            connectedDevice = tvDevices.first()
            deviceInfoText.text = "Device ditemukan: ${connectedDevice?.name}"
            statusText.text = "Device ditemukan! Klik Hubungkan"
            statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            
            connectButton.text = "Hubungkan ke ${connectedDevice?.name}"
            connectButton.isEnabled = true
        } else {
            statusText.text = "TV B860H tidak ditemukan"
            statusText.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            
            // Show all paired devices
            val allDevices = pairedDevices.joinToString("\n") { "${it.name} - ${it.address}" }
            deviceInfoText.text = "Paired devices:\n$allDevices"
            
            Toast.makeText(this, "Pair B860H dulu di Bluetooth settings", Toast.LENGTH_LONG).show()
        }
    }

    private fun connectToDevice() {
        if (connectedDevice == null) {
            Toast.makeText(this, "Scan device dulu", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                bluetoothSocket = connectedDevice?.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream

                runOnUiThread {
                    statusText.text = "✓ Terkoneksi ke ${connectedDevice?.name}"
                    statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                    connectButton.text = "Terhubung"
                    connectButton.isEnabled = false
                    scanButton.isEnabled = false
                    
                    Toast.makeText(this, "Berhasil terkoneksi!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    statusText.text = "Gagal terkoneksi: ${e.message}"
                    statusText.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    Toast.makeText(this, "Koneksi gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun sendCommand(command: String) {
        if (outputStream == null) {
            Toast.makeText(this, "Belum terkoneksi ke TV", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                // Kirim command sebagai byte array
                val message = when(command) {
                    "OK" -> byteArrayOf(0x0D) // Enter key
                    "PLAY_PAUSE" -> byteArrayOf(0x20) // Space key
                    "OK_PLAY_PAUSE" -> byteArrayOf(0x0D, 0x20) // Enter + Space
                    "VOL_UP" -> byteArrayOf(0x00, 0x00, 0x40, 0x00) // Volume up command
                    "VOL_DOWN" -> byteArrayOf(0x00, 0x00, 0x80, 0x00) // Volume down command
                    else -> command.toByteArray()
                }
                
                outputStream?.write(message)
                outputStream?.flush()

                runOnUiThread {
                    val displayText = when(command) {
                        "OK_PLAY_PAUSE" -> "OK + Play/Pause terkirim!"
                        "PLAY_PAUSE" -> "Play/Pause terkirim!"
                        "VOL_UP" -> "Volume Up terkirim!"
                        "VOL_DOWN" -> "Volume Down terkirim!"
                        else -> "$command terkirim!"
                    }
                    Toast.makeText(this, displayText, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Gagal mengirim: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
