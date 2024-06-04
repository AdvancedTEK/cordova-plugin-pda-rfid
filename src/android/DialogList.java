package cordova.plugin.first.plugin;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import cordova.plugin.first.plugin.adapter.DialogListAdapter;

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.*;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class DialogList extends Dialog {

    private Context context;
    private RecyclerView recyclerView;
    public static DialogListAdapter dialogListAdapter;
    OnDialogResult onDialogResult;

    public static Button doneButton;
    public static Button closeButton;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public DialogList(Context context) {
        super(context);
        this.context = context;
        Log.d("hello", "DialogList constructor: " + context.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get dialog view
        super.onCreate(savedInstanceState != null ? savedInstanceState : new Bundle());
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
        setContentView(resources.getIdentifier("dialog_layout", "layout", packageName));
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        Log.d("hello", "DialogList onCreate: " + packageName);

        // create recyclerView & adapter
        dialogListAdapter = new DialogListAdapter(context);
        recyclerView = findViewById(resources.getIdentifier("recycler_view", "id", packageName));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(dialogListAdapter);
        Log.d("hello", "recyclerView onCreate: " + packageName);

        // init dialog buttons
        doneButton = findViewById(resources.getIdentifier("done", "id", packageName));
        closeButton = findViewById(resources.getIdentifier("close", "id", packageName));
        setButtonEnabled(doneButton, false);
        setButtonEnabled(closeButton, false);

        // set button onClick - 完成掃描
        doneButton.setOnClickListener(view -> handler.post(() -> {
            stopReader();
            Log.d("DialogList", "doneButton: " + dialogListAdapter.getEpcStringList());

            // pass data back to activity
            if (onDialogResult != null) {
                onDialogResult.finish(dialogListAdapter.getEpcStringList());
            }
            this.dismiss();
        }));

        // set button onClick - 清除
        closeButton.setOnClickListener(view -> handler.post(() -> {
            stopReader();
            dialogListAdapter.clearEpcStringList();
            setButtonEnabled(doneButton, false);
        }));

        // set button onClick - 關閉 dialog
        Button closeDialogButton = findViewById(resources.getIdentifier("closeDialog", "id", packageName));
        closeDialogButton.setOnClickListener(view -> handler.post(() -> {
            stopReader();
            dialogListAdapter.clearEpcStringList();
            this.dismiss();
        }));
    }

    private void stopReader() {
        if (PdaPlugin.reader != null && PdaPlugin.reading) {
            PdaPlugin.reader.stopReading();
            PdaPlugin.reading = false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("onKeyDown", "onKeyDown: " + keyCode);
        // keyDown 開始掃描
        if (keyCode == 293 && PdaPlugin.reader != null && !PdaPlugin.reading) {
            PdaPlugin.reading = true;
            PdaPlugin.reader.startReading();
            Log.d("DialogList", "start reading...");
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // keyUp 停止掃描
        if (keyCode == 293 && PdaPlugin.reader != null) {
            PdaPlugin.reader.stopReading();
            PdaPlugin.reading = false;
            Log.d("DialogList", "stop reading...");
        }
        return false;
    }

    public static void updateButtons(int epcListSize) {
        setButtonEnabled(doneButton, epcListSize == 1);
        setButtonEnabled(closeButton, epcListSize > 0);
    }

    public static void setButtonEnabled(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(enabled ? "#0A7EB8" : "#DFDFDF")
        ));
    }

    public void setDialogResult(OnDialogResult dialogResult) {
        onDialogResult = dialogResult;
    }

    public interface OnDialogResult {
        void finish(String result);
    }
}
