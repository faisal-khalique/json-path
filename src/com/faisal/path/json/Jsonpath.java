package com.faisal.path.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.faisal.exception.path.json.JsonException;

public class Jsonpath {

	private Map<String, String> jpaths = null;
	private Map<String, Object> resolvedJPaths = null;
	private Map<String, Object> valuesResolvedByPath = null;
	private String jsonString = null;

	public Jsonpath() {
		this.resolvedJPaths = new LinkedHashMap<String, Object>();
		this.valuesResolvedByPath = new LinkedHashMap<String, Object>();
	}

	public Jsonpath(Map<String, String> jpaths, String jsonString) {
		this();
		this.jpaths = jpaths;
		this.jsonString = jsonString;
	}

	public void setJpaths(Map<String, String> jpaths) {
		this.jpaths = jpaths;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public Map<String, Object> parseJPaths() throws JsonException {
		readJPaths();
		processJSON(jsonString, resolvedJPaths);
		processNesteds();
		return valuesResolvedByPath;
	}

	public Map<String, Object> parseJPaths(boolean logPerfomanceTime) throws JsonException {
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
		long t1 = System.nanoTime();
		readJPaths();
		long t2 = System.nanoTime();
		processJSON(jsonString, resolvedJPaths);
		processNesteds();
		System.out
				.println("Time to restructure the JPaths - " + TimeUnit.NANOSECONDS.toMicros(t2 - t1) + " micro-sec.");
		long t3 = System.nanoTime();
		long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Time to process the JPaths - " + TimeUnit.NANOSECONDS.toMicros(t3 - t2) + " micro-sec.");
		System.out.println("Memory consumption is " + (memoryAfter - memoryBefore) + " bytes");
		return valuesResolvedByPath;
	}

	private void readJPaths() {
		for (Map.Entry<String, String> jpath : jpaths.entrySet()) {
			readJPath(jpath.getKey(), jpath.getValue(), resolvedJPaths);
		}
	}

	@SuppressWarnings("unchecked")
	private void readJPath(String key, String value, Map<String, Object> processedJPaths) {
		Map<String, Object> childJPath = null;
		if (key.contains(".")) {
			String[] keys = key.split("\\.", 2);
			childJPath = (Map<String, Object>) processedJPaths.get(keys[0]);
			if (childJPath == null) {
				childJPath = new LinkedHashMap<String, Object>();
			}
			readJPath(keys[1], value, childJPath);
			processedJPaths.put(keys[0], childJPath);
		} else {
			processedJPaths.put(key, value);
		}
	}

	private void processNesteds() throws JsonException {
		for (Map.Entry<String, Object> tag : resolvedJPaths.entrySet()) {
			processNesteds(tag.getKey(), tag.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	private void processNesteds(String tagKey, Object tagValue) throws JsonException {
		if (tagKey != null && tagValue != null) {
			Map<String, Object> tagValues = null;
			Object jObject = null;
			if (Map.class.isAssignableFrom(tagValue.getClass())) {
				tagValues = (Map<String, Object>) tagValue;
				jObject = valuesResolvedByPath.remove(tagKey);
				processJSON(jObject == null ? null : jObject.toString(), Map.class.cast(tagValue));
				for (Map.Entry<String, Object> tag : tagValues.entrySet()) {
					processNesteds(tag.getKey(), tag.getValue());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processJSON(String jsonString, Map<String, Object> tags) throws JsonException {
		if (jsonString != null && tags != null) {
			if (jsonString.startsWith("[")) {
				JsonArray jas = new JsonArray(new JsonTokener(jsonString));
				List<Object> lists = jas.myArrayList;
				JsonObject j = null;
				String listString = null;
				for (Object list : lists) {
					if (String.class.isAssignableFrom(list.getClass())) {
						listString = String.class.cast(list);
						if (listString.startsWith("[")) {
							processJSON(listString, tags);
						} else {
							j = new JsonObject(new JsonTokener(listString), tags);
							for (Map.Entry<String, Object> m : j.maps.entrySet()) {
								if (valuesResolvedByPath.get(m.getKey()) != null) {
									((List<Object>) valuesResolvedByPath.get(m.getKey())).add(m.getValue());
								} else {
									List<Object> l = new ArrayList<Object>();
									l.add(m.getValue());
									valuesResolvedByPath.put(m.getKey(), l);
								}
							}
						}
					}
				}
			} else {
				JsonObject j = new JsonObject(new JsonTokener(jsonString), tags);
				valuesResolvedByPath.putAll(j.maps);
			}
		}
	}

}
