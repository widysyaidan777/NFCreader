package com.widy.appwidy;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.widy.appwidy.parser.NdefMessageParser;
import com.widy.appwidy.record.ParserNdefRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ReaderActivity extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);
        text = (TextView) findViewById(R.id.textView);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "NO NFC CAPABILITIES", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;

            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                {
                    for (int i = 0; i < rawMsgs.length; i++) {
                        msgs[i] = (NdefMessage) rawMsgs[i];
                    }
                }
            } else {
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }
            displayMsgs(msgs);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    private void displayMsgs(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<ParserNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();

        for (int i = 0; i < size; i++) {
            ParserNdefRecord record = records.get(i);
            String str = record.str();
            builder.append(str).append("\n");
        }
        text.setText(builder.toString());
    }

    private String dumpTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareClassic = MifareClassic.get(tag);
                    mifareClassic.connect();
                    boolean authA = mifareClassic.authenticateSectorWithKeyA(2, MifareClassic.KEY_NFC_FORUM);
                    boolean authB = mifareClassic.authenticateSectorWithKeyB(2, MifareClassic.KEY_DEFAULT);
                    Log.i("hey", "authA : " + authA);
                    Log.i("hey", "authB" + authB);
                    if (authB && authA) {
                        byte[] bWrite = new byte[16];
                        byte[] hello = "hello".getBytes(StandardCharsets.US_ASCII);
                        System.arraycopy(hello, 0, bWrite, 0, hello.length);
                        mifareClassic.writeBlock(0, bWrite);
                        Log.i("hey", "write : " + Arrays.toString(bWrite));

                        byte[] bRead = mifareClassic.readBlock(0);
                        String str = new String(bRead, StandardCharsets.US_ASCII);
                        Log.i("hey", "read bytes : " + Arrays.toString(bRead));
                        Log.i("hey", "read string: " + str);
                        Toast.makeText(this, "read : " + str, Toast.LENGTH_SHORT).show();
                        Log.i("hey", "expected : " + new String(bWrite, StandardCharsets.US_ASCII));
                    }
                    mifareClassic.close();
                } catch (IOException e) {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    Log.i("hey", "Error");
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        return sb.toString();
    }
}
