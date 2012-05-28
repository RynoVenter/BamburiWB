package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ShowPackedRSO extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
//Initialising the screenfields to be used

		VtiUserExitScreenField scrRefDoc = getScreenField("REFDOC_S");
		VtiUserExitScreenField scrDate = getScreenField("DATE_S");
		VtiUserExitScreenField scrTime = getScreenField("TIME_S");
		VtiUserExitScreenField scrPacker = getScreenField("PACKER_S");
		VtiUserExitScreenField scrShift = getScreenField("SHIFT_S");
		VtiUserExitScreenField scrTruckReg = getScreenField("REGNO");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrVti_Ref = getScreenField("VTI_REF");
		VtiUserExitScreenField scrCMtart = getScreenField("C_MTART");
		
		VtiUserExitScreenTable tblItems = getScreenTable("TB_ITEMS");
		VtiUserExitScreenTable tblOrders = getScreenTable("TB_ORDERS");
		
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
		VtiExitLdbTable soItemsLdb = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable configLdb = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable packingLdb = getLocalDatabaseTable("YSWB_PACKING");
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
//Validating the instantiation of the ldb's
		if(registerLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_REGISTER");	
		if(soItemsLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_SO_ITEMS");
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
				
		
//&&&&&&&&&&& Table queries

		//Config
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
				bagsUoM = 0;
				return new VtiUserExitResult(000,"Bags Unit of Measure details not maintained in Config.");
			}
			else
			{
				bagsUoM = configLdbRows[0].getLongFieldValue("KEYVAL1");;
			}
			
	
			//Register
			
			VtiExitLdbSelectCriterion [] registerSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
									new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
										//new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),		
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
	        
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
			VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);

			if(registerLdbRows.length == 0)
				return new VtiUserExitResult(999,"The following arrival not found, " + docType + " " + scrRefDoc.getFieldValue() + " : TRUCKREG " +  scrTruckReg.getFieldValue() + ", using reference " + scrVRef.getFieldValue());
	
		Log.trace(0,"orders tbl records " + tblOrders.getRowCount());
			
		for(int i = 0;i < tblOrders.getRowCount();i++)
		{		
			
			Log.trace(0,"SO crit " + tblOrders.getRow(i).getFieldValue("VBELN_I") + ":" + scrCMtart.getFieldValue());
	//So Items
			VtiExitLdbSelectCriterion [] soSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, tblOrders.getRow(i).getFieldValue("VBELN_I")),
								new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, scrCMtart.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
	        
			VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
			VtiExitLdbTableRow[] soLdbRows = soItemsLdb.getMatchingRows(soSelCondGrp);
			
			/*End of Table queries
			****************************************************************************************/
			
				docType = "VBELN";
			
					
			/*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
			 Fill the TB_ITEMS screen table with the items associated with the order number.
			 Calculate the bags for the weight to be packed.
			*****************************************************************************************/
			
			if(soLdbRows.length > 0)
			{
				//tblItems.clear();
				
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
							Log.error("MAT not filled.Length of content" +  soLdbRows[so].getFieldValue("MATNR").length()+ ", Content :" + soLdbRows[so].getFieldValue("MATNR"),soba);
						}
					}
					
					newRow.setFieldValue("MENGE",soLdbRows[so].getFieldValue("NTGEW"));
					newRow.setFieldValue("POSNR",soLdbRows[so].getFieldValue("POSNR"));
					newRow.setFieldValue("Q_REQ",soLdbRows[so].getFieldValue("BAGS"));
						
						VtiExitLdbSelectCriterion [] packSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soLdbRows[so].getFieldValue("VBELN")),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
											//new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVti_Ref.getFieldValue()),
												new VtiExitLdbSelectCondition("POSNR", VtiExitLdbSelectCondition.EQ_OPERATOR, soLdbRows[so].getFieldValue("POSNR")),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
				
						VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
						VtiExitLdbTableRow[] packedLdbRows = packingLdb.getMatchingRows(packSelCondGrp);

						if(packedLdbRows.length == 0)
							return new VtiUserExitResult(000,"Packing details not found.");
						
					newRow.setFieldValue("BAGNR",packedLdbRows[0].getFieldValue("BAGNR"));
					scrDate.setFieldValue(packedLdbRows[0].getFieldValue("START_DATE"));
					newRow.setFieldValue("ISSUED",packedLdbRows[0].getFieldValue("ISSUED"));
					newRow.setFieldValue("MATNR",packedLdbRows[0].getFieldValue("MATNR"));
					scrPacker.setFieldValue(packedLdbRows[0].getFieldValue("PACKER"));
					newRow.setFieldValue("QUEUE",packedLdbRows[0].getFieldValue("QUEUENO"));
					scrShift.setFieldValue(packedLdbRows[0].getFieldValue("SHIFT"));
					scrTime.setFieldValue(packedLdbRows[0].getFieldValue("START_TIME"));
					
						
					tblItems.appendRow(newRow);
				}
			}
		}
		/****************************************************************************************
		*****************************************************************************************/
		return new VtiUserExitResult();
	}
}
