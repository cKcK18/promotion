package com.ken.promotion;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CourseDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_detail);

		setUpView();
	}

	@SuppressWarnings("unused")
	private void setUpView() {
		final ImageView back = (ImageView) findViewById(R.id.promotion_detail_back_button);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		final ImageView share = (ImageView) findViewById(R.id.promotion_detail_share_button);

		final ImageView image = (ImageView) findViewById(R.id.image_with_caption);

		final TextView phoneNumberOnImage = (TextView) findViewById(R.id.image_with_caption_phone);

		final TextView contentOnImage = (TextView) findViewById(R.id.image_with_caption_content);

		final TextView header = (TextView) findViewById(R.id.image_with_caption_header);

		final TextView secondHeader = (TextView) findViewById(R.id.image_with_caption_second_header);

		final TextView dealine = (TextView) findViewById(R.id.course_deadline);

		final TextView teacher = (TextView) findViewById(R.id.course_teacher);

		final TextView cost = (TextView) findViewById(R.id.course_cost);

		final TextView location = (TextView) findViewById(R.id.course_location);

		final TextView consultation = (TextView) findViewById(R.id.course_consultation);

		final TextView email = (TextView) findViewById(R.id.course_email);

		final Button consultationButton = (Button) findViewById(R.id.course_consultation_button);
		consultationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + "0987654321"));
				startActivity(intent);
			}
		});

		final Button emailConsultationButton = (Button) findViewById(R.id.course_email_consultation_button);
		emailConsultationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("mailto:" + email.getText());
				Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
				startActivity(intent);
			}
		});
	}
}
