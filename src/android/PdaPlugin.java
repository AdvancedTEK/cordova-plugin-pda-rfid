package cordova.plugin.first.plugin;

import android.content.Context;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.*;
import com.thingmagic.*;
import com.unitech.api.pogo.PogoCtrl;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import com.unitech.api.Key.KeySetting;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.*;

public class PdaPlugin extends CordovaPlugin {

    private Context context;
    private static CordovaInterface cordovaInterface;

    public PogoCtrl pogoCtrl = null;
    public String uartPath = null;
    public static Reader reader = null;
    private static boolean reading = false;
    private static boolean readerCreated = false;
    private static List<String> epcStringList;
    private Thread readingThread;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Toast logToast;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
        cordovaInterface = cordova;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext pluginCallback) throws JSONException {
        switch (action) {
            case "createReader": // 建立 RFID reader
                createReader(args.optInt(0, 3000), pluginCallback);
                return true;
            case "destroyReader": // 銷毀 RFID reader
                destroyReader(pluginCallback);
                return true;
            case "startReading": // 開始掃描
                startReading(pluginCallback);
                return true;
            case "readRfidTags": // 掃描到n個tag就結束
                readRfidTags(args.optInt(0, 5), args.optInt(1, 3000), pluginCallback);
                return true;
            case "stopReading": // 停止掃描
                stopReading(pluginCallback);
                return true;
            case "setScanMode": // 切換掃描模式
                setScanMode(args.optString(0, "barcode"), pluginCallback);
                return true;
            default:
                return false;
        }
    }

    // 切換掃描模式
    private void setScanMode(String keyMode, CallbackContext pluginCallback) {
        new Thread(() -> {

            try {
                KeySetting keySetting = KeySetting.getInstance(context);
                boolean mode = "RFID".equalsIgnoreCase(keyMode);
                // true: RFID, false: barcode scanner
                keySetting.setKeyRemapStatus(mode);
                keySetting.changeTriggerKeyMode(mode);

                boolean keyRemapStatus = keySetting.getKeyRemapStatus().getBoolean(KeySetting.BUNDLE_KEY_REMAPPING);
                String RFIDTriggerKeyCode = keySetting.getRFIDTriggerKeyCode().getString(KeySetting.BUNDLE_KEY_CODE);
                String ScannerTriggerKeyCode = keySetting.getScannerTriggerKeyCode().getString(KeySetting.BUNDLE_KEY_CODE);
                String TriggerKeyCode = keySetting.getTriggerKeyCode().getString(KeySetting.BUNDLE_KEY_CODE);

                String message = String.format("KeyRemapStatus=%s\nRFIDTriggerKeyCode=%s\nScannerTriggerKeyCode=%s\nTriggerKeyCode=%s",
                        keyRemapStatus ? "RFID" : "Scanner", RFIDTriggerKeyCode, ScannerTriggerKeyCode, TriggerKeyCode);
                Log.d("test", "setScanMode: \n" + message);
                pluginCallback.success(keyRemapStatus ? "RFID" : "Scanner");
            } catch (Exception e) {
                pluginCallback.error(e.getMessage());
            }

        }).start();
    }

    // 建立 RFID reader
    public void createReader(int power, CallbackContext pluginCallback) {
        epcStringList = new ArrayList<>();

        new Thread(() -> {
            Log.d("test", "createReader: Reader Initializing...");

            try {
                if (pogoCtrl == null) {
                    pogoCtrl = PogoCtrl.getInstance(context);
                    Log.d("test", "createReader pogoCtrl: " + pogoCtrl);
                }
                if (uartPath == null) {
                    uartPath = pogoCtrl.getUartPath().getString("UART");
                    Log.d("test", "createReader uartPath: " + uartPath);
                }
                pogoCtrl.powerOff();
                Log.d("test", "createReader: powerOff");
                SystemClock.sleep(100);
                pogoCtrl.powerOn();
                Log.d("test", "createReader: powerOn");
                SystemClock.sleep(300);

                Reader.setSerialTransport("ute", new SerialTransportRS232.Factory());
                reader = Reader.create(uartPath);
                Log.d("test", "createReader: reader created");
                reader.connect();
                Log.d("test", "createReader: reader connected");

                byte length = Integer.valueOf("6").byteValue();
                Gen2.ReadData tagRead = new Gen2.ReadData(Gen2.Bank.getBank(1), 2, length);
                int iPass = Integer.parseUnsignedInt("00000000", 16);
                Gen2.Password password = new Gen2.Password(iPass);
                Log.d("test", String.format("[byteLength:%s, tagRead:%s, iPass:%s, password:%s]", length, tagRead, iPass, password));

                reader.paramSet(TMConstants.TMR_PARAM_GEN2_ACCESSPASSWORD, password);
                reader.paramSet("/reader/region/id", Reader.Region.valueOf("NA"));
                reader.paramSet("/reader/radio/readPower", power); // ********* 讀取功率 (單位：centi-dBm)
                reader.paramSet("/reader/commandTimeout", 1000);
                reader.paramSet("/reader/gen2/BLF", Gen2.LinkFrequency.LINK250KHZ);
                reader.paramSet("/reader/gen2/session", Gen2.Session.S1);
                reader.paramSet("/reader/gen2/tagEncoding", Gen2.TagEncoding.M8);
                reader.paramSet("/reader/gen2/target", Gen2.Target.A);
                reader.paramSet("/reader/gen2/q", new Gen2.DynamicQ());
                reader.paramSet("/reader/gen2/tari", Gen2.Tari.TARI_25US);
                reader.paramSet("/reader/read/asyncOnTime", 300);
                reader.paramSet("/reader/read/asyncOffTime", 300);
                reader.paramSet("/reader/status/temperatureEnable", true);
                reader.paramSet("/reader/tagop/antenna", 1);
                reader.paramSet(TMConstants.TMR_PARAM_READ_PLAN, new SimpleReadPlan(new int[]{1}, TagProtocol.GEN2, false));
                String[] paramArray = new String[]{"/reader/region/id", "/reader/radio/readPower", "/reader/commandTimeout", "/reader/gen2/BLF", "/reader/gen2/session", "/reader/gen2/tagEncoding", "/reader/gen2/target", "/reader/gen2/q", "/reader/gen2/tari", "/reader/read/asyncOnTime", "/reader/read/asyncOffTime", "/reader/status/temperatureEnable", "/reader/tagop/antenna"};
                for (String param : paramArray) {
                    Log.d("test", String.format("set parameter: %-33s %s", param, String.valueOf(reader.paramGet(param))));
                }

                reader.addReadExceptionListener(readExceptionListener);
                Log.d("test", "add read exception listener");
                reader.addReadListener(readListener);
                Log.d("test", "add read listener");
                reader.addStatusListener(statusListener);
                Log.d("test", "add status listener");

            } catch (Exception e) {
                Log.e("test", "run: ", e);
                pluginCallback.error(e.getMessage());
            }

            readerCreated = true;
            Log.d("test", "createReader: Reader Initialized!");
            pluginCallback.success("success");

        }).start();
    }

    // 開始掃描
    public void startReading(CallbackContext pluginCallback) {
        if (!readerCreated) {
            pluginCallback.error("Reader還沒建完");
            return;
        }

        reading = true;
        reader.startReading();
        pluginCallback.success("success");
    }

    // 掃描到n個tag就結束
    public void readRfidTags(int tagsToRead, int timeout, CallbackContext pluginCallback) {
        Log.d("test", "readRfidTags: " + tagsToRead + " / " + timeout);
        if (!readerCreated) {
            pluginCallback.error("Reader還沒建完");
            return;
        }

        readingThread = new Thread(() -> {
            toast("開始掃描...");
            long start = System.currentTimeMillis();
            reader.startReading();
            reading = true;

            while (epcStringList.size() < tagsToRead) {
                if (readingThread.isInterrupted()) {
                    break;
                }
                Log.d("test", "readRfidTags: reading...");
                if (System.currentTimeMillis() - start > timeout) { // 超過 timeout 時間後直接 return
                    if (epcStringList.size() > 0) {
                        break;
                    }

                    reader.stopReading();
                    reading = false;
                    epcStringList.clear();
                    pluginCallback.error("沒辦法在 " + timeout / 1000.0 + " 秒內掃描到 RFID tag");
                    return;
                }
            }

            if (!readingThread.isInterrupted()) {
                this.stopReading(pluginCallback);
            }
        });
        readingThread.start();
    }

    // 停止掃描
    public void stopReading(CallbackContext pluginCallback) {
        if (!readerCreated) {
            pluginCallback.error("Reader還沒建完");
            return;
        }

        reading = false;
        reader.stopReading();
        if (Objects.nonNull(readingThread)) {
            readingThread.interrupt();
        }
        Log.d("test", "stopReading: " + epcStringList.toString().substring(1, epcStringList.toString().length() - 1));
        pluginCallback.success(epcStringList.toString());
        epcStringList.clear();
    }

    public void destroyReader(CallbackContext pluginCallback) {
        if (!readerCreated) {
            pluginCallback.error("Reader還沒建完");
            return;
        }
        reader.destroy();
        Log.d("test", "destroyReader: Reader destroyed!");
        pluginCallback.success("success");
    }

    public static ReadListener readListener = (reader, tagReadData) -> {
        String epcString = tagReadData.getTag().epcString();
        if (epcString != null && reading) {
            if (epcStringList.contains(epcString)) {
//                epcStringList.add(epcString + " " + Instant.now().getEpochSecond());
                Log.d("test", "=== tagRead ===  " + epcString + "  " + tagReadData.getRssi() + " (重複掃描)");
            } else {
                epcStringList.add(epcString);
                Log.d("test", "=== tagRead ===  " + epcString + "  " + tagReadData.getRssi());
            }
        }
    };

    public static ReadExceptionListener readExceptionListener = (reader, e) -> {
        Log.e("test", "tagReadException: " + e.getTagReads() + " / " + e.getMessage());
    };

    public static StatusListener statusListener = (reader, statusReports) -> {
//        Log.d("test", "statusMessage: " + Arrays.toString(statusReports));
    };

    public void toast(String message) {
        handler.post(() -> {
            Toast.makeText(cordovaInterface.getActivity(), message, Toast.LENGTH_SHORT).show();
        });
    }

}
