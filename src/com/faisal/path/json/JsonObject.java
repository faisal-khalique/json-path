package com.faisal.path.json;

import java.util.LinkedHashMap;
import java.util.Map;

import com.faisal.exception.path.json.JsonException;

public class JsonObject {

	public Map<String, Object> maps = new LinkedHashMap<String, Object>();
	public static final Object NULL = new NULL();

	public JsonObject(JsonTokener x, Map<String, Object> mappingKeys) throws JsonException {
		Map<String, Object> mappingKeysCopy = new LinkedHashMap<String, Object>();
		mappingKeysCopy.putAll(mappingKeys);
		char c;
		String key;

		c = x.nextClean();
		if (c != '{') {
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}
		c = x.nextClean();
		do {
			switch (c) {
			case 0:
				throw x.syntaxError("A JSONObject text must end with '}'");
			case '}':
				return;
			default:
				key = (String) x.nextValue(c);
			}

			c = x.nextClean();
			if (c != ':') {
				throw x.syntaxError("Expected a ':' after a key");
			}
			putOnce(getMappedKey(key, mappingKeysCopy), x.nextValue(x.nextClean()));

			char f = x.nextClean();
			switch (f) {
			case ',':
				c = x.nextClean();
				if (c == '}') {
					return;
				}
				break;
			case '}':
				return;
			default:
				throw x.syntaxError("Expected a ',' or '}' got " + f);
			}
		} while (mappingKeysCopy.size() > 0);
	}

	public JsonObject putOnce(String key, Object value) throws JsonException {
		if (key != null && value != null) {
			if (opt(key) != null) {
				throw new JsonException("Duplicate key \"" + key + "\"");
			}
			put(key, value);
		}
		return this;
	}

	public JsonObject put(String key, Object value) throws JsonException {
		testValidity(value);
		this.maps.put(key, value);
		return this;
	}

	public Object remove(String key) {
		return this.maps.remove(key);
	}

	private String getMappedKey(String jsonKey, Map<String, Object> mappedKeys) {
		String keyToReturn = null;
		Object mappedKey = mappedKeys.remove(jsonKey);
		if (mappedKey != null) {
			keyToReturn = jsonKey;
			if (String.class.isAssignableFrom(mappedKey.getClass())) {
				keyToReturn = String.class.cast(mappedKey);
			}
		}
		return keyToReturn;
	}

	public Object opt(String key) {
		return key == null ? null : this.maps.get(key);
	}

	public static void testValidity(Object o) throws JsonException {
		if (o != null) {
			if (o instanceof Double) {
				if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
					throw new JsonException("JSON does not allow non-finite numbers.");
				}
			} else if (o instanceof Float) {
				if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
					throw new JsonException("JSON does not allow non-finite numbers.");
				}
			}
		}
	}

	public static Object stringToValue(String string) {
		if (string.equals("")) {
			return string;
		}
		if (string.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (string.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}
		if (string.equalsIgnoreCase("null")) {
			return JsonObject.NULL;
		}

		char b = string.charAt(0);
		if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
			if (b == '0' && string.length() > 2 && (string.charAt(1) == 'x' || string.charAt(1) == 'X')) {
				try {
					return new Integer(Integer.parseInt(string.substring(2), 16));
				} catch (Exception ignore) {
				}
			}
			try {
				if (string.indexOf('.') > -1 || string.indexOf('e') > -1 || string.indexOf('E') > -1) {
					return Double.valueOf(string);
				} else {
					Long myLong = new Long(string);
					if (myLong.longValue() == myLong.intValue()) {
						return new Integer(myLong.intValue());
					} else {
						return myLong;
					}
				}
			} catch (Exception ignore) {
			}
		}
		return string;
	}

	private static final class NULL {

		protected final Object clone() {
			return this;
		}

		public boolean equals(Object object) {
			return object == null || object == this;
		}

		public String toString() {
			return "null";
		}
	}

}
