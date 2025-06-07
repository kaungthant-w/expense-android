package com.example.myapplication

import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for displaying Bluetooth devices in RecyclerView
 */
class BluetoothDeviceAdapter(
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {
    
    private val devices = mutableListOf<BluetoothDevice>()
    
    fun updateDevices(newDevices: List<BluetoothDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }
    
    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }
    
    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return DeviceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }
    
    override fun getItemCount(): Int = devices.size
    
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceIcon: ImageView = itemView.findViewById(R.id.imageViewDeviceIcon)
        private val deviceName: TextView = itemView.findViewById(R.id.textViewDeviceName)
        private val deviceAddress: TextView = itemView.findViewById(R.id.textViewDeviceAddress)
        private val deviceStatus: TextView = itemView.findViewById(R.id.textViewDeviceStatus)
        
        fun bind(device: BluetoothDevice) {
            try {
                // Check for permissions before accessing device properties
                if (ActivityCompat.checkSelfPermission(
                        itemView.context,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED) {
                    
                    val name = device.name ?: "Unknown Device"
                    deviceName.text = name
                    deviceAddress.text = device.address
                    
                    // Set device icon based on bond state
                    when (device.bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            deviceIcon.setImageResource(R.drawable.ic_bluetooth_connected)
                            deviceStatus.text = "Paired"
                            deviceStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                        }
                        BluetoothDevice.BOND_BONDING -> {
                            deviceIcon.setImageResource(R.drawable.ic_bluetooth_searching)
                            deviceStatus.text = "Pairing..."
                            deviceStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                        }
                        else -> {
                            deviceIcon.setImageResource(R.drawable.ic_bluetooth)
                            deviceStatus.text = "Available"
                            deviceStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                        }
                    }
                    
                    // Set printer icon for printer devices
                    val lowerName = name.lowercase()
                    if (lowerName.contains("printer") || lowerName.contains("pos") || 
                        lowerName.contains("receipt") || lowerName.contains("thermal")) {
                        deviceIcon.setImageResource(R.drawable.ic_printer)
                    }
                    
                } else {
                    deviceName.text = "Permission Required"
                    deviceAddress.text = "Enable Bluetooth permissions"
                    deviceStatus.text = "Unavailable"
                    deviceIcon.setImageResource(R.drawable.ic_bluetooth_disabled)
                }
                
            } catch (e: SecurityException) {
                deviceName.text = "Permission Denied"
                deviceAddress.text = "Bluetooth access restricted"
                deviceStatus.text = "Error"
                deviceIcon.setImageResource(R.drawable.ic_bluetooth_disabled)
            }
            
            itemView.setOnClickListener {
                onDeviceClick(device)
            }
        }
    }
}
