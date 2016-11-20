package com.helixtech;


import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBSnapshot;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsRequest;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsResult;
import com.amazonaws.services.rds.model.ListTagsForResourceRequest;
import com.amazonaws.services.rds.model.ListTagsForResourceResult;
import com.amazonaws.services.rds.model.Tag;

public class RDSInfo {

	String profileName;
    Regions region;
    String accountId;
	AmazonRDSClient client;
	
	public static void main(String[] args) {
		List<String> snapshots = new RDSInfo(args[0], args[1], args[2]).getRDSSnapshotsByTag("App", "CV");
		printList("RDS snapshots:", snapshots);
		
		List<String> instances = new RDSInfo(args[0], args[1], args[2]).getRDSInstancesByTag("App", "CV");
		printList("RDS instances:", instances);
		
	}
	
	public static void printList(String title, List<String> list) {
		System.out.println(title);
		for (String id : list) {
			System.out.println(id);
		}		
	}

	public RDSInfo(String profileName, String region, String accountId) {
		this.profileName = profileName;
		this.region = Regions.fromName(region);
		this.accountId = accountId;
		ProfileCredentialsProvider credentialProvider = new ProfileCredentialsProvider(profileName);
		client = new AmazonRDSClient(credentialProvider);
	}
	
	public List<String> getRDSnapshots() {
		ArrayList<String> snapshots = new ArrayList<String>();
		DescribeDBSnapshotsRequest request = new DescribeDBSnapshotsRequest()
				.withMaxRecords(100);

		try {
			DescribeDBSnapshotsResult result = null;
			do {
				result = client.describeDBSnapshots(request);
				
				for (DBSnapshot snapshot : result.getDBSnapshots()) {
					snapshots.add(snapshot.getDBSnapshotIdentifier());
				}
				request.setMarker(result.getMarker());
			} while (result.getMarker() != null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return snapshots;
	}
	public List<String> getRDSSnapshotsByTag(String tagKey, String tagValue) {
		ArrayList<String> snapshots = new ArrayList<String>();
		DescribeDBSnapshotsRequest request = new DescribeDBSnapshotsRequest()
				.withMaxRecords(100);
		try {
			DescribeDBSnapshotsResult result = null;
			do {
				result = client.describeDBSnapshots(request);
				
				for (DBSnapshot snapshot : result.getDBSnapshots()) {
					//dig down to the tags
					//System.out.println(formatRDSSnapshotArn(snapshot));
					ListTagsForResourceRequest listTagRequest = (new ListTagsForResourceRequest())
							.withResourceName(formatRDSSnapshotArn(snapshot));
					ListTagsForResourceResult listTagResult = client.listTagsForResource(listTagRequest);					
					
					for( Tag tag : listTagResult.getTagList()) {
						if (tag.getKey().equals(tagKey) &&
								tag.getValue().equals(tagValue))					
							snapshots.add(snapshot.getDBSnapshotIdentifier());
					}
				}
				request.setMarker(result.getMarker());
			} while (result.getMarker() != null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return snapshots;
	}
	
	public List<String> getRDSInstancesByTag(String tagKey, String tagValue) {
		ArrayList<String> instances = new ArrayList<String>();
		DescribeDBInstancesRequest request = new DescribeDBInstancesRequest()
				.withMaxRecords(100);
		try {
			DescribeDBInstancesResult result = null;
			do {
				result = client.describeDBInstances(request);
				
				for (DBInstance instance : result.getDBInstances()) {
					//dig down to the tags
					//System.out.println(formatRDSSnapshotArn(snapshot));
					ListTagsForResourceRequest listTagRequest = (new ListTagsForResourceRequest())
							.withResourceName(formatRDSInstanceArn(instance));
					ListTagsForResourceResult listTagResult = client.listTagsForResource(listTagRequest);					
					
					for( Tag tag : listTagResult.getTagList()) {
						if (tag.getKey().equals(tagKey) &&
								tag.getValue().equals(tagValue))					
							instances.add(instance.getDBInstanceIdentifier());
					}
				}
				request.setMarker(result.getMarker());
			} while (result.getMarker() != null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return instances;
	}

	public String formatRDSSnapshotArn(DBSnapshot snapshot) {
		return "arn:aws:rds:" + region.getName() 
				+ ":" + accountId + ":snapshot:" + snapshot.getDBSnapshotIdentifier();
	}

	public String formatRDSInstanceArn(DBInstance instance) {
		return "arn:aws:rds:" + region.getName() 
				+ ":" + accountId + ":db:" + instance.getDBInstanceIdentifier();
	}
}
