package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatWB extends VtiUserExit
{/*Perform general setup and settings at the startup 
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField versionIDField = getScreenField("CLASSVERSION");
		if (versionIDField == null) return new VtiUserExitResult(999, "Error Retrieving Version Info");

		//Set Class Version ( Hardcoded ) ( Change before every SAP Package role out )
		versionIDField.setFieldValue("Version 5.04");

		
		// Trigger the uploads to SAP, if a connection is available.
		String hostName = getHostInterfaceName();
		
		boolean hostConnected = isHostInterfaceConnected(hostName);
		
		VtiExitLdbTable logonlLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		if (logonlLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		try
		{
			hostConnected = isHostInterfaceConnected(hostName);
			
			if (hostConnected)
			{ 
				VtiExitLdbRequest ldbReqDownloadNewPassword = new VtiExitLdbRequest(logonlLdbTable,VtiExitLdbRequest.REFRESH);
				ldbReqDownloadNewPassword.submit(false);
			}
		}
		catch (VtiExitException ee)
		{
		}
		
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		if(confLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_CONFIG failed to load.");
		
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "QUEUE"),
		};
      
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("KEYVAL1",true),
			};
		VtiExitLdbTableRow configLdbRows[] = confLdbTable.getMatchingRows(configSelCondGrp, orderBy);
		
		if(configLdbRows.length == 0)
			return new VtiUserExitResult(999, "No queue information availible in the config table");
		
		for(int i = 0;i<configLdbRows.length;i++)
		{
			String qField = "Q" + (i+1);
			VtiUserExitScreenField scrFieldQ = getScreenField(qField);
			
			if(scrFieldQ == null)
				break;
			
			scrFieldQ.setFieldValue(configLdbRows[i].getFieldValue("KEYVAL1"));
		}
		return new VtiUserExitResult();
	}
}
