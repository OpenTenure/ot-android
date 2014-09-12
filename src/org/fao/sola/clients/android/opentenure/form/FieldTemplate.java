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
package org.fao.sola.clients.android.opentenure.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.form.constraint.DateTimeFormatConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.DoubleRangeConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.IntegerConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.IntegerRangeConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.LengthConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.NotNullConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.OptionConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.RegexpFormatConstraint;
import org.fao.sola.clients.android.opentenure.form.exception.FormException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FieldTemplate {
	
	@JsonIgnore
	private String id;
	@JsonIgnore
	private SectionTemplate section;
	protected String name;
	protected String displayName;
	protected String hint;
	protected FieldType type;
	protected List<FieldConstraint> constraints;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SectionTemplate getSection() {
		return section;
	}

	public void setSection(SectionTemplate schema) {
		this.section = schema;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getHint() {
		return hint;
	}
	
	public void setHint(String hint) {
		this.hint = hint;
	}

	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public List<FieldConstraint> getConstraints() {
		return constraints;
	}
	
	public void setConstraints(List<FieldConstraint> fieldConconstraintsstraints) {
		this.constraints = fieldConconstraintsstraints;
	}

	@Override
	public String toString() {
		return "FieldTemplate ["
				+ "id=" + id
				+ ", name=" + name
				+ ", type=" + type
				+ ", hint=" + hint
				+ ", displayName=" + displayName
				+ ", constraints=" + Arrays.toString(constraints.toArray()) + "]";
	}
	
	public FieldTemplate(){
		this.id = UUID.randomUUID().toString();
		this.constraints = new ArrayList<FieldConstraint>();
	}

	public FieldTemplate(FieldTemplate fieldTemplate){
		this.id = UUID.randomUUID().toString();
		if(fieldTemplate.getName() != null){
			this.name = new String(fieldTemplate.getName());
		}

		if(fieldTemplate.getDisplayName() != null){
			this.displayName = new String(fieldTemplate.getDisplayName());
		}
		if(fieldTemplate.getHint() != null){
			this.hint = new String(fieldTemplate.getHint());
		}
		this.type = fieldTemplate.getType();
		this.constraints = new ArrayList<FieldConstraint>();
		if(fieldTemplate.getConstraints() != null){
			for(FieldConstraint con:fieldTemplate.getConstraints()){
				// Can't copy a constraint since it is an abstract class
				if(con instanceof DateTimeFormatConstraint){
					this.constraints.add(new DateTimeFormatConstraint((DateTimeFormatConstraint)con));
				}else if(con instanceof IntegerConstraint){
					this.constraints.add(new IntegerConstraint((IntegerConstraint)con));
				}else if(con instanceof LengthConstraint){
					this.constraints.add(new LengthConstraint((LengthConstraint)con));
				}else if(con instanceof NotNullConstraint){
					this.constraints.add(new NotNullConstraint((NotNullConstraint)con));
				}else if(con instanceof OptionConstraint){
					this.constraints.add(new OptionConstraint((OptionConstraint)con));
				}else if(con instanceof IntegerRangeConstraint){
					this.constraints.add(new IntegerRangeConstraint((IntegerRangeConstraint)con));
				}else if(con instanceof DoubleRangeConstraint){
					this.constraints.add(new DoubleRangeConstraint((DoubleRangeConstraint)con));
				}else if(con instanceof RegexpFormatConstraint){
					this.constraints.add(new RegexpFormatConstraint((RegexpFormatConstraint)con));
				}else{
					this.constraints.add(con);
				}
		}
		}
	}
	
	public void addConstraint(FieldConstraint fieldConstraint) throws Exception {
		if(fieldConstraint.appliesTo(this)){
			constraints.add(fieldConstraint);
		}else{
			throw new FormException("Constraint " + fieldConstraint.toString() + " does not apply to field " + this.toString());
		}
	}

	public FieldConstraint getFailedConstraint(FieldPayload payload) {
		for(FieldConstraint fieldConstraint : constraints){
			if(!fieldConstraint.check(payload)){
				return fieldConstraint;
			}
		}
		return null;
	}

	public String toJson() {
		ObjectMapper mapper = new ObjectMapper();
		  try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		  return null;
	}
	
	public static FieldTemplate fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		FieldTemplate field;
		try {
			field = mapper.readValue(json, FieldTemplate.class);
			return field;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
