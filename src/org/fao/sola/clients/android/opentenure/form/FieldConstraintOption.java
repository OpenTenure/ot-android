package org.fao.sola.clients.android.opentenure.form;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FieldConstraintOption {
	@JsonIgnore
	private String id;
	@JsonIgnore
	private FieldConstraint fieldConstraint;
	protected String name;
	protected String displayName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FieldConstraint getFieldConstraint() {
		return fieldConstraint;
	}

	public void setFieldConstraint(FieldConstraint fieldConstraint) {
		this.fieldConstraint = fieldConstraint;
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

	public FieldConstraintOption(){
		this.id = UUID.randomUUID().toString();
	}

	public FieldConstraintOption(String name, String displayName){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = displayName;
	}

	public FieldConstraintOption(FieldConstraintOption fieldConstraintOption){
		this.id = UUID.randomUUID().toString();
		this.name = new String(fieldConstraintOption.getName());
		this.displayName = new String(fieldConstraintOption.getDisplayName());
	}

	@Override
	public String toString() {
		return "Option [name=" + name
				+ ", displayName=" + displayName
				+ ", id=" + id
				+ "]";
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
	
	public static FieldConstraintOption fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		FieldConstraintOption fieldConstraintOption;
		try {
			fieldConstraintOption = mapper.readValue(json, FieldConstraintOption.class);
			return fieldConstraintOption;
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
