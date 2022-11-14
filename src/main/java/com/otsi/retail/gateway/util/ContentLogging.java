package com.otsi.retail.gateway.util;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Target;
import org.hibernate.annotations.Type;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

@Table(name = "content_logging")
@Entity
public class ContentLogging extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Type(type = "com.otsi.retail.gateway.util.CustomJsonType")
	@Target(Map.class)
	private Map<String, List<String>> headers;

	private String encryptedPayload;

	private String payload;

	private String requestUrl;

	private String requestURI;

	private String servletPath;

	private String contextPath;

	@Type(type = "com.otsi.retail.gateway.util.CustomJsonType")
	@Target(JsonNode.class)
	private JsonNode params;

	private String remoteIp;

	private String methodType;

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	private String inetIp;

	public String getInetIp() {
		return inetIp;
	}

	public void setInetIp(String inetIp) {
		this.inetIp = inetIp;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public String getMethodType() {
		return methodType;
	}

	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEncryptedPayload() {
		return encryptedPayload;
	}

	public void setEncryptedPayload(String encryptedPayload) {
		this.encryptedPayload = encryptedPayload;
	}

	public JsonNode getParams() {
		return params;
	}

	public void setParams(JsonNode params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "ContentLogging [headers=" + headers + ", payload=" + payload + ", requestUrl=" + requestUrl
				+ ", requestURI=" + requestURI + ", servletPath=" + servletPath + ", contextPath=" + contextPath
				+ ", params=" + params + ", remoteIp=" + remoteIp + ", methodType=" + methodType + ", inetIp=" + inetIp
				+ "]";
	}

}
