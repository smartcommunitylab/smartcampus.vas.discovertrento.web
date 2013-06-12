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
package eu.trentorise.smartcampus.dt.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import eu.trentorise.smartcampus.presentation.data.BasicObject;

public class BaseDTObject extends BasicObject {
	private static final long serialVersionUID = 3589900794339644582L;
	// common fields
	private String domainType;
	private String domainId;
	private String description = null;
	private String title = null;
	private String source = null; // service 'source' of the object

	// semantic entity
	private Long entityId = null;
	
	// only for user-created objects
	private String creatorId = null;
	private String creatorName = null;

	// community data
	private CommunityData communityData = null;
	
	// categorization
	private String type = null;
	private boolean typeUserDefined = false; // category set by users

	// common data
	private double[] location;
	private Long fromTime;
	private Long toTime;
	private String timing;

	private Map<String,Object> customData;
	
	public BaseDTObject() {
		super();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}

	public Long getFromTime() {
		return fromTime;
	}

	public void setFromTime(Long fromTime) {
		this.fromTime = fromTime;
	}

	public Long getToTime() {
		return toTime;
	}

	public void setToTime(Long toTime) {
		this.toTime = toTime;
	}

	public String getDomainType() {
		return domainType;
	}

	public void setDomainType(String domainType) {
		this.domainType = domainType;
	} 

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public boolean isTypeUserDefined() {
		return typeUserDefined;
	}

	public void setTypeUserDefined(boolean typeUserDefined) {
		this.typeUserDefined = typeUserDefined;
	}

	public CommunityData getCommunityData() {
		return communityData;
	}

	public void setCommunityData(CommunityData communityData) {
		this.communityData = communityData;
	}

	public Map<String, Object> getCustomData() {
		return customData;
	}

	public void setCustomData(Map<String, Object> customData) {
		this.customData = customData;
	}

	public String getTiming() {
		return timing;
	}

	public void setTiming(String timing) {
		this.timing = timing;
	}

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		String s = 
				"[{\"location\": null," +
				"\"type\": \"culture\", \"description\": \"Posti liberi al 2012_07_24 05.45.00 : 84\"," +
				"\"source\": \"primiallaprima\"," +
				"\"title\": \"ALDO, GIOVANNI e GIACOMO\"," +
				"\"ratings\": [],\"averageRating\": 10, " +
				"\"textTags\": []," +
				"\"conceptIds\": []," +
				"\"domainType\": null," +
				"\"id\": \"500f051cc7396d21f239d4ed\"," +
				"\"version\": 102, " +
				"\"user\": null," +
				"\"updateTime\": -1" +
				"}]";
		List<BaseDTObject> list = new ObjectMapper().readValue(s, new TypeReference<List<BaseDTObject>>() { });
		System.err.println(list);
	}

	/**
	 * @param event
	 * @param userId
	 */
	public void filterUserData(String userId) {
		CommunityData.filterUserData(communityData, userId);
	}


}
