package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatLoadingRSO extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
	
		VtiUserExitScreenField scrTruckReg = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrRefDoc = getScreenField("REFDOC");
		
		if(scrTruckReg == null) return new VtiUserExitResult(999,"Screen field TRUCKREG not loaded.");
		if(scrRefDoc == null) return new VtiUserExitResult(999,"Screen field REFDOC not loaded.");

		if(scrRefDoc.getFieldValue().length() == 0)
			scrTruckReg.setFieldValue("");
		
		VtiUserExitScreenTable items = getScreenTable("TB_ITEMS");
		
		if(items == null) return new VtiUserExitResult(999,"Screen table TB_ITEMS not loaded.");
		
		int tableRows = items.getRowCount();
		boolean hasSo = false;

		
		Date currNow = new Date();
		String currDate = DateFormatter.format("yyyyMMdd", currNow);
		String currTime = DateFormatter.format("HHmmss", currNow);
		String stampNow = currDate + currTime;
		
		Long longStampNow = new Long(stampNow);
		long timeStampNow = longStampNow.longValue();
		long timeStampMin2d = timeStampNow - 2000000;
		String timeStampMin2Days = Long.toString(timeStampMin2d);
		String docType = null;
		String packDocType = null;
		
		VtiExitLdbTable bagsLdbTable = getLdbTable("YSWB_BAGS");
		VtiExitLdbTable soLdbTable = getLdbTable("YSWB_SO_HEADER");
		VtiExitLdbTable loadingLdbTable = getLdbTable("YSWB_LOADING");
		VtiExitLdbTable packingLdbTable = getLdbTable("YSWB_PACKING");
		
		VtiExitLdbTable poItemsLdb = getLdbTable("YSWB_PO_ITEMS");
		VtiExitLdbTable icItemsLdb = getLdbTable("YSWB_IC_ITEMS");
		
		if(bagsLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_BAGS did not load properly.");
		if(soLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_SO_HEADER did not load properly.");
		if(loadingLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_LOADING did not load properly.");
		if(packingLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_PACKING did not load properly.");
			
		VtiExitLdbSelectCriterion [] itemsSelConds = 
		{
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
		};
        
		VtiExitLdbSelectConditionGroup itemsSelCondGrp = new VtiExitLdbSelectConditionGroup(itemsSelConds, true);
		VtiExitLdbTableRow[] itemsLdbRows = bagsLdbTable.getMatchingRows(itemsSelCondGrp);

		if(itemsLdbRows.length == 0)
			return new VtiUserExitResult(999,"Bags not maintained for this server.");

		for(int i = 0;i<tableRows;i++)
		{
			VtiUserExitScreenTableRow currItem = items.getRow(i);
			
			currItem.clearPossibleValues("BAGDESC");
			
			for(int r = 0;r < itemsLdbRows.length;r++)
			{
				currItem.addPossibleValue("BAGDESC",itemsLdbRows[r].getFieldValue("MATNR") + ":" + itemsLdbRows[r].getFieldValue("MAKTX"));
			}
		}
		
		if(scrTruckReg.getFieldValue().length() == 0)
		{
			VtiExitLdbSelectCriterion [] soHeaderSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "ASSIGNED"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
							//new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "NEW"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NS_OPERATOR, "0"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASS"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "SAP ERROR"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup soHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderSelConds, true);
			VtiExitLdbTableRow[] soHeaderLdbRows = soLdbTable.getMatchingRows(soHeaderSelCondGrp);
						
			scrTruckReg.clearPossibleValues();
			for(int s = 0;s<soHeaderLdbRows.length;s++)
			{
				//Only add if the loading count is less than the packing count.
				VtiExitLdbSelectCriterion [] packingSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("TRUCK")),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("VBELN")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
        
				VtiExitLdbSelectConditionGroup packingSelCondGrp = new VtiExitLdbSelectConditionGroup(packingSelConds, true);
				VtiExitLdbTableRow[] packingLdbRows = packingLdbTable.getMatchingRows(packingSelCondGrp);
				
				VtiExitLdbSelectCriterion [] loadSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("TRUCK")),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("VBELN")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
        
				VtiExitLdbSelectConditionGroup loadSelCondGrp = new VtiExitLdbSelectConditionGroup(loadSelConds, true);
				VtiExitLdbTableRow[] loadLdbRows = loadingLdbTable.getMatchingRows(loadSelCondGrp);
			
			
				if(loadLdbRows.length < packingLdbRows.length)
					scrTruckReg.addPossibleValue(soHeaderLdbRows[s].getFieldValue("TRUCK"));
			}
		
				
		}
					
		return new VtiUserExitResult();
	}
}
