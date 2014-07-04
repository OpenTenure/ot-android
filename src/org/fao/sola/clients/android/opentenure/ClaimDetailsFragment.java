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
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.LandUse;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.print.PDFClaimExporter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
	private ClaimListener claimListener;
	private final Calendar localCalendar = Calendar.getInstance();

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

		inflater.inflate(R.menu.claim_details, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data != null) { // No selection has been done

			switch (requestCode) {
			case SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT:
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
							.equals(ClaimStatus._UPLOAD_INCOMPLETE)) {
				bar.setVisibility(View.GONE);
				status.setVisibility(View.GONE);

			} else {

				status = (TextView) rootView.findViewById(R.id.claim_status);

				int progress = FileSystemUtilities.getUploadProgress(claim);
				System.out.println("ClaimDetailsFragment Qui il progress e' : "
						+ progress);
				// Setting the update value in the progress bar
				bar.setVisibility(View.VISIBLE);
				bar.setProgress(progress);
				status.setVisibility(View.VISIBLE);
				status.setText(claim.getStatus() + " " + progress + " %");

			}
		}

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
			((View) rootView.findViewById(R.id.claimant))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(rootView.getContext(),
									SelectPersonActivity.class);

							// SOLA DB cannot store the same person twice

							ArrayList<String> idsWithClaims = Person
									.getIdsWithClaims();

							ArrayList<String> idsWithShares = Person
									.getIdsWithShares();

							ArrayList<String> excludeList = new ArrayList<String>();

							excludeList.addAll(idsWithClaims);
							excludeList.addAll(idsWithShares);

							intent.putStringArrayListExtra(
									SelectPersonActivity.EXCLUDE_PERSON_IDS_KEY,
									excludeList);

							startActivityForResult(
									intent,
									SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT);
						}
					});

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
			File challengedPersonPictureFile = Person
					.getPersonPictureFile(challengedPerson.getPersonId());
			challengedClaimantImageView.setImageBitmap(Person.getPersonPicture(
					rootView.getContext(), challengedPersonPictureFile, 128));
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
			File personPictureFile = Person.getPersonPictureFile(claimant
					.getPersonId());
			claimantImageView.setImageBitmap(Person.getPersonPicture(
					rootView.getContext(), personPictureFile, 128));
		}
	}

	private void load(Claim claim) {

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

				((EditText) rootView.findViewById(R.id.claim_notes_input_field))
						.setFocusable(false);

			}

			Person claimant = claim.getPerson();
			loadClaimant(claimant);
			loadChallengedClaim(claim.getChallengedClaim());

		}
	}

	public void saveClaim() {

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
			return;
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

		java.util.Date dob = null;
		try {
			dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
					.parse(((EditText) rootView
							.findViewById(R.id.date_of_start_input_field))
							.getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
			dob = null;
		}

		if (dob != null)
			claim.setDateOfStart(new Date(dob.getTime()));

		claim.setPerson(person);
		claim.setChallengedClaim(challengedClaim);
		if (claim.create() == 1) {

			FileSystemUtilities.createClaimFileSystem(claim.getClaimId());
			claimActivity.setClaimId(claim.getClaimId());

			createPersonAsOwner(person);

		}
		claimListener.onClaimSaved();

	}

	public int updateClaim() {

		Person person = Person.getPerson(((TextView) rootView
				.findViewById(R.id.claimant_id)).getText().toString());
		Claim challengedClaim = Claim
				.getClaim(((TextView) rootView
						.findViewById(R.id.challenge_to_claim_id)).getText()
						.toString());

		// Claim claim = Claim.getClaim(claimActivity.getClaimId());
		Claim claim = new Claim();
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

		java.util.Date dob = null;
		try {
			dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
					.parse(((EditText) rootView
							.findViewById(R.id.date_of_start_input_field))
							.getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
			dob = null;
		}

		if (dob != null)
			claim.setDateOfStart(new Date(dob.getTime()));

		claim.setPerson(person);
		claim.setChallengedClaim(challengedClaim);
		return claim.update();

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
				int updated = updateClaim();

				if (updated == 1) {
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

	public int createPersonAsOwner(Person claimant) {
		try {

			Owner owner = new Owner(true);
			owner.setClaimId(claimActivity.getClaimId());
			owner.setPersonId(claimant.getPersonId());
			owner.setShares(100);

			owner.create();

			return 1;

		} catch (Exception e) {
			// TODO: handle exception

			Log.d("Details", "An error " + e.getMessage());

			e.printStackTrace();

			return 0;
		}

	}

	private void updateDoB() {

		EditText dateOfBirth = (EditText) getView().findViewById(
				R.id.date_of_start_input_field);
		String myFormat = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		dateOfBirth.setText(sdf.format(localCalendar.getTime()));
	}

}