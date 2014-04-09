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

package org.springframework.jdbc.roma.impl.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.roma.api.generator.RowMapperFieldGenerator;
import org.springframework.jdbc.roma.impl.util.RowMapperUtil;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

import net.sf.cglib.proxy.LazyLoader;

/**
 * @author Serkan ÖZAL
 */
public abstract class ProxyMapLoader<K, V> implements LazyLoader {

	private static final Map<String, Class<ProxyMapLoader<?, ?>>> proxyMapLoaderClassMap = 
			new HashMap<String, Class<ProxyMapLoader<?, ?>>>();
	
	public abstract Map<K, V> load();
	
	@Override
	public Object loadObject() throws Exception {
		return load();
	}
	
	@SuppressWarnings("rawtypes")
	public static ProxyMapLoader createProxyMapLoader(String loadingCode, String classPath, Object[] parameters) {
		return createProxyMapLoader(null, loadingCode, classPath, parameters);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ProxyMapLoader createProxyMapLoader(String id, String loadingCode, String classPath, Object[] parameters) {
		Class<ProxyMapLoader<?, ?>> proxyMapLoaderClass = null;
		if (id != null) {
			proxyMapLoaderClass = proxyMapLoaderClassMap.get(id);
		}
		try {
			if (proxyMapLoaderClass == null) {
				ClassPool cp = ClassPool.getDefault();
				CtClass generatedCls = cp.makeClass("ProxyMapLoader" + RowMapperUtil.generateRandomClassPostFix());
				generatedCls.defrost();
				generatedCls.setSuperclass((cp.get(ProxyMapLoader.class.getName())));
				
				for (int i = 0; i < parameters.length; i++) {
					String fieldName = parameters[i++].toString();
					Object value = parameters[i];
					if (value == null) {
						continue;
					}
					CtField ctf = new CtField(cp.get(value.getClass().getName()), fieldName, generatedCls);
					generatedCls.addField(ctf);
					String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
					CtMethod ctfMapter = 
								new CtMethod(
										CtClass.voidType, 
										setterName,
										new CtClass[] {cp.get(value.getClass().getName())},
										generatedCls);
					ctfMapter.setModifiers(Modifier.PUBLIC);
					String setterBody = 
						"{" + "\n" +
							"\t" + "this." + fieldName + " = " + "$1;" + "\n" +		
						"}";
					ctfMapter.setBody(setterBody);
					generatedCls.addMethod(ctfMapter);
				}
	
				String variablesCode = "";
				int separatorIndex = loadingCode.indexOf(RowMapperFieldGenerator.VARIABLES_AND_CODE_SEPARATOR);
				if (separatorIndex > 0) {
					variablesCode = loadingCode.substring(0, separatorIndex);
					loadingCode = loadingCode.substring(separatorIndex + 
														RowMapperFieldGenerator.VARIABLES_AND_CODE_SEPARATOR.length());
				}
				CtMethod loadMethod = 
						new CtMethod(
			 					cp.get(Map.class.getName()), 
								"load",
								null,
								generatedCls);	
				loadMethod.setModifiers(Modifier.PUBLIC);
				String loadMethodBody = 
					"{" + "\n" +
						"\t" + variablesCode +
						"\t" + loadingCode + ";" + 
					"}";
				loadMethod.setBody(loadMethodBody);
				generatedCls.addMethod(loadMethod);
				
				proxyMapLoaderClass = generatedCls.toClass();
				
				if (id != null) {
					proxyMapLoaderClassMap.put(id, proxyMapLoaderClass);
				}
			}	
			
			ProxyMapLoader pll = (ProxyMapLoader)proxyMapLoaderClass.newInstance();
				
			for (int i = 0; i < parameters.length; i++) {
				String fieldName = parameters[i++].toString();
				Object value = parameters[i];
				if (value == null) {
					continue;
				}
				String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
				Method setterMethod = pll.getClass().getDeclaredMethod(setterName, value.getClass());
				setterMethod.invoke(pll, value);
			}
				
			return pll;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}
	
}
