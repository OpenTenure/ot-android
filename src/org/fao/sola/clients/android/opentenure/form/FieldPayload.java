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
import java.math.BigDecimal;
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
	private FieldValueType valueType;
	private String stringPayload;
	private BigDecimal bigDecimalPayload;
	private Boolean booleanPayload;
	
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

	public FieldValueType getValueType() {
		return valueType;
	}
	public void setValueType(FieldValueType type) {
		this.valueType = type;
	}

	public String getStringPayload() {
		return stringPayload;
	}

	public void setStringPayload(String stringPayload) {
		this.stringPayload = stringPayload;
	}

	public BigDecimal getBigDecimalPayload() {
		return bigDecimalPayload;
	}

	public void setBigDecimalPayload(BigDecimal bigDecimalPayload) {
		this.bigDecimalPayload = bigDecimalPayload;
	}

	public Boolean getBooleanPayload() {
		return booleanPayload;
	}

	public void setBooleanPayload(Boolean booleanPayload) {
		this.booleanPayload = booleanPayload;
	}

	
	@Override
	public String toString() {
		return "FieldPayload ["
				+ "id=" + id
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", type=" + type
				+ ", stringPayload=" + stringPayload
				+ ", bigDecimalPayload=" + bigDecimalPayload
				+ ", booleanPayload=" + booleanPayload
				+ ", valueType=" + valueType
				+ "]";
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
		
		this.type = field.getType();
		if(field.getStringPayload() != null){
			this.stringPayload = new String(field.getStringPayload());
		}
		if(field.getBigDecimalPayload() != null){
			this.bigDecimalPayload = new BigDecimal(field.getBigDecimalPayload().toString());
		}
		if(field.getBooleanPayload() != null){
			this.booleanPayload = Boolean.valueOf(field.getBooleanPayload());
		}

		this.valueType = field.getValueType();
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
			this.valueType = FieldValueType.BOOL;
			break;
		case DATE:
			this.valueType = FieldValueType.TEXT;
			break;
		case TIME:
			this.valueType = FieldValueType.TEXT;
			break;
		case DECIMAL:
			this.valueType = FieldValueType.NUMBER;
			break;
		case DOCUMENT:
			this.valueType = FieldValueType.TEXT;
			break;
		case GEOMETRY:
			this.valueType = FieldValueType.TEXT;
			break;
		case INTEGER:
			this.valueType = FieldValueType.NUMBER;
			break;
		case SNAPSHOT:
			this.valueType = FieldValueType.TEXT;
			break;
		case TEXT:
			this.valueType = FieldValueType.TEXT;
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
