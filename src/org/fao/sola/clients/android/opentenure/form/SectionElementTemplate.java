package org.fao.sola.clients.android.opentenure.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SectionElementTemplate {

	@JsonIgnore
	private String id;
	private String name;
	private String displayName;
	private List<FieldTemplate> fields;
	@JsonIgnore
	private SectionTemplate section;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SectionElementTemplate(){
		this.id = UUID.randomUUID().toString();
		this.fields = new ArrayList<FieldTemplate>();
	}

	public SectionElementTemplate(SectionElementTemplate st){
		this.id = UUID.randomUUID().toString();
		this.name = new String(st.getName());
		this.displayName = new String(st.getDisplayName());
		this.fields = new ArrayList<FieldTemplate>();
		for(FieldTemplate fieldTemplate:st.getFields()){
			this.fields.add(new FieldTemplate(fieldTemplate));
		}
	}

	@Override
	public String toString() {
		return "SectionElementTemplate ["
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public List<FieldTemplate> getFields() {
		return fields;
	}

	public void setFields(List<FieldTemplate> fields) {
		this.fields = fields;
	}

	public void addField(FieldTemplate fieldTemplate) {
		fields.add(fieldTemplate);
	}

	public FieldConstraint getFailedConstraint(SectionElementPayload payload) {

		Iterator<FieldPayload> payloadIterator = payload.getFields().iterator();

		for(FieldTemplate fieldTemplate : fields){
			FieldConstraint fieldConstraint = fieldTemplate.getFailedConstraint(payloadIterator.next());
			if(fieldConstraint != null){
				return fieldConstraint;
			}
		}
		return null;
	}

	public SectionTemplate getSection() {
		return section;
	}

	public void setSection(SectionTemplate sectionTemplate) {
		this.section = sectionTemplate;
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
	
	public static SectionElementTemplate fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		SectionElementTemplate section;
		try {
			section = mapper.readValue(json, SectionElementTemplate.class);
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
