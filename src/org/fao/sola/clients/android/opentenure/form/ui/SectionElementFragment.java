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

import org.fao.sola.clients.android.opentenure.ModeDispatcher.Mode;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FieldTemplate;
import org.fao.sola.clients.android.opentenure.form.FieldType;
import org.fao.sola.clients.android.opentenure.form.FieldValueType;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionTemplate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionElementFragment extends Fragment {

	private static final String ELEMENT_PAYLOAD_KEY = "elementPayload";
	private static final String ELEMENT_TEMPLATE_KEY = "elementTemplate";
	private View rootView;
	private SectionElementPayload elementPayload;
	private SectionTemplate elementTemplate;
	private Mode mode;

	public SectionElementFragment(SectionElementPayload payload, SectionTemplate template, Mode mode){
		this.elementTemplate = template;
		this.elementPayload = payload;
		this.mode = mode;
	}

	public SectionElementPayload getEditedElement() {
		return elementPayload;
	}

	public SectionElementFragment(){
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.field_group, menu);
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_field_group, container, false);
		setHasOptionsMenu(true);
		InputMethodManager imm = (InputMethodManager) rootView.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInputFromWindow(rootView.getWindowToken(), 0, InputMethodManager.HIDE_IMPLICIT_ONLY);
		
		if(savedInstanceState != null && savedInstanceState.getString(ELEMENT_PAYLOAD_KEY) != null){
			elementPayload = SectionElementPayload.fromJson(savedInstanceState.getString(ELEMENT_PAYLOAD_KEY));
		}
		if(savedInstanceState != null && savedInstanceState.getString(ELEMENT_TEMPLATE_KEY) != null){
			elementTemplate = SectionTemplate.fromJson(savedInstanceState.getString(ELEMENT_TEMPLATE_KEY));
		}
		if(savedInstanceState != null && savedInstanceState.getString(SectionElementActivity.MODE_KEY) != null){
			mode = Mode.valueOf(savedInstanceState.getString(SectionElementActivity.MODE_KEY));
		}
		update();
		return rootView;
	}
	
	private void update(){
		LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.fragment_field_group);
		int i = 0;
		int currentLanguageItemOrder = org.fao.sola.clients.android.opentenure.model.Language.getLanguage(OpenTenureApplication.getLocalization()).getItemOrder();
		int defaultLanguageItemOrder = org.fao.sola.clients.android.opentenure.model.Language.getDefaultLanguage().getItemOrder();
		for(final FieldTemplate field:elementTemplate.getFieldTemplateList()){
			FieldPayload fieldPayload = null;
			if(elementPayload.getFieldPayloadList().size() > i){
				fieldPayload = elementPayload.getFieldPayloadList().get(i);
			}else{
				// For some reason the payload of this form has fewer fields than the template
				// used to build it. This is probably due to editing the template without
				// changing its id, so we build a dummy text field on the fly not to break
				// the GUI
				fieldPayload = new FieldPayload();
				fieldPayload.setFieldType(FieldType.TEXT);
				fieldPayload.setFieldValueType(FieldValueType.TEXT);
			}
			// Add label
			TextView label = new TextView(getActivity());
			label.setTextSize(20);
			label.setPadding(0, 10, 0, 8);
			label.setTextAppearance(getActivity(), android.R.attr.textAppearanceMedium);
			label.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			label.setText(Html.fromHtml(field.getDisplayName()));
			ll.addView(label);
			// Add input field

			switch(field.getFieldType()){
			case DATE:
				ll.addView(FieldViewFactory.getViewForDateField(getActivity(), currentLanguageItemOrder, defaultLanguageItemOrder, field, fieldPayload, mode));
				break;
			case TIME:
				ll.addView(FieldViewFactory.getViewForTimeField(getActivity(), currentLanguageItemOrder, defaultLanguageItemOrder, field, fieldPayload, mode));
				break;
			case SNAPSHOT:
			case DOCUMENT:
			case GEOMETRY:
			case TEXT:
				ll.addView(FieldViewFactory.getViewForTextField(getActivity(), currentLanguageItemOrder, defaultLanguageItemOrder, field, fieldPayload, mode));
				break;
			case DECIMAL:
				ll.addView(FieldViewFactory.getViewForDecimalField(getActivity(), currentLanguageItemOrder, defaultLanguageItemOrder, field, fieldPayload, mode));
				break;
			case INTEGER:
				ll.addView(FieldViewFactory.getViewForNumberField(getActivity(), currentLanguageItemOrder, defaultLanguageItemOrder, field, fieldPayload, mode));
				break;
			case BOOL:
				ll.addView(FieldViewFactory.getViewForBooleanField(getActivity(), currentLanguageItemOrder, defaultLanguageItemOrder, field, fieldPayload, mode));
				break;
			default:
				break;
			}
			i++;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(ELEMENT_PAYLOAD_KEY, elementPayload.toJson());
		outState.putString(ELEMENT_TEMPLATE_KEY, elementTemplate.toJson());
		outState.putString(SectionElementActivity.MODE_KEY, mode.toString());
		super.onSaveInstanceState(outState);
	}
}