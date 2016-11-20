package com.helixtech;

import java.util.*;

public class AmiMgrHelper {
	public static List<String>  getAwsAccounts() {
		String[] accounts = { "asap", "mgmt", "stage", "prod-int", "prod-ext"};
		return Arrays.asList(accounts);

	}
	public static List<String>  getStatus() {
		String[] status = { "alpha", "beta", "released", "retired", "marked_to_delete"};
		return Arrays.asList(status);
	}
	public static String getToken() {
		return "0f2f26c6-fcc7-4dc5-be8e-38767da896f1";
	}

	public static String getGitUrl() {
		return  "git@github.helix.gsa.gov:HelixDevOps";
	}
}