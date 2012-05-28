package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatDeregister extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrRefNo = getScreenField("VTI");
		VtiUserExitScreenField scrRegno = getScreenField("REGNO");
		VtiUserExitScreenField scrDereg = getScreenField("BTN_DEREG");
		VtiUserExitScreenField scrOrdNum = getScreenField("ORD_NUM");
		
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		
		VtiExitLdbTable soHLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		if (soHLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		
		scrDereg.setHiddenFlag(true);
		
		VtiExitLdbSelectCriterion [] statHistSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
						new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statHistSelCondGrp = new VtiExitLdbSelectConditionGroup(statHistSelConds, true);
		VtiExitLdbTableRow[] statusWLdbRows = statusLdbTable.getMatchingRows(statHistSelCondGrp);
						
		if(statusWLdbRows.length > 0)
			if(statusWLdbRows[0].getFieldValue("WGH_STATUS").equalsIgnoreCase("REJECTED"))
				scrDereg.setHiddenFlag(false);
		
		VtiExitLdbSelectCriterion [] soHeadSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
						new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soHeadSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeadSelConds, true);
		VtiExitLdbTableRow[] soHLdbRows = soHLdbTable.getMatchingRows(soHeadSelCondGrp);
						
		if(soHLdbRows.length > 0)
			if(soHLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("REJECTED"))
				scrDereg.setHiddenFlag(false);
				
		return new VtiUserExitResult();
	}
}
