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
import it.sayservice.platform.core.common.util.ServiceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.Circle;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.trentorise.smartcampus.common.Constants;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.ObjectFilter;
import eu.trentorise.smartcampus.dt.model.POIData;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.SimpleDTObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.processor.EventProcessorImpl;

@Controller
public class ObjectController extends AbstractObjectController {

	private static final String SEARCH_FILTER_PARAM = "filter";
	
	@Autowired
	private DomainEngineClient domainEngineClient; 

//	@Autowired
//	private SemanticClient semanticClient; 

	@RequestMapping(value="/objects/{id}/rate", method = RequestMethod.PUT)
	public ResponseEntity<Object> rate(HttpServletRequest request, @RequestParam String rating, @PathVariable String id) {
		try {
			BaseDTObject obj = (BaseDTObject)storage.getObjectById(id);
			Map<String,Object> parameters = new HashMap<String, Object>();
			parameters.put("user", getUserId(request));
			parameters.put("rating", rating);
			Object o = ServiceUtil.deserializeObject((byte[])domainEngineClient.invokeDomainOperationSync("rate", obj.getDomainType(), obj.getDomainId(), parameters, null));
			String oString = domainEngineClient.searchDomainObject(obj.getDomainType(), obj.getDomainId(), null);
			DomainObject dObj = new DomainObject(oString);
			if (obj instanceof EventObject)
				obj = EventProcessorImpl.convertEventObject(dObj, storage);
			else if (obj instanceof POIObject)
				obj = EventProcessorImpl.convertPOIObject(dObj);
			else if (obj instanceof StoryObject)
				obj = EventProcessorImpl.convertStoryObject(dObj, storage);
			storage.storeObject(obj);
			return new ResponseEntity<Object>(o, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed to rate object with id " +id+": "+e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.METHOD_FAILURE);
		}
	}

	@RequestMapping(value="/objects/{id}/attend", method = RequestMethod.PUT)
	public ResponseEntity<Object> attend(HttpServletRequest request,  @PathVariable String id) {
		return performDomainOperation(request, id, "attend");
	}

	@RequestMapping(value="/objects/{id}/notAttend", method = RequestMethod.PUT)
	public ResponseEntity<Object> notAttend(HttpServletRequest request, @PathVariable String id) {
		return performDomainOperation(request, id, "notAttend");
	}

	private ResponseEntity<Object> performDomainOperation(HttpServletRequest request, String id, String operation) {
		try {
			BaseDTObject obj = (BaseDTObject)storage.getObjectById(id);
			Map<String,Object> parameters = new HashMap<String, Object>();
			String userId = getUserId(request);
			parameters.put("user", userId);
			domainEngineClient.invokeDomainOperation(operation, obj.getDomainType(), obj.getDomainId(), parameters, null, null);
			String oString = domainEngineClient.searchDomainObject(obj.getDomainType(), obj.getDomainId(), null);
			DomainObject dObj = new DomainObject(oString);
			if (obj instanceof EventObject){
				obj = EventProcessorImpl.convertEventObject(dObj, storage);
				EventObject.filterUserData((EventObject)obj, userId);
			}
			else if (obj instanceof POIObject) {
				obj = EventProcessorImpl.convertPOIObject(dObj);
			} 
			else if (obj instanceof StoryObject){
				obj = EventProcessorImpl.convertStoryObject(dObj, storage);
				StoryObject.filterUserData((StoryObject)obj, userId);
			}

			storage.storeObject(obj);
			return new ResponseEntity<Object>(obj, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed to update object with id " +id+": "+e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.METHOD_FAILURE);
		}
	}
	@RequestMapping(method = RequestMethod.GET, value = "/objects")
	public ResponseEntity<Map<String,List<BaseDTObject>>> getAllObject(HttpServletRequest request) throws Exception {
		Map<String,List<BaseDTObject>> map = new HashMap<String, List<BaseDTObject>>();
		String userId = getUserId(request);
		try {
			ObjectFilter filterObj = null;
			Map<String,Object> criteria = null;
			Class<?> clazz = null;
			String filter = request.getParameter(SEARCH_FILTER_PARAM);
			if (filter != null) {
				filterObj = new ObjectMapper().readValue(filter, ObjectFilter.class);//Util.convert(filter, ObjectFilter.class);
				criteria = filterObj.getCriteria() != null ? filterObj.getCriteria() : new HashMap<String, Object>();
				if (filterObj.getDomainType() != null) {
					criteria.put("domainType", filterObj.getDomainType());
				}
				if (filterObj.getTypes() != null && ! filterObj.getTypes().isEmpty()) {
					criteria.put("type", Collections.singletonMap("$in", filterObj.getTypes()));
				}
				if (filterObj.isMyObjects()) criteria.put("attending", userId);
				if (filterObj.getClassName() != null) {
					try {
						clazz = Thread.currentThread().getContextClassLoader().loadClass(filterObj.getClassName());
					} catch (Exception e) {
						logger.warn("Unknown class: "+filterObj.getClassName());
					}
				}
			} else {
				filterObj = new ObjectFilter();
				criteria = Collections.emptyMap();
			}

			if (filterObj.getSkip()==null) filterObj.setSkip(0);

			Circle circle = null;
			if (filterObj.getCenter() != null && filterObj.getRadius() != null) {
				circle = new Circle(filterObj.getCenter()[0], filterObj.getCenter()[1],filterObj.getRadius());
			}
			List<BaseDTObject> objects = null;
			
			if (filterObj.getLimit() != null) {
				objects = storage.searchObjects((Class<BaseDTObject>)clazz, circle, filterObj.getText(), filterObj.getFromTime(), filterObj.getToTime(), criteria, filterObj.getSort(), filterObj.getLimit(), filterObj.getSkip());
			} else {
				objects = storage.searchObjects((Class<BaseDTObject>)clazz, circle, filterObj.getText(), filterObj.getFromTime(), filterObj.getToTime(), criteria, filterObj.getSort());
			}
			if (objects != null) {
				for (BaseDTObject o : objects) {
					List<BaseDTObject> protos = map.get(o.getClass().getCanonicalName());
					if (protos == null) {
						protos = new ArrayList<BaseDTObject>();
						map.put(o.getClass().getCanonicalName(), protos);
					}
					if (o instanceof EventObject) {
						EventObject.filterUserData((EventObject)o, userId);
					}
					if (o instanceof StoryObject) {
						StoryObject.filterUserData((StoryObject)o, userId);
					}
					protos.add(o);
				}
			}
		} catch (Exception e) {
			logger.error("failed to find objects: "+ e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<Map<String,List<BaseDTObject>>>(HttpStatus.METHOD_FAILURE);
		}
		return new ResponseEntity<Map<String,List<BaseDTObject>>>(map,HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/objects/simple")
	public ResponseEntity<Map<String,List<SimpleDTObject>>> getAllObjectSimple(HttpServletRequest request) throws Exception {
		ResponseEntity<Map<String,List<BaseDTObject>>> all = getAllObject(request);
		Map<String,List<SimpleDTObject>> map = new HashMap<String, List<SimpleDTObject>>();
		if (all.getBody() != null) {
			for (String key : all.getBody().keySet()) {
				List<BaseDTObject> list = all.getBody().get(key);
				List<SimpleDTObject> nlist = new ArrayList<SimpleDTObject>();
				if (list != null) {
					for (BaseDTObject o : list) {
						SimpleDTObject so = new SimpleDTObject(o);
						if (o instanceof POIObject) {
							so.setAddress(POIData.getAddressString(((POIObject) o).getPoi()));
							so.setEntityType(Constants.ENTTIY_TYPE_POI);
						} 
						else if (o instanceof EventObject) {
							so.setEntityType(Constants.ENTTIY_TYPE_EVENT);
							POIObject poi = null;
							try {
								poi = storage.getObjectById(((EventObject) o).getPoiId(), POIObject.class);
							} catch (NotFoundException e) {
							}
							if (poi != null) so.setAddress(POIData.getAddressString(poi.getPoi()));
						}
						nlist.add(so);
					}
				}
				map.put(key, nlist);
			}
		}
		return new ResponseEntity<Map<String,List<SimpleDTObject>>>(map, all.getStatusCode());
		
	}

}
