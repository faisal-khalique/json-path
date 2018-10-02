package com.faisal.path.json;

import java.util.ArrayList;
import java.util.List;

import com.faisal.exception.path.json.JsonException;

public class JsonArray {

	public List<Object> myArrayList;

	public JsonArray() {
		myArrayList = new ArrayList<Object>();
	}

	public JsonArray(JsonTokener x) throws JsonException {
		this();
		char currentChar = x.nextClean();
		if (currentChar != '[') {
			throw x.syntaxError("A JSONArray text must start with '['");
		}
		char currentCharPlus1 = x.nextClean();
		if (currentCharPlus1 != ']') {
			for (;;) {
				if (currentCharPlus1 == ',') {
					this.myArrayList.add("null");
				} else {
					this.myArrayList.add(x.nextValue(currentCharPlus1));
				}
				currentCharPlus1 = x.nextClean();
				switch (currentCharPlus1) {
				case ',':
					currentCharPlus1 = x.nextClean();
					if (currentCharPlus1 == ']') {
						return;
					}
					break;
				case ']':
					return;
				default:
					throw x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}

}
