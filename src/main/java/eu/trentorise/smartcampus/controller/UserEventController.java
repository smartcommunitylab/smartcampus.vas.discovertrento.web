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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.UserEventObject;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.common.util.Util;
import eu.trentorise.smartcampus.presentation.data.BasicObject;
import eu.trentorise.smartcampus.processor.EventProcessorImpl;

@Controller
public class UserEventController extends AbstractObjectController {

	@Autowired
	private DomainEngineClient domainEngineClient; 
	
	@RequestMapping(method = RequestMethod.POST, value="/eu.trentorise.smartcampus.dt.model.UserEventObject")
	public ResponseEntity<UserEventObject> createEvent(HttpServletRequest request, @RequestBody Map<String,Object> objMap) {
		UserEventObject obj = Util.convert(objMap, UserEventObject.class);
		EventObject tmp = null;
		obj.setId(new ObjectId().toString());
		Map<String,Object> parameters = new HashMap<String, Object>();
		try {
			obj.setCreatorId(getUserId(request));
			obj.setDomainType("eu.trentorise.smartcampus.domain.discovertrento.UserEventObject");
			tmp = Util.convert(obj, EventObject.class);
			storage.storeObject(tmp);
			
			parameters.put("creator", getUserId(request));
			if (obj.getPoiId() == null) {
				logger.error("Error creating UserEvent: empty poiId");
				return new ResponseEntity<UserEventObject>(HttpStatus.METHOD_FAILURE);
			}
			POIObject poi = storage.getObjectById(obj.getPoiId(), POIObject.class);
			parameters.put("data", Util.convert(obj.toGenericEvent(poi), Map.class));
			parameters.put("communityData",  Util.convert(obj.getCommunityData(), Map.class));
			domainEngineClient.invokeDomainOperation(
					"createEvent", 
					"eu.trentorise.smartcampus.domain.discovertrento.UserEventFactory", 
					"eu.trentorise.smartcampus.domain.discovertrento.UserEventFactory.0", 
					parameters, null, null);
			
		} catch (Exception e) {
			logger.error("Failed to create userEvent: "+e.getMessage());
			e.printStackTrace();
			try {
				if (tmp != null) storage.deleteObject(tmp);
			} catch (DataException e1) {
				logger.error("Failed to cleanup userEvent: "+e1.getMessage());
			}

			return new ResponseEntity<UserEventObject>(HttpStatus.METHOD_FAILURE);
		} 
		return new ResponseEntity<UserEventObject>(obj,HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value="/eu.trentorise.smartcampus.dt.model.UserEventObject/{id}")
	public ResponseEntity<EventObject> updateEvent(HttpServletRequest request, @RequestBody Map<String,Object> objMap, @PathVariable String id) {
		UserEventObject obj = Util.convert(objMap, UserEventObject.class);
		try {
			if (obj.getPoiId() == null) {
				logger.error("Error creating UserEvent: empty poiId");
				return new ResponseEntity<EventObject>(HttpStatus.METHOD_FAILURE);
			}
			
			Map<String,Object> parameters = new HashMap<String, Object>();
			String operation = null;
			// TODO IN THIS WAY CAN MODIFY ONLY OWN OBJECTS, OTHERWISE ONLY TAGS IN COMMUNITY DATA
			if (!getUserId(request).equals(obj.getCreatorId())) {
				operation = "updateCommunityData";
				if (obj.getCommunityData() != null) {
					obj.getCommunityData().setNotes(null);
					obj.getCommunityData().setRatings(null);
				}
				parameters.put("newCommunityData",  Util.convert(obj.getCommunityData(), Map.class));
			} else {
				operation = "updateEvent";
				POIObject poi = storage.getObjectById(obj.getPoiId(), POIObject.class);
				parameters.put("newData", Util.convert(obj.toGenericEvent(poi), Map.class)); 
				parameters.put("newCommunityData",  Util.convert(obj.getCommunityData(), Map.class));
			}
			
			if (obj.getDomainId() ==  null) {
				DomainObject dobj = upgradeDO(obj, domainEngineClient);
				if (dobj != null) {
					EventObject newObj = EventProcessorImpl.convertEventObject(dobj, storage);
					obj.setEntityId(newObj.getEntityId());
				}
			}
			
			domainEngineClient.invokeDomainOperation(
					operation, 
					"eu.trentorise.smartcampus.domain.discovertrento.UserEventObject", 
					obj.getDomainId(),
					parameters, null, null); 
			
			String oString = domainEngineClient.searchDomainObject(obj.getDomainType(), obj.getDomainId(), null);
			DomainObject dObj = new DomainObject(oString);
			EventObject uObj = EventProcessorImpl.convertEventObject(dObj, storage);
			storage.storeObject(uObj);
			uObj.filterUserData(getUserId(request));
			return new ResponseEntity<EventObject>(uObj,HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed to update userEvent: "+e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<EventObject>(HttpStatus.METHOD_FAILURE);
		}
		
	}

	@RequestMapping(method = RequestMethod.DELETE, value="/eu.trentorise.smartcampus.dt.model.UserEventObject/{id}")
	public ResponseEntity<UserEventObject> deleteEvent(HttpServletRequest request, @PathVariable String id) {

		EventObject event = null;
		try {
			event = storage.getObjectById(id,EventObject.class);
			// CAN DELETE ONLY OWN OBJECTS
			if (!getUserId(request).equals(event.getCreatorId())) {
				logger.error("Attempt to delete not owned object. User "+getUserId(request)+", object "+event.getId());
				return new ResponseEntity<UserEventObject>(HttpStatus.METHOD_FAILURE);
			}
		} catch (NotFoundException e) {
			return new ResponseEntity<UserEventObject>(HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed to delete userEvent: "+e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<UserEventObject>(HttpStatus.METHOD_FAILURE);
		}


		try {
			if (event.getDomainId() == null) {
				upgradeDO(event, domainEngineClient);
			}
			if (event.getDomainId() != null) {
				Map<String,Object> parameters = new HashMap<String, Object>(0);
				domainEngineClient.invokeDomainOperation(
						"deleteEvent", 
						"eu.trentorise.smartcampus.domain.discovertrento.UserEventObject", 
						event.getDomainId(),
						parameters, null, null); 
				storage.deleteObject(event);
			}
		} catch (Exception e) {
			logger.error("Failed to delete userEvent: "+e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<UserEventObject>(HttpStatus.METHOD_FAILURE);
		}
		
		return new ResponseEntity<UserEventObject>(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.UserEventObject")
	public ResponseEntity<List<EventObject>> getAllEventObject(HttpServletRequest request) throws Exception {
		Map<String,Object> criteria = new HashMap<String, Object>();
		criteria.put("domainType", "eu.trentorise.smartcampus.domain.discovertrento.UserEventObject");
		List<EventObject> list = storage.searchObjects(EventObject.class, criteria);
		String userId = getUserId(request);
		for (BaseDTObject bo : list) {
			bo.filterUserData(userId);
		}
		return new ResponseEntity<List<EventObject>>(list, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.UserEventObject/{id}")
	public ResponseEntity<BasicObject> getEventObjectById(HttpServletRequest request, @PathVariable String id) throws Exception {
		try {
			EventObject e = storage.getObjectById(id, EventObject.class);
			if (e != null) e.filterUserData(getUserId(request));
			return new ResponseEntity<BasicObject>(e,HttpStatus.OK);
		} catch (NotFoundException e) {
			logger.error("UserEventObject with id "+ id+" does not exist");
			return new ResponseEntity<BasicObject>(HttpStatus.METHOD_FAILURE);
		}
	}

}
