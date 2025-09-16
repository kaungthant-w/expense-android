package com.hsu.expense

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*

/**
 * Bluetooth Printer Manager for connecting to Bluetooth receipt printers
 * Supports ESC/POS commands for thermal printers
 */
class BluetoothPrinterManager(private val context: Context) {
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false
    
    // UUID for Serial Port Profile (SPP)
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    // ESC/POS Commands
    companion object {
        private const val ESC = 0x1B.toByte()
        private const val GS = 0x1D.toByte()
        private const val LF = 0x0A.toByte()
        private const val CR = 0x0D.toByte()
        
        // Text formatting commands
        val INITIALIZE = byteArrayOf(ESC, '@'.code.toByte())
        val ALIGN_LEFT = byteArrayOf(ESC, 'a'.code.toByte(), 0)
        val ALIGN_CENTER = byteArrayOf(ESC, 'a'.code.toByte(), 1)
        val ALIGN_RIGHT = byteArrayOf(ESC, 'a'.code.toByte(), 2)
        val BOLD_ON = byteArrayOf(ESC, 'E'.code.toByte(), 1)
        val BOLD_OFF = byteArrayOf(ESC, 'E'.code.toByte(), 0)
        val UNDERLINE_ON = byteArrayOf(ESC, '-'.code.toByte(), 1)
        val UNDERLINE_OFF = byteArrayOf(ESC, '-'.code.toByte(), 0)
        val DOUBLE_HEIGHT_ON = byteArrayOf(ESC, '!'.code.toByte(), 0x10)
        val DOUBLE_WIDTH_ON = byteArrayOf(ESC, '!'.code.toByte(), 0x20)
        val DOUBLE_SIZE_ON = byteArrayOf(ESC, '!'.code.toByte(), 0x30)
        val NORMAL_SIZE = byteArrayOf(ESC, '!'.code.toByte(), 0)
        val CUT_PAPER = byteArrayOf(GS, 'V'.code.toByte(), 66, 3)
        val FEED_LINES = byteArrayOf(ESC, 'd'.code.toByte(), 3)
        
        // Font sizes
        val FONT_SMALL = byteArrayOf(ESC, '!'.code.toByte(), 0x00)
        val FONT_MEDIUM = byteArrayOf(ESC, '!'.code.toByte(), 0x01)
        val FONT_LARGE = byteArrayOf(ESC, '!'.code.toByte(), 0x11)
    }
    
    interface BluetoothPrinterCallback {
        fun onDeviceFound(device: BluetoothDevice, deviceName: String)
        fun onDiscoveryFinished()
        fun onConnectionSuccess()
        fun onConnectionFailed(error: String)
        fun onPrintSuccess()
        fun onPrintFailed(error: String)
        fun onDisconnected()
    }
    
