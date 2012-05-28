package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CalcSoBags extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		int bagsUoM = 0;
		long orderTotal = 0 ;
		VtiUserExitScreenField scrWFSalesOrd = getScreenField("VBELN");

		VtiExitLdbTable soItemsLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		if (soItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");
		
		VtiExitLdbTable soHeaderLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		if (soHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");

			
		VtiExitLdbTable configLdb = getLocalDatabaseTable("YSWB_CONFIG");
		if (configLdb == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");

		
		VtiExitLdbSelectCriterion [] soItemsSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, "DIEN"),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soItemsSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsSelConds, true);
		VtiExitLdbTableRow[] soItemsLdbRows = soItemsLdbTable.getMatchingRows(soItemsSelCondGrp);
		
		if(soItemsLdbRows.length == 0)
		{
			return new VtiUserExitResult(000, "Sales Order not found, bags not calculated.");
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
		
		
		for(int i = 0;i < soItemsLdbRows.length; i++)
		{
			if(soItemsLdbRows[i].getFieldValue("MEINS").equalsIgnoreCase("TO"))
			{
				soItemsLdbRows[i].setIntegerFieldValue("BAGS",soItemsLdbRows[i].getIntegerFieldValue("LSMENGE") * 1000 / bagsUoM);
			
				try
				{
					soItemsLdbTable.saveRow(soItemsLdbRows[i]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save dispatch data to the Packing table.",ee);
					return new VtiUserExitResult(999,"Unable to Save sales order items.");
				}
			}
			
			if(soItemsLdbRows[i].getFieldValue("MTART").equalsIgnoreCase("FERT"))
				orderTotal = orderTotal + soItemsLdbRows[i].getLongFieldValue("LSMENGE");
			
		}
		
		VtiExitLdbSelectCriterion [] soHeaderSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
		};
      
		VtiExitLdbSelectConditionGroup soHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderSelConds, true);
		VtiExitLdbTableRow[] soHeaderLdbRows = soHeaderLdbTable.getMatchingRows(soHeaderSelCondGrp);
		
		if(soHeaderLdbRows.length == 0)
		{
			return new VtiUserExitResult(000, "Sales Order not validated.");
		}
		
		soHeaderLdbRows[0].setFieldValue("LSMENGE",orderTotal);
		soHeaderLdbRows[0].setFieldValue("MEINS","TO");
		
		try
		{
			soHeaderLdbTable.saveRow(soHeaderLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to Save dispatch data to the Packing table.",ee);
			return new VtiUserExitResult(999,"Unable to Save Sales order header.");
		}

		return new VtiUserExitResult(000, 1,"Offline order ," + scrWFSalesOrd.getFieldValue() + " , created successfully.");
	}
}
