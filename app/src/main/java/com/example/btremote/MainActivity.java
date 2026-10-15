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
import android.content.Intent;
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
 * Clean & Simple Bluetooth HID Universal Remote, Touchpad & Keyboard.
 * Works seamlessly with Android TV, Google TV, PC/Laptop, Projectors & Media Players.
 */
public class MainActivity extends Activity {

    // Report IDs
    private static final int ID_KEYBOARD = 1;
    private static final int ID_MOUSE = 2;
    private static final int ID_CONSUMER = 3;

    // Keyboard Usages (Usage Page 0x07)
    private static final byte K_RIGHT = 0x4F, K_LEFT = 0x50, K_DOWN = 0x51, K_UP = 0x52;
    private static final byte K_ENTER = 0x28, K_ESC = 0x29, K_BACKSPACE = 0x2A;
    private static final byte K_TAB = 0x2B, K_SPACE = 0x2C, K_MENU = 0x65;

    // Consumer Usages (Usage Page 0x0C)
    private static final int C_POWER = 0x30;
    private static final int C_VOL_UP = 0xE9, C_VOL_DOWN = 0xEA, C_MUTE = 0xE2;
    private static final int C_PLAY_PAUSE = 0xCD;
    private static final int C_FAST_FORWARD = 0x8E, C_REWIND = 0x8F;
    private static final int C_HOME = 0x223, C_BACK = 0x224;

    private static final byte[] DESCRIPTOR = new byte[]{
            // 1. Keyboard (Report ID 1)
            0x05, 0x01,                    // Usage Page (Generic Desktop)
            0x09, 0x06,                    // Usage (Keyboard)
            (byte) 0xA1, 0x01,             // Collection (Application)
            (byte) 0x85, (byte) ID_KEYBOARD,//   Report ID (1)
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
            (byte) 0x81, 0x00,             //   Input (Data, Array)
            (byte) 0xC0,                   // End Collection (Keyboard)

            // 2. Mouse (Report ID 2)
            0x05, 0x01,                    // Usage Page (Generic Desktop)
            0x09, 0x02,                    // Usage (Mouse)
            (byte) 0xA1, 0x01,             // Collection (Application)
            (byte) 0x85, (byte) ID_MOUSE,  //   Report ID (2)
            0x09, 0x01,                    //   Usage (Pointer)
            (byte) 0xA1, 0x00,             //   Collection (Physical)
            0x05, 0x09,                    //     Usage Page (Buttons)
            0x19, 0x01,                    //     Usage Minimum (1)
            0x29, 0x03,                    //     Usage Maximum (3)
            0x15, 0x00,                    //     Logical Minimum (0)
            0x25, 0x01,                    //     Logical Maximum (1)
            (byte) 0x95, 0x03,             //     Report Count (3)
            0x75, 0x01,                    //     Report Size (1)
            (byte) 0x81, 0x02,             //     Input (Data, Variable, Absolute)
            (byte) 0x95, 0x01,             //     Report Count (1)
            0x75, 0x05,                    //     Report Size (5)
            (byte) 0x81, 0x03,             //     Input (Constant, Variable, Absolute)
            0x05, 0x01,                    //     Usage Page (Generic Desktop)
            0x09, 0x30,                    //     Usage (X)
            0x09, 0x31,                    //     Usage (Y)
            0x15, (byte) 0x81,             //     Logical Minimum (-127)
            0x25, 0x7F,                    //     Logical Maximum (127)
            0x75, 0x08,                    //     Report Size (8)
            (byte) 0x95, 0x02,             //     Report Count (2)
            (byte) 0x81, 0x06,             //     Input (Data, Variable, Relative)
            0x09, 0x38,                    //     Usage (Wheel)
            0x15, (byte) 0x81,             //     Logical Minimum (-127)
            0x25, 0x7F,                    //     Logical Maximum (127)
            0x75, 0x08,                    //     Report Size (8)
            (byte) 0x95, 0x01,             //     Report Count (1)
            (byte) 0x81, 0x06,             //     Input (Data, Variable, Relative)
            (byte) 0xC0,                   //   End Collection (Physical)
            (byte) 0xC0,                   // End Collection (Mouse)

            // 3. Consumer / Media Remote (Report ID 3)
            0x05, 0x0C,                    // Usage Page (Consumer Devices)
            0x09, 0x01,                    // Usage (Consumer Control)
            (byte) 0xA1, 0x01,             // Collection (Application)
            (byte) 0x85, (byte) ID_CONSUMER,//  Report ID (3)
            0x15, 0x00,                    //   Logical Minimum (0)
            0x26, (byte) 0xFF, 0x03,       //   Logical Maximum (1023)
            0x19, 0x00,                    //   Usage Minimum (0)
            0x2A, (byte) 0xFF, 0x03,       //   Usage Maximum (1023)
            0x75, 0x10,                    //   Report Size (16)
            (byte) 0x95, 0x01,             //   Report Count (1)
            (byte) 0x81, 0x00,             //   Input (Data, Array)
            (byte) 0xC0                    // End Collection (Consumer)
    };

