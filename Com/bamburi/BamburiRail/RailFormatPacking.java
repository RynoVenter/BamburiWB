package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RailFormatPacking extends VtiUserExit
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
		
		VtiExitLdbTable bagsLdbTable = getLdbTable("YSWB_BAGS");
		VtiExitLdbTable packingLdbTable = getLdbTable("YSWB_PACKING");
		
		if(bagsLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_BAGS did not load properly.");
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
						
		return new VtiUserExitResult();
	}
}
