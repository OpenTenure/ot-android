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
package org.fao.sola.clients.android.opentenure.form.ui;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintOption;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FieldTemplate;
import org.fao.sola.clients.android.opentenure.form.constraint.DateTimeFormatConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.OptionConstraint;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class FieldViewFactory {

	private static final long MIN_TIME_BETWEEN_TOAST = 500;

	public static View getSpinner(final Activity activity,
			List<FieldConstraintOption> options, final FieldTemplate field,
			final FieldPayload payload) {

		List<String> displayNames = new ArrayList<String>();
		final List<String> names = new ArrayList<String>();

		for (FieldConstraintOption option : options) {
			names.add(option.getName());
			displayNames.add(option.getDisplayName());
		}

		final Spinner spinner = new Spinner(activity);

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				activity, android.R.layout.simple_spinner_dropdown_item,
				displayNames);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				payload.setStringPayload(names.get(arg2));
				FieldConstraint constraint;
				if ((constraint = field.getFailedConstraint(payload)) != null) {
					((TextView) arg0.getChildAt(0)).setTextColor(Color.RED);
					Toast.makeText(activity.getBaseContext(),
							constraint.displayErrorMsg(), Toast.LENGTH_SHORT)
							.show();
				} else {
					((TextView) arg0.getChildAt(0)).setTextColor(Color.BLACK);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinner.setAdapter(spinnerArrayAdapter);

		return spinner;
	}

	public static View getViewForTextField(final Activity activity,
			final FieldTemplate field, final FieldPayload payload) {
		for (FieldConstraint constraint : field.getConstraints()) {
			if (constraint instanceof OptionConstraint) {
				return getSpinner(activity,
						((OptionConstraint) constraint).getOptions(), field,
						payload);
			}
		}
		final EditText text;
		text = new EditText(activity);
		text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		text.setInputType(InputType.TYPE_CLASS_TEXT);
		text.setHint(field.getHint());
		text.addTextChangedListener(new TextWatcher() {
			long lastTime = System.currentTimeMillis();

			@Override
			public void afterTextChanged(Editable s) {

				if ("".toString().equalsIgnoreCase(s.toString())) {
					payload.setStringPayload(null);
				} else {
					payload.setStringPayload(s.toString());
				}

				FieldConstraint constraint;
				if ((constraint = field.getFailedConstraint(payload)) != null) {
					text.setTextColor(Color.RED);
					if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
						Toast.makeText(activity.getBaseContext(),
								constraint.displayErrorMsg(),
								Toast.LENGTH_SHORT).show();
						lastTime = System.currentTimeMillis();
					}

				} else {
					text.setTextColor(Color.BLACK);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});
		return text;
	}

	public static View getViewForNumberField(final Activity activity,
			final FieldTemplate field, final FieldPayload payload) {
		final EditText number;
		number = new EditText(activity);
		number.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		number.setInputType(InputType.TYPE_CLASS_NUMBER);
		number.setHint(field.getHint());
		number.addTextChangedListener(new TextWatcher() {
			long lastTime = System.currentTimeMillis();

			@Override
			public void afterTextChanged(Editable s) {
				if ("".toString().equalsIgnoreCase(s.toString())) {
					payload.setBigDecimalPayload(null);
				} else {
					payload.setBigDecimalPayload(
							new BigDecimal(Double.parseDouble(s.toString())));
				}

				FieldConstraint constraint;
				if ((constraint = field.getFailedConstraint(payload)) != null) {
					number.setTextColor(Color.RED);
					if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
						Toast.makeText(activity.getBaseContext(),
								constraint.displayErrorMsg(),
								Toast.LENGTH_SHORT).show();
						lastTime = System.currentTimeMillis();
					}
				} else {
					number.setTextColor(Color.BLACK);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});
		return number;
	}

	public static View getViewForBooleanField(final Activity activity,
			final FieldTemplate field, final FieldPayload payload) {
		final CheckBox bool;
		bool = new CheckBox(activity);
		bool.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		bool.setHint(field.getHint());
		bool.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					payload.setBooleanPayload(Boolean.valueOf(true));
				}
			}

		});
		return bool;
	}

	public static View getViewForDateField(final Activity activity,
			final FieldTemplate field, final FieldPayload payload) {
		String tmpFormat = null;
		for (FieldConstraint constraint : field.getConstraints()) {
			if (constraint instanceof DateTimeFormatConstraint
					&& constraint.getFormat() != null) {
				tmpFormat = constraint.getFormat();
			}
		}
		final String format = tmpFormat;
		final EditText datetime;
		final Calendar localCalendar = Calendar.getInstance();
		datetime = new EditText(activity);
		datetime.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		datetime.setInputType(InputType.TYPE_CLASS_DATETIME);
		datetime.setHint(field.getHint());
		final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				localCalendar.set(Calendar.YEAR, year);
				localCalendar.set(Calendar.MONTH, monthOfYear);
				localCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				if (format != null) {
					SimpleDateFormat sdf = new SimpleDateFormat(format,
							Locale.US);
					sdf.format(localCalendar.getTime());
					datetime.setText(sdf.format(localCalendar.getTime()));
				}
			}

		};

		datetime.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				new DatePickerDialog(activity, date, localCalendar
						.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH),
						localCalendar.get(Calendar.DAY_OF_MONTH)).show();
				return true;
			}
		});
		datetime.addTextChangedListener(new TextWatcher() {
			long lastTime = System.currentTimeMillis();
			@Override
			public void afterTextChanged(Editable s) {

				if ("".toString().equalsIgnoreCase(s.toString())) {
					payload.setStringPayload(null);
				} else {
					payload.setStringPayload(s.toString());
				}

				FieldConstraint constraint;
				if ((constraint = field.getFailedConstraint(payload)) != null) {
					datetime.setTextColor(Color.RED);
					if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
					Toast.makeText(activity.getBaseContext(),
							constraint.displayErrorMsg(), Toast.LENGTH_SHORT)
							.show();
					lastTime = System.currentTimeMillis();
					}
				} else {
					datetime.setTextColor(Color.BLACK);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});
		return datetime;
	}

	public static View getViewForTimeField(final Activity activity,
			final FieldTemplate field, final FieldPayload payload) {
		String tmpFormat = null;
		for (FieldConstraint constraint : field.getConstraints()) {
			if (constraint instanceof DateTimeFormatConstraint
					&& constraint.getFormat() != null) {
				tmpFormat = constraint.getFormat();
			}
		}
		final String format = tmpFormat;
		final EditText datetime;
		final Calendar localCalendar = Calendar.getInstance();
		datetime = new EditText(activity);
		datetime.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		datetime.setInputType(InputType.TYPE_CLASS_DATETIME);
		datetime.setHint(field.getHint());
		final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hour, int min) {
				localCalendar.set(Calendar.HOUR_OF_DAY, hour);
				localCalendar.set(Calendar.MINUTE, min);
				if (format != null) {
					SimpleDateFormat sdf = new SimpleDateFormat(format,
							Locale.US);
					sdf.format(localCalendar.getTime());
					datetime.setText(sdf.format(localCalendar.getTime()));
				}
			}

		};

		datetime.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				new TimePickerDialog(activity, time, localCalendar
						.get(Calendar.HOUR_OF_DAY), localCalendar
						.get(Calendar.MINUTE), true).show();
				return true;
			}
		});
		datetime.addTextChangedListener(new TextWatcher() {
			long lastTime = System.currentTimeMillis();
			@Override
			public void afterTextChanged(Editable s) {

				if ("".toString().equalsIgnoreCase(s.toString())) {
					payload.setStringPayload(null);
				} else {
					payload.setStringPayload(s.toString());
				}

				FieldConstraint constraint;
				if ((constraint = field.getFailedConstraint(payload)) != null) {
					datetime.setTextColor(Color.RED);
					if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
					Toast.makeText(activity.getBaseContext(),
							constraint.displayErrorMsg(), Toast.LENGTH_SHORT)
							.show();
					lastTime = System.currentTimeMillis();
					}
				} else {
					datetime.setTextColor(Color.BLACK);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});
		return datetime;
	}
}
