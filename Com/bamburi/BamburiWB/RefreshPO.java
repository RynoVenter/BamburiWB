package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RefreshPO extends VtiUserExit
{/*Refresh the list of PO headers
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		boolean hostConnected = isHostInterfaceConnected(hostName);
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
		VtiUserExitScreenField scrFDate = getScreenField("S_DATE");
		VtiUserExitScreenTable scrTPurchOrd = getScreenTable("TB_SALES");
		VtiUserExitScreenField scrWTransfer = getScreenField("BSART");
				
		if (scrFDate == null) return new VtiUserExitResult(999, "Unable to initialise screen field S_DATE.");
		if (scrTPurchOrd == null) return new VtiUserExitResult(999, "Unable to initialise screen table TB_SALES.");
		if(scrWTransfer == null) return new VtiUserExitResult (999,"Failed to initialise BSART.");
		
		VtiExitLdbTable poHeaderLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable poItemsLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		
		if (poHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		if (poItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_ITEMS.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		
		boolean isTransfer = false;
		
		if(scrWTransfer.getFieldValue().equalsIgnoreCase("UB"))
			isTransfer = true;
		
		DBCalls dbCall = new DBCalls();
		
		try
		{
			if (hostConnected)
			{ 
				dbCall.ldbRefresh("YSWB_PO_HEADER", this);
				dbCall.ldbRefresh("YSWB_PO_ITEMS", this);				
			}
			else
			{
				return new VtiUserExitResult(999,"The refresh is not currently possible.");
			}
		}
		catch ( VtiExitException ee)
		{
			Log.error("Error refreshing the Purchase Order tables.", ee);
			return new VtiUserExitResult(999,"The refresh is not currently possible.");
		}
		
		scrTPurchOrd.clear();

		if(scrFDate.getFieldValue().length() > 0)
		{
				VtiExitLdbSelectCriterion [] poHeaderSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("BEDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFDate.getFieldValue()),
								new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.CS_OPERATOR, scrWTransfer.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup poHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderSelConds, true);
				VtiExitLdbTableRow[] poHeaderLdbRows = poHeaderLdbTable.getMatchingRows(poHeaderSelCondGrp);

				if(poHeaderLdbRows.length == 0)
					return new VtiUserExitResult(999,"No new Purchase Orders.");
				
				
				VtiExitLdbSelectCriterion [] statusSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("BEDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFDate.getFieldValue()),
								new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.CS_OPERATOR, scrWTransfer.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
				VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);

				if(statusLdbRows.length == 0)
					return new VtiUserExitResult(999,"Orders not found in the Status table.");
				
			
				for(int c = 0; c < poHeaderLdbRows.length; c++)
				{
					VtiUserExitScreenTableRow newRow = scrTPurchOrd.getNewRow();
			
					newRow.setFieldValue("DATE",poHeaderLdbRows[c].getFieldValue("BEDAT"));
					newRow.setFieldValue("TRUCK_REG",statusLdbRows[c].getFieldValue("TRUCK"));
					newRow.setFieldValue("EBELN",statusLdbRows[c].getFieldValue("EBELN"));
					newRow.setFieldValue("VENDOR",poHeaderLdbRows[c].getFieldValue("NAME1"));
					newRow.setFieldValue("STATUS",poHeaderLdbRows[c].getFieldValue("STATUS"));
				
					scrTPurchOrd.appendRow(newRow);
				}
		}
		else
		{
				VtiExitLdbSelectCriterion [] poHeaderSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.EQ_OPERATOR, "UB"),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup poHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderSelConds, true);
				VtiExitLdbTableRow[] poHeaderLdbRows = poHeaderLdbTable.getMatchingRows(poHeaderSelCondGrp);

				if(poHeaderLdbRows.length == 0)
					return new VtiUserExitResult(999,"No new Purchase Orders.");
				
				
				VtiExitLdbSelectCriterion [] statusSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.CS_OPERATOR, scrWTransfer.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
				VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);

				if(statusLdbRows.length == 0)
					return new VtiUserExitResult(999,"Orders not found in the Status table.");
			
				for(int c = 0; c < poHeaderLdbRows.length; c++)
				{
					VtiUserExitScreenTableRow newRow = scrTPurchOrd.getNewRow();
			
					newRow.setFieldValue("DATE",poHeaderLdbRows[c].getFieldValue("BEDAT"));
					newRow.setFieldValue("TRUCK_REG",poHeaderLdbRows[c].getFieldValue("TRUCK"));
					newRow.setFieldValue("EBELN",poHeaderLdbRows[c].getFieldValue("EBELN"));
					newRow.setFieldValue("VENDOR",poHeaderLdbRows[c].getFieldValue("NAME1"));
					newRow.setFieldValue("STATUS",poHeaderLdbRows[c].getFieldValue("STATUS"));
			
					scrTPurchOrd.appendRow(newRow);
				}			
		}
		
		return new VtiUserExitResult();
		
	}
}
