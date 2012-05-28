package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class QueueDisplayFormat extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{			
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		
		if(confLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_CONFIG failed to load.");
		
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "QUEUE"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
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
			String tField = "TYPE" + (i+1);
			VtiUserExitScreenField scrFieldQ = getScreenField(qField);
			VtiUserExitScreenField scrFieldT = getScreenField(tField);
			
			if(scrFieldQ == null)
				break;
			
			scrFieldQ.setFieldValue(configLdbRows[i].getFieldValue("KEYVAL1"));
			scrFieldT.setFieldValue(configLdbRows[i].getFieldValue("KEYVAL3"));
		}
		

		return new VtiUserExitResult();
	}
}
