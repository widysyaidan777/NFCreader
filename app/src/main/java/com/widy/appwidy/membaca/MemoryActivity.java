package com.widy.appwidy.membaca;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.widy.appwidy.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

public class MemoryActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    TextView textViewInfo;
    final static String TAG = "NFC Reader";
    IntentFilter writeTagFilters[];
    EditText et;
    Button btn;
    String content;
    static NdefMessage message;
    public static final byte[] KEY_DEFAULT =
            {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};

    private final String[][] techList = new String[][]{
            new String[]{
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName()
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memory_activity);

        textViewInfo = (TextView) findViewById(R.id.textViewOne);
        textViewInfo = (TextView) findViewById(R.id.textViewTwo);
        textViewInfo = (TextView) findViewById(R.id.textViewThree);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NO NFC CAPABILITIES",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        String[][] enableFlags = new String[][] {
                new String[] {
                        NfcA.class.getName(),
                        NfcB.class.getName(),
                        NfcV.class.getName(),
                        NfcF.class.getName(),
                        IsoDep.class.getName()
                }
        };
        writeTagFilters = new IntentFilter[]{
                tagDetected
        };
    }


    private View.OnClickListener onclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            content = et.getText().toString();
            message = createTextMessage(content);
            Log.v(TAG,content);
        }
    };
    private void readFromIntent (Intent intent){
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++){
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs){
        if (msgs == null || msgs.length == 0) return;
        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;

        try {
            text = new String(payload, languageCodeLength+1, payload.length-languageCodeLength-1,textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnssuportedEncoding", e.toString());
        }
        textViewInfo.append("\nNFC Content: "+text);
    }



    @Override
    protected void onResume() {
        super.onResume();
        assert nfcAdapter != null;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }
    protected void onPause(){
        super.onPause();
        if (nfcAdapter != null){
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
        readFromIntent(intent);

    }


    public NdefMessage createTextMessage(String content){
        try {
            byte[] lang = Locale.getDefault().getLanguage().getBytes("UTF-8");
            byte[] text = content.getBytes("UTF-8");
            int langSize = lang.length;
            int textLength = text.length;

            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
            payload.write((byte) (langSize & 0x1f));
            payload.write(lang, 0, langSize);
            payload.write(text, 0, textLength);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT, new byte[0],
                    payload.toByteArray());
            return new NdefMessage(new NdefRecord[]{record});
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage[] msgs;
            NfcA nfcA = NfcA.get(tag);
            assert tag != null;
            byte[] payload = detectTagData(tag).getBytes();
            writeTag2(tag, message);
        }
    }

    private String detectTagData(Tag tag){

        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex): ").append(toHex(id)).append('\n');
        sb.append('\n');
        sb.append("ID (dec): ").append(toDec(id)).append('\n');
        sb.append('\n');
        sb.append("ID (reverse hex):").append(toReversedHex(id)).append('\n');
        sb.append('\n');
        sb.append("ID (reverse dec):").append(toReversedDec(id)).append('\n');
        sb.append('\n');


        String prefix = "android.nfc.tech";
        sb.append("TagTechnology: ");
        for (String tech : tag.getTechList()){
            sb.append(tech.substring(prefix.length()));
            sb.append(" , ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";
                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    mifareTag.connect();
                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                        case MifareClassic.SIZE_1K:
                            type = "Size_1K";
                            break;
                        case MifareClassic.SIZE_2K:
                            type = "Size_2K";
                            break;
                        case MifareClassic.SIZE_4K:
                            type = "Size_4K";
                            break;
                    }

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

                    sb.append('\n');
                    sb.append("Authentication with Key A: ");
                    sb.append(mifareTag.authenticateSectorWithKeyA(1, MifareClassic.KEY_NFC_FORUM));
                    sb.append('\n');

                    sb.append('\n');
                    sb.append("Authentication with Key B: ");
                    sb.append(mifareTag.authenticateSectorWithKeyB(1, MifareClassic.KEY_DEFAULT));
                    sb.append('\n');

                    mifareTag.close();

                } catch (Exception e) {
                    sb.append("Mifare Classic error" + e.getMessage());
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
                            sb.append(toHex(nfcA.getAtqa()));
                            sb.append('\n');

                            sb.append('\n');
                            sb.append("SAK: ");
                            sb.append(nfcA.getSak());
                            sb.append('\n');
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
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
        Log.v("test", sb.toString());
        textViewInfo.setText(sb);
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
    private String toHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i){
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0){
                sb.append(" ");
            }
        }
        return sb.toString();
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

    public void writeTag2(Tag tag, NdefMessage message){
        if (tag != null){
            try {
                Ndef ndefTag = Ndef.get(tag);
                if (ndefTag == null){
                    NdefFormatable nForm = NdefFormatable.get(tag);
                    if (nForm != null){
                        nForm.connect();
                        nForm.format(message);
                        nForm.close();
                    }
                } else {
                    ndefTag.connect();
                    ndefTag.writeNdefMessage(message);
                    ndefTag.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void writeTag (MifareUltralight mifareUlTag){
        try {
            mifareUlTag.connect();
            mifareUlTag.writePage(4, "get".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(5,"fast".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(6, "NFC".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(7, "now".getBytes(Charset.forName("US-ASCII")));
            Log.e(TAG, "write success");
        } catch (IOException e){
            Log.e(TAG, "IOException while writing MifareUltraLight...", e);
        }finally {
            try {
                mifareUlTag.close();
            } catch (IOException e){
                Log.e(TAG, "IOException while closing MifareUltralight");
            }
        }
    }
    public String readTag (MifareUltralight mifareUlTag){
        try {
            mifareUlTag.connect();
            byte[] payload = mifareUlTag.readPages(4);
            return new String(payload, Charset.forName("US-ASCII"));
        }catch (IOException e){
            Log.e(TAG, "IOException while reading MifareUltraLight ...", e);
        }finally {
            if (mifareUlTag != null){
                try {
                    mifareUlTag.close();
                } catch (IOException e){
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
        return null;
    }
}
