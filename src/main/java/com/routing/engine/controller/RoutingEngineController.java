/**
 * 
 */
package com.routing.engine.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.routing.engine.redis.RedisMap;
import com.routing.engine.request.BestRouteInput;
import com.routing.engine.response.BestRouteOutput;
import com.routing.engine.response.GenericResponse;
import com.routing.engine.services.RoutingEngineService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author 
 *
 */
@RestController
@RequestMapping("/engine")
@CrossOrigin
public class RoutingEngineController {

	@Autowired
	RoutingEngineService routingEngineService;
	
//	@Value("${agent.id}")
//	private String agentId;

	@Autowired
	private RedisMap redisMap;



	@PostMapping("/bestRoute")
	public GenericResponse bestRoute(@RequestBody BestRouteInput request, HttpServletRequest servlet)
			throws ParseException, JsonParseException, JsonMappingException, IOException {

		GenericResponse gResponse = new GenericResponse();
		gResponse = new GenericResponse();
		gResponse.setError("");
		gResponse.setStatus(1001);

		// String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new
		// java.util.Date());
		BestRouteOutput response = new BestRouteOutput();
		/*
		 * if(request.getCrmId().equals("5") ) { response.setAgentId("5000");
		 * response.setAgentName("Henry"); } else if(request.getCrmId().equals("10")) {
		 * response.setAgentId("10000"); response.setAgentName("Jon"); } else {
		 * response.setAgentId(agentId); response.setAgentName(agentName); }
		 */
		String agentId = servlet.getHeader("TENANTID");
		response.setUserId(agentId);
		gResponse.setMessage("Test API");
		response.setAgentName("Test agentName");
		gResponse.setValue(response);


		return gResponse;

	}



	@GetMapping("/get-value/{hashKey}/{field}")
	public String getValueFromRedis(@PathVariable String hashKey, @PathVariable String field) {
		return redisMap.getValueFromHash(hashKey, field);
	}


	@PostMapping("/bestRoutev1")
	public ResponseEntity<GenericResponse> bestRouteNew(@RequestBody BestRouteInput request, HttpServletRequest header)
			throws JsonParseException, JsonMappingException, IOException {
		String tenantId = header.getHeader("TENANTID");
		System.out.println(tenantId);
		return routingEngineService.getBestRoute(request, tenantId);
	}
	@PostMapping("/availableAgent")
	public ResponseEntity<GenericResponse> availableAgent(@RequestBody String agentDetailsJson,HttpServletRequest header)
			throws JsonParseException, JsonMappingException, IOException {
		String tenantId = header.getHeader("TENANTID");
		System.out.println(tenantId);
		return routingEngineService.availableAgent(agentDetailsJson,tenantId);
	}

//	@PostMapping("/bestRouteEmail")
//	public ResponseEntity<GenericResponse> bestRouteEmail(@RequestBody BestRouteEmailInput request)
//			throws JsonParseException, JsonMappingException, IOException {
//		return routingEngineService.getBestRouteEmail(request);
//	}

//	public String getAgentId() {
//		return agentId;
//	}
	
	@PostMapping("/acceptChat")
	public ResponseEntity<GenericResponse> acceptChat(@RequestBody BestRouteInput request,HttpServletRequest header)
			throws JsonParseException, JsonMappingException, IOException {
		String tenantId = header.getHeader("TENANTID");
		System.out.println(tenantId);
		return routingEngineService.acceptChat(request,tenantId);
	}
}
