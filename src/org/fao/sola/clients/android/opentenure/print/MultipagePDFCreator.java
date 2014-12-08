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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.R;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.media.ExifInterface;
import android.util.Log;


public class MultipagePDFCreator {

	public static final int A4_PAGE_WIDTH = 595;
	public static final int A4_PAGE_HEIGHT = 842;
	public static final int LETTER_PAGE_WIDTH = 612;
	public static final int LETTER_PAGE_HEIGHT = 792;
	public static final int DEFAULT_HORIZONTAL_MARGIN = 40;
	public static final int DEFAULT_VERTICAL_MARGIN = 40;
	public static final int DEFAULT_VERTICAL_SPACE = 5;
	public static final int DEFAULT_HORIZONTAL_SPACE = 5;
	public static final String FONT_SANS_SERIF = "sans-serif";

	String claimId = null;
	static PdfDocument document;
	private File filePdf;
	private String fileName = "";
	private String type = "";
	private Context context;
	private Paint typeface = null;
	private int horizontalMargin = DEFAULT_HORIZONTAL_MARGIN;
	private int verticalMargin = DEFAULT_VERTICAL_MARGIN;
	private int horizontalSpace = DEFAULT_HORIZONTAL_SPACE;
	private int verticalSpace = DEFAULT_VERTICAL_SPACE;
	private int currentPageIndex = 1;
	private Page currentPage = null;
	private int currentX = 0;
	private int currentLineHeight = 0;
	private int currentY = 0;
	private int pageWidth = A4_PAGE_WIDTH;
	private int pageHeight = A4_PAGE_HEIGHT;

	public MultipagePDFCreator(Context context, String claimId, String fileName, String type) {
		this.claimId = claimId;
		this.context = context;
		this.filePdf = new File(
				FileSystemUtilities.getAttachmentFolder(claimId), fileName
						+ ".pdf");
		this.fileName = filePdf.getName();
		this.type = type;
	}

