package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RefreshSO extends VtiUserExit
{
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
		VtiUserExitScreenTable scrTSalesOrd = getScreenTable("TB_SALES");
		if (scrFDate == null) return new VtiUserExitResult(999, "Unable to initialise screen field S_DATE.");
		if (scrTSalesOrd == null) return new VtiUserExitResult(999, "Unable to initialise screen table TB_SALES.");
		
		VtiExitLdbTable soHeaderLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable soItemsLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		if (soHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (soItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");
		
		DBCalls dbCall = new DBCalls();
		
		try
		{
			if (hostConnected)
			{ 
				dbCall.ldbRefresh("YSWB_SO_HEADER", this);
				dbCall.ldbRefresh("YSWB_SO_ITEMS", this);				
			}
			else
			{
				return new VtiUserExitResult(999,"The refresh is not currently possible.");
			}
		}
		catch ( VtiExitException ee)
		{
			Log.error("Error refreshing the Sales Order tables.", ee);
			return new VtiUserExitResult(999,"The refresh is not currently possible.");
		}
		
		scrTSalesOrd.clear();
		
		if(scrFDate.getFieldValue().length() > 0)
		{
			VtiExitLdbSelectCriterion [] soHeaderSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFDate.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				
			};
        
			VtiExitLdbSelectConditionGroup soHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderSelConds, true);
			VtiExitLdbTableRow[] soHeaderLdbRows = soHeaderLdbTable.getMatchingRows(soHeaderSelCondGrp);

			if(soHeaderLdbRows.length == 0)
				return new VtiUserExitResult(999,"No new Sales Orders.");
			
			for(int c = 0; c < soHeaderLdbRows.length; c++)
			{
				VtiUserExitScreenTableRow newRow = scrTSalesOrd.getNewRow();
			
				newRow.setFieldValue("DATE",soHeaderLdbRows[c].getFieldValue("AUDAT"));
				newRow.setFieldValue("TRUCK_REG",soHeaderLdbRows[c].getFieldValue("TRUCK"));
				newRow.setFieldValue("VBELN",soHeaderLdbRows[c].getFieldValue("VBELN"));
				newRow.setFieldValue("CUSTOMER",soHeaderLdbRows[c].getFieldValue("NAME1"));
				newRow.setFieldValue("STATUS",soHeaderLdbRows[c].getFieldValue("STATUS"));
			
				scrTSalesOrd.appendRow(newRow);
			}
		}
		else
		{
			VtiExitLdbSelectCriterion [] soHeaderSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup soHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderSelConds, true);
			VtiExitLdbTableRow[] soHeaderLdbRows = soHeaderLdbTable.getMatchingRows(soHeaderSelCondGrp);

			if(soHeaderLdbRows.length == 0)
				return new VtiUserExitResult(999,"No new Sales Orders.");
			
			for(int c = 0; c < soHeaderLdbRows.length; c++)
			{
				VtiUserExitScreenTableRow newRow = scrTSalesOrd.getNewRow();
			
				newRow.setFieldValue("DATE",soHeaderLdbRows[c].getFieldValue("AUDAT"));
				newRow.setFieldValue("TRUCK_REG",soHeaderLdbRows[c].getFieldValue("TRUCK"));
				newRow.setFieldValue("VBELN",soHeaderLdbRows[c].getFieldValue("VBELN"));
				newRow.setFieldValue("CUSTOMER",soHeaderLdbRows[c].getFieldValue("NAME1"));
				newRow.setFieldValue("STATUS",soHeaderLdbRows[c].getFieldValue("STATUS"));
			
				scrTSalesOrd.appendRow(newRow);
			}
		}
		
		
		return new VtiUserExitResult();
		
	}
}
