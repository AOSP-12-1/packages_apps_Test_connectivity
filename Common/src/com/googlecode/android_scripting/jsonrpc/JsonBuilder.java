/*
 * Copyright (C) 2010 Google Inc.
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

package com.googlecode.android_scripting.jsonrpc;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64Codec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseSettings;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Location;
import android.media.session.MediaSessionInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.telecomm.PhoneAccountHandle;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.SmsMessage;
import android.telephony.gsm.GsmCellLocation;
import android.util.DisplayMetrics;

import com.googlecode.android_scripting.ConvertUtils;
import com.googlecode.android_scripting.event.Event;

public class JsonBuilder {

    private JsonBuilder() {
        // This is a utility class.
    }

    @SuppressWarnings("unchecked")
    public static Object build(Object data) throws JSONException {
        if (data == null) {
            return JSONObject.NULL;
        }
        if (data instanceof Integer) {
            return data;
        }
        if (data instanceof Float) {
            return data;
        }
        if (data instanceof Double) {
            return data;
        }
        if (data instanceof Long) {
            return data;
        }
        if (data instanceof String) {
            return data;
        }
        if (data instanceof Boolean) {
            return data;
        }
        if (data instanceof JSONObject) {
            return data;
        }
        if (data instanceof JSONArray) {
            return data;
        }
        if (data instanceof Set<?>) {
            List<Object> items = new ArrayList<Object>((Set<?>) data);
            return buildJsonList(items);
        }
        if (data instanceof Collection<?>) {
          List<Object> items = new ArrayList<Object>((Collection<?>) data);
          return buildJsonList(items);
        }
        if (data instanceof List<?>) {
            return buildJsonList((List<?>) data);
        }
        if (data instanceof Address) {
            return buildJsonAddress((Address) data);
        }
        if (data instanceof Location) {
            return buildJsonLocation((Location) data);
        }
        if (data instanceof Bundle) {
            return buildJsonBundle((Bundle) data);
        }
        if (data instanceof Intent) {
            return buildJsonIntent((Intent) data);
        }
        if (data instanceof Event) {
            return buildJsonEvent((Event) data);
        }
        if (data instanceof Map<?, ?>) {
            // TODO(damonkohler): I would like to make this a checked cast if possible.
            return buildJsonMap((Map<String, ?>) data);
        }
        if (data instanceof ParcelUuid) {
          return data.toString();
        }
        if (data instanceof ScanResult) {
            return buildJsonScanResult((ScanResult) data);
        }
        if (data instanceof android.bluetooth.le.ScanResult) {
            return buildJsonBleScanResult((android.bluetooth.le.ScanResult) data);
        }
        if (data instanceof AdvertiseSettings) {
            return buildJsonBleAdvertiseSettings((AdvertiseSettings) data);
        }
        if (data instanceof BluetoothGattService) {
            return buildJsonBluetoothGattService((BluetoothGattService) data);
        }
        if (data instanceof BluetoothGattCharacteristic) {
            return buildJsonBluetoothGattCharacteristic((BluetoothGattCharacteristic) data);
        }
        if (data instanceof BluetoothGattDescriptor) {
            return buildJsonBluetoothGattDescriptor((BluetoothGattDescriptor) data);
        }
        if (data instanceof BluetoothDevice) {
            return buildJsonBluetoothDevice((BluetoothDevice) data);
        }
        if (data instanceof CellLocation) {
            return buildJsonCellLocation((CellLocation) data);
        }
        if (data instanceof WifiInfo) {
            return buildJsonWifiInfo((WifiInfo) data);
        }
        if (data instanceof NeighboringCellInfo) {
            return buildNeighboringCellInfo((NeighboringCellInfo) data);
        }
        if (data instanceof InetSocketAddress) {
            return buildInetSocketAddress((InetSocketAddress) data);
        }
        if (data instanceof Point) {
            return buildPoint((Point) data);
        }
        if (data instanceof SmsMessage) {
            return buildSmsMessage((SmsMessage) data);
        }
        if (data instanceof PhoneAccountHandle) {
            return buildPhoneAccountHandle((PhoneAccountHandle) data);
        }
        if (data instanceof MediaSessionInfo) {
            return buildMediaSessionInfo((MediaSessionInfo) data);
        }
        if (data instanceof DisplayMetrics) {
            return buildDisplayMetrics((DisplayMetrics) data);
        }
        if (data instanceof byte[]) {
            return Base64Codec.encodeBase64((byte[]) data);
        }
        if (data instanceof Object[]) {
            return buildJSONArray((Object[]) data);
        }
        return data.toString();
        // throw new JSONException("Failed to build JSON result. " + data.getClass().getName());
    }

    private static Object buildMediaSessionInfo(MediaSessionInfo data) throws JSONException {
        JSONObject info = new JSONObject();
        info.put("PackageName", data.getPackageName());
        info.put("Pid", data.getPid());
        info.put("Id", data.getId());
        return info;
    }

    private static Object buildJsonBluetoothDevice(BluetoothDevice data) throws JSONException {
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("address", data.getAddress());
        deviceInfo.put("state", data.getBondState());
        deviceInfo.put("name", data.getName());
        deviceInfo.put("type", data.getType());
        return deviceInfo;
    }

    private static JSONArray buildJSONArray(Object[] data) throws JSONException {
        JSONArray result = new JSONArray();
        for (Object o : data) {
            result.put(build(o));
        }
        return result;
    }

    private static Object buildInetSocketAddress(InetSocketAddress data) {
        JSONArray address = new JSONArray();
        address.put(data.getHostName());
        address.put(data.getPort());
        return address;
    }

    private static <T> JSONArray buildJsonList(final List<T> list) throws JSONException {
        JSONArray result = new JSONArray();
        for (T item : list) {
            result.put(build(item));
        }
        return result;
    }

    private static JSONObject buildJsonAddress(Address address) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("admin_area", address.getAdminArea());
        result.put("country_code", address.getCountryCode());
        result.put("country_name", address.getCountryName());
        result.put("feature_name", address.getFeatureName());
        result.put("phone", address.getPhone());
        result.put("locality", address.getLocality());
        result.put("postal_code", address.getPostalCode());
        result.put("sub_admin_area", address.getSubAdminArea());
        result.put("thoroughfare", address.getThoroughfare());
        result.put("url", address.getUrl());
        return result;
    }

    private static JSONObject buildJsonLocation(Location location) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("altitude", location.getAltitude());
        result.put("latitude", location.getLatitude());
        result.put("longitude", location.getLongitude());
        result.put("time", location.getTime());
        result.put("accuracy", location.getAccuracy());
        result.put("speed", location.getSpeed());
        result.put("provider", location.getProvider());
        result.put("bearing", location.getBearing());
        return result;
    }

    private static JSONObject buildJsonBundle(Bundle bundle) throws JSONException {
        JSONObject result = new JSONObject();
        for (String key : bundle.keySet()) {
            result.put(key, build(bundle.get(key)));
        }
        return result;
    }

    private static JSONObject buildJsonIntent(Intent data) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("data", data.getDataString());
        result.put("type", data.getType());
        result.put("extras", build(data.getExtras()));
        result.put("categories", build(data.getCategories()));
        result.put("action", data.getAction());
        ComponentName component = data.getComponent();
        if (component != null) {
            result.put("packagename", component.getPackageName());
            result.put("classname", component.getClassName());
        }
        result.put("flags", data.getFlags());
        return result;
    }

    private static JSONObject buildJsonEvent(Event event) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("name", event.getName());
        result.put("data", build(event.getData()));
        result.put("time", event.getCreationTime());
        return result;
    }

    private static JSONObject buildJsonMap(Map<String, ?> map) throws JSONException {
        JSONObject result = new JSONObject();
        for (Entry<String, ?> entry : map.entrySet()) {
            result.put(entry.getKey(), build(entry.getValue()));
        }
        return result;
    }

    private static JSONObject buildJsonScanResult(ScanResult scanResult) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("bssid", scanResult.BSSID);
        result.put("ssid", scanResult.SSID);
        result.put("frequency", scanResult.frequency);
        result.put("level", scanResult.level);
        result.put("capabilities", scanResult.capabilities);
        result.put("timestamp", scanResult.timestamp);
        // The following fields are hidden for now, uncomment when they're unhidden
        // result.put("seen", scanResult.seen);
        // result.put("distanceCm", scanResult.distanceCm);
        // result.put("distanceSdCm", scanResult.distanceSdCm);
        // if (scanResult.informationElements != null){
        // JSONArray infoEles = new JSONArray();
        // for(ScanResult.InformationElement ie : scanResult.informationElements) {
        // JSONObject infoEle = new JSONObject();
        // infoEle.put("id", ie.id);
        // infoEle.put("bytes", Base64Codec.encodeBase64(ie.bytes));
        // infoEles.put(infoEle);
        // }
        // result.put("InfomationElements", infoEles);
        // } else
        // result.put("InfomationElements", null);
        return result;
    }

    private static JSONObject buildJsonBleScanResult(android.bluetooth.le.ScanResult scanResult)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("rssi", scanResult.getRssi());
        result.put("timestampSeconds", scanResult.getTimestampNanos());
        result.put("scanRecord", build(ConvertUtils.convertByteArrayToString(
                scanResult.getScanRecord().getBytes())));
        result.put("deviceInfo", build(scanResult.getDevice()));
        return result;
    }

    private static JSONObject buildJsonBleAdvertiseSettings(AdvertiseSettings advertiseSettings)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("mode", advertiseSettings.getMode());
        result.put("txPowerLevel", advertiseSettings.getTxPowerLevel());
        result.put("isConnectable", advertiseSettings.getIsConnectable());
        return result;
    }

    private static Object buildJsonBluetoothGattService(BluetoothGattService data)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("instanceId", data.getInstanceId());
        result.put("type", data.getType());
        result.put("gattCharacteristicList", build(data.getCharacteristics()));
        result.put("includedServices", build(data.getIncludedServices()));
        result.put("uuid", data.getUuid().toString());
        return result;
    }

    private static Object buildJsonBluetoothGattCharacteristic(BluetoothGattCharacteristic data)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("instanceId", data.getInstanceId());
        result.put("permissions", data.getPermissions());
        result.put("properties", data.getProperties());
        result.put("writeType", data.getWriteType());
        result.put("descriptorsList", build(data.getDescriptors()));
        result.put("uuid", data.getUuid().toString());
        result.put("value", build(data.getValue()));

        return result;
    }

    private static Object buildJsonBluetoothGattDescriptor(BluetoothGattDescriptor data)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("instanceId", data.getInstanceId());
        result.put("permissions", data.getPermissions());
        result.put("characteristic", data.getCharacteristic());
        result.put("uuid", data.getUuid().toString());
        result.put("value", build(data.getValue()));
        return result;
    }

    private static Object buildPoint(Point data) throws JSONException {
        JSONObject point = new JSONObject();
        point.put("x", data.x);
        point.put("y", data.y);
        return point;
    }

    private static Object buildSmsMessage(SmsMessage data) throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("originatingAddress", data.getOriginatingAddress());
        msg.put("messageBody", data.getMessageBody());
        return msg;
    }

    private static Object buildPhoneAccountHandle(PhoneAccountHandle data) throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("id", data.getId());
        return msg;
    }

    private static Object buildDisplayMetrics(DisplayMetrics data) throws JSONException {
        JSONObject dm = new JSONObject();
        dm.put("widthPixels", data.widthPixels);
        dm.put("heightPixels", data.heightPixels);
        dm.put("noncompatHeightPixels", data.noncompatHeightPixels);
        dm.put("noncompatWidthPixels", data.noncompatWidthPixels);
        return dm;
    }

    private static JSONObject buildJsonCellLocation(CellLocation cellLocation)
        throws JSONException {
        JSONObject result = new JSONObject();
        if (cellLocation instanceof GsmCellLocation) {
            GsmCellLocation location = (GsmCellLocation) cellLocation;
            result.put("lac", location.getLac());
            result.put("cid", location.getCid());
        }
        // TODO(damonkohler): Add support for CdmaCellLocation. Not supported until API level 5.
        return result;
    }

    private static JSONObject buildJsonWifiInfo(WifiInfo data) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("hidden_ssid", data.getHiddenSSID());
        result.put("ip_address", data.getIpAddress());
        result.put("link_speed", data.getLinkSpeed());
        result.put("network_id", data.getNetworkId());
        result.put("rssi", data.getRssi());
        result.put("bssid", data.getBSSID());
        result.put("mac_address", data.getMacAddress());
        result.put("ssid", data.getSSID());
        String supplicantState = "";
        switch (data.getSupplicantState()) {
            case ASSOCIATED:
                supplicantState = "associated";
                break;
            case ASSOCIATING:
                supplicantState = "associating";
                break;
            case COMPLETED:
                supplicantState = "completed";
                break;
            case DISCONNECTED:
                supplicantState = "disconnected";
                break;
            case DORMANT:
                supplicantState = "dormant";
                break;
            case FOUR_WAY_HANDSHAKE:
                supplicantState = "four_way_handshake";
                break;
            case GROUP_HANDSHAKE:
                supplicantState = "group_handshake";
                break;
            case INACTIVE:
                supplicantState = "inactive";
                break;
            case INVALID:
                supplicantState = "invalid";
                break;
            case SCANNING:
                supplicantState = "scanning";
                break;
            case UNINITIALIZED:
                supplicantState = "uninitialized";
                break;
            default:
                supplicantState = null;
        }
        result.put("supplicant_state", build(supplicantState));
        return result;
    }

    private static JSONObject buildNeighboringCellInfo(NeighboringCellInfo data)
            throws JSONException {
        // TODO(damonkohler): Additional information available at API level 5.
        JSONObject result = new JSONObject();
        result.put("cid", data.getCid());
        result.put("rssi", data.getRssi());
        return result;
    }
}
