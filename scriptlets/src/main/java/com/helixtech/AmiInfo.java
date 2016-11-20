package com.helixtech;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Filter;
//import com.amazonaws.services.ec2.model.Instance;
//import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.AmazonEC2Client;

public class AmiInfo {
    
    String profileName;
    Regions region;
    String accountId;
    AmazonEC2Client instanceClient;
    
    public static void main(String[] args) {
        List<String> stacks;// = new InstanceInfo(args[0], args[1], args[2]).getStackNames();
        System.out.println("START Instance");
        HashMap<String, String> hm = new HashMap<String, String>();
        //add key-value pair to hashmap
        //hm.put("tag:Author", "Sean Riggs");
        //hm.put("tag:CostControl", "IO-SandBox");
        //hm.put("tag:PlatformMode", "Mode 1");
        hm.put("tag:Creator", "Samal Dimdung");
        //hm.put("image-id", "SOA22D-IMAGE-20DEC2015SOA22D-20DEC2015");
        hm.put("is-public", "false");
        //hm.put("", "");

        List<Image> images = new AmiInfo(args[0], args[1], args[2]).getImageByTags(hm);
        System.out.println(images.size());
        for (Image img : images) {

            System.out.print(img.getImageId() + " --- " + img.getName() + " --- ");
            for(Tag tag : img.getTags()) {
                //if(tag.getKey().equals("Name")) {
                //    System.out.println(tag.getValue());
                //}
            }
        }
        System.out.println("DONE");
    }
    
    //See https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-instances.html
    //For example key value pairs
    public List<Image> getImageByTags(Map<String, String> tags) {
        
        DescribeImagesRequest request = new DescribeImagesRequest();

        Collection<Filter> filterCol = new ArrayList<Filter>();
        for(Map.Entry<String, String>t : tags.entrySet()) {
            List<String> values = new ArrayList<String>();
            if(t.getValue().length() > 0) {
                values.add(t.getValue());
                filterCol.add(new Filter(t.getKey(), values));
            }
        }

        DescribeImagesResult instanceResult = instanceClient.describeImages(request.withFilters(filterCol));

        List<Image> reservations = instanceResult.getImages();
        return reservations;
    }
    
    


    public AmiInfo(String profileName, String region, String accountId) {

        this.profileName = profileName;
        this.region = Regions.fromName(region);
        this.accountId = accountId;
        ProfileCredentialsProvider credentialProvider = new ProfileCredentialsProvider(profileName);

        instanceClient = new AmazonEC2Client(credentialProvider);
        
    }   
}