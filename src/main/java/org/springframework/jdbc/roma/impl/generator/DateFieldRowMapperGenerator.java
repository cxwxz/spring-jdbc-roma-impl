/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.roma.impl.generator;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;

import org.springframework.jdbc.roma.api.config.manager.ConfigManager;
import org.springframework.jdbc.roma.api.domain.model.config.RowMapperTimeFieldConfig;

/**
 * @author Serkan ÖZAL
 */
public class DateFieldRowMapperGenerator<T> extends AbstractRowMapperFieldGenerator<T> {

	private boolean asTimestamp = false;
	
	public DateFieldRowMapperGenerator(Field field, ConfigManager configManager) {
		super(field, configManager);
		if (Timestamp.class.isAssignableFrom(field.getType())) {
			asTimestamp = true;
		}
		else {
			RowMapperTimeFieldConfig timeFieldConfig = configManager.getRowMapperTimeFieldConfig(field);
			if (timeFieldConfig != null && timeFieldConfig.isAsTimestamp()) {
				asTimestamp = true;
			}
		}	
	}
	
	@Override
	public String doFieldMapping(Field f) {
		String setterMethodName = getSetterMethodName(f);
		String setValueExpr = null;
		if (asTimestamp) {
			setValueExpr = RESULT_SET_ARGUMENT + ".getTimestamp(\"" + columnName + "\")";
		}
		else {
			setValueExpr = RESULT_SET_ARGUMENT + ".getDate(\"" + columnName + "\")";
		}	
		rowMapper.addAdditionalClass(Date.class);
		return 
			wrapWithNullCheck(	
				GENERATED_OBJECT_NAME + "." + setterMethodName + "(" + setValueExpr + ");",
				setterMethodName);
	}

}
