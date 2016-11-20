package com.helixtech;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.AmazonEC2Client;

public class InstanceInfo {
    
    String profileName;
    Regions region;
    String accountId;
    AmazonEC2Client instanceClient;
    
    public static void main(String[] args) {
        List<String> stacks;// = new InstanceInfo(args[0], args[1], args[2]).getStackNames();
        System.out.println("START Instance");
        HashMap<String, String> hm = new HashMap<String, String>();
        //add key-value pair to hashmap
        hm.put("tag:CostControl", "ASAP");
        hm.put("tag:PlatformMode", "Level 1");
        hm.put("instance-id", "");

        List<Reservation> reservations = new InstanceInfo(args[0], args[1], args[2]).getInstanceByTags(hm);
        
        for (Reservation reservation : reservations) {
            List<Instance> instances = reservation.getInstances();
            for (Instance instance : instances) {
                System.out.println(instance.getInstanceId() + " " + instance.getPrivateIpAddress() + " " + instance.getTags());
            }
        }
        System.out.println("DONE");
    }
    
    //See https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-instances.html
    //For example key value pairs
    public List<Reservation> getInstanceByTags(Map<String, String> tags) {
        
        DescribeInstancesRequest request = new DescribeInstancesRequest();

        Collection<Filter> filterCol = new ArrayList<Filter>();
        for(Map.Entry<String, String>t : tags.entrySet()) {
            List<String> values = new ArrayList<String>();
            if(t.getValue().length() > 0) {
                values.add(t.getValue());
                filterCol.add(new Filter(t.getKey(), values));
            }
        }

        DescribeInstancesResult instanceResult = instanceClient.describeInstances(request.withFilters(filterCol));
        
        System.out.println(instanceResult.toString());
        List<Reservation> reservations = instanceResult.getReservations();
        System.out.println(reservations.toString());
        return reservations;
    }
    
    public InstanceInfo(String profileName, String region, String accountId) {

        this.profileName = profileName;
        this.region = Regions.fromName(region);
        this.accountId = accountId;
        ProfileCredentialsProvider credentialProvider = new ProfileCredentialsProvider(profileName);

        instanceClient = new AmazonEC2Client(credentialProvider);
        
    }   
}
