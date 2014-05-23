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
import java.util.List;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.LoginActivity;
import org.fao.sola.clients.android.opentenure.network.LogoutTask;
import org.fao.sola.clients.android.opentenure.network.SaveClaimTask;
import org.fao.sola.clients.android.opentenure.print.PDFClaimExporter;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimDetailsFragment extends Fragment {

	View rootView;
	private ClaimDispatcher claimActivity;
	private ModeDispatcher modeActivity;

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
	}

	public ClaimDetailsFragment() {
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
//		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
			MenuItem itemIn;
			MenuItem itemOut;

			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Log.d(this.getClass().getName(), "Is the user logged in ? : "
					+ OpenTenureApplication.isLoggedin());

			if (OpenTenureApplication.isLoggedin()) {

				itemIn = menu.getItem(4);
				itemIn.setVisible(false);
				itemOut = menu.getItem(5);
				itemOut.setVisible(true);

			} else {

				itemIn = menu.getItem(4);
				itemIn.setVisible(true);
				itemOut = menu.getItem(5);
				itemOut.setVisible(false);
			}

//		}
		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.claim_details, menu);
//		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
//			menu.removeItem(R.id.action_save);
//			menu.removeItem(R.id.action_submit);
//			menu.removeItem(R.id.action_export);
//			menu.removeItem(R.id.action_login);
//			menu.removeItem(R.id.action_logout);
//			menu.removeItem(R.id.action_print);
//		}
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
		load(Claim.getClaim(claimActivity.getClaimId()));

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
			((View) rootView.findViewById(R.id.claimant))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(rootView.getContext(),
									SelectPersonActivity.class);
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
							startActivityForResult(
									intent,
									SelectClaimActivity.SELECT_CLAIM_ACTIVITY_RESULT);
						}
					});
		}

		return rootView;
	}

	private void preload() {
		// Claim name
		((EditText) rootView.findViewById(R.id.claim_name_input_field))
				.setText(getResources().getString(R.string.na));

		// Claimant
		((TextView) rootView.findViewById(R.id.claimant_id)).setTextSize(8);
		((TextView) rootView.findViewById(R.id.claimant_id))
				.setText(getResources().getString(R.string.na));
		((TextView) rootView.findViewById(R.id.claimant_slogan))
				.setText(getResources().getString(R.string.na));
		ImageView claimantImageView = (ImageView) rootView
				.findViewById(R.id.claimant_picture);
		claimantImageView.setImageDrawable(getResources().getDrawable(
				R.drawable.ic_contact_picture));

		// Challenged claim
		((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
				.setTextSize(8);
		((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
				.setText(getResources().getString(R.string.na));
		((TextView) rootView.findViewById(R.id.challenge_to_claim_slogan))
				.setText(getResources().getString(R.string.na));
		((TextView) rootView.findViewById(R.id.challenge_to_claim_status))
				.setText(getResources().getString(R.string.na));

		// Challenged claimant
		ImageView challengedClaimantImageView = (ImageView) rootView
				.findViewById(R.id.challenge_to_claimant_picture);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_contact_picture);

		challengedClaimantImageView.setImageBitmap(Bitmap.createScaledBitmap(
				bitmap, 128, 128, true));
	}

	private void loadChallengedClaim(Claim challengedClaim) {

		if (challengedClaim != null) {
			Person challengedPerson = challengedClaim.getPerson();
			((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
					.setTextSize(8);
			((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
					.setText(challengedClaim.getClaimId());
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
			if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
				((EditText) rootView.findViewById(R.id.claim_name_input_field))
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
		claim.setName(((EditText) rootView
				.findViewById(R.id.claim_name_input_field)).getText()
				.toString());
		claim.setPerson(person);
		claim.setChallengedClaim(challengedClaim);
		if (claim.create() == 1) {

			FileSystemUtilities.createClaimFileSystem(claim.getClaimId());
			claimActivity.setClaimId(claim.getClaimId());
		}

	}

	public void updateClaim() {

		Person person = Person.getPerson(((TextView) rootView
				.findViewById(R.id.claimant_id)).getText().toString());
		Claim challengedClaim = Claim
				.getClaim(((TextView) rootView
						.findViewById(R.id.challenge_to_claim_id)).getText()
						.toString());

		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		claim.setName(((EditText) rootView
				.findViewById(R.id.claim_name_input_field)).getText()
				.toString());
		claim.setPerson(person);
		claim.setChallengedClaim(challengedClaim);
		claim.update();
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

			if (!OpenTenureApplication.isLoggedin()) {
				toast = Toast.makeText(rootView.getContext(),
						R.string.message_login_before, Toast.LENGTH_SHORT);
				toast.show();
				return true;

			} else {

				if (claimActivity.getClaimId() != null) {

					JsonUtilities.createClaimJson(claimActivity.getClaimId());
					List<Vertex> vertices = Vertex.getVertices(claimActivity
							.getClaimId());
					Log.d(this.getClass().getName(),
							"mapGeometry: "
									+ Vertex.mapWKTFromVertices(vertices));
					Log.d(this.getClass().getName(),
							"gpsGeometry: "
									+ Vertex.gpsWKTFromVertices(vertices));

					SaveClaimTask saveClaimtask = new SaveClaimTask();
					saveClaimtask.execute(claimActivity.getClaimId());

				} else {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_save_claim_before_submit,
							Toast.LENGTH_SHORT);
					toast.show();
				}
				return true;
			}

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

		case R.id.action_login:

			OpenTenureApplication.setActivity(getActivity());

			Context context = getActivity().getApplicationContext();
			Intent intent = new Intent(context, LoginActivity.class);
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
		case R.id.action_print:
			try {
				PDFClaimExporter pdf = new PDFClaimExporter(
						rootView.getContext(), claimActivity.getClaimId());
				intent = new Intent(Intent.ACTION_VIEW);
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

}