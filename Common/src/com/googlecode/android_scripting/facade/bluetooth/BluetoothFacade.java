/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting.facade.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.MainThread;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic Bluetooth functions.
 */
@RpcMinSdk(5)
public class BluetoothFacade extends RpcReceiver {
    private final Service mService;
    private final BroadcastReceiver mDiscoveryReceiver;
    private final IntentFilter discoveryFilter;
    private final EventFacade mEventFacade;
    private final BluetoothStateReceiver mStateReceiver;
    private Map<String, BluetoothConnection> connections =
            new HashMap<String, BluetoothConnection>();
    private BluetoothAdapter mBluetoothAdapter;

    public static ConcurrentHashMap<String, BluetoothDevice> DiscoveredDevices;

    public BluetoothFacade(FacadeManager manager) {
        super(manager);
        mBluetoothAdapter = MainThread.run(manager.getService(), new Callable<BluetoothAdapter>() {
            @Override
            public BluetoothAdapter call() throws Exception {
                return BluetoothAdapter.getDefaultAdapter();
            }
        });
        mEventFacade = manager.getReceiver(EventFacade.class);
        mService = manager.getService();

        DiscoveredDevices = new ConcurrentHashMap<String, BluetoothDevice>();
        discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mDiscoveryReceiver = new DiscoveryCacheReceiver();
        mStateReceiver = new BluetoothStateReceiver();
    }

