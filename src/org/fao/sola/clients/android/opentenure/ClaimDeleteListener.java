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

import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.AdjacenciesNotes;
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;
import org.fao.sola.clients.android.opentenure.model.PropertyLocation;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.WithdrawClaimTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimDeleteListener implements OnClickListener {

	String claimId;
	ViewHolder vh;

	public ClaimDeleteListener(String claimId, ViewHolder vh) {

		this.claimId = claimId;
		this.vh = vh;
	}

	@Override
	public void onClick(final View v) {
		// TODO Auto-generated method stub

		final Claim claim = Claim.getClaim(claimId);
		boolean onlyLocal = true;

		if (claim.getStatus().equals(ClaimStatus._UNMODERATED)
				|| claim.getStatus().equals(ClaimStatus._CHALLENGED)
				|| claim.getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE)
				|| claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)) {
			if (!OpenTenureApplication.isLoggedin()) {

				Toast toast = Toast.makeText(v.getContext(),
						R.string.message_login_before, Toast.LENGTH_SHORT);
				toast.show();
				return;

			} else if (OpenTenureApplication.getUsername().equals(
					claim.getRecorderName())) {
				onlyLocal = false;

			}
		}

		if (!onlyLocal) {
			if (claimId != null) {

				// Here the withdrawn case
				// custom dialog
				final Dialog dialog = new Dialog(v.getContext());
				dialog.setContentView(R.layout.custom_remove_claim);
				dialog.setTitle(R.string.withdraw_claim);

				// Confirm Dialog
				TextView message = (TextView) dialog
						.findViewById(R.id.remove_quest);
				message.setTextSize(30);

				// Confirm Button

				final Button confirmButton = (Button) dialog
						.findViewById(R.id.ClaimWithdrawnConfirm);
				confirmButton.setText(R.string.confirm);

				confirmButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						// Here the community server call

						WithdrawClaimTask wdc = new WithdrawClaimTask();
						wdc.execute(claimId);

						dialog.dismiss();

					}
				});

				// Delete locally Button

				final Button deleteLocally = (Button) dialog
						.findViewById(R.id.ClaimDeleteLocally);
				deleteLocally.setText(R.string.delete_locally);

				deleteLocally.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						Person claimant = claim.getPerson();

						List<ShareProperty> list = ShareProperty
								.getShares(claimId);
						for (Iterator iterator = list.iterator(); iterator
								.hasNext();) {
							ShareProperty share = (ShareProperty) iterator
									.next();

							List<Owner> owners = Owner.getOwners(share.getId());

							share.deleteShare();

							if (!claim.getStatus().equals(ClaimStatus._CREATED)
									&& !claim.getStatus().equals(
											ClaimStatus._UPLOAD_INCOMPLETE)
									&& !claim.getStatus().equals(
											ClaimStatus._UPLOAD_ERROR)) {

								for (Iterator iteratorO = owners.iterator(); iteratorO
										.hasNext();) {
									Owner owner = (Owner) iteratorO.next();

									Person person = Person.getPerson(owner
											.getPersonId());
									owner.delete();
									person.delete();

								}

							}

						}

						List<Vertex> vertexList = claim.getVertices();
						for (Iterator iterator = vertexList.iterator(); iterator
								.hasNext();) {
							Vertex vertex = (Vertex) iterator.next();
							vertex.delete();
						}

						List<Attachment> attachments = claim.getAttachments();

						for (Iterator iterator = attachments.iterator(); iterator
								.hasNext();) {
							Attachment attachment = (Attachment) iterator
									.next();

							attachment.delete();

						}

						List<PropertyLocation> locations = claim
								.getPropertyLocations();
						for (Iterator iterator = locations.iterator(); iterator
								.hasNext();) {
							PropertyLocation location = (PropertyLocation) iterator
									.next();
							location.delete();
						}

						List<Adjacency> adjacencies = Adjacency
								.getAdjacencies(claimId);
						for (Iterator iterator = adjacencies.iterator(); iterator
								.hasNext();) {
							Adjacency adjacency = (Adjacency) iterator.next();

							adjacency.delete();

						}

						AdjacenciesNotes adjacenciesNotes = AdjacenciesNotes
								.getAdjacenciesNotes(claimId);
						if (adjacenciesNotes != null)
							adjacenciesNotes.delete();

						if (claim.delete() != 0) {

							FileSystemUtilities.deleteClaim(claimId);

							dialog.dismiss();

							/*
							 * Finally delete the claimant if is the case
							 */

							if (!claim.getStatus().equals(ClaimStatus._CREATED)
									&& !claim.getStatus().equals(
											ClaimStatus._UPLOAD_INCOMPLETE)
									&& !claim.getStatus().equals(
											ClaimStatus._UPLOAD_ERROR))
								claimant.delete();
							
							OpenTenureApplication.getLocalClaimsFragment()
							.refresh();


							Toast toast = Toast.makeText(
									OpenTenureApplication.getContext(),
									OpenTenureApplication
											.getContext()
											.getResources()
											.getString(
													R.string.message_deleted_claim),
									Toast.LENGTH_LONG);
							toast.show();
							
							
						} else {

							Toast toast = Toast.makeText(
									OpenTenureApplication.getContext(),
									OpenTenureApplication
											.getContext()
											.getResources()
											.getString(
													R.string.message_error_deleting_claim),
									Toast.LENGTH_LONG);
							toast.show();

						}

					}
				});

				// Cancel Button

				final Button cancelButton = (Button) dialog
						.findViewById(R.id.ClaimWithdrawnDelete);
				cancelButton.setText(R.string.cancel);

				cancelButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						dialog.dismiss();

					}
				});

				dialog.show();
			} else {
				Toast toast = Toast.makeText(v.getContext(),
						R.string.message_save_claim_before_submit,
						Toast.LENGTH_SHORT);
				toast.show();
			}

		}

		else {

			AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());

			dialog.setTitle(R.string.action_remove_claim);
			dialog.setMessage(R.string.message_remove_claim);

			dialog.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

							List<ShareProperty> list = ShareProperty
									.getShares(claimId);
							
							for (Iterator iterator = list.iterator(); iterator
									.hasNext();) {
								ShareProperty share = (ShareProperty) iterator
										.next();

								List<Owner> owners = Owner.getOwners(share.getId());

								share.deleteShare();

								if (!claim.getStatus().equals(ClaimStatus._CREATED)
										&& !claim.getStatus().equals(
												ClaimStatus._UPLOAD_INCOMPLETE)
										&& !claim.getStatus().equals(
												ClaimStatus._UPLOAD_ERROR)) {

									for (Iterator iteratorO = owners.iterator(); iteratorO
											.hasNext();) {
										Owner owner = (Owner) iteratorO.next();

										Person person = Person.getPerson(owner
												.getPersonId());
										owner.delete();
										person.delete();

									}

								}

							}

							List<Vertex> vertexList = claim.getVertices();
							for (Iterator iterator = vertexList.iterator(); iterator
									.hasNext();) {
								Vertex vertex = (Vertex) iterator.next();
								vertex.delete();
							}

							List<PropertyLocation> locations = claim
									.getPropertyLocations();
							for (Iterator iterator = locations.iterator(); iterator
									.hasNext();) {
								PropertyLocation location = (PropertyLocation) iterator
										.next();
								location.delete();
							}

							List<Attachment> attachments = claim
									.getAttachments();

							for (Iterator iterator = attachments.iterator(); iterator
									.hasNext();) {
								Attachment attachment = (Attachment) iterator
										.next();

								attachment.delete();

							}

							List<Adjacency> adjacencies = Adjacency
									.getAdjacencies(claimId);
							for (Iterator iterator = adjacencies.iterator(); iterator
									.hasNext();) {
								Adjacency adjacency = (Adjacency) iterator
										.next();

								adjacency.delete();

							}

							AdjacenciesNotes adjacenciesNotes = AdjacenciesNotes
									.getAdjacenciesNotes(claimId);
							if (adjacenciesNotes != null)
								adjacenciesNotes.delete();

							Person claimant = claim.getPerson();

							if (claim.delete() != 0) {

								FileSystemUtilities.deleteClaim(claimId);

								dialog.dismiss();

								/* Finally delete claimant if is the case */
								if (!claim.getStatus().equals(ClaimStatus._CREATED)
										&& !claim.getStatus().equals(
												ClaimStatus._UPLOAD_INCOMPLETE)
										&& !claim.getStatus().equals(
												ClaimStatus._UPLOAD_ERROR))
									claimant.delete();
								
								OpenTenureApplication.getLocalClaimsFragment()
								.refresh();


								Toast toast = Toast.makeText(
										OpenTenureApplication.getContext(),
										OpenTenureApplication
												.getContext()
												.getResources()
												.getString(
														R.string.message_deleted_claim),
										Toast.LENGTH_LONG);
								toast.show();

							} else {

								Toast toast = Toast.makeText(
										OpenTenureApplication.getContext(),
										OpenTenureApplication
												.getContext()
												.getResources()
												.getString(
														R.string.message_error_deleting_claim),
										Toast.LENGTH_LONG);
								toast.show();

							}

						}
					});

			dialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});

			dialog.show();

		}

	}
}
