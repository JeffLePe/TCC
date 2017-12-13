package com.ligacarro.aplicativo.aplicativoligacarro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Jefferson on 30/09/2017.
 */

public class ConnectionThread extends Thread{

    BluetoothSocket btSocket = null;
    BluetoothServerSocket btServerSocket = null;
    InputStream input = null;
    OutputStream output = null;
    String btDevAddress = null;
    String myUUID = "00001101-0000-1000-8000-00805F9B34FB";
    boolean server;
    boolean running = false;
    boolean isConnected = false;

    //construtor que prepara o dispositivo para atuar como servidor.
    public ConnectionThread() {
        this.server = true;
    }

    /* construtor prepara o dispositivo para atuar como cliente.
     * -Tem como parâmetro uma string contendo o endereço mac do dispositivo Bluetooth para o qual deve ser solicitada uma conexão */
    public ConnectionThread(String btDevAddress) {
        this.server = false;
        this.btDevAddress = btDevAddress;
    }

    /* Método run() contem as instruções que serão efetivamente realizadas em uma nova thread */
    public void run() {
        /* Anuncia que a thread está sendo executada.
         * Pega uma referencia para o adaptador Bluetooth padrão. */
        this.running = true;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        /* Determina que ações executar dependendo se a thread está configurada para atuar como servidor ou cliente */
        if(this.server) {
            try{
                /* Cria um socket de servidor bluetooth
                * O socket servidor é usado apenas para iniciar a conexão. Permanece em estado de espera até que algum cliente estabeleça uma conexão */
                btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("Super Counter", UUID.fromString(myUUID));
                btSocket = btServerSocket.accept();

                /* Se a conexão foi estabelecida o socket pode ser liberado */
                if(btSocket != null) {
                    btServerSocket.close();
                }

            }catch (IOException e) {
                /* Se ocorrer uma exceção, exibe o stack trace para debug. Envia um código para a Activity principal, informando que a conexão falhou */
                e.printStackTrace();
            }
        }
        else {
            try{
                /* Obtem uma representação do dispositivo Bluetooth com endereço btDevAddress.
                 * Cria um socket Bluetooth */
                BluetoothDevice btDevice = btAdapter.getRemoteDevice(btDevAddress);
                btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(myUUID));

                /* Envia ao sistema um comando para cancelar qualquer processo de descoberta em execução */
                btAdapter.cancelDiscovery();

                /* Solicita uma conexão ao dispositivo cujo endereço é btDevAddress.
                 * Permanece em estado de espera até que a conexão seja estabelecida. */

                btSocket.connect();

            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Gerencia a conexão */
        if(btSocket.isConnected()) {
            /* Envia um código para a activity principal informando que a conexão ocorreu com sucesso. */
            this.isConnected = true;

            try{
                /* Obtem referências para os fluxos de entrada e saída do socket Bluetooth */
                input = btSocket.getInputStream();
                output = btSocket.getOutputStream();

            }catch (IOException e) {
                e.printStackTrace();
                this.isConnected = false;
            }
        }
    }

    /* O método utilizado pela Activity principal para transmitir uma menssagem ao outro lado da conexão.
     * A mensagem deve ser representada por um array de byte */
    public void write(byte[] data) {
        if(output != null) {
            try{
                //trasmite a mensagem
                output.write(data);

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            IOException e = new IOException();
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return this.isConnected;
    }
}
