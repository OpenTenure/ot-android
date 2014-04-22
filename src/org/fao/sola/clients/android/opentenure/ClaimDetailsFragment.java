/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.fao.sola.clients.android.opentenure;

import java.io.File;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.LoginActivity;
import org.fao.sola.clients.android.opentenure.network.LogoutTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

public class ClaimDetailsFragment extends Fragment {

	View rootView;
	private ClaimDispatcher claimActivity;
	final Calendar localCalendar = Calendar.getInstance();
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	File claimantPictureFile;
	Menu menu;
	ImageView claimantImageView;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			claimActivity = (ClaimDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ClaimDispatcher");
		}
	}


	public ClaimDetailsFragment() {}


	@Override
	public void onPrepareOptionsMenu(Menu menu){
		MenuItem itemIn;
		MenuItem itemOut;		
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Is the user logged in ? : " + OpenTenureApplication.isLoggedin());

		
		if(OpenTenureApplication.isLoggedin()){
			
		itemIn = menu.getItem(4);
		itemIn.setVisible(false);
		itemOut = menu.getItem(5);
		itemOut.setVisible(true);
		
		}
		else{
			
			itemIn = menu.getItem(4);
			itemIn.setVisible(true);
			itemOut = menu.getItem(5);
			itemOut.setVisible(false);
		}
		
		this.menu = menu;
		super.onPrepareOptionsMenu(menu);
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


		inflater.inflate(R.menu.claim_details, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_claim_details, container,
				false);
		setHasOptionsMenu(true);
		InputMethodManager imm = (InputMethodManager) rootView.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

		EditText dateOfBirth = (EditText) rootView
				.findViewById(R.id.date_of_birth_input_field);

		final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				localCalendar.set(Calendar.YEAR, year);
				localCalendar.set(Calendar.MONTH, monthOfYear);
				localCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateLabel();
			}

		};

		dateOfBirth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new DatePickerDialog(rootView.getContext(), date, localCalendar
						.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH),
						localCalendar.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		claimantImageView = (ImageView) rootView
				.findViewById(R.id.claimant_picture);
		claimantImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (claimantPictureFile != null) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(claimantPictureFile));
					startActivityForResult(intent,
							CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				} else {
					Toast toast = Toast.makeText(rootView.getContext(),
							R.string.message_save_before_adding_content,
							Toast.LENGTH_SHORT);
					toast.show();
				}

			}
		});
		if (claimActivity.getClaimId() != null) {
			load(claimActivity.getClaimId());
		}

		return rootView;
	}

	private void load(String claimId) {
		Claim claim = Claim.getClaim(claimId);
		((EditText) rootView.findViewById(R.id.first_name_input_field))
		.setText(claim.getPerson().getFirstName());
		((EditText) rootView.findViewById(R.id.last_name_input_field))
		.setText(claim.getPerson().getLastName());
		((EditText) rootView.findViewById(R.id.date_of_birth_input_field))
		.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US)
		.format(claim.getPerson().getDateOfBirth()));
		((EditText) rootView.findViewById(R.id.place_of_birth_input_field))
		.setText(claim.getPerson().getPlaceOfBirth());
		((EditText) rootView.findViewById(R.id.postal_address_input_field))
		.setText(claim.getPerson().getPostalAddress());
		((EditText) rootView.findViewById(R.id.email_address_input_field))
		.setText(claim.getPerson().getEmailAddress());
		((RadioButton) rootView.findViewById(R.id.gender_feminine_input_field))
		.setChecked((claim.getPerson().getGender().equals("F")));
		((RadioButton) rootView.findViewById(R.id.gender_masculine_input_field))
		.setChecked((claim.getPerson().getGender().equals("M")));
		((EditText) rootView.findViewById(R.id.mobile_phone_number_input_field))
		.setText(claim.getPerson().getMobilePhoneNumber());
		((EditText) rootView
				.findViewById(R.id.contact_phone_number_input_field))
				.setText(claim.getPerson().getContactPhoneNumber());
		((EditText) rootView.findViewById(R.id.claim_name_input_field))
		.setText(claim.getName());
		claimantPictureFile = Person.getPersonPictureFile(claim.getPerson()
				.getPersonId());
		try {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				claimantImageView.setImageBitmap(Person.getPersonPicture(
						rootView.getContext(), claimantPictureFile, 128));
			} else {
				claimantImageView.setImageBitmap(Person.getPersonPicture(
						rootView.getContext(), claimantPictureFile, 128));
			}
		} catch (Exception e) {
			claimantImageView.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_contact_picture));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				try {
					claimantImageView.setImageBitmap(Person.getPersonPicture(
							rootView.getContext(), claimantPictureFile, 128));
				} catch (Exception e) {
					claimantImageView.setImageDrawable(getResources()
							.getDrawable(R.drawable.ic_contact_picture));
				}
			}
		}
	}

	private void updateLabel() {

		EditText dateOfBirth = (EditText) getView().findViewById(
				R.id.date_of_birth_input_field);
		String myFormat = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		dateOfBirth.setText(sdf.format(localCalendar.getTime()));
	}

	public void saveClaim() {
		Person person = new Person();
		person.setFirstName(((EditText) rootView
				.findViewById(R.id.first_name_input_field)).getText()
				.toString());
		person.setLastName(((EditText) rootView
				.findViewById(R.id.last_name_input_field)).getText().toString());
		java.util.Date dob;
		try {
			dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
			.parse(((EditText) rootView
					.findViewById(R.id.date_of_birth_input_field))
					.getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
			dob = new java.util.Date();
		}
		person.setDateOfBirth(new Date(dob.getTime()));
		person.setPlaceOfBirth(((EditText) rootView
				.findViewById(R.id.place_of_birth_input_field)).getText()
				.toString());
		person.setPostalAddress(((EditText) rootView
				.findViewById(R.id.postal_address_input_field)).getText()
				.toString());
		person.setEmailAddress(((EditText) rootView
				.findViewById(R.id.email_address_input_field)).getText()
				.toString());
		person.setMobilePhoneNumber(((EditText) rootView
				.findViewById(R.id.mobile_phone_number_input_field)).getText()
				.toString());
		person.setContactPhoneNumber(((EditText) rootView
				.findViewById(R.id.contact_phone_number_input_field)).getText()
				.toString());
		if(((RadioButton) rootView
				.findViewById(R.id.gender_feminine_input_field)).isChecked())
			person.setGender("F");
		if(((RadioButton) rootView
				.findViewById(R.id.gender_masculine_input_field)).isChecked())
			person.setGender("M");
		person.create();

		Claim claim = new Claim();
		claim.setPerson(person);
		claim.setName(((EditText) rootView
				.findViewById(R.id.claim_name_input_field)).getText()
				.toString());
		if (claim.create() == 1) {

			FileSystemUtilities.createClaimFileSystem(claim.getClaimId());

			claimActivity.setClaimId(claim.getClaimId());
			claimantPictureFile = Person.getPersonPictureFile(claim.getPerson()
					.getPersonId());
		}
	}

	public void updateClaim() {

		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		claim.setName(((EditText) rootView
				.findViewById(R.id.claim_name_input_field)).getText()
				.toString());
		claim.update();
		claim.getPerson().setFirstName(
				((EditText) rootView.findViewById(R.id.first_name_input_field))
				.getText().toString());
		claim.getPerson().setLastName(
				((EditText) rootView.findViewById(R.id.last_name_input_field))
				.getText().toString());
		java.util.Date dob;
		try {
			dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
			.parse(((EditText) rootView
					.findViewById(R.id.date_of_birth_input_field))
					.getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
			dob = new java.util.Date();
		}
		claim.getPerson().setDateOfBirth(new Date(dob.getTime()));
		claim.getPerson().setPlaceOfBirth(
				((EditText) rootView
						.findViewById(R.id.place_of_birth_input_field))
						.getText().toString());
		claim.getPerson().setPostalAddress(
				((EditText) rootView
						.findViewById(R.id.postal_address_input_field))
						.getText().toString());
		claim.getPerson().setEmailAddress(
				((EditText) rootView
						.findViewById(R.id.email_address_input_field))
						.getText().toString());
		claim.getPerson().setMobilePhoneNumber(
				((EditText) rootView
						.findViewById(R.id.mobile_phone_number_input_field))
						.getText().toString());
		claim.getPerson().setContactPhoneNumber(
				((EditText) rootView
						.findViewById(R.id.contact_phone_number_input_field))
						.getText().toString());		
		if(((RadioButton) rootView
				.findViewById(R.id.gender_feminine_input_field)).isChecked())
			claim.getPerson().setGender("F");
		if(((RadioButton) rootView
				.findViewById(R.id.gender_masculine_input_field)).isChecked())
			claim.getPerson().setGender("M");
		claim.getPerson().update();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
		switch (item.getItemId()) {
		

		
		
		case R.id.action_save:

			if (claimActivity.getClaimId() == null) {
				saveClaim();
				if (claimActivity.getClaimId() != null) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();
				} else {
					toast = Toast
							.makeText(rootView.getContext(),
									R.string.message_unable_to_save,
									Toast.LENGTH_SHORT);
					toast.show();
				}
			} else {
				updateClaim();
				if (claimActivity.getClaimId() != null) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();
				} else {
					toast = Toast
							.makeText(rootView.getContext(),
									R.string.message_unable_to_save,
									Toast.LENGTH_SHORT);
					toast.show();
				}
			}

			return true;
		case R.id.action_submit:

			if(!OpenTenureApplication.isLoggedin()){
				toast = Toast
						.makeText(rootView.getContext(),
								R.string.message_login_before,
								Toast.LENGTH_SHORT);
				toast.show();
				return true;				

			}
			else{

				if (claimActivity.getClaimId() != null) {				
					
					
					JsonUtilities.
						createClaimJson(claimActivity.getClaimId());
					List<Vertex> vertices = Vertex.getVertices(claimActivity.getClaimId());
					Log.d(this.getClass().getName(), "mapGeometry: " + Vertex.mapWKTFromVertices(vertices));
					Log.d(this.getClass().getName(), "gpsGeometry: " + Vertex.gpsWKTFromVertices(vertices));
					
					/*
					 * A temporary Moke Submission of the Claim
					 * */
					Moke.mokeSubmit(claimActivity.getClaimId());

					toast = Toast.makeText(rootView.getContext(),
							R.string.message_submitted, Toast.LENGTH_SHORT);
					toast.show();
				} else {
					toast = Toast
							.makeText(rootView.getContext(),
									R.string.message_save_before_submit,
									Toast.LENGTH_SHORT);
					toast.show();
				}
				return true;
			}


		case R.id.action_export:


			if (claimActivity.getClaimId() != null) {

				AlertDialog.Builder metadataDialog = new AlertDialog.
						Builder(rootView.getContext());

				metadataDialog.setTitle(R.string.password);

				final EditText input = new EditText(rootView.
						getContext());

				input.
				setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				input.
				setTransformationMethod(PasswordTransformationMethod.getInstance());
				metadataDialog.setView(input);

				metadataDialog.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						String password = input.getText().toString();
						dialog.dismiss();

						new ExporterTask(rootView.getContext()).execute(password,
								claimActivity.getClaimId());

						return;

					}
				});

				metadataDialog.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

				metadataDialog.show();



			}else {
				toast = Toast
						.makeText(rootView.getContext(),
								R.string.message_save_before_submit,
								Toast.LENGTH_SHORT);
				toast.show();
			}
			return true;


		case R.id.action_login:
			
			OpenTenureApplication.setActivity(getActivity());
			
        	Context context = getActivity().getApplicationContext();
        	Intent intent = new Intent( context, LoginActivity.class );            	            	 
        	startActivity(intent);
        	
        	OpenTenureApplication.setActivity(getActivity());
        	
        	return false;
        	
        	
		case R.id.action_logout:	
		
			
			
			try {
				
				LogoutTask logoutTask = new LogoutTask();
				
				logoutTask.execute(getActivity());	
								
			} catch (Exception e) {
				Log.d("Details", "An error ");
				
				e.printStackTrace();
			}
			
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}