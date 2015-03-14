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
package org.fao.sola.clients.android.opentenure.print;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.maps.EditablePropertyBoundary;
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.DocumentType;
import org.fao.sola.clients.android.opentenure.model.IdType;
import org.fao.sola.clients.android.opentenure.model.LandUse;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;

@SuppressLint("NewApi")
// Suppressions needed to allow compiling for API level 17
public class PDFClaimExporter {

	public static final int A4_PAGE_WIDTH = 595;
	public static final int A4_PAGE_HEIGHT = 842;
	public static final int LETTER_PAGE_WIDTH = 612;
	public static final int LETTER_PAGE_HEIGHT = 792;
	public static final int DEFAULT_HORIZONTAL_MARGIN = 40;
	public static final int DEFAULT_VERTICAL_MARGIN = 40;
	public static final int DEFAULT_VERTICAL_SPACE = 5;
	public static final int DEFAULT_HORIZONTAL_SPACE = 5;
	public static final String FONT_SANS_SERIF = "sans-serif";

	static PdfDocument document;
	private String fileName;
	private String mapFileName;
	private int horizontalMargin = DEFAULT_HORIZONTAL_MARGIN;
	private int verticalMargin = DEFAULT_VERTICAL_MARGIN;
	private int horizontalSpace = DEFAULT_HORIZONTAL_SPACE;
	private int verticalSpace = DEFAULT_VERTICAL_SPACE;
	private int currentPageIndex = 1;
	private Page currentPage = null;
	private Paint typeface = null;
	private int currentX = 0;
	private int currentLineHeight = 0;
	private int currentY = 0;
	private int pageWidth = A4_PAGE_WIDTH;
	private int pageHeight = A4_PAGE_HEIGHT;

