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

package org.springframework.jdbc.roma.impl.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.jdbc.roma.impl.util.RowMapperUtil;

/**
 * @author Serkan ÖZAL
 */
public class RowMapperUtilTest {

	@Test
	public void generateRandomClassPostFix() {
		String generatedPostFix;
		
		generatedPostFix = RowMapperUtil.generateRandomClassPostFix();
		Assert.assertNotNull(generatedPostFix);
		Assert.assertEquals(36, generatedPostFix.length());
		
		Map<String, String> generatedPostFixMap = new HashMap<String, String>();
		for (int i = 0; i < 100; i++) {
			generatedPostFix = RowMapperUtil.generateRandomClassPostFix();
			Assert.assertFalse(generatedPostFixMap.containsKey(generatedPostFix));
			generatedPostFixMap.put(generatedPostFix, generatedPostFix);
		}
	}
	
}
