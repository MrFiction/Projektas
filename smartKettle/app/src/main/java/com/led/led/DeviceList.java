package com.led.led;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends ActionBarActivity
{
    // Valdikliai
    Button mygtukasSuporuotas;
    ListView prietaisųSąrašas;

    // Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        // Kviečiu valdiklius
        mygtukasSuporuotas = (Button)findViewById(R.id.button);
        prietaisųSąrašas = (ListView)findViewById(R.id.listView);

        // Jei prietaisas turi Bluetooh
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            // Parodoma žinutė, jei prietaisas neturi Bluetooth adapterio
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            // Baigti APK
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
                // Paprašyti įjungti Bluetooth
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
        }

        mygtukasSuporuotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); // Gauti prietaiso vardą ir adresą
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        prietaisųSąrašas.setAdapter(adapter);
        prietaisųSąrašas.setOnItemClickListener(myListClickListener); // Metodas iškviečiamas, kai prietaisas iš sąrašo yra paspaudžiamas

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Gauti prietaiso MAC adresą, paskutinius 17 simbolių
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Pradėti naują veiklą
            Intent i = new Intent(DeviceList.this, ledControl.class);

            // Pakeisti veiklą
            i.putExtra(EXTRA_ADDRESS, address); // šitas bus gautas ledControl (class) Activity
            startActivity(i);
        }
    };
}
