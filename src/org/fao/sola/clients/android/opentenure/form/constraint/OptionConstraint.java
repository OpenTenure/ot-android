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
package org.fao.sola.clients.android.opentenure.form.constraint;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintOption;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintType;
import org.fao.sola.clients.android.opentenure.form.FieldType;
import org.fao.sola.clients.android.opentenure.form.FieldValue;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("OptionConstraint")
public class OptionConstraint extends FieldConstraint {

	public void addOption(FieldConstraintOption fieldConstraintOption) {
		fieldConstraintOption.setFieldConstraint(this);
		fieldConstraintOptions.add(fieldConstraintOption);
		this.errorMsg = "Value {1} of {0} is not one of {2}";
	}

	public OptionConstraint() {
		super();
		type = FieldConstraintType.OPTION;
		addApplicableType(FieldType.TEXT);
		addApplicableType(FieldType.INTEGER);
		addApplicableType(FieldType.DECIMAL);
	}

	public OptionConstraint(OptionConstraint oc) {
		super(oc);
	}

	public OptionConstraint(List<FieldConstraintOption> fieldConstraintOptions) {
		super();
		type = FieldConstraintType.OPTION;
		addApplicableType(FieldType.TEXT);
		addApplicableType(FieldType.INTEGER);
		addApplicableType(FieldType.DECIMAL);
		setOptions(fieldConstraintOptions);
	}

	@Override
	public boolean check(FieldValue fieldValue) {
		displayErrorMsg = null;
		for(FieldConstraintOption fieldConstraintOption:fieldConstraintOptions){
			if(fieldConstraintOption.getName().equalsIgnoreCase(fieldValue.getStringPayload())){
				return true;
			}
		}
		List<String> optionValues = new ArrayList<String>();
		for(FieldConstraintOption fieldConstraintOption:fieldConstraintOptions){
			optionValues.add(fieldConstraintOption.getName());
		}
		displayErrorMsg = MessageFormat.format(errorMsg, displayName, fieldValue.getStringPayload(), Arrays.toString(optionValues.toArray()));
		return false;
	}
}
