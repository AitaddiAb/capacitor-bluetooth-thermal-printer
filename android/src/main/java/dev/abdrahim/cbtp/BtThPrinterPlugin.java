package dev.abdrahim.cbtp;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

@CapacitorPlugin(name = "BtThPrinter")
public class BtThPrinterPlugin extends Plugin {

    private DatecsSDKWrapper datecsWrapper;

    @Override
    public void load() {
        super.load();
        datecsWrapper = new DatecsSDKWrapper(this);
    }

    @PluginMethod
    public void connect(PluginCall call) {
        String address = call.getString("address");
        if (address == null || address.isEmpty()) {
            call.reject("Address is required");
            return;
        }

        datecsWrapper.setAddress(address);
        datecsWrapper.connect(new DatecsSDKWrapper.DatecsCallback() {
            @Override
            public void onSuccess(Object result) {
                call.resolve();
            }

            @Override
            public void onError(Object error) {
                if (error instanceof Map) {
                    Map<String, Object> errorMap = (Map<String, Object>) error;
                    JSObject errorObj = new JSObject();
                    for (Map.Entry<String, Object> entry : errorMap.entrySet()) {
                        errorObj.put(entry.getKey(), entry.getValue());
                    }
                    call.reject((String) errorMap.get("message"), "", errorObj);
                } else {
                    call.reject(error != null ? error.toString() : "Connection failed");
                }
            }
        });
    }

    @PluginMethod
    public void printImage(PluginCall call) {
        String image = call.getString("image");
        Integer width = call.getInt("width", 384);
        Integer height = call.getInt("height", 0);
        Integer align = call.getInt("align", 1);

        if (image == null || image.isEmpty()) {
            call.reject("Image is required");
            return;
        }

        datecsWrapper.setCallback(new DatecsSDKWrapper.DatecsCallback() {
            @Override
            public void onSuccess(Object result) {
                call.resolve();
            }

            @Override
            public void onError(Object error) {
                if (error instanceof Map) {
                    Map<String, Object> errorMap = (Map<String, Object>) error;
                    JSObject errorObj = new JSObject();
                    for (Map.Entry<String, Object> entry : errorMap.entrySet()) {
                        errorObj.put(entry.getKey(), entry.getValue());
                    }
                    call.reject((String) errorMap.get("message"), "", errorObj);
                } else {
                    call.reject(error != null ? error.toString() : "Print failed");
                }
            }
        });

        datecsWrapper.printImage(image, width, height, align);
    }

    @PluginMethod
    public void listBluetoothDevices(PluginCall call) {
        datecsWrapper.getBluetoothPairedDevices(new DatecsSDKWrapper.DatecsCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof List) {
                    List<Map<String, Object>> devices = (List<Map<String, Object>>) result;
                    JSObject ret = new JSObject();
                    JSONArray devicesArray = new JSONArray();
                    for (Map<String, Object> device : devices) {
                        JSObject deviceObj = new JSObject();
                        for (Map.Entry<String, Object> entry : device.entrySet()) {
                            deviceObj.put(entry.getKey(), entry.getValue());
                        }
                        devicesArray.put(deviceObj);
                    }
                    ret.put("devices", devicesArray);
                    call.resolve(ret);
                } else {
                    JSObject ret = new JSObject();
                    ret.put("devices", new JSONArray());
                    call.resolve(ret);
                }
            }

            @Override
            public void onError(Object error) {
                JSObject ret = new JSObject();
                ret.put("devices", new JSONArray());
                call.resolve(ret);
            }
        });
    }
}
