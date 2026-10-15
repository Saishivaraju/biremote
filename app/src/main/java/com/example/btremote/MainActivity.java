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
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
 * Premium Bluetooth HID Universal Remote, Touchpad & Keyboard.
 * Serverless controller for Android TV, Google TV, Fire TV, Apple TV, PC & Mac.
 */
public class MainActivity extends Activity {

    // Report IDs
    private static final int ID_KEYBOARD = 1;
    private static final int ID_CONSUMER = 2;
    private static final int ID_MOUSE = 3;

    // Keyboard usages (Usage Page 0x07)
    private static final byte K_RIGHT = 0x4F, K_LEFT = 0x50, K_DOWN = 0x51,
            K_UP = 0x52, K_ENTER = 0x28, K_ESC = 0x29, K_BACKSPACE = 0x2A,
            K_TAB = 0x2B, K_SPACE = 0x2C, K_DELETE = 0x4C;

    // Consumer usages (Usage Page 0x0C)
    // 0x41 is Consumer Menu Pick / DPAD_CENTER (crucial for Android TV apps!)
    private static final int C_DPAD_CENTER = 0x41;
    private static final int C_POWER = 0x30, C_VOL_UP = 0xE9, C_VOL_DOWN = 0xEA,
            C_MUTE = 0xE2, C_PLAY_PAUSE = 0xCD, C_HOME = 0x223, C_BACK = 0x224;

    private static final byte[] DESCRIPTOR = new byte[]{
            // -------------------------------------------------------------
            // Keyboard, report ID 1
            // -------------------------------------------------------------
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
            (byte) 0x81, 0x02,             //   Input (Data, Variable, Absolute) - modifiers
            (byte) 0x95, 0x01,             //   Report Count (1)
            0x75, 0x08,                    //   Report Size (8)
            (byte) 0x81, 0x01,             //   Input (Constant) - reserved
            (byte) 0x95, 0x06,             //   Report Count (6)
            0x75, 0x08,                    //   Report Size (8)
            0x15, 0x00,                    //   Logical Minimum (0)
            0x25, 0x65,                    //   Logical Maximum (101)
            0x05, 0x07,                    //   Usage Page (Key Codes)
            0x19, 0x00,                    //   Usage Minimum (0)
            0x29, 0x65,                    //   Usage Maximum (101)
            (byte) 0x81, 0x00,             //   Input (Data, Array) - 6 keycodes
            (byte) 0xC0,                   // End Collection

            // -------------------------------------------------------------
            // Consumer control, report ID 2
            // -------------------------------------------------------------
            0x05, 0x0C,                    // Usage Page (Consumer Devices)
            0x09, 0x01,                    // Usage (Consumer Control)
            (byte) 0xA1, 0x01,             // Collection (Application)
            (byte) 0x85, 0x02,             //   Report ID (2)
            0x15, 0x00,                    //   Logical Minimum (0)
            0x26, (byte) 0xFF, 0x03,       //   Logical Maximum (1023)
            0x19, 0x00,                    //   Usage Minimum (0)
            0x2A, (byte) 0xFF, 0x03,       //   Usage Maximum (1023)
            0x75, 0x10,                    //   Report Size (16)
            (byte) 0x95, 0x01,             //   Report Count (1)
            (byte) 0x81, 0x00,             //   Input (Data, Array)
            (byte) 0xC0,                   // End Collection

            // -------------------------------------------------------------
            // Mouse, report ID 3
            // -------------------------------------------------------------
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
            (byte) 0x81, 0x03,             //     Input (Constant, Variable, Absolute)
            // X and Y
            0x05, 0x01,                    //     Usage Page (Generic Desktop)
            0x09, 0x30,                    //     Usage (X)
            0x09, 0x31,                    //     Usage (Y)
            0x15, (byte) 0x81,             //     Logical Minimum (-127)
            0x25, 0x7F,                    //     Logical Maximum (127)
            0x75, 0x08,                    //     Report Size (8)
            (byte) 0x95, 0x02,             //     Report Count (2)
            (byte) 0x81, 0x06,             //     Input (Data, Variable, Relative)
            // Wheel
            0x09, 0x38,                    //     Usage (Wheel)
            0x15, (byte) 0x81,             //     Logical Minimum (-127)
            0x25, 0x7F,                    //     Logical Maximum (127)
            0x75, 0x08,                    //     Report Size (8)
            (byte) 0x95, 0x01,             //     Report Count (1)
            (byte) 0x81, 0x06,             //     Input (Data, Variable, Relative)
            (byte) 0xC0,                   //   End Collection (Physical)
            (byte) 0xC0                    // End Collection (Application)
    };

