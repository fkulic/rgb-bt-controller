package com.fkulic.ledcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.UUID;

import yuku.ambilwarna.AmbilWarnaDialog;

import static com.fkulic.ledcontroller.MainActivity.EXTRA_ADDRESS;


public class ControlLedActivity extends AppCompatActivity {
    private static final String TAG = "ControlLedActivity";

    // Views
    TextView tvHexNewColor;
    TextView tvNewColor;
    TextView tvHexOldColor;
    TextView tvOldColor;
    Button btnDisconnect;
    Button btnSelectColor;
    ToggleButton btnOnOff;

    // Bluetooth stuff
    String mAddress = null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID of HC-06 BT module

    // colors
    int mOldColor = 0xffffffff;
    int mNewColor = 0xffffffff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_led);
        setUpUi();

        // get MAC address from intent
        Intent intent = getIntent();
        mAddress = intent.getStringExtra(EXTRA_ADDRESS);
        new ConnectBT().execute();

        // Click listener for On/Off button
        btnOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnOnOff.setText("ON");
                    writeToOutputStream("ON");
                } else {
                    btnOnOff.setText("OFF");
                    writeToOutputStream("OFF");
                }
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });

        btnSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectColor();
            }
        });
    }

    private void setUpUi() {
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnOnOff = (ToggleButton) findViewById(R.id.btnOnOff);
        btnSelectColor = (Button) findViewById(R.id.btnSelectColor);
        tvHexNewColor = (TextView) findViewById(R.id.tvHexNewColor);
        tvNewColor = (TextView) findViewById(R.id.tvNewColor);
        tvHexOldColor = (TextView) findViewById(R.id.tvHexOldColor);
        tvOldColor = (TextView) findViewById(R.id.tvOldColor);

        String hexColorOld = String.format("#%06X", (0xFFFFFF & mOldColor));
        String hexColorNew = String.format("#%06X", (0xFFFFFF & mNewColor));
        tvHexOldColor.setText(String.valueOf(hexColorOld));
        tvHexNewColor.setText(String.valueOf(hexColorNew));
    }

    private void selectColor() {
        final AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, mNewColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mOldColor = mNewColor;
                mNewColor = color;
                updateTextViews(color);
                writeToOutputStream(getFormattedColor(color));
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

        });
        dialog.show();
    }

    private void updateTextViews(int color) {
        String hexColorOld = String.format("#%06X", (0xFFFFFF & mOldColor));
        tvHexOldColor.setText(String.valueOf(hexColorOld));
        tvOldColor.setBackgroundColor(mOldColor);

        String hexColorNew = String.format("#%06X", (0xFFFFFF & color));
        tvHexNewColor.setText(String.valueOf(hexColorNew));
        tvNewColor.setBackgroundColor(color);
    }

    private String getFormattedColor(int color) {
        int[] colors = new int[3];
        StringBuilder sb = new StringBuilder("c");
        colors[0] = Color.red(color);
        colors[1] = Color.green(color);
        colors[2] = Color.blue(color);
        for (int c : colors) {
            if (c < 10) {
                sb.append("00");
            } else if (c < 100) {
                sb.append("0");
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private void writeToOutputStream(String command) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(command.getBytes());
            } catch (IOException e) {
                Log.d(TAG, "Error writing to output stream: " + e.getMessage());
                showToast("Error");
            }
        }
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                showToast("Error");
            }
        }
        // switch back to MainActivity
        finish();
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private ProgressDialog mProgressDialog;
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            //show Progress dialog
            mProgressDialog = ProgressDialog.show(ControlLedActivity.this, "Connecting...", "Please wait.");
        }

        // connect to BT device using background thread
        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = myBluetooth.getRemoteDevice(mAddress);
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                Log.d(TAG, "Error while connecting: " + e.getMessage());
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                showToast("Connection Failed.");
                finish(); // go back to MainActivity
            } else {
                showToast("Connected.");
                isBtConnected = true;
            }
            mProgressDialog.dismiss();
        }
    }
}
