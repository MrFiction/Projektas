package com.led.led;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

public class ledControl extends ActionBarActivity {

    Button btnDis;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    // SPP UUID
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); // gaunamas adresas iš Bluetooth prietaiso

        // ledControl vaizdas
        setContentView(R.layout.activity_led_control);

        // iškviesti valdiklius
        btnDis = (Button)findViewById(R.id.button1);

        new ConnectBT().execute(); // Iškviesti klasę prisijungimui

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); // atsijungti
            }
        });

    }

    private void Disconnect()
    {
        if (btSocket!=null) // jei Bluetooth Socket užimtas
        {
            try
            {
                btSocket.close(); // išjungti ryšį
                // msg("Atsijungta.");                      CIA
            }
            catch (IOException e)
            { msg("Klaida");}
        }
        finish(); // grįžti į pradinį langą
    }

    // greitas būdas iškviesti Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; // jeigu jis yra, beveik prisijungta

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Prisijungiama...", "Prašome palaukti!");  // rodoma progreso žinutė
        }

        @Override
        protected Void doInBackground(Void... devices) // kol progreso žinutė rodoma, beveik prisijungta
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                 myBluetooth = BluetoothAdapter.getDefaultAdapter(); // gaunamas Bluetooth prietaisa
                 BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address); // prisijungia prie prietaiso adreso ir patikrina, jei jis galimas
                 btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID); // sukuriamas RFCOMM (SPP) ryšys
                 BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                 btSocket.connect(); // pradedama prisijungti
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false; // jeigu nepavyksta, galima patikrinti išimtis
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) // po doInBackground, patikrinama, ar viskas veikia gerai
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Prisijungti nepavyko. Pabandykite dar kartą!");
                finish();
            }
            else
            {
                msg("Prisijungta.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
