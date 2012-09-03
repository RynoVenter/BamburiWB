package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatPacking extends VtiUserExit
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
		VtiExitLdbTable stoILdbTable = getLdbTable("YSWB_PO_ITEMS");
		VtiExitLdbTable icILdbTable = getLdbTable("YSWB_IC_ITEMS");
		VtiExitLdbTable statusLdbTable = getLdbTable("YSWB_STATUS");
		VtiExitLdbTable packingLdbTable = getLdbTable("YSWB_PACKING");
		VtiExitLdbTable wbLdbTable = getLdbTable("YSWB_WB");
		
		if(bagsLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_BAGS did not load properly.");
		if(soLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_SO_HEADER did not load properly.");
		if(statusLdbTable == null) return new VtiUserExitResult(999,"Local database table YSWB_STATUS did not load properly.");
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
		
		VtiExitLdbOrderSpecification [] orderBy = 
				{
					new VtiExitLdbOrderSpecification("VTIREF",true),
				};
		
		if(scrTruckReg.getFieldValue().length() == 0)
		{
			VtiExitLdbSelectCriterion [] soHeaderSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.NS_OPERATOR, "R"),
								//new VtiExitLdbSelectCondition("RETAIL_ORDER", VtiExitLdbSelectCondition.NE_OPERATOR,"X"), 
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
			
			VtiExitLdbSelectCriterion [] statusSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
							new VtiExitLdbSelectCondition("DOCTYPE", VtiExitLdbSelectCondition.NE_OPERATOR, "NB"),
							new VtiExitLdbSelectCondition("DOCTYPE", VtiExitLdbSelectCondition.NE_OPERATOR, "ZNB"),
							new VtiExitLdbSelectCondition("DOCTYPE", VtiExitLdbSelectCondition.NE_OPERATOR, "ZRO"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
							//new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "NEW"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NS_OPERATOR, "0"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASS"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "ASSIGNED"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "SAP ERROR"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
			VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
			
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
				if(soHeaderLdbRows[s].getFieldValue("VBELN").length() > 0)
				{
					VtiExitLdbSelectCriterion [] soIPSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("VBELN")),
									new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR,  "DIEN"),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
        
					VtiExitLdbSelectConditionGroup soIPSelCondGrp = new VtiExitLdbSelectConditionGroup(soIPSelConds, true);
					VtiExitLdbTableRow[] soIPLdbRows = soILdbTable.getMatchingRows(soIPSelCondGrp);
					
					Log.trace(0, "SI length + " + soIPLdbRows.length + " for " +  soHeaderLdbRows[s].getFieldValue("VBELN") + " on loop " + s);
			
					//only add if wb recs is more than the pack recs
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
				
					VtiExitLdbSelectCriterion [] wbSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("TRUCK")),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("VBELN")),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

					};
        
					VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
					VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp, orderBy);
							
							
					blnAdd2List = true;
					if(	wbLdbRows.length > 0)
					{
						Log.trace(0,"Packing format result condition 1 wb length " + wbLdbRows.length + " packing length " +  (packingLdbRows.length + "/" + soIPLdbRows.length ) + " truck " + soHeaderLdbRows[s].getFieldValue("TRUCK"));
						Log.trace(0,"Packing format result condition 2 status " + wbLdbRows[wbLdbRows.length -1].getFieldValue("STATUS"));
						Log.trace(0,"Order " + soHeaderLdbRows[s].getFieldValue("VBELN"));

						
						if(wbLdbRows.length > (packingLdbRows.length / soIPLdbRows.length ) || 
						   (wbLdbRows[wbLdbRows.length -1].getFieldValue("STATUS").equalsIgnoreCase("REJECTED") && wbLdbRows.length >= (packingLdbRows.length / soIPLdbRows.length )))
						{
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
				
							if(blnAdd2List)
								scrTruckReg.addPossibleValue(soHeaderLdbRows[s].getFieldValue("TRUCK"));
						}
					}
				}
			}
	//non sales order		
			for(int r = 0;r < statusLdbRows.length;r++)
			{
				//only add if wb recs is more than the pack recs
				VtiExitLdbSelectCriterion [] packingSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("TRUCKREG")),
								new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("EBELN")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
        
				VtiExitLdbSelectConditionGroup packingSelCondGrp = new VtiExitLdbSelectConditionGroup(packingSelConds, true);
				VtiExitLdbTableRow[] packingLdbRows = packingLdbTable.getMatchingRows(packingSelCondGrp);
				
				VtiExitLdbSelectCriterion [] wbSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("TRUCKREG")),
								new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("EBELN")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
        
				VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
				VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp, orderBy);
						
						
				blnAdd2List = true;
				//check if stoc trans
				if(	wbLdbRows.length > 0 && statusLdbRows[r].getFieldValue("STOCKTRNF").length() > 0)
				{
					
					Log.trace(0,"Packing format result condition 1 wb length " + wbLdbRows.length + " packing length " +  packingLdbRows.length + " truck " + statusLdbRows[r].getFieldValue("TRUCKREG"));
					Log.trace(0,"Packing format result condition 2 status " + wbLdbRows[wbLdbRows.length -1].getFieldValue("STATUS"));
					Log.trace(0,"Order " + statusLdbRows[r].getFieldValue("STOCKTRNF"));
					
					if(wbLdbRows.length > packingLdbRows.length || 
					   (wbLdbRows[wbLdbRows.length -1].getFieldValue("STATUS").equalsIgnoreCase("REJECTED") && wbLdbRows.length >= packingLdbRows.length))
					{
						VtiExitLdbSelectCriterion [] stoItemSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("STOCKTRNF")),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
        
						VtiExitLdbSelectConditionGroup stoItemSelCondGrp = new VtiExitLdbSelectConditionGroup(stoItemSelConds, true);
						VtiExitLdbTableRow[] stoItemsLdbRows = stoILdbTable.getMatchingRows(stoItemSelCondGrp);	
				
						Log.trace(0, "STO record count = " + stoItemsLdbRows.length + " for " + statusLdbRows[r].getFieldValue("STOCKTRNF"));
						
						if(stoItemsLdbRows.length > 0)
						{
							for(int sti = 0; sti < stoItemsLdbRows.length;sti++)
							{						
								for(int c = 0;c < exclMatLdbRows.length;c++)
								{
									if(stoItemsLdbRows[sti].getFieldValue("MATNR").equalsIgnoreCase(exclMatLdbRows[c].getFieldValue("KEYVAL1")))
										blnAdd2List = false;
									
								}
							}
						}
						
						if(blnAdd2List)
							scrTruckReg.addPossibleValue(statusLdbRows[r].getFieldValue("TRUCKREG"));
					}
				}
				
				blnAdd2List = true;
				
				Log.trace(0,"Add to list? true");
				
				//only add if wb recs is more than the pack recs
				VtiExitLdbSelectCriterion [] packingICSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("TRUCKREG")),
								new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("DELIVDOC")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
        
				VtiExitLdbSelectConditionGroup packingICSelCondGrp = new VtiExitLdbSelectConditionGroup(packingICSelConds, true);
				VtiExitLdbTableRow[] packingICLdbRows = packingLdbTable.getMatchingRows(packingICSelCondGrp);
				
				VtiExitLdbSelectCriterion [] wbICSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("TRUCKREG")),
								new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("DELIVDOC")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
        
				VtiExitLdbSelectConditionGroup wbICSelCondGrp = new VtiExitLdbSelectConditionGroup(wbICSelConds, true);
				VtiExitLdbTableRow[] wbICLdbRows = wbLdbTable.getMatchingRows(wbICSelCondGrp, orderBy);
				Log.trace(0,"Checking IC");					
				if(	wbICLdbRows.length > 0 && statusLdbRows[r].getFieldValue("DELIVDOC").length() > 0)
				{
					
					Log.trace(0,"IC Packing format result condition 1 wb length " + wbLdbRows.length + " packing length " +  packingLdbRows.length + " truck " + statusLdbRows[r].getFieldValue("TRUCKREG"));
					Log.trace(0,"IC Packing format result condition 2 status " + wbLdbRows[wbLdbRows.length -1].getFieldValue("STATUS"));
					Log.trace(0,"IC Order " + statusLdbRows[r].getFieldValue("DELIVDOC"));
					
					if(wbICLdbRows.length > packingICLdbRows.length || 
					   (wbICLdbRows[wbICLdbRows.length -1].getFieldValue("STATUS").equalsIgnoreCase("REJECTED") && wbICLdbRows.length >= packingICLdbRows.length))
					{
						
						Log.trace(0, "Checking if bulk ic.");
						VtiExitLdbSelectCriterion [] icItemSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, statusLdbRows[r].getFieldValue("DELIVDOC")),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
        
						VtiExitLdbSelectConditionGroup icItemSelCondGrp = new VtiExitLdbSelectConditionGroup(icItemSelConds, true);
						VtiExitLdbTableRow[] icItemsLdbRows = icILdbTable.getMatchingRows(icItemSelCondGrp);	
					
						
						if(icItemsLdbRows.length > 0)
						{
							for(int ic = 0; ic < icItemsLdbRows.length;ic++)
							{						
								for(int c = 0;c < exclMatLdbRows.length;c++)
								{
									if(icItemsLdbRows[ic].getFieldValue("MATNR").equalsIgnoreCase(exclMatLdbRows[c].getFieldValue("KEYVAL1")))
									{Log.trace(0,"Add to list? false");
										blnAdd2List = false;
									}
								}
							}
						}
							
						if(blnAdd2List)
							scrTruckReg.addPossibleValue(statusLdbRows[r].getFieldValue("TRUCKREG"));
					}
				}
			}
		}
					
		return new VtiUserExitResult();
	}
}
