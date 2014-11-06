package com.ken.promotion;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PromotionDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_promotion_detail);

		setUpView();
	}

	@SuppressWarnings("unused")
	private void setUpView() {
		final ImageView back = (ImageView) findViewById(R.id.promotion_detail_back_button);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// finish();
				startActivity(new Intent(PromotionDetailActivity.this, CourseDetailActivity.class));
			}
		});
		final ImageView share = (ImageView) findViewById(R.id.promotion_detail_share_button);

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
		retrieve.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		final TextView address = (TextView) findViewById(R.id.promotion_address);

		final TextView phoneNumber = (TextView) findViewById(R.id.promotion_phone_number);

		final TextView businessHours = (TextView) findViewById(R.id.promotion_business_hours);

		final TextView retrieveMapButton = (TextView) findViewById(R.id.promotion_retrieve_map);
		retrieveMapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				retrieveGoogleMap(address.getText().toString());
			}
		});
		final TextView call = (TextView) findViewById(R.id.promotion_call);
		call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + "0987654321"));
				startActivity(intent);
			}
		});
	}

	private void retrieveGoogleMap(String targetAddress) {
		List<Address> addresses = null;
		int retry = 0;

		final Geocoder geocoder = new Geocoder(this);
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
			startActivity(it);
		} else {
			Toast.makeText(this, "address not found !", Toast.LENGTH_SHORT).show();
		}
	}
}