    // Color Palette
    private static final int C_BG = Color.parseColor("#0C0F17");
    private static final int C_CARD = Color.parseColor("#151B2B");
    private static final int C_CARD_BORDER = Color.parseColor("#222D47");
    private static final int C_BTN = Color.parseColor("#1D253A");
    private static final int C_BTN_BORDER = Color.parseColor("#2E3B5C");
    private static final int C_BTN_PRESSED = Color.parseColor("#2A3654");
    private static final int C_INDIGO = Color.parseColor("#6366F1");
    private static final int C_INDIGO_PRESSED = Color.parseColor("#4F46E5");
    private static final int C_EMERALD = Color.parseColor("#10B981");
    private static final int C_EMERALD_PRESSED = Color.parseColor("#059669");
    private static final int C_CRIMSON = Color.parseColor("#EF4444");
    private static final int C_CRIMSON_PRESSED = Color.parseColor("#DC2626");
    private static final int C_TEXT_PRIMARY = Color.parseColor("#F8FAFC");
    private static final int C_TEXT_SECONDARY = Color.parseColor("#94A3B8");
    private static final int C_TEXT_MUTED = Color.parseColor("#64748B");

    private BluetoothAdapter adapter;
    private BluetoothHidDevice hid;
    private BluetoothDevice target;
    private TextView statusDot;
    private TextView statusText;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private View remoteContainer;
    private View trackpadContainer;
    private View keyboardContainer;

