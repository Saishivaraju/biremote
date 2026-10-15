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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Phone acts as a Bluetooth HID remote:
 * - Keyboard + text typing
 * - Consumer media keys
 * - Touchpad / Mouse control with clicks and scroll
 * Works with Android TV, Google TV, Fire TV, Apple TV, PC, Mac.
 * Requires Android 9+ (API 28).
 */
public class MainActivity extends Activity {

    // Report IDs
    private static final int ID_KEYBOARD = 1;
    private static final int ID_CONSUMER = 2;
    private static final int ID_MOUSE = 3;

    // Keyboard usages
    private static final byte K_RIGHT = 0x4F, K_LEFT = 0x50, K_DOWN = 0x51,
            K_UP = 0x52, K_ENTER = 0x28, K_ESC = 0x29, K_BACKSPACE = 0x2A,
            K_TAB = 0x2B, K_SPACE = 0x2C, K_DELETE = 0x4C;

    // Consumer usages
    private static final int C_POWER = 0x30, C_VOL_UP = 0xE9, C_VOL_DOWN = 0xEA,
            C_MUTE = 0xE2, C_PLAY_PAUSE = 0xCD, C_HOME = 0x223, C_BACK = 0x224;

    private static final byte[] DESCRIPTOR = new byte[]{
            // Keyboard, report ID 1
            0x05, 0x01,                    // Usage Page (Generic Desktop)
            0x09, 0x06,                    // Usage (Keyboard)
            (byte) 0xA1, 0x01,             // Collection (Application)
            (byte) 0x85, 0x01,             //   Report ID (1)
            0x05, 0x07,                    //   Usage Page (Key Codes)
            0x19, (byte) 0xE0,             //   Usage Minimum (224)
            0x29, (byte) 0xE7,             //   Usage Maximum (231)
            0x15, 0x00,                    //   Logical Minimum (0)
            0x25, 0x01,                    //   Logical Maximum (1)
            0x75, 0x01,                    //   Report Size (1)
            (byte) 0x95, 0x08,             //   Report Count (8)
            (byte) 0x81, 0x02,             //   Input (Data, Variable, Absolute) - modifier keys
            (byte) 0x95, 0x01,             //   Report Count (1)
            0x75, 0x08,                    //   Report Size (8)
            (byte) 0x81, 0x01,             //   Input (Constant) - reserved byte
            (byte) 0x95, 0x06,             //   Report Count (6)
            0x75, 0x08,                    //   Report Size (8)
            0x15, 0x00,                    //   Logical Minimum (0)
            0x25, 0x65,                    //   Logical Maximum (101)
            0x05, 0x07,                    //   Usage Page (Key Codes)
            0x19, 0x00,                    //   Usage Minimum (0)
            0x29, 0x65,                    //   Usage Maximum (101)
            (byte) 0x81, 0x00,             //   Input (Data, Array) - 6 keycodes
            (byte) 0xC0,                   // End Collection

            // Consumer control, report ID 2
            0x05, 0x0C,                    // Usage Page (Consumer Devices)
            0x09, 0x01,                    // Usage (Consumer Control)
            (byte) 0xA1, 0x01,             // Collection (Application)
            (byte) 0x85, 0x02,             //   Report ID (2)
            0x15, 0x00,                    //   Logical Minimum (0)
            0x26, (byte) 0xFF, 0x03,       //   Logical Maximum (1023 / 0x03FF)
            0x19, 0x00,                    //   Usage Minimum (0)
            0x2A, (byte) 0xFF, 0x03,       //   Usage Maximum (1023 / 0x03FF)
            0x75, 0x10,                    //   Report Size (16)
            (byte) 0x95, 0x01,             //   Report Count (1)
            (byte) 0x81, 0x00,             //   Input (Data, Array)
            (byte) 0xC0,                   // End Collection

            // Mouse, report ID 3
            0x05, 0x01,                    // Usage Page (Generic Desktop)
            0x09, 0x02,                    // Usage (Mouse)
            (byte) 0xA1, 0x01,             // Collection (Application)
            (byte) 0x85, 0x03,             //   Report ID (3)
            0x09, 0x01,                    //   Usage (Pointer)
            (byte) 0xA1, 0x00,             //   Collection (Physical)
            // 3 Buttons
            0x05, 0x09,                    //     Usage Page (Buttons)
            0x19, 0x01,                    //     Usage Minimum (1)
            0x29, 0x03,                    //     Usage Maximum (3)
            0x15, 0x00,                    //     Logical Minimum (0)
            0x25, 0x01,                    //     Logical Maximum (1)
            (byte) 0x95, 0x03,             //     Report Count (3)
            0x75, 0x01,                    //     Report Size (1)
            (byte) 0x81, 0x02,             //     Input (Data, Variable, Absolute)
            // 5 bits padding
            (byte) 0x95, 0x01,             //     Report Count (1)
            0x75, 0x05,                    //     Report Size (5)
            (byte) 0x81, 0x01,             //     Input (Constant)
            // X and Y
            0x05, 0x01,                    //     Usage Page (Generic Desktop)
            0x09, 0x30,                    //     Usage (X)
            0x09, 0x31,                    //     Usage (Y)
            0x15, (byte) 0x81,             //     Logical Minimum (-127)
            0x25, 0x7F,                    //     Logical Maximum (127)
            0x75, 0x08,                    //     Report Size (8)
            (byte) 0x95, 0x02,             //     Report Count (2)
            (byte) 0x81, 0x06,             //     Input (Data, Variable, Relative)
            // Wheel scroll
            0x09, 0x38,                    //     Usage (Wheel)
            0x15, (byte) 0x81,             //     Logical Minimum (-127)
            0x25, 0x7F,                    //     Logical Maximum (127)
            0x75, 0x08,                    //     Report Size (8)
            (byte) 0x95, 0x01,             //     Report Count (1)
            (byte) 0x81, 0x06,             //     Input (Data, Variable, Relative)
            (byte) 0xC0,                   //   End Collection
            (byte) 0xC0                    // End Collection
    };

