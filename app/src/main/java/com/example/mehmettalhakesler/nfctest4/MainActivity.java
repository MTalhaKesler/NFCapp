package com.example.mehmettalhakesler.nfctest4;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter myBluetoothAdapter;
    BluetoothSocket mmSocket;
    private UUID uuid = UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"); //Standart bağlantı UUID
    private Set<BluetoothDevice> pairedDevices;

    OutputStream mmOutputStream; // TRANSMITTER olarak kullanılan nesne. Datalar bu nesne üzerinden yollanıyor.
    InputStream mmInputStream;   // RECEIVER olarak kullanılan nesne. Datalar bu nesne üzerinden okunuyor.

    private NfcAdapter adp;
    private Tag tag;
    private TextView txt;
    private TextView text;
    private boolean baglanti = false;
    private Context context;

    private void init() {
        adp = NfcAdapter.getDefaultAdapter(this);
        txt = (TextView) findViewById(R.id.txt);
        context = this;
    }

    private Handler hnd = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public Handler getHandler() {
        return hnd;
    }

    private void CheckNfc() {
        if (adp != null) {
            if (adp.isEnabled()) {
                this.setTitle("NFC açık");
            } else {
                this.setTitle("NFC kapalı");
            }
        } else {
            this.setTitle("Cihaz NFC desteklemiyor");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (intent.getAction() == NfcAdapter.ACTION_TAG_DISCOVERED) {
            TagDiscover(tag);
            adp.ignore(tag, 700, new TagRemoveHandler(), hnd);
            //Intent intent = new Intent(this,MainActivity.class);
        }
    }

    private void TagDiscover(Tag tag) {
        Toast.makeText(this, "NFC Tagi algılandı", Toast.LENGTH_SHORT).show();
        baglanti = true;
        txt.setText("Tag bağlantısı sağlandı");
    }

    public class TagRemoveHandler implements NfcAdapter.OnTagRemovedListener {
        @Override
        public void onTagRemoved() {
            Toast.makeText(context, "Tag bağlantısı kesildi", Toast.LENGTH_SHORT).show();
            txt.setText(String.valueOf("Tag okunamıyor. Lütfen yaklaştırınız."));
            baglanti = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        new AsyncT().execute();
        if (!myBluetoothAdapter.isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Dikkat!");
            builder.setMessage("Lütfen Önce Bluetooth'u açınız");
            builder.setNegativeButton("TAMAM", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(0);
                }
            });
            builder.show();
        } else {
            try {
                connectBT();
            } catch (Exception e) {
            }
        }

        init();
        CheckNfc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        adp.enableForegroundDispatch(this, pi, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        adp.disableForegroundDispatch(this);
    }

    void connectBT() throws IOException {
        try {
            BluetoothDevice device = myBluetoothAdapter.getRemoteDevice("98:D3:31:90:8A:42");
            // Bluetooth modülünün MAC adresi
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            // Standard UUID
            mmSocket = device.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            text.setText("Bağlantı sağlandı");

        } catch (IOException e) {
            Log.d("bağlantı yok", e.getMessage());
            text.setText("Bağlantı sağlanamadı");
        }
    }

    private class AsyncT extends AsyncTask<String, Void, String> {

        private String result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if (baglanti) {
                    try {
                        mmOutputStream.write('1');
                    } catch (IOException ex) {
                    }

                    Thread.sleep(200);

                    result = "abc";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            new AsyncT().execute();
        }
    }
}
