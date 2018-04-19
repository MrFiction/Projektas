package com.led.led;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import android.util.Log;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class ledControl extends AppCompatActivity {

    Button btnDis;
    Button onOFF;
    TextView txtArduino;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    private InputStream inStream = null;
    Handler handler = new Handler();
    byte delimiter = 10;
    boolean stopWorker = false;
    int readBufferPosition = 0;
    byte[] readBuffer = new byte[1024];

    // SPP UUID
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); // gaunamas adresas iš Bluetooth prietaiso

        // ledControl vaizdas
        setContentView(R.layout.activity_led_control);

        btnDis = (Button) findViewById(R.id.button1);

        new ConnectBT().execute(); // Iškviesti klasę prisijungimui

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); // atsijungti
            }
        });

        txtArduino = (TextView) findViewById(R.id.textView4);  // arduino temperatura

        onOFF = (Button)findViewById(R.id.button4); // virti ar stabdyti

        onOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText temp = (EditText) findViewById(R.id.editText3);
                int sk = Integer.valueOf(temp.getText().toString());
                sk = sk + 100;
                turnHeatingOfforON(sk); // switch
            }
        });

    }

    private void turnHeatingOfforON(int temp)
    {
        if (btSocket!=null)
        {
            try
            {
                if(onOFF.getText().equals("VIRTI"))
                {
                    onOFF.setText("STABDYTI");
                    btSocket.getOutputStream().write(temp);
                }
                else
                {
                    onOFF.setText("VIRTI");
                    btSocket.getOutputStream().write(0);
                }
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void getData() {
        try  {
            inStream = btSocket.getInputStream();
        }
        catch (IOException e) {
        }

        Thread workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = inStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if(txtArduino.getText().toString().equals("..")) {
                                                txtArduino.setText(data);
                                            } else {
                                                txtArduino.setText(data);
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    private void Disconnect()
    {
        if (btSocket!=null) // jei Bluetooth Socket užimtas
        {
            try
            {
                btSocket.close(); // išjungti ryšį
                msg("Atsijungta.");
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
                 btSocket.connect(); // pradedama prisijungt
                    getData();
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
