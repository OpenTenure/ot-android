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
	@JsonIgnore
	private String id;
	private String name;
	private String displayName;
	private List<FieldPayload> fields;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public SectionElementPayload(){
		this.id = UUID.randomUUID().toString();
		this.fields = new ArrayList<FieldPayload>();
	}

	public SectionElementPayload(SectionElementPayload se){
		this.id = UUID.randomUUID().toString();
		this.name = new String(se.getName());
		this.displayName = se.getDisplayName();
		this.fields = new ArrayList<FieldPayload>();
		for(FieldPayload fieldPayload:se.getFields()){
			this.fields.add(new FieldPayload(fieldPayload));
		}
	}

	public SectionElementPayload(SectionTemplate st) {
		this.id = UUID.randomUUID().toString();
		this.name = new String(st.getElementName());
		this.displayName = st.getDisplayName();
		this.fields = new ArrayList<FieldPayload>();
		for(FieldTemplate ft:st.getFields()){
			this.fields.add(new FieldPayload(ft));
		}
	}

	@Override
	public String toString() {
		return "SectionElementPayload ["
				+ "id=" + id
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", fields=" + Arrays.toString(fields.toArray())
				+ "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<FieldPayload> getFields() {
		return fields;
	}

	public void setFields(List<FieldPayload> fields) {
		this.fields = fields;
	}

	public void addField(FieldPayload field) {
		fields.add(field);
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
