package com.widy.appwidy.parser;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.widy.appwidy.record.ParserNdefRecord;
import com.widy.appwidy.record.SmartPoster;
import com.widy.appwidy.record.TextRecord;
import com.widy.appwidy.record.UriRecord;

import java.util.ArrayList;
import java.util.List;

public class NdefMessageParser {
    private NdefMessageParser() {
    }
    public static List<ParserNdefRecord> parse(NdefMessage message){
        return getRecords(message.getRecords());
    }
    public static List<ParserNdefRecord> getRecords(NdefRecord[] records){
        List<ParserNdefRecord> elements = new ArrayList<ParserNdefRecord>();

        for (final NdefRecord record : records) {
            if (UriRecord.isUri(record)) {
                elements.add(UriRecord.parse(record));
            } else if (TextRecord.isText(record)) {
                elements.add(TextRecord.parse(record));
            } else if (SmartPoster.isPoster(record)) {
                elements.add(SmartPoster.parse(record));
            } else {
                elements.add(new ParserNdefRecord() {
                    public String str() {
                        return new String(record.getPayload());
                    }
                });
            }
        }
        return elements;
    }
}

