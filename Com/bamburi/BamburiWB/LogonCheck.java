package com.bamburi.bamburiwb;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class LogonCheck extends VtiUserExit
{/*Determine if a password reset is needed.
	  */
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.
		String usable = "";
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

		//Logon Password dataset
		VtiExitLdbTable logonlLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		if (logonlLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		
		VtiExitLdbSelectCriterion logonSelConds = new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId());
        
		VtiExitLdbSelectConditionGroup logonSelCondGrp = new VtiExitLdbSelectConditionGroup(logonSelConds, true);
		VtiExitLdbTableRow[] logonLdbRows = logonlLdbTable.getMatchingRows(logonSelCondGrp);

		if(logonLdbRows.length == 0)
			return new VtiUserExitResult(999, "Unable to query table YSWB_LOGON.");
		
		usable = logonLdbRows[0].getFieldValue("USEABLE");
		
		if(!usable.equalsIgnoreCase("X"))
		{
			 sessionHeader.setNextFunctionId("YSWB_NEWLOGON");
		}
		else
		{
			VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
				if (confLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		
			VtiExitLdbSelectCriterion [] confSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "APP"),
								new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, sessionHeader.getUserId()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
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
		return new VtiUserExitResult(); 
	}
	
}
