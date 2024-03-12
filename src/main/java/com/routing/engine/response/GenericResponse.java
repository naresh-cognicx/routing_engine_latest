package com.routing.engine.response;

import java.sql.Timestamp;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericResponse {

	private String timestamp;
	private Object status;
	private String error;
	private String message;
	@JsonIgnore
	private String path;
	private Object value;

	public GenericResponse() {
		this.timestamp = new Timestamp(new Date().getTime()).toString();
	}

	public GenericResponse(Object status, String message, String error, Object value) {
		this.status = status;
		this.message = message;
		this.error = error;
		this.value = value;
		this.timestamp = new Timestamp(new Date().getTime()).toString();
	}

	public GenericResponse(String message, String error) {
		this.message = message;
		this.error = error;
		this.timestamp = new Timestamp(new Date().getTime()).toString();
	}

	public GenericResponse(GenericResponse genericResponse) {
		Date date = new Date();
		long time = date.getTime();
		Timestamp ts = new Timestamp(time);
		this.timestamp = ts.toString();
		this.status = genericResponse.status;
		this.error = genericResponse.error;
		this.message = genericResponse.message;
		this.path = genericResponse.path;
		this.value = genericResponse.value;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Object getStatus() {
		return status;
	}

	public void setStatus(Object status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}