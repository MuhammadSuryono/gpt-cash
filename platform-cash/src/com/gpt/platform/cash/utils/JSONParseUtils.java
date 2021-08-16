package com.gpt.platform.cash.utils;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;

public class JSONParseUtils {
	/**
	 * Reads the request body from the request and returns it as a String.
	 *
	 * @param request HttpServletRequest that contains the request body
	 * @return request body as a String or null
	 */
	public static String readRequestBody(HttpServletRequest request) throws Exception {
	    try {
	        // Read from request
	        StringBuilder buffer = new StringBuilder();
	        BufferedReader reader = request.getReader();
	        String line;
	        while ((line = reader.readLine()) != null) {
	            buffer.append(line);
	        }
	        return buffer.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	    }
	}
}
