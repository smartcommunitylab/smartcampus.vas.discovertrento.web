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


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.data.BasicObject;

@Controller
public class StoryController extends AbstractObjectController {

	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.StoryObject")
	public ResponseEntity<List<StoryObject>> getAllStoryObject(HttpServletRequest request) throws Exception {
		List<StoryObject> list = storage.getObjectsByType(StoryObject.class);
		StoryObject.filterUserData(list, getUserId(request));
		return new ResponseEntity<List<StoryObject>>(list, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.StoryObject/{id}")
	public ResponseEntity<BasicObject> getStoryObjectById(HttpServletRequest request, @PathVariable String id) throws Exception {
		try {
			StoryObject o = storage.getObjectById(id, StoryObject.class);
			StoryObject.filterUserData(o, getUserId(request));
			return new ResponseEntity<BasicObject>(o,HttpStatus.OK);
		} catch (NotFoundException e) {
			logger.error("StoryObject with id "+ id+" does not exist");
			e.printStackTrace();
			return new ResponseEntity<BasicObject>(HttpStatus.METHOD_FAILURE);
		}
	}
}
