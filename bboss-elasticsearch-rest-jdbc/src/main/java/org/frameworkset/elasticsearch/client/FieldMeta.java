package org.frameworkset.elasticsearch.client;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.frameworkset.util.annotations.DateFormateMeta;

public class FieldMeta {
	private String esFieldName;
	private String dbColumnName;
	private DateFormateMeta dateFormateMeta;
	public String getEsFieldName() {
		return esFieldName;
	}

	public void setEsFieldName(String esFieldName) {
		this.esFieldName = esFieldName;
	}

	public String getDbColumnName() {
		return dbColumnName;
	}

	public void setDbColumnName(String dbColumnName) {
		this.dbColumnName = dbColumnName;
	}

	public DateFormateMeta getDateFormateMeta() {
		return dateFormateMeta;
	}

	public void setDateFormateMeta(DateFormateMeta dateFormateMeta) {
		this.dateFormateMeta = dateFormateMeta;
	}


}
