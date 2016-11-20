package com.helixtech;


import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class MongoInfo {

	String profileName;
	Regions region;
	String accountId;
	AmazonS3Client client;

	public static void main(String[] args) {
		MongoInfo info = new MongoInfo(args[0], args[1], args[2]);
		List<String> list = info.getMongoBackups("helixtech-development-east-backups", "mongodb", 
				"CMD", "Apache22-JBoss52-Mongo3-SOLRlw123");
		printList("Mongo Backups:", list);
	}

	public static void printList(String title, List<String> list) {
		System.out.println(title);
		for (String elem : list) {
			System.out.println(elem);
		}
	}

	public MongoInfo(String profileName, String region, String accountId) {
		this.profileName = profileName;
		this.region = Regions.fromName(region);
		this.accountId = accountId;
		ProfileCredentialsProvider credentialProvider = new ProfileCredentialsProvider(
				profileName);
		client = new AmazonS3Client(credentialProvider);
	}

	public List<String> getMongoBackups(String bucket, String component, String app, String stack) {
		List<String> list = new ArrayList<String>();

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucket).withPrefix(
						component + "/" + app + "/" + stack + "/");
		System.out.println(component + "/" + app + "/" + stack + "/");
		ObjectListing objectListing;
		do {
			objectListing = client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing
					.getObjectSummaries()) {
				String key = objectSummary.getKey();
				if (! key.endsWith("/")) {
					list.add(objectSummary.getKey());
				}
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());
		return list;
		
	}

}
