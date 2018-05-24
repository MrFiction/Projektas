package com.led.led;

import android.os.Handler;
import android.content.Context;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.marcinmoskala.arcseekbar.ProgressListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class KettleControl extends AppCompatActivity {

    Button btnDis;
    Button onOFF;
    TextView arduinoData;
    Switch switcher;

    TextView seekBarTemp;
    TextView celcius;

    String address = null;
    boolean stopWorker = false;
    int readBufferPosition = 0;
    byte delimiter = 10;
    byte[] readBuffer = new byte[1024];

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private InputStream inStream = null;
    private ProgressDialog progress;

    Handler handler = new Handler();

    ArcSeekBar defaultSeekBar;

    private static final int uniqueID = 65664;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);
        setContentView(R.layout.activity_kettle_control);

        celcius = (TextView)findViewById(R.id.textView7);

        btnDis = (Button) findViewById(R.id.button1);

        new ConnectBT().execute();

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });

        switcher = (Switch) findViewById(R.id.switcheris);
        arduinoData = (TextView) findViewById(R.id.textView4);
        onOFF = (Button)findViewById(R.id.button4);
        seekBarTemp = (TextView)findViewById(R.id.textView3);
        defaultSeekBar = (ArcSeekBar)findViewById(R.id.defaultSeekBar);

        defaultSeekBar.setVisibility(View.GONE);
        seekBarTemp.setVisibility(View.GONE);

        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = switcher.isChecked();
                if(!check)
                {
                    msg("Bus viriama iki numatytos temperatūros (90°)!");
                    defaultSeekBar.setVisibility(View.GONE);
                    seekBarTemp.setVisibility(View.GONE);
                }
                else
                {
                    arduinoData.setText("60");
                    msg("Pasirinkite norimą temperatūra!");
                    defaultSeekBar.setVisibility(View.VISIBLE);
                    seekBarTemp.setVisibility(View.GONE);
                }
        }});


        onOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sk;
                boolean check = switcher.isChecked();
                if(!check)
                {
                    sk = 190;
                    turnHeatingOfforON(sk);
                }
                else
                {
                    sk = Integer.parseInt(seekBarTemp.getText().toString());
                    sk = sk + 100;
                    turnHeatingOfforON(sk);
                }
            }
        });
    }

    private void getNotifications()
    {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Pranešimas")
                        .setContentText("Vanduo pasiekė pasirinktiną temperatūros!")
                        .setTicker("Gautas pranešimas apie užvertą vandenį!")
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(this, AppCompatActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(uniqueID, builder.build());
    }

    private void getNotifications2()
    {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Pranešimas")
                        .setContentText("Vanduo užvirė iki numatytos temperatūros!")
                        .setTicker("Gautas pranešimas apie užvertą vandenį!")
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(this, AppCompatActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(uniqueID, builder.build());
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
                    switcher.setChecked(false);
                    defaultSeekBar.setVisibility(View.GONE);
                }
            }
            catch (IOException e)
            {
                msg("Klaida.");
            }
        }
    }

    private void getData() {
        try
        {
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
                                    final byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if(arduinoData.getText().toString().equals(".."))
                                            {
                                                // arduinoData.setText(data);
                                            }
                                            else
                                            {
                                                // arduinoData.setText(data);

                                                boolean check = switcher.isChecked();
                                                if(check)
                                                {
                                                    if(onOFF.getText().equals("VIRTI")) {

                                                        arduinoData.setText(data);
                                                        defaultSeekBar.setOnProgressChangedListener(new ProgressListener() {
                                                            @Override
                                                            public void invoke(int i) {
                                                                defaultSeekBar.setMaxProgress(91);
                                                                i = (i / 3) + 60;
                                                                arduinoData.setText("" + i);
                                                                seekBarTemp.setText("" + i);
                                                            }
                                                        });
                                                    }
                                                    int temperatura = Integer.parseInt(seekBarTemp.getText().toString());
                                                    String tempas = temperatura + "\r";

                                                    if(onOFF.getText().equals("STABDYTI"))
                                                    {
                                                        defaultSeekBar.setVisibility(View.GONE);
                                                        arduinoData.setText(data);

                                                        if(arduinoData.getText().toString().equals(tempas))
                                                        {
                                                            getNotifications();
                                                            seekBarTemp.setVisibility(View.GONE);
                                                            onOFF.setText("VIRTI");
                                                            onOFF.setEnabled(false);

                                                            try {
                                                                btSocket.getOutputStream().write(0);
                                                            } catch (IOException e) {
                                                                msg("Klaida.");
                                                            }
                                                        }
                                                    }
                                                    if ((arduinoData.getText().toString().equals("59\r")) && (!onOFF.isEnabled()))
                                                    {
                                                        defaultSeekBar.setVisibility(View.VISIBLE);
                                                        onOFF.setEnabled(true);
                                                    }
                                                }
                                                else
                                                {
                                                    int temperatura = 90;
                                                    arduinoData.setText(data);

                                                    String tempas = temperatura + "\r";
                                                    if(arduinoData.getText().toString().equals(tempas)) {
                                                        getNotifications2();
                                                        onOFF.setText("VIRTI");
                                                        defaultSeekBar.setVisibility(View.GONE);
                                                        seekBarTemp.setVisibility(View.GONE);
                                                        switcher.setChecked(false);
                                                        onOFF.setEnabled(false);

                                                        try {
                                                            btSocket.getOutputStream().write(0);
                                                        } catch (IOException e) {
                                                            msg("Klaida.");
                                                        }
                                                    }
                                                    if ((arduinoData.getText().toString().equals("59\r")) && (!onOFF.isEnabled()))
                                                    {
                                                        onOFF.setEnabled(true);
                                                    }
                                                }
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
        if (btSocket!=null)
        {
            try
            {
                btSocket.close();
                msg("Atsijungta.");
            }
            catch (IOException e)
            {
                msg("Klaida.");
            }
        }
        finish();
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(KettleControl.this, "Prisijungiama...", "Prašome palaukti!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                    getData();
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
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