    private boolean isStreamingText = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(C_BG);
            getWindow().setNavigationBarColor(C_BG);
        }

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
    protected void onResume() {
        super.onResume();
        if (hid != null) {
            checkConnectedState();
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
            updateStatus("Bluetooth permission denied", false);
        }
    }

    private void initBluetooth() {
        BluetoothManager bm = getSystemService(BluetoothManager.class);
        adapter = bm != null ? bm.getAdapter() : BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            updateStatus("Bluetooth is OFF. Turn ON & tap Re-register", false);
            return;
        }
        updateStatus("Initializing HID Service...", false);
        adapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                hid = (BluetoothHidDevice) proxy;
                ui.postDelayed(() -> registerHid(), 250);
            }

            @Override
            public void onServiceDisconnected(int profile) {
                hid = null;
                updateStatus("HID service disconnected", false);
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
                BluetoothHidDevice.SUBCLASS1_COMBO, DESCRIPTOR);

        boolean started = hid.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(),
                new BluetoothHidDevice.Callback() {
                    @Override
                    public void onAppStatusChanged(BluetoothDevice d, boolean registered) {
                        ui.post(() -> {
                            if (registered) {
                                checkConnectedState();
                            } else {
                                updateStatus("HID registration pending • Tap Refresh", false);
                            }
                        });
                    }

                    @Override
                    public void onConnectionStateChanged(BluetoothDevice d, int state) {
                        ui.post(() -> {
                            if (state == BluetoothProfile.STATE_CONNECTED) {
                                target = d;
                                String name = d.getName() != null ? d.getName() : d.getAddress();
                                updateStatus("Connected to " + name, true);
                            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                                if (d.equals(target)) target = null;
                                checkConnectedState();
                            }
                        });
                    }

                    // Handshake responses: Prevents the host (Android TV) from timing out and disconnecting!
                    @Override
                    public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
                        if (hid == null) return;
                        if (id == ID_KEYBOARD) {
                            hid.replyReport(device, type, id, new byte[8]);
                        } else if (id == ID_CONSUMER) {
                            hid.replyReport(device, type, id, new byte[2]);
                        } else if (id == ID_MOUSE) {
                            hid.replyReport(device, type, id, new byte[4]);
                        } else {
                            hid.reportError(device, (byte) 0x03);
                        }
                    }

                    @Override
                    public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
                        if (hid != null) {
                            hid.reportError(device, (byte) 0x00);
                        }
                    }
                });

        if (!started) {
            updateStatus("Service busy • Tap Re-register", false);
        }
    }

    /**
     * Resolves the target Bluetooth device dynamically.
     * Prevents dropped commands if the connection state callback didn't fire.
     */
    private BluetoothDevice getTarget() {
        if (target != null) return target;
        if (hid != null) {
            try {
                List<BluetoothDevice> connected = hid.getConnectedDevices();
                if (connected != null && !connected.isEmpty()) {
                    target = connected.get(0);
                    return target;
                }
            } catch (Exception ignored) {}
        }
        if (adapter != null) {
            try {
                Set<BluetoothDevice> bonded = adapter.getBondedDevices();
                if (bonded != null && !bonded.isEmpty()) {
                    target = bonded.iterator().next();
                    return target;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void checkConnectedState() {
        BluetoothDevice d = getTarget();
        if (d != null) {
            String name = d.getName() != null ? d.getName() : d.getAddress();
            updateStatus("Connected to " + name, true);
        } else {
            updateStatus("Ready • Tap Connect to pair", true);
        }
    }

    private void updateStatus(String text, boolean isPositive) {
        ui.post(() -> {
            if (statusText != null) statusText.setText(text);
            if (statusDot != null) {
                statusDot.setTextColor(isPositive ? C_EMERALD : Color.parseColor("#F59E0B"));
            }
        });
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
                .setTitle("Select Paired Device")
                .setItems(names, (dlg, which) -> {
                    target = list.get(which);
                    hid.connect(target);
                    String name = target.getName() != null ? target.getName() : target.getAddress();
                    updateStatus("Connecting to " + name + "...", true);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---- Key & String Transmission ----

    private void key(byte usage) {
        sendKey(usage, (byte) 0);
    }

    private void sendKey(byte usage, byte modifier) {
        BluetoothDevice d = getTarget();
        if (hid == null || d == null) return;
        hid.sendReport(d, ID_KEYBOARD, new byte[]{modifier, 0, usage, 0, 0, 0, 0, 0});
        ui.postDelayed(() -> {
            if (hid != null && d != null)
                hid.sendReport(d, ID_KEYBOARD, new byte[8]);
        }, 40);
    }

    /**
     * OK Button Fix:
     * Sends BOTH DPAD_CENTER (Consumer 0x41) AND Keyboard Enter (0x28).
     * This guarantees it selects cards on Netflix/YouTube TV apps AND web forms/dialogs!
     */
    private void pressOk() {
        consumer(C_DPAD_CENTER);
        key(K_ENTER);
    }

    private void pressBack() {
        consumer(C_BACK);
        key(K_ESC);
    }

    private void sendString(String text) {
        BluetoothDevice d = getTarget();
        if (text == null || text.isEmpty() || hid == null || d == null) return;
        isStreamingText = true;
        new Thread(() -> {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                sendChar(c);
                try {
                    Thread.sleep(35);
                } catch (InterruptedException ignored) {}
            }
            isStreamingText = false;
        }).start();
    }

    private void sendChar(char c) {
        BluetoothDevice d = getTarget();
        if (hid == null || d == null) return;

        byte modifier = 0;
        byte keycode = 0;

        if (c >= 'a' && c <= 'z') {
            keycode = (byte) (0x04 + (c - 'a'));
        } else if (c >= 'A' && c <= 'Z') {
            modifier = 0x02;
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

        if (keycode != 0) {
            hid.sendReport(d, ID_KEYBOARD, new byte[]{modifier, 0, keycode, 0, 0, 0, 0, 0});
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {}
            if (hid != null && d != null) {
                hid.sendReport(d, ID_KEYBOARD, new byte[8]);
            }
        }
    }

    // ---- Media & Mouse ----

    private void consumer(int usage) {
        BluetoothDevice d = getTarget();
        if (hid == null || d == null) return;
        hid.sendReport(d, ID_CONSUMER,
                new byte[]{(byte) (usage & 0xFF), (byte) ((usage >> 8) & 0xFF)});
        ui.postDelayed(() -> {
            if (hid != null && d != null)
                hid.sendReport(d, ID_CONSUMER, new byte[]{0, 0});
        }, 50);
    }

    private void mouseMove(int dx, int dy) {
        BluetoothDevice d = getTarget();
        if (hid == null || d == null) return;
        hid.sendReport(d, ID_MOUSE, new byte[]{0, (byte) dx, (byte) dy, 0});
    }

    private void mouseClick(int button) {
        BluetoothDevice d = getTarget();
        if (hid == null || d == null) return;
        hid.sendReport(d, ID_MOUSE, new byte[]{(byte) button, 0, 0, 0});
        ui.postDelayed(() -> {
            if (hid != null && d != null)
                hid.sendReport(d, ID_MOUSE, new byte[]{0, 0, 0, 0});
        }, 40);
    }

    private void mouseScroll(int wheel) {
        BluetoothDevice d = getTarget();
        if (hid == null || d == null) return;
        hid.sendReport(d, ID_MOUSE, new byte[]{0, 0, 0, (byte) wheel});
    }

    // ---- UI Builder ----

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(C_BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(18), dp(24), dp(18), dp(32));

        // 1. Header Banner
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, dp(4), 0, dp(14));

        TextView appTitle = new TextView(this);
        appTitle.setText("BT REMOTE");
        appTitle.setTextColor(C_TEXT_PRIMARY);
        appTitle.setTextSize(20);
        appTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        appTitle.setLetterSpacing(0.12f);
        header.addView(appTitle);

        TextView subTitle = new TextView(this);
        subTitle.setText("Universal HID Smart Controller");
        subTitle.setTextColor(C_TEXT_MUTED);
        subTitle.setTextSize(11);
        subTitle.setLetterSpacing(0.05f);
        header.addView(subTitle);
        root.addView(header);

        // 2. Status Badge Card
        LinearLayout statusBadge = new LinearLayout(this);
        statusBadge.setOrientation(LinearLayout.HORIZONTAL);
        statusBadge.setGravity(Gravity.CENTER);
        statusBadge.setBackground(shape(C_CARD, C_CARD_BORDER, dp(1), dp(24)));
        statusBadge.setPadding(dp(16), dp(8), dp(16), dp(8));

        statusDot = new TextView(this);
        statusDot.setText("● ");
        statusDot.setTextSize(14);
        statusDot.setTextColor(Color.parseColor("#F59E0B"));
        statusBadge.addView(statusDot);

        statusText = new TextView(this);
        statusText.setText("Connecting Bluetooth...");
        statusText.setTextSize(12);
        statusText.setTextColor(C_TEXT_SECONDARY);
        statusBadge.addView(statusText);
        root.addView(statusBadge);

        // 3. Quick Action Row (Connect & Refresh)
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(14), 0, dp(14));

        Button btnConnect = customButton("🔗 Connect Device", C_INDIGO, C_INDIGO_PRESSED, Color.WHITE, dp(12));
        btnConnect.setOnClickListener(v -> pickDevice());

        Button btnRefresh = customButton("🔄 Re-register", C_BTN, C_BTN_PRESSED, C_TEXT_SECONDARY, dp(12));
        btnRefresh.setOnClickListener(v -> {
            updateStatus("Refreshing HID service...", false);
            if (hid != null) registerHid();
            else initBluetooth();
        });

        actions.addView(btnConnect);
        actions.addView(btnRefresh);
        root.addView(actions);

        // 4. Tab Navigation (Segmented pill bar)
        LinearLayout navTabs = new LinearLayout(this);
        navTabs.setOrientation(LinearLayout.HORIZONTAL);
        navTabs.setBackground(shape(Color.parseColor("#101524"), C_CARD_BORDER, dp(1), dp(16)));
        navTabs.setPadding(dp(4), dp(4), dp(4), dp(4));

        Button tabRemote = customButton("📺 REMOTE", C_INDIGO, C_INDIGO_PRESSED, Color.WHITE, dp(12));
        Button tabTouch = customButton("🖱️ TOUCHPAD", Color.TRANSPARENT, C_BTN, C_TEXT_MUTED, dp(12));
        Button tabKeyb = customButton("⌨️ KEYBOARD", Color.TRANSPARENT, C_BTN, C_TEXT_MUTED, dp(12));

        tabRemote.setOnClickListener(v -> switchTab(0, tabRemote, tabTouch, tabKeyb));
        tabTouch.setOnClickListener(v -> switchTab(1, tabRemote, tabTouch, tabKeyb));
        tabKeyb.setOnClickListener(v -> switchTab(2, tabRemote, tabTouch, tabKeyb));

        navTabs.addView(tabRemote);
        navTabs.addView(tabTouch);
        navTabs.addView(tabKeyb);
        root.addView(navTabs);

        // 5. Container Views
        remoteContainer = buildRemoteView();
        trackpadContainer = buildTrackpadView();
        keyboardContainer = buildKeyboardView();

        root.addView(remoteContainer);
        root.addView(trackpadContainer);
        root.addView(keyboardContainer);

        switchTab(0, tabRemote, tabTouch, tabKeyb);

        scroll.addView(root);
        setContentView(scroll);
    }

    private void switchTab(int index, Button tabRemote, Button tabTouch, Button tabKeyb) {
        remoteContainer.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        trackpadContainer.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        keyboardContainer.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        Button[] tabs = new Button[]{tabRemote, tabTouch, tabKeyb};
        for (int i = 0; i < tabs.length; i++) {
            if (i == index) {
                tabs[i].setBackground(shape(C_INDIGO, Color.TRANSPARENT, 0, dp(12)));
                tabs[i].setTextColor(Color.WHITE);
                tabs[i].setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            } else {
                tabs[i].setBackground(shape(Color.TRANSPARENT, Color.TRANSPARENT, 0, dp(12)));
                tabs[i].setTextColor(C_TEXT_MUTED);
                tabs[i].setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            }
        }

        if (index == 1) {
            ui.postDelayed(() -> {
                mouseMove(2, 2);
                mouseMove(-2, -2);
            }, 100);
        }
    }

    // ---- 1. Authentic Remote View ----

    private View buildRemoteView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setGravity(Gravity.CENTER_HORIZONTAL);
        v.setPadding(0, dp(16), 0, 0);

        // Power & Home cluster
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        Button btnPower = iconBtn("⏻ Power", C_CRIMSON, C_CRIMSON_PRESSED, Color.WHITE, view -> consumer(C_POWER));
        Button btnHome = iconBtn("🏠 Home", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> consumer(C_HOME));
        Button btnBack = iconBtn("↩ Back", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> pressBack());
        topBar.addView(btnPower);
        topBar.addView(btnHome);
        topBar.addView(btnBack);
        v.addView(topBar);

        // Circular D-Pad Container Card
        LinearLayout dpadCard = new LinearLayout(this);
        dpadCard.setOrientation(LinearLayout.VERTICAL);
        dpadCard.setGravity(Gravity.CENTER);
        dpadCard.setBackground(shape(C_CARD, C_CARD_BORDER, dp(1), dp(32)));
        dpadCard.setPadding(dp(16), dp(20), dp(16), dp(20));
        LinearLayout.LayoutParams dpadParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dpadParams.setMargins(0, dp(16), 0, dp(16));
        dpadCard.setLayoutParams(dpadParams);

        // Up
        dpadCard.addView(dpadRow(blank(), dpadBtn("▲", view -> key(K_UP)), blank()));
        // Left - OK - Right
        Button btnLeft = dpadBtn("◀", view -> key(K_LEFT));
        // Fixed OK: Triggers both DPAD_CENTER (Consumer 0x41) and Enter (0x28)
        Button btnOk = dpadCenterBtn("OK", view -> pressOk());
        Button btnRight = dpadBtn("▶", view -> key(K_RIGHT));
        dpadCard.addView(dpadRow(btnLeft, btnOk, btnRight));
        // Down
        dpadCard.addView(dpadRow(blank(), dpadBtn("▼", view -> key(K_DOWN)), blank()));

        v.addView(dpadCard);

        // Volume & Mute Row
        LinearLayout volRow = new LinearLayout(this);
        volRow.setOrientation(LinearLayout.HORIZONTAL);
        Button btnVolDown = iconBtn("🔉 Vol -", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> consumer(C_VOL_DOWN));
        Button btnMute = iconBtn("🔇 Mute", C_BTN, C_BTN_PRESSED, Color.parseColor("#FBBF24"), view -> consumer(C_MUTE));
        Button btnVolUp = iconBtn("🔊 Vol +", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> consumer(C_VOL_UP));
        volRow.addView(btnVolDown);
        volRow.addView(btnMute);
        volRow.addView(btnVolUp);
        v.addView(volRow);

        // Playback row
        LinearLayout playRow = new LinearLayout(this);
        playRow.setOrientation(LinearLayout.HORIZONTAL);
        Button btnPlay = iconBtn("⏯️ Play/Pause", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> consumer(C_PLAY_PAUSE));
        Button btnEsc = iconBtn("☰ Menu / Esc", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> key(K_ESC));
        playRow.addView(btnPlay);
        playRow.addView(btnEsc);
        v.addView(playRow);

        return v;
    }

    // ---- 2. Authentic Touchpad View ----

    private View buildTrackpadView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setGravity(Gravity.CENTER_HORIZONTAL);
        v.setPadding(0, dp(16), 0, 0);

        TextView hint = new TextView(this);
        hint.setText("PRECISION GLASS TRACKPAD");
        hint.setTextColor(C_TEXT_MUTED);
        hint.setTextSize(11);
        hint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        hint.setLetterSpacing(0.12f);
        hint.setPadding(0, 0, 0, dp(8));
        v.addView(hint);

        // Touchpad Surface
        View pad = new View(this);
        int padHeight = dp(270);
        LinearLayout.LayoutParams padParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, padHeight);
        padParams.setMargins(0, 0, 0, dp(14));
        pad.setLayoutParams(padParams);

        GradientDrawable padDrawable = new GradientDrawable();
        padDrawable.setColor(Color.parseColor("#131929"));
        padDrawable.setCornerRadius(dp(20));
        padDrawable.setStroke(dp(1), Color.parseColor("#38466B"));
        pad.setBackground(padDrawable);

        // Touch handling with requestDisallowInterceptTouchEvent so ScrollView doesn't hijack swipes!
        pad.setOnTouchListener(new View.OnTouchListener() {
            private float lastX, lastY;
            private long downTime;
            private boolean moved = false;
            private static final float SENSITIVITY = 2.2f;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        lastX = event.getX();
                        lastY = event.getY();
                        downTime = System.currentTimeMillis();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        float dx = (event.getX() - lastX) * SENSITIVITY;
                        float dy = (event.getY() - lastY) * SENSITIVITY;
                        if (Math.abs(dx) > 0.3f || Math.abs(dy) > 0.3f) {
                            moved = true;
                            int sendDx = Math.max(-127, Math.min(127, Math.round(dx)));
                            int sendDy = Math.max(-127, Math.min(127, Math.round(dy)));
                            mouseMove(sendDx, sendDy);
                            lastX = event.getX();
                            lastY = event.getY();
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        if (!moved && (System.currentTimeMillis() - downTime) < 300) {
                            mouseClick(1); // Tap to Left Click
                        }
                        return true;
                }
                return false;
            }
        });
        v.addView(pad);

        // Mouse click triggers
        LinearLayout clickRow = new LinearLayout(this);
        clickRow.setOrientation(LinearLayout.HORIZONTAL);
        Button btnLeft = iconBtn("🖱️ Left Click", C_INDIGO, C_INDIGO_PRESSED, Color.WHITE, view -> mouseClick(1));
        Button btnRight = iconBtn("🖱️ Right Click", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> mouseClick(2));
        clickRow.addView(btnLeft);
        clickRow.addView(btnRight);
        v.addView(clickRow);

        // Scroll Row
        LinearLayout scrollRow = new LinearLayout(this);
        scrollRow.setOrientation(LinearLayout.HORIZONTAL);
        Button btnScrollUp = iconBtn("▲ Scroll Up", C_BTN, C_BTN_PRESSED, C_TEXT_SECONDARY, view -> mouseScroll(2));
        Button btnScrollDown = iconBtn("▼ Scroll Down", C_BTN, C_BTN_PRESSED, C_TEXT_SECONDARY, view -> mouseScroll(-2));
        scrollRow.addView(btnScrollUp);
        scrollRow.addView(btnScrollDown);
        v.addView(scrollRow);

        TextView guide = new TextView(this);
        guide.setText("💻 Laptop/PC: Cursor appears everywhere.\n📺 Android TV: Use D-Pad for TV menus. Cursor appears in web browsers, media players, and mouse-enabled apps.");
        guide.setTextColor(C_TEXT_MUTED);
        guide.setTextSize(11);
        guide.setGravity(Gravity.CENTER);
        guide.setPadding(dp(8), dp(12), dp(8), dp(4));
        v.addView(guide);

        return v;
    }

    // ---- 3. Authentic Keyboard View ----

    private View buildKeyboardView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setPadding(0, dp(16), 0, 0);

        TextView hint = new TextView(this);
        hint.setText("DIRECT TEXT & LIVE TYPING");
        hint.setTextColor(C_TEXT_MUTED);
        hint.setTextSize(11);
        hint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        hint.setLetterSpacing(0.12f);
        hint.setPadding(dp(4), 0, 0, dp(8));
        v.addView(hint);

        // Text card
        LinearLayout inputCard = new LinearLayout(this);
        inputCard.setOrientation(LinearLayout.VERTICAL);
        inputCard.setBackground(shape(C_CARD, C_CARD_BORDER, dp(1), dp(18)));
        inputCard.setPadding(dp(14), dp(14), dp(14), dp(14));

        EditText input = new EditText(this);
        input.setHint("Type or paste movie title, URL, or password...");
        input.setHintTextColor(Color.parseColor("#4B5563"));
        input.setTextColor(Color.WHITE);
        input.setTextSize(15);
        input.setPadding(dp(14), dp(14), dp(14), dp(14));
        input.setBackground(shape(Color.parseColor("#0E131F"), Color.parseColor("#2B3754"), dp(1), dp(12)));

        // Live typing: As you type or delete on your phone keyboard, it sends keys live!
        input.addTextChangedListener(new TextWatcher() {
            private String prevText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isStreamingText) return;
                String curText = s.toString();
                if (curText.length() > prevText.length()) {
                    String added = curText.substring(prevText.length());
                    for (char c : added.toCharArray()) {
                        sendChar(c);
                    }
                } else if (curText.length() < prevText.length()) {
                    int diff = prevText.length() - curText.length();
                    for (int i = 0; i < diff; i++) {
                        key(K_BACKSPACE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Pressing Enter on phone soft keyboard triggers OK / Search on TV
        input.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            pressOk();
            return true;
        });

        inputCard.addView(input);

        Button btnSend = customButton("🚀 STREAM FULL TEXT TO TV", C_EMERALD, C_EMERALD_PRESSED, Color.WHITE, dp(12));
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        sendParams.setMargins(0, dp(12), 0, 0);
        btnSend.setLayoutParams(sendParams);
        btnSend.setOnClickListener(view -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                sendString(text);
                input.setText("");
            }
        });
        inputCard.addView(btnSend);
        v.addView(inputCard);

        TextView keysHeader = new TextView(this);
        keysHeader.setText("COMMAND KEYCAPS");
        keysHeader.setTextColor(C_TEXT_MUTED);
        keysHeader.setTextSize(11);
        keysHeader.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        keysHeader.setLetterSpacing(0.12f);
        keysHeader.setPadding(dp(4), dp(20), 0, dp(8));
        v.addView(keysHeader);

        // Keycap row 1
        LinearLayout r1 = new LinearLayout(this);
        r1.setOrientation(LinearLayout.HORIZONTAL);
        r1.addView(iconBtn("⌫ Backspace", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> key(K_BACKSPACE)));
        r1.addView(iconBtn("↵ Enter / OK", C_INDIGO, C_INDIGO_PRESSED, Color.WHITE, view -> pressOk()));
        v.addView(r1);

        // Keycap row 2
        LinearLayout r2 = new LinearLayout(this);
        r2.setOrientation(LinearLayout.HORIZONTAL);
        r2.addView(iconBtn("␣ Space", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> key(K_SPACE)));
        r2.addView(iconBtn("⇥ Tab", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> key(K_TAB)));
        r2.addView(iconBtn("⌦ Delete", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> key(K_DELETE)));
        v.addView(r2);

        // Keycap row 3
        LinearLayout r3 = new LinearLayout(this);
        r3.setOrientation(LinearLayout.HORIZONTAL);
        r3.addView(iconBtn("⎋ Esc / Exit", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> key(K_ESC)));
        r3.addView(iconBtn("🏠 Home", C_BTN, C_BTN_PRESSED, C_TEXT_PRIMARY, view -> consumer(C_HOME)));
        v.addView(r3);

        return v;
    }

    // ---- Visual Component Helpers ----

    private Button customButton(String text, int normalColor, int pressedColor, int textColor, int radiusDp) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(textColor);
        b.setTextSize(12);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        b.setBackground(buttonSelector(normalColor, pressedColor, C_CARD_BORDER, dp(1), dp(radiusDp)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(44), 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(params);
        return b;
    }

    private Button iconBtn(String text, int normalColor, int pressedColor, int textColor, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(textColor);
        b.setTextSize(13);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        b.setBackground(buttonSelector(normalColor, pressedColor, C_BTN_BORDER, dp(1), dp(14)));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(params);
        return b;
    }

    private Button dpadBtn(String arrow, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(arrow);
        b.setTextSize(18);
        b.setTextColor(C_TEXT_PRIMARY);
        b.setBackground(buttonSelector(C_BTN, C_BTN_PRESSED, C_BTN_BORDER, dp(1), dp(16)));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(58), 1f);
        params.setMargins(dp(6), dp(6), dp(6), dp(6));
        b.setLayoutParams(params);
        return b;
    }

    private Button dpadCenterBtn(String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        b.setBackground(buttonSelector(C_INDIGO, C_INDIGO_PRESSED, Color.parseColor("#818CF8"), dp(2), dp(29)));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(58), 1f);
        params.setMargins(dp(6), dp(6), dp(6), dp(6));
        b.setLayoutParams(params);
        return b;
    }

    private LinearLayout dpadRow(View... views) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        for (View v : views) r.addView(v);
        return r;
    }

    private View blank() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        return v;
    }

    private GradientDrawable shape(int bgColor, int strokeColor, int strokeWidth, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(bgColor);
        d.setCornerRadius(radius);
        if (strokeWidth > 0) d.setStroke(strokeWidth, strokeColor);
        return d;
    }

    private StateListDrawable buttonSelector(int normalBg, int pressedBg, int strokeColor, int strokeWidth, int radius) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, shape(pressedBg, strokeColor, strokeWidth, radius));
        s.addState(new int[]{}, shape(normalBg, strokeColor, strokeWidth, radius));
        return s;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
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
