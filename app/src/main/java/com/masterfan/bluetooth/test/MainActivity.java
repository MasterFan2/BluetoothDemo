package com.masterfan.bluetooth.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private boolean isRegister = false;//
    private DeviceReceiver mydevice = new DeviceReceiver();
    private BluetoothAdapter blueadapter = null;
    private List<String> deviceList = new ArrayList<>();

    //    private RecyclerView recyclerView;
    private ListView listView;
    private DevicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new DevicesAdapter();
        listView.setAdapter(adapter);

        setBluetooth();
        findAvalibleDevice();
        blueadapter.startDiscovery();
    }

    public class DevicesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_bluetooth, parent, false);
                holder = new MyViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (MyViewHolder) convertView.getTag();
            }
            holder.nameTxt.setText(deviceList.get(position));
            return convertView;
        }

        public class MyViewHolder {//extends RecyclerView.ViewHolder {

            private TextView nameTxt;

            public MyViewHolder(View itemView) {
//                super(itemView);
                nameTxt = (TextView) itemView.findViewById(R.id.name_txt);
            }
        }
    }

    private void setBluetooth() {
        blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (blueadapter != null) {  //Device support Bluetooth
            //确认开启蓝牙
            if (!blueadapter.isEnabled()) {
                //请求用户开启
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RESULT_FIRST_USER);
                //使蓝牙设备可见，方便配对
                Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
                startActivity(in);
                //直接开启，不经过提示
                blueadapter.enable();
                S.o("开启蓝牙");
            } else {
                S.o("蓝牙已开启");
            }
        } else {   //Device does not support Bluetooth
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("No bluetooth devices");
            dialog.setMessage("Your equipment does not support bluetooth, please change device");
            dialog.setNegativeButton("cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            dialog.show();
        }
    }

    private void findAvalibleDevice() {
        //获取可配对蓝牙设备
        Set<BluetoothDevice> device = blueadapter.getBondedDevices();

        if (blueadapter != null && blueadapter.isDiscovering()) {
            deviceList.clear();
            adapter.notifyDataSetChanged();
        }
        if (device.size() > 0) { //存在已经配对过的蓝牙设备
            for (Iterator<BluetoothDevice> it = device.iterator(); it.hasNext(); ) {
                BluetoothDevice btd = it.next();
                deviceList.add(btd.getName() + '\n' + btd.getAddress());
                adapter.notifyDataSetChanged();
            }
        } else {  //不存在已经配对过的蓝牙设备
            deviceList.add("No can be matched to use bluetooth");
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isRegister) {
            S.o("注册广播监听");
            isRegister = true;
            IntentFilter filterStart = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mydevice, filterStart);
            registerReceiver(mydevice, filterEnd);
        }
    }

    @Override
    protected void onDestroy() {
        if (blueadapter != null && blueadapter.isDiscovering()) {
            blueadapter.cancelDiscovery();
        }
        if (isRegister) {
            isRegister = false;
            unregisterReceiver(mydevice);
        }
        super.onDestroy();
    }

    class DeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {

                    S.o("找到设备:" + device.getName() + ":" + device.getAddress());
                    //搜索没有配过对的蓝牙设备
//                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    ParcelUuid[] uuids = device.getUuids();
                    String str = device.getName() + '\n' + device.getAddress();
                    if(uuids != null && uuids.length > 0){
                        str += '\n' + uuids[0].getUuid().toString();
                    }
                    deviceList.add(str);
                    adapter.notifyDataSetChanged();
//                    }
                }
            }
        }
    }
}