    private BluetoothAdapter adapter;
    private BluetoothHidDevice hid;
    private BluetoothDevice target;
    private TextView status;
    private final Handler ui = new Handler(Looper.getMainLooper());

    // UI mode containers
    private View remoteContainer;
    private View trackpadContainer;
    private View keyboardContainer;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();

        if (Build.VERSION.SDK_INT >= 31) {
            List<String> perms = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (!perms.isEmpty()) {
                requestPermissions(perms.toArray(new String[0]), 1);
            } else {
                initBluetooth();
            }
        } else {
            initBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] p, int[] r) {
        boolean allGranted = true;
        for (int res : r) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            initBluetooth();
        } else {
            status.setText("Bluetooth permissions denied. Please allow Nearby devices / Bluetooth.");
        }
    }

    private void initBluetooth() {
        BluetoothManager bm = getSystemService(BluetoothManager.class);
        adapter = bm != null ? bm.getAdapter() : BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            status.setText("Turn Bluetooth on, then reopen or tap Retry");
            return;
        }
        status.setText("Connecting Bluetooth HID service...");
        adapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                hid = (BluetoothHidDevice) proxy;
                ui.postDelayed(() -> registerHid(), 300);
            }

            @Override
            public void onServiceDisconnected(int profile) {
                hid = null;
                ui.post(() -> status.setText("HID service disconnected"));
            }
        }, BluetoothProfile.HID_DEVICE);
    }

    private void registerHid() {
        if (hid == null) return;
        try {
            hid.unregisterApp();
        } catch (Exception ignored) {}

        BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                "BT Remote", "Phone remote", "Me",
                BluetoothHidDevice.SUBCLASS1_NONE, DESCRIPTOR);

        boolean initiated = hid.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(),
                new BluetoothHidDevice.Callback() {
                    @Override
                    public void onAppStatusChanged(BluetoothDevice d, boolean registered) {
                        ui.post(() -> {
                            if (registered) {
                                status.setText("Ready. Tap Connect and pick your TV.");
                            } else {
                                status.setText("HID not registered. Tap 'Retry Registration'. If still failing, check Xiaomi App Info > Other permissions.");
                            }
                        });
                    }

                    @Override
                    public void onConnectionStateChanged(BluetoothDevice d, int state) {
                        ui.post(() -> {
                            if (state == BluetoothProfile.STATE_CONNECTED) {
                                target = d;
                                String name = d.getName() != null ? d.getName() : d.getAddress();
                                status.setText("Connected: " + name);
                            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                                if (d.equals(target)) target = null;
                                status.setText("Disconnected");
                            }
                        });
                    }
                });

        if (!initiated) {
            ui.post(() -> status.setText("registerApp failed. Tap 'Retry Registration'."));
        }
    }

    private void pickDevice() {
        if (hid == null || adapter == null) return;
        Set<BluetoothDevice> bonded = adapter.getBondedDevices();
        final List<BluetoothDevice> list = new ArrayList<>(bonded);
        String[] names = new String[list.size()];
        for (int i = 0; i < names.length; i++) {
            String name = list.get(i).getName();
            names[i] = (name != null && !name.isEmpty()) ? name : list.get(i).getAddress();
        }

        new AlertDialog.Builder(this)
                .setTitle("Paired devices")
                .setItems(names, (dlg, which) -> hid.connect(list.get(which)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---- sending keys & text ----

    private void key(byte usage) {
        sendKey(usage, (byte) 0);
    }

    private void sendKey(byte usage, byte modifier) {
        if (hid == null || target == null) return;
        hid.sendReport(target, ID_KEYBOARD, new byte[]{modifier, 0, usage, 0, 0, 0, 0, 0});
        ui.postDelayed(() -> {
            if (hid != null && target != null)
                hid.sendReport(target, ID_KEYBOARD, new byte[8]);
        }, 40);
    }

    private void sendString(String text) {
        if (text == null || text.isEmpty() || hid == null || target == null) return;
        new Thread(() -> {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                sendChar(c);
                try {
                    Thread.sleep(40);
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void sendChar(char c) {
        byte modifier = 0;
        byte keycode = 0;

        if (c >= 'a' && c <= 'z') {
            keycode = (byte) (0x04 + (c - 'a'));
        } else if (c >= 'A' && c <= 'Z') {
            modifier = 0x02; // Left Shift
            keycode = (byte) (0x04 + (c - 'A'));
        } else if (c >= '1' && c <= '9') {
            keycode = (byte) (0x1E + (c - '1'));
        } else if (c == '0') {
            keycode = 0x27;
        } else {
            switch (c) {
                case ' ': keycode = 0x2C; break;
                case '\n': keycode = 0x28; break;
                case '\t': keycode = 0x2B; break;
                case '-': keycode = 0x2D; break;
                case '_': modifier = 0x02; keycode = 0x2D; break;
                case '=': keycode = 0x2E; break;
                case '+': modifier = 0x02; keycode = 0x2E; break;
                case '[': keycode = 0x2F; break;
                case '{': modifier = 0x02; keycode = 0x2F; break;
                case ']': keycode = 0x30; break;
                case '}': modifier = 0x02; keycode = 0x30; break;
                case '\\': keycode = 0x31; break;
                case '|': modifier = 0x02; keycode = 0x31; break;
                case ';': keycode = 0x33; break;
                case ':': modifier = 0x02; keycode = 0x33; break;
                case '\'': keycode = 0x34; break;
                case '"': modifier = 0x02; keycode = 0x34; break;
                case '`': keycode = 0x35; break;
                case '~': modifier = 0x02; keycode = 0x35; break;
                case ',': keycode = 0x36; break;
                case '<': modifier = 0x02; keycode = 0x36; break;
                case '.': keycode = 0x37; break;
                case '>': modifier = 0x02; keycode = 0x37; break;
                case '/': keycode = 0x38; break;
                case '?': modifier = 0x02; keycode = 0x38; break;
                case '!': modifier = 0x02; keycode = 0x1E; break;
                case '@': modifier = 0x02; keycode = 0x1F; break;
                case '#': modifier = 0x02; keycode = 0x20; break;
                case '$': modifier = 0x02; keycode = 0x21; break;
                case '%': modifier = 0x02; keycode = 0x22; break;
                case '^': modifier = 0x02; keycode = 0x23; break;
                case '&': modifier = 0x02; keycode = 0x24; break;
                case '*': modifier = 0x02; keycode = 0x25; break;
                case '(': modifier = 0x02; keycode = 0x26; break;
                case ')': modifier = 0x02; keycode = 0x27; break;
                default: return;
            }
        }

        if (keycode != 0 && hid != null && target != null) {
            hid.sendReport(target, ID_KEYBOARD, new byte[]{modifier, 0, keycode, 0, 0, 0, 0, 0});
            try {
                Thread.sleep(25);
            } catch (InterruptedException ignored) {}
            if (hid != null && target != null) {
                hid.sendReport(target, ID_KEYBOARD, new byte[8]);
            }
        }
    }

    // ---- Consumer media keys ----

    private void consumer(int usage) {
        if (hid == null || target == null) return;
        hid.sendReport(target, ID_CONSUMER,
                new byte[]{(byte) (usage & 0xFF), (byte) ((usage >> 8) & 0xFF)});
        ui.postDelayed(() -> {
            if (hid != null && target != null)
                hid.sendReport(target, ID_CONSUMER, new byte[]{0, 0});
        }, 60);
    }

    // ---- Mouse / Touchpad ----

    private void mouseMove(int dx, int dy) {
        if (hid == null || target == null) return;
        hid.sendReport(target, ID_MOUSE, new byte[]{0, (byte) dx, (byte) dy, 0});
    }

    private void mouseClick(int button) {
        if (hid == null || target == null) return;
        hid.sendReport(target, ID_MOUSE, new byte[]{(byte) button, 0, 0, 0});
        ui.postDelayed(() -> {
            if (hid != null && target != null)
                hid.sendReport(target, ID_MOUSE, new byte[]{0, 0, 0, 0});
        }, 45);
    }

    private void mouseScroll(int wheel) {
        if (hid == null || target == null) return;
        hid.sendReport(target, ID_MOUSE, new byte[]{0, 0, 0, (byte) wheel});
    }

    // ---- UI ----

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(20, 32, 20, 32);

        status = new TextView(this);
        status.setText("Starting...");
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, 16);
        root.addView(status);

        // Top actions
        root.addView(row(
                btn("Connect", v -> pickDevice()),
                btn("Retry / Refresh", v -> {
                    status.setText("Re-registering...");
                    if (hid != null) registerHid();
                    else initBluetooth();
                })
        ));

        // Mode Navigation Tabs
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, 16, 0, 24);

        Button tabRemote = btn("📺 Remote", null);
        Button tabTouch = btn("🖱️ Touchpad", null);
        Button tabKeyboard = btn("⌨️ Keyboard", null);

        tabRemote.setOnClickListener(v -> switchTab(0, tabRemote, tabTouch, tabKeyboard));
        tabTouch.setOnClickListener(v -> switchTab(1, tabRemote, tabTouch, tabKeyboard));
        tabKeyboard.setOnClickListener(v -> switchTab(2, tabRemote, tabTouch, tabKeyboard));

        tabs.addView(tabRemote);
        tabs.addView(tabTouch);
        tabs.addView(tabKeyboard);
        root.addView(tabs);

        // Build 3 pages
        remoteContainer = buildRemoteView();
        trackpadContainer = buildTrackpadView();
        keyboardContainer = buildKeyboardView();

        root.addView(remoteContainer);
        root.addView(trackpadContainer);
        root.addView(keyboardContainer);

        // Show remote by default
        switchTab(0, tabRemote, tabTouch, tabKeyboard);

        scroll.addView(root);
        setContentView(scroll);
    }

    private void switchTab(int index, Button... tabBtns) {
        remoteContainer.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        trackpadContainer.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        keyboardContainer.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        for (int i = 0; i < tabBtns.length; i++) {
            tabBtns[i].setBackgroundColor(i == index ? Color.parseColor("#1976D2") : Color.parseColor("#E0E0E0"));
            tabBtns[i].setTextColor(i == index ? Color.WHITE : Color.BLACK);
        }
    }

    // 1. Remote View
    private View buildRemoteView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);

        v.addView(row(
                btn("Power", view -> consumer(C_POWER)),
                btn("Home", view -> consumer(C_HOME)),
                btn("Back", view -> consumer(C_BACK))));
        v.addView(row(blank(), btn("▲ Up", view -> key(K_UP)), blank()));
        v.addView(row(
                btn("◀ Left", view -> key(K_LEFT)),
                btn("● OK", view -> key(K_ENTER)),
                btn("Right ▶", view -> key(K_RIGHT))));
        v.addView(row(blank(), btn("▼ Down", view -> key(K_DOWN)), blank()));
        v.addView(row(
                btn("Vol -", view -> consumer(C_VOL_DOWN)),
                btn("Mute", view -> consumer(C_MUTE)),
                btn("Vol +", view -> consumer(C_VOL_UP))));
        v.addView(row(
                btn("Play / Pause", view -> consumer(C_PLAY_PAUSE)),
                btn("Esc / Menu", view -> key(K_ESC))));

        return v;
    }

    // 2. Trackpad / Touchscreen View
    private View buildTrackpadView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView label = new TextView(this);
        label.setText("Swipe surface to move cursor • Tap to Left Click");
        label.setTextSize(13);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, 0, 0, 12);
        v.addView(label);

        // Touchpad surface view
        View pad = new View(this);
        int padHeight = (int) (getResources().getDisplayMetrics().density * 260);
        LinearLayout.LayoutParams padParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, padHeight);
        padParams.setMargins(8, 0, 8, 16);
        pad.setLayoutParams(padParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#ECEFF1"));
        bg.setCornerRadius(16);
        bg.setStroke(3, Color.parseColor("#90A4AE"));
        pad.setBackground(bg);

        pad.setOnTouchListener(new View.OnTouchListener() {
            private float lastX, lastY;
            private long downTime;
            private boolean moved = false;
            private static final float SENSITIVITY = 1.3f;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        downTime = System.currentTimeMillis();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = (event.getX() - lastX) * SENSITIVITY;
                        float dy = (event.getY() - lastY) * SENSITIVITY;
                        if (Math.abs(dx) > 0.4f || Math.abs(dy) > 0.4f) {
                            moved = true;
                            int sendDx = Math.max(-127, Math.min(127, Math.round(dx)));
                            int sendDy = Math.max(-127, Math.min(127, Math.round(dy)));
                            mouseMove(sendDx, sendDy);
                            lastX = event.getX();
                            lastY = event.getY();
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!moved && (System.currentTimeMillis() - downTime) < 300) {
                            mouseClick(1); // Left click
                        }
                        return true;
                }
                return false;
            }
        });

        v.addView(pad);

        // Mouse buttons
        v.addView(row(
                btn("🖱️ Left Click", view -> mouseClick(1)),
                btn("🖱️ Right Click", view -> mouseClick(2))
        ));

        // Scroll buttons
        v.addView(row(
                btn("Scroll Up ▲", view -> mouseScroll(2)),
                btn("Scroll Down ▼", view -> mouseScroll(-2))
        ));

        return v;
    }

    // 3. Keyboard View
    private View buildKeyboardView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(this);
        label.setText("Type or paste text below to send to your TV/PC:");
        label.setTextSize(14);
        label.setPadding(8, 0, 8, 8);
        v.addView(label);

        EditText input = new EditText(this);
        input.setHint("e.g. YouTube video name, password, URL...");
        input.setPadding(24, 24, 24, 24);
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.parseColor("#F5F5F5"));
        inputBg.setCornerRadius(10);
        inputBg.setStroke(2, Color.parseColor("#BDBDBD"));
        input.setBackground(inputBg);
        v.addView(input);

        Button sendBtn = btn("🚀 Send Text to TV", view -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                sendString(text);
                input.setText("");
            }
        });
        sendBtn.setBackgroundColor(Color.parseColor("#2E7D32"));
        sendBtn.setTextColor(Color.WHITE);
        v.addView(sendBtn);

        TextView quickKeysLabel = new TextView(this);
        quickKeysLabel.setText("Quick Keys:");
        quickKeysLabel.setTextSize(13);
        quickKeysLabel.setPadding(8, 16, 8, 4);
        v.addView(quickKeysLabel);

        v.addView(row(
                btn("⌫ Backspace", view -> key(K_BACKSPACE)),
                btn("↵ Enter", view -> key(K_ENTER))
        ));
        v.addView(row(
                btn("␣ Space", view -> key(K_SPACE)),
                btn("⇥ Tab", view -> key(K_TAB)),
                btn("Del", view -> key(K_DELETE))
        ));

        return v;
    }

    private Button btn(String t, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(t);
        if (l != null) b.setOnClickListener(l);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(6, 6, 6, 6);
        b.setLayoutParams(params);
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
