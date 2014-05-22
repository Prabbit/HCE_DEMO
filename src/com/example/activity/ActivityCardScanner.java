package com.example.activity;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import java.nio.charset.Charset;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;

public class ActivityCardScanner extends Activity implements ReaderCallback
{
	private static final String MY_CARDIO_APP_TOKEN = "f5c575f4b0da4410838660f5d3e962f8";
	private Button mScanButton;
	private TextView mCardDataView;  
	private String mCardDetails;  

	private int MY_SCAN_REQUEST_CODE = 100; 

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_scanner);
		setupUI();


	}



	private void setupUI() {
		mCardDataView = (TextView)findViewById(R.id.resultTextView);
		mScanButton = (Button)findViewById(R.id.scanButton);
	}





	@Override
	protected void onResume() {
		super.onResume();

		if (CardIOActivity.canReadCardWithCamera(this)) {
			mScanButton.setText("Scan a Credit Card");
		}
		else {
			mScanButton.setText("Enter credit card information");
		}
	}

	public void onScanPress(View v) {

		Intent scanIntent = new Intent(this, CardIOActivity.class);
		scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, MY_CARDIO_APP_TOKEN);
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false);
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); 
		scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false); 
		startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == MY_SCAN_REQUEST_CODE) {
			if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
				CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

				mCardDetails = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";

				if (scanResult.isExpiryValid()) {
					mCardDetails += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
				}

				if (scanResult.cvv != null) {
					mCardDetails += "CVV has " + scanResult.cvv.length() + " digits.\n";
				}


				if (scanResult.postalCode != null) {
					mCardDetails += "Postal Code: " + scanResult.postalCode + "\n";
				}
			}
			else {
				mCardDetails = "Scan was canceled.";
			}
			mCardDataView.setText(mCardDetails);
		}
	}



	@Override
	public void onTagDiscovered(Tag tag) {
		Log.i("HCE DEMO", "TAG FOUND");
	}

	public void onSendCardDetails(View v){
		if(mCardDetails!=null && !mCardDetails.isEmpty()){
			Intent lIntent = new Intent(ActivityCardScanner.this, ActivitySendCardData.class);
			lIntent.putExtra("CARDDATA", mCardDetails);
			startActivity(lIntent);
		}
		else
			Toast.makeText(this, "Card Details not available. Scan the card to get the details.", 0).show();

	}


	public static NdefRecord createCardDataRecord(String text, Locale locale, boolean encodeInUtf8) {
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

}