    class DiscoveryCacheReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("Found device " + device.getAliasName());
                if (!DiscoveredDevices.containsKey(device.getAddress())) {
                    String name = device.getAliasName();
                    if (name != null) {
                        DiscoveredDevices.put(device.getAliasName(), device);
                    }
                    DiscoveredDevices.put(device.getAddress(), device);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mEventFacade.postEvent("BluetoothDiscoveryFinished", new Bundle());
                mService.unregisterReceiver(mDiscoveryReceiver);
            }
        }
    }

    class BluetoothStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // TODO: Keep track of the separate states be a separate method.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                Bundle msg = new Bundle();
                if (state == BluetoothAdapter.STATE_ON) {
                    msg.putString("State", "ON");
                    mEventFacade.postEvent("BluetoothStateChangedOn", msg);
                    mService.unregisterReceiver(mStateReceiver);
                } else if(state == BluetoothAdapter.STATE_OFF) {
                    msg.putString("State", "OFF");
                    mEventFacade.postEvent("BluetoothStateChangedOff", msg);
                    mService.unregisterReceiver(mStateReceiver);
                }
                msg.clear();
            }
        }
    }

    public static boolean deviceMatch(BluetoothDevice device, String deviceID) {
        return deviceID.equals(device.getAliasName()) || deviceID.equals(device.getAddress());
    }

    public static <T> BluetoothDevice getDevice(ConcurrentHashMap<String, T> devices, String device)
            throws Exception {
        if (devices.containsKey(device)) {
            return (BluetoothDevice) devices.get(device);
        } else {
            throw new Exception("Can't find device " + device);
        }
    }

    public static BluetoothDevice getDevice(Collection<BluetoothDevice> devices, String deviceID)
            throws Exception {
        Log.d("Looking for " + deviceID);
        for (BluetoothDevice bd : devices) {
            Log.d(bd.getAliasName() + " " + bd.getAddress());
            if (deviceMatch(bd, deviceID)) {
                Log.d("Found match " + bd.getAliasName() + " " + bd.getAddress());
                return bd;
            }
        }
        throw new Exception("Can't find device " + deviceID);
    }

    public static boolean deviceExists(Collection<BluetoothDevice> devices, String deviceID) {
        for (BluetoothDevice bd : devices) {
            if (deviceMatch(bd, deviceID)) {
                Log.d("Found match " + bd.getAliasName() + " " + bd.getAddress());
                return true;
            }
        }
        return false;
    }

    @Rpc(description = "Requests that the device be discoverable for Bluetooth connections.")
    public void bluetoothMakeDiscoverable(
            @RpcParameter(name = "duration",
                          description = "period of time, in seconds,"
                                      + "during which the device should be discoverable")
            @RpcDefault("300")
            Integer duration) {
        Log.d("Making discoverable for " + duration + " seconds.\n");
        mBluetoothAdapter
                .setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, duration);
    }

    @Rpc(description = "Requests that the device be not discoverable.")
    public void bluetoothMakeUndiscoverable() {
        Log.d("Making undiscoverable\n");
        mBluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
    }

    @Rpc(description = "Queries a remote device for it's name or null if it can't be resolved")
    public String bluetoothGetRemoteDeviceName(
            @RpcParameter(name = "address", description = "Bluetooth Address For Target Device")
            String address) {
        try {
            BluetoothDevice mDevice;
            mDevice = mBluetoothAdapter.getRemoteDevice(address);
            return mDevice.getName();
        } catch (Exception e) {
            return null;
        }
    }

    @Rpc(description = "Get local Bluetooth device name")
    public String bluetoothGetLocalName() {
        return mBluetoothAdapter.getName();
    }

    @Rpc(description = "Sets the Bluetooth visible device name", returns = "true on success")
    public boolean bluetoothSetLocalName(
        @RpcParameter(name = "name", description = "New local name")
        String name) {
        return mBluetoothAdapter.setName(name);
    }

    @Rpc(description = "Returns the hardware address of the local Bluetooth adapter. ")
    public String bluetoothGetLocalAddress() {
        return mBluetoothAdapter.getAddress();
    }

    @Rpc(description = "Returns the UUIDs supported by local Bluetooth adapter.")
    public ParcelUuid[] bluetoothGetLocalUuids() {
        return mBluetoothAdapter.getUuids();
    }

    @Rpc(description = "Gets the scan mode for the local dongle.\r\n" + "Return values:\r\n"
            + "\t-1 when Bluetooth is disabled.\r\n"
            + "\t0 if non discoverable and non connectable.\r\n"
            + "\r1 connectable non discoverable." + "\r3 connectable and discoverable.")
    public int bluetoothGetScanMode() {
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF
                || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
            return -1;
        }
        switch (mBluetoothAdapter.getScanMode()) {
            case BluetoothAdapter.SCAN_MODE_NONE:
                return 0;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                return 1;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                return 3;
            default:
                return mBluetoothAdapter.getScanMode() - 20;
        }
    }

    @Rpc(description = "Return the set of BluetoothDevice that are paired to the local adapter.")
    public Set<BluetoothDevice> bluetoothGetBondedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    @Rpc(description = "Checks Bluetooth state.", returns = "True if Bluetooth is enabled.")
    public Boolean bluetoothCheckState() {
        return mBluetoothAdapter.isEnabled();
    }

    @Rpc(description = "Toggle Bluetooth on and off.", returns = "True if Bluetooth is enabled.")
    public Boolean bluetoothToggleState(@RpcParameter(name = "enabled")
    @RpcOptional
    Boolean enabled,
            @RpcParameter(name = "prompt",
                          description = "Prompt the user to confirm changing the Bluetooth state.")
            @RpcDefault("false")
            Boolean prompt) {
        mService.registerReceiver(mStateReceiver,
                                  new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        if (enabled == null) {
            enabled = !bluetoothCheckState();
        }
        if (enabled) {
            // TODO(damonkohler): Make this synchronous as well.
            mBluetoothAdapter.enable();
        } else {
            // TODO(damonkohler): Add support for prompting on disable.
            // TODO(damonkohler): Make this synchronous as well.
            shutdown();
            mBluetoothAdapter.disable();
        }
        return enabled;
    }


    @Rpc(description = "Start the remote device discovery process. ",
         returns = "true on success, false on error")
    public Boolean bluetoothStartDiscovery() {
        mService.registerReceiver(mDiscoveryReceiver, discoveryFilter);
        return mBluetoothAdapter.startDiscovery();
    }

    @Rpc(description = "Cancel the current device discovery process.",
         returns = "true on success, false on error")
    public Boolean bluetoothCancelDiscovery() {
        mService.unregisterReceiver(mDiscoveryReceiver);
        return mBluetoothAdapter.cancelDiscovery();
    }

    @Rpc(description = "If the local Bluetooth adapter is currently"
                     + "in the device discovery process.")
    public Boolean bluetoothIsDiscovering() {
        return mBluetoothAdapter.isDiscovering();
    }

    @Rpc(description = "Get all the discovered bluetooth devices.")
    public Collection<BluetoothDevice> bluetoothGetDiscoveredDevices() {
        while (bluetoothIsDiscovering())
            ;
        return DiscoveredDevices.values();
    }

    @Rpc(description = "Enable or disable the Bluetooth HCI snoop log")
    public boolean bluetoothConfigHciSnoopLog(
            @RpcParameter(name = "value", description = "enable or disable log")
            Boolean value
            ) {
        return mBluetoothAdapter.configHciSnoopLog(value);
    }

    @Override
    public void shutdown() {
        for (Map.Entry<String, BluetoothConnection> entry : connections.entrySet()) {
            entry.getValue().stop();
        }
        connections.clear();
    }
}
