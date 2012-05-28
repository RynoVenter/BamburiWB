package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class UpdatePackingRSO extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Get Screen Elements
		VtiUserExitScreenField scrFShift = getScreenField("SHIFT");
		VtiUserExitScreenField scrFPacker = getScreenField("PACKER");
		VtiUserExitScreenField scrFTruckReg = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrFRefDoc = getScreenField("REFDOC");
		VtiUserExitScreenField scrFIsSo = getScreenField("IS_SO");
		VtiUserExitScreenField scrFIsPo = getScreenField("IS_PO");
		VtiUserExitScreenField scrFIsIC = getScreenField("IS_IC");
		VtiUserExitScreenField scrFSTime = getScreenField("TIME");
		VtiUserExitScreenField scrFSDate = getScreenField("DATE");
		VtiUserExitScreenField scrDocType = getScreenField("DOCTYPE");
		VtiUserExitScreenField scrWVtiRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrWC_Packing = getScreenField("C_PACKING");
		VtiUserExitScreenField scrEndTime = getScreenField("ENDTIME");
		VtiUserExitScreenField scrLoader = getScreenField("LOADER");
		VtiUserExitScreenField scrStartTime = getScreenField("STARTTIME");
		
		VtiUserExitScreenTable scrTbl = getScreenTable("TB_ITEMS");
		
		if(scrFShift == null) return new VtiUserExitResult(999,"Screenfield SHIFT did not load successfully");
		if(scrFPacker == null) return new VtiUserExitResult(999,"Screenfield PACKER did not load successfully");
		if(scrFTruckReg == null) return new VtiUserExitResult(999,"Screenfield TRUCKREG did not load successfully");
		if(scrFRefDoc == null) return new VtiUserExitResult(999,"Screenfield REFDOC did not load successfully");
		if(scrFIsSo == null) return new VtiUserExitResult(999,"Screenfield IS_SO did not load successfully");
		if(scrFIsPo == null) return new VtiUserExitResult(999,"Screenfield IS_PO did not load successfully");
		if(scrFIsIC == null) return new VtiUserExitResult(999,"Screenfield IS_IC did not load successfully");
		if(scrFSTime == null) return new VtiUserExitResult(999,"Screenfield TIME did not load successfully");
		if(scrFSDate == null) return new VtiUserExitResult(999,"Screenfield DATE did not load successfully");
		if(scrDocType == null) return new VtiUserExitResult(999,"Screenfield DOCTYPE did not load successfully");
		if(scrWVtiRef == null) return new VtiUserExitResult(999,"Screenfield VTI_REF did not load successfully");
		
		if(scrTbl == null) return new VtiUserExitResult(999,"Screenfield TB_ITEMS did not load successfully");
		
		Date currNow = new Date();
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String tranType = "";
		String orderDoc = "";
		String statusOrd = "";
		String errorMsg = null;
		String rejectionErr = "";
		
		String stampNow = currLdbDate + currLdbTime;
	
		if(scrFTruckReg.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "Please select the truck registration.");
		
		if(scrStartTime.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "Please select the start time.");
		
		if(scrEndTime.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "Please select the end time.");
		
		if(scrFShift.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "Please select the shift.");

		if(scrFRefDoc.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,"Please select the truck registration.");
		//Get local tables to use
		VtiExitLdbTable ldbPacking = getLocalDatabaseTable("YSWB_PACKING");
		if(ldbPacking == null) return new VtiUserExitResult(999,"Table YSWB_PACKING was not loaded.");
		
		VtiExitLdbTable ldbLoading = getLocalDatabaseTable("YSWB_LOADING");
		if(ldbLoading == null) return new VtiUserExitResult(999,"Table YSWB_LOADING was not loaded.");
		
		int sTblRowCount = scrTbl.getRowCount();
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		

		//Check row fields for entries.	
		for(int sTblRowValid = 0; sTblRowValid < sTblRowCount; sTblRowValid++)
		{
			VtiUserExitScreenTableRow currRow = scrTbl.getRow(sTblRowValid);
			
			if(currRow.getFieldValue("QUEUE").length() == 0)
				return new VtiUserExitResult(999,1,"Check that the packing line for all items are captured.");
			
			if(currRow.getIntegerFieldValue("Q_ISS") != currRow.getIntegerFieldValue("Q_REQ"))
				return new VtiUserExitResult(999,1,"Check that all fields for all items are captured.");
			
			if(currRow.getFieldValue("BROKEN").length() == 0)
				return new VtiUserExitResult(999,1,"Check that all fields for all items are captured.");
			
			if(currRow.getFieldValue("DAMAGED").length() == 0)
				return new VtiUserExitResult(999,1,"Check that all fields for all items are captured.");
			
			
		}
		
		
		for(int sTblRowInc = 0; sTblRowInc < sTblRowCount; sTblRowInc++)
		{
			VtiUserExitScreenTableRow currRow = scrTbl.getRow(sTblRowInc);
					
				if(scrDocType.getFieldValue().equalsIgnoreCase(""))
				{
					tranType = "PACKING";
					orderDoc = "VBELN";
					statusOrd = "VBELN";
				}
				
				//Change previous items to rejected
				VtiExitLdbSelectCriterion [] packSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition(orderDoc, VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("VBELN_IP")),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
									new VtiExitLdbSelectCondition("POSNR", VtiExitLdbSelectCondition.EQ_OPERATOR, currRow.getFieldValue("POSNR")),
										new VtiExitLdbSelectCondition("EBELP", VtiExitLdbSelectCondition.NE_OPERATOR, "1"),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
				};
			
				VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
				
				VtiExitLdbOrderSpecification [] pOrderBy = 
					{
						new VtiExitLdbOrderSpecification("VTIREF",false),
					};
				
				VtiExitLdbTableRow[] packedLdbRows = ldbPacking.getMatchingRows(packSelCondGrp,pOrderBy);
				
				if(packedLdbRows.length == 0)
					return new VtiUserExitResult(999, "Packing record not found.");			
							   
			if(packedLdbRows.length > 0)
			{
				long packVtiRef = packedLdbRows[0].getLongFieldValue("VTIREF");
				packedLdbRows[0].setFieldValue("BROKEN", currRow.getFieldValue("BROKEN"));
				packedLdbRows[0].setFieldValue("DAMAGED", currRow.getFieldValue("DAMAGED"));
				packedLdbRows[0].setFieldValue("EBELP", "1");
				packedLdbRows[0].setFieldValue("TIMESTAMP", "");
				
				VtiExitLdbTableRow ldbRowLoad = ldbLoading.newRow();
				
				ldbRowLoad.setFieldValue("L_VTIREF", Long.toString(packVtiRef));
				ldbRowLoad.setFieldValue("START_DATE", scrFSDate.getFieldValue());
				ldbRowLoad.setFieldValue("END_TIME", scrEndTime.getFieldValue());
				ldbRowLoad.setFieldValue("LOADER", scrLoader.getFieldValue());
				ldbRowLoad.setFieldValue("MATNR",currRow.getFieldValue("MATNR"));
				ldbRowLoad.setFieldValue("POSNR",currRow.getFieldValue("POSNR"));
				ldbRowLoad.setFieldValue("QUEUENO",currRow.getFieldValue("QUEUE"));
				ldbRowLoad.setFieldValue("ISSUED",currRow.getFieldValue("Q_ISS"));
				ldbRowLoad.setFieldValue("VBELN",currRow.getFieldValue("VBELN_IP"));
				ldbRowLoad.setFieldValue("SERVERGRP", getServerGroup());
				ldbRowLoad.setFieldValue("SERVERID", getServerId());
				ldbRowLoad.setFieldValue("SHIFT",scrFShift.getFieldValue());
				ldbRowLoad.setFieldValue("START_TIME", scrStartTime.getFieldValue());
				ldbRowLoad.setFieldValue("TRUCKREG", scrFTruckReg.getFieldValue());
				ldbRowLoad.setFieldValue("VTIREF", scrWVtiRef.getFieldValue());
				ldbRowLoad.setFieldValue("OFF_ORDER",scrFRefDoc.getFieldValue());
						
				try
				{
					ldbLoading.saveRow(ldbRowLoad);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save data to the YSWB_LOADING table.",ee);
					return new VtiUserExitResult(999,"Unable to update data to the Loading table.");
				}
				
				try
				{
					ldbPacking.saveRow(packedLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save data to the YSWB_PACKING table.",ee);
					return new VtiUserExitResult(999,"Unable to Save data to the Packing table.");
				}

			}
			else
			{
				errorMsg = "Packing not done yet.";
			}
			
		}
		
		if(errorMsg != null)
			return new VtiUserExitResult(999,1, errorMsg);
		else
		{
			sessionHeader.setNextFunctionId("YSWB_MAIN");
			
			return new VtiUserExitResult();
		}
	}
}
