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
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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

public class PersonFragment extends Fragment {

	View rootView;
	private PersonDispatcher personActivity;
	final Calendar localCalendar = Calendar.getInstance();
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	File personPictureFile;
	String mode;
	Menu menu;
	ImageView claimantImageView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			personActivity = (PersonDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement PersonDispatcher");
		}
	}
	
	public void setMode(String mode){
		this.mode = mode;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.person, menu);

		if(mode.equalsIgnoreCase(PersonActivity.MODE_RO)){
			menu.removeItem(R.id.action_save);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_person, container, false);
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
				updateDoB();
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
				if (personPictureFile != null) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(personPictureFile));
					startActivityForResult(intent,
							CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				} else {
					Toast toast = Toast.makeText(rootView.getContext(),
							R.string.message_save_person_before_adding_content,
							Toast.LENGTH_SHORT);
					toast.show();
				}

			}
		});
		if (personActivity.getPersonId() != null) {
			load(personActivity.getPersonId());
		}

		return rootView;
	}

	private void load(String personId) {
		Person person = Person.getPerson(personId);
		((EditText) rootView.findViewById(R.id.first_name_input_field))
				.setText(person.getFirstName());
		((EditText) rootView.findViewById(R.id.last_name_input_field))
				.setText(person.getLastName());
		((EditText) rootView.findViewById(R.id.date_of_birth_input_field))
				.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US)
						.format(person.getDateOfBirth()));
		((EditText) rootView.findViewById(R.id.place_of_birth_input_field))
				.setText(person.getPlaceOfBirth());
		((EditText) rootView.findViewById(R.id.postal_address_input_field))
				.setText(person.getPostalAddress());
		((EditText) rootView.findViewById(R.id.email_address_input_field))
				.setText(person.getEmailAddress());
		((RadioButton) rootView.findViewById(R.id.gender_feminine_input_field))
				.setChecked((person.getGender().equals("F")));
		((RadioButton) rootView.findViewById(R.id.gender_masculine_input_field))
				.setChecked((person.getGender().equals("M")));
		((EditText) rootView.findViewById(R.id.mobile_phone_number_input_field))
				.setText(person.getMobilePhoneNumber());
		((EditText) rootView
				.findViewById(R.id.contact_phone_number_input_field))
				.setText(person.getContactPhoneNumber());
		personPictureFile = Person.getPersonPictureFile(person.getPersonId());
		try {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				claimantImageView.setImageBitmap(Person.getPersonPicture(
						rootView.getContext(), personPictureFile, 128));
			} else {
				claimantImageView.setImageBitmap(Person.getPersonPicture(
						rootView.getContext(), personPictureFile, 128));
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
							rootView.getContext(), personPictureFile, 128));
				} catch (Exception e) {
					claimantImageView.setImageDrawable(getResources()
							.getDrawable(R.drawable.ic_contact_picture));
				}
			}
		}
	}

	private void updateDoB() {

		EditText dateOfBirth = (EditText) getView().findViewById(
				R.id.date_of_birth_input_field);
		String myFormat = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		dateOfBirth.setText(sdf.format(localCalendar.getTime()));
	}

	public void savePerson() {
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
		if (((RadioButton) rootView
				.findViewById(R.id.gender_feminine_input_field)).isChecked())
			person.setGender("F");
		if (((RadioButton) rootView
				.findViewById(R.id.gender_masculine_input_field)).isChecked())
			person.setGender("M");

		if (person.create() == 1) {

			personActivity.setPersonId(person.getPersonId());
			personPictureFile = Person.getPersonPictureFile(person
					.getPersonId());
		}

	}

	public void updatePerson() {

		Person person = Person.getPerson(personActivity.getPersonId());
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
		if (((RadioButton) rootView
				.findViewById(R.id.gender_feminine_input_field)).isChecked())
			person.setGender("F");
		if (((RadioButton) rootView
				.findViewById(R.id.gender_masculine_input_field)).isChecked())
			person.setGender("M");
		person.update();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Toast toast;
		switch (item.getItemId()) {

		case R.id.action_save:

			if (personActivity.getPersonId() == null) {
				savePerson();
				if (personActivity.getPersonId() != null) {
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
				updatePerson();
				if (personActivity.getPersonId() != null) {
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}