package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RailShowPacked extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
//Initialising the screenfields to be used

		VtiUserExitScreenField scrRefDoc = getScreenField("REFDOC_S");
		VtiUserExitScreenField scrDate = getScreenField("DATE_S");
		VtiUserExitScreenField scrTime = getScreenField("TIME_S");
		VtiUserExitScreenField scrPacker = getScreenField("PACKER_S");
		VtiUserExitScreenField scrShift = getScreenField("SHIFT_S");
		VtiUserExitScreenField scrTruckReg = getScreenField("WAGON");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrVti_Ref = getScreenField("VTI_REF");
		VtiUserExitScreenField scrCMtart = getScreenField("C_MTART");
		
		VtiUserExitScreenTable tblItems = getScreenTable("TB_ITEMS");
		
//Validating the instantiation of the screenfields

		if(scrRefDoc == null) return new VtiUserExitResult(999,"Failed to initialise REFDOC_S");
		if(scrDate == null) return new VtiUserExitResult(999,"Failed to initialise DATE_S");
		if(scrTime == null) return new VtiUserExitResult(999,"Failed to initialise TIME_S");
		if(scrPacker == null) return new VtiUserExitResult(999,"Failed to initialise PACKER_S");
		if(scrShift == null) return new VtiUserExitResult(999,"Failed to initialise SHIFT_S");
		if(scrTruckReg == null) return new VtiUserExitResult(999,"Failed to initialise TRUCKREG");
		if(scrVRef == null) return new VtiUserExitResult(999,"Failed to initialise VTIREF");
		if(scrVti_Ref == null) return new VtiUserExitResult(999,"Failed to initialise VTI_REF");
		if(scrCMtart == null) return new VtiUserExitResult(999,"Failed to initialise C_MTART");
		
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
		String errorMsg = "";
		
		Long longStampNow = new Long(stampNow);
		long timeStampNow = longStampNow.longValue();
		long timeStampMin5m = timeStampNow - 500000000;
		String timeStampMin5Months = Long.toString(timeStampMin5m);
		long bagsUoM = 0;
		long gateWeight = 0;
		long bags = 0;
				
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
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, scrCMtart.getFieldValue()),
		};
        
		VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
		VtiExitLdbTableRow[] soLdbRows = soItemsLdb.getMatchingRows(soSelCondGrp);

//Po Items
		VtiExitLdbSelectCriterion [] poSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
		};
        
		VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
		VtiExitLdbTableRow[] poLdbRows = poItemsLdb.getMatchingRows(poSelCondGrp);

//Ic Items
		VtiExitLdbSelectCriterion [] icSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
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
		if(soLdbRows.length > 0  && scrRefDoc.getFieldValue().length() > 0 && hasItems == false)
		{
			docType = "VBELN";
			hasItems = true;
		}
		if(poLdbRows.length > 0  && scrRefDoc.getFieldValue().length() > 0 && hasItems == false)
		{
			docType = "STOCKTRNF";
			hasItems = true;
		}
		if(icLdbRows.length > 0  && scrRefDoc.getFieldValue().length() > 0 && hasItems == false)
		{
			docType = "DELIVDOC";
			hasItems = true;
		}
		
		if(!hasItems)
			return new VtiUserExitResult(999,"No items found for the Truck and Order.");

//Register
		
		VtiExitLdbSelectCriterion [] registerSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
									//new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),		
		};
        
		VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
		VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);

		if(registerLdbRows.length == 0)
			return new VtiUserExitResult(999,"The following arrival not found, " + docType + " " + scrRefDoc.getFieldValue() + " : TRUCKREG " +  scrTruckReg.getFieldValue() + ", using reference " + scrVRef.getFieldValue());
		
	if(poLdbRows.length > 0  && scrRefDoc.getFieldValue().length() > 0)
		{
			docType = "EBELN";
		}
	
