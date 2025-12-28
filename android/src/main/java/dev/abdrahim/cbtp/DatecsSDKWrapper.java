package dev.abdrahim.cbtp;

import com.getcapacitor.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Exception;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.reflect.Method;

import android.app.Application;
import android.app.Activity;
import android.util.Log;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.datecs.api.BuildInfo;
import com.datecs.api.printer.ProtocolAdapter;

public class DatecsSDKWrapper {
    private static final String LOG_TAG = "BluetoothPrinter";
    private Printer mPrinter;
    private ProtocolAdapter mProtocolAdapter;
    private BluetoothSocket mBluetoothSocket;
    private boolean mRestart;
    private String mAddress;
    private DatecsCallback mConnectCallback;
    private DatecsCallback mCallback;
    private Plugin mPlugin;
    private final Application app;

    public interface DatecsCallback {
        void onSuccess(Object result);
        void onError(Object error);
    }

    /**
     * Interface de eventos da Impressora
     */
    private final ProtocolAdapter.PrinterListener mChannelListener = new ProtocolAdapter.PrinterListener() {
        @Override
        public void onPaperStateChanged(boolean hasNoPaper) {
            if (hasNoPaper) {
                sendStatusUpdate(true, false);
            } else {
                sendStatusUpdate(true, true);
            }
        }

        @Override
        public void onThermalHeadStateChanged(boolean overheated) {
            if (overheated) {
                closeActiveConnections();
                sendStatusUpdate(false, false);
            }
        }

        @Override
        public void onBatteryStateChanged(boolean lowBattery) {
            sendStatusUpdate(true, true, lowBattery);
        }
    };

    private Map<Integer, String> errorCode = new HashMap<Integer, String>();

    public DatecsSDKWrapper(Plugin plugin) {
        mPlugin = plugin;
        app = plugin.getActivity().getApplication();

        this.errorCode.put(1, "Bluetooth adapter not available");
        this.errorCode.put(2, "No Bluetooth device found");
        this.errorCode.put(3, "The number of lines must be between 0 and 255");
        this.errorCode.put(4, "Error feeding paper to the printer");
        this.errorCode.put(5, "Error printing");
        this.errorCode.put(6, "Error fetching status");
        this.errorCode.put(7, "Error fetching temperature");
        this.errorCode.put(8, "Error printing barcode");
        this.errorCode.put(9, "Error printing test page");
        this.errorCode.put(10, "Error setting barcode settings");
        this.errorCode.put(11, "Error printing image");
        this.errorCode.put(12, "Error printing rectangle");
        this.errorCode.put(13, "Error printing rectangle");
        this.errorCode.put(14, "Error printing rectangle");
        this.errorCode.put(15, "Error printing rectangle");
        this.errorCode.put(16, "Error printing rectangle");
        this.errorCode.put(17, "Error printing rectangle");
        this.errorCode.put(18, "Failed to connect");
        this.errorCode.put(19, "Could not create Bluetooth socket");
        this.errorCode.put(20, "Failed to initialize");
        this.errorCode.put(21, "Could not write these bytes to this printer");
        this.errorCode.put(22, "Error printing QRcode");
    }

    private Map<String, Object> getErrorByCode(int code) {
        return this.getErrorByCode(code, null);
    }

    private Map<String, Object> getErrorByCode(int code, Exception exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", code);
        error.put("message", errorCode.get(code));
        if (exception != null) {
            error.put("exception", exception.getMessage());
        }
        return error;
    }

    /**
     * Busca todos os dispositivos Bluetooth pareados com o device
     *
     * @param callback
     */
    protected void getBluetoothPairedDevices(DatecsCallback callback) {
        BluetoothAdapter mBluetoothAdapter = null;
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                callback.onError(this.getErrorByCode(1));
                return;
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mPlugin.getActivity().startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                java.util.List<Map<String, Object>> deviceList = new java.util.ArrayList<>();
                for (BluetoothDevice device : pairedDevices) {
                    Map<String, Object> map = new HashMap<>();
                    int deviceType = 0;
                    try {
                        Method method = device.getClass().getMethod("getType");
                        if (method != null) {
                            deviceType = (Integer) method.invoke(device);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    map.put("type", deviceType);
                    map.put("address", device.getAddress());
                    map.put("name", device.getName());
                    String deviceAlias = device.getName();
                    try {
                        Method method = device.getClass().getMethod("getAliasName");
                        if (method != null) {
                            deviceAlias = (String) method.invoke(device);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    map.put("aliasName", deviceAlias);
                    deviceList.add(map);
                }
                callback.onSuccess(deviceList);
            } else {
                callback.onError(this.getErrorByCode(2));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
            callback.onError(e.getMessage());
        }
    }

    /**
     * Seta em memória o endereço da impressora cuja conexão está sendo estabelecida
     *
     * @param address
     */
    protected void setAddress(String address) {
        mAddress = address;
    }

    /**
     * Callback de cada requisição, que efetivamente recebe os retornos dos métodos
     *
     * @param callback
     */
    public void setCallback(DatecsCallback callback) {
        mCallback = callback;
    }

    /**
     * Valida o endereço da impressora e efetua a conexão
     *
     * @param callback
     */
    protected void connect(DatecsCallback callback) {
        mConnectCallback = callback;
        closeActiveConnections();
        if (BluetoothAdapter.checkBluetoothAddress(mAddress)) {
            establishBluetoothConnection(mAddress, callback);
        }
    }

    /**
     * Encerra todas as conexões com impressoras e dispositivos Bluetooth ativas
     */
    public synchronized void closeActiveConnections() {
        closePrinterConnection();
        closeBluetoothConnection();
    }

    /**
     * Encerra a conexão com a impressora
     */
    private synchronized void closePrinterConnection() {
        if (mPrinter != null) {
            mPrinter.close();
        }

        if (mProtocolAdapter != null) {
            mProtocolAdapter.close();
        }
    }

    /**
     * Finaliza o socket Bluetooth e encerra todas as conexões
     */
    private synchronized void closeBluetoothConnection() {
        BluetoothSocket socket = mBluetoothSocket;
        mBluetoothSocket = null;
        if (socket != null) {
            try {
                Thread.sleep(50);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Efetiva a conexão com o dispositivo Bluetooth
     *
     * @param address
     * @param callback
     */
    private void establishBluetoothConnection(final String address, final DatecsCallback callback) {
        final DatecsSDKWrapper sdk = this;
        mPlugin.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        BluetoothDevice device = adapter.getRemoteDevice(address);
                        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                        InputStream in = null;
                        OutputStream out = null;
                        adapter.cancelDiscovery();

                        try {
                            mBluetoothSocket = createBluetoothSocket(device, uuid, callback);
                            Thread.sleep(50);
                            mBluetoothSocket.connect();
                            in = mBluetoothSocket.getInputStream();
                            out = mBluetoothSocket.getOutputStream();
                        } catch (IOException e) {
                            //fallback
                            try {
                                mBluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device, 1);
                                Thread.sleep(50);
                                mBluetoothSocket.connect();
                                in = mBluetoothSocket.getInputStream();
                                out = mBluetoothSocket.getOutputStream();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                callback.onError(sdk.getErrorByCode(18, ex));
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onError(sdk.getErrorByCode(18, e));
                            return;
                        }

                        try {
                            initializePrinter(in, out, callback);
                            sendStatusUpdate(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            callback.onError(sdk.getErrorByCode(20));
                            return;
                        }
                    }
                });
                t.start();
            }
        });
    }

    /**
     * Cria um socket Bluetooth
     *
     * @param device
     * @param uuid
     * @param callback
     * @return BluetoothSocket
     * @throws IOException
     */
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, UUID uuid, final DatecsCallback callback) throws IOException {
        try {
            Method method = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
            return (BluetoothSocket) method.invoke(device, uuid);
        } catch (Exception e) {
            e.printStackTrace();
            sendStatusUpdate(false);
            callback.onError(this.getErrorByCode(19));
        }
        return device.createInsecureRfcommSocketToServiceRecord(uuid);
    }

    /**
     * Inicializa a troca de dados com a impressora
     * @param inputStream
     * @param outputStream
     * @param callback
     * @throws IOException
     */
    protected void initializePrinter(InputStream inputStream, OutputStream outputStream, DatecsCallback callback) throws IOException {
        mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);
        if (mProtocolAdapter.isProtocolEnabled()) {
            mProtocolAdapter.setPrinterListener(mChannelListener);
            
            final ProtocolAdapter.Channel channel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
            
            mPrinter = new Printer(channel.getInputStream(), channel.getOutputStream());
        } else {
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(), mProtocolAdapter.getRawOutputStream());
        }

        mPrinter.setConnectionListener(new Printer.ConnectionListener() {
            @Override
            public void onDisconnect() {
                sendStatusUpdate(false);
            }
        });
        callback.onSuccess(null);
    }

    /**
     * Print an image
     *
     * @param image String (BASE64 encoded image)
     * @param width
     * @param height
     * @param align
     */
    public void printImage(String image, int width, int height, int align) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            byte[] decodedByte = Base64.decode(image, 0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);

            if (bitmap == null) {
                throw new IllegalArgumentException("Decoded bitmap is null");
            }

            int imgWidth = bitmap.getWidth();
            int imgHeight = bitmap.getHeight();

            // Ensure width is within printer capability (max 384px, multiple of 8)
            int targetWidth = Math.min(384, imgWidth);
            targetWidth = targetWidth - (targetWidth % 8); // multiple of 8
            if (targetWidth <= 0) {
                targetWidth = 384;
            }

            if (imgWidth != targetWidth) {
                float scale = (float) targetWidth / (float) imgWidth;
                int targetHeight = Math.max(1, Math.round(imgHeight * scale));
                bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                imgWidth = bitmap.getWidth();
                imgHeight = bitmap.getHeight();
            }

            final int[] argb = new int[imgWidth * imgHeight];

            bitmap.getPixels(argb, 0, imgWidth, 0, 0, imgWidth, imgHeight);
            bitmap.recycle();

            // Use actual bitmap dimensions after scaling
            mPrinter.printImage(argb, imgWidth, imgHeight, align, true);
            mPrinter.flush();
            mCallback.onSuccess(null);
        } catch (Exception e) {
            e.printStackTrace();
            mCallback.onError(this.getErrorByCode(11, e));
        }
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param connection status
     */
    private void sendStatusUpdate(boolean isConnected, boolean hasPaper, boolean lowBattery) {
        // Status updates can be sent via LocalBroadcastManager if needed
        // For now, we'll skip this as it's not essential for the 3 core methods
    }
    
    private void sendStatusUpdate(boolean isConnected, boolean hasPaper) {
        this.sendStatusUpdate(isConnected, hasPaper, false);
    }

    private void sendStatusUpdate(boolean isConnected) {
        this.sendStatusUpdate(isConnected, true, false);
    }
}
