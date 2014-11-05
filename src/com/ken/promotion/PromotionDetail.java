package com.ken.promotion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

	@Override
	protected void onFinishInflate() {
		final Button retrieveMapButton = (Button) findViewById(R.id.promotion_retrieve_map);
		retrieveMapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Context context = getContext();
				List<Address> addresses = new ArrayList<Address>();
				int retry = 0;

				Geocoder geocoder = new Geocoder(context);
				while (addresses.size() == 0 && retry < 3) {
					try {
						addresses = geocoder.getFromLocationName("台北火車站", 1);
					} catch (IOException e) {
						e.printStackTrace();
					}
					++retry;
				}
				if (addresses.size() > 0) {
					final Address address = addresses.get(0);
					final double latitude = address.getLatitude();
					final double longitude = address.getLongitude();

					final Uri uri = Uri.parse(String.format("geo:%f,%f?z=%d", latitude, longitude, 17));
					final Intent it = new Intent(Intent.ACTION_VIEW, uri);
					context.startActivity(it);
				} else {
					Toast.makeText(context, "address not found !", Toast.LENGTH_SHORT).show();
				}
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
}
