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

import org.fao.sola.clients.android.opentenure.R.string;
import org.fao.sola.clients.android.opentenure.model.IdType;
import org.fao.sola.clients.android.opentenure.model.LandUse;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class PersonFragment extends Fragment {

	private View rootView;
	private PersonDispatcher personActivity;
	private ModeDispatcher mainActivity;
	private final Calendar localCalendar = Calendar.getInstance();
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private File personPictureFile;
	private ImageView claimantImageView;
	private boolean allowSave = true;

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
		try {
			mainActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ModeDispatcher");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.person, menu);

		if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0
				|| !allowSave) {
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

		preload();

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

		dateOfBirth.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				new DatePickerDialog(rootView.getContext(), date, localCalendar
						.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH),
						localCalendar.get(Calendar.DAY_OF_MONTH)).show();
				return true;
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

	private void preload() {
		// ID TYPE Spinner
		Spinner spinnerIT = (Spinner) rootView
				.findViewById(R.id.id_type_spinner);

		IdType it = new IdType();

		List<String> idTypelist = it.getDisplayValues();

		ArrayAdapter<String> dataAdapterIT = new ArrayAdapter<String>(
				OpenTenureApplication.getContext(),
				android.R.layout.simple_spinner_item, idTypelist) {
		};
		dataAdapterIT.setDropDownViewResource(R.layout.my_spinner);

		spinnerIT.setAdapter(dataAdapterIT);

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

		if (person.getPersonType() != null) {
			((RadioButton) rootView
					.findViewById(R.id.physical_person_input_field))
					.setChecked((person.getPersonType()
							.equals(Person._PHYSICAL)));
			((RadioButton) rootView.findViewById(R.id.legal_person_input_field))
					.setChecked((person.getPersonType().equals(Person._LEGAL)));
		}
		((RadioButton) rootView.findViewById(R.id.gender_masculine_input_field))
				.setChecked((person.getGender().equals("M")));
		((EditText) rootView.findViewById(R.id.mobile_phone_number_input_field))
				.setText(person.getMobilePhoneNumber());
		((EditText) rootView
				.findViewById(R.id.contact_phone_number_input_field))
				.setText(person.getContactPhoneNumber());
		((Spinner) rootView.findViewById(R.id.id_type_spinner))
				.setSelection(new IdType().getIndexByCodeType(person
						.getIdType()));

		((EditText) rootView.findViewById(R.id.id_number)).setText(person
				.getIdNumber());

		if (person.hasUploadedClaims()) {
			((EditText) rootView.findViewById(R.id.first_name_input_field))
					.setFocusable(false);
			((EditText) rootView.findViewById(R.id.last_name_input_field))
					.setFocusable(false);
			((EditText) rootView.findViewById(R.id.date_of_birth_input_field))
					.setFocusable(false);
			((EditText) rootView.findViewById(R.id.date_of_birth_input_field))
					.setOnLongClickListener(null);
			((EditText) rootView.findViewById(R.id.place_of_birth_input_field))
					.setFocusable(false);
			((EditText) rootView.findViewById(R.id.postal_address_input_field))
					.setFocusable(false);
			((EditText) rootView.findViewById(R.id.email_address_input_field))
					.setFocusable(false);
			((RadioButton) rootView
					.findViewById(R.id.gender_feminine_input_field))
					.setClickable(false);
			((RadioButton) rootView
					.findViewById(R.id.gender_masculine_input_field))
					.setClickable(false);
			((RadioButton) rootView
					.findViewById(R.id.physical_person_input_field))
					.setClickable(false);
			((RadioButton) rootView.findViewById(R.id.legal_person_input_field))
					.setClickable(false);
			((EditText) rootView
					.findViewById(R.id.mobile_phone_number_input_field))
					.setFocusable(false);
			((EditText) rootView
					.findViewById(R.id.contact_phone_number_input_field))
					.setFocusable(false);
			((Spinner) rootView.findViewById(R.id.id_type_spinner))
					.setFocusable(false);
			((Spinner) rootView.findViewById(R.id.id_type_spinner))
					.setClickable(false);
			((EditText) rootView.findViewById(R.id.id_number))
					.setFocusable(false);
			allowSave = false;
			getActivity().invalidateOptionsMenu();
		}

		personPictureFile = Person.getPersonPictureFile(person.getPersonId());
		claimantImageView.setImageBitmap(Person.getPersonPicture(
				rootView.getContext(), personPictureFile, 128));
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

	public int savePerson() {
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
			// dob = new java.util.Date();
			return 3;
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
		person.setContactPhoneNumber(((EditText) rootView
				.findViewById(R.id.contact_phone_number_input_field)).getText()
				.toString());

		String idTypeDispValue = (String) ((Spinner) rootView
				.findViewById(R.id.id_type_spinner)).getSelectedItem();
		person.setIdType(new IdType().getTypebyDisplayValue(idTypeDispValue));

		person.setIdNumber(((EditText) rootView.findViewById(R.id.id_number))
				.getText().toString());

		if (((RadioButton) rootView
				.findViewById(R.id.gender_feminine_input_field)).isChecked())
			person.setGender("F");
		if (((RadioButton) rootView
				.findViewById(R.id.gender_masculine_input_field)).isChecked())
			person.setGender("M");

		if (((RadioButton) rootView.findViewById(R.id.legal_person_input_field))
				.isChecked())
			person.setPersonType(Person._LEGAL);
		if (((RadioButton) rootView
				.findViewById(R.id.physical_person_input_field)).isChecked())
			person.setPersonType(Person._PHYSICAL);

		if (person.getDateOfBirth() == null || person.getFirstName() == null
				|| person.getLastName() == null || person.getGender() == null
				|| person.getPlaceOfBirth() == null || person.getPersonType() == null
						|| person.getPersonType().trim().equals(""))
			return 2;

		if (person.create() == 1) {

			personActivity.setPersonId(person.getPersonId());
			personPictureFile = Person.getPersonPictureFile(person
					.getPersonId());

			return 1;
		}
		return 0;

	}

	public int updatePerson() {

		// Person person = Person.getPerson(personActivity.getPersonId());
		Person person = new Person();
		person.setPersonId(personActivity.getPersonId());
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
			// dob = new java.util.Date();
			return 3;
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
		
		if (((RadioButton) rootView.findViewById(R.id.legal_person_input_field))
				.isChecked())
			person.setPersonType(Person._LEGAL);
		if (((RadioButton) rootView
				.findViewById(R.id.physical_person_input_field)).isChecked())
			person.setPersonType(Person._PHYSICAL);

		String idTypeDispValue = (String) ((Spinner) rootView
				.findViewById(R.id.id_type_spinner)).getSelectedItem();
		person.setIdType(new IdType().getTypebyDisplayValue(idTypeDispValue));

		person.setIdNumber(((EditText) rootView.findViewById(R.id.id_number))
				.getText().toString());

		if (person.getDateOfBirth() == null
				|| person.getDateOfBirth().equals("")
				|| person.getFirstName() == null
				|| person.getFirstName().trim().equals("")
				|| person.getLastName() == null
				|| person.getLastName().trim().equals("")
				|| person.getGender() == null
				|| person.getGender().trim().equals("")
				|| person.getPersonType() == null
				|| person.getPersonType().trim().equals("")
				|| person.getPlaceOfBirth() == null
				|| person.getPlaceOfBirth().trim().equals(""))
			return 2;

		return person.update();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Toast toast;
		switch (item.getItemId()) {

		case R.id.action_save:

			if (personActivity.getPersonId() == null) {

				int saved = savePerson();
				if (saved == 1) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();
				} else if (saved == 2) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_fields,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (saved == 3) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_birthdate,
							Toast.LENGTH_SHORT);
					toast.show();
				} else {
					toast = Toast
							.makeText(rootView.getContext(),
									R.string.message_unable_to_save,
									Toast.LENGTH_SHORT);
					toast.show();
				}
			} else {

				int updated = updatePerson();

				if (updated == 1) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();
				} else if (updated == 2) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_fields,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (updated == 3) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_birthdate,
							Toast.LENGTH_SHORT);
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