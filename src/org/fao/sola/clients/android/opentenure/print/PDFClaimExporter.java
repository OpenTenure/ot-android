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
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.maps.PropertyBoundary;
import org.fao.sola.clients.android.opentenure.model.Claim;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;

@SuppressLint("NewApi") // Suppressions needed to allow compiling for API level 17
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
		fileName = FileSystemUtilities.getOpentenureFolder() + File.separator
				+ "print_and_sign_this.pdf";
		mapFileName = FileSystemUtilities.getAttachmentFolder(claimId)
				+ File.separator + PropertyBoundary.DEFAULT_MAP_FILE_NAME;
		try {
			document = new PdfDocument();

			Claim claim = Claim.getClaim(claimId);

			addPage(document, context, claimId);

			setFont(FONT_SANS_SERIF, Typeface.NORMAL);
			writeText(context.getResources().getString(R.string.app_name) + " " + context.getResources().getString(R.string.claim) + ": " + claim.getName() + " (id: " + claimId + ")");
			newLine();
			drawBitmap(bitmapFromResource(context, R.drawable.sola_logo, 128,
					110));
			newLine();
			writeText(context.getResources().getString(R.string.first_name) + ": " + claim.getPerson().getFirstName());
			newLine();
			writeText(context.getResources().getString(R.string.last_name) + ": " + claim.getPerson().getLastName());
			newLine();
			writeText(context.getResources().getString(R.string.date_of_birth) + ": " + claim.getPerson().getDateOfBirth());
			newLine();
			writeText(context.getResources().getString(R.string.place_of_birth) + ": " + claim.getPerson().getPlaceOfBirth());
			newLine();
			writeText(context.getResources().getString(R.string.postal_address) + ": " + claim.getPerson().getPostalAddress());
			newLine();
			writeText(context.getResources().getString(R.string.contact_phone_number) + ": " + claim.getPerson().getContactPhoneNumber());
			newLine();
			drawHorizontalLine();
			newLine();
			drawBitmap(getMapPicture(mapFileName, 515));
			newLine();
			drawHorizontalLine();
			newLine();
			newLine();
			writeText(context.getResources().getString(R.string.date));
			drawHorizontalLine(pageWidth/2);
			writeText(context.getResources().getString(R.string.signature));
			drawHorizontalLine(pageWidth - horizontalMargin);
			newLine();

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

	private void setFont(String fontName, int style) {
		Typeface tf = Typeface.create(fontName, style);
		typeface = new Paint();
		typeface.setTypeface(tf);
		typeface.setAntiAlias(true);
	}

	private void writeText(String text) {
		Rect bounds = new Rect();
		typeface.getTextBounds(text, 0, text.length(), bounds);
		currentPage.getCanvas().drawText(text, currentX, currentY + bounds.height(),
				typeface);
		currentX += bounds.width() + horizontalSpace;
		currentLineHeight = Math.max(currentLineHeight, bounds.height());
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
		currentPage.getCanvas().drawLine(currentX, currentY + currentLineHeight,
				pageWidth - horizontalMargin, currentY + currentLineHeight, typeface);
		currentX = pageWidth - horizontalMargin;

	}

	private void drawHorizontalLine(int to) {
		currentPage.getCanvas().drawLine(currentX, currentY + currentLineHeight,
				to, currentY + currentLineHeight, typeface);
		currentX = to + horizontalSpace;

	}

	private void addPage(PdfDocument document, Context context,
			String claimId) {

		if (currentPage != null) {
			document.finishPage(currentPage);
		}
		// crate a page description
		PageInfo pageInfo = new PageInfo.Builder(pageWidth,
				pageHeight, currentPageIndex++).create();

		// start a page
		currentPage = document.startPage(pageInfo);
		currentX = horizontalMargin;
		currentY = verticalMargin;

	}

	private Bitmap bitmapFromResource(Context context, int resId,
			int width, int height) {
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
