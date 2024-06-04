package cordova.plugin.first.plugin;

import android.content.Context;

import android.os.SystemClock;
import com.thingmagic.*;
import com.unitech.api.pogo.PogoCtrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import com.unitech.api.Key.KeySetting;

import org.json.JSONArray;
import org.json.JSONException;

public class PdaPlugin extends CordovaPlugin {

    private CallbackContext callbackContext;
    private static CordovaInterface cordovaInterface;
    private Context context;

    public PogoCtrl pogoCtrl = null;
    public String uartPath = null;
    public static Reader reader = null;
    public static boolean reading = false;
    public DialogList dialogList;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
        cordovaInterface = cordova;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        switch (action) {
            case "setScanMode":
                setScanMode(args.getString(0), callbackContext);
                return true;
            case "scan":
                openDialog(callbackContext);
                return true;
            case "createReader":
                createReader(args.optInt(0, 3000), callbackContext);
                return true;
            default:
                return false;
        }
    }

    private void openDialog(CallbackContext callbackContext) {
        handler.post(() -> {
            try {
                dialogList = new DialogList(cordova.getActivity()) {
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                    }
                };

                // 接 dialog 傳回來的資料
                dialogList.setDialogResult(new DialogList.OnDialogResult() {
                    @Override
                    public void finish(String result) {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);
                    }
                });

                dialogList.show();

            } catch (Exception e) {
                callbackContext.error("ERROR" + e);
            }
        });
    }

    // 建立 RFID reader
    public void createReader(int power, CallbackContext pluginCallback) {
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

            Log.d("test", "createReader: Reader Initialized!");
            pluginCallback.success("success");

        }).start();
    }

    private static final ReadListener readListener = (reader, data) -> {
        handler.post(() -> {
            DialogList.dialogListAdapter.add(data.getTag().epcString());
        });
        Log.d("test", "=== tagRead ===  " + data.getTag().epcString() + "  " + data.getRssi() + " |  count:  " + data.getReadCount());
    };

    private static final ReadExceptionListener readExceptionListener = (reader, e) -> {
        Log.e("test", "readExceptionListener: " + e.getTagReads() + " / " + e.getMessage());
    };

    private static final StatusListener statusListener = (reader, statusReports) -> {
        // Log.d("test", "statusMessage: " + Arrays.toString(statusReports));
    };

    private void setScanMode(String keyMode, CallbackContext callbackContext) {
        new Thread(() -> {
            try {
                KeySetting keySetting = KeySetting.getInstance(context);
                boolean mode = "RFID".equalsIgnoreCase(keyMode);
                keySetting.setKeyRemapStatus(mode);
                keySetting.changeTriggerKeyMode(mode);

                boolean keyRemapStatus = keySetting.getKeyRemapStatus().getBoolean(KeySetting.BUNDLE_KEY_REMAPPING);
                String RFIDTriggerKeyCode = keySetting.getRFIDTriggerKeyCode().getString(KeySetting.BUNDLE_KEY_CODE);
                String ScannerTriggerKeyCode = keySetting.getScannerTriggerKeyCode().getString(KeySetting.BUNDLE_KEY_CODE);
                String TriggerKeyCode = keySetting.getTriggerKeyCode().getString(KeySetting.BUNDLE_KEY_CODE);

                String message = String.format("KeyRemapStatus=%s\nRFIDTriggerKeyCode=%s\nScannerTriggerKeyCode=%s\nTriggerKeyCode=%s",
                        keyRemapStatus ? "RFID" : "Scanner", RFIDTriggerKeyCode, ScannerTriggerKeyCode, TriggerKeyCode);
                callbackContext.success(message);
                toast("已切換掃描模式為" + (keyRemapStatus ? "RFID" : "Scanner"));

            } catch (Exception e) {
                callbackContext.error("ERROR" + e);
            }
        }).start();
    }

    public void toast(String message) {
        Toast.makeText(cordova.getActivity(), message, Toast.LENGTH_SHORT).show();
    }

}
