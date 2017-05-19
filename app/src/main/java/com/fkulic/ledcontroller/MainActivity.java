package com.fkulic.ledcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int REQ_BT_ENABLE = 10;

    ListView lvDevices;

    private BluetoothAdapter myBluetooth = null;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvDevices = (ListView) findViewById(R.id.lvDevices);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            // no bluetooth on device
            showToast("Bluetooth Device Not Available");
            finish();
        } else if (!myBluetooth.isEnabled()) {
            // ask user to turn on bluetooth
            requestBluetoothEnable();
        } else {
            showPairedDevices();
        }
    }

    private void requestBluetoothEnable() {
        Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnBTon, REQ_BT_ENABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_BT_ENABLE) {
            if (resultCode == RESULT_OK) {
                showPairedDevices();
            } else {
                showToast("Please enable your Bluetooth");
            }
        }
    }

    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        List<String> list = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            showToast("No Paired Bluetooth Devices Found.");
        }

        // Adapter for devices list view, item click listener
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        lvDevices.setAdapter(adapter);
        lvDevices.setOnItemClickListener(devicesListItemClickListener);
    }

    private AdapterView.OnItemClickListener devicesListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // get MAC address - last 17 characters
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Start ControlLedActivity
            Intent intent = new Intent(MainActivity.this, ControlLedActivity.class);
            intent.putExtra(EXTRA_ADDRESS, address);
            startActivity(intent);
        }
    };

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}
