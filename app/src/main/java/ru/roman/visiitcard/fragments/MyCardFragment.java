package ru.roman.visiitcard.fragments;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.roman.visiitcard.BluetoothIntentService;
import ru.roman.visiitcard.R;

public class MyCardFragment extends Fragment implements View.OnClickListener {

    BluetoothIntentService bluetoothIntentService;
    Button startSearch;
    Button onBluetooth;
    Button offBluetooth;
    TextView textInfo;
    ListView mListDevices;

    BluetoothReceiver bluetoothReceiver;
    BluetoothAdapter mBluetoothAdapter;
    String nameBTadapter;
    String actionStateChanged;

    Intent bluetoothIntent;

    private boolean isBluetoothReceiverRegister;
    private boolean isDiscoveryReceiverRegister;

    private boolean isDiscoverDevice;

    DiscoveryDevice discoveryDevice;
    DeviceFoundReceiver deviceFoundReceiver;

    ArrayList<String> catNames = new ArrayList<>();
    ArrayAdapter devicesAdapter;
    ListView listInfo;

    //константы для соединения Bluetooth устройств
    final int DISCOVERY_REQUEST = 123;

    public MyCardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        discoveryDevice = new DiscoveryDevice();

        bluetoothIntentService = new BluetoothIntentService();

        View view = inflater.inflate(R.layout.fragment_my_card, container, false);
        startSearch = (Button) view.findViewById(R.id.startSearch);
        onBluetooth = (Button) view.findViewById(R.id.onBluetooth);
        offBluetooth = (Button) view.findViewById(R.id.offBluetooth);
        textInfo = (TextView) view.findViewById(R.id.text_info);

        listInfo = (ListView) view.findViewById(R.id.listInfo);

        deviceFoundReceiver = new DeviceFoundReceiver();

        devicesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        listInfo.setAdapter(devicesAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null) {

            nameBTadapter = mBluetoothAdapter.getName();
            Toast.makeText(getActivity(), "Ваше устройство поддерживает Bluetooth", Toast.LENGTH_SHORT).show();

            startSearch.setOnClickListener(this);
            onBluetooth.setOnClickListener(this);
            offBluetooth.setOnClickListener(this);
        } else {
            startSearch.setEnabled(false);
            onBluetooth.setEnabled(false);
            offBluetooth.setEnabled(false);

            Toast.makeText(getActivity(), "Bluetooth не поддерживается устройством ", Toast.LENGTH_SHORT).show();
        }

        bluetoothReceiver = new BluetoothReceiver();


        actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;

        bluetoothIntent = new Intent(getActivity(), BluetoothIntentService.class);

        isBluetoothReceiverRegister = false;
        isDiscoveryReceiverRegister = false;
        isDiscoverDevice = false;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO: 26.01.2017 Регистрируем приемник
        getActivity().registerReceiver(discoveryDevice, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        getActivity().registerReceiver(bluetoothReceiver, new IntentFilter(actionStateChanged));
        getActivity().registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    public void onPause() {
        super.onPause();

        // TODO: 26.01.2017 Снимаем регистрацию приемника
        getActivity().unregisterReceiver(discoveryDevice);
        getActivity().unregisterReceiver(bluetoothReceiver);
        getActivity().unregisterReceiver(deviceFoundReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startSearch:
                discoverDevice();
                break;
            case R.id.onBluetooth:
                onBluetooth();
                break;
            case R.id.offBluetooth:
                offBluetooth();
                break;
        }
    }


    public void discoverDevice() {


        Toast.makeText(getActivity(), "Нажата кнопка 'Начать поиск!'", Toast.LENGTH_SHORT).show();
        isDiscoveryReceiverRegister = true;
        devicesAdapter.clear();
        isDiscoverDevice = true;

        if (mBluetoothAdapter.isEnabled()) {
            // TODO: 13.01.2017 если BluetoothWork уже включен,запускаем функцию обнаружения устройств с включеным BluetoothWork, чтобы узнать установлено ли приложение на них
            getBluetoothAdapterName();
            Toast.makeText(getActivity(), "BluetoothAdapter уже включен!", Toast.LENGTH_SHORT).show();
            textInfo.setText("Bluetooth уже включен");
            getActivity().startService(bluetoothIntent);
            mBluetoothAdapter.startDiscovery();
        } else {
            mBluetoothAdapter.enable();
            textInfo.setText("Bluetooth включается...");
        }

    }


    public void onBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), DISCOVERY_REQUEST);
        } else {
            Toast.makeText(getActivity(), "Bluetooth уже включен", Toast.LENGTH_SHORT).show();
            textInfo.setText("Bluetooth уже включен");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DISCOVERY_REQUEST && resultCode > 0) {
            textInfo.setText("Нажата кнопка Bluetooth");
            Toast.makeText(getActivity(), "Нажата кнопка Bluetooth!", Toast.LENGTH_SHORT).show();
        } else {
            textInfo.setText("Вы отменили включение Bluetooth");
        }
    }

    public void offBluetooth() {

        isDiscoverDevice = false;
        if (mBluetoothAdapter.isEnabled()) {
            textInfo.setText("Bluetooth выключился");
            mBluetoothAdapter.disable();
        } else {
            Toast.makeText(getActivity(), "Bluetooth не был включен", Toast.LENGTH_SHORT).show();
            textInfo.setText("Bluetooth не был включен");
        }
    }


    private void getBluetoothAdapterName() {
        if (nameBTadapter.endsWith(".visitApp")) {
            Toast.makeText(getActivity(), "Имя BluetoothAdapter уже установлено: " + nameBTadapter, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Имя BluetoothAdapter еще не изменено: " + nameBTadapter, Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.setName(nameBTadapter + ".visitApp");
        }
    }


    // TODO: 22.01.2017 Приемник для определения, когда BluetoothAdapter включен
    class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (state == BluetoothAdapter.STATE_ON) {
                Toast.makeText(getActivity(), "BluetoothAdapter включился!", Toast.LENGTH_SHORT).show();
                textInfo.setText("BluetoothAdapter включился!");
                getBluetoothAdapterName();
                isBluetoothReceiverRegister = true;

                if (isDiscoverDevice) {
                    mBluetoothAdapter.startDiscovery();
                }

            }

        }
    }

    // TODO: 24.01.2017 Приемник для определения, что поиск Bluetooth устройств закончен
    class DiscoveryDevice extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                Toast.makeText(getActivity(), "Поиск устройств Bluetooth закончен!", Toast.LENGTH_SHORT).show();
                textInfo.setText("Поиск устройств Bluetooth закончен!");
            }
        }
    }

    // TODO: 19.02.2017  Приемник для обнаружения устройств с включенным Bluetooth
    class DeviceFoundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                String device = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                devicesAdapter.add(device);
                devicesAdapter.notifyDataSetChanged();

                Toast.makeText(getActivity(), "Обнаружено новое устройство! " + device, Toast.LENGTH_SHORT).show();
                textInfo.setText("Обнаружено новое устройство!");
            }
        }
    }


}
