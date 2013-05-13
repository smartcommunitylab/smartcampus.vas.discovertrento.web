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


import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.presentation.common.util.Util;
import eu.trentorise.smartcampus.presentation.data.BasicObject;
import eu.trentorise.smartcampus.presentation.data.SyncData;
import eu.trentorise.smartcampus.presentation.data.SyncDataRequest;
import eu.trentorise.smartcampus.presentation.storage.sync.BasicObjectSyncStorage;

@Controller
public class SyncController extends AbstractObjectController {

	@Autowired
	private BasicObjectSyncStorage storage;

	@RequestMapping(method = RequestMethod.POST, value = "/sync")
	public ResponseEntity<SyncData> synchronize(HttpServletRequest request, @RequestParam long since, @RequestBody Map<String,Object> obj) throws Exception{
		String userId = getUserId(request);
		SyncDataRequest syncReq = Util.convertRequest(obj, since);
		// temporary workaround for older version: do not sync the mobility data.
		if (syncReq.getSyncData().getExclude() == null) {
			syncReq.getSyncData().setExclude(Collections.<String,Object>singletonMap("source", "smartplanner-transitstops"));
		}
		
		SyncData result = storage.getSyncData(syncReq.getSince(), userId, syncReq.getSyncData().getInclude(), syncReq.getSyncData().getExclude());
		filterResult(result, userId);
		storage.cleanSyncData(syncReq.getSyncData(), userId);

		return new ResponseEntity<SyncData>(result,HttpStatus.OK);
	}

	private void filterResult(SyncData result, String userId) {
		if (result.getUpdated() != null) {
			List<BasicObject> list = result.getUpdated().get(EventObject.class.getName());
			if (list != null && !list.isEmpty()) {
				for (Iterator<BasicObject> iterator = list.iterator(); iterator.hasNext();) {
					EventObject event = (EventObject) iterator.next();
					// skip old events where user does not participate
					if (!checkDate(event) &&
						(event.getAttending()==null || !event.getAttending().contains(userId))) 
					{
						iterator.remove();
						continue;
					}
					EventObject.filterUserData((EventObject)event, userId);
				}
			}
			list = result.getUpdated().get(StoryObject.class.getName());
			if (list != null && !list.isEmpty()) {
				for (BasicObject story : list) {
					StoryObject.filterUserData((StoryObject)story, userId);
				}
			}
		}
	}

	private boolean checkDate(EventObject obj) {
		long ref = System.currentTimeMillis()-24*60*60*1000;
		return (obj.getFromTime() > ref) || (obj.getToTime() != null && obj.getToTime() > 0 && obj.getToTime() > ref);
	}
}
