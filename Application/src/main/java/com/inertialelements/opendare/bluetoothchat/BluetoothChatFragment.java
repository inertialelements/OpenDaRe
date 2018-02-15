/*
 * Copyright (C) 2014 The Android Open Source Project
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

/*
 * Copyright (C) 2015 GT Silicon Pvt Ltd
 *
 * Licensed under the Creative Commons Attribution 4.0
 * International Public License (the "CCBY4.0 License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://creativecommons.org/licenses/by/4.0/legalcode
 *
 * Note that the CCBY4.0 license is applicable only for the modifications made
 * by GT Silicon Pvt Ltd
 *
 * Modifications made by GT Silicon Pvt Ltd are within the following comments:
 * // BEGIN - Added by GT Silicon - BEGIN //
 * {Code included or modified by GT Silicon}
 * // END - Added by GT Silicon - END //
 */
package com.inertialelements.opendare.bluetoothchat;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inertialelements.opendare.R;
import com.inertialelements.opendare.common.logger.Log;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.inertialelements.opendare.bluetoothchat.Utilities.REQUEST_PERMISSIONS_LOG_STORAGE;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
// BEGIN - Added by GT Silicon - BEGIN //
    // Layout Views

//    private EditText mOutEditText;
    private Button mSendButton;
    private Button mStopButton;
    private TextView sview;
    private TextView dis;
    private TextView avgspeed;

    private TextView X;
    private TextView Y;
    private TextView Z;
// END - Added by GT Silicon - END //
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
// BEGIN - Added by GT Silicon - BEGIN //
     Calendar c,filenameDate;
     SimpleDateFormat sdf;

    byte[] process_off={0x32, 0x00, 0x32};
    byte[] output_off={0x22, 0x00, 0x22};
    int bytes,i,j,step_counter,package_number,package_number_1,package_number_2,package_number_old=0;
    int[] header= {0,0,0,0};
    byte [] ack = new byte[5];

    double sin_phi, cos_phi;
    float [] payload= new float[14];
    double[] final_data=new double[3];

    double[] dx =new double [4];

    double[] x_sw=new double[4];

    byte[] temp=new byte[4];


    double []delta= {0.0,0.0,0.0};
    double distance=0.0;
    double distance1=0.0;


    private static final boolean D = true;
    long timeSec1 = 0;
    long timeSec2 = 0;
    double avg = 0;

    //variables for processing
    long timeSec6 = 0;
    DecimalFormat df1 =new DecimalFormat("0.00");
// END - Added by GT Silicon - END //
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // BEGIN - Added by GT Silicon - BEGIN //
//        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        mStopButton = (Button) view.findViewById(R.id.button_stop);
        sview = (TextView) view.findViewById(R.id.stepcount);
        dis = (TextView) view.findViewById(R.id.dis);
        avgspeed = (TextView) view.findViewById(R.id.avgspeed);
        X= (TextView) view.findViewById(R.id.X);
        Y= (TextView) view.findViewById(R.id.Y);
        Z= (TextView) view.findViewById(R.id.Z);
        timerValue = (TextView) view.findViewById(R.id.timer);
        // END - Added by GT Silicon - END //
       }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        // Initialize the compose field with a listener for the return key
//        mOutEditText.setOnEditorActionListener(mWriteListener);
    // BEGIN - Added by GT Silicon - BEGIN //
        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Send a message using content of the edit text widget
                if (mConnectedDeviceName == null) {
                    AlertDialog.Builder alertdialog = new AlertDialog.Builder(getActivity());
                    alertdialog.setTitle("Please connect the device");
                    alertdialog.setMessage("Device is not connected. Press OK to connect the device");
                    alertdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

                            //  return true;
                            //  mConversationArrayAdapter.add("BT Connected");
                        }
                    });
                    alertdialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertdialog.show();

                }
                else {
                    checkAndRequestWriteLog();
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                }

                filenameDate = Calendar.getInstance();
                byte[] send = {0x34, 0x00, 0x34};
                mChatService.write(send);										//Deadreckoning reset
                x_sw[0]=x_sw[1]=x_sw[2]=x_sw[3]=0.0;

            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatService.write(process_off);
                mChatService.write(output_off);
                mChatService.stop();
                mConnectedDeviceName = null;
                step_counter = 0;
                counter = 0;
                delta[0] = 0;
                delta[1] = 0;
                delta[2] = 0;
                x_sw[0] = 0;
                x_sw[1] = 0;
                x_sw[2] = 0;
                x_sw[3] = 0;
                distance1 = 0;
                distance = 0;
                timeSwapBuff = 0L;
                customHandler.removeCallbacks(updateTimerThread);
                   }
        });
    // END - Added by GT Silicon - END //
       // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }



    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }
