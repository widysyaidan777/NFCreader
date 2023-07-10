package com.widy.appwidy;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.widy.appwidy.parser.NdefMessageParser;
import com.widy.appwidy.record.ParserNdefRecord;

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

        if (nfcAdapter == null){
            Toast.makeText(this, "NO NFC CAPABILITIES", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (nfcAdapter != null){
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }
    protected void onPause(){
        super.onPause();
        if (nfcAdapter != null){
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent){
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;

            if (rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];{
                    for (int i = 0; i < rawMsgs.length; i++){
                        msgs[i] = (NdefMessage) rawMsgs[i];
                    }
                }
            } else {
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                msgs = new NdefMessage[] {msg};
            }
            displayMsgs(msgs);
        }
    }
    private void showWirelessSettings(){
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    private void displayMsgs(NdefMessage[] msgs){
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<ParserNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();

        for (int i = 0; i < size; i++){
            ParserNdefRecord record = records.get(i);
            String str = record.str();
            builder.append(str).append("\n");
        }
        text.setText(builder.toString());
    }
    private String dumpTagData(Tag tag){
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex):").append(toHex(id)).append('\n');
        sb.append('\n');
        sb.append("ID (dec):").append(toDec(id)).append('\n');
        sb.append('\n');
        sb.append("ID (reverse hex):").append(toReversedHex(id)).append('\n');
        sb.append('\n');
        sb.append("ID (reverse dec):").append(toReversedDec(id)).append('\n');
        sb.append('\n');

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()){
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()){
            if (tech.equals(MifareClassic.class.getName())){
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);
                    mifareTag.connect();

                    switch (mifareTag.getType()){
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append('\n');

                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append('\n');
                    sb.append("Mifare Size: ");
                    sb.append(mifareTag.getSize());
                    sb.append('\n');

                    sb.append('\n');
                    sb.append("Mifare Timeout: ");
                    sb.append(mifareTag.getTimeout());
                    sb.append('\n');

                    sb.append('\n');
                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append(", ");
                    sb.append("blocks: ");
                    sb.append(mifareTag.getBlockCountInSector(0));
                    sb.append(", ");
                    sb.append("bytes: ");
                    sb.append(mifareTag.getBlockCountInSector(0) * mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append('\n');
                    sb.append("Mifare Max Transceive Length: ");
                    sb.append(mifareTag.getMaxTransceiveLength());
                    sb.append('\n');


                } catch (Exception e){
                    sb.append("Mifare classic error: " + e.getMessage());
                }
            }
            if (tech.equals(NfcA.class.getName())) {
                sb.append('\n');
                NfcA nfcA = NfcA.get(tag);
                if (nfcA != null) {
                    try {
                        nfcA.connect();
                        nfcA.transceive(new byte[]{
                                (byte) 0xA2,
                                (byte) 0x03,
                                (byte) 0xE1, (byte) 0x10, (byte) 0x06, (byte) 0x00
                        });
                        nfcA.transceive(new byte[]{
                                (byte) 0xA2,
                                (byte) 0x04,
                                (byte) 0x03, (byte) 0x00, (byte) 0xFE, (byte) 0x00
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            nfcA.close();
                            sb.append('\n');
                            sb.append("ATQA: ");
                            sb.append(nfcA.getAtqa());
                            sb.append('\n');

                            sb.append('\n');
                            sb.append("SAK: ");
                            sb.append(nfcA.getSak());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (tech.equals(MifareUltralight.class.getName())){
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()){
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
    private String toHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i){
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0 ){
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    private String toReversedHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++){
            if (i > 0){
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }
    private long toDec(byte[] bytes){
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i){
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }
    private long toReversedDec(byte[] bytes){
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i){
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }
}

