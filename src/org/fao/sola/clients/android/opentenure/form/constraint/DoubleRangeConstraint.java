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
package org.fao.sola.clients.android.opentenure.form.constraint;

import java.math.BigDecimal;
import java.text.MessageFormat;

import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintType;
import org.fao.sola.clients.android.opentenure.form.FieldType;
import org.fao.sola.clients.android.opentenure.form.FieldValue;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("DoubleRangeConstraint")
public class DoubleRangeConstraint extends FieldConstraint {

	public DoubleRangeConstraint() {
		super();
		type = FieldConstraintType.DOUBLE_RANGE;
		addApplicableType(FieldType.DECIMAL);
		this.errorMsg = "Value {1} of {0} is not between {2} and {3}";
	}

	public DoubleRangeConstraint(DoubleRangeConstraint drc) {
		super(drc);
		setMinValue(drc.getMinValue());
		setMaxValue(drc.getMaxValue());
	}

	public DoubleRangeConstraint(BigDecimal minValue, BigDecimal maxValue) {
		super();
		type = FieldConstraintType.DOUBLE_RANGE;
		addApplicableType(FieldType.DECIMAL);
		setMinValue(minValue);
		setMaxValue(maxValue);
	}

	@Override
	public void setMinValue(BigDecimal minValue) {
		if(minValue != null){
			this.minValue = new BigDecimal(minValue.doubleValue());
		}else{
			this.minValue = null;
		}
	}

	@Override
	public void setMaxValue(BigDecimal maxValue) {
		if(maxValue != null){
			this.maxValue = new BigDecimal(maxValue.doubleValue());
		}else{
			this.maxValue = null;
		}
	}

	@Override
	public boolean check(FieldValue fieldValue) {
		displayErrorMsg = null;
		if ((minValue != null && fieldValue != null
				&& fieldValue.getBigDecimalPayload() != null && fieldValue.getBigDecimalPayload().compareTo(minValue) < 0)
				|| (maxValue != null && fieldValue != null
						&& fieldValue.getBigDecimalPayload() != null && fieldValue.getBigDecimalPayload().compareTo(maxValue) > 0)) {
			displayErrorMsg = MessageFormat.format(errorMsg, displayName, fieldValue.getBigDecimalPayload(), minValue, maxValue);
			return false;
		}
		return true;
	}
}
