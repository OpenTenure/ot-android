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
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.MD5;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.Projection;
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

	private Marker up;
	private Marker down;
	private Marker left;
	private Marker right;
	private Marker remove;
	private Marker moveTo;
	private Marker relativeEdit;
	private Marker cancel;
	private Marker target;
	private Marker add;
	private Marker selectedVertex;
	
	private static final float UP_INITIAL_ROTATION = 180.0f;
	private static final float DOWN_INITIAL_ROTATION = 0.0f;
	private static final float LEFT_INITIAL_ROTATION = 90.0f;
	private static final float RIGHT_INITIAL_ROTATION = 270.0f;
	private static final int PIXELS_PER_STEP = 5;

	private boolean handleMarkerEditClick(Marker mark, final List<BasePropertyBoundary> existingProperties){
		if(remove == null || relativeEdit == null || cancel == null){
			return false;
		}

		if (mark.getId().equalsIgnoreCase(remove.getId())) {
			removeVertex(selectedVertex);
			drawBoundary();
			updateVertices();
			resetAdjacency(existingProperties);
			hideMarkerEditControls();
			selectedVertex = null;
			return true;
		}
		if (mark.getId().equalsIgnoreCase(relativeEdit.getId())) {
			showRelativeMarkerEditControls();
			return true;
		}
		if (mark.getId().equalsIgnoreCase(cancel.getId())) {
			deselect();
			return true;
		}
		return false;
	}

	private boolean handleRelativeMarkerEditClick(Marker mark, final List<BasePropertyBoundary> existingProperties){
		if(up == null || down == null || left == null || right == null || add == null || moveTo == null || cancel == null || target == null){
			return false;
		}
		
		Projection projection = map.getProjection();
		Point screenLocation = projection.toScreenLocation(target.getPosition());
		
		if (mark.getId().equalsIgnoreCase(up.getId())) {
			Log.d(this.getClass().getName(),"up");
			screenLocation.y -= PIXELS_PER_STEP;
			target.setPosition(projection.fromScreenLocation(screenLocation));
			target.setTitle("Lat: " + target.getPosition().latitude + ", Lon: " + target.getPosition().longitude + ", Dist: " + getTargetDistance());
			target.showInfoWindow();
			return true;
		}else if (mark.getId().equalsIgnoreCase(down.getId())) {
			Log.d(this.getClass().getName(),"down");
			screenLocation.y += PIXELS_PER_STEP;
			target.setPosition(projection.fromScreenLocation(screenLocation));
			target.setTitle("Lat: " + target.getPosition().latitude + ", Lon: " + target.getPosition().longitude + ", Dist: " + getTargetDistance());
			target.showInfoWindow();
			return true;
		}else if (mark.getId().equalsIgnoreCase(left.getId())) {
			Log.d(this.getClass().getName(),"left");
			screenLocation.x -= PIXELS_PER_STEP;
			target.setPosition(projection.fromScreenLocation(screenLocation));
			target.setTitle("Lat: " + target.getPosition().latitude + ", Lon: " + target.getPosition().longitude + ", Dist: " + getTargetDistance());
			target.showInfoWindow();
			return true;
		}else if (mark.getId().equalsIgnoreCase(right.getId())) {
			Log.d(this.getClass().getName(),"right");
			screenLocation.x += PIXELS_PER_STEP;
			target.setPosition(projection.fromScreenLocation(screenLocation));
			target.setTitle("Lat: " + target.getPosition().latitude + ", Lon: " + target.getPosition().longitude + ", Dist: " + getTargetDistance());
			target.showInfoWindow();
			return true;
		}else if (mark.getId().equalsIgnoreCase(add.getId())) {
			Log.d(this.getClass().getName(),"add");
			insertVertex(target.getPosition());
			resetAdjacency(existingProperties);
			deselect();
			return true;
		}else if (mark.getId().equalsIgnoreCase(moveTo.getId())) {
			Log.d(this.getClass().getName(),"moveTo");
			// Click on 'Move to' marker: insert a marker at target position and remove the selected
			insertVertex(target.getPosition());
			removeVertex(selectedVertex);
			hideMarkerEditControls();
			selectedVertex = null;
			drawBoundary();
			updateVertices();
			resetAdjacency(existingProperties);
			return true;
		}else if (mark.getId().equalsIgnoreCase(cancel.getId())) {
			Log.d(this.getClass().getName(),"cancel");
			deselect();
			return true;
		}else if (mark.getId().equalsIgnoreCase(target.getId())) {
			Log.d(this.getClass().getName(),"target");
			return true;
		}else{
			return false;
		}
	}

	private void deselect(){
		hideMarkerEditControls();
		if(selectedVertex != null){
			selectedVertex.setIcon(BitmapDescriptorFactory
					.fromResource(R.drawable.ot_blue_marker));
		selectedVertex = null;
		}
	}
	
	private void hideMarkerEditControls(){
		if(up != null){
			up.remove();
			up = null;
		}
		if(down != null){
			down.remove();
			down = null;
		}
		if(left != null){
			left.remove();
			left = null;
		}
		if(right != null){
			right.remove();
			right = null;
		}
		if(target != null){
			target.remove();
			target = null;
		}
		if(relativeEdit != null){
			relativeEdit.remove();
			relativeEdit = null;
		}
		if(remove != null){
			remove.remove();
			remove = null;
		}
		if(add != null){
			add.remove();
			add = null;
		}
		if(moveTo != null){
			moveTo.remove();
			moveTo = null;
		}
		if(cancel != null){
			cancel.remove();
			cancel = null;
		}

	}

	private Point getControlUpPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y + 4*markerHeight);
	}

	private Point getControlDownPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y + 8*markerHeight);
	}

	private Point getControlLeftPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x - 2*markerWidth, markerScreenPosition.y + 6*markerHeight);
	}

	private Point getControlRightPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x + 2*markerWidth, markerScreenPosition.y + 6*markerHeight);
	}

	private Point getControlRelativeEditPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlRemovePosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x - 2*markerWidth, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlAddPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x - 2*markerWidth, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlMoveToPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlCancelPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x + 2*markerWidth, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlTargetPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y);
	}
	
	private float getTargetDistance() {
		if(selectedVertex == null || target == null){
			return 0.0f;
		}
		
		float[] results = new float[1];
		Location.distanceBetween(target.getPosition().latitude, target.getPosition().longitude,
				selectedVertex.getPosition().latitude, selectedVertex.getPosition().longitude, results);
		return results[0];
	}

	public void refreshMarkerEditControls(float bearing){
		
		if(selectedVertex == null){
			return;
		}

		// Reposition visible edit controls (excluding target)
		
		Projection projection = map.getProjection();
		Point screenPosition = projection.toScreenLocation(selectedVertex.getPosition());

		Bitmap bmp = BitmapFactory
				.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
		int iconHeight = bmp.getHeight();
		int iconWidth = bmp.getWidth();

		if(up != null){
			up.setRotation(UP_INITIAL_ROTATION);
			up.setPosition(projection.fromScreenLocation(getControlUpPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(down != null){
			down.setRotation(DOWN_INITIAL_ROTATION);
			down.setPosition(projection.fromScreenLocation(getControlDownPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(left != null){
			left.setRotation(LEFT_INITIAL_ROTATION);
			left.setPosition(projection.fromScreenLocation(getControlLeftPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(right != null){
			right.setRotation(RIGHT_INITIAL_ROTATION);
			right.setPosition(projection.fromScreenLocation(getControlRightPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(relativeEdit != null){
			relativeEdit.setPosition(projection.fromScreenLocation(getControlRelativeEditPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(remove != null){
			remove.setPosition(projection.fromScreenLocation(getControlRemovePosition(screenPosition, iconWidth, iconHeight)));
		}
		if(add != null){
			add.setPosition(projection.fromScreenLocation(getControlAddPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(moveTo != null){
			moveTo.setPosition(projection.fromScreenLocation(getControlMoveToPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(cancel != null){
			cancel.setPosition(projection.fromScreenLocation(getControlCancelPosition(screenPosition, iconWidth, iconHeight)));
		}

	}

	private void showMarkerEditControls() {
		
		hideMarkerEditControls();
		
		Projection projection = map.getProjection();
		Point markerScreenPosition = projection.toScreenLocation(selectedVertex.getPosition());

		Bitmap bmp = BitmapFactory
				.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
		int markerHeight = bmp.getHeight();
		int markerWidth = bmp.getWidth();

		remove = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlRemovePosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_close_clear_cancel)));
		relativeEdit = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlRelativeEditPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title("0.0 m")
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_action_move)));
		cancel = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_block)));
	}

	private void showRelativeMarkerEditControls() {
		
		Projection projection = map.getProjection();
		Point markerScreenPosition = projection.toScreenLocation(selectedVertex.getPosition());

		Bitmap bmp = BitmapFactory
				.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
		int markerHeight = bmp.getHeight();
		int markerWidth = bmp.getWidth();

		hideMarkerEditControls();

		up = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlUpPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title(context.getString(R.string.up))
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_find_next_holo_light)).rotation(UP_INITIAL_ROTATION));
		
		down = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlDownPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title(context.getString(R.string.down))
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_find_next_holo_light)).rotation(DOWN_INITIAL_ROTATION));
		left = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlLeftPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title(context.getString(R.string.left))
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_find_next_holo_light)).rotation(LEFT_INITIAL_ROTATION));
		right = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlRightPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title(context.getString(R.string.right))
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_find_next_holo_light)).rotation(RIGHT_INITIAL_ROTATION));
		add = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlAddPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_add)));
		moveTo = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlMoveToPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_goto)));
		cancel = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_block)));
		target = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlTargetPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title("0.0 m")
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_mylocation)));
	}

	public EditablePropertyBoundary(final Context context, final GoogleMap map, final Claim claim,
			final ClaimDispatcher claimActivity, boolean allowDragging) {
		super(context, map, claim);
		this.claimActivity = claimActivity;
		this.allowDragging = allowDragging;

		if (vertices != null && vertices.size() > 0) {
			int i = 0;
			for (Vertex vertex : vertices) {
				Marker mark = createMarker(i++, vertex.getMapPosition());
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

	private boolean handleVertexClick(final Marker mark){
		if (verticesMap.containsKey(mark.getId())) {
			selectedVertex = mark;
			selectedVertex.setIcon(BitmapDescriptorFactory.defaultMarker());
			selectedVertex.showInfoWindow();
			showMarkerEditControls();
			return true;
		}
		return false;
		
	}

	private boolean handleClick(){
		// Can only be a click on the property name, deselect and let the event flow
		deselect();
		return false;
	}

	public boolean handleMarkerClick(final Marker mark, final List<BasePropertyBoundary> existingProperties){
		if(handleMarkerEditClick(mark, existingProperties)){
			return true;
		}else if(handleRelativeMarkerEditClick(mark, existingProperties)){
			return true;
		}else if(handleVertexClick(mark)){
			return true;
		}else{
			return handleClick();
		}
	}

	public void removeVertex(Marker mark) {
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

		Marker mark = createMarker(vertices.size(), position);
		Vertex vert = new Vertex(position);
		vert.setClaimId(claimActivity.getClaimId());
		insertVertex(vert);
		verticesMap.put(mark.getId(), vert);
	}

	private void insertVertex(Vertex newVertex) {

		double minDistance = Double.MAX_VALUE;
		int insertIndex = 0;

		if (vertices.size() < 2) {

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

	private Marker createMarker(int index, LatLng position) {
		if(allowDragging){
			return map.addMarker(new MarkerOptions()
			.position(position)
			.title(index + ", Lat: " + position.latitude + ", Lon: " + position.longitude)
			.draggable(true)
			.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ot_blue_marker)));
		}else{
			return map.addMarker(new MarkerOptions()
			.position(position)
			.title(index + ", Lat: " + position.latitude + ", Lon: " + position.longitude)
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
