package com.routing.engine.services;

import org.springframework.http.ResponseEntity;

import com.routing.engine.request.BestRouteEmailInput;
import com.routing.engine.request.BestRouteInput;
import com.routing.engine.response.GenericResponse;

public interface RoutingEngineService {
	ResponseEntity<GenericResponse> getBestRoute(BestRouteInput request,String tenantId);
	ResponseEntity<GenericResponse> availableAgent(String agentDetailsJson,String tenantId);
	//ResponseEntity<GenericResponse> pushChatToPriorityQueue(BestRouteInput request);
	ResponseEntity<GenericResponse> acceptChat(BestRouteInput request,String tenantId);
//	ResponseEntity<GenericResponse> getBestRouteEmail(BestRouteEmailInput request);
}
