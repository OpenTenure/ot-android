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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.button.listener.SaveDetailsListener;
import org.fao.sola.clients.android.opentenure.button.listener.SaveDetailsNegativeListener;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.maps.EditablePropertyBoundary;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.LandUse;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.print.PDFClaimExporter;

import com.google.android.gms.maps.model.Marker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimDetailsFragment extends Fragment {

	View rootView;
	private ClaimDispatcher claimActivity;
	private ModeDispatcher modeActivity;
	private FormDispatcher formDispatcher;
	private ClaimListener claimListener;
	private final Calendar localCalendar = Calendar.getInstance();
	private static final int PERSON_RESULT = 100;

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
		try {
			modeActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ModeDispatcher");
		}
		try {
			claimListener = (ClaimListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ClaimListener");
		}
		try {
			formDispatcher = (FormDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement FormDispatcher");
		}
	}

	public ClaimDetailsFragment() {
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();

		inflater.inflate(R.menu.claim_details, menu);

		super.onCreateOptionsMenu(menu, inflater);

		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		if (claim != null && !claim.isModifiable()) {
			menu.removeItem(R.id.action_save);
		}

		setHasOptionsMenu(true);
		// setRetainInstance(true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data != null) { // No selection has been done

			switch (requestCode) {
			case PERSON_RESULT:
				String personId = data
						.getStringExtra(PersonActivity.PERSON_ID_KEY);

				Person claimant = Person.getPerson(personId);
				loadClaimant(claimant);
				break;
			case SelectClaimActivity.SELECT_CLAIM_ACTIVITY_RESULT:
				String claimId = data
						.getStringExtra(ClaimActivity.CLAIM_ID_KEY);
				Claim challengedClaim = Claim.getClaim(claimId);
				
				loadChallengedClaim(challengedClaim);

				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_claim_details, container,
				false);
		setHasOptionsMenu(true);

		// setRetainInstance(true);
		InputMethodManager imm = (InputMethodManager) rootView.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

		preload();

		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		load(claim);

		ProgressBar bar = (ProgressBar) rootView
				.findViewById(R.id.progress_bar);
		TextView status = (TextView) rootView.findViewById(R.id.claim_status);

		if (claim != null) {

			if (!claim.getStatus().equals(ClaimStatus._UPLOADING)
					&& !claim.getStatus()
							.equals(ClaimStatus._UPDATE_INCOMPLETE)
					&& !claim.getStatus()
							.equals(ClaimStatus._UPLOAD_INCOMPLETE)
					&& !claim.getStatus().equals(ClaimStatus._UPDATING)) {
				bar.setVisibility(View.GONE);
				status.setVisibility(View.GONE);

			} else {

				status = (TextView) rootView.findViewById(R.id.claim_status);

				int progress = FileSystemUtilities.getUploadProgress(
						claim.getClaimId(), claim.getStatus(),
						claim.getAttachments());

				// Setting the update value in the progress bar
				bar.setVisibility(View.VISIBLE);
				bar.setProgress(progress);
				status.setVisibility(View.VISIBLE);
				status.setText(claim.getStatus() + " " + progress + " %");

			}
		}

		((View) rootView.findViewById(R.id.claimant))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// ////////

						// Intent intent = new Intent(rootView.getContext(),
						// SelectPersonActivity.class);
						//
						// // SOLA DB cannot store the same person twice
						//
						// ArrayList<String> idsWithSharesOrClaims = Person
						// .getIdsWithSharesOrClaims();
						//
						// intent.putStringArrayListExtra(
						// SelectPersonActivity.EXCLUDE_PERSON_IDS_KEY,
						// idsWithSharesOrClaims);
						//
						// startActivityForResult(
						// intent,
						// SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT);

						String claimantId = ((TextView) rootView
								.findViewById(R.id.claimant_id)).getText()
								.toString();

						if (claimantId != null && !claimantId.trim().equals("")) {

							Intent intent = new Intent(rootView.getContext(),
									PersonActivity.class);
							intent.putExtra(PersonActivity.PERSON_ID_KEY,
									((TextView) rootView
											.findViewById(R.id.claimant_id))
											.getText());
							intent.putExtra(PersonActivity.MODE_KEY,
									modeActivity.getMode().toString());
							startActivityForResult(intent, PERSON_RESULT);

						} else {

							AlertDialog.Builder dialog = new AlertDialog.Builder(
									rootView.getContext());

							dialog.setTitle(R.string.new_entity);
							dialog.setMessage(R.string.message_entity_type);

							dialog.setPositiveButton(R.string.person,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											Intent intent = new Intent(rootView
													.getContext(),
													PersonActivity.class);
											intent.putExtra(
													PersonActivity.PERSON_ID_KEY,
													PersonActivity.CREATE_PERSON_ID);
											intent.putExtra(
													PersonActivity.ENTIY_TYPE,
													PersonActivity.TYPE_PERSON);
											intent.putExtra(
													PersonActivity.MODE_KEY,
													modeActivity.getMode()
															.toString());
											startActivityForResult(intent,
													PERSON_RESULT);
										}
									});

							dialog.setNegativeButton(R.string.group,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											Intent intent = new Intent(rootView
													.getContext(),
													PersonActivity.class);
											intent.putExtra(
													PersonActivity.PERSON_ID_KEY,
													PersonActivity.CREATE_PERSON_ID);
											intent.putExtra(
													PersonActivity.ENTIY_TYPE,
													PersonActivity.TYPE_GROUP);
											intent.putExtra(
													PersonActivity.MODE_KEY,
													modeActivity.getMode()
															.toString());
											startActivityForResult(intent,
													PERSON_RESULT);

										}
									});

							dialog.show();

						}

					}
				});

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
			((View) rootView.findViewById(R.id.challenge_to))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(rootView.getContext(),
									SelectClaimActivity.class);
							// Excluding from the list of claims that can be
							// challenged
							ArrayList<String> excludeList = new ArrayList<String>();
							List<Claim> claims = Claim.getAllClaims();
							for (Claim claim : claims) {
								// Challenges and local claims not yet
								// uploaded
								if (claim.getChallengedClaim() != null
										|| claim.getStatus()
												.equalsIgnoreCase(
														Claim.Status.created
																.toString())
										|| claim.getStatus().equalsIgnoreCase(
												Claim.Status.uploading
														.toString())) {
									excludeList.add(claim.getClaimId());
								}
							}
							intent.putStringArrayListExtra(
									SelectClaimActivity.EXCLUDE_CLAIM_IDS_KEY,
									excludeList);
							startActivityForResult(
									intent,
									SelectClaimActivity.SELECT_CLAIM_ACTIVITY_RESULT);
						}
					});
		}

		return rootView;
	}

	private void preload() {

		// Code Types Spinner
		Spinner spinner = (Spinner) rootView
				.findViewById(R.id.claimTypesSpinner);

		ClaimType ct = new ClaimType();

		List<String> list = ct.getClaimsTypesDispalyValues();

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
				OpenTenureApplication.getContext(),
				android.R.layout.simple_spinner_item, list) {
		};
		dataAdapter.setDropDownViewResource(R.layout.my_spinner);

		spinner.setAdapter(dataAdapter);

		// Land Uses Spinner
		Spinner spinnerLU = (Spinner) rootView
				.findViewById(R.id.landUseSpinner);

		LandUse lu = new LandUse();

		List<String> landUseslist = lu.getDisplayValues();

		ArrayAdapter<String> dataAdapterLU = new ArrayAdapter<String>(
				OpenTenureApplication.getContext(),
				android.R.layout.simple_spinner_item, landUseslist) {
		};
		dataAdapterLU.setDropDownViewResource(R.layout.my_spinner);

		spinnerLU.setAdapter(dataAdapterLU);

		// Claimant
		((TextView) rootView.findViewById(R.id.claimant_id)).setTextSize(8);
		((TextView) rootView.findViewById(R.id.claimant_id)).setText("");
		ImageView claimantImageView = (ImageView) rootView
				.findViewById(R.id.claimant_picture);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_contact_picture);

		claimantImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 128,
				128, true));

		// Challenged claim
		((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
				.setTextSize(8);
		((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
				.setText("");
		((TextView) rootView.findViewById(R.id.challenge_to_claim_status))
				.setText("");

		// Challenged claimant
		ImageView challengedClaimantImageView = (ImageView) rootView
				.findViewById(R.id.challenge_to_claimant_picture);

		challengedClaimantImageView.setImageBitmap(Bitmap.createScaledBitmap(
				bitmap, 128, 128, true));

		EditText dateOfStart = (EditText) rootView
				.findViewById(R.id.date_of_start_input_field);

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

		dateOfStart.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				new DatePickerDialog(rootView.getContext(), date, localCalendar
						.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH),
						localCalendar.get(Calendar.DAY_OF_MONTH)).show();
				return true;
			}
		});
	}

	private void loadChallengedClaim(Claim challengedClaim) {

		if (challengedClaim != null) {
			Person challengedPerson = challengedClaim.getPerson();
			((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
					.setTextSize(8);
			((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
					.setText(challengedClaim.getClaimId());
			((TextView) rootView.findViewById(R.id.challenge_to_claim_slogan))
					.setBackgroundColor(getResources().getColor(
							R.color.light_background_opentenure));
			((TextView) rootView.findViewById(R.id.challenge_to_claim_slogan))
					.setText(challengedClaim.getName() + ", "
							+ getResources().getString(R.string.by) + ": "
							+ challengedPerson.getFirstName() + " "
							+ challengedPerson.getLastName());
			((TextView) rootView.findViewById(R.id.challenge_to_claim_status))
					.setText(challengedClaim.getStatus());
			ImageView challengedClaimantImageView = (ImageView) rootView
					.findViewById(R.id.challenge_to_claimant_picture);
			

			// File challengedPersonPictureFile = Person
			// .getPersonPictureFile(challengedPerson.getPersonId());
			challengedClaimantImageView
					.setImageBitmap(Person.getPersonPicture(
							rootView.getContext(),
							challengedPerson.getPersonId(), 128));

			ImageView challengedClaimantRemoveButton = (ImageView) rootView
					.findViewById(R.id.action_remove_challenge);

			if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) != 0)
				challengedClaimantRemoveButton.setVisibility(View.VISIBLE);

			challengedClaimantRemoveButton
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {

							((TextView) rootView
									.findViewById(R.id.challenge_to_claim_id))
									.setText("");

							((TextView) rootView
									.findViewById(R.id.challenge_to_claim_slogan))
									.setBackgroundColor(getResources()
											.getColor(
													R.color.dark_background_opentenure));

							((TextView) rootView
									.findViewById(R.id.challenge_to_claim_slogan))
									.setText(getResources()
											.getString(
													R.string.message_touch_to_select_a_claim));

							((TextView) rootView
									.findViewById(R.id.challenge_to_claim_status))
									.setText("");

							((ImageView) rootView
									.findViewById(R.id.action_remove_challenge))
									.setVisibility(View.INVISIBLE);

						}
					});

		}
	}

	private void loadClaimant(Person claimant) {
		if (claimant != null) {

			((TextView) rootView.findViewById(R.id.claimant_id)).setTextSize(8);
			((TextView) rootView.findViewById(R.id.claimant_id))
					.setText(claimant.getPersonId());
			((TextView) rootView.findViewById(R.id.claimant_slogan))
					.setBackgroundColor(getResources().getColor(
							R.color.light_background_opentenure));
			((TextView) rootView.findViewById(R.id.claimant_slogan))
					.setText(claimant.getFirstName() + " "
							+ claimant.getLastName());
			ImageView claimantImageView = (ImageView) rootView
					.findViewById(R.id.claimant_picture);
			// File personPictureFile = Person.getPersonPictureFile(claimant
			// .getPersonId());
			claimantImageView.setImageBitmap(Person.getPersonPicture(
					rootView.getContext(), claimant.getPersonId(), 128));

			ImageView claimantRemove = (ImageView) rootView
					.findViewById(R.id.action_remove_person);
			claimantRemove.setVisibility(View.INVISIBLE);

		}
	}

	public void load(Claim claim) {

		if (claim != null) {

			((EditText) rootView.findViewById(R.id.claim_name_input_field))
					.setText(claim.getName());
			((Spinner) rootView.findViewById(R.id.claimTypesSpinner))
					.setSelection(new ClaimType().getIndexByCodeType(claim
							.getType()));

			((Spinner) rootView.findViewById(R.id.landUseSpinner))
					.setSelection(new LandUse().getIndexByCodeType(claim
							.getLandUse()));

			((EditText) rootView.findViewById(R.id.claim_notes_input_field))
					.setText(claim.getNotes());

			if (claim.getDateOfStart() != null) {

				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US)
								.format(claim.getDateOfStart()));
			} else {
				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setText("");
			}
			if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
				((EditText) rootView.findViewById(R.id.claim_name_input_field))
						.setFocusable(false);
				((Spinner) rootView.findViewById(R.id.claimTypesSpinner))
						.setFocusable(false);
				((Spinner) rootView.findViewById(R.id.claimTypesSpinner))
						.setClickable(false);

				((Spinner) rootView.findViewById(R.id.landUseSpinner))
						.setFocusable(false);
				((Spinner) rootView.findViewById(R.id.landUseSpinner))
						.setClickable(false);
				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setFocusable(false);
				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setLongClickable(false);

				((EditText) rootView.findViewById(R.id.claim_notes_input_field))
						.setFocusable(false);

			}
			Person claimant = null;
			String claimantId = ((TextView) rootView
					.findViewById(R.id.claimant_id)).getText().toString();

			if (claimantId == null || claimantId.trim().equals(""))
				claimant = claim.getPerson();
			else
				claimant = Person.getPerson(claimantId);
			loadClaimant(claimant);
			loadChallengedClaim(claim.getChallengedClaim());

		}
	}

	public int saveClaim() {

		Person person = Person.getPerson(((TextView) rootView
				.findViewById(R.id.claimant_id)).getText().toString());
		Claim challengedClaim = Claim
				.getClaim(((TextView) rootView
						.findViewById(R.id.challenge_to_claim_id)).getText()
						.toString());

		Claim claim = new Claim();
		String claimName = ((EditText) rootView
				.findViewById(R.id.claim_name_input_field)).getText()
				.toString();

		if (claimName == null || claimName.trim().equals(""))
			return 3;
		claim.setName(claimName);

		String displayValue = (String) ((Spinner) rootView
				.findViewById(R.id.claimTypesSpinner)).getSelectedItem();
		claim.setType(new ClaimType().getTypebyDisplayValue(displayValue));

		String landUseDispValue = (String) ((Spinner) rootView
				.findViewById(R.id.landUseSpinner)).getSelectedItem();
		claim.setLandUse(new LandUse().getTypebyDisplayValue(landUseDispValue));

		String notes = ((EditText) rootView
				.findViewById(R.id.claim_notes_input_field)).getText()
				.toString();

		claim.setNotes(notes);

		String startDate = ((EditText) rootView
				.findViewById(R.id.date_of_start_input_field)).getText()
				.toString();

		java.util.Date dob = null;

		if (startDate != null && !startDate.trim().equals("")) {
			try {

				dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
						.parse(startDate);

				if (dob != null)
					claim.setDateOfStart(new Date(dob.getTime()));

			} catch (ParseException e) {
				e.printStackTrace();
				dob = null;
				return 2;
			}

		}

		if (person == null)
			return 4;

		claim.setPerson(person);
		claim.setChallengedClaim(challengedClaim);
		// Still allow saving the claim if the dynamic part contains errors
		isFormValid();
		claim.setDynamicForm(formDispatcher.getEditedFormPayload());
		claim.setVersion("0");

		if (claim.create() == 1) {
			List<Vertex> vertices = Vertex.getVertices(claim.getClaimId());
			if(challengedClaim != null && (vertices == null || vertices.size() == 0)){
				copyVerticesFromChallengedClaim(challengedClaim.getClaimId(), claim.getClaimId());
			}

			FileSystemUtilities.createClaimFileSystem(claim.getClaimId());
			claimActivity.setClaimId(claim.getClaimId());

			if (createPersonAsOwner(person) == 0)
				return 0;

			claimListener.onClaimSaved();
			return 1;

		} else
			return 5;

	}
	
	private void copyVerticesFromChallengedClaim(String challengedClaimId, String challengingClaimId){
		Log.d(this.getClass().getName(), "copying vertices from " + challengedClaimId + " to " + challengingClaimId);
		// delete eventually existing vertices
		Vertex.deleteVertices(challengingClaimId);
		// get vertices from the challenged claim
		List<Vertex> vertices = Vertex.getVertices(challengedClaimId);
		List<Vertex> copiedVertices = new ArrayList<Vertex>();
		for(Vertex vertex:vertices){
			Vertex copiedVertex = new Vertex(vertex);
			// associate it to the challenging claim id
			copiedVertex.setClaimId(challengingClaimId);
			copiedVertices.add(copiedVertex);
		}
		// save them again
		Vertex.createVertices(copiedVertices);

	}

	public int updateClaim() {

		Person person = Person.getPerson(((TextView) rootView
				.findViewById(R.id.claimant_id)).getText().toString());
		Claim challengedClaim = Claim
				.getClaim(((TextView) rootView
						.findViewById(R.id.challenge_to_claim_id)).getText()
						.toString());

		// Claim claim = Claim.getClaim(claimActivity.getClaimId());
		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		claim.setClaimId(claimActivity.getClaimId());
		claim.setName(((EditText) rootView
				.findViewById(R.id.claim_name_input_field)).getText()
				.toString());

		if (claim.getName() == null || claim.getName().trim().equals(""))
			return 0;

		String displayValue = (String) ((Spinner) rootView
				.findViewById(R.id.claimTypesSpinner)).getSelectedItem();
		claim.setType(new ClaimType().getTypebyDisplayValue(displayValue));

		String landUseDispValue = (String) ((Spinner) rootView
				.findViewById(R.id.landUseSpinner)).getSelectedItem();
		claim.setLandUse(new LandUse().getTypebyDisplayValue(landUseDispValue));

		String notes = ((EditText) rootView
				.findViewById(R.id.claim_notes_input_field)).getText()
				.toString();

		claim.setNotes(notes);

		String startDate = ((EditText) rootView
				.findViewById(R.id.date_of_start_input_field)).getText()
				.toString();

		java.util.Date dob = null;

		if (startDate != null && !startDate.trim().equals("")) {
			try {

				dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
						.parse(startDate);

				if (dob != null)
					claim.setDateOfStart(new Date(dob.getTime()));

			} catch (ParseException e) {
				e.printStackTrace();
				dob = null;
				return 2;
			}

		}
		// Still allow saving the claim if the dynamic part contains errors
		isFormValid();

		if (createPersonAsOwner(person) == 0)
			return 0;

		claim.setPerson(person);
		claim.setChallengedClaim(challengedClaim);
		claim.setDynamicForm(formDispatcher.getEditedFormPayload());

		int result = claim.update();

		if (challengedClaim != null) {
			List<Vertex> vertices = Vertex.getVertices(claim.getClaimId());
			if (vertices == null || vertices.size() == 0) {
				copyVerticesFromChallengedClaim(challengedClaim.getClaimId(),
						claim.getClaimId());
			}
		}
		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
		switch (item.getItemId()) {

		case R.id.action_save:

			if (claimActivity.getClaimId() == null) {
				int resultSave = saveClaim();
				if (resultSave == 1) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();
				}
				if (resultSave == 2) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_startdate,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (resultSave == 3) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_unable_to_save_missing_claim_name,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (resultSave == 4) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_unable_to_save_missing_person,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (resultSave == 5) {
					toast = Toast
							.makeText(rootView.getContext(),
									R.string.message_unable_to_save,
									Toast.LENGTH_SHORT);
					toast.show();
				}
			} else {
				int updated = updateClaim();

				if (updated == 1) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();

				} else if (updated == 2) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_startdate,
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

		case R.id.action_export:

			if (claimActivity.getClaimId() != null) {

				AlertDialog.Builder metadataDialog = new AlertDialog.Builder(
						rootView.getContext());

				metadataDialog.setTitle(R.string.password);

				final EditText input = new EditText(rootView.getContext());

				input.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				input.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
				metadataDialog.setView(input);

				metadataDialog.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								String password = input.getText().toString();
								dialog.dismiss();

								new ExporterTask(rootView.getContext())
										.execute(password,
												claimActivity.getClaimId());

								return;

							}
						});

				metadataDialog.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								return;
							}
						});

				metadataDialog.show();

			} else {
				toast = Toast.makeText(rootView.getContext(),
						R.string.message_save_claim_before_submit,
						Toast.LENGTH_SHORT);
				toast.show();
			}
			return true;

		case R.id.action_print:
			Claim claim = Claim.getClaim(claimActivity.getClaimId());
			boolean mapPresent = false;

			for (Attachment attachment : claim.getAttachments()) {
				if (EditablePropertyBoundary.DEFAULT_MAP_FILE_NAME
						.equalsIgnoreCase(attachment.getFileName())
						&& EditablePropertyBoundary.DEFAULT_MAP_FILE_TYPE
								.equalsIgnoreCase(attachment.getFileType())
						&& EditablePropertyBoundary.DEFAULT_MAP_MIME_TYPE
								.equalsIgnoreCase(attachment.getMimeType())) {
					mapPresent = true;
				}
			}
			if (!mapPresent) {
				toast = Toast.makeText(rootView.getContext(),
						R.string.message_save_snapshot_before_printing,
						Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}
			try {
				PDFClaimExporter pdf = new PDFClaimExporter(
						rootView.getContext(), claimActivity.getClaimId());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://" + pdf.getFileName()),
						"application/pdf");
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(intent);

			} catch (Error e) {
				toast = Toast.makeText(rootView.getContext(),
						R.string.message_not_supported_on_this_device,
						Toast.LENGTH_SHORT);
				toast.show();
			}

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// public int createPersonAsOwner(Person claimant, String oldClaimantId) {
	// try {
	// int share = 0;
	//
	// ShareProperty toDelete =
	// ShareProperty.getOwner(claimActivity.getClaimId(),
	// oldClaimantId);
	//
	// if (toDelete != null) {
	// share = toDelete.getShares();
	//
	// toDelete.delete();
	// } else {
	//
	// List<ShareProperty> owners = Claim.getClaim(claimActivity.getClaimId())
	// .getOwners();
	// int sum = 0;
	//
	// for (Iterator iterator = owners.iterator(); iterator.hasNext();) {
	//
	// ShareProperty owner = (ShareProperty) iterator.next();
	// sum = sum + owner.getShares();
	// }
	//
	// share = 100 - sum;
	// }
	//
	// ShareProperty owner = new ShareProperty(true);
	//
	// owner.setClaimId(claimActivity.getClaimId());
	// owner.setPersonId(claimant.getPersonId());
	// owner.setShares(share);
	//
	// owner.create();
	//
	// return 1;
	//
	// } catch (Exception e) {
	// Log.d("Details", "An error " + e.getMessage());
	//
	// e.printStackTrace();
	//
	// return 0;
	// }
	//
	// }

	public boolean checkChanges() {

		boolean changed = false;

		Claim claim = Claim.getClaim(claimActivity.getClaimId());

		if (claim != null) {

			if (!claim.getName().equals(
					((EditText) rootView
							.findViewById(R.id.claim_name_input_field))
							.getText().toString()))
				changed = true;
			else {
				Person person = Person.getPerson(((TextView) rootView
						.findViewById(R.id.claimant_id)).getText().toString());
				if (!claim.getPerson().getPersonId()
						.equals(person.getPersonId()))
					changed = true;
				else {
					Claim challengedClaim = Claim.getClaim(((TextView) rootView
							.findViewById(R.id.challenge_to_claim_id))
							.getText().toString());
					if (challengedClaim == null
							&& claim.getChallengedClaim() != null)
						changed = true;
					else if (challengedClaim != null
							&& claim.getChallengedClaim() == null)
						changed = true;
					else if (challengedClaim != null
							&& claim.getChallengedClaim() != null
							&& !claim.getChallengedClaim().getClaimId()
									.equals(challengedClaim.getClaimId()))
						changed = true;
					else {
						String claimType = (String) ((Spinner) rootView
								.findViewById(R.id.claimTypesSpinner))
								.getSelectedItem();

						if (!claim.getType().equals(
								new ClaimType()
										.getTypebyDisplayValue(claimType)))
							changed = true;
						else {
							String landUseDispValue = (String) ((Spinner) rootView
									.findViewById(R.id.landUseSpinner))
									.getSelectedItem();
							if (claim.getLandUse() == null
									&& new LandUse()
											.getTypebyDisplayValue(landUseDispValue) != null)
								changed = true;
							else if (!claim
									.getLandUse()
									.equals(new LandUse()
											.getTypebyDisplayValue(landUseDispValue)))
								changed = true;
							else {

								String notes = ((EditText) rootView
										.findViewById(R.id.claim_notes_input_field))
										.getText().toString();

								if (claim.getNotes() != null
										&& !claim.getNotes().equals(notes))
									changed = true;
								else {
									String startDate = ((EditText) rootView
											.findViewById(R.id.date_of_start_input_field))
											.getText().toString();

									if (claim.getDateOfStart() == null
											|| claim.getDateOfStart()
													.equals("")) {

										if (startDate != null
												&& !startDate.equals(""))
											changed = true;
									} else {
										java.util.Date dob = null;

										if (startDate != null
												&& !startDate.trim().equals("")) {

											try {
												dob = new SimpleDateFormat(
														"yyyy-MM-dd", Locale.US)
														.parse(startDate);

												Date date = new Date(
														dob.getTime());

												if (claim.getDateOfStart()
														.compareTo(date) != 0)
													changed = true;

											} catch (ParseException e) {
												e.printStackTrace();
												dob = null;

											}

										} else {
											changed = isFormChanged();
										}

									}

								}

							}

						}

					}

				}
			}

			if (changed) {

				AlertDialog.Builder saveChangesDialog = new AlertDialog.Builder(
						this.getActivity());
				saveChangesDialog.setTitle(R.string.title_save_claim_dialog);
				String dialogMessage = OpenTenureApplication.getContext()
						.getString(R.string.message_save_changes);

				saveChangesDialog.setMessage(dialogMessage);

				saveChangesDialog.setPositiveButton(R.string.confirm,
						new SaveDetailsListener(this));

				saveChangesDialog.setNegativeButton(R.string.cancel,
						new SaveDetailsNegativeListener(this));
				saveChangesDialog.show();

			}
		} else {

			String claimName = ((EditText) rootView
					.findViewById(R.id.claim_name_input_field)).getText()
					.toString();
			if (claimName != null && !claimName.trim().equals(""))
				changed = true;
			else {

				String person = ((TextView) rootView
						.findViewById(R.id.claimant_id)).getText().toString();
				if (person != null && !person.trim().equals(""))
					changed = true;
				else {

					String challengedClaim = ((TextView) rootView
							.findViewById(R.id.challenge_to_claim_id))
							.getText().toString();
					if (challengedClaim != null
							&& !challengedClaim.trim().equals(""))
						changed = true;

					else {

						String notes = ((EditText) rootView
								.findViewById(R.id.claim_notes_input_field))
								.getText().toString();

						if (notes != null && !notes.trim().equals(""))
							changed = true;
						else {

							String startDate = ((EditText) rootView
									.findViewById(R.id.date_of_start_input_field))
									.getText().toString();

							if (startDate != null
									&& !startDate.trim().equals(""))
								changed = true;
							else {
								changed = isFormChanged();
							}

						}

					}

				}

			}
			if (changed) {
				AlertDialog.Builder saveChangesDialog = new AlertDialog.Builder(
						this.getActivity());
				saveChangesDialog.setTitle(R.string.title_save_claim_dialog);
				String dialogMessage = OpenTenureApplication.getContext()
						.getString(R.string.message_discard_changes);

				saveChangesDialog.setMessage(dialogMessage);

				saveChangesDialog.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								return;
							}
						});

				saveChangesDialog.setPositiveButton(R.string.confirm,
						new SaveDetailsNegativeListener(this));
				saveChangesDialog.show();
			}
		}
		return changed;

	}

	@Override
	public void onResume() {
		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		load(claim);

		super.onResume();

	};

	public int createPersonAsOwner(Person claimant) {
		try {
			List<ShareProperty> shares = ShareProperty.getShares(claimActivity
					.getClaimId());

			int value = 0;

			for (Iterator<ShareProperty> iterator = shares.iterator(); iterator
					.hasNext();) {
				ShareProperty shareProperty = (ShareProperty) iterator.next();
				value = value + shareProperty.getShares();
			}

			int shareValue = 100 - value;

			if (shareValue > 0) {
				ShareProperty share = new ShareProperty();

				share.setClaimId(claimActivity.getClaimId());
				share.setShares(shareValue);

				share.create();

				Person claimantCopy = claimant.copy();
				claimantCopy.create();

				File personImg = new File(
						FileSystemUtilities.getClaimantFolder(claimant
								.getPersonId())
								+ File.separator
								+ claimant.getPersonId() + ".jpg");

				if (personImg != null)
					FileSystemUtilities.copyFileInClaimantFolder(
							claimantCopy.getPersonId(), personImg);

				Owner owner = new Owner();
				owner.setPersonId(claimantCopy.getPersonId());
				owner.setShareId(share.getId());

				owner.create();
			}
			return 1;

		} catch (Exception e) {
			Log.d("Details", "An error " + e.getMessage());

			e.printStackTrace();

			return 0;
		}

	}

	private boolean isFormValid() {
		FormPayload formPayload = formDispatcher.getEditedFormPayload();
		FormTemplate formTemplate = formDispatcher.getFormTemplate();
		FieldConstraint constraint = null;
		if ((constraint = formTemplate.getFailedConstraint(formPayload)) != null) {
			Toast.makeText(rootView.getContext(), constraint.displayErrorMsg(),
					Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}

	private boolean isFormChanged() {
		FormPayload editedFormPayload = formDispatcher.getEditedFormPayload();
		FormPayload originalFormPayload = formDispatcher
				.getOriginalFormPayload();

		if (((editedFormPayload != null) && (originalFormPayload == null))
				|| ((editedFormPayload == null) && (originalFormPayload != null))
				|| !editedFormPayload.toJson().equalsIgnoreCase(
						originalFormPayload.toJson())) {
			return true;
		} else {
			return false;
		}
	}

	private void updateDoB() {

		EditText dateOfBirth = (EditText) getView().findViewById(
				R.id.date_of_start_input_field);
		String myFormat = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		dateOfBirth.setText(sdf.format(localCalendar.getTime()));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}