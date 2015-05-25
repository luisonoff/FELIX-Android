/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmsoft.bluetooth.le;


import com.example.bluetooth.le.R;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//LUIS
import android.view.MotionEvent;
import android.view.Display;
import android.graphics.Point;
import android.view.WindowManager;
import android.os.AsyncTask;
import java.util.*;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.Timer;
import java.util.TimerTask;
//FIN LUIS
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    private ScheduledExecutorService scheduleTaskExecutor;
//    private ScheduledThreadPoolExecutor scheduleTaskExecutor;


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mDataField,dataM,dataC,dataA,dataB,dataY;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private int screenX,screenY;

    private int index_back = 0;
    private String bufferBLE = "";
    
    EditText edtSend;
	ScrollView svResult;

    Button btnmagenta,btncyan,btnyellow,btnoff,btnxy,btnxy2;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            
            Log.e(TAG, "mBluetoothLeService is okay");
            //LUIS
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //The connection is successful
            	Log.e(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //Disconnect
                mConnected = false;
                invalidateOptionsMenu();
                btnmagenta.setEnabled(false);
                btncyan.setEnabled(false);
                btnyellow.setEnabled(false);
                btnoff.setEnabled(false);
                btnxy.setEnabled(false);
                btnxy2.setEnabled(false);
                clearUI();
            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //You can start work again
            {
            	mConnected = true;
            	mDataField.setText("");
            	ShowDialog();
                btnmagenta.setEnabled(true);
                btncyan.setEnabled(true);
                btnyellow.setEnabled(true);
                btnoff.setEnabled(true);
                btnxy.setEnabled(true);
                btnxy2.setEnabled(true);

            	Log.e(TAG, "In what we need");
            	invalidateOptionsMenu();
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //Receive data
            	Log.e(TAG, "RECV DATA");
            	String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            	if (data != null) {
                	if (mDataField.length() > 500)
                		mDataField.setText("");
                    mDataField.append(data);
                    bufferBLE = bufferBLE.concat(data);
                    svResult.post(new Runnable() {
            			public void run() {
            				svResult.fullScroll(ScrollView.FOCUS_DOWN);
            			}
            		});
                    int index = 0;
                    while(index+10 < bufferBLE.length()){
                        if( bufferBLE.charAt(index) == '#'){
                            if( bufferBLE.charAt(index+1) == 'Y'){

                                int indexE = bufferBLE.indexOf(';', index+1);
                                if(indexE != -1) {
                                    dataY.setText("Yaw\n" + bufferBLE.substring(index + 2, indexE));
                                }
                            }
                            if( bufferBLE.charAt(index+1) == 'B'){
                                int indexE = bufferBLE.indexOf(';', index+1);
                                if(indexE != -1) {
                                    dataB.setText("Bat\n" + bufferBLE.substring(index + 2, indexE) + " V");
                                }
                            }

//                            if( data.charAt(index+1) == 'B'){
//                                int indexE = data.indexOf(';', index+1);
//                                if(indexE != -1) {
//                                    dataC.setText(data.substring(index + 2, indexE));
//                                }
//                            }
                            if( bufferBLE.charAt(index+1) == 'A'){
                                if( bufferBLE.charAt(index+2) == '1') dataA.setText("HIT!  HIT!");
                                else dataA.setText("");
                            }
                        }
                        index+=1; // No eficiente ya que revisa el string mas veces de las necesarias, ademas falta buffer para asegurarnos de que no se pierden mensajes
                    }
                    if( bufferBLE.length() > 10 ) bufferBLE = bufferBLE.substring(bufferBLE.length()-10); //Asi pilla hasta el final del string. REVISAR ESTO. Size del buffer llega a 30 MAX.
                }
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {                                        //Initialization
        super.onCreate(savedInstanceState);

        // Background TASK
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5); //the number of threads to keep in the pool, even if they are idle
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.e(TAG, "BACKGROUND ");
                //AQUI COSAS PARA HACER EN BACKGROUND. ENVIO BLE
//                mBluetoothLeService.WriteValue("#L4;");
//                if( index_back == 0) mBluetoothLeService.WriteValue("#L4;");
//                if( index_back == 1) {
//                    mBluetoothLeService.WriteValue("#L5;");
//                    Log.e(TAG, "Indexback 1 ");
//                }
//                if( index_back == 2) mBluetoothLeService.WriteValue("#L6;");
//                index_back += 1;
//                if( index_back > 2 ) index_back = 0;

                // If you need update UI, simply do this:
                runOnUiThread(new Runnable() {
                    public void run() {
                        // update your UI component here.
//                        myTextView.setText("refreshed");
                    }
                });
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);  // Necesario el retardo de 1 segundo para que no intente llamar a bluetooth sin tener identificador valido o salta excepcion

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        int mUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY     //Necesita API19
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        getWindow().getDecorView().setSystemUiVisibility(mUIFlag);

        setContentView(R.layout.gatt_services_characteristics);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenX = size.x;
        screenY = size.y;






        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mDataField = (TextView) findViewById(R.id.data_value);
        dataM = (TextView) findViewById(R.id.dataM);
        dataC = (TextView) findViewById(R.id.dataC);
        dataA = (TextView) findViewById(R.id.dataA);
        dataY = (TextView) findViewById(R.id.dataY);
        dataB = (TextView) findViewById(R.id.dataB);
//        edtSend = (EditText) this.findViewById(R.id.edtSend);
//        edtSend.setText("1");
        svResult = (ScrollView) this.findViewById(R.id.svResult);
        
        btnmagenta = (Button) this.findViewById(R.id.btnmagenta);
        btnmagenta.setOnClickListener(new ClickEvent());
        btnmagenta.setEnabled(false);

        btnxy = (Button) this.findViewById(R.id.btnxy);
        btnxy.setOnTouchListener(new TouchEvent());
        btnxy.setEnabled(false);

        btnxy2 = (Button) this.findViewById(R.id.btnxy2);
        btnxy2.setOnTouchListener(new TouchEvent());
        btnxy2.setEnabled(false);

        btncyan = (Button) this.findViewById(R.id.btncyan);
        btncyan.setOnClickListener(new ClickEvent());
        btncyan.setEnabled(false);

        btnyellow = (Button) this.findViewById(R.id.btnyellow);
        btnyellow.setOnClickListener(new ClickEvent());
        btnyellow.setEnabled(false);

        btnoff = (Button) this.findViewById(R.id.btnoff);
        btnoff.setOnClickListener(new ClickEvent());
        btnoff.setEnabled(false);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        //LUIS  Go back to home if paused
        if(mConnected)
        {
            mBluetoothLeService.disconnect();
            mConnected = false;
        }
        onBackPressed();


        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        scheduleTaskExecutor.shutdown(); // Closes background task

        //this.unregisterReceiver(mGattUpdateReceiver);
        //unbindService(mServiceConnection);
        if(mBluetoothLeService != null)
        {
        	mBluetoothLeService.close();
        	mBluetoothLeService = null;
        }
        Log.d(TAG, "We are in destroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                              //Click the button
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
            	if(mConnected)
            	{
            		mBluetoothLeService.disconnect();
            		mConnected = false;
            	}
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void ShowDialog()
    {
    	Toast.makeText(this, "Connected to Felix!", Toast.LENGTH_SHORT).show();
        mBluetoothLeService.WriteValue("#L2;");
    }

 //Button event
	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == btnmagenta) {
				if(!mConnected) return;
				
//				if (edtSend.length() < 1) {
//					Toast.makeText(DeviceControlActivity.this, "Please enter the content to be sent", Toast.LENGTH_SHORT).show();
//					return;
//				}

                //LUIS
				mBluetoothLeService.WriteValue("#L6;");

                //LUIS COMMENTED
                //mBluetoothLeService.WriteValue(edtSend.getText().toString());
				
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				if(imm.isActive())
//					imm.hideSoftInputFromWindow(edtSend.getWindowToken(), 0);
//				//todo Send data
			}
            //LUIS
            if (v == btncyan) {
                if(!mConnected) return;

                //LUIS
                mBluetoothLeService.WriteValue("#L5;");
            }
            if (v == btnyellow) {
                if(!mConnected) return;

                //LUIS
                mBluetoothLeService.WriteValue("#L4;");
            }
            if (v == btnoff) {
                if(!mConnected) return;

                //LUIS
                mBluetoothLeService.WriteValue("#L0;");
            }
		}

	}

    class TouchEvent implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == btnxy) {// Zona gas
                if(!mConnected) return true;
                if(event.getActionMasked() == 1 ){ //Detecta cuando levantas dedo.
                    mBluetoothLeService.WriteValue("#mm;");
                    dataM.setText("Gas\nOFF");
//                    mBluetoothLeService.WriteValue("#mm;"); // Por BUG. Porque arduino pierde mensajes.  NO RESUELVE
//                    mBluetoothLeService.WriteValue("#mm;");
                }
                else {
//                    Log.e(TAG, String.format("X: %.0f  Y: %.0f", (event.getX() - screenX * 5 / 28) * 28000 / 5 / screenX, (screenY / 2 - event.getY()) * 2000 / screenY));
                    float posY = (screenY / 2 - event.getY()) * 2000 / screenY;
                    if( posY > 1000 ) posY = 1000;
                    if( posY < -1000 ) posY = -1000;
                    float posX = (event.getX()-screenX *5 / 28)*28000/5/screenX;
                    if( posX > 1000 ) posX = 1000;
                    if( posX < -1000 ) posX = -1000;

                    mBluetoothLeService.WriteValue(String.format("#M%.0f;",posY));
                    dataM.setText(String.format("Gas\n%.0f", (posY + 1000) / 2));
                }

            }

            if ( v == btnxy2) { //Zona control
                if(!mConnected) return true;
                if(event.getActionMasked() == 1 ){ //Detecta cuando levantas dedo.
                    mBluetoothLeService.WriteValue("#cc;");
                    dataC.setText("Control\nOFF");

//                    mBluetoothLeService.WriteValue("#cc;"); // Por BUG. Porque arduino pierde mensajes.  NO RESUELVE
//                    mBluetoothLeService.WriteValue("#cc;");
                }
                else {
                    // Esta es la forma de calcular coordenadas [-1000,+1000] Tener en cuenta que pueden salir valores mayores. OK Si botones son 5/14 de pantalla en anchura (no va fino) y 100% altura.
//                    Log.e(TAG, String.format("X2: %.0f  Y2: %.0f", (event.getX()-screenX *5 / 28)*28000/5/screenX, (screenY / 2 - event.getY()) * 2000 / screenY));
                    float posY = (screenY / 2 - event.getY()) * 2000 / screenY;
                    if( posY > 1000 ) posY = 1000;
                    if( posY < -1000 ) posY = -1000;
                    float posX = (event.getX()-screenX *5 / 28)*28000/5/screenX;
                    if( posX > 1000 ) posX = 1000;
                    if( posX < -1000 ) posX = -1000;

                    mBluetoothLeService.WriteValue(String.format("#C%.0f;",posY));
                    mBluetoothLeService.WriteValue(String.format("#Y%.0f;",posX));
                    dataC.setText(String.format("Control\n%.0f", posY));
                }

            }
            return true;
        }

    }
	
    private static IntentFilter makeGattUpdateIntentFilter() {                        //Registered the event received
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

}
