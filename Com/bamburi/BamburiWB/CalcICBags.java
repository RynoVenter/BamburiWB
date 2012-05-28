package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CalcICBags extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		int bagsUoM = 0;
		VtiUserExitScreenField scrICOrd = getScreenField("VBELN");

		VtiExitLdbTable icItemsLdbTable = getLocalDatabaseTable("YSWB_IC_ITEMS");
		if (icItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_ITEMS.");
		
		VtiExitLdbTable configLdb = getLocalDatabaseTable("YSWB_CONFIG");
		if (configLdb == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");

		
		VtiExitLdbSelectCriterion [] icItemsSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrICOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup icItemsSelCondGrp = new VtiExitLdbSelectConditionGroup(icItemsSelConds, true);
		VtiExitLdbTableRow[] icItemsLdbRows = icItemsLdbTable.getMatchingRows(icItemsSelCondGrp);
		
		if(icItemsLdbRows.length == 0)
		{
			return new VtiUserExitResult(000, "Intercompany Order not found, bags not calculated.");
		}
		
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "BAGS_UOM"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbTableRow[] configLdbRows = configLdb.getMatchingRows(configSelCondGrp);
		
		if(configLdbRows.length == 0)
		{
			bagsUoM = 50;
			return new VtiUserExitResult(000,"Bags Unit of Measure details not maintained in Config.");
		}
		else
		{
			bagsUoM = configLdbRows[0].getIntegerFieldValue("KEYVAL1");;
		}
		
		
		for(int i = 0;i < icItemsLdbRows.length; i++)
		{
			if(icItemsLdbRows[i].getFieldValue("MEINS").equalsIgnoreCase("TO"))
			{
				icItemsLdbRows[i].setIntegerFieldValue("BAGS",icItemsLdbRows[i].getIntegerFieldValue("LFIMG") * 1000 / bagsUoM);
			
				try
				{
					icItemsLdbTable.saveRow(icItemsLdbRows[i]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save dispatch data to the Packing table.",ee);
					return new VtiUserExitResult(999,"Unable to Save dispatched data to the Packing table.");
				}
			}
			
		}

		return new VtiUserExitResult(000, 1,"Offline order ," + scrICOrd.getFieldValue() + " , created successfully.");
	}
}