    private var callback: BluetoothPrinterCallback? = null
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    
    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }
    
    fun setCallback(callback: BluetoothPrinterCallback) {
        this.callback = callback
    }
    
    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }
    
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasRequiredPermissions()) return emptyList()
        
        return try {
            bluetoothAdapter?.bondedDevices?.filter { device ->
                // Filter for printer-like devices
                val deviceName = device.name?.lowercase() ?: ""
                deviceName.contains("printer") || 
                deviceName.contains("pos") || 
                deviceName.contains("receipt") ||
                deviceName.contains("thermal") ||
                deviceName.contains("esc") ||
                deviceName.contains("zebra") ||
                deviceName.contains("epson") ||
                deviceName.contains("star")
            } ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }
    
    fun startDeviceDiscovery() {
        if (!hasRequiredPermissions()) return
        
        discoveredDevices.clear()
        
        // Register receiver for discovery
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(discoveryReceiver, filter)
        
        try {
            bluetoothAdapter?.startDiscovery()
        } catch (e: SecurityException) {
            callback?.onConnectionFailed("Permission denied for Bluetooth discovery")
        }
    }
    
    fun stopDeviceDiscovery() {
        if (!hasRequiredPermissions()) return
        
        try {
            bluetoothAdapter?.cancelDiscovery()
            context.unregisterReceiver(discoveryReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
    
    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    
                    device?.let { bluetoothDevice ->
                        if (!discoveredDevices.contains(bluetoothDevice)) {
                            val deviceName = try {
                                bluetoothDevice.name ?: "Unknown Device"
                            } catch (e: SecurityException) {
                                "Unknown Device"
                            }
                            
                            // Filter for printer-like devices
                            val name = deviceName.lowercase()
                            if (name.contains("printer") || name.contains("pos") || 
                                name.contains("receipt") || name.contains("thermal") || 
                                name.contains("esc") || name.contains("zebra") || 
                                name.contains("epson") || name.contains("star")) {
                                discoveredDevices.add(bluetoothDevice)
                                callback?.onDeviceFound(bluetoothDevice, deviceName)
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    callback?.onDiscoveryFinished()
                    try {
                        context.unregisterReceiver(this)
                    } catch (e: Exception) {
                        // Receiver might already be unregistered
                    }
                }
            }
        }
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        if (!hasRequiredPermissions()) {
            callback?.onConnectionFailed("Bluetooth permissions not granted")
            return
        }
        
        Thread {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                isConnected = true
                
                callback?.onConnectionSuccess()
            } catch (e: IOException) {
                isConnected = false
                callback?.onConnectionFailed("Failed to connect: ${e.message}")
                try {
                    bluetoothSocket?.close()
                } catch (closeException: IOException) {
                    // Ignore
                }
            } catch (e: SecurityException) {
                callback?.onConnectionFailed("Permission denied for Bluetooth connection")
            }
        }.start()
    }
    
    fun disconnect() {
        try {
            isConnected = false
            outputStream?.close()
            bluetoothSocket?.close()
            callback?.onDisconnected()
        } catch (e: IOException) {
            // Ignore
        }
    }
    
    fun isConnected(): Boolean = isConnected
    
    fun printText(text: String) {
        if (!isConnected || outputStream == null) {
            callback?.onPrintFailed("Printer not connected")
            return
        }
        
        Thread {
            try {
                outputStream?.write(text.toByteArray())
                outputStream?.flush()
                callback?.onPrintSuccess()
            } catch (e: IOException) {
                callback?.onPrintFailed("Failed to print: ${e.message}")
            }
        }.start()
    }
    
    fun printCommand(command: ByteArray) {
        if (!isConnected || outputStream == null) {
            callback?.onPrintFailed("Printer not connected")
            return
        }
        
        Thread {
            try {
                outputStream?.write(command)
                outputStream?.flush()
            } catch (e: IOException) {
                callback?.onPrintFailed("Failed to send command: ${e.message}")
            }
        }.start()
    }
      fun printReceipt(
        title: String,
        expenses: List<ExpenseItem>,
        totalAmount: Double,
        currency: String,
        paperWidth: Int = 48 // mm
    ) {
        if (!isConnected || outputStream == null) {
            callback?.onPrintFailed("Printer not connected")
            return
        }
        
        Thread {
            try {
                val output = buildReceiptContent(title, expenses, totalAmount, currency, paperWidth)
                outputStream?.write(output)
                outputStream?.flush()
                callback?.onPrintSuccess()
            } catch (e: IOException) {
                callback?.onPrintFailed("Failed to print receipt: ${e.message}")
            }
        }.start()
    }
      private fun buildReceiptContent(
        title: String,
        expenses: List<ExpenseItem>,
        totalAmount: Double,
        currency: String,
        paperWidth: Int
    ): ByteArray {
        val output = mutableListOf<Byte>()
        
        // Initialize printer
        output.addAll(INITIALIZE.toList())
        
        // Print title
        output.addAll(ALIGN_CENTER.toList())
        output.addAll(BOLD_ON.toList())
        output.addAll(DOUBLE_SIZE_ON.toList())
        output.addAll(title.toByteArray().toList())
        output.add(LF)
        output.add(LF)
        output.addAll(NORMAL_SIZE.toList())
        output.addAll(BOLD_OFF.toList())
        
        // Print date
        output.addAll(ALIGN_CENTER.toList())
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        output.addAll(currentDate.toByteArray().toList())
        output.add(LF)
        output.add(LF)
        
        // Print separator line
        output.addAll(ALIGN_LEFT.toList())
        val separatorLength = when (paperWidth) {
            48 -> 32
            58 -> 38
            80 -> 48
            112 -> 64
            else -> 32
        }
        output.addAll("-".repeat(separatorLength).toByteArray().toList())
        output.add(LF)
        
        // Print header
        output.addAll(BOLD_ON.toList())
        val headerFormat = when (paperWidth) {
            48 -> "%-3s %-12s %7s"
            58 -> "%-3s %-15s %9s"
            80 -> "%-3s %-20s %12s"
            112 -> "%-3s %-30s %18s"
            else -> "%-3s %-12s %7s"
        }
        val header = String.format(headerFormat, "No", "Item", "Amount")
        output.addAll(header.toByteArray().toList())
        output.add(LF)
        output.addAll(BOLD_OFF.toList())
        output.addAll("-".repeat(separatorLength).toByteArray().toList())
        output.add(LF)
          // Print expenses
        expenses.forEachIndexed { index, expense ->
            val amount = "${expense.price} $currency"
            val expenseFormat = when (paperWidth) {
                48 -> "%-3d %-12s %7s"
                58 -> "%-3d %-15s %9s"
                80 -> "%-3d %-20s %12s"
                112 -> "%-3d %-30s %18s"
                else -> "%-3d %-12s %7s"
            }
            
            val name = if (expense.name.length > (when (paperWidth) {
                48 -> 12
                58 -> 15
                80 -> 20
                112 -> 30
                else -> 12
            })) {
                expense.name.substring(0, when (paperWidth) {
                    48 -> 9
                    58 -> 12
                    80 -> 17
                    112 -> 27
                    else -> 9
                }) + "..."
            } else {
                expense.name
            }
            
            val line = String.format(expenseFormat, index + 1, name, amount)
            output.addAll(line.toByteArray().toList())
            output.add(LF)
        }
        
        // Print total
        output.addAll("-".repeat(separatorLength).toByteArray().toList())
        output.add(LF)
        output.addAll(BOLD_ON.toList())
        output.addAll(DOUBLE_WIDTH_ON.toList())
        val totalFormat = when (paperWidth) {
            48 -> "%16s %7s"
            58 -> "%19s %9s"
            80 -> "%24s %12s"
            112 -> "%34s %18s"
            else -> "%16s %7s"
        }
        val total = String.format(totalFormat, "TOTAL:", "$totalAmount $currency")
        output.addAll(total.toByteArray().toList())
        output.add(LF)
        output.addAll(NORMAL_SIZE.toList())
        output.addAll(BOLD_OFF.toList())
        
        // Print footer
        output.add(LF)
        output.addAll(ALIGN_CENTER.toList())
        output.addAll("Thank you!".toByteArray().toList())
        output.add(LF)
        output.add(LF)
        
        // Feed and cut paper
        output.addAll(FEED_LINES.toList())
        output.addAll(CUT_PAPER.toList())
        
        return output.toByteArray()
    }
}
