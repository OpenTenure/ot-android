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

import org.fao.sola.clients.android.opentenure.model.Person;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class DeletePersonListener implements OnClickListener {

	String personId;
	
	public DeletePersonListener(String personId) {

		this.personId = personId;
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		Person person = Person.getPerson(personId);
		

		Toast toast;
	
		System.out.println("Sto cercando di cancellare " + person.getLastName() + " "  + person.getFirstName());

		int result = Person.deletePerson(person);

		if (result > 0) {

			String message = String.format(OpenTenureApplication
					.getContext().getString(
							R.string.message_remove_person,
							person.getFirstName()+ " "  + person.getLastName()));

			toast = Toast.makeText(
					OpenTenureApplication.getContext(), message,
					Toast.LENGTH_SHORT);
			toast.show();

			OpenTenureApplication.getPersonsFragment().refresh();

		}

		else {

			String message = String.format(OpenTenureApplication
					.getContext().getString(
							R.string.message_error_remove_person,
							person.getFirstName() + " " + person.getLastName()));

			toast = Toast.makeText(
					OpenTenureApplication.getContext(), message,
					Toast.LENGTH_SHORT);
			toast.show();
			


		}
		
	}

}
