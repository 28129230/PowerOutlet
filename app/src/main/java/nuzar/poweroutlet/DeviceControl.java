package nuzar.poweroutlet;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class DeviceControl {

    private String mDeviceName;
    private String mAddress;
    private final BluetoothAdapter mAdapter;
    private boolean findState;
    private Handler mHandler;
    private ConnectThread mConnectThread;



    public DeviceControl(String DeviceName,Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDeviceName = DeviceName;
        mHandler = handler;
        mConnectThread = new ConnectThread();
    }

    public void setup(){
        mConnectThread.findinBonded();
        if(!findState){
            Message msg1 = mHandler.obtainMessage(MainActivity.MESSAGE_NOT_BONDED);
            msg1.sendToTarget();
        }
    }

    public void start(){
        mConnectThread.start();
    }

    public void stop(){
        mConnectThread.cancel();
    }


    private class ConnectThread extends Thread{

        private BluetoothDevice mmDevice;
        private BluetoothSocket mmSocket;
        private BluetoothDevice GetDevice;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public void findinBonded(){
            Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
            if(pairedDevices.size() > 0){
                for (BluetoothDevice tmp : pairedDevices) {
                    if (tmp.getName().equals(mDeviceName)) {
                        GetDevice = tmp;
                    }else {
                        findState = false;
                        return;
                    }
                }
            }else {
                findState = false;
                return;
            }

            String tmpadd = GetDevice.getAddress();

            if(BluetoothAdapter.checkBluetoothAddress(tmpadd)){
                mAddress = tmpadd;
                findState = true;
            }

        }

        public void run(){
            mmDevice = mAdapter.getRemoteDevice(mAddress);
            BluetoothSocket tmp = null;
            try{
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket =tmp;
            mAdapter.cancelDiscovery();
            try{
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.TOAST, "无法连接设备，请检查设备是否打开！");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
                try{
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
