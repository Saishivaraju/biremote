package com.example.btremote;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Phone acts as a Bluetooth HID remote (keyboard + media keys).
 * Works with devices that accept Bluetooth HID: Android TV, Google TV,
 * Fire TV, Apple TV, many smart TVs and smart projectors, PCs.
 * Requires Android 9+ (API 28).
 */
public class MainActivity extends Activity {

    // Report IDs
    private static final int ID_KEYBOARD = 1;
    private static final int ID_CONSUMER = 2;

    // Keyboard usages
    private static final byte K_RIGHT = 0x4F, K_LEFT = 0x50, K_DOWN = 0x51,
            K_UP = 0x52, K_ENTER = 0x28, K_ESC = 0x29;

    // Consumer usages
    private static final int C_POWER = 0x30, C_VOL_UP = 0xE9, C_VOL_DOWN = 0xEA,
            C_MUTE = 0xE2, C_PLAY_PAUSE = 0xCD, C_HOME = 0x223, C_BACK = 0x224;

    private static final byte[] DESCRIPTOR = new byte[]{
            // Keyboard, report ID 1
            0x05, 0x01, 0x09, 0x06, (byte) 0xA1, 0x01, (byte) 0x85, 0x01,
            0x05, 0x07, 0x19, (byte) 0xE0, 0x29, (byte) 0xE7, 0x15, 0x00, 0x25, 0x01,
            0x75, 0x01, (byte) 0x95, 0x08, (byte) 0x81, 0x02,
            (byte) 0x95, 0x01, 0x75, 0x08, (byte) 0x81, 0x01,
            (byte) 0x95, 0x06, 0x75, 0x08, 0x15, 0x00, 0x25, 0x65,
            0x05, 0x07, 0x19, 0x00, 0x29, 0x65, (byte) 0x81, 0x00,
            (byte) 0xC0,
            // Consumer control, report ID 2
            0x05, 0x0C, 0x09, 0x01, (byte) 0xA1, 0x01, (byte) 0x85, 0x02,
            0x15, 0x00, 0x26, (byte) 0xFF, 0x03, 0x19, 0x00, 0x2A, (byte) 0xFF, 0x03,
            0x75, 0x10, (byte) 0x95, 0x01, (byte) 0x81, 0x00,
            (byte) 0xC0
    };

    private BluetoothAdapter adapter;
    private BluetoothHidDevice hid;
    private BluetoothDevice target;
    private TextView status;
    private final Handler ui = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();

        if (Build.VERSION.SDK_INT >= 31
                && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        } else {
            initBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] p, int[] r) {
        if (r.length > 0 && r[0] == PackageManager.PERMISSION_GRANTED) initBluetooth();
        else status.setText("Bluetooth permission denied");
    }

    private void initBluetooth() {
        BluetoothManager bm = getSystemService(BluetoothManager.class);
        adapter = bm.getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            status.setText("Turn Bluetooth on, then reopen the app");
            return;
        }
        adapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                hid = (BluetoothHidDevice) proxy;
                registerHid();
            }

            @Override
            public void onServiceDisconnected(int profile) {
                hid = null;
            }
        }, BluetoothProfile.HID_DEVICE);
    }

    private void registerHid() {
        BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                "BT Remote", "Phone remote", "Me",
                BluetoothHidDevice.SUBCLASS1_COMBO, DESCRIPTOR);

        hid.registerApp(sdp, null, null, getMainExecutor(),
                new BluetoothHidDevice.Callback() {
                    @Override
                    public void onAppStatusChanged(BluetoothDevice d, boolean registered) {
                        status.setText(registered
                                ? "Ready. Tap Connect and pick your TV."
                                : "HID not registered (device may not support it)");
                    }

                    @Override
                    public void onConnectionStateChanged(BluetoothDevice d, int state) {
                        if (state == BluetoothProfile.STATE_CONNECTED) {
                            target = d;
                            status.setText("Connected: " + d.getName());
                        } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                            if (d.equals(target)) target = null;
                            status.setText("Disconnected");
                        }
                    }
                });
    }

    private void pickDevice() {
        if (hid == null || adapter == null) return;
        Set<BluetoothDevice> bonded = adapter.getBondedDevices();
        final List<BluetoothDevice> list = new ArrayList<>(bonded);
        String[] names = new String[list.size()];
        for (int i = 0; i < names.length; i++) names[i] = list.get(i).getName();

        new AlertDialog.Builder(this)
                .setTitle("Paired devices")
                .setItems(names, (dlg, which) -> hid.connect(list.get(which)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---- sending keys ----

    private void key(byte usage) {
        if (hid == null || target == null) return;
        hid.sendReport(target, ID_KEYBOARD, new byte[]{0, 0, usage, 0, 0, 0, 0, 0});
        ui.postDelayed(() -> {
            if (hid != null && target != null)
                hid.sendReport(target, ID_KEYBOARD, new byte[8]);
        }, 60);
    }

    private void consumer(int usage) {
        if (hid == null || target == null) return;
        hid.sendReport(target, ID_CONSUMER,
                new byte[]{(byte) (usage & 0xFF), (byte) ((usage >> 8) & 0xFF)});
        ui.postDelayed(() -> {
            if (hid != null && target != null)
                hid.sendReport(target, ID_CONSUMER, new byte[]{0, 0});
        }, 60);
    }

    // ---- UI (built in code, no XML needed) ----

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(24, 48, 24, 24);

        status = new TextView(this);
        status.setText("Starting...");
        status.setTextSize(16);
        root.addView(status);

        root.addView(btn("Connect", v -> pickDevice()));
        root.addView(row(
                btn("Power", v -> consumer(C_POWER)),
                btn("Home", v -> consumer(C_HOME)),
                btn("Back", v -> consumer(C_BACK))));
        root.addView(row(blank(), btn("Up", v -> key(K_UP)), blank()));
        root.addView(row(
                btn("Left", v -> key(K_LEFT)),
                btn("OK", v -> key(K_ENTER)),
                btn("Right", v -> key(K_RIGHT))));
        root.addView(row(blank(), btn("Down", v -> key(K_DOWN)), blank()));
        root.addView(row(
                btn("Vol -", v -> consumer(C_VOL_DOWN)),
                btn("Mute", v -> consumer(C_MUTE)),
                btn("Vol +", v -> consumer(C_VOL_UP))));
        root.addView(row(
                btn("Play/Pause", v -> consumer(C_PLAY_PAUSE)),
                btn("Esc", v -> key(K_ESC))));

        setContentView(root);
    }

    private Button btn(String t, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(t);
        b.setOnClickListener(l);
        b.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return b;
    }

    private View blank() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        return v;
    }

    private LinearLayout row(View... views) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        for (View v : views) r.addView(v);
        return r;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hid != null) {
            hid.unregisterApp();
            adapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hid);
        }
    }
}
