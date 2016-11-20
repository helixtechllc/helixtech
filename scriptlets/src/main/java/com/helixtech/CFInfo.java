package com.helixtech;


import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.*;
import com.amazonaws.services.config.model.ResourceType;

public class CFInfo {
	
	ArrayList<String> ACTIVE_STATUS;
	String profileName;
    Regions region;
    String accountId;
	AmazonCloudFormationClient client;
	
	public static void main(String[] args) {
		List<String> stacks = new CFInfo(args[0], args[1], args[2]).getStackNames();
		printList("All Stacks:", stacks);
		
		stacks = new CFInfo(args[0], args[1], args[2]).getStackNamesByTag("App", "CV");
		printList("CV Stacks:", stacks);
		
		String  prefix = "HelixAcquisitionGateway-" + "1.0".replaceAll("\\.", "-") + "-";
		stacks = new CFInfo(args[0], args[1], args[2]).getStackNotPrefixedNames(prefix);
		printList(prefix + " Stacks:", stacks);
		
	}
	
	public static void printList(String title, List<String> list) {
		System.out.println(title);
		for (String id : list) {
			System.out.println(id);
		}		
	}

	public CFInfo(String profileName, String region, String accountId) {
		//The 'ACTIVE' statuses
		ACTIVE_STATUS = new ArrayList<String>();
		ACTIVE_STATUS.add(StackStatus.CREATE_COMPLETE.name());
		ACTIVE_STATUS.add(StackStatus.CREATE_FAILED.name());
		ACTIVE_STATUS.add(StackStatus.ROLLBACK_COMPLETE.name());
		ACTIVE_STATUS.add(StackStatus.ROLLBACK_FAILED.name());
		ACTIVE_STATUS.add(StackStatus.UPDATE_COMPLETE.name());
		ACTIVE_STATUS.add(StackStatus.UPDATE_ROLLBACK_COMPLETE.name());		
		ACTIVE_STATUS.add(StackStatus.UPDATE_ROLLBACK_FAILED.name());	
		ACTIVE_STATUS.add(StackStatus.DELETE_FAILED.name());
		this.profileName = profileName;
		this.region = Regions.fromName(region);
		this.accountId = accountId;
		ProfileCredentialsProvider credentialProvider = new ProfileCredentialsProvider(profileName);
		client = new AmazonCloudFormationClient(credentialProvider);
	}
	
  public List<String> getResourceByStack(String prefix, String resourceType) {

    ArrayList<String> resources = new ArrayList<String>();
    ListStacksRequest request = new ListStacksRequest().withStackStatusFilters(ACTIVE_STATUS);
    
    try {
      ListStacksResult result = null;
      do {
        result = client.listStacks(request);
        ArrayList<String> stackResults = new ArrayList<String>();
        for (StackSummary stack : result.getStackSummaries()) {
          if (stack.getStackName().startsWith(prefix)) {  
            //stacks.add(stackName);
              
            System.out.println(stack.getStackName());
            ListStackResourcesRequest resultsResources = new ListStackResourcesRequest().withStackName(stack.getStackName());
            ListStackResourcesResult lsrr;// = resultsResources
            lsrr = client.listStackResources(resultsResources);
            List<StackResourceSummary> srsList = lsrr.getStackResourceSummaries();
            for (StackResourceSummary srs : srsList) {
                if(srs.getResourceType().equals(resourceType)) {
                    resources.add(srs.getPhysicalResourceId());
                }
            }
          }
        }
        request.setNextToken(result.getNextToken());
      } while (result.getNextToken() != null);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return resources;
  }
  
    /**
   * For Chris so he can update the list to have the dropdown populated.
   * Use the full cf stack name to grab version number.
   * @param prefix
   * @return
   */
  public List<String> getStackOutputsByPrefix(String prefix, String outputToGrab) {
      ArrayList<String> stacksOutputList = new ArrayList<String>();
      ListStacksRequest request = new ListStacksRequest().withStackStatusFilters(ACTIVE_STATUS);
      
      try {
          ListStacksResult result = null;
          do {
              result = client.listStacks(request);
              for (StackSummary stack : result.getStackSummaries()) {
                  String stackName = stack.getStackName();
                  
                  if (stackName.startsWith(prefix)) { 
                      //System.out.println(stackName);
                      DescribeStacksRequest stackRequest = new DescribeStacksRequest().withStackName(stackName);
                      DescribeStacksResult stackResult = client.describeStacks(stackRequest);
                      Stack outStack = stackResult.getStacks().get(0);
                      List<Output> stackOutput = outStack.getOutputs();
                      for(Output output: stackOutput) {
                          if(output.getOutputKey().equals(outputToGrab)) {
                              //System.out.println(output.getOutputKey() + " " + output.getOutputValue());
                              stacksOutputList.add(output.getOutputValue());
                          }
                      }
                      
                      //stacks.add(stackName.substring(prefix.length()));
                      
                  }
                      
              }
              request.setNextToken(result.getNextToken());
          } while (result.getNextToken() != null);

      } catch (Exception e) {
          e.printStackTrace();
      }
      return stacksOutputList;
  }
  
	public List<String> getStackNotPrefixedNames(String prefix) {
		ArrayList<String> stacks = new ArrayList<String>();
		ListStacksRequest request = new ListStacksRequest().withStackStatusFilters(ACTIVE_STATUS);
		
		try {
			ListStacksResult result = null;
			do {
				result = client.listStacks(request);
				
				for (StackSummary stack : result.getStackSummaries()) {
					String stackName = stack.getStackName();
					if (stackName.startsWith(prefix)) {	
						stacks.add(stackName.substring(prefix.length()));
					}
						
				}
				request.setNextToken(result.getNextToken());
			} while (result.getNextToken() != null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return stacks;
	}
	public List<String> getStackNames() {
		ArrayList<String> stacks = new ArrayList<String>();
		ListStacksRequest request = new ListStacksRequest().withStackStatusFilters(ACTIVE_STATUS);
		
		try {
			ListStacksResult result = null;
			do {
				result = client.listStacks(request);
				
				for (StackSummary stack : result.getStackSummaries()) {
					stacks.add(stack.getStackName());
				}
				request.setNextToken(result.getNextToken());
			} while (result.getNextToken() != null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return stacks;
	}
	public List<String> getStackNamesByTag(String tagKey, String tagValue) {
		ArrayList<String> stacks = new ArrayList<String>();
		DescribeStacksRequest request = new DescribeStacksRequest();
		try {
			DescribeStacksResult result = null;
			do {
				result = client.describeStacks(request);
				
				for (Stack stack : result.getStacks()) {
					if (! ACTIVE_STATUS.contains(stack.getStackStatus())) {
						continue;
					}
					for( Tag tag : stack.getTags()) {
						if (tag.getKey().equals(tagKey) &&
								tag.getValue().equals(tagValue))					
							stacks.add(stack.getStackName());
					}
				}
				request.setNextToken(result.getNextToken());
			} while (result.getNextToken() != null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return stacks;
	}
  
  public List<Output> getStackOutputs(String stackName) {
    List<Output> stackOutput = new ArrayList<Output>();
    DescribeStacksRequest request = new DescribeStacksRequest().withStackName(stackName);
    try {
      DescribeStacksResult result = null;
      result = client.describeStacks(request);
      //Should only return one stack
      Stack stack = result.getStacks().get(0);
      stackOutput = stack.getOutputs();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return stackOutput;
  }
	
}