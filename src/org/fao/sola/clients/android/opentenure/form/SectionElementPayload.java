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

public class SectionElementPayload {
	
	@JsonIgnore
	private SectionPayload section;
	private String id;
	private List<FieldPayload> fieldPayloadList;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SectionElementPayload(){
		this.id = UUID.randomUUID().toString();
		this.fieldPayloadList = new ArrayList<FieldPayload>();
	}

	public SectionElementPayload(SectionElementPayload se){
		this.id = UUID.randomUUID().toString();
		this.fieldPayloadList = new ArrayList<FieldPayload>();
		for(FieldPayload fieldPayload:se.getFieldPayloadList()){
			this.fieldPayloadList.add(new FieldPayload(fieldPayload));
		}
	}

	public SectionElementPayload(SectionTemplate st) {
		this.id = UUID.randomUUID().toString();
		this.fieldPayloadList = new ArrayList<FieldPayload>();
		for(FieldTemplate ft:st.getFieldTemplateList()){
			this.fieldPayloadList.add(new FieldPayload(ft));
		}
	}

	@Override
	public String toString() {
		return "SectionElementPayload ["
				+ "id=" + id
				+ ", fieldPayloadList=" + Arrays.toString(fieldPayloadList.toArray())
				+ "]";
	}

	public List<FieldPayload> getFieldPayloadList() {
		return fieldPayloadList;
	}

	public void setFieldPayloadList(List<FieldPayload> fieldPayloadList) {
		this.fieldPayloadList = fieldPayloadList;
	}

	public void addField(FieldPayload field) {
		fieldPayloadList.add(field);
	}

	public SectionPayload getSection() {
		return section;
	}

	public void setSection(SectionPayload sectionPayload) {
		this.section = sectionPayload;
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
	
	public static SectionElementPayload fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		SectionElementPayload section;
		try {
			section = mapper.readValue(json, SectionElementPayload.class);
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