    // Clean, modern, unified color palette
    private static final int C_BG = Color.parseColor("#11151E");
    private static final int C_CARD = Color.parseColor("#1B212D");
    private static final int C_CARD_BORDER = Color.parseColor("#283144");
    private static final int C_BTN = Color.parseColor("#222A3A");
    private static final int C_BTN_BORDER = Color.parseColor("#323E56");
    private static final int C_BTN_PRESSED = Color.parseColor("#2C364A");
    private static final int C_PRIMARY = Color.parseColor("#3B82F6");
    private static final int C_PRIMARY_PRESSED = Color.parseColor("#2563EB");
    private static final int C_SUCCESS = Color.parseColor("#10B981");
    private static final int C_SUCCESS_PRESSED = Color.parseColor("#059669");
    private static final int C_DANGER = Color.parseColor("#EF4444");
    private static final int C_DANGER_PRESSED = Color.parseColor("#DC2626");
    private static final int C_TEXT = Color.parseColor("#F9FAFB");
    private static final int C_TEXT_MUTED = Color.parseColor("#94A3B8");

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            updateStatus("Bluetooth is OFF. Turn ON Bluetooth", false);
            return;
        }
        updateStatus("Initializing HID...", false);
        adapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                hid = (BluetoothHidDevice) proxy;
                ui.postDelayed(() -> registerHid(), 250);
            }

            @Override
            public void onServiceDisconnected(int profile) {
                hid = null;
                updateStatus("HID Disconnected", false);
            }
        }, BluetoothProfile.HID_DEVICE);
    }

    private void registerHid() {
        if (hid == null) return;
        try {
            hid.unregisterApp();
        } catch (Exception ignored) {}

        BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                "BT Remote", "Universal Remote", "Google",
                BluetoothHidDevice.SUBCLASS1_COMBO, DESCRIPTOR);

        boolean ok = hid.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(),
                new BluetoothHidDevice.Callback() {
                    @Override
                    public void onAppStatusChanged(BluetoothDevice d, boolean registered) {
                        ui.post(() -> {
                            if (registered) {
                                checkConnectedState();
                            } else {
                                updateStatus("Ready • Tap Connect", true);
                            }
                        });
                    }

                    @Override
                    public void onConnectionStateChanged(BluetoothDevice d, int state) {
                        ui.post(() -> {
                            if (state == BluetoothProfile.STATE_CONNECTED) {
                                target = d;
                                String name = d.getName() != null ? d.getName() : d.getAddress();
                                updateStatus("Connected: " + name, true);
                            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                                if (d.equals(target)) target = null;
                                checkConnectedState();
                            }
                        });
                    }

                    @Override
                    public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
                        if (hid == null) return;
                        if (id == ID_KEYBOARD || id == 0) {
                            hid.replyReport(device, type, (byte) 1, new byte[8]);
                        } else if (id == ID_MOUSE) {
                            hid.replyReport(device, type, (byte) 2, new byte[4]);
                        } else if (id == ID_CONSUMER) {
                            hid.replyReport(device, type, (byte) 3, new byte[2]);
                        } else {
                            hid.replyReport(device, type, id, new byte[bufferSize > 0 ? Math.min(bufferSize, 8) : 8]);
                        }
                    }

                    @Override
                    public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
                        if (hid != null) hid.reportError(device, (byte) 0x00);
                    }
                });

        if (!ok) {
            updateStatus("Service busy • Tap Connect", false);
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180);
        startActivity(discoverableIntent);

        new AlertDialog.Builder(this)
                .setTitle("Pairing Mode Active 📡")
                .setMessage("Your phone is now discoverable for 3 minutes!\n\n"
                        + "👉 On Laptop / PC:\n"
                        + "1. Go to Windows Settings > Bluetooth.\n"
                        + "2. Click 'Add Device' > Select 'BT Remote'.\n\n"
                        + "👉 On TV / Projector:\n"
                        + "1. Go to Settings > Remotes & Accessories.\n"
                        + "2. Add Accessory > Select 'BT Remote'.")
                .setPositiveButton("OK", null)
                .show();
    }

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
            updateStatus("Connected: " + name, true);
        } else {
            updateStatus("Ready • Tap Connect", false);
        }
    }

    private void updateStatus(String text, boolean isPositive) {
        ui.post(() -> {
            if (statusText != null) statusText.setText(text);
            if (statusDot != null) {
                statusDot.setTextColor(isPositive ? C_SUCCESS : Color.parseColor("#F59E0B"));
            }
        });
    }

    private void pickDevice() {
        if (hid == null || adapter == null) {
            initBluetooth();
            return;
        }
        Set<BluetoothDevice> bonded = adapter.getBondedDevices();
        final List<BluetoothDevice> list = new ArrayList<>(bonded);
        String[] names = new String[list.size()];
        for (int i = 0; i < names.length; i++) {
            String name = list.get(i).getName();
            names[i] = (name != null && !name.isEmpty()) ? name : list.get(i).getAddress();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Device (TV, Laptop, Projector)")
                .setItems(names, (dlg, which) -> {
                    target = list.get(which);
                    hid.connect(target);
                    String name = target.getName() != null ? target.getName() : target.getAddress();
                    updateStatus("Connecting to " + name + "...", true);
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Pair New 📡", (dlg, which) -> makeDiscoverable())
                .show();
    }

    // ---- HID Signal Emitters ----

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
     * Universal OK / Enter key:
     * Sends Keyboard Return/Enter (0x28).
     * Works natively across all TV video players (YouTube, Netflix, Prime, MX Player),
     * Leanback, dialog confirmations, and PC/Laptop.
     */
    private void pressOk() {
        key(K_ENTER);
    }

    /**
     * Universal Play / Pause:
     * Sends Consumer Play/Pause (0xCD) and Keyboard Spacebar (0x2C).
     * Seamlessly pauses and plays videos in every player app.
     */
    private void pressPlayPause() {
        consumer(C_PLAY_PAUSE);
        ui.postDelayed(() -> key(K_SPACE), 50);
    }

    private void pressBack() {
        consumer(C_BACK);
        ui.postDelayed(() -> key(K_ESC), 40);
    }

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

    // ---- Clean, Simple UI ----

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(C_BG);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(24));

        // 1. Clean Top Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(12));

        TextView title = new TextView(this);
        title.setText("BT REMOTE");
        title.setTextColor(C_TEXT);
        title.setTextSize(18);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(title);

        Button btnPair = cleanButton("📡 Pair", C_BTN, C_BTN_PRESSED, C_TEXT, dp(8));
        btnPair.setOnClickListener(v -> makeDiscoverable());
        LinearLayout.LayoutParams pairParams = new LinearLayout.LayoutParams(dp(80), dp(38));
        pairParams.setMargins(0, 0, dp(8), 0);
        btnPair.setLayoutParams(pairParams);
        header.addView(btnPair);

        Button btnConnect = cleanButton("🔗 Connect", C_PRIMARY, C_PRIMARY_PRESSED, Color.WHITE, dp(8));
        btnConnect.setOnClickListener(v -> pickDevice());
        btnConnect.setLayoutParams(new LinearLayout.LayoutParams(dp(95), dp(38)));
        header.addView(btnConnect);

        root.addView(header);

        // 2. Status Pill
        LinearLayout statusPill = new LinearLayout(this);
        statusPill.setOrientation(LinearLayout.HORIZONTAL);
        statusPill.setGravity(Gravity.CENTER_VERTICAL);
        statusPill.setBackground(shape(C_CARD, C_CARD_BORDER, dp(1), dp(10)));
        statusPill.setPadding(dp(12), dp(8), dp(12), dp(8));

        statusDot = new TextView(this);
        statusDot.setText("● ");
        statusDot.setTextSize(14);
        statusDot.setTextColor(Color.parseColor("#F59E0B"));
        statusPill.addView(statusDot);

        statusText = new TextView(this);
        statusText.setText("Connecting Bluetooth...");
        statusText.setTextSize(13);
        statusText.setTextColor(C_TEXT_MUTED);
        statusPill.addView(statusText);
        root.addView(statusPill);

        // 3. Tab Selector
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackground(shape(C_CARD, C_CARD_BORDER, dp(1), dp(12)));
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tabParams.setMargins(0, dp(12), 0, dp(12));
        tabs.setLayoutParams(tabParams);

        Button tabRemote = cleanButton("📺 Remote", C_PRIMARY, C_PRIMARY_PRESSED, Color.WHITE, dp(8));
        Button tabTouch = cleanButton("🖱️ Mouse", Color.TRANSPARENT, C_BTN, C_TEXT_MUTED, dp(8));
        Button tabKeyb = cleanButton("⌨️ Keyboard", Color.TRANSPARENT, C_BTN, C_TEXT_MUTED, dp(8));

        tabRemote.setOnClickListener(v -> switchTab(0, tabRemote, tabTouch, tabKeyb));
        tabTouch.setOnClickListener(v -> switchTab(1, tabRemote, tabTouch, tabKeyb));
        tabKeyb.setOnClickListener(v -> switchTab(2, tabRemote, tabTouch, tabKeyb));

        tabs.addView(tabRemote);
        tabs.addView(tabTouch);
        tabs.addView(tabKeyb);
        root.addView(tabs);

        // 4. View Containers
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
                tabs[i].setBackground(shape(C_PRIMARY, Color.TRANSPARENT, 0, dp(8)));
                tabs[i].setTextColor(Color.WHITE);
                tabs[i].setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            } else {
                tabs[i].setBackground(shape(Color.TRANSPARENT, Color.TRANSPARENT, 0, dp(8)));
                tabs[i].setTextColor(C_TEXT_MUTED);
                tabs[i].setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            }
        }
    }

    // ---- 1. Simple Clean Remote View ----

    private View buildRemoteView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);

        // Top Row: Power, Mute, Back
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.addView(remoteBtn("⏻ Power", C_DANGER, C_DANGER_PRESSED, Color.WHITE, view -> consumer(C_POWER)));
        topRow.addView(remoteBtn("🔇 Mute", C_BTN, C_BTN_PRESSED, C_TEXT, view -> consumer(C_MUTE)));
        topRow.addView(remoteBtn("↩ Back", C_BTN, C_BTN_PRESSED, C_TEXT, view -> pressBack()));
        v.addView(topRow);

        // D-Pad Surface Card
        LinearLayout dpadCard = new LinearLayout(this);
        dpadCard.setOrientation(LinearLayout.VERTICAL);
        dpadCard.setGravity(Gravity.CENTER);
        dpadCard.setBackground(shape(C_CARD, C_CARD_BORDER, dp(1), dp(24)));
        dpadCard.setPadding(dp(16), dp(18), dp(16), dp(18));
        LinearLayout.LayoutParams dpadParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dpadParams.setMargins(0, dp(12), 0, dp(12));
        dpadCard.setLayoutParams(dpadParams);

        // Up
        dpadCard.addView(dpadRow(blank(), dpadArrow("▲", view -> key(K_UP)), blank()));

        // Left - OK - Right
        Button btnLeft = dpadArrow("◀", view -> key(K_LEFT));
        Button btnOk = dpadCenter("OK", view -> pressOk());
        // Long press OK also triggers play/pause as a helpful shortcut
        btnOk.setOnLongClickListener(view -> {
            pressPlayPause();
            updateStatus("⏯️ Play/Pause", true);
            return true;
        });
        Button btnRight = dpadArrow("▶", view -> key(K_RIGHT));
        dpadCard.addView(dpadRow(btnLeft, btnOk, btnRight));

        // Down
        dpadCard.addView(dpadRow(blank(), dpadArrow("▼", view -> key(K_DOWN)), blank()));

        v.addView(dpadCard);

        // Prominent Play/Pause Button (Single tap to pause/play any video app)
        Button btnPlayPause = cleanButton("⏯️ PLAY / PAUSE", C_SUCCESS, C_SUCCESS_PRESSED, Color.WHITE, dp(12));
        btnPlayPause.setTextSize(15);
        btnPlayPause.setOnClickListener(view -> {
            pressPlayPause();
            updateStatus("⏯️ Play / Pause", true);
        });
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(50));
        playParams.setMargins(0, 0, 0, dp(12));
        btnPlayPause.setLayoutParams(playParams);
        v.addView(btnPlayPause);

        // Volume & Home Row
        LinearLayout volRow = new LinearLayout(this);
        volRow.setOrientation(LinearLayout.HORIZONTAL);
        volRow.addView(remoteBtn("🔉 Vol -", C_BTN, C_BTN_PRESSED, C_TEXT, view -> consumer(C_VOL_DOWN)));
        volRow.addView(remoteBtn("🏠 Home", C_BTN, C_BTN_PRESSED, C_TEXT, view -> consumer(C_HOME)));
        volRow.addView(remoteBtn("🔊 Vol +", C_BTN, C_BTN_PRESSED, C_TEXT, view -> consumer(C_VOL_UP)));
        v.addView(volRow);

        // Media Navigation Row (Rewind, Menu, Fast Forward)
        LinearLayout navRow = new LinearLayout(this);
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        navRow.addView(remoteBtn("⏪ -10s", C_BTN, C_BTN_PRESSED, C_TEXT_MUTED, view -> {
            consumer(C_REWIND);
            key(K_LEFT);
        }));
        navRow.addView(remoteBtn("☰ Menu", C_BTN, C_BTN_PRESSED, C_TEXT_MUTED, view -> {
            key(K_MENU);
            key(K_ESC);
        }));
        navRow.addView(remoteBtn("⏩ +10s", C_BTN, C_BTN_PRESSED, C_TEXT_MUTED, view -> {
            consumer(C_FAST_FORWARD);
            key(K_RIGHT);
        }));
        v.addView(navRow);

        return v;
    }

    // ---- 2. Simple Clean Touchpad View ----

    private View buildTrackpadView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);

        // Touchpad Surface
        View pad = new View(this);
        int padHeight = dp(280);
        LinearLayout.LayoutParams padParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, padHeight);
        padParams.setMargins(0, 0, 0, dp(12));
        pad.setLayoutParams(padParams);
        pad.setBackground(shape(C_CARD, C_CARD_BORDER, dp(1), dp(16)));

        pad.setOnTouchListener(new View.OnTouchListener() {
            private float lastX, lastY;
            private long downTime;
            private boolean moved = false;
            private static final float SENSITIVITY = 2.4f;

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
                    case MotionEvent.ACTION_CANCEL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        // Tap to Click
                        if (!moved && (System.currentTimeMillis() - downTime) < 250) {
                            mouseClick(1);
                        }
                        return true;
                }
                return false;
            }
        });
        v.addView(pad);

        // Mouse Click Row
        LinearLayout clickRow = new LinearLayout(this);
        clickRow.setOrientation(LinearLayout.HORIZONTAL);
        clickRow.addView(remoteBtn("🖱️ Left Click", C_PRIMARY, C_PRIMARY_PRESSED, Color.WHITE, view -> mouseClick(1)));
        clickRow.addView(remoteBtn("🖱️ Right Click", C_BTN, C_BTN_PRESSED, C_TEXT, view -> mouseClick(2)));
        v.addView(clickRow);

        // Scroll Row
        LinearLayout scrollRow = new LinearLayout(this);
        scrollRow.setOrientation(LinearLayout.HORIZONTAL);
        scrollRow.addView(remoteBtn("▲ Scroll Up", C_BTN, C_BTN_PRESSED, C_TEXT_MUTED, view -> mouseScroll(3)));
        scrollRow.addView(remoteBtn("▼ Scroll Down", C_BTN, C_BTN_PRESSED, C_TEXT_MUTED, view -> mouseScroll(-3)));
        v.addView(scrollRow);

        return v;
    }

    // ---- 3. Simple Clean Keyboard View ----

    private View buildKeyboardView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);

        EditText input = new EditText(this);
        input.setHint("Type text to send to TV / Laptop...");
        input.setHintTextColor(Color.parseColor("#64748B"));
        input.setTextColor(Color.WHITE);
        input.setTextSize(15);
        input.setPadding(dp(14), dp(14), dp(14), dp(14));
        input.setBackground(shape(C_CARD, C_CARD_BORDER, dp(1), dp(12)));

        // Live typing
        input.addTextChangedListener(new TextWatcher() {
            private String prevText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isStreamingText) return;
                String cur = s.toString();
                if (cur.length() > prevText.length()) {
                    String added = cur.substring(prevText.length());
                    for (char c : added.toCharArray()) {
                        sendChar(c);
                    }
                } else if (cur.length() < prevText.length()) {
                    int diff = prevText.length() - cur.length();
                    for (int i = 0; i < diff; i++) {
                        key(K_BACKSPACE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        input.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            pressOk();
            return true;
        });
        v.addView(input);

        // Send Button
        Button btnSend = cleanButton("🚀 Send Text", C_SUCCESS, C_SUCCESS_PRESSED, Color.WHITE, dp(10));
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        sendParams.setMargins(0, dp(10), 0, dp(16));
        btnSend.setLayoutParams(sendParams);
        btnSend.setOnClickListener(view -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                sendString(text);
                input.setText("");
            }
        });
        v.addView(btnSend);

        // Quick Keycaps
        LinearLayout r1 = new LinearLayout(this);
        r1.setOrientation(LinearLayout.HORIZONTAL);
        r1.addView(remoteBtn("⌫ Backspace", C_BTN, C_BTN_PRESSED, C_TEXT, view -> key(K_BACKSPACE)));
        r1.addView(remoteBtn("↵ Enter", C_PRIMARY, C_PRIMARY_PRESSED, Color.WHITE, view -> pressOk()));
        v.addView(r1);

        LinearLayout r2 = new LinearLayout(this);
        r2.setOrientation(LinearLayout.HORIZONTAL);
        r2.addView(remoteBtn("␣ Space", C_BTN, C_BTN_PRESSED, C_TEXT, view -> key(K_SPACE)));
        r2.addView(remoteBtn("⇥ Tab", C_BTN, C_BTN_PRESSED, C_TEXT, view -> key(K_TAB)));
        r2.addView(remoteBtn("⎋ Esc", C_BTN, C_BTN_PRESSED, C_TEXT, view -> key(K_ESC)));
        v.addView(r2);

        return v;
    }

    // ---- UI Helper Builders ----

    private Button cleanButton(String text, int normalColor, int pressedColor, int textColor, int radiusDp) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(textColor);
        b.setTextSize(13);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        b.setBackground(buttonSelector(normalColor, pressedColor, C_CARD_BORDER, dp(1), dp(radiusDp)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(44), 1f);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        b.setLayoutParams(params);
        return b;
    }

    private Button remoteBtn(String text, int normalColor, int pressedColor, int textColor, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(textColor);
        b.setTextSize(13);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        b.setBackground(buttonSelector(normalColor, pressedColor, C_BTN_BORDER, dp(1), dp(12)));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
        params.setMargins(dp(3), dp(3), dp(3), dp(3));
        b.setLayoutParams(params);
        return b;
    }

    private Button dpadArrow(String arrow, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(arrow);
        b.setTextSize(18);
        b.setTextColor(C_TEXT);
        b.setBackground(buttonSelector(C_BTN, C_BTN_PRESSED, C_BTN_BORDER, dp(1), dp(14)));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(56), 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(params);
        return b;
    }

    private Button dpadCenter(String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        b.setBackground(buttonSelector(C_PRIMARY, C_PRIMARY_PRESSED, Color.parseColor("#60A5FA"), dp(2), dp(28)));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(56), 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
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
