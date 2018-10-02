package com.faisal.path.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.faisal.exception.path.json.JsonException;

public class JsonTokener {

	private int character;
	private boolean eof;
	private int index;
	private int line;
	private char previous;
	private Reader reader;
	private boolean usePrevious;

	public JsonTokener(String s) {
		this(new StringReader(s));
	}

	public JsonTokener(Reader reader) {
		this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
		this.eof = false;
		this.usePrevious = false;
		this.previous = 0;
		this.index = 0;
		this.character = 1;
		this.line = 1;
	}

	public Object nextValue(char firstChar) throws JsonException {
		char c = firstChar;
		String string;

		switch (c) {
		case '"':
		case '\'':
			return nextString(c);
		case '{':
			return nextJSONAsString(c);
		case '[':
			return nextJSONArrayAsString(c);
		}

		StringBuffer sb = new StringBuffer();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
			sb.append(c);
			c = next();
		}
		back();

		string = sb.toString().trim();
		if (string.equals("")) {
			throw syntaxError("Missing value");
		}
		return JsonObject.stringToValue(string);
	}

	public String nextJSONAsString(char braceChar) throws JsonException {
		char c;
		StringBuffer sb = new StringBuffer();
		int braceCount = 1;
		sb.append(braceChar);
		do {
			c = next();
			switch (c) {
			case 0:
			case '{':
				sb.append(c);
				braceCount++;
				break;
			case '}':
				sb.append(c);
				braceCount--;
				break;
			case '\n':
			case '\r':
				throw syntaxError("Unterminated string");
			case '\\':
				c = next();
				switch (c) {
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'u':
					sb.append((char) Integer.parseInt(next(4), 16));
					break;
				case '"':
				case '\'':
				case '\\':
				case '/':
					sb.append(c);
					break;
				default:
					throw syntaxError("Illegal escape.");
				}
				break;
			default:
				sb.append(c);
			}
		} while (braceCount > 0);
		return sb.toString();
	}

	public String nextJSONArrayAsString(char braceChar) throws JsonException {
		int braceCount = 1;
		char c;
		StringBuffer sb = new StringBuffer();
		sb.append(braceChar);
		do {
			c = next();
			switch (c) {
			case 0:
			case '[':
				sb.append(c);
				braceCount++;
				break;
			case ']':
				sb.append(c);
				braceCount--;
				break;
			case '\n':
			case '\r':
				throw syntaxError("Unterminated string");
			case '\\':
				c = next();
				switch (c) {
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'u':
					sb.append((char) Integer.parseInt(next(4), 16));
					break;
				case '"':
				case '\'':
				case '\\':
				case '/':
					sb.append(c);
					break;
				default:
					throw syntaxError("Illegal escape.");
				}
				break;
			default:
				sb.append(c);
			}
		} while (braceCount > 0);
		return sb.toString();
	}

	public char nextClean() throws JsonException {
		for (;;) {
			char c = next();
			if (c == 0 || c > ' ') {
				return c;
			}
		}
	}

	public char next() throws JsonException {
		int c;
		if (this.usePrevious) {
			this.usePrevious = false;
			c = this.previous;
		} else {
			try {
				c = this.reader.read();
			} catch (IOException exception) {
				throw new JsonException(exception);
			}

			if (c <= 0) { // End of stream
				this.eof = true;
				c = 0;
			}
		}
		this.index += 1;
		if (this.previous == '\r') {
			this.line += 1;
			this.character = c == '\n' ? 0 : 1;
		} else if (c == '\n') {
			this.line += 1;
			this.character = 0;
		} else {
			this.character += 1;
		}
		this.previous = (char) c;
		return this.previous;
	}

	public String next(int n) throws JsonException {
		if (n == 0) {
			return "";
		}

		char[] chars = new char[n];
		int pos = 0;

		while (pos < n) {
			chars[pos] = next();
			if (end()) {
				throw syntaxError("Substring bounds error");
			}
			pos += 1;
		}
		return new String(chars);
	}

	public boolean end() {
		return eof && !usePrevious;
	}

	public JsonException syntaxError(String message) {
		return new JsonException(message + toString());
	}

	public String toString() {
		return " at " + index + " [character " + this.character + " line " + this.line + "]";
	}

	public void back() throws JsonException {
		if (usePrevious || index <= 0) {
			throw new JsonException("Stepping back two steps is not supported");
		}
		this.index -= 1;
		this.character -= 1;
		this.usePrevious = true;
		this.eof = false;
	}

	public String nextString(char quote) throws JsonException {
		char c;
		StringBuffer sb = new StringBuffer();
		for (;;) {
			c = next();
			switch (c) {
			case 0:
			case '\n':
			case '\r':
				throw syntaxError("Unterminated string");
			case '\\':
				c = next();
				switch (c) {
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'u':
					sb.append((char) Integer.parseInt(next(4), 16));
					break;
				case '"':
				case '\'':
				case '\\':
				case '/':
					sb.append(c);
					break;
				default:
					throw syntaxError("Illegal escape." + c);
				}
				break;
			default:
				if (c == quote) {
					return sb.toString();
				}
				sb.append(c);
			}
		}
	}

}
