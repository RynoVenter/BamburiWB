package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RailFillPickingList extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
//Initialising the screenfields to be used
		VtiUserExitScreenField scrSoNo = getScreenField("IS_SO");
		VtiUserExitScreenField scrPoNo = getScreenField("IS_PO");
		VtiUserExitScreenField scrIcNo = getScreenField("IS_IC");
		VtiUserExitScreenField scrTruckReg = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrCMtart = getScreenField("C_MTART");
		VtiUserExitScreenField scrPQty = getScreenField("P_QTY");
		VtiUserExitScreenField scrWQty = getScreenField("W_QTY");
		VtiUserExitScreenField scrRQty = getScreenField("R_QTY");
		VtiUserExitScreenField btnSave = getScreenField("BT_SAVE");
		VtiUserExitScreenField scrWagon = getScreenField("WAGON");
		
		VtiUserExitScreenTable tblItems = getScreenTable("TB_ITEMS");
		
//Validating the instantiation of the screenfields
		if(scrSoNo == null) return new VtiUserExitResult(999,"Failed to initialise IS_SO");
		if(scrPoNo == null) return new VtiUserExitResult(999,"Failed to initialise IS_PO");
		if(scrIcNo == null) return new VtiUserExitResult(999,"Failed to initialise IS_IC");
		if(scrTruckReg == null) return new VtiUserExitResult(999,"Failed to initialise TRUCKREG");
		if(scrVRef == null) return new VtiUserExitResult(999,"Failed to initialise VTI_REF");
		if(scrCMtart == null) return new VtiUserExitResult(999,"Failed to initialise C_MTART");
		if(scrWagon == null) return new VtiUserExitResult(999,"Failed to initialise WAGON");

		if(btnSave == null) return new VtiUserExitResult(999,"Failed to initialise BT_SAVE");
		
		if(tblItems == null) return new VtiUserExitResult(999,"Failed to initialise TB_ITEMS");
		
//Initialising the ldb' to use	
		VtiExitLdbTable registerLdb = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable gatepassLdb = getLocalDatabaseTable("YSWB_GATEPASS");
		VtiExitLdbTable soItemsLdb = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable poItemsLdb = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable icItemsLdb = getLocalDatabaseTable("YSWB_IC_ITEMS");
		VtiExitLdbTable statusLdb = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable configLdb = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable packingLdb = getLocalDatabaseTable("YSWB_PACKING");
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
//Validating the instantiation of the ldb's
		if(registerLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_REGISTER");	
		if(gatepassLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_GATEPASS");
		if(soItemsLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_SO_ITEMS");
		if(poItemsLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_PO_ITEMS");
		if(icItemsLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_IC_ITEMS");
		if(statusLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_STATUS");
		if(configLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_CONFIG");
		if(packingLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_PACKING");
		if(sessionHeader == null) return new VtiUserExitResult(999,"Failed to get Header Info");
		
//&&&&&&&&&&& Declaring all the variables and constants to be used
		
		boolean hasItems = false;
		String docType = "";
		Date currNow = new Date();
		String currDate = DateFormatter.format("yyyyMMdd", currNow);
		String currTime = DateFormatter.format("HHmmss", currNow);
		String stampNow = currDate + currTime;
		String order = "";
		
		Double doubleStampNow = new Double(stampNow);
		double timeStampNow = doubleStampNow.doubleValue();
		double timeStampMin5m = timeStampNow - 500000000;
		String timeStampMin5Months = Double.toString(timeStampMin5m);
		long bagsUoM = 0;
		long gateWeight = 0;
		long bags = 0;
		String packDocType = "";
		
		tblItems.clear();
		
		
		
		/*End of the declaration and and instantiation of fields and tables
		****************************************************************************************/
		
//&&&&&&&&&&& Validation on screen field values
		

		/*End of validation
		****************************************************************************************/
		
//&&&&&&&&&&& Table queries
		
//So Items
		VtiExitLdbSelectCriterion [] soSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrSoNo.getFieldValue()),
							new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, scrCMtart.getFieldValue()),
		};
        
		VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
		VtiExitLdbTableRow[] soLdbRows = soItemsLdb.getMatchingRows(soSelCondGrp);

//Po Items
		VtiExitLdbSelectCriterion [] poSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrPoNo.getFieldValue()),
		};
        
		VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
		VtiExitLdbTableRow[] poLdbRows = poItemsLdb.getMatchingRows(poSelCondGrp);

//Ic Items
		VtiExitLdbSelectCriterion [] icSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrIcNo.getFieldValue()),
		};
        
		VtiExitLdbSelectConditionGroup icSelCondGrp = new VtiExitLdbSelectConditionGroup(icSelConds, true);
		VtiExitLdbTableRow[] icLdbRows = icItemsLdb.getMatchingRows(icSelCondGrp);
	
//Config
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "BAGS_UOM"),
		};
        
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbTableRow[] configLdbRows = configLdb.getMatchingRows(configSelCondGrp);
		if(configLdbRows.length == 0)
		{
			bagsUoM = 0;
			return new VtiUserExitResult(000,"Bags Unit of Measure details not maintained in Config.");
		}
		else
		{
			bagsUoM = configLdbRows[0].getLongFieldValue("KEYVAL1");;
		}
		
		/*End of Table queries
		****************************************************************************************/
		if(soLdbRows.length > 0  && scrSoNo.getFieldValue().length() > 0)
		{
			docType = "VBELN";
			packDocType = "VBELN";
			order = scrSoNo.getFieldValue();
			hasItems = true;
		}
		if(poLdbRows.length > 0  && scrPoNo.getFieldValue().length() > 0)
		{
			docType = "STOCKTRNF";
			packDocType = "EBELN";
			order = scrPoNo.getFieldValue();
			hasItems = true;
		}
		if(icLdbRows.length > 0  && scrIcNo.getFieldValue().length() > 0)
		{
			docType = "DELIVDOC";
			packDocType = "DELIVDOC";
			order = scrIcNo.getFieldValue();
			hasItems = true;
		}
		
		if(!hasItems)
			return new VtiUserExitResult(999,"No items found for the Truck and Order.");

		//Packing LDB Count 
		VtiExitLdbSelectCriterion [] packSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(packDocType, VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
		};
        
		VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
		VtiExitLdbTableRow[] packLdbRows = packingLdb.getMatchingRows(packSelCondGrp);
		