// BEGIN - Added by GT Silicon - BEGIN //
    long timeSec3 = 0;
    private int counter = 0;
    private long StepD = 0;
    private Calendar t_origin;
    String finaltime;
    long timeSec;
    private int timer;
    double Avgspeed;
    double speednow = 0;
    private long timeprint;
// END - Added by GT Silicon - END //
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                        // BEGIN - Added by GT Silicon - BEGIN //
                            // Vibrate for 500 milliseconds
                          //  vib.vibrate(300);
                            step_counter=0;final_data[0]=0; final_data[1]=0;final_data[2]=0;timeSec1=0;timeSec2=0;timeSec6=0;timeSec3 = 0;timeSec=0;Avgspeed = 0;speednow = 0;
                            x_sw[0] = 0;
                            x_sw[1] = 0;
                            x_sw[2] = 0;
                            x_sw[3] = 0;
                            distance = 0;
                            distance1 = 0;
                            avg = 0;
                            StepD = 0; //stepDuration
                           // startTime = SystemClock.uptimeMillis();
                         //   customHandler.postDelayed(updateTimerThread,0);
                         //   customHandler.removeCallbacks(updateTimerThread);
                            PrintData();
                        // END - Added by GT Silicon - END //
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));

                            // mConversationArrayAdapter.clear();
                           // Toast.makeText(getActivity(),"bt_Connected",
                              //      Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
              /*  case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;*/
                case Constants.MESSAGE_READ:
                    // BEGIN - Added by GT Silicon - BEGIN //
                    byte[] buffer = (byte[]) msg.obj;
                    int i=0;
                    bytes = buffer.length;

                    Log.i(TAG, "read buffer in hex"  + byte2HexStr(buffer,64));
                    // writetofile( bytestring,byte2HexStr(buffer,64)+"\n" );
                    for(j=0;j<4;j++)
                    {
                        header[j]=buffer[i++]& 0xFF;          //HEADER ASSIGNED
                        Log.i(TAG," "+ header[j]);
                    }
                    for(j=0;j<14;j++)
                    {
                        for(int k=0;k<4;k++)
                            temp[k]=buffer[i++];
                        payload[j]=ByteBuffer.wrap(temp).getFloat();				//PAYLOAD ASSIGNED //
                     }

                    i++;i++;

                    Log.i(TAG, ""+ payload[0]+ "  "+ payload[2]);

                    ++i;++i;												// FOR SKIPPING CHECKSUM
                    package_number_1=header[1];
                    package_number_2=header[2];
                    ack = createAck(ack,package_number_1,package_number_2);
                    mChatService.write(ack);								//ACKNOWLEDGEMENT SENT
                    Log.i(TAG, "Acknowledgement Written");
                    package_number = package_number_1*256 + package_number_2;		//PACKAGE NUMBER ASSIGNED
                    //writetofile(bytestring, "package number:- "+ package_number+"\n");
                    if(package_number_old != package_number)
                    {
                        for(j=0;j<4;j++)
                            dx[j]=(double)payload[j];

                        stepwise_dr_tu();
                        Log.e(TAG, "final data sent" + final_data[0] + " " + final_data[1] + " "+final_data[2]);
                        c = Calendar.getInstance();
                        sdf = new SimpleDateFormat("HHmmss");
                        long timeSec= (c.getTimeInMillis()-filenameDate.getTimeInMillis());
                         if(timeSec != timeSec1){
                            timeSec1= timeSec;
                        }
                       if(distance1 >= 0.05)
                        {


                            timeSec3 = timeSec1-timeSec2;

                            timeSec6 = timeSec6 + timeSec3;
                            timeSec2 =timeSec1;
                            long timeSec5= (c.getTimeInMillis()-filenameDate.getTimeInMillis());
                            step_counter++;
                            DecimalFormat df1 =new DecimalFormat("0.00");
                            DecimalFormat df2 =new DecimalFormat("000");
                            avg = distance/step_counter;
                            speednow = (distance1*3.6)/(timeSec3/1000);
                            Avgspeed = distance*3.6/(timeSec6/1000);
                            StepD = timeSec6/step_counter; //stepDuration
                            StepData stepData = new StepData(step_counter,final_data[0],final_data[1],final_data[2],distance);
                            Utilities.writeDataToLog(getActivity().getApplicationContext(),stepData);
                            PrintData();
                            //	mConversationArrayAdapter.add(" x= " + df.format(final_data[0]) +" y= "+df.format(final_data[1])+" z= "+df.format(final_data[2])+ " Speed= "+df.format(distance1*18/timeSec*5)+"kmph"+" dis= "+ df.format(distance) +" step= "+ step_counter);
                            mConversationArrayAdapter.add("  "+step_counter+". x= " + df1.format(final_data[0])+"m   y= "+df1.format(final_data[1])+"m   z= "+df1.format(final_data[2])+"m"+" "+"Speed = "+df1.format(speednow)+"m");
                           }

                        package_number_old=package_number;
                    }
                    // END - Added by GT Silicon - END //
                    // construct a string from the valid bytes in the buffer
                 //   String readMessage = new String(readBuf, 0, msg.arg1);
                //    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    // BEGIN - Added by GT Silicon - BEGIN //
    public void PrintData() {
        dis.setText(" " + df1.format(distance));//totaldistance
        avgspeed.setText(" "+df1.format(Avgspeed));//avgspeed
        sview.setText(" "+step_counter);//stepcount
        X.setText(" " + df1.format(final_data[0]));//x
        X.invalidate();
        Y.setText(" " + df1.format(final_data[1]));//y
        Y.invalidate();
        Z.setText(" " + df1.format(final_data[2]));//z
        Z.invalidate();
    }
    // END - Added by GT Silicon - END //

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
            // BEGIN - Added by GT Silicon - BEGIN //
            case R.id.help:{
                Intent helpdesk = new Intent(getActivity(), Helpdesk.class);
                startActivity(helpdesk);
                return true;
            }
            // END - Added by GT Silicon - END //
        }
        return false;
    }

    // BEGIN - Added by GT Silicon - BEGIN //
    public static String byte2HexStr(byte[] paramArrayOfByte, int paramInt)
    {
        StringBuilder localStringBuilder1 = new StringBuilder("");
        int i = 0;
        for (;;)
        {
            if (i >= paramInt)
            {
                String str1 = localStringBuilder1.toString().trim();
                Locale localLocale = Locale.US;
                return str1.toUpperCase(localLocale);
            }
            String str2 = Integer.toHexString(paramArrayOfByte[i] & 0xFF);
            if (str2.length() == 1) {
                str2 = "0" + str2;
            }
            localStringBuilder1.append(str2);
            localStringBuilder1.append(" ");
            i += 1;
        }
    }

    public byte[] createAck(byte[] ack, int package_number_1, int package_number_2)
    {
        ack[0]=0x01;
        ack[1]=(byte)package_number_1;
        ack[2]=	(byte)package_number_2;
        ack[3]=	(byte)((1+package_number_1+package_number_2-(1+package_number_1+package_number_2) % 256)/256);
        ack[4]=	(byte)((1+package_number_1+package_number_2) % 256);
        return ack;
    }

    public void stepwise_dr_tu()
    {
        sin_phi=(float) Math.sin(x_sw[3]);
        cos_phi=(float) Math.cos(x_sw[3]);
        Log.i(TAG, "Sin_phi and cos_phi created");
		delta[0]=cos_phi*dx[0]-sin_phi*dx[1];
        delta[1]=sin_phi*dx[0]+cos_phi*dx[1];
        delta[2]=dx[2];
        x_sw[0]+=delta[0];
        x_sw[1]+=delta[1];
        x_sw[2]+=delta[2];
        x_sw[3]+=dx[3];
        final_data[0]=x_sw[0];
        final_data[1]=x_sw[1];
        final_data[2]=x_sw[2];
        distance1=Math.sqrt((delta[0]*delta[0]+delta[1]*delta[1]+delta[2]*delta[2]));
        distance+=Math.sqrt((delta[0]*delta[0]+delta[1]*delta[1]));
    }

    long timeInMilliseconds = 0L;
    private long startTime = 0L;
    long updatedTime = 0L;
    long timeSwapBuff = 0L;
    private Handler customHandler = new Handler();
    private TextView timerValue;

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hr = mins/60;

            secs = secs % 60;
           // int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText(" "+ hr + ":" + mins + ":"+ String.format("%02d", secs));
            customHandler.postDelayed(this, 0);
        }
    };

    /**
     * Check the permission whether app can create or write files. If permission is not granted,
     * then request user to grant write storage permission otherwise will create log file and write data.
     */
    private void checkAndRequestWriteLog(){
        if (Build.VERSION.SDK_INT >= 23){
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED ) {
                Utilities.writeHeaderToLog(getActivity().getApplicationContext());
            }else{
                getActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_LOG_STORAGE);
            }
        }else{
            Utilities.writeHeaderToLog(getActivity().getApplicationContext());
        }
    }
// END - Added by GT Silicon - END //
}
