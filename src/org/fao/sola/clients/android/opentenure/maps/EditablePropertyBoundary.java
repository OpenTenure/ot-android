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
package org.fao.sola.clients.android.opentenure.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.MD5;
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vividsolutions.jts.algorithm.distance.DistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class EditablePropertyBoundary extends BasePropertyBoundary {

	public static final String DEFAULT_MAP_FILE_NAME = "_map_.jpg";
	private Map<String, Vertex> verticesMap = new HashMap<String, Vertex>();
	private ClaimDispatcher claimActivity;
	private boolean allowDragging;

	public EditablePropertyBoundary(final Context context, final GoogleMap map, final Claim claim,
			final ClaimDispatcher claimActivity, boolean allowDragging) {
		super(context, map, claim);
		this.claimActivity = claimActivity;

		if (vertices != null && vertices.size() > 0) {
			for (Vertex vertex : vertices) {
				Marker mark = createMarker(vertex.getMapPosition());
				verticesMap.put(mark.getId(), vertex);
			}
		}
	}

	public void updateVertices() {

		Vertex.deleteVertices(claimActivity.getClaimId());

		for (int i = 0; i < vertices.size(); i++) {
			Vertex vertex = vertices.get(i);
			vertex.setSequenceNumber(i);
			Vertex.createVertex(vertex);
		}
		calculateGeometry();

	}

	public void resetAdjacency(List<BasePropertyBoundary> existingProperties){

		List<BasePropertyBoundary> adjacentProperties = findAdjacentProperties(existingProperties);
		Adjacency.deleteAdjacencies(claimActivity.getClaimId());

		if(adjacentProperties != null){

			for (BasePropertyBoundary adjacentProperty : adjacentProperties) {
				
				Adjacency adj = new Adjacency();
				adj.setSourceClaimId(claimActivity.getClaimId());
				adj.setDestClaimId(adjacentProperty.getClaimId());
				adj.setCardinalDirection(getCardinalDirection(adjacentProperty));
				adj.create();
			}
		}
	}

	public void moveMarker(Marker mark) {
		verticesMap.get(mark.getId()).setMapPosition(mark.getPosition());
	}

	public boolean handleMarkerClick(final Marker mark, final List<BasePropertyBoundary> existingProperties){
		if (verticesMap.get(mark.getId()) == null) {
			return false;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.message_remove_marker);
		dialog.setMessage("Marker " + mark.getTitle() + ", at lon: "
				+ mark.getPosition().longitude + ", lat: "
				+ mark.getPosition().latitude);
		dialog.setPositiveButton(R.string.confirm,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {

						removeMarker(mark);
						drawBoundary();
						updateVertices();
						resetAdjacency(existingProperties);
					}
				});
		dialog.setNegativeButton(R.string.cancel,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
					}
				});
		dialog.show();
		return true;
		
	}

	public void removeMarker(Marker mark) {
		vertices.remove(verticesMap.remove(mark.getId()));
		mark.remove();
	}

	public void insertVertex(LatLng position) {

		if (claimActivity.getClaimId() == null) {
			// Useless to add markers without a claim
			Toast toast = Toast.makeText(context,
					R.string.message_save_claim_before_adding_content,
					Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		Marker mark = createMarker(position);
		Vertex vert = new Vertex(position);
		vert.setClaimId(claimActivity.getClaimId());
		insertVertex(vert);
		verticesMap.put(mark.getId(), vert);
	}

	private void insertVertex(Vertex newVertex) {

		double minDistance = Double.MAX_VALUE;
		int insertIndex = 0;

		if (vertices.size() < 3) {

			vertices.add(newVertex);
			return;
		}

		for (int i = 0; i < vertices.size(); i++) {

			Vertex from = vertices.get(i);
			Vertex to = null;

			if (i == vertices.size() - 1) {
				to = vertices.get(0);
			} else {
				to = vertices.get(i + 1);
			}

			PointPairDistance ppd = new PointPairDistance();
			DistanceToPoint.computeDistance(
					new LineSegment(from.getMapPosition().longitude, from
							.getMapPosition().latitude,
							to.getMapPosition().longitude,
							to.getMapPosition().latitude),
					new Coordinate(newVertex.getMapPosition().longitude,
							newVertex.getMapPosition().latitude), ppd);

			double currDistance = ppd.getDistance();

			if (currDistance < minDistance) {
				minDistance = currDistance;
				insertIndex = i + 1;
			}

		}
		vertices.add(insertIndex, newVertex);
		updateVertices();
		drawBoundary();
	}

	private Marker createMarker(LatLng position) {
		if(allowDragging){
			return map.addMarker(new MarkerOptions()
			.position(position)
			.title(vertices.size() + "")
			.draggable(true)
			.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ot_blue_marker)));
		}else{
			return map.addMarker(new MarkerOptions()
			.position(position)
			.title(vertices.size() + "")
			.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ot_blue_marker)));
		}
	}

	public void saveSnapshot() {

		if (claimActivity.getClaimId() != null) {
			map.snapshot(new SnapshotReadyCallback() {

				@Override
				public void onSnapshotReady(Bitmap bmp) {
					FileOutputStream out = null;
					String claimId = claimActivity.getClaimId();
					String path = FileSystemUtilities
							.getAttachmentFolder(claimId)
							+ File.separator
							+ DEFAULT_MAP_FILE_NAME;
					try {
						out = new FileOutputStream(path);
						bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
						Claim claim = Claim.getClaim(claimId);
						for (Attachment att : claim.getAttachments()) {
							if (att.getFileName().equals(DEFAULT_MAP_FILE_NAME)) {
								att.delete();
							}
						}
						Attachment att = new Attachment();
						att.setClaimId(claimId);
						att.setDescription("Map");
						att.setFileName(DEFAULT_MAP_FILE_NAME);
						att.setFileType("image");
						att.setMimeType("image/jpeg");
						att.setMD5Sum(MD5.calculateMD5(new File(path)));
						att.setPath(path);
						att.setSize(new File(path).length());
						att.create();
						Toast toast = Toast.makeText(context,
								R.string.message_map_snapshot_saved,
								Toast.LENGTH_SHORT);
						toast.show();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (out != null) {
							try {
								out.close();
							} catch (Throwable ignore) {
							}
						}
					}
				}
			});
		}else{
			Toast toast = Toast.makeText(context,
					R.string.message_save_claim_before_adding_content,
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}
}
