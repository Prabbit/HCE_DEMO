package com.example.activity;


import java.nio.charset.Charset;
import java.util.Locale;

import com.example.R;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;

public class ActivitySendCardData extends Activity {

	private NfcAdapter mNfcAdapter;
	private NdefMessage mNdefMessage;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		setContentView(R.layout.activity_send_card_data);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


		mNdefMessage = new NdefMessage(
				new NdefRecord[] {
						createNewTextRecord(this.getIntent().getStringExtra("CARDDATA"), Locale.ENGLISH, true)}); 
	}

	public static NdefRecord createNewTextRecord(String text, Locale locale, boolean encodeInUtf8) {
		byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

		Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
		byte[] textBytes = text.getBytes(utfEncoding);

		int utfBit = encodeInUtf8 ? 0 : (1 << 7);
		char status = (char)(utfBit + langBytes.length);

		byte[] data = new byte[1 + langBytes.length + textBytes.length]; 
		data[0] = (byte)status;
		System.arraycopy(langBytes, 0, data, 1, langBytes.length);
		System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		super.onResume();

		if (mNfcAdapter != null)
			mNfcAdapter.enableForegroundNdefPush(this, mNdefMessage);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPause() {
		super.onPause();

		if (mNfcAdapter != null)
			mNfcAdapter.disableForegroundNdefPush(this);
	}
}
