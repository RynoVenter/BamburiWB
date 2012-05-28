package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatPackingRS extends VtiUserExit
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
		boolean hasSt = false;
		boolean hasIc = false;
		
		VtiExitLdbTable bagsLdbTable = getLdbTable("YSWB_BAGS");
		VtiExitLdbTable soLdbTable = getLdbTable("YSWB_SO_HEADER");
		VtiExitLdbTable soILdbTable = getLdbTable("YSWB_SO_ITEMS");
		VtiExitLdbTable packingLdbTable = getLdbTable("YSWB_PACKING");
		
		if(bagsLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_BAGS did not load properly.");
		if(soLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_SO_HEADER did not load properly.");
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
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.SW_OPERATOR, "R"),
								new VtiExitLdbSelectCondition("RETAIL_ORDER", VtiExitLdbSelectCondition.NE_OPERATOR,"X"), 
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
									//new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "NEW"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NS_OPERATOR, "0"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASS"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "SAP ERROR"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "ASSIGNED"),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup soHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderSelConds, true);
			VtiExitLdbTableRow[] soHeaderLdbRows = soLdbTable.getMatchingRows(soHeaderSelCondGrp);
			
			
			
			scrTruckReg.clearPossibleValues();
			
			VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
			if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		
			VtiExitLdbSelectCriterion [] exclMatSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.CS_OPERATOR, "BULK"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup exclMatSelCondGrp = new VtiExitLdbSelectConditionGroup(exclMatSelConds, true);
			VtiExitLdbTableRow[] exclMatLdbRows = configLdbTable.getMatchingRows(exclMatSelCondGrp);
				
			if(exclMatLdbRows.length == 0)
				return new VtiUserExitResult(999, "Bulk material configuration not maintained in YSWB_CONFIG");
			
			boolean blnAdd2List = true;
			
			for(int s = 0;s < soHeaderLdbRows.length;s++)
			{
				/*blnAdd2List = true;
					
				VtiExitLdbSelectCriterion [] soItemSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("VBELN")),
								new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, "DIEN"),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup soItemSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemSelConds, true);
				VtiExitLdbTableRow[] soItemsLdbRows = soILdbTable.getMatchingRows(soItemSelCondGrp);	
								
					for(int si = 0; si < soItemsLdbRows.length;si++)
					{						
						for(int c = 0;c < exclMatLdbRows.length;c++)
						{
							if(soItemsLdbRows[si].getFieldValue("MATNR").equalsIgnoreCase(exclMatLdbRows[c].getFieldValue("KEYVAL1")))
								blnAdd2List = false;
						}
					}
				
				if(blnAdd2List)*/
					scrTruckReg.addPossibleValue(soHeaderLdbRows[s].getFieldValue("TRUCK"));
			}
		}
					
		return new VtiUserExitResult();
	}
}
