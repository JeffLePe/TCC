package com.ligacarro.aplicativo.aplicativoligacarro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.Set;

public class DispositivosPareados extends ListActivity {

    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayAdapter<String> btArrayAdapter;
    private ListView listaDispositivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_pareados);

        listaDispositivos = (ListView) findViewById(android.R.id.list);

        Set<BluetoothDevice> dispPareados = btAdapter.getBondedDevices();

        btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listaDispositivos.setAdapter(btArrayAdapter);

        if(dispPareados.size() > 0) {
            //precorre a lita de dispositivos pareados para extrair suas informações
            for (BluetoothDevice device: dispPareados) {
                //pega o nome e endreço MAC de cada dispositivo pareado e os insere no listAdapter para ser mostrado no listView
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        listaDispositivos.setAdapter(btArrayAdapter);

        //inicia descoberta por novos dispositivos usando o adapador bluetooth
        btAdapter.startDiscovery();
        //cria um filtro para capturar o momento em que o dipositivo é descoberto
        //registra o filtro e define um receptor para o evento de descoberto
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        //método executado toda vez que um dispositivo é descoberto
        @Override
        public void onReceive(Context context, Intent intent) {
            //obtem o Intent que gerou a ação.
            //verifica se a ação corresponde a descoberta de um novo dispositivo
            //obtem um objeto que represeta o dispositivo descoberto.
            //registra o nome e endereço do dispositivo capturado na lista
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //Extrai o endereço a partir do conteudo do elemento selecionado na lista
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String item = (String) btArrayAdapter.getItem(position);
        String devName = item.substring(0, item.indexOf("\n"));
        String devAddress = item.substring(item.indexOf("\n")+1, item.length());

        //chama o método para deixar o dispositivo visível para outros

        //Utiliza um Intent para encapsular as informções de nome e endereço informando a Activity principal sobre o sucesso do processo
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Dispositivo: " + devName + "MAC: " + devAddress);
        dialog.setNeutralButton("OK", null);
        dialog.show();

        enableVisibility();
        Intent intentRetorno = new Intent();
        intentRetorno.putExtra("devName", devName);
        intentRetorno.putExtra("devAddress", devAddress);
        setResult(RESULT_OK, intentRetorno);
        finish();

    }

    public void enableVisibility() {

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }
}
