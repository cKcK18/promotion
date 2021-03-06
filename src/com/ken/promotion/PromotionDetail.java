package com.ken.promotion;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PromotionDetail extends LinearLayout {

	public PromotionDetail(Context context) {
		this(context, null);
	}

	public PromotionDetail(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PromotionDetail(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setUpView(context);
	}

	protected void setUpView(Context context) {
	}

	@SuppressWarnings("unused")
	@Override
	protected void onFinishInflate() {
		final ImageView image = (ImageView) findViewById(R.id.image_with_caption);
		final TextView phoneNumberOnImage = (TextView) findViewById(R.id.image_with_caption_phone);
		final TextView contentOnImage = (TextView) findViewById(R.id.image_with_caption_content);
		final TextView header = (TextView) findViewById(R.id.image_with_caption_header);
		final TextView secondHeader = (TextView) findViewById(R.id.image_with_caption_second_header);
		final TextView city = (TextView) findViewById(R.id.image_with_caption_city);
		final TextView division = (TextView) findViewById(R.id.image_with_caption_division);
		final TextView mainContent = (TextView) findViewById(R.id.image_with_caption_main_content);
		final TextView tradeName = (TextView) findViewById(R.id.promotion_trade_name);
		final TextView retrieve = (TextView) findViewById(R.id.promotion_retrieve);
		final TextView address = (TextView) findViewById(R.id.promotion_address);
		final TextView phoneNumber = (TextView) findViewById(R.id.promotion_phone_number);
		final TextView businessHours = (TextView) findViewById(R.id.promotion_business_hours);

		final Button retrieveMapButton = (Button) findViewById(R.id.promotion_retrieve_map);
		retrieveMapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				retrieveGoogleMap(address.getText().toString());
			}
		});
		final Button call = (Button) findViewById(R.id.promotion_call);
		call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Context context = getContext();
				final Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + "0987654321"));
				context.startActivity(intent);
			}
		});
	}

	private void retrieveGoogleMap(String targetAddress) {
		final Context context = getContext();
		List<Address> addresses = null;
		int retry = 0;

		final Geocoder geocoder = new Geocoder(context);
		do {
			try {
				addresses = geocoder.getFromLocationName(targetAddress, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			++retry;
		} while ((addresses == null || addresses.size() == 0) && retry < 3);

		if (addresses != null && addresses.size() > 0) {
			final Address address = addresses.get(0);
			final double latitude = address.getLatitude();
			final double longitude = address.getLongitude();

			final Uri uri = Uri.parse(String.format("geo:%f,%f?z=%d", latitude, longitude, 18));
			final Intent it = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(it);
		} else {
			Toast.makeText(context, "address not found !", Toast.LENGTH_SHORT).show();
		}
	}
}
