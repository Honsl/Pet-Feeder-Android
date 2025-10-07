package com.honsl.petfeeder

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.RequiresPermission
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import com.honsl.petfeeder.Schedule
import java.util.UUID
import kotlin.collections.forEach

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddFeederFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddFeederFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_feeder, container, false)
    }
    val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BLE_GATT", "Connected to GATT server")
                    gatt.discoverServices()
                    activity?.runOnUiThread {
                        view?.findViewById<ConstraintLayout>(R.id.ble_connect_error)?.visibility = View.INVISIBLE
                        view?.findViewById<ConstraintLayout>(R.id.ble_connect_wifi)?.visibility = View.VISIBLE

                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BLE_GATT", "Disconnected from GATT server")
                    activity?.runOnUiThread {
                        view?.findViewById<ConstraintLayout>(R.id.ble_connect_error)?.visibility = View.VISIBLE
                        view?.findViewById<ConstraintLayout>(R.id.ble_connect_wifi)?.visibility = View.INVISIBLE
                    }
                    gatt.close()
                }

            }
        }
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val responseData = characteristic.value
            val responseText = responseData.toString(Charsets.UTF_8)

            Log.d("BLE_GATT", "Response from ESP32: $responseText")

            // Get the feeder name from UI
            val name: String? = view?.findViewById<EditText>(R.id.editTextFeederName)?.text?.toString()

            // Proceed only if we have a valid name and an IP address was returned
            if (!name.isNullOrBlank() && !responseText.contains("Error")) {
                val feeder = Feeder(name, "OK",responseText, levelLeft = 0f, levelRight = 0f, schedule = mutableListOf<Schedule>())
                val jsonManager = JsonManager(requireContext())
                val result = jsonManager.saveToJSON(feeder)

                if (result) {
                    Log.d("JSON_UPDATE", "Feeder saved successfully: $feeder")
                } else {
                    Log.e("JSON_UPDATE", "Failed to save feeder data")
                }
            } else {
                Log.e("BLE_GATT", "Feeder name is null or blank")
            }

            // If you want to update the UI, do it on the main thread
            activity?.runOnUiThread {
                parentFragmentManager.popBackStack();
            }
        }
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE_GATT", "Services discovered successfully")
                val service = gatt.getService(UUID.fromString("dfb4e5ce-76f5-40b5-8cdc-d0096e2ef6be"))
                val characteristic = service?.getCharacteristic(UUID.fromString("f4de9c04-97ed-4d88-8c10-ef418642b6df"))

// Enable local notifications
                gatt.setCharacteristicNotification(characteristic, true)

// Set up the descriptor (CCCD - Client Characteristic Configuration Descriptor)
                val descriptor = characteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            } else {
                Log.e("BLE_GATT", "Service discovery failed with status $status")
            }
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bluetoothManager: BluetoothManager =
            requireContext().getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString("dfb4e5ce-76f5-40b5-8cdc-d0096e2ef6be")))
            // .setServiceUuid(ParcelUuid(UUID.fromString("dfb4e5ce-76f5-40b5-8cdc-d0096e2ef6be")))
            .build()



        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        val filtered2 = pairedDevices?.filter { device ->
            device.name == "Pet Feeder"
        }
        Log.d("Paired BLE","$filtered2")
        var gatt : BluetoothGatt?=null
        if (filtered2?.isNotEmpty() == true){
            gatt =  filtered2.first().connectGatt(requireContext(),false,gattCallback);
        }



        view.findViewById<Button>(R.id.button_setup_wifi)
            .setOnClickListener { Log.d("BUTTONS", "sent WIFI information" )

                val service = gatt?.getService(UUID.fromString("dfb4e5ce-76f5-40b5-8cdc-d0096e2ef6be"))
                val characteristic = service?.getCharacteristic(UUID.fromString("f4de9c04-97ed-4d88-8c10-ef418642b6df"))

                val ssid:String = view.findViewById<EditText>(R.id.editTextSSID).text.toString();
                val password:String = view.findViewById<EditText>(R.id.editTextPassword).text.toString();

                val jsonPayload = """{"ssid":"$ssid","password":"$password"}"""
                val data = jsonPayload.toByteArray(Charsets.UTF_8)

// Set the value and write
                characteristic?.value = data
                val success = gatt?.writeCharacteristic(characteristic)

                Log.d("BLE_GATT", "Write initiated: $success")

            }

    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddFeederFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddFeederFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}