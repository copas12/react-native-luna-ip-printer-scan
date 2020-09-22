package com.lunalibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import android.util.Log;

import static com.facebook.react.common.ReactConstants.TAG;

public class LunaIpPrinterScanModule extends ReactContextBaseJavaModule {
    public static List<String> DSLITE_LIST = Arrays.asList("192.0.0.0", "192.0.0.1", "192.0.0.2", "192.0.0.3", "192.0.0.4", "192.0.0.5", "192.0.0.6", "192.0.0.7");
    final ExecutorService executor = Executors.newFixedThreadPool(20);

    private final ReactApplicationContext reactContext;

    public LunaIpPrinterScanModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "LunaIpPrinterScan";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }


    @ReactMethod
    public void scan() throws ExecutionException, InterruptedException {
        try {
            Log.d(TAG, "SCAN IP");
            String deviceIp = getIPV4Address().get();
            Log.d(TAG, "DEVICE IP: " + deviceIp);
            final ExecutorService es = Executors.newFixedThreadPool(50);
            final WritableArray printers = new WritableNativeArray();
            String[] ipPart = deviceIp.split("\\.");
            String subnetOri = ipPart[0] + "." + ipPart[1] + "." + ipPart[2];
            final int ipPart2Int = Integer.parseInt(ipPart[2]);
            String subnetMinOne = null;
            ArrayList<String> ips = new ArrayList<String>();
            ips.add(subnetOri);

            if (ipPart2Int > 0) {
                subnetMinOne = ipPart[0] + "." + ipPart[1] + "." + Integer.toString((ipPart2Int - 1));
                ips.add(subnetMinOne);
            }

            String subnetPlusOne = null;
            if (ipPart2Int < 255) {
                subnetPlusOne = ipPart[0] + "." + ipPart[1] + "." + Integer.toString((ipPart2Int + 1));
                ips.add(subnetPlusOne);
            }

            final int timeout = 300;
            final List<Future<WritableNativeMap>> futures = new ArrayList<>();

            for (int counter = 0; counter < ips.size(); counter++) { 
                String s = ips.get(counter);
                for (int i = 0; i <= 255; i++) {
                    String ip = s + "." + i;
                    Log.d(TAG, "IP TO SCAN: " + ip);
                    futures.add(portIsOpen(es, ip, 9100, timeout));
                }
            } 
          
            es.shutdown();
            for (final Future<WritableNativeMap> f : futures) {
                try {
                    if (f.get().getBoolean("open")) {
                        WritableMap params = Arguments.createMap(); // add here the data you want to send
                        params.putString("ip", f.get().getString("ip")); // <- example
                        Log.d(TAG, "OPEN IP: " + f.get().getString("ip"));

                        sendEvent(reactContext, "IpPrinter", params );
                    }
                } catch (ExecutionException e) {
//              e.printStackTrace();
                } catch (InterruptedException e) {
//              e.printStackTrace();
                }
            }
           
        } catch (ExecutionException e) {
            
//              e.printStackTrace();
        } catch (InterruptedException e) {
                // e.printStackTrace();
        }
    }

    public static Future<WritableNativeMap> portIsOpen(
        final ExecutorService es,
        final String ip,
        final int port,
        final int timeout
            ) {
            return es.submit(new Callable<WritableNativeMap>() {
                public WritableNativeMap call() {
                    WritableNativeMap result = new WritableNativeMap();
                    result.putString("ip", ip);
                    result.putBoolean("open", false);
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ip, port), timeout);
                        socket.close();
                        result.putBoolean("open", true);
                        return result;
                    } catch (Exception ex) {
                        return result;
                    }
                }
            });
    }

    private void sendEvent(
        ReactContext reactContext,
        String eventName,
        WritableMap params) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    @ReactMethod
    public Future<String> getIPV4Address() {
        return executor.submit(new Callable<String>() {
            public String call() {
                String ipAddress = "0.0.0.0";
                String tmp = "0.0.0.0";

                for (InterfaceAddress address : getInetAddresses()) {
                    if (!address.getAddress().isLoopbackAddress() && address.getAddress() instanceof Inet4Address) {
                        tmp = address.getAddress().getHostAddress().toString();
                        if (!inDSLITERange(tmp)) {
                            ipAddress = tmp;
                        }
                    }
                }
                return ipAddress;
            }
        });
    }


    private Boolean inDSLITERange(String ip) {
        // Fixes issue https://github.com/pusherman/react-native-network-info/issues/43
        // Based on comment https://github.com/pusherman/react-native-network-info/issues/43#issuecomment-358360692
        // added this check in getIPAddress and getIPV4Address
        return LunaIpPrinterScanModule.DSLITE_LIST.contains(ip);
    }


    private List<InterfaceAddress> getInetAddresses() {
        List<InterfaceAddress> addresses = new ArrayList<>();
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();

                for (InterfaceAddress interface_address : intf.getInterfaceAddresses()) {
                    addresses.add(interface_address);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
        return addresses;
    }
}