//Register
		VtiExitLdbSelectCriterion [] registerSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWagon.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
		};
        
		VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
		VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
		Log.info(registerLdbRows.length + "rows matching Regs during Packing disp for " + docType + "order :" + order);
		if(registerLdbRows.length == 0)
			return new VtiUserExitResult(999,"Arrival entry not found.");
	
//Status
		if(soLdbRows.length == 0)
		{
			String orderNo = "";
			if(scrPoNo.getFieldValue().length() > 0)
			{
				orderNo = scrPoNo.getFieldValue();
			}
			else
			{
				orderNo = scrIcNo.getFieldValue();
			}
				   
			VtiExitLdbSelectCriterion [] statHeaderSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWagon.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
			};
			
			VtiExitLdbSelectConditionGroup statHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(statHeaderSelConds, true);
			VtiExitLdbTableRow[] statHeaderLdbRows = statusLdb.getMatchingRows(statHeaderSelCondGrp);

			if(statHeaderLdbRows.length == 0)
				return new VtiUserExitResult(999,"STO or Inter Company Order not assigned to truck.");
		}
		
		//Gate Pass
		if(registerLdbRows[0].getFieldValue("GATE_PASS").length() > 0)
		{
			VtiExitLdbSelectCriterion [] gateSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, registerLdbRows[0].getFieldValue("GATE_PASS")),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
			};
			
			VtiExitLdbSelectConditionGroup gateSelCondGrp = new VtiExitLdbSelectConditionGroup(gateSelConds, true);
			VtiExitLdbTableRow[] gateLdbRows = gatepassLdb.getMatchingRows(gateSelCondGrp);

			if(gateLdbRows.length == 0)
			{
				gateWeight = 0;
			}
			else
			{
				gateWeight = gateLdbRows[0].getLongFieldValue("MENGE");
			}
		}
		
		/*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		 Fill the TB_ITEMS screen table with the items associated with the order number.
		 Calculate the bags for the weight to be packed.
		*****************************************************************************************/
		
		if(soLdbRows.length > 0  && scrSoNo.getFieldValue().length() > 0)
		{
			if(gateWeight > 0 && bagsUoM > 0)
				bags = (gateWeight * 1000) / bagsUoM;
			tblItems.clear();
			
			VtiUserExitScreenTableRow newRow;
			
			for(int so = 0;so < soLdbRows.length;so++)
			{
				newRow = tblItems.getNewRow();
				newRow.setFieldValue("DESC",soLdbRows[so].getFieldValue("ARKTX"));
				newRow.setFieldValue("MATNR",soLdbRows[so].getFieldValue("MATNR"));
				
				if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOADING"))
					newRow.setFieldValue("MEINS",soLdbRows[so].getFieldValue("MEINS"));
				
				try
				{
					newRow.setFieldValue("MAT",soLdbRows[so].getFieldValue("MATNR").substring(soLdbRows[so].getFieldValue("MATNR").length() - 8,soLdbRows[so].getFieldValue("MATNR").length()));
				}
				catch (StringIndexOutOfBoundsException sobe)
				{	
					try
					{
						newRow.setFieldValue("MAT",soLdbRows[so].getFieldValue("MATNR"));
					}
					catch (StringIndexOutOfBoundsException soba)
					{
						Log.error("MAT not filled.Length of content" +  soLdbRows[so].getFieldValue("MATNR").length()+ ", Content :" + soLdbRows[so].getFieldValue("MATNR"), soba );
					}
				}
				
				newRow.setFieldValue("MENGE",soLdbRows[so].getFieldValue("NTGEW"));
				newRow.setFieldValue("POSNR",soLdbRows[so].getFieldValue("POSNR"));
				newRow.setFieldValue("Q_REQ",soLdbRows[so].getFieldValue("BAGS"));
				newRow.setFieldValue("TXTBAGNR","BAG NR:");
				newRow.setFieldValue("Q","Q:");
				
				tblItems.appendRow(newRow);
			}
		}
		
		if(poLdbRows.length > 0  && scrPoNo.getFieldValue().length() > 0)
		{
			if(gateWeight > 0 && bagsUoM > 0)
			{
				bags = (gateWeight * 1000) / bagsUoM;
			}
			tblItems.clear();
			
			VtiUserExitScreenTableRow newRow;
			
			for(int po = 0;po < poLdbRows.length;po++)
			{
				newRow = tblItems.getNewRow();
				newRow.setFieldValue("DESC",poLdbRows[po].getFieldValue("TXZ01"));
				newRow.setFieldValue("MATNR",poLdbRows[po].getFieldValue("MATNR"));
				
				if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOADING"))
					newRow.setFieldValue("MEINS",poLdbRows[po].getFieldValue("MEINS"));
				
				try
				{
					newRow.setFieldValue("MAT",poLdbRows[po].getFieldValue("MATNR").substring(poLdbRows[po].getFieldValue("MATNR").length() - 8,poLdbRows[po].getFieldValue("MATNR").length()));
				}
				catch (StringIndexOutOfBoundsException sobe)
				{	
					try
					{
						newRow.setFieldValue("MAT",poLdbRows[po].getFieldValue("MATNR"));
					}
					catch (StringIndexOutOfBoundsException soba)
					{
						Log.warn("MAT not filled.Length of content" +  poLdbRows[po].getFieldValue("MATNR").length()+ ", Content :" + poLdbRows[po].getFieldValue("MATNR"),soba);
					}
				}
				if(gateWeight > 0)
					newRow.setFieldValue("MENGE",gateWeight);
				else
					newRow.setFieldValue("MENGE",poLdbRows[po].getFieldValue("NTGEW"));
				
				newRow.setFieldValue("POSNR",poLdbRows[po].getFieldValue("EBELP"));
				newRow.setFieldValue("Q_REQ",bags);
				newRow.setFieldValue("TXTBAGNR","BAG NR:");
				newRow.setFieldValue("Q","Q:");
				
				tblItems.appendRow(newRow);
			}
		}

		if(icLdbRows.length > 0  && scrIcNo.getFieldValue().length() > 0)
		{
			if(gateWeight > 0 && bagsUoM > 0)
			{
				bags = (gateWeight * 1000) / bagsUoM;
			}
			tblItems.clear();
			
			VtiUserExitScreenTableRow newRow;
			
			for(int ic = 0;ic < icLdbRows.length;ic++)
			{
				newRow = tblItems.getNewRow();
				newRow.setFieldValue("DESC",icLdbRows[ic].getFieldValue("ARKTX"));
				newRow.setFieldValue("MATNR",icLdbRows[ic].getFieldValue("MATNR"));
				
				if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOADING"))
					newRow.setFieldValue("MEINS",icLdbRows[ic].getFieldValue("MEINS"));
				
				try
				{
					newRow.setFieldValue("MAT",icLdbRows[ic].getFieldValue("MATNR").substring(icLdbRows[ic].getFieldValue("MATNR").length() - 8,icLdbRows[ic].getFieldValue("MATNR").length()));
				}
				catch (StringIndexOutOfBoundsException sobe)
				{	
					try
					{
						newRow.setFieldValue("MAT",icLdbRows[ic].getFieldValue("MATNR"));
					}
					catch (StringIndexOutOfBoundsException soba)
					{
						Log.warn("MAT not filled.Length of content" +  icLdbRows[ic].getFieldValue("MATNR").length()+ ", Content :" + icLdbRows[ic].getFieldValue("MATNR"), soba);
					}
				}
				
				newRow.setFieldValue("MENGE",icLdbRows[ic].getFieldValue("LFIMG"));
				newRow.setFieldValue("POSNR",icLdbRows[ic].getFieldValue("POSNR"));
				newRow.setFieldValue("TXTBAGNR","BAG NR:");
				newRow.setFieldValue("Q","Q:");
				newRow.setFieldValue("Q_REQ",icLdbRows[ic].getFieldValue("BAGS"));
				
				tblItems.appendRow(newRow);
			}
		}
		/****************************************************************************************
		*****************************************************************************************/
		return new VtiUserExitResult();
	}
}
