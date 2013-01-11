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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.data.BasicObject;

@Controller
public class POIController extends AbstractObjectController {

	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.POIObject")
	public ResponseEntity<List<POIObject>> getAllPOIObject() throws DataException {
		List<POIObject> list = storage.getObjectsByType(POIObject.class);
		return new ResponseEntity<List<POIObject>>(list, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value="/eu.trentorise.smartcampus.dt.model.POIObject/{id}")
	public ResponseEntity<BasicObject> getPOIObjectById(@PathVariable String id) throws DataException {
		try {
			return new ResponseEntity<BasicObject>(storage.getObjectById(id, POIObject.class),HttpStatus.OK);
		} catch (NotFoundException e) {
			logger.error("POIObject with id "+ id+" does not exist");
			e.printStackTrace();
			return new ResponseEntity<BasicObject>(HttpStatus.METHOD_FAILURE);
		}
	}
}
