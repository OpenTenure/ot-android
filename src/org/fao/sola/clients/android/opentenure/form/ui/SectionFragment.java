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

import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.ModeDispatcher.Mode;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionPayload;
import org.fao.sola.clients.android.opentenure.form.SectionTemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class SectionFragment extends ListFragment {

	private View rootView;
	private SectionPayload editedSection;
	private SectionTemplate sectionTemplate;
	private Mode mode;

	public SectionFragment(SectionPayload section, SectionTemplate sectionTemplate, Mode mode){
		this.sectionTemplate = sectionTemplate;
		this.editedSection = section;
		this.mode = mode;
	}

	public SectionFragment(){
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.multiple_field_group, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			Intent intent = new Intent(rootView.getContext(),
					SectionElementActivity.class);
			intent.putExtra(SectionElementActivity.SECTION_ELEMENT_PAYLOAD_KEY,
					new SectionElementPayload(sectionTemplate).toJson());
			intent.putExtra(SectionElementActivity.SECTION_TEMPLATE_KEY,
					sectionTemplate.toJson());
			intent.putExtra(SectionElementActivity.MODE_KEY, mode
					.toString());
			startActivityForResult(intent, SectionElementActivity.SECTION_ELEMENT_ACTIVITY_RESULT);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data != null) { // No selection has been done

			switch (requestCode) {
			case SectionElementActivity.SECTION_ELEMENT_ACTIVITY_RESULT:
				String fieldGroup = data
				.getStringExtra(SectionElementActivity.SECTION_ELEMENT_PAYLOAD_KEY);
				SectionElementPayload newSectionElement = SectionElementPayload.fromJson(fieldGroup);
				editedSection.getElements().add(newSectionElement);
				update();
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.multiple_field_group_list, container,
				false);
		setHasOptionsMenu(true);
		update();
		return rootView;
	}
	
	protected void update() {

			List<SectionElementListTO> ownersListTOs = new ArrayList<SectionElementListTO>();

			for(SectionElementPayload sectionElement : editedSection.getElements()){
				
				SectionElementListTO fglto = new SectionElementListTO();
				fglto.setName(ownersListTOs.size() + "");
				StringBuffer sb = new StringBuffer();
				for(FieldPayload field:sectionElement.getFields()){
					if(sb.length() == 0){
						sb.append(",");
					}
					if(field.getStringPayload() != null)
						sb.append(field.getStringPayload());
					if(field.getBigDecimalPayload() != null)
						sb.append(field.getBigDecimalPayload());
					if(field.getBooleanPayload() != null)
						sb.append(field.getBooleanPayload());
				}
				fglto.setSlogan(sb.toString());
				fglto.setJson(sectionElement.toJson());
				ownersListTOs.add(fglto);
			}

			ArrayAdapter<SectionElementListTO> adapter = null;

			if(mode.compareTo(ModeDispatcher.Mode.MODE_RO) == 0){
				adapter = new SectionElementListAdapter(rootView.getContext(), ownersListTOs, editedSection, sectionTemplate, true);
			}else{
				adapter = new SectionElementListAdapter(rootView.getContext(), ownersListTOs, editedSection, sectionTemplate, false);
			}

			setListAdapter(adapter);
			adapter.notifyDataSetChanged();

	}
}
