package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CalcMaxWeight extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{

		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");
		VtiUserExitScreenField scrMaxWeight = getScreenField("MAXWEIGHT");		
		VtiUserExitScreenField scrGatePass = getScreenField("GATEPASS");	
	
		if(scrNoAxels == null) return new VtiUserExitResult (999,"Failed to initialise NOAXELS.");
		if(scrMaxWeight == null) return new VtiUserExitResult (999,"Failed to initialise MAXWEIGHT.");
		if(scrGatePass == null) return new VtiUserExitResult (999,"Failed to initialise GATEPASS.");
	
		scrGatePass.setFieldValue("");
		
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
						
			VtiExitLdbSelectCriterion [] configSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "TONSPERAXLE"),
				new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, scrNoAxels.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
			VtiExitLdbTableRow[] configLdbRows = confLdbTable.getMatchingRows(configSelCondGrp);
		
			if(configLdbRows.length == 0)
			{
				return new VtiUserExitResult (999,"Maximum axle weight not configured.");		
			}
			
		scrMaxWeight.setLongFieldValue(configLdbRows[0].getLongFieldValue("KEYVAL1"));
		
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
			if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

		sessionHeader.setTitle("WB: Vehicle Arrival - " + currDate + " " + currTime);
		
		return new VtiUserExitResult();
	}
}