	public PDFClaimExporter(Context context, String claimId) {

		try {
			Claim claim = Claim.getClaim(claimId);

			if (claim.getClaimNumber() != null)
				fileName = FileSystemUtilities.getCertificatesFolder()
						+ File.separator + claim.getClaimNumber() + ".pdf";
			else
				fileName = FileSystemUtilities.getCertificatesFolder()
						+ File.separator + claim.getName() + ".pdf";

			mapFileName = FileSystemUtilities.getAttachmentFolder(claimId)
					+ File.separator
					+ EditablePropertyBoundary.DEFAULT_MAP_FILE_NAME;
			document = new PdfDocument();

			addPage(document, context, claimId);

			drawBitmap(bitmapFromResource(context, R.drawable.open_tenure_logo,
					128, 85));

			moveX(15);
			moveY(15);

			if (claim.getClaimNumber() != null) {
				writeBoldText(context.getResources().getString(R.string.claim)
						+ " #" + claim.getClaimNumber(), 25);
			} else {
				writeBoldText(context.getResources().getString(R.string.claim)
						+ ": " + claim.getName(), 25);
			}

			moveX(45);
			moveY(30);

			Date date = new Date();

			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("dd/MM/yyyy HH:MM");
			String dataStr = sdf.format(new Date());

			int x = currentX;
			int y = currentY;

			moveX(10);
			writeText(context.getResources().getString(R.string.generated_on)
					+ " :");
			currentX = x;
			moveY(20);
			writeText(dataStr);
			moveY(-60);

			newLine();
			drawHorizontalLine();
			newLine();
			writeBoldText(OpenTenureApplication.getContext().getResources()
					.getString(R.string.claimant_no_star), 18);

			moveY(5);

			if (claim.getPerson().getPersonType().equals(Person._PHYSICAL)) {
				newLine();
				writeBoldText(
						context.getResources().getString(R.string.first_name)
								+ " :", 16);
				setX(200);
				writeBoldText(
						context.getResources().getString(R.string.last_name)
								+ " :", 16);
				setX(400);
				writeBoldText(
						context.getResources().getString(
								R.string.date_of_birth_simple)
								+ ": ", 16);
			} else {
				newLine();
				writeBoldText(
						context.getResources().getString(R.string.group_name)
								+ " :", 16);
				setX(300);
				writeBoldText(
						context.getResources().getString(
								R.string.date_of_establishment_label)
								+ " :", 16);
			}

			newLine();

			if (claim.getPerson().getPersonType().equals(Person._GROUP)) {
				writeText(claim.getPerson().getFirstName());
				setX(300);
				if (claim.getPerson().getDateOfBirth() != null)
					writeText(sdf.format(claim.getPerson().getDateOfBirth()));
			} else {
				writeText(claim.getPerson().getFirstName());
				setX(200);
				writeText(claim.getPerson().getLastName());
				setX(400);
				if (claim.getPerson().getDateOfBirth() != null)
					writeText(sdf.format(claim.getPerson().getDateOfBirth()));

			}
			newLine();
			newLine();
			writeBoldText(
					context.getResources().getString(R.string.postal_address)
							+ ": ", 16);
			setX(300);
			writeBoldText(
					context.getResources().getString(
							R.string.contact_phone_number)
							+ ": ", 16);
			newLine();
			if (claim.getPerson().getPostalAddress() != null)
				writeText(claim.getPerson().getPostalAddress());
			setX(300);
			if (claim.getPerson().getContactPhoneNumber() != null)
				writeText(" " + claim.getPerson().getContactPhoneNumber());
			newLine();
			newLine();

			writeBoldText(context.getResources().getString(R.string.id_type)
					+ ": ", 16);
			setX(300);

			writeBoldText(context.getResources().getString(R.string.id_number)
					+ ": ", 16);
			newLine();
			if (claim.getPerson().getIdType() != null)
				writeText(new IdType().getDisplayValueByType(claim.getPerson()
						.getIdType()));
			setX(300);
			if (claim.getPerson().getIdNumber() != null)
				writeText(claim.getPerson().getIdNumber());
			newLine();
			newLine();
			drawHorizontalLine();
			newLine();

			/*---------------------------------------------- OWNERS ------------------------------------------------------ */

			writeBoldText(context.getResources().getString(R.string.owners), 16);

			List<ShareProperty> shares = claim.getShares();
			int i = 0;
			for (Iterator iterator = shares.iterator(); iterator.hasNext();) {
				if (isPageEnding())
					addPage(document, context, claimId);

				++i;
				newLine();
				newLine();
				newLine();
				newLine();
				drawHorizontalLine();
				newLine();

				ShareProperty shareProperty = (ShareProperty) iterator.next();

				writeBoldText(
						context.getResources().getString(R.string.title_share)
								+ " " + i + " :" + shareProperty.getShares() + " %",
						16);
				newLine();
				List<Owner> owners = Owner.getOwners(shareProperty.getId());
				for (Iterator iterator2 = owners.iterator(); iterator2
						.hasNext();) {
					
					if (isPageEnding())
						addPage(document, context, claimId);
					
					Owner owner = (Owner) iterator2.next();
					Person person = Person.getPerson(owner.getPersonId());

					if (person.getPersonType().equals(Person._PHYSICAL)) {
						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.first_name)
										+ " :", 16);
						setX(200);
						writeBoldText(
								context.getResources().getString(
										R.string.last_name)
										+ " :", 16);
						setX(400);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_birth_simple)
										+ ": ", 16);
					} else {
						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.group_name)
										+ " :", 16);
						setX(300);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_establishment_label)
										+ " :", 16);
					}

					newLine();

					if (person.getPersonType().equals(Person._GROUP)) {

						writeText(person.getFirstName());
						setX(300);
						if (person.getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
					} else {
						writeText(person.getFirstName());
						setX(200);
						writeText(person.getLastName());
						setX(400);
						if (person.getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));

					}
					newLine();
					newLine();
					writeBoldText(
							context.getResources().getString(
									R.string.postal_address)
									+ ": ", 16);
					setX(300);
					writeBoldText(
							context.getResources().getString(
									R.string.contact_phone_number)
									+ ": ", 16);
					newLine();
					if (person.getPostalAddress() != null)
						writeText(claim.getPerson().getPostalAddress());
					setX(300);

					
					if (person.getContactPhoneNumber() != null)
						writeText(" "
								+ claim.getPerson().getContactPhoneNumber());
					newLine();
					newLine();

					writeBoldText(
							context.getResources().getString(R.string.id_type)
									+ ": ", 16);
					setX(300);

					writeBoldText(
							context.getResources()
									.getString(R.string.id_number) + ": ", 16);
					newLine();
					if (person.getIdType() != null)
						writeText(new IdType().getDisplayValueByType(claim
								.getPerson().getIdType()));
					setX(300);
					if (person.getIdNumber() != null)
						writeText(claim.getPerson().getIdNumber());
					newLine();
					newLine();

				}

			}

			/*------------------     DOCUMENTS -------------------------------------*/
			newLine();
			if (isPageEnding())
				addPage(document, context, claimId);
			drawHorizontalLine();
			newLine();
			newLine();
			writeBoldText(
					context.getResources().getString(
							R.string.title_claim_documents), 18);
			newLine();
			writeBoldText(context.getResources().getString(R.string.type), 16);
			setX(300);
			writeBoldText(context.getResources()
					.getString(R.string.description), 16);
			newLine();
			List<Attachment> attachments = claim.getAttachments();

			for (Iterator iterator = attachments.iterator(); iterator.hasNext();) {
				Attachment attachment = (Attachment) iterator.next();

				writeText((new DocumentType()).getDisplayVauebyType(attachment
						.getFileType()));

				setX(300);
				writeText(attachment.getDescription());
				newLine();
				newLine();
			}

			/*------------------ ADDITIONAL INFO -------------------------------------*/
			newLine();
			drawHorizontalLine();
			if (isPageEnding())
				addPage(document, context, claimId);
			newLine();
			newLine();
			writeBoldText(context.getResources()
					.getString(R.string.claim_notes), 18);
			newLine();
			newLine();
			newLine();
			writeText(claim.getNotes());
			if (isPageEnding())
				addPage(document, context, claimId);

			/*------------------ PARCEL -------------------------------------*/
			newLine();
			drawHorizontalLine();
			
			if (isPageEnding())
				addPage(document, context, claimId);
			
			newLine();
			newLine();
			newLine();
			writeBoldText(context.getResources().getString(R.string.parcel), 18);
			newLine();
			newLine();
			writeBoldText(
					context.getResources().getString(R.string.claim_area_label),
					16);
			setX(130);
			writeBoldText(
					context.getResources().getString(
							R.string.claim_type_no_star), 16);
			setX(260);
			writeBoldText(context.getResources().getString(R.string.land_use),
					16);
			setX(390);
			writeBoldText(
					context.getResources().getString(
							R.string.date_of_start_label_print), 16);
			newLine();
			writeText(claim.getClaimArea() + " "
					+ context.getResources().getString(R.string.square_meters));
			setX(130);
			writeText(new ClaimType().getDisplayValueByType(claim.getType()));
			setX(260);
			
			writeText(new LandUse().getDisplayValueByType(claim.getLandUse()));
			setX(390);
			sdf.applyPattern("dd/MM/yyyy");
			if (claim.getDateOfStart() != null)
				writeText(sdf.format(claim.getDateOfStart()));
			newLine();
			newLine();
			drawHorizontalLine();
			// ---------------------------------------------------- Adjacent
			// claims section -------------------------//

			newLine();
			newLine();
			newLine();
			
			if (isPageEnding())
				addPage(document, context, claimId);
			
			writeBoldText(
					context.getResources().getString(R.string.adjacent_claims),
					18);
			newLine();
			List<Adjacency> adjList = Adjacency.getAdjacencies(claimId);
			for (Adjacency adj : adjList) {
				Claim adjacentClaim;
				String direction;
				if (adj.getSourceClaimId().equalsIgnoreCase(claimId)) {
					adjacentClaim = Claim.getClaim(adj.getDestClaimId());
					direction = Adjacency.getCardinalDirection(context,
							adj.getCardinalDirection());
				} else {
					adjacentClaim = Claim.getClaim(adj.getSourceClaimId());
					direction = Adjacency.getCardinalDirection(context,
							Adjacency.getReverseCardinalDirection(adj
									.getCardinalDirection()));
				}
				newLine();
				newLine();
				writeText(context.getResources().getString(
						R.string.cardinal_direction)
						+ ": "
						+ direction
						+ ", "
						+ context.getResources().getString(R.string.property)
						+ ": "
						+ adjacentClaim.getName()
						+ ", "
						+ context.getResources().getString(R.string.by)
						+ ": "
						+ adjacentClaim.getPerson().getFirstName()
						+ " "
						+ adjacentClaim.getPerson().getLastName());
			}
			newLine();
			newLine();
			newLine();
			drawHorizontalLine();
			if (isPageEnding())
				addPage(document, context, claimId);
			newLine();
			newLine();
			newLine();
			newLine();
			writeBoldText(
					context.getResources().getString(
							R.string.adjacent_properties), 18);
			newLine();
			newLine();
			newLine();
			writeBoldText(context.getResources().getString(R.string.north), 16);
			setX(300);
			writeBoldText(context.getResources().getString(R.string.south), 16);
			newLine();
			newLine();
			newLine();
			if (claim.getAdjacenciesNotes() != null) {
				writeText(claim.getAdjacenciesNotes().getNorthAdjacency());
				setX(300);
				writeText(claim.getAdjacenciesNotes().getSouthAdjacency());
			}
			newLine();
			newLine();
			newLine();
			writeBoldText(context.getResources().getString(R.string.east), 16);
			setX(300);
			writeBoldText(context.getResources().getString(R.string.west), 16);
			newLine();
			if (claim.getAdjacenciesNotes() != null) {
				writeText(claim.getAdjacenciesNotes().getEastAdjacency());
				setX(300);
				writeText(claim.getAdjacenciesNotes().getWestAdjacency());
			}

			// ---------------------------------------------------------------------------------------------
			// MAP screenshot section
			addPage(document, context, claimId);
			newLine();
			drawBitmap(getMapPicture(mapFileName, 515));

			/*------------------ SIGNATURE -------------------------------------*/
			drawHorizontalLine(pageWidth / 2);
			newLine();
			moveY(80);
			writeBoldText(context.getResources().getString(R.string.date), 18);
			drawHorizontalLine(pageWidth / 2);
			writeBoldText(context.getResources().getString(R.string.signature),
					18);
			drawHorizontalLine(pageWidth - horizontalMargin);

			if (currentPage != null) {
				document.finishPage(currentPage);
			}
			document.writeTo(new FileOutputStream(fileName));
			document.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}

	private void setFont(String fontName, int style, int size) {
		Typeface tf = Typeface.create(fontName, style);
		typeface = new Paint();
		typeface.setTypeface(tf);
		typeface.setAntiAlias(true);
		typeface.setTextSize(size);

	}

	private void writeBoldText(String text, int size) {

		setFont(FONT_SANS_SERIF, Typeface.BOLD, size);
		writeText(text);
		setFont(FONT_SANS_SERIF, Typeface.NORMAL, 15);
	}

	private void writeText(String text) {
		if (text != null) {
			Rect bounds = new Rect();
			typeface.getTextBounds(text, 0, text.length(), bounds);
			currentPage.getCanvas().drawText(text, currentX,
					currentY + (bounds.height() - bounds.bottom), typeface);
			currentX += bounds.width() + horizontalSpace;
			currentLineHeight = Math.max(currentLineHeight, bounds.height());
		}
	}

	private void newLine() {
		currentX = horizontalMargin;
		currentY += currentLineHeight + verticalSpace;
		currentLineHeight = 0;
	}

	private void drawBitmap(Bitmap bmp) {
		currentPage.getCanvas().drawBitmap(bmp, currentX, currentY, null);
		currentX += bmp.getWidth() + horizontalSpace;
		currentLineHeight = Math.max(currentLineHeight, bmp.getHeight());
	}

	private void drawHorizontalLine() {
		currentPage.getCanvas().drawLine(currentX,
				currentY + currentLineHeight, pageWidth - horizontalMargin,
				currentY + currentLineHeight, typeface);
		currentX = pageWidth - horizontalMargin;

	}

	private void drawHorizontalLine(int to) {
		currentPage.getCanvas().drawLine(currentX,
				currentY + currentLineHeight, to, currentY + currentLineHeight,
				typeface);
		currentX = to + horizontalSpace;

	}

	private void moveX(int space) {

		currentX += space;

	}

	private void setX(int x) {

		currentX = x;

	}

	private void moveY(int space) {

		currentY += space;

	}

	private void addPage(PdfDocument document, Context context, String claimId) {

		if (currentPage != null) {
			document.finishPage(currentPage);
		}
		// crate a page description
		PageInfo pageInfo = new PageInfo.Builder(pageWidth, pageHeight,
				currentPageIndex++).create();

		// start a page
		currentPage = document.startPage(pageInfo);
		currentX = horizontalMargin;
		currentY = verticalMargin;

	}

	public boolean isPageEnding() {

		if ((pageHeight - currentY) < 150)
			return true;

		return false;
	}

	private Bitmap bitmapFromResource(Context context, int resId, int width,
			int height) {
		try {
			Bitmap resource = BitmapFactory.decodeResource(
					context.getResources(), resId);
			return Bitmap.createScaledBitmap(resource, width, height, false);
		} catch (Exception e) {
		}
		return null;
	}

	public Bitmap getMapPicture(String fileName, int size) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap bitmap = BitmapFactory.decodeFile(fileName);

		int height = bitmap.getHeight();
		int width = bitmap.getWidth();
		int startOffset = 0;

		Bitmap croppedBitmap = null;

		if (height > width) {
			// Portrait
			startOffset = (height - width) / 2;
			croppedBitmap = Bitmap.createBitmap(bitmap, 0, startOffset, width,
					width);
		} else {
			// Landscape
			startOffset = (width - height) / 2;
			croppedBitmap = Bitmap.createBitmap(bitmap, startOffset, 0, height,
					height);
		}
		return Bitmap.createScaledBitmap(croppedBitmap, size, size, true);
	}

}
