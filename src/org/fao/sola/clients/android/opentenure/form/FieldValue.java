package org.fao.sola.clients.android.opentenure.form;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FieldValue {
	
	private String id;
	@JsonIgnore
	private FieldPayload fieldPayload;
	private FieldValueType fieldValueType;
	private String stringPayload;
	private BigDecimal bigDecimalPayload;
	private Boolean booleanPayload;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FieldPayload getFieldPayload() {
		return fieldPayload;
	}

	public void setFieldPayload(FieldPayload fieldPayload) {
		this.id = fieldPayload.getId();
		this.fieldPayload = fieldPayload;
	}

	public FieldValueType getFieldValueType() {
		return fieldValueType;
	}
	public void setFieldValueType(FieldValueType fieldValueType) {
		this.fieldValueType = fieldValueType;
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
		return "Value ["
				+ "id=" + id
				+ ", stringPayload=" + stringPayload
				+ ", bigDecimalPayload=" + bigDecimalPayload
				+ ", booleanPayload=" + booleanPayload
				+ ", fieldValueType=" + fieldValueType + "]";
	}

	public FieldValue(){
		this.id = UUID.randomUUID().toString();
		this.fieldValueType = FieldValueType.TEXT;
	}

	public FieldValue(FieldValueType type){
		this.id = UUID.randomUUID().toString();
		this.fieldValueType = type;
	}

	public FieldValue(FieldValue fieldValue){
		this.id = UUID.randomUUID().toString();
		this.fieldValueType = fieldValue.getFieldValueType();
		if(fieldValue.getStringPayload() != null){
			this.stringPayload = new String(fieldValue.getStringPayload());
		}
		if(fieldValue.getBigDecimalPayload() != null){
			this.bigDecimalPayload = new BigDecimal(fieldValue.getBigDecimalPayload().toString());
		}
		if(fieldValue.getBooleanPayload() != null){
			this.booleanPayload = Boolean.valueOf(fieldValue.getBooleanPayload());
		}
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
	
	public static FieldValue fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		FieldValue field;
		try {
			field = mapper.readValue(json, FieldValue.class);
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