	public File create() {
		try {

			filePdf.createNewFile();

			List<String> tmpFileStorage = FileSystemUtilities
					.getListForMultipage(claimId);

			if (filePdf.exists()) {
				this.document = new PdfDocument();

				FileOutputStream fos = new FileOutputStream(filePdf);

				addPage(document, claimId);

				drawBitmap(bitmapFromResource(context, R.drawable.sola_logo,
						128, 110));

				newLine();
				newLine();
				writeBoldText("FILE NAME : " + fileName);
				newLine();
				writeBoldText("CLAIM ID : " + claimId);
				newLine();
				writeBoldText("FILE TYPE : " + type);
				newLine();
				newLine();

				for (Iterator iterator = tmpFileStorage.iterator(); iterator
						.hasNext();) {

					String name = (String) iterator.next();

					File jpgFile = new File(
							FileSystemUtilities.getMultipageFolder(claimId),
							name);

					if (jpgFile.exists()) {

						addPage(document, claimId);
						writeBoldText("FILE TYPE : " + type);
						newLine();
						newLine();
						drawBitmap(getMapPicture(jpgFile.getAbsolutePath()));
					}

				}

				if (currentPage != null) {
					document.finishPage(currentPage);
				}

				document.writeTo(fos);
				document.close();

			} else {
				System.out.println("Il file non esiste nun me chiedete WHY"
						+ filePdf.getName());

			}

		} catch (Throwable e) {
			Log.d(this.getClass().getName(), "Error during creation of pdf "
					+ e.getMessage());
			e.printStackTrace();
			
			return null;
		}
		return filePdf;

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

	private void writeBoldText(String text) {
		setFont(FONT_SANS_SERIF, Typeface.BOLD);
		writeText(text);
		setFont(FONT_SANS_SERIF, Typeface.NORMAL);
	}

	private void writeText(String text) {
		Rect bounds = new Rect();
		typeface.getTextBounds(text, 0, text.length(), bounds);
		currentPage.getCanvas().drawText(text, currentX,
				currentY + (bounds.height() - bounds.bottom), typeface);
		currentX += bounds.width() + horizontalSpace;
		currentLineHeight = Math.max(currentLineHeight, bounds.height());
		System.out.println("Write text X :" + currentX + "current Y "
				+ currentY + "currentLineHeight" + currentLineHeight);
	}

	private void newLine() {
		currentX = horizontalMargin;
		currentY += currentLineHeight + verticalSpace;
		currentLineHeight = 0;

		System.out.println("New Line X :" + currentX + "current Y " + currentY);
	}

	private void drawBitmap(Bitmap bmp) {

		System.out.println("X :" + currentX + "current Y " + currentY);
		Paint paint = new Paint();
		
		currentPage.getCanvas().drawBitmap(bmp, currentX, currentY, paint);
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

	private void addPage(PdfDocument document, String claimId) {

		if (currentPage != null) {
			document.finishPage(currentPage);
		}
		// crate a page description
		PageInfo pageInfo = new PageInfo.Builder(pageWidth, pageHeight,
				currentPageIndex++).create();

		// start a page
		currentLineHeight = 0;
		currentPage = document.startPage(pageInfo);
		currentX = horizontalMargin;
		currentY = verticalMargin;

		System.out.println("Add page " + "X :" + currentX + "current Y "
				+ currentY);

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
	

	public Bitmap getMapPicture(String fileName) {

		Bitmap decoded=null;
		String exifOrientation = null;
		Bitmap ultimateBitmap = null;
		Bitmap bitmap = null;
		
		try {
		

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ALPHA_8;
		options.inPreferQualityOverSpeed = true;

		bitmap = BitmapFactory.decodeFile(fileName, options);

		ExifInterface oldExif;
		
		
			oldExif = new ExifInterface(fileName);
			exifOrientation = oldExif
					.getAttribute(ExifInterface.TAG_ORIENTATION);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int orientation = 0;
		try {
			try {
				orientation = Integer.parseInt(exifOrientation);

			} catch (Exception e) {
				// TODO: handle exception
				System.out
						.println("Exception parsing position. orientation is "
								+ exifOrientation);
				orientation = 0;
			}

			final Matrix bitmapMatrix = new Matrix();
			switch (orientation) {
			case 1:
				break; // top left
			case 2:
				bitmapMatrix.postScale(-1, 1);
				break; // top right
			case 3:
				bitmapMatrix.postRotate(180);
				break; // bottom right
			case 4:
				bitmapMatrix.postRotate(180);
				bitmapMatrix.postScale(-1, 1);
				break; // bottom left
			case 5:
				bitmapMatrix.postRotate(90);
				bitmapMatrix.postScale(1, -1);
				break; // left top
			case 6:
				bitmapMatrix.postRotate(90);
				break; // right top
			case 7:
				bitmapMatrix.postRotate(270);
				bitmapMatrix.postScale(1, -1);
				break; // right bottom
			case 8:
				bitmapMatrix.postRotate(270);
				break; // left bottom
			default:
				break; // Unknown
			}

			int height = bitmap.getHeight();
			int width = bitmap.getWidth();

			bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
					bitmapMatrix, false);

			height = bitmap.getHeight();
			width = bitmap.getWidth();

			System.out.println("Height" + height + "width " + width);

			if (height > A4_PAGE_HEIGHT) {
				height = A4_PAGE_HEIGHT - 100;
				width = (height / 3) * 2;
			}

			if (width > A4_PAGE_WIDTH) {
				width = A4_PAGE_WIDTH - 100;
				height = (width * 2) / 3;
			}
			ultimateBitmap = Bitmap.createScaledBitmap(bitmap, width, height,
					true);			
		
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ultimateBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
			decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

			System.out.println("Original  dimensions "+ ultimateBitmap.getWidth()+" "+ultimateBitmap.getHeight()+ " the body count "+ultimateBitmap.getByteCount());
			System.out.println("Compressed dimensions " + decoded.getWidth()+" "+decoded.getHeight() + " the body count "+decoded.getByteCount() );
			

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return decoded;

	}
}
