package com.ligacarro.aplicativo.aplicativoligacarro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.bluetooth.*;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainLigaCarro extends Activity implements View.OnClickListener{

    private ConnectionThread connect;
    private static int PROCURA_DISPOSITIVO = 1;

    private Button buttonAdicionar;
    private Button buttonExcluir;
    private Button buttonLigaDesliga;

    private boolean statusLigado = false;

    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_liga_carro);

        //link entre as represetações gráficas e seus componentes java
        buttonAdicionar = (Button) findViewById(R.id.buttonAdicionar);
        buttonExcluir = (Button) findViewById(R.id.buttonExcluir);
        buttonLigaDesliga = (Button) findViewById(R.id.buttonLigaDesliga);

        buttonAdicionar.setOnClickListener(this);
        buttonExcluir.setOnClickListener(this);
        buttonLigaDesliga.setOnClickListener(this);

        AlertDialog.Builder dialog;

        //Verifica se o dispositivo bluetooth está funcionando
        if(adapter != null) {

            dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Ótimo! Hardware Bluetooth está funcionando!");
            dialog.setNeutralButton("OK", null);
            dialog.show();

        }
        else{

            dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Que pena! Hardware Bluetooth não está funcionando");
            dialog.setNeutralButton("OK", null);
            dialog.show();

        }

        //Ativa o dispositivo Bluetooth
        adapter.enable();
        if(adapter.isEnabled()) {

            dialog.setMessage("Bluetooth Ativado!");
            dialog.setNeutralButton("OK", null);
            dialog.show();

        }

        try{
            Thread.sleep(1000);
        }catch (Exception E) {
            E.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        if(v == buttonAdicionar) {
            adicionaDigital();
        }
        if(v == buttonExcluir) {
            excluiDigitais();
        }
        if(v == buttonLigaDesliga) {
            ligaDesliga();
        }

    }

    public void adicionaDigital() {

        AlertDialog.Builder dialogConfirm = new AlertDialog.Builder(this);
        dialogConfirm.setTitle("Cadastrar Digital");
        dialogConfirm.setMessage("Deseja cadastrar impressão digital?");
        dialogConfirm.setPositiveButton("SIM",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connect.write("C" .getBytes());
                    }
                }
        );
        dialogConfirm.setNegativeButton("NÃO", null);
        dialogConfirm.show();

    }

    public void excluiDigitais() {

        AlertDialog.Builder dialogConfirm = new AlertDialog.Builder(this);
        dialogConfirm.setTitle("Excluir Digitais");
        dialogConfirm.setMessage("Deseja realmente excluir todas as digitais? \n Ao realizar este comando todas as digitais serão apagadas do sistema.");
        dialogConfirm.setPositiveButton("SIM",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connect.write("D" .getBytes());
                    }
                }
        );
        dialogConfirm.setNegativeButton("NÃO", null);
        dialogConfirm.show();

    }

    public void ligaDesliga() {

        if(statusLigado == false) {
            connect.write("HIGH" .getBytes());
            statusLigado = true;
        }
        else {
            connect.write("LOW" .getBytes());
            statusLigado = false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setVisible(true);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.mn_opacao1) {
            procuraDispositivo();
        }

        return super.onOptionsItemSelected(item);

    }

    public void procuraDispositivo() {

        Intent procuraDispositivo = new Intent(this, DispositivosPareados.class);
        startActivityForResult(procuraDispositivo, PROCURA_DISPOSITIVO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == PROCURA_DISPOSITIVO) {
            if(resultCode == RESULT_OK) {
                String devName = data.getStringExtra("devName");
                String devAddress = data.getStringExtra("devAddress");
                conectaDispositivo(devName, devAddress);
            }
            else {
                new AlertDialog.Builder(this).setTitle("Erro").setMessage("Nenhum dispositivo selecionado!");
            }
        }

    }

    public void conectaDispositivo (String name, String address) {

        AlertDialog.Builder dialogStatus = new AlertDialog.Builder(this);
        dialogStatus.setMessage("Dispositivo: " + name + "\n" + "MAC: " + address);
        dialogStatus.setNeutralButton("OK", null);
        dialogStatus.show();
        connect = new ConnectionThread(address);
        connect.run();

    }

    public static Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString = new String(data);

            if(dataString.equals("---N")){
                //apresentaMensagem("Ocorreu um erro durante a conexão");
            }
            else if(dataString.equals("---S")){
                //apresentaMensagem("Conectado");
            }
            else if(dataString.equals("---I")){
                //apresentaMensagem("Ocorreu um erro durante a conexão neste lugar cliente");
            }
            else if(dataString.equals("---A")){
                //apresentaMensagem("Ocorreu um erro durante a conexão neste lugar servidor");
            }
            else if(dataString.equals("---B")) {
                //apresentaMensagem("Ocorreu um erro durante a conexão neste lugar gerencia");
            }
            else if(dataString.equals("LOW")) {
                //apresentaMensagem("Chegou LOW");
            }
            else{
                //apresentaMensagem(dataString);
            }
        }

        /*private void apresentaMensagem(String message) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(MainLigaCarro.class);
            dialog.setMessage(message);
            dialog.setNeutralButton("OK", null);
            dialog.show();

        }*/
    };


}
