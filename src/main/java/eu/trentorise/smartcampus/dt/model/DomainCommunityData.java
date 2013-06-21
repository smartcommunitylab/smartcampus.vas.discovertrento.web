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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.trentorise.smartcampus.common.Concept;

public class DomainCommunityData implements Serializable {
	private static final long serialVersionUID = 4477053879449012696L;
	
	protected List<Concept> tags;
	protected String notes;
	protected int averageRating;
	protected List<Rating> ratings;
	protected Map<String, String> following = new HashMap<String, String>();
	
	public DomainCommunityData() {
		super();
	}

	public Map<String, String> getFollowing() {
		return following;
	}

	public void setFollowing(Map<String, String> following) {
		this.following = following;
	}

	public List<Concept> getTags() {
		return tags;
	}

	public void setTags(List<Concept> tags) {
		this.tags = tags;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(int averageRating) {
		this.averageRating = averageRating;
	}

	public List<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(List<Rating> ratings) {
		this.ratings = ratings;
	}
	
}
