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
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.form.constraint.IntegerRangeConstraint;
import org.fao.sola.clients.android.opentenure.form.field.IntegerField;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SectionTemplate {
	
	@JsonIgnore
	private String id;
	@JsonIgnore
	private FormTemplate form;
	private String name;
	private String displayName;
	private int minOccurrences;
	private int maxOccurrences;
	private SectionElementTemplate elementTemplate;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FormTemplate getForm() {
		return form;
	}

	public void setForm(FormTemplate formTemplate) {
		this.form = formTemplate;
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

	public int getMinOccurrences() {
		return minOccurrences;
	}

	public void setMinOccurrences(int minOccurrences) {
		this.minOccurrences = minOccurrences;
	}

	public int getMaxOccurrences() {
		return maxOccurrences;
	}

	public void setMaxOccurrences(int maxOccurrences) {
		this.maxOccurrences = maxOccurrences;
	}

	public SectionElementTemplate getElementTemplate() {
		return elementTemplate;
	}

	public void setElementTemplate(SectionElementTemplate elementTemplate) {
		if(elementTemplate != null){
			elementTemplate.setId(getId());
			elementTemplate.setSection(this);
		}
		this.elementTemplate = elementTemplate;
	}

	@Override
	public String toString() {
		return "SectionTemplate ["
				+ "id=" + id
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", minOccurrences=" + minOccurrences
				+ ", maxOccurrences=" + maxOccurrences
				+ ", elementTemplate=" + elementTemplate
				+ "]";
	}

	public SectionTemplate(String name, SectionElementTemplate elementTemplate){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = name;
		this.elementTemplate = elementTemplate;
		this.minOccurrences = 1;
		this.maxOccurrences = 1;
	}

	public SectionTemplate(SectionElementTemplate elementTemplate){
		this.id = UUID.randomUUID().toString();
		this.elementTemplate = elementTemplate;
	}

	public SectionTemplate(String name, String displayName, SectionElementTemplate elementTemplate){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = displayName;
		this.elementTemplate = elementTemplate;
		this.minOccurrences = 1;
		this.maxOccurrences = 1;
	}

	public SectionTemplate(String name, String displayName, int minOccurrences, int maxOccurrences, SectionElementTemplate elementTemplate){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = displayName;
		this.elementTemplate = elementTemplate;
		this.minOccurrences = minOccurrences;
		this.maxOccurrences = maxOccurrences;
	}

	public SectionTemplate(String name){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = name;
		this.elementTemplate = null;
		this.minOccurrences = 1;
		this.maxOccurrences = 1;
	}

	public SectionTemplate(){
		this.id = UUID.randomUUID().toString();
		this.elementTemplate = null;
	}

	public SectionTemplate(SectionTemplate sec){
		this.id = UUID.randomUUID().toString();
		this.minOccurrences = sec.minOccurrences;
		this.maxOccurrences = sec.maxOccurrences;
		this.name = new String(sec.getName());
		this.displayName = new String(sec.getDisplayName());
		this.elementTemplate = new SectionElementTemplate(sec.getElementTemplate());
	}

	public SectionTemplate(String name, String displayName){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = displayName;
		this.elementTemplate = null;
		this.minOccurrences = 1;
		this.maxOccurrences = 1;
	}
	
	public FieldConstraint getFailedConstraint(SectionPayload payload) {
		
		List<SectionElementPayload> elements = payload.getElements();

		if ((elements.size() < minOccurrences) || (elements.size() > maxOccurrences)) {
			IntegerRangeConstraint constraint = new IntegerRangeConstraint();
			constraint.setMinValue(BigDecimal.valueOf(minOccurrences));
			constraint.setMaxValue(BigDecimal.valueOf(maxOccurrences));
			FieldTemplate fieldTemplate = new IntegerField();
			FieldValue fieldValue = new FieldValue();
			fieldValue.setType(FieldValueType.NUMBER);
			fieldValue.setBigDecimalPayload(new BigDecimal(elements.size()));
			fieldTemplate.setName(name);
			fieldTemplate.setDisplayName(displayName);
			constraint.setErrorMsg("must contain between {1} and {2} items");
			try {
				fieldTemplate.addConstraint(constraint);
			} catch (Exception e) {
				e.printStackTrace();
			}
			constraint.check(fieldValue);
			return constraint;
		}
		for(SectionElementPayload element:elements){
			if(elementTemplate.getFailedConstraint(element) != null){
				return elementTemplate.getFailedConstraint(element);
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
	
	public static SectionTemplate fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		SectionTemplate section;
		try {
			section = mapper.readValue(json, SectionTemplate.class);
			return section;
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
