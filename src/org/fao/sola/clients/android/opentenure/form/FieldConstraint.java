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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
include=JsonTypeInfo.As.PROPERTY,
property="classType")

@JsonSubTypes({
    @JsonSubTypes.Type(value=org.fao.sola.clients.android.opentenure.form.constraint.DateTimeFormatConstraint.class, name="DateTimeFormatConstraint"),
    @JsonSubTypes.Type(value=org.fao.sola.clients.android.opentenure.form.constraint.DoubleRangeConstraint.class, name="DoubleRangeConstraint"),
    @JsonSubTypes.Type(value=org.fao.sola.clients.android.opentenure.form.constraint.IntegerRangeConstraint.class, name="IntegerRangeConstraint"),
    @JsonSubTypes.Type(value=org.fao.sola.clients.android.opentenure.form.constraint.IntegerConstraint.class, name="IntegerConstraint"),
    @JsonSubTypes.Type(value=org.fao.sola.clients.android.opentenure.form.constraint.LengthConstraint.class, name="LengthConstraint"),
    @JsonSubTypes.Type(value=org.fao.sola.clients.android.opentenure.form.constraint.NotNullConstraint.class, name="NotNullConstraint"),
    @JsonSubTypes.Type(value=org.fao.sola.clients.android.opentenure.form.constraint.OptionConstraint.class, name="OptionConstraint"),
    @JsonSubTypes.Type(value=org.fao.sola.clients.android.opentenure.form.constraint.RegexpFormatConstraint.class, name="RegexpFormatConstraint")
})

public class FieldConstraint {
	@JsonIgnore
	protected String id;
	@JsonIgnore
	protected FieldTemplate fieldTemplate;
	protected String name;
	protected String displayName;
	protected String errorMsg;
	protected String displayErrorMsg;
	protected String format;
	protected BigDecimal minValue;
	protected BigDecimal maxValue;
	protected FieldConstraintType type;
	@JsonIgnore
	protected List<FieldType> applicableTypes;
	protected List<FieldConstraintOption> fieldConstraintOptions;
	protected boolean check(FieldValue fieldValue){
		displayErrorMsg = "You can't check a generic constraint";
		return false;
	}


	public FieldTemplate getFieldTemplate() {
		return fieldTemplate;
	}


	public void setFieldTemplate(FieldTemplate fieldTemplate) {
		this.fieldTemplate = fieldTemplate;
	}

	public List<FieldConstraintOption> getOptions() {
		return fieldConstraintOptions;
	}

	public void setOptions(List<FieldConstraintOption> fieldConstraintOptions) {
		this.fieldConstraintOptions = fieldConstraintOptions;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}


	public BigDecimal getMinValue() {
		return minValue;
	}

	public void setMinValue(BigDecimal minValue) {
		if(minValue != null){
			this.minValue = new BigDecimal(minValue.toString());
		}else{
			this.minValue = null;
		}
	}

	public BigDecimal getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(BigDecimal maxValue) {
		if(maxValue != null){
			this.maxValue = new BigDecimal(maxValue.toString());
		}else{
			this.maxValue = null;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public FieldConstraintType getType() {
		return type;
	}

	public void setType(FieldConstraintType type) {
		this.type = type;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public List<FieldType> getApplicableTypes() {
		return applicableTypes;
	}

	public void setApplicableTypes(List<FieldType> applicableTypes) {
		this.applicableTypes = applicableTypes;
	}

	@Override
	public String toString() {
		return "Constraint ["
				+ "id=" + id
				+ ", applicableTypes=" + Arrays.toString(applicableTypes.toArray())
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", errorMsg=" + errorMsg
				+ ", displayErrorMsg=" + displayErrorMsg
				+ ", format=" + format
				+ ", minValue=" + minValue
				+ ", maxValue=" + maxValue
				+ ", options=" + Arrays.toString(fieldConstraintOptions.toArray())
				+ ", type=" + type + "]";
	}

	public String displayErrorMsg() {
		return displayErrorMsg;
	}

	public FieldConstraint() {
		this.id = UUID.randomUUID().toString();
		applicableTypes = new ArrayList<FieldType>();
		fieldConstraintOptions = new ArrayList<FieldConstraintOption>();
	}
	
	public FieldConstraint(FieldConstraint fieldConstraint) {
		this.id = UUID.randomUUID().toString();
		if(fieldConstraint.getName() != null){
			this.name = new String(fieldConstraint.getName());
		}
		if(fieldConstraint.getDisplayName() != null){
			this.displayName = new String(fieldConstraint.getDisplayName());
		}
		if(fieldConstraint.getErrorMsg() != null){
			this.errorMsg = new String(fieldConstraint.getErrorMsg());
		}
		this.type = fieldConstraint.getType();
		applicableTypes = new ArrayList<FieldType>();
		if(fieldConstraint.getApplicableTypes() != null){
			for(FieldType type:fieldConstraint.getApplicableTypes()){
				applicableTypes.add(type);
			}
		}
		fieldConstraintOptions = new ArrayList<FieldConstraintOption>();
		if(fieldConstraint.getOptions() != null){
			for(FieldConstraintOption option:fieldConstraint.getOptions()){
				fieldConstraintOptions.add(option);
			}
		}
	}
	
	protected void addApplicableType(FieldType type){
		applicableTypes.add(type);
	}

	public boolean appliesTo(FieldTemplate fieldTemplate){
		for(FieldType type:applicableTypes){
			if(fieldTemplate.getType() == type){
				return true;
			}
		}
		return false;
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
	
	public static FieldConstraint fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		FieldConstraint fieldConstraint;
		try {
			fieldConstraint = mapper.readValue(json, FieldConstraint.class);
			return fieldConstraint;
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
