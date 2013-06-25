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

import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.dt.model.UserStoryObject;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.common.util.Util;
import eu.trentorise.smartcampus.presentation.data.BasicObject;
import eu.trentorise.smartcampus.processor.EventProcessorImpl;

@Controller
public class UserStoryController extends AbstractObjectController {

	@Autowired
	private DomainEngineClient domainEngineClient; 
	
	@RequestMapping(method = RequestMethod.POST, value="/eu.trentorise.smartcampus.dt.model.UserStoryObject")
	public ResponseEntity<UserStoryObject> createStory(HttpServletRequest request, @RequestBody Map<String,Object> objMap) {
		UserStoryObject obj = Util.convert(objMap, UserStoryObject.class);
		StoryObject tmp = null;
		obj.setId(new ObjectId().toString());
		Map<String,Object> parameters = new HashMap<String, Object>();
		try {
			obj.setCreatorId(getUserId(request));
			obj.setDomainType("eu.trentorise.smartcampus.domain.discovertrento.StoryObject");
			tmp = Util.convert(obj, StoryObject.class);
			storage.storeObject(tmp);
			
			parameters.put("creator", getUserId(request));
			parameters.put("data", Util.convert(obj.toGenericStory(), Map.class));
			parameters.put("communityData",  Util.convert(obj.getDomainCommunityData(), Map.class));
			domainEngineClient.invokeDomainOperation(
					"createStory", 
					"eu.trentorise.smartcampus.domain.discovertrento.StoryFactory", 
					"eu.trentorise.smartcampus.domain.discovertrento.StoryFactory.0", 
					parameters, null, null);
			
		} catch (Exception e) {
			logger.error("Failed to create userStory: "+e.getMessage());
			e.printStackTrace();
			try {
				if (tmp != null) storage.deleteObject(tmp);
			} catch (DataException e1) {
				logger.error("Failed to cleanup userStory: "+e1.getMessage());
			}

			return new ResponseEntity<UserStoryObject>(HttpStatus.METHOD_FAILURE);
		} 
		return new ResponseEntity<UserStoryObject>(obj,HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value="/eu.trentorise.smartcampus.dt.model.UserStoryObject/{id}")
	public ResponseEntity<StoryObject> updateStory(HttpServletRequest request, @RequestBody Map<String,Object> objMap, @PathVariable String id) {
		UserStoryObject obj = Util.convert(objMap, UserStoryObject.class);
		try {
			Map<String,Object> parameters = new HashMap<String, Object>();
			String operation = null;
			// TODO IN THIS WAY CAN MODIFY ONLY OWN OBJECTS, OTHERWISE ONLY TAGS IN COMMUNITY DATA
			if (!getUserId(request).equals(obj.getCreatorId())) {
				operation = "updateCommunityData";
				if (obj.getCommunityData() != null) {
					obj.getCommunityData().setNotes(null);
					obj.getCommunityData().setRatings(null);
				}
				parameters.put("newCommunityData",  Util.convert(obj.getDomainCommunityData(), Map.class));
			} else {
				operation = "updateStory";
				parameters.put("newData", Util.convert(obj.toGenericStory(), Map.class)); 
				parameters.put("newCommunityData",  Util.convert(obj.getDomainCommunityData(), Map.class));
			}
			
			if (obj.getDomainId() ==  null) {
				DomainObject dobj = upgradeDO(obj, domainEngineClient);
				if (dobj != null) {
					StoryObject newObj = EventProcessorImpl.convertStoryObject(dobj, storage);
					obj.setEntityId(newObj.getEntityId());
				}
			}

			domainEngineClient.invokeDomainOperation(
					operation, 
					"eu.trentorise.smartcampus.domain.discovertrento.StoryObject", 
					obj.getDomainId(),
					parameters, null, null); 
			
			String oString = domainEngineClient.searchDomainObject(obj.getDomainType(), obj.getDomainId(), null);
			DomainObject dObj = new DomainObject(oString);
			StoryObject uObj = EventProcessorImpl.convertStoryObject(dObj, storage);
			storage.storeObject(uObj);
			
			uObj.filterUserData(getUserId(request));
			return new ResponseEntity<StoryObject>(uObj,HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Failed to update userStory: "+e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<StoryObject>(HttpStatus.METHOD_FAILURE);
		}
		
	}

	@RequestMapping(method = RequestMethod.DELETE, value="/eu.trentorise.smartcampus.dt.model.UserStoryObject/{id}")
	public ResponseEntity<UserStoryObject> deleteStory(HttpServletRequest request, @PathVariable String id) {

		StoryObject story = null;
		try {
			story = storage.getObjectById(id,StoryObject.class);
			// CAN DELETE ONLY OWN OBJECTS
			if (!getUserId(request).equals(story.getCreatorId())) {
				logger.error("Attempt to delete not owned object. User "+getUserId(request)+", object "+story.getId());
				return new ResponseEntity<UserStoryObject>(HttpStatus.METHOD_FAILURE);
			}
		} catch (NotFoundException e) {
			return new ResponseEntity<UserStoryObject>(HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed to delete userStory: "+e.getMessage());
			return new ResponseEntity<UserStoryObject>(HttpStatus.METHOD_FAILURE);
		}

		
		Map<String,Object> parameters = new HashMap<String, Object>(0);
		try {
			if (story.getDomainId() == null) {
				upgradeDO(story, domainEngineClient);
			}
			if (story.getDomainId() != null) {
				domainEngineClient.invokeDomainOperation(
						"deleteStory", 
						"eu.trentorise.smartcampus.domain.discovertrento.StoryObject", 
						story.getDomainId(),
						parameters, null, null); 
				storage.deleteObject(story);
			}
		} catch (Exception e) {
			logger.error("Failed to delete userStory: "+e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<UserStoryObject>(HttpStatus.METHOD_FAILURE);
		}
		
		return new ResponseEntity<UserStoryObject>(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.UserStoryObject")
	public ResponseEntity<List<StoryObject>> getAllStoryObject(HttpServletRequest request) throws Exception {
		Map<String,Object> criteria = new HashMap<String, Object>();
		criteria.put("domainType", "eu.trentorise.smartcampus.domain.discovertrento.StoryObject");
		List<StoryObject> list = storage.searchObjects(StoryObject.class, criteria);
		String userId = getUserId(request);
		for (StoryObject so : list) {
			so.filterUserData(userId);
		}
		return new ResponseEntity<List<StoryObject>>(list, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.UserStoryObject/{id}")
	public ResponseEntity<BasicObject> getStoryObjectById(HttpServletRequest request, @PathVariable String id) throws Exception {
		try {
			StoryObject e = storage.getObjectById(id, StoryObject.class);
			if (e != null) e.filterUserData(getUserId(request));
			return new ResponseEntity<BasicObject>(e,HttpStatus.OK);
		} catch (NotFoundException e) {
			logger.error("UserStoryObject with id "+ id+" does not exist");
			return new ResponseEntity<BasicObject>(HttpStatus.METHOD_FAILURE);
		}
	}

}
