package com.ihub.android.app;

import java.util.ArrayList;
import java.util.List;

import com.ihub.android.app.data.IhubDatabaseHelper;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import greendroid.widget.item.SubtextItem;

public class ShowProfile extends GDListActivity{

	private String memberID, name, occupation, image;
	private IhubDatabaseHelper dbHelper;
	private Button btnShowFullProfile, btnCancel;
	private Person person;
	private TextView txtNames, txtOccupation;
	private ImageView imgProfilePic;
	private ListView member_details_list;
	private String TAG = "ShowProfile";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle("Member Profile");
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		memberID = bundle.getString("MemberID");
		name = bundle.getString("Name");
		occupation = bundle.getString("Occupation");
		image = bundle.getString("image");
		person = new Person();
		
		setActionBarContentView(R.layout.show_profile);
		
		dbHelper = new IhubDatabaseHelper(this);
		dbHelper.createDatabase();
		dbHelper.createTable();
		
		Cursor cursor = dbHelper.getMember(Long.parseLong(memberID));
		cursor.moveToFirst();
		int i = 0;
		do {
			person.setFirstName(cursor.getString(1));
			person.setLastName(cursor.getString(2));
			person.setCountry(cursor.getString(3));
			person.setQrCode(cursor.getString(4));
			person.setProfilePic(cursor.getString(5));
			person.setOccupation(cursor.getString(6));
			person.setCloudUserID(cursor.getString(7));
			person.setMemberType(cursor.getString(8));
			person.setTelephone(cursor.getString(9));
			person.setEmailAddress(cursor.getString(10));
			i++;
		} while (cursor.moveToNext());
		
		cursor.close();
		Log.w(TAG, "Telephone - "+person.getTelephone()+":: Email Add - "+person.getEmailAddress());
		
		txtNames = (TextView) findViewById(R.id.txt_name);
		txtOccupation = (TextView) findViewById(R.id.txt_occupation);
		imgProfilePic = (ImageView) findViewById(R.id.profile_pic);
		Log.w(TAG, "NAmes: " + person.getFirstName() + " "
				+ person.getLastName());
		txtNames.setText(person.getFirstName());

		member_details_list = (ListView) findViewById(R.id.member_details_list);
		List<Item> items = new ArrayList<Item>();

		items.add(new SubtextItem("Name ", person.getFirstName()+" "+person.getLastName()));
		items.add(new SubtextItem("Occupation", person.getOccupation()));
		items.add(new SubtextItem("Email Address", person.getEmailAddress()));
		final ItemAdapter adapter = new ItemAdapter(this, items);
		
		member_details_list.setAdapter(adapter);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap bm = BitmapFactory.decodeFile("/sdcard/ihub/pics/"
				+ person.getProfilePic(), options);

		imgProfilePic.setImageBitmap(bm);
		
		btnShowFullProfile = (Button) findViewById(R.id.btn_view_full_profile);
		btnShowFullProfile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ihub.co.ke"));
				startActivity(intent);
			}
		});
		
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		
	}
	
}
