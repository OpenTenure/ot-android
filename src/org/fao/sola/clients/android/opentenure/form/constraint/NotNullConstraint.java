package org.fao.sola.clients.android.opentenure.form.constraint;

import java.text.MessageFormat;

import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintType;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FieldType;
import org.fao.sola.clients.android.opentenure.form.FieldValueType;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("NotNullConstraint")
public class NotNullConstraint extends FieldConstraint {

	public NotNullConstraint(){
		super();
		type=FieldConstraintType.NOT_NULL;
		addApplicableType(FieldType.DATE);
		addApplicableType(FieldType.DECIMAL);
		addApplicableType(FieldType.DOCUMENT);
		addApplicableType(FieldType.GEOMETRY);
		addApplicableType(FieldType.INTEGER);
		addApplicableType(FieldType.SNAPSHOT);
		addApplicableType(FieldType.TEXT);
		addApplicableType(FieldType.TIME);
		this.errorMsg = "Value of {0} is mandatory";
	}

	public NotNullConstraint(NotNullConstraint nnc){
		super(nnc);
	}

	@Override
	public boolean check(FieldPayload fieldPayload) {
		displayErrorMsg = null;
		if(fieldPayload == null
				|| (fieldPayload.getBooleanPayload()==null && fieldPayload.getValueType() == FieldValueType.BOOL)
				|| (fieldPayload.getBigDecimalPayload()==null && fieldPayload.getValueType() == FieldValueType.NUMBER)
				|| (fieldPayload.getStringPayload()==null && fieldPayload.getValueType() == FieldValueType.TEXT)){
			displayErrorMsg = MessageFormat.format(errorMsg, displayName);
			return false;
		}
		return true;
	}
}
