/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.controller;


import it.sayservice.platform.client.DomainEngineClient;
import it.sayservice.platform.client.DomainObject;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.ServicePOIObject;
import eu.trentorise.smartcampus.presentation.common.util.Util;
import eu.trentorise.smartcampus.processor.EventProcessorImpl;

@Controller
public class ServicePOIController extends AbstractObjectController {

	@Autowired
	private DomainEngineClient domainEngineClient; 
	
	@RequestMapping(method = RequestMethod.PUT, value="/eu.trentorise.smartcampus.dt.model.ServicePOIObject/{id}")
	public ResponseEntity<ServicePOIObject> updatePOI(HttpServletRequest request, @RequestBody Map<String,Object> objMap, @PathVariable String id) {
		ServicePOIObject obj = Util.convert(objMap, ServicePOIObject.class);
		
		Map<String,Object> parameters = new HashMap<String, Object>(1);

		Map<String,Object> customData = new HashMap<String, Object>();
		if (obj.getType() != null) {
			customData.put("type", obj.getType());
		}
		parameters.put("newCustomData", customData);
		parameters.put("newCommunityData",  Util.convert(obj.getCommunityData(), Map.class));
		try {
			domainEngineClient.invokeDomainOperation(
					"updateCustomData", 
					"eu.trentorise.smartcampus.domain.discovertrento.ServicePOIObject", 
					obj.getDomainId(),
					parameters, null, null); 
			
			String oString = domainEngineClient.searchDomainObject(obj.getDomainType(), obj.getDomainId(), null);
			DomainObject dObj = new DomainObject(oString);
			POIObject uObj = EventProcessorImpl.convertPOIObject(dObj);
			storage.storeObject(uObj);

			
		} catch (Exception e) {
			logger.error("Failed to update ServicePOI: "+e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<ServicePOIObject>(HttpStatus.METHOD_FAILURE);
		}
		
		return new ResponseEntity<ServicePOIObject>(obj,HttpStatus.OK);
	}

//	@RequestMapping(method = RequestMethod.DELETE, value="/eu.trentorise.smartcampus.dt.model.UserPOIObject/{id}")
//	public ResponseEntity<UserPOIObject> deletePOI(HttpServletRequest request, @PathVariable String id) {
//
//		POIObject poi = null;
//		try {
//			poi = storage.getObjectById(id,POIObject.class);
//		} catch (NotFoundException e) {
//			return new ResponseEntity<UserPOIObject>(HttpStatus.OK);
//		} catch (DataException e) {
//			logger.error("Failed to delete userPOI: "+e.getMessage());
//			return new ResponseEntity<UserPOIObject>(HttpStatus.METHOD_FAILURE);
//		}
//		try {
//			domainEngineClient.invokeDomainOperation(
//					"deletePOI", 
//					"eu.trentorise.smartcampus.domain.discovertrento.UserPOIObject", 
//					poi.getDomainId(),
//					new HashMap<String, Object>(0), null, null); 
//		} catch (InvocationException e) {
//			logger.error("Failed to delete userPOI: "+e.getMessage());
//			return new ResponseEntity<UserPOIObject>(HttpStatus.METHOD_FAILURE);
//		}
//		
//		return new ResponseEntity<UserPOIObject>(HttpStatus.OK);
//	}
//
//	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.UserPOIObject")
//	public ResponseEntity<List<POIObject>> getAllPOIObject() throws DataException {
//		Map<String,Object> criteria = new HashMap<String, Object>();
//		criteria.put("domainType", "eu.trentorise.smartcampus.domain.discovertrento.UserPOIObject");
//		List<POIObject> list = storage.searchObjects(POIObject.class, criteria);
//		return new ResponseEntity<List<POIObject>>(list, HttpStatus.OK);
//	}
//
//	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.UserPOIObject/{id}")
//	public ResponseEntity<BasicObject> getPOIObjectById(@PathVariable String id) throws DataException {
//		try {
//			return new ResponseEntity<BasicObject>(storage.getObjectById(id, POIObject.class),HttpStatus.OK);
//		} catch (NotFoundException e) {
//			logger.error("UserPOIObject with id "+ id+" does not exist");
//			return new ResponseEntity<BasicObject>(HttpStatus.METHOD_FAILURE);
//		}
//	}
}
