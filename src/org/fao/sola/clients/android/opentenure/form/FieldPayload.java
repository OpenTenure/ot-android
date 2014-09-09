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
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FieldPayload {
	
	@JsonIgnore
	private String id;
	@JsonIgnore
	private SectionElementPayload sectionElement;
	protected String name;
	protected String displayName;
	protected FieldType type;
	protected FieldValue value;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SectionElementPayload getSectionElement() {
		return sectionElement;
	}

	public void setSectionElement(SectionElementPayload sectionElement) {
		this.sectionElement = sectionElement;
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
	
	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public FieldValue getValue() {
		return value;
	}
	
	public void setValue(FieldValue fieldValue) {
		if(fieldValue!=null){
			fieldValue.setId(getId());
			fieldValue.setField(this);
		}
		this.value = fieldValue;
	}
	
	@Override
	public String toString() {
		return "FieldPayload ["
				+ "value=" + value
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", id=" + id
				+ ", type=" + type + "]";
	}
	
	public FieldPayload(){
		this.id = UUID.randomUUID().toString();
	}

	public FieldPayload(FieldPayload field){
		this.id = UUID.randomUUID().toString();
		if(field.getName() != null){
			this.name = new String(field.getName());
		}

		if(field.getDisplayName() != null){
			this.displayName = new String(field.getDisplayName());
		}
		
		this.value = new FieldValue(field.getValue());

		this.type = field.getType();
	}

	public FieldPayload(FieldTemplate field){
		this.id = UUID.randomUUID().toString();
		if(field.getName() != null){
			this.name = new String(field.getName());
		}
		if(field.getDisplayName() != null){
			this.displayName = new String(field.getDisplayName());
		}
		switch(field.getType()){
		case BOOL:
			this.value = new FieldValue(FieldValueType.BOOL);
			break;
		case DATE:
			this.value = new FieldValue(FieldValueType.TEXT);
			break;
		case TIME:
			this.value = new FieldValue(FieldValueType.TEXT);
			break;
		case DECIMAL:
			this.value = new FieldValue(FieldValueType.NUMBER);
			break;
		case DOCUMENT:
			this.value = new FieldValue(FieldValueType.TEXT);
			break;
		case GEOMETRY:
			this.value = new FieldValue(FieldValueType.TEXT);
			break;
		case INTEGER:
			this.value = new FieldValue(FieldValueType.NUMBER);
			break;
		case SNAPSHOT:
			this.value = new FieldValue(FieldValueType.TEXT);
			break;
		case TEXT:
			this.value = new FieldValue(FieldValueType.TEXT);
			break;
		}
		this.type = field.getType();
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
	
	public static FieldPayload fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		FieldPayload field;
		try {
			field = mapper.readValue(json, FieldPayload.class);
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
