package com.helixtech;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.SnapshotState;
import com.amazonaws.services.ec2.model.Tag;

public class SolrInfo {

	String profileName;
    Regions region;
    String accountId;
    AmazonEC2Client client;
	
	public static void main(String[] args) {		       
		Map<String, String> tags = new HashMap<String, String>() {{
		       put("App","testing"); put("AppVersion","1.0");}};		       		       
		SolrInfo info = new SolrInfo(args[0], args[1], args[2]);
		List<String> list = info.getEC2SnapshotIdsByTags(tags);
		printList("EC2 snapshot ids:", list);
		
		list = info.getEC2SnapshotDescriptionsByTags(tags);
		printList("EC2 snapshots Description:", list);

		list = info.getEC2SnapshotIdAndTagsByTags(tags, "Name");
		printList("EC2 snapshots id+name:", list);
		
	}
	
	/*
	
	public static void printList(String title, SolrInfo info) {
		Map<String, String> tags = new HashMap<String, String>() {{
		       put("App","testing"); put("AppVersion","1.0");}};		       		       
		List<String> list = info.getEC2SnapshotsByTag(tags);
		System.out.println(title);
		for (String id : list) {
			System.out.println(id + ": " + info.getEC2SnapshotTagValue(id,  "Name"));
		}		
	}
	*/
	
	public static void printList(String title, List<String> list) {
		System.out.println(title);
		for (String elem : list) {
			System.out.println(elem);
		}		
	}

	public SolrInfo(String profileName, String region, String accountId) {
		this.profileName = profileName;
		this.region = Regions.fromName(region);
		this.accountId = accountId;
		ProfileCredentialsProvider credentialProvider = new ProfileCredentialsProvider(profileName);
		client = new AmazonEC2Client(credentialProvider);
	}
	
	public List<String> getEC2SnapshotIdAndTagsByTags(Map<String, String> tags, String tagKey) {
		List<String> list;
		try {
	        Method method = Snapshot.class.getMethod("getSnapshotId", null);
	        
	        list = getEC2SnapshotsInfoByTags(tags, method);
		}
        catch (NoSuchMethodException e) {
        	list = new ArrayList<String>(0);
		}
		for (int i = 0; i < list.size(); i++) {
			String id = list.get(i);
			list.set(i, id + "-" + getEC2SnapshotTagValue(id, tagKey));
		}
		return list;
	}
	
	public List<String> getEC2SnapshotIdsByTags(Map<String, String> tags) {
		try {
	        Method method = Snapshot.class.getMethod("getSnapshotId", null);
	        
	        return getEC2SnapshotsInfoByTags(tags, method);
		}
        catch (NoSuchMethodException e) {
        	return new ArrayList<String>(0);
		}
	}

	public List<String> getEC2SnapshotDescriptionsByTags(Map<String, String> tags) {
		try {
	        Method method = Snapshot.class.getMethod("getDescription", null);
	        
	        return getEC2SnapshotsInfoByTags(tags, method);
		}
        catch (NoSuchMethodException e) {
        	return new ArrayList<String>(0);
		}
	}
	
	private List<String> getEC2SnapshotsInfoByTags(Map<String, String> tags, Method method) {
		ArrayList<String> snapshots = new ArrayList<String>();
		DescribeSnapshotsRequest request = new DescribeSnapshotsRequest();
		try {
			DescribeSnapshotsResult result = client.describeSnapshots(request);
			
			for (Snapshot snapshot : result.getSnapshots()) {
				if (! SnapshotState.fromValue(snapshot.getState()).equals(SnapshotState.Completed)) {
					continue;
				}
				
				int foundTags = 0;
				for (Map.Entry<String, String> entry : tags.entrySet()) {

				    for (Tag tag: snapshot.getTags()) {
				    	if (tag.getKey().equals(entry.getKey()) 
				    			&& tag.getValue().equals(entry.getValue())) {
				    		foundTags++;
				    	}
				    }
				}
				if (foundTags == tags.size()) {
					snapshots.add((String)method.invoke(snapshot, new Object[0]));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return snapshots;
	}

	public String getEC2SnapshotTagValue(String id, String tagKey) {
		String value = null;
		
		DescribeSnapshotsRequest request = new DescribeSnapshotsRequest().withSnapshotIds(id);
		try {
			DescribeSnapshotsResult result = client.describeSnapshots(request);
			
			for (Snapshot snapshot : result.getSnapshots()) {
			    for (Tag tag: snapshot.getTags()) {
			    	if (tag.getKey().equals(tagKey)) {
			    		value = tag.getValue();
			    		break;
			    	}
			    }
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
}