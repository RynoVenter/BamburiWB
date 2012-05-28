package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetStartUpInfo extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if(sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

		VtiUserExitScreenField userIDField = getScreenField("USERID");
		VtiUserExitScreenField versionIDField = getScreenField("CLASSVERSION");
		VtiUserExitScreenField passwIDField = getScreenField("PASSWORD");
		VtiUserExitScreenField newPasswIDField = getScreenField("NEW_PASSWORD");
		VtiUserExitScreenField conFPasswIDField = getScreenField("CONF_PASSWORD");
		VtiUserExitScreenField authLevelField = getScreenField("AUTHLEVEL");
		
		if(userIDField == null) return new VtiUserExitResult(999, "Screen field USERID does not exist");
		if(passwIDField == null) return new VtiUserExitResult(999, "Screen field PASSWORD does not exist");
		if(!sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOGON"))
		{
			if (newPasswIDField == null) return new VtiUserExitResult(999, "Screen field NEW PASSWORD does not exist");
			if (conFPasswIDField == null) return new VtiUserExitResult(999, "Screen field CONFIRM PASSWORD does not exist");
		}
		
		String validate = "";
		String usable = "";
		
		boolean runValidate = false;

		sessionHeader.setUserId(userIDField.getFieldValue());
		
		if(authLevelField != null) 
			if(!authLevelField.getFieldValue().equalsIgnoreCase("ADMIN"))
			{
				Log.warn("Wrongfull access attempted by " + sessionHeader.getUserId());
				return new VtiUserExitResult(999, sessionHeader.getUserId() + " have no Admin rights.");
			}
		//Validate and update password details.
		//Logon Password dataset
		VtiExitLdbTable logonlLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		if (logonlLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		
		VtiExitLdbSelectCriterion [] logonSelConds = 
				{
						new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, userIDField.getFieldValue()),
				};
        
		VtiExitLdbSelectConditionGroup logonSelCondGrp = new VtiExitLdbSelectConditionGroup(logonSelConds, true);
		VtiExitLdbTableRow[] logonLdbRows = logonlLdbTable.getMatchingRows(logonSelCondGrp);

		if(logonLdbRows.length == 0) return new VtiUserExitResult(999, "Unable to query table YSWB_LOGON.");
		
		usable = logonLdbRows[0].getFieldValue("USEABLE");
		validate = logonLdbRows[0].getFieldValue("PASSWORD");

		if(!usable.equalsIgnoreCase("X") && !sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_NEWLOGON"))
			 sessionHeader.setNextFunctionId("YSWB_NEWLOGON");
		else if(passwIDField.getFieldValue().equals(validate)  && sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOGON"))
		{
			VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
				if (confLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		
			VtiExitLdbSelectCriterion [] confSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "APP"),
								new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, sessionHeader.getUserId()),
				};
      
			VtiExitLdbSelectConditionGroup confSelCondGrp = new VtiExitLdbSelectConditionGroup(confSelConds, true);
			VtiExitLdbTableRow[] confLdbRows = confLdbTable.getMatchingRows(confSelCondGrp);
		
			if(confLdbRows.length == 0)
				return new VtiUserExitResult(999,"No profile match this user. Please set this user up to use this application.");
		
			String screen2Load = confLdbRows[0].getFieldValue("KEYVAL1");
			
				try
				{
					sessionHeader.setNextFunctionId(screen2Load);
				}
				catch(NullPointerException ee)
				{
					Log.error("Failed to open " + confLdbRows[0].getFieldValue("KEYVAL1") + " correctly.");
					return new VtiUserExitResult(999,"Failed to open " + confLdbRows[0].getFieldValue("KEYVAL1") + " correctly.");
					
				}
		}
		else if(!passwIDField.getFieldValue().equals(validate)  && sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOGON"))
		{
			return new VtiUserExitResult(999,"Password not valid.");
		}
		else if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_NEWLOGON"))
		{
			if(passwIDField.getFieldValue().equals(validate)  && newPasswIDField.getFieldValue().equals(conFPasswIDField.getFieldValue()))
			{
				VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
					if (confLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		
				VtiExitLdbSelectCriterion [] confSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "APP"),
								new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, sessionHeader.getUserId()),
				};
      
				VtiExitLdbSelectConditionGroup confSelCondGrp = new VtiExitLdbSelectConditionGroup(confSelConds, true);
				VtiExitLdbTableRow[] confLdbRows = confLdbTable.getMatchingRows(confSelCondGrp);
		
				if(confLdbRows.length == 0)
					return new VtiUserExitResult(999,"No profile match this user. Please set this user up to use this application.");
		
				String screen2Load = confLdbRows[0].getFieldValue("KEYVAL1");
			
					try
					{
						sessionHeader.setNextFunctionId(screen2Load);
					}
					catch(NullPointerException ee)
					{
						Log.error("Failed to open " + confLdbRows[0].getFieldValue("KEYVAL1") + " correctly.");
					}
			}
		}

		//Validate old Password and then validate new password, then set and save the new row to the table.
		if(!sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOGON"))
		{
			if(passwIDField.getFieldValue().equals(validate) && !logonLdbRows[0].getFieldValue("USEABLE").equalsIgnoreCase("X"))
			{
				if(newPasswIDField.getFieldValue().equals(conFPasswIDField.getFieldValue()))
				{
					if(newPasswIDField.getFieldValue().equals(validate))
					{
						newPasswIDField.setFieldValue("");
						conFPasswIDField.setFieldValue("");
						return new VtiUserExitResult(999, "Please provide a password not similar to previous passwords.");
					}
					if(newPasswIDField.getFieldValue().length() < 6)
					{
						newPasswIDField.setFieldValue("");
						conFPasswIDField.setFieldValue("");
						return new VtiUserExitResult(999, "The password needs to be at least 6 characters long.");
					}
					
					if(newPasswIDField.getFieldValue().length() > 14)
					{
						newPasswIDField.setFieldValue("");
						conFPasswIDField.setFieldValue("");
						return new VtiUserExitResult(999, "The password needs to be shorter than 15 characters.");
					}
					
					for(int u = 0;u < logonLdbRows.length;u++)
					{
						logonLdbRows[u].setFieldValue("PASSWORD",conFPasswIDField.getFieldValue());
						logonLdbRows[u].setFieldValue("USEABLE","X");
						logonLdbRows[u].setFieldValue("TIMESTAMP","");
					}
					
					try
					{					
					
					for(int s = 0;s < logonLdbRows.length;s++)
					{
						logonlLdbTable.saveRow(logonLdbRows[s]);
					}
							
						// Trigger the uploads to SAP, if a connection is available.
							String hostName = getHostInterfaceName();
							boolean hostConnected = isHostInterfaceConnected(hostName);

							if (hostConnected)
							{ 
								VtiExitLdbRequest ldbReqUploadNewPassword = new VtiExitLdbRequest(logonlLdbTable,VtiExitLdbRequest.UPLOAD);
								ldbReqUploadNewPassword.submit(false);
							}
					}
					catch (VtiExitException ee)
					{
						return new VtiUserExitResult(999, "Failed to update new password.");
					}
				}
			}
			else
			{
				return new VtiUserExitResult(999, "Please try again, your password and details provided was incorrect.");
			}
			
				if(!newPasswIDField.getFieldValue().equals(conFPasswIDField.getFieldValue()))
				{
					newPasswIDField.setFieldValue("");
					conFPasswIDField.setFieldValue("");
					return new VtiUserExitResult(999, "Please ensure that the new password and confirmation password is the same.");
				}

		}

		return new VtiUserExitResult();
	}
}

