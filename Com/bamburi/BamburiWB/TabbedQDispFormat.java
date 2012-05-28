package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class TabbedQDispFormat extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField btnP1 = getScreenField("BT_P1");
		VtiUserExitScreenField btnP2 = getScreenField("BT_P2");
		VtiUserExitScreenField btnP3 = getScreenField("BT_P3");
		VtiUserExitScreenField btnP4 = getScreenField("BT_P4");
		VtiUserExitScreenField btnP5 = getScreenField("BT_P5");
		VtiUserExitScreenField btnP6 = getScreenField("BT_P6");
		VtiUserExitScreenField btnP7 = getScreenField("BT_P7");
		VtiUserExitScreenField btnP8 = getScreenField("BT_P8");
		VtiUserExitScreenField btnP9 = getScreenField("BT_P9");
		
		if (btnP1 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P1.");
		if (btnP2 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P2.");
		if (btnP3 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P3.");
		if (btnP4 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P4.");
		if (btnP5 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P5.");
		if (btnP6 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P6.");
		if (btnP7 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P7.");
		if (btnP8 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P8.");
		if (btnP9 == null) return new VtiUserExitResult(999, "Unable to initialise field BT_P9.");
		
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "QUEUEDISP"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
		
		VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("KEYVAL2",true),
			};
      
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbTableRow[] configLdbRows = configLdbTable.getMatchingRows(configSelCondGrp, orderBy);

		if(configLdbRows.length == 0)
			return new VtiUserExitResult(999, "Config for queue display not maintained.");
		
		if(configLdbRows.length >= 1)
			btnP1.setFieldValue(configLdbRows[0].getFieldValue("KEYVAL1"));
		
		if(configLdbRows.length >= 2)
			btnP2.setFieldValue(configLdbRows[1].getFieldValue("KEYVAL1"));
		
		if(configLdbRows.length >= 3)
			btnP3.setFieldValue(configLdbRows[2].getFieldValue("KEYVAL1"));
		
		if(configLdbRows.length >= 4)
			btnP4.setFieldValue(configLdbRows[3].getFieldValue("KEYVAL1"));
		
		if(configLdbRows.length >= 5)
			btnP5.setFieldValue(configLdbRows[4].getFieldValue("KEYVAL1"));
		
		if(configLdbRows.length >= 6)
			btnP6.setFieldValue(configLdbRows[5].getFieldValue("KEYVAL1"));
		
		if(configLdbRows.length >= 7)
			btnP7.setFieldValue(configLdbRows[6].getFieldValue("KEYVAL1"));
		
		if(configLdbRows.length >= 8)
			btnP8.setFieldValue(configLdbRows[7].getFieldValue("KEYVAL1"));
		
		if(configLdbRows.length >= 9)
			btnP9.setFieldValue(configLdbRows[8].getFieldValue("KEYVAL1"));
		
		DBCalls qUpdateCall = new DBCalls();
		
		qUpdateCall.ldbRefreshOnly("YSWB_QUEUE", this);
		

		return new VtiUserExitResult();
	}
}
