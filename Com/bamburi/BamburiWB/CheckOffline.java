package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckOffline extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitHeaderInfo sessionInfo = getHeaderInfo();
		
		VtiUserExitScreenTable ordersTable = getScreenTable("TB_SALES");
		VtiUserExitScreenField orderType = getScreenField("ORDERTYPE");
		
		if(ordersTable == null) return new VtiUserExitResult (999,"Failed to initialise TB_SALES.");
		if(orderType == null) return new VtiUserExitResult (999,"Failed to initialise orderType.");
		
		VtiUserExitScreenTableRow currRow = ordersTable.getActiveRow();

		VtiExitLdbTable soHeaderLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable poItemsLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable icHeaderLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable inspectLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiUserExitScreenField scrFDispWeight = getScreenField("ZEROD");
		
		if (soHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (poItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_ITEMS.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (icHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_HEADER.");
		if (inspectLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		
		
		//if(!scrFDispWeight.getFieldValue().equalsIgnoreCase("ZEROD"))
		//	return new VtiUserExitResult(999, "Make sure the truck has left the weighbridge.");
		//Check host connection
		String hostName = getHostInterfaceName();
		forceHeartbeat(hostName,true,250);
		boolean hostConnected = isHostInterfaceConnected(hostName);

		//Determine offline and weigh screen for the SO
		if(sessionInfo.getFunctionId().equalsIgnoreCase("YSWB_TROUTBOUND"))
		{
			VtiExitLdbSelectCriterion [] soHeaderSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("DATE")),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("STATUS")),
								new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("TRUCK_REG")),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("VBELN")),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup soHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderSelConds, true);
			VtiExitLdbTableRow[] soHeaderLdbRows = soHeaderLdbTable.getMatchingRows(soHeaderSelCondGrp);
			
			if(soHeaderLdbRows.length != 0)
			{	
				if(soHeaderLdbRows[0].getFieldValue("VBELN").substring(0,1).equalsIgnoreCase("R"))
				{
					sessionInfo.setNextFunctionId("YSWB_RSOWEIGH");
				}
				else
				{
					sessionInfo.setNextFunctionId("YSWB_WEIGH");
				}
			}
			
		};		
		//Determine offline and weigh screen for the PO
		if(sessionInfo.getFunctionId().equalsIgnoreCase("YSWB_TRINBOUND"))
		{
			VtiExitLdbSelectCriterion [] inspectSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("REGNO")),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup inspectSelCondGrp = new VtiExitLdbSelectConditionGroup(inspectSelConds, true);
			VtiExitLdbTableRow[] inspectLdbRows = inspectLdbTable.getMatchingRows(inspectSelCondGrp);
			
			if(inspectLdbRows.length == 0)
				return new VtiUserExitResult(999, "Inspection not found.");		
	
			if(inspectLdbRows[0].getFieldValue("STOCK").equalsIgnoreCase("X"))
				orderType.setFieldValue("YSWB_ORIGIN");
			else
				orderType.setFieldValue("YSWB_FUEL_SUPPL");

			VtiExitLdbSelectCriterion [] statusSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("STATUS")),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("REGNO")),
								new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("EBELN")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
			VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
			
			VtiExitLdbSelectCriterion [] poSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						//new VtiExitLdbSelectCondition("BEDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("DATE")),
							new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("EBELN")),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
			VtiExitLdbTableRow[] poLdbRows = poItemsLdbTable.getMatchingRows(poSelCondGrp);

			if(statusLdbRows.length != 0 && poLdbRows.length != 0)
			{	
				if(statusLdbRows[0].getFieldValue("STOCK").equalsIgnoreCase("X") ||
				   poLdbRows[0].getFieldValue("MATKL").equalsIgnoreCase("45") ||
				   poLdbRows[0].getFieldValue("MATKL").equalsIgnoreCase("46"))
				{
					sessionInfo.setNextFunctionId("YSWB_WEIGH_COMP");
				}
				else
				{
					hostConnected = isHostInterfaceConnected(hostName);


					//check if offline, offline uses yswb_weigh2_off
					if(hostConnected)
					{
						sessionInfo.setNextFunctionId("YSWB_WEIGH2");
					}
					else
					{
						sessionInfo.setNextFunctionId("YSWB_WEIGH2_OFF");
					}
				}
			}
		}
	
		//Determine offline and weigh screen for the Transfer
		if(sessionInfo.getFunctionId().equalsIgnoreCase("YSWB_TRANSFER"))
		{		
			VtiExitLdbSelectCriterion [] statusSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("STATUS")),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("REGNO")),
								new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("EBELN")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
			VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
			
			VtiExitLdbSelectCriterion [] poSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						//new VtiExitLdbSelectCondition("BEDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("DATE")),
							new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("EBELN")),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
			VtiExitLdbTableRow[] poLdbRows = poItemsLdbTable.getMatchingRows(poSelCondGrp);

			if(statusLdbRows.length != 0 && poLdbRows.length != 0)
			{
					//check if offline, offline uses yswb_weigh2_off
					if(hostConnected)
					{
						sessionInfo.setNextFunctionId("YSWB_WEIGH2");
					}
					else
					{
						sessionInfo.setNextFunctionId("YSWB_WEIGH2_OFF");
					}
			}
		}
		
		//Determine offline and weigh screen for the IC
		if(sessionInfo.getFunctionId().equalsIgnoreCase("YSWB_TRIC"))
		{
			VtiExitLdbSelectCriterion [] statusSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("STATUS")),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("TRUCK_REG")),
								new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("VBELN")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
			VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
			
			VtiExitLdbSelectCriterion [] icSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("VBELN")),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup icSelCondGrp = new VtiExitLdbSelectConditionGroup(icSelConds, true);
			VtiExitLdbTableRow[] icLdbRows = icHeaderLdbTable.getMatchingRows(icSelCondGrp);

			if(statusLdbRows.length != 0 && icLdbRows.length != 0)
			{
				sessionInfo.setNextFunctionId("YSWB_WEIGH_DEL");
			}
		}
		
		//Determine offline and weigh screen for the nodoc
		if(sessionInfo.getFunctionId().equalsIgnoreCase("YSWB_TRNODOC"))
		{
			orderType.setFieldValue("YSWB_ORIGIN");
				
			VtiExitLdbSelectCriterion [] statusSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("STATUS")),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("REGNO")),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("VTIREF")),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
			VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
			
			if(statusLdbRows.length != 0)
			{
				sessionInfo.setNextFunctionId("YSWB_WEIGH_ND");
			}
		}
		
		return new VtiUserExitResult();
	}
}
