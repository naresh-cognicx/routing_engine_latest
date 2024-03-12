package com.routing.engine.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routing.engine.request.ChatRequestDetails;
import com.routing.engine.services.impl.RoutingEngineServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

	public static String nullRemove(String value) {
		return value != null ? value:"";
	}
	
	public static List<String> convertStringIntoList(String val){
		ObjectMapper mapper = new ObjectMapper();
		LinkedList<String>ap = null;		
		try {
			 ap = mapper.readValue(val, LinkedList.class);
			 
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error : "+e.getMessage());
//			e.printStackTrace();
		}
		return ap ;
	}

	public static LinkedHashMap<String,Object> convertStringIntMap(String val){
		ObjectMapper mapper = new ObjectMapper();
		LinkedHashMap <String,Object> ap = null;		
		try {
			 ap = mapper.readValue(val, LinkedHashMap.class);
			 
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error : "+e.getMessage());
//			e.printStackTrace();
		}
		return ap ;
	}

}
