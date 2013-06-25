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

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommunityData extends DomainCommunityData {
	private static final long serialVersionUID = 5926048335916274968L;

	private int ratingsCount = 0;
	private int followsCount = 0;

	public CommunityData() {
		super();
	}

	public void setFollowing(Map<String, String> following) {
		super.setFollowing(following);
		setFollowsCount(following == null? 0 : following.size());
	}

	public void setRatings(List<Rating> ratings) {
		super.setRatings(ratings);
		setRatingsCount(ratings == null? 0 : ratings.size());
	}
	
	public static void filterUserData(CommunityData data, String userId) {
		if (data == null) return;
		List<Rating> ratings = data.getRatings();
		if (ratings != null && !ratings.isEmpty()) {
			boolean found = false;
			for (Rating r : ratings) {
				if (r.getUserId().equals(userId)) {
					data.setRatings(Collections.singletonList(r));
					found = true;
					break;
				}
			}
			if (!found) {
				data.setRatings(Collections.<Rating>emptyList());
			}
			data.setRatingsCount(ratings.size());
		}
		if (data.getFollowing() != null && ! data.getFollowing().isEmpty()) {
			if (data.getFollowing().containsKey(userId)) {
				data.setFollowing(Collections.singletonMap(userId,data.getFollowing().get(userId)));
			} else {
				data.setFollowing(Collections.<String,String>emptyMap());
			}
			data.setFollowsCount(data.getFollowing().size());
		}
	}

	public static void filterUserData(List<CommunityData> datas, String userId) {
		for (CommunityData data : datas) {
			filterUserData(data, userId);
		}
	}

	public int getRatingsCount() {
		return ratingsCount;
	}

	public void setRatingsCount(int ratingsCount) {
		this.ratingsCount = ratingsCount;
	}

	public int getFollowsCount() {
		return followsCount;
	}

	public void setFollowsCount(int followCount) {
		this.followsCount = followCount;
	}
}