//Status
		if(soLdbRows.length == 0)
		{
			VtiExitLdbSelectCriterion [] statHeaderSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
									//new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "WEIGH 1"),				
			};
			
			VtiExitLdbSelectConditionGroup statHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(statHeaderSelConds, true);
			VtiExitLdbTableRow[] statHeaderLdbRows = statusLdb.getMatchingRows(statHeaderSelCondGrp);

			if(statHeaderLdbRows.length == 0)
				return new VtiUserExitResult(999,"STO or Inter Company Order not assigned to truck. Attemp to show" +  docType +".");
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
				errorMsg = "Gatepass not attached during Arrival, bags not calculated for weight.";
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
		
		if(soLdbRows.length > 0)
		{
			if(gateWeight > 0 && bagsUoM > 0)
				bags = (gateWeight * 1000) / bagsUoM;
			
			tblItems.clear();
			
			VtiUserExitScreenTableRow newRow;
			
			for(int so = 0;so < soLdbRows.length;so++)
			{
				newRow = tblItems.getNewRow();
				
				newRow.setFieldValue("DESC",soLdbRows[so].getFieldValue("ARKTX"));
				newRow.setFieldValue("TXTBAGNR","BAG NR:");
				newRow.setFieldValue("Q","Q:");
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
						Log.warn("MAT not filled.Length of content" +  soLdbRows[so].getFieldValue("MATNR").length()+ ", Content :" + soLdbRows[so].getFieldValue("MATNR"),soba);
					}
				}
				
				newRow.setFieldValue("MENGE",soLdbRows[so].getFieldValue("NTGEW"));
				newRow.setFieldValue("POSNR",soLdbRows[so].getFieldValue("POSNR"));
				newRow.setFieldValue("Q_REQ",soLdbRows[so].getFieldValue("BAGS"));
					
					VtiExitLdbSelectCriterion [] packSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
										new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVti_Ref.getFieldValue()),
											new VtiExitLdbSelectCondition("POSNR", VtiExitLdbSelectCondition.EQ_OPERATOR, soLdbRows[so].getFieldValue("POSNR")),
					};
			
					VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
					VtiExitLdbTableRow[] packedLdbRows = packingLdb.getMatchingRows(packSelCondGrp);

					if(packedLdbRows.length > 0)
					{
						newRow.setFieldValue("BAGNR",packedLdbRows[0].getFieldValue("BAGNR"));
						newRow.setFieldValue("BROKEN",packedLdbRows[0].getFieldValue("BROKEN"));
						scrDate.setFieldValue(packedLdbRows[0].getFieldValue("START_DATE"));
						newRow.setFieldValue("ISSUED",packedLdbRows[0].getFieldValue("ISSUED"));
						newRow.setFieldValue("MATNR",packedLdbRows[0].getFieldValue("MATNR"));
						scrPacker.setFieldValue(packedLdbRows[0].getFieldValue("PACKER"));
						newRow.setFieldValue("QUEUE",packedLdbRows[0].getFieldValue("QUEUENO"));
						scrShift.setFieldValue(packedLdbRows[0].getFieldValue("SHIFT"));
						scrTime.setFieldValue(packedLdbRows[0].getFieldValue("START_TIME"));
					}
					else
						return new VtiUserExitResult(000,"Packing details not found.");
					
					tblItems.appendRow(newRow);
			}
		}
		
		if(poLdbRows.length > 0)
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
				newRow.setFieldValue("TXTBAGNR","BAG NR:");
				newRow.setFieldValue("Q","Q:");
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
					
					VtiExitLdbSelectCriterion [] packSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
										new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVti_Ref.getFieldValue()),
											new VtiExitLdbSelectCondition("POSNR", VtiExitLdbSelectCondition.EQ_OPERATOR, poLdbRows[po].getFieldValue("EBELP")),
					};
			
					VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
					VtiExitLdbTableRow[] packedLdbRows = packingLdb.getMatchingRows(packSelCondGrp);

					if(packedLdbRows.length > 0)
					{
						newRow.setFieldValue("BAGNR",packedLdbRows[0].getFieldValue("BAGNR"));
						newRow.setFieldValue("BROKEN",packedLdbRows[0].getFieldValue("BROKEN"));
						scrDate.setFieldValue(packedLdbRows[0].getFieldValue("START_DATE"));
						newRow.setFieldValue("ISSUED",packedLdbRows[0].getFieldValue("ISSUED"));
						newRow.setFieldValue("MATNR",packedLdbRows[0].getFieldValue("MATNR"));
						scrPacker.setFieldValue(packedLdbRows[0].getFieldValue("PACKER"));
						newRow.setFieldValue("QUEUE",packedLdbRows[0].getFieldValue("QUEUENO"));
						scrShift.setFieldValue(packedLdbRows[0].getFieldValue("SHIFT"));
						scrTime.setFieldValue(packedLdbRows[0].getFieldValue("START_TIME"));
					}
					else
						return new VtiUserExitResult(000,"Packing details not found.");
				
				
				tblItems.appendRow(newRow);
			}
		}

		if(icLdbRows.length > 0)
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
				newRow.setFieldValue("TXTBAGNR","BAG NR:");
				newRow.setFieldValue("Q","Q:");
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
						Log.warn("MAT not filled.Length of content" +  icLdbRows[ic].getFieldValue("MATNR").length()+ ", Content :" + icLdbRows[ic].getFieldValue("MATNR"),soba);
					}
				}
								
				newRow.setFieldValue("MENGE",icLdbRows[ic].getFieldValue("LFIMG"));
				newRow.setFieldValue("POSNR",icLdbRows[ic].getFieldValue("POSNR"));
				newRow.setFieldValue("Q_REQ",icLdbRows[ic].getFieldValue("BAGS"));
				
					VtiExitLdbSelectCriterion [] packSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
										new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVti_Ref.getFieldValue()),
											new VtiExitLdbSelectCondition("POSNR", VtiExitLdbSelectCondition.EQ_OPERATOR, icLdbRows[ic].getFieldValue("POSNR")),
					};
			
					VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
					VtiExitLdbTableRow[] packedLdbRows = packingLdb.getMatchingRows(packSelCondGrp);

					if(packedLdbRows.length > 0)
					{
						newRow.setFieldValue("BAGNR",packedLdbRows[0].getFieldValue("BAGNR"));
						newRow.setFieldValue("BROKEN",packedLdbRows[0].getFieldValue("BROKEN"));
						scrDate.setFieldValue(packedLdbRows[0].getFieldValue("START_DATE"));
						newRow.setFieldValue("ISSUED",packedLdbRows[0].getFieldValue("ISSUED"));
						newRow.setFieldValue("MATNR",packedLdbRows[0].getFieldValue("MATNR"));
						scrPacker.setFieldValue(packedLdbRows[0].getFieldValue("PACKER"));
						newRow.setFieldValue("QUEUE",packedLdbRows[0].getFieldValue("QUEUENO"));
						scrShift.setFieldValue(packedLdbRows[0].getFieldValue("SHIFT"));
						scrTime.setFieldValue(packedLdbRows[0].getFieldValue("START_TIME"));
					}
					else
						return new VtiUserExitResult(000,"Packing details not found.");
					
				tblItems.appendRow(newRow);
			}
		}
		
		hasItems = false;
		/****************************************************************************************
		*****************************************************************************************/
		return new VtiUserExitResult();
	}
}
