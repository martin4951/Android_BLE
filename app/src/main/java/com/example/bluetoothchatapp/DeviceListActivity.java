package com.example.bluetoothchatapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanRecord;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DeviceListActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    //private BluetoothAdapter bluetoothAdapter;
    private ListView listPairedDevices, listAvailableDevices;
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;
    private Handler mHandler;

    //Adapter for list items
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    //Bluetooth adapter used to returned all the available devices

    private BluetoothLeScanner btScanner;

    //Progress bar
    private ProgressBar progressScanDevices;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this;


        init();
    }

    private void init()
    {
        listPairedDevices = findViewById(R.id.list_paired_devices);
        listAvailableDevices = findViewById(R.id.list_available_devices);

        progressScanDevices = findViewById(R.id.progress_scan_devices);

        //Parameters: context and a layout file
        adapterPairedDevices = new ArrayAdapter<String>(context, R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(context, R.layout.device_list_item);

        listPairedDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);

        //Add itemclicklisteners for our list views
        listAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //Get the detail from the listed item (device) including name and address. We just want the address
                String info = ((TextView) view).getText().toString();
                //Last 17 characters (the address of the device
                String address = info.substring(info.length() - 17);

                Intent intent = new Intent();
                intent.putExtra("deviceAddress", address);
                setResult(RESULT_OK, intent);
                //Finish the activity
                finish();
            }
        });


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //This returns all the paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

//        if(pairedDevices != null && pairedDevices.size() > 0)
//        {
//            for(BluetoothDevice device : pairedDevices)
//            {
//
//                //Populate our paired devices
//                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
//
//            }
//        }

        //Create an intent filter for the two events we're interested in
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceListener, intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDeviceListener, intentFilter1);

    }


    //Need a mechanism to listen to incoming scanned devices - need a broadcast receiver
    //Only used for normal Bluetooth (NOT BLE)
    private BroadcastReceiver bluetoothDeviceListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            //If the action found is our current action
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                //If the found device isn't one from the paired devices list
                if(device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    adapterAvailableDevices.add(device.getName() + '\n' + device.getAddress() + '\n' + rssi);
                }
            }
            //Discovering devices is finished
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                //Hide scanner
                progressScanDevices.setVisibility(View.GONE);
                if(adapterAvailableDevices.getCount() == 0)
                {
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                }
                //If devices found
                else
                {
                    Toast.makeText(context, "Click on device to create connection", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.menu_scan_devices:
                scanDevices();
                return true;
            case R.id.menu_stop_scan_devices:
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                progressScanDevices.setVisibility(View.GONE);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void scanDevices()
    {
        progressScanDevices.setVisibility(View.VISIBLE);
        adapterAvailableDevices.clear();

        //Toast.makeText(context, "Scanning started", Toast.LENGTH_SHORT).show();

        //If looking for available devices is true
        if(mBluetoothAdapter.isDiscovering())
        {
            //Cancel the scan
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        //Restart the scan
        mBluetoothAdapter.startLeScan(mLeScanCallback);


    }




    private void scanLeDevice(final boolean enable, Context context, Intent intent) {
        String action = intent.getAction();
        //Toast.makeText(context, "1111111111", Toast.LENGTH_SHORT).show();
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "HEREEEE", Toast.LENGTH_SHORT).show();
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {

            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        String action = getIntent().getAction();

                        @Override
                        public void run() {

                            if(device.getName() != null && device.getName().equalsIgnoreCase("TSIv2")) {
                                adapterAvailableDevices.clear();

                                List<Byte> list = new ArrayList<>();
                                for (byte b : scanRecord) {
                                    list.add(b);

                                }

                                adapterAvailableDevices.add(list.toString());


                            }
                            //Log.w("myApp", device.getName().toString());


                            //}

                        }
                    });
                }
            };
}