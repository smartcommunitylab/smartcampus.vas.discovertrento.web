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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trentorise.smartcampus.ac.provider.AcService;
import eu.trentorise.smartcampus.ac.provider.filters.AcProviderFilter;
import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.data.GeoTimeObjectSyncStorage;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;

public class AbstractObjectController {

	@Autowired
	private AcService acService;

	@Autowired
	protected GeoTimeObjectSyncStorage storage;

	protected Log logger = LogFactory.getLog(this.getClass());

	protected String getUserId(HttpServletRequest request) throws Exception {
		return ""+retrieveUser(request).getId();
	}

	protected User retrieveUser(HttpServletRequest request) throws Exception {
		String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
		return acService.getUserByToken(token);
	}

	protected DomainObject upgradeDO(BaseDTObject obj, DomainEngineClient client) throws Exception {
		if (obj.getDomainId() ==  null) {
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("id", obj.getId());
			List<String> list = client.searchDomainObjects(obj.getDomainType(), params, null);
			if (list != null && !list.isEmpty()) {
				DomainObject dobj = new DomainObject(list.get(0));
				obj.setDomainId(dobj.getId());
				return dobj;
			}
		}
		return null;

	}
}
