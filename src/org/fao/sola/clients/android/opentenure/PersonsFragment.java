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

import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PersonsFragment extends ListFragment {

	private View rootView;
	private static final int PERSON_RESULT = 100;

	private ModeDispatcher mainActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mainActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ModeDispatcher");
		}
	}

	public PersonsFragment() {
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.persons, menu);

		if(mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0){
			menu.removeItem(R.id.action_new);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			Intent intent = new Intent(rootView.getContext(),
					PersonActivity.class);
			intent.putExtra(PersonActivity.PERSON_ID_KEY, PersonActivity.CREATE_PERSON_ID);
			intent.putExtra(PersonActivity.MODE_KEY, mainActivity.getMode().toString());
			startActivityForResult(intent, PERSON_RESULT);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		default:
			update();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.persons_list, container,
				false);
		setHasOptionsMenu(true);
	    EditText inputSearch = (EditText) rootView.findViewById(R.id.filter_input_field);
	    inputSearch.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	    inputSearch.addTextChangedListener(new TextWatcher() {

	        @Override
	        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
	        }

	        @Override
	        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

	        @Override
	        public void afterTextChanged(Editable arg0) {
	            ((PersonsListAdapter)getListAdapter()).getFilter().filter(arg0.toString());
	        }
	    });

		update();

		return rootView;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
			Intent intent = new Intent(rootView.getContext(),
					PersonActivity.class);
			intent.putExtra(PersonActivity.PERSON_ID_KEY, ((TextView)v.findViewById(R.id.person_id)).getText());
			intent.putExtra(PersonActivity.MODE_KEY, mainActivity.getMode().toString());
			startActivityForResult(intent, PERSON_RESULT);
		}else{
			Intent resultIntent = new Intent();
			resultIntent.putExtra(PersonActivity.PERSON_ID_KEY, ((TextView)v.findViewById(R.id.person_id)).getText());
			getActivity().setResult(SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT, resultIntent);
			getActivity().finish();
		}
	}

	protected void update() {
		List<Person> persons = Person.getAllPersons();
		List<PersonListTO> personListTOs = new ArrayList<PersonListTO>();

		for(Person person : persons){
			PersonListTO pto = new PersonListTO();
			pto.setId(person.getPersonId());
			pto.setSlogan(person.getFirstName()+ " " + person.getLastName());
			personListTOs.add(pto);
		}
		ArrayAdapter<PersonListTO> adapter = new PersonsListAdapter(rootView.getContext(), personListTOs);
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();

	}
}
