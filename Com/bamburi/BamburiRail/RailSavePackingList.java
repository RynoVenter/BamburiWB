package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RailSavePackingList extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		
		//Get Screen Elements
		VtiUserExitScreenField scrFShift = getScreenField("SHIFT");
		VtiUserExitScreenField scrFPacker = getScreenField("PACKER");
		VtiUserExitScreenField scrFTruckReg = getScreenField("WAGON");
		VtiUserExitScreenField scrFRefDoc = getScreenField("REFDOC");
		VtiUserExitScreenField scrFIsSo = getScreenField("IS_SO");
		VtiUserExitScreenField scrFIsPo = getScreenField("IS_PO");
		VtiUserExitScreenField scrFIsIC = getScreenField("IS_IC");
		VtiUserExitScreenField scrFSTime = getScreenField("TIME");
		VtiUserExitScreenField scrFSDate = getScreenField("DATE");
		VtiUserExitScreenField scrFOrdStat = getScreenField("ORDER_STAT");
		VtiUserExitScreenField scrDocType = getScreenField("DOCTYPE");
		VtiUserExitScreenField scrWVtiRef = getScreenField("VTI_REF");
		VtiUserExitScreenField scrWC_Packing = getScreenField("C_PACKING");
		
		VtiUserExitScreenTable scrTbl = getScreenTable("TB_ITEMS");
		
		if(scrFShift == null) return new VtiUserExitResult(999,"Screenfield SHIFT did not load successfully");
		if(scrFPacker == null) return new VtiUserExitResult(999,"Screenfield PACKER did not load successfully");
		if(scrFTruckReg == null) return new VtiUserExitResult(999,"Screenfield TRUCKREG did not load successfully");
		if(scrFRefDoc == null) return new VtiUserExitResult(999,"Screenfield REFDOC did not load successfully");
		if(scrFIsSo == null) return new VtiUserExitResult(999,"Screenfield IS_SO did not load successfully");
		if(scrFIsPo == null) return new VtiUserExitResult(999,"Screenfield IS_PO did not load successfully");
		if(scrFIsIC == null) return new VtiUserExitResult(999,"Screenfield IS_IC did not load successfully");
		if(scrFSTime == null) return new VtiUserExitResult(999,"Screenfield TIME did not load successfully");
		if(scrFOrdStat == null) return new VtiUserExitResult(999,"Screenfield ORDER_STAT did not load successfully");
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
	
		if(scrFRefDoc.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,"Please select the truck registration.");
		//Get local tables to use
		VtiExitLdbTable ldbPacking = getLocalDatabaseTable("YSWB_PACKING");
		if(ldbPacking == null) return new VtiUserExitResult(999,"Table YSWB_PACKING was not loaded.");
		
		VtiExitLdbTable ldbTranQ = getLocalDatabaseTable("YSWB_TRAN_QUEUE");
		if(ldbTranQ == null) return new VtiUserExitResult(999,"Table YSWB_TRAN_QUEUE was not loaded.");
		
		VtiExitLdbTable ldbWb = getLocalDatabaseTable("YSWB_WB");
		if(ldbWb == null) return new VtiUserExitResult(999,"Table YSWB_WB was not loaded.");
		
		int sTblRowCount = scrTbl.getRowCount();
		DBCalls upDb = new DBCalls();
		long tranNo = 0;
		
		
		try
		{
			tranNo = getNextNumberFromNumberRange("YSWB_PACK");
		}
		catch(VtiExitException ee)
		{
			Log.error("Unable to create number from YSWB_PACK",ee);
			return new VtiUserExitResult(999,"Unable to retrieve a transaction number from number range YSWB_PACK");
		}
		
			if(scrFShift.getFieldValue().length() < 1)
				return new VtiUserExitResult(999,"Please select the Shift.");
			
			if(sTblRowCount < 1)
				return new VtiUserExitResult(999,"Please select the line packed.");
			
			if(scrFTruckReg.getFieldValue().length() < 1)
				return new VtiUserExitResult(999,"Please select the Wagon Number.");
			
		//Check row fields for entries.	
		for(int sTblRowInc = 0; sTblRowInc < sTblRowCount; sTblRowInc++)
		{
			VtiUserExitScreenTableRow currRow = scrTbl.getRow(sTblRowInc);
			
			if(currRow.getIntegerFieldValue("Q_REQ") < 1)
				return new VtiUserExitResult(999,"Please indicate how many was issued.");
//			if(currRow.getFieldValue("QUEUE").length() < 1)
	//			return new VtiUserExitResult(999,"Please select the Packing Line.");
			if(currRow.getFieldValue("BAGNR").length() < 1)
				return new VtiUserExitResult(999,"Please select a bag.");
		}
		//Populate table row and save
			scrWVtiRef.setFieldValue(tranNo);
		for(int sTblRowInc = 0; sTblRowInc < sTblRowCount; sTblRowInc++)
		{
				VtiUserExitScreenTableRow currRow = scrTbl.getRow(sTblRowInc);
				VtiExitLdbTableRow ldbRowPacking = ldbPacking.newRow();
				
				
			if(currRow.getFieldValue("CHK_SEL").equalsIgnoreCase("X"))
		    {	
				//Populate TBL Fields
				ldbRowPacking.setFieldValue("SERVERID", getServerId());
				ldbRowPacking.setFieldValue("SERVERGRP", getServerGroup());
				
				if(scrDocType.getFieldValue().equalsIgnoreCase(""))
				{
					ldbRowPacking.setFieldValue("VBELN", scrFRefDoc.getFieldValue());
					tranType = "PACKING";
					orderDoc = "VBELN";
					statusOrd = "VBELN";
				}
				if(scrDocType.getFieldValue().equalsIgnoreCase("UB"))
				{
					ldbRowPacking.setFieldValue("EBELN", scrFRefDoc.getFieldValue());
					tranType = "PACKING_PO";
					orderDoc = "EBELN";
					statusOrd = "STOCKTRNF";
				}
				if(scrDocType.getFieldValue().equalsIgnoreCase("ZIC"))
				{
					ldbRowPacking.setFieldValue("VBELN", scrFRefDoc.getFieldValue());
					ldbRowPacking.setFieldValue("DELIVDOC", scrFRefDoc.getFieldValue());
					tranType = "PACKING_PO";
					orderDoc = "VBELN";
					statusOrd = "DELIVDOC";
				}
				//Change previous items to rejected
				VtiExitLdbSelectCriterion [] packSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition(orderDoc, VtiExitLdbSelectCondition.EQ_OPERATOR, scrFRefDoc.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
				};
			
				VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
				VtiExitLdbTableRow[] packedLdbRows = ldbPacking.getMatchingRows(packSelCondGrp);
								
				ldbRowPacking.setFieldValue("POSNR", currRow.getFieldValue("POSNR"));
				//ldbRowPacking.setFieldValue("QUEUENO", currRow.getFieldValue("QUEUE"));
				ldbRowPacking.setFieldValue("VTIREF", tranNo);
				ldbRowPacking.setFieldValue("TRUCKREG", scrFTruckReg.getFieldValue());
				ldbRowPacking.setFieldValue("MATNR", currRow.getFieldValue("MATNR"));
				ldbRowPacking.setFieldValue("ISSUED", currRow.getFieldValue("Q_REQ"));
				ldbRowPacking.setFieldValue("BROKEN", currRow.getFieldValue("BROKEN"));
				ldbRowPacking.setFieldValue("PACKER", scrFPacker.getFieldValue());			
				ldbRowPacking.setFieldValue("BAGNR", currRow.getFieldValue("BAGNR"));		
				ldbRowPacking.setFieldValue("SHIFT", scrFShift.getFieldValue());
				ldbRowPacking.setFieldValue("STATUS", "DISPATCH");
				ldbRowPacking.setFieldValue("TIMESTAMP", "");
				ldbRowPacking.setFieldValue("START_TIME", scrFSTime.getFieldValue());
				ldbRowPacking.setFieldValue("START_DATE", scrFSDate.getFieldValue());
				
				
				
				try
				{
					ldbPacking.saveRow(ldbRowPacking);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save data to the YSWB_PACKING table.",ee);
					return new VtiUserExitResult(999,"Unable to Save data to the Packing table.");
				}
			}
		}
		
			try
			{
				upDb.ldbUpload("YSWB_PACKING", this);
				upDb.ldbDownload("YSWB_BAGS", this);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable to Save data to the PACKING table.",ee);
				return new VtiUserExitResult(999,"Unable to Save data to the Packing table.");
			}
			
		scrFRefDoc.setFieldValue("");
		scrFSTime.setFieldValue(currTime);
		scrFSDate.setFieldValue(currDate);
		
		int rCount = scrTbl.getRowCount();
		int i = 0;
		
		while(i<rCount)
		{
			scrTbl.deleteRowAt(i);
			 rCount = scrTbl.getRowCount();
		}
			scrDocType.setFieldValue("");
			scrFRefDoc.setFieldValue("");
			scrFIsSo.setFieldValue("");
			scrFIsPo.setFieldValue("");
			scrFIsIC.setFieldValue("");
			
		return new VtiUserExitResult();
	
	}
}
