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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SectionPayload {
	
	@JsonIgnore
	private String id;
	@JsonIgnore
	private FormPayload form;
	private String name;
	private String displayName;
	private int minOccurrences;
	private int maxOccurrences;
	private List<SectionElementPayload> elements;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FormPayload getForm() {
		return form;
	}

	public void setForm(FormPayload form) {
		this.form = form;
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

	public List<SectionElementPayload> getElements() {
		return elements;
	}

	public void setElements(List<SectionElementPayload> elements) {
		this.elements = elements;
	}
	
	@Override
	public String toString() {
		return "SectionPayload ["
				+ "id=" + id
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", minOccurrences=" + minOccurrences
				+ ", maxOccurrences=" + maxOccurrences
				+ ", elements=" + Arrays.toString(elements.toArray())
				+ "]";
	}

	public SectionPayload(String name, String displayName){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = displayName;
		this.elements = new ArrayList<SectionElementPayload>();
	}

	public SectionPayload(){
		this.id = UUID.randomUUID().toString();
		this.elements = new ArrayList<SectionElementPayload>();
	}

	public SectionPayload(SectionPayload sp){
		this.id = UUID.randomUUID().toString();
		this.name = new String(sp.getName());
		this.displayName = new String(sp.getDisplayName());
		this.minOccurrences = sp.getMinOccurrences();
		this.maxOccurrences = sp.getMaxOccurrences();
		this.elements = new ArrayList<SectionElementPayload>();
		for(SectionElementPayload sep:sp.getElements()){
			this.elements.add(new SectionElementPayload(sep));
		}
	}

	public SectionPayload(SectionTemplate st){
		this.id = UUID.randomUUID().toString();
		this.name = new String(st.getName());
		this.minOccurrences = st.getMinOccurrences();
		this.maxOccurrences = st.getMaxOccurrences();
		this.displayName = new String(st.getDisplayName());
		this.elements = new ArrayList<SectionElementPayload>();
	}

	public void addElement(SectionElementPayload element) {
		elements.add(element);
	}

	public void addElement(SectionTemplate sectionTemplate) {
		this.addElement(new SectionElementPayload(sectionTemplate));
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
	
	public static SectionPayload fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		SectionPayload section;
		try {
			section = mapper.readValue(json, SectionPayload.class);
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
