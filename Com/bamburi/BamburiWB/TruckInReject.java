package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class TruckInReject extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currLdbDate = DateFormatter.format("ddMMyyyy", currNow);
		String currLDBTime = DateFormatter.format("HHmmss", currNow);
		
		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		
		//Declarations of variables and elements. Followed by the checking of the elements.
		VtiUserExitScreenField scrWFPurchOrd = getScreenField("EBELN");
		VtiUserExitScreenField scrWFRejReason = getScreenField("REJ_REASON");
		VtiUserExitScreenField scrFWeight1 = getScreenField("WEIGHT1");
		VtiUserExitScreenField scrFWeight2 = getScreenField("WEIGHT2");
		VtiUserExitScreenField scrFNett = getScreenField("NETT_WEIGHT");
		VtiUserExitScreenField scrTruck = getScreenField("TRUCK_REG");
		VtiUserExitScreenField scrWTransfer = getScreenField("BSART");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrSlipNo = getScreenField("VTI_REF");
		VtiUserExitScreenField scrCBBridge = getScreenField("WEIGHBRIDGE");		
		
		if(scrWFPurchOrd == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
		if(scrWFRejReason == null) return new VtiUserExitResult (999,"Failed to initialise REJ_REASON.");
		if(scrWTransfer == null) return new VtiUserExitResult (999,"Failed to initialise BSART.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise REFNO.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");

		//Database TBL Declaration
		VtiExitLdbTable poHeaderTWRejLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable statusRejLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable regRejLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable wbTWRejLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		
		if (poHeaderTWRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		if (wbTWRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (statusRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (regRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		
		int tr = 0; 
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");

		VtiUserExitScreenTableRow wghbr = scrTblCustom.getRow(tr);
		VtiUserExitScreenTableRow trckReg = scrTblCustom.getRow(tr + 1);
		VtiUserExitScreenTableRow driv = scrTblCustom.getRow(tr + 2);
		VtiUserExitScreenTableRow custsupp = scrTblCustom.getRow(tr + 3);
		VtiUserExitScreenTableRow transprtr = scrTblCustom.getRow(tr + 4);
		VtiUserExitScreenTableRow tranType = scrTblCustom.getRow(tr + 5);
		VtiUserExitScreenTableRow allwgh = scrTblCustom.getRow(tr + 6);
		VtiUserExitScreenTableRow ordNo = scrTblCustom.getRow(tr + 7);
		VtiUserExitScreenTableRow rebag = scrTblCustom.getRow(tr + 8);
		VtiUserExitScreenTableRow packline = scrTblCustom.getRow(tr + 9);
		VtiUserExitScreenTableRow packLoad = scrTblCustom.getRow(tr + 10);
		VtiUserExitScreenTableRow segtype = scrTblCustom.getRow(tr + 11);
		VtiUserExitScreenTableRow shift = scrTblCustom.getRow(tr + 12);
		VtiUserExitScreenTableRow remarks = scrTblCustom.getRow(tr + 13);
		VtiUserExitScreenTableRow tralnum = scrTblCustom.getRow(tr + 14);
		
		String truck = trckReg.getFieldValue("FIELDVALUE");
		String product = "";
		String type = tranType.getFieldValue("FIELDVALUE");
		double allocWght = allwgh.getDoubleFieldValue("FIELDVALUE");
		String pckline = packline.getFieldValue("FIELDVALUE");
		String user = "";
		String driver = driv.getFieldValue("FIELDVALUE");
		String pshift = shift.getFieldValue("FIELDVALUE");
		
		
		boolean isTransfer = false;
		String docType = "EBELN";
		
		DBCalls dbCall = new DBCalls();
		
		if(scrWTransfer.getFieldValue().equalsIgnoreCase("UB"))
		{
			isTransfer = true;
			docType = "STOCKTRNF";
		}
		
		if(scrWFRejReason.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "Please select a rejection reason.");
		//Set next Function
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		if(isTransfer)
		{
			sessionHeader.setNextFunctionId("YSWB_TRANSFER");
		}
		else
		{
			sessionHeader.setNextFunctionId("YSWB_TRINBOUND");
		}
		//Dataset Declaration
		VtiExitLdbSelectCriterion [] poHeaderTWRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.CS_OPERATOR, scrWTransfer.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poHeaderTWRejSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderTWRejSelConds, true);
		VtiExitLdbTableRow[] poHeaderTWRejLdbRows = poHeaderTWRejLdbTable.getMatchingRows(poHeaderTWRejSelCondGrp);

		if(poHeaderTWRejLdbRows.length == 0)
			return new VtiUserExitResult(999, "No purchase order detail exist.");
		
		
		VtiExitLdbSelectCriterion [] statusRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statusRejSelCondGrp = new VtiExitLdbSelectConditionGroup(statusRejSelConds, true);
		VtiExitLdbTableRow[] statusRejLdbRows = statusRejLdbTable.getMatchingRows(statusRejSelCondGrp);

		if(statusRejLdbRows.length == 0)
			return new VtiUserExitResult(999, "No order detail exist in the Status table.");
		
		
		String poStatus = statusRejLdbRows[0].getFieldValue("WGH_STATUS");
		
		if(poStatus.equalsIgnoreCase("Failed") || poStatus.equalsIgnoreCase("Complete") 
				||poStatus.equalsIgnoreCase("New")  || poStatus.substring(0,1).equalsIgnoreCase("0"))
						return new VtiUserExitResult(999,"No processing allowed, check status.");
		//Set Reject Status
		statusRejLdbRows[0].setFieldValue("WGH_STATUS","Rejected");
		statusRejLdbRows[0].setFieldValue("STATUS","A");
		statusRejLdbRows[0].setFieldValue("TIMESTAMP","");
		
		//Update Register. Change back to Assigned status
		
		VtiExitLdbSelectCriterion [] regRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup regRejSelCondGrp = new VtiExitLdbSelectConditionGroup(regRejSelConds, true);
		VtiExitLdbTableRow[] regRejLdbRows = regRejLdbTable.getMatchingRows(regRejSelCondGrp);

		if(regRejLdbRows.length == 0)
			return new VtiUserExitResult(999, "No order detail exist in the Status table.");
		

		regRejLdbRows[0].setFieldValue("INSPSTATUS","A");
		regRejLdbRows[0].setFieldValue("TIMESTAMP","");
		
		VtiExitLdbSelectCriterion [] wbTWRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup wbTWRejSelCondGrp = new VtiExitLdbSelectConditionGroup(wbTWRejSelConds, true);
		VtiExitLdbTableRow[] wbTWRejLdbRows = wbTWRejLdbTable.getMatchingRows(wbTWRejSelCondGrp);

		if(wbTWRejLdbRows.length == 0)
		{
			//Create weight transactions in YSWB_WB
			VtiExitLdbTableRow ldbRowWeigh1 = wbTWRejLdbTable.newRow();
			
			//Populate TBL Fields
			ldbRowWeigh1.setFieldValue("SERVERGROUP", getServerGroup());
			ldbRowWeigh1.setFieldValue("SERVERID", getServerId());
			ldbRowWeigh1.setFieldValue("TRANDATE", currLdbDate);
			ldbRowWeigh1.setFieldValue("VTIREF", scrSlipNo.getFieldValue());
			ldbRowWeigh1.setFieldValue("TRUCKREG", scrRegNo.getFieldValue());
			ldbRowWeigh1.setFieldValue("VTIREFA",poHeaderTWRejLdbRows[0].getFieldValue("VTIREF"));
			ldbRowWeigh1.setFieldValue("VBELN", scrWFPurchOrd.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLDBTime);
			ldbRowWeigh1.setFieldValue("PRINTFLAG", "X");
			ldbRowWeigh1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
			ldbRowWeigh1.setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRANSPORTTYPE",tranType.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("ALLOC_WHT",allwgh.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("DELVNO",ordNo.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("REBAG",rebag.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("REMARKS","WB rejected before weigh done.");
			ldbRowWeigh1.setFieldValue("USERID",sessionHeader.getUserId());
			ldbRowWeigh1.setFieldValue("TIMESTAMP","");
			
			ldbRowWeigh1.setFieldValue("STATUS","Rejected");
			ldbRowWeigh1.setFieldValue("REJFLAG","X");
			ldbRowWeigh1.setFieldValue("REJTIME",currLDBTime);
			ldbRowWeigh1.setFieldValue("REJRSN",scrWFRejReason.getFieldValue());

			
			try
			{
				wbTWRejLdbTable.saveRow(ldbRowWeigh1);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save rejection details.");
			}
		}
		else
		{		
			wbTWRejLdbRows[0].setFieldValue("SHIFT",pshift);
			wbTWRejLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
			wbTWRejLdbRows[0].setFieldValue("STATUS","Rejected");
			wbTWRejLdbRows[0].setFieldValue("REJFLAG","X");
			wbTWRejLdbRows[0].setFieldValue("REJTIME",currLDBTime);
			wbTWRejLdbRows[0].setFieldValue("REJRSN",scrWFRejReason.getFieldValue());
			wbTWRejLdbRows[0].setFieldValue("TIMESTAMP","");
			wbTWRejLdbRows[0].setFieldValue("ALLOC_WHT",allocWght);
			wbTWRejLdbRows[0].setFieldValue("DRIVER",driver);
			wbTWRejLdbRows[0].setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
	
			try
			{
				wbTWRejLdbTable.saveRow(wbTWRejLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save rejections details.");
			}
		}
		
		try
		{
			regRejLdbTable.saveRow(regRejLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to reject this order.",ee);
			return new VtiUserExitResult(999,"Unable to reject this order.");
		}
		
		try
		{
			statusRejLdbTable.saveRow(statusRejLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to reject this order.",ee);
			return new VtiUserExitResult(999,"Unable to reject this order.");
		}
		
/*		try
		{
			wbTWRejLdbTable.saveRow(wbTWRejLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to reject this order.",ee);
			return new VtiUserExitResult(999,"Unable to reject this order.");
		}*/
		
			VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "Complete"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)
			{
			
				qTLdbRows[0].setFieldValue("Q_STATUS","Rejected");
				qTLdbRows[0].setFieldValue("TIMESTAMP","");

			
				try
				{
					queueLdbTable.saveRow(qTLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
						Log.error("Error updating queue status, please correct in the queue.",ee);
					return new VtiUserExitResult(000,"Unable to update the queue.");
				}
			}
			
		try
		{
			boolean hostConnected = isHostInterfaceConnected(hostName);

			if (hostConnected)
			{ 
			
				dbCall.ldbUpload("YSWB_WB", this);
				dbCall.ldbUpload("YSWB_REGISTER", this);
				dbCall.ldbUpload("YSWB_STATUS", this);
				dbCall.ldbUpload("YSWB_QUEUE", this);
			}
			
		}
		catch (VtiExitException ee)
		{
			Log.error("Error updating weighin status, please try again.",ee);
			return new VtiUserExitResult(999,"Unable to update Sales Order, check status.");
		}
		StringBuffer Header = new StringBuffer();
		StringBuffer addDet = new StringBuffer();
		
		Header.append("NAIROBI GRINDING PLANT");
		addDet.append("P.O Box 524, ATHI RIVER");
		
		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", Header.toString()),
				new VtiExitKeyValuePair("&addDet&", addDet.toString()),
				new VtiExitKeyValuePair("&soTime&", currLDBTime),
				new VtiExitKeyValuePair("&truck&", truck),
				new VtiExitKeyValuePair("&pType&", type),
				new VtiExitKeyValuePair("&allocWght&", Double.toString(allocWght)),
				new VtiExitKeyValuePair("&product&", scrWFPurchOrd.getFieldValue()),
				new VtiExitKeyValuePair("&w1&", scrFWeight1.getFieldValue()),
				new VtiExitKeyValuePair("&w2&", scrFWeight2.getFieldValue()),
				new VtiExitKeyValuePair("&nett&", scrFNett.getFieldValue()),
				new VtiExitKeyValuePair("&rejreason&",scrWFRejReason.getStringFieldValue()),
				new VtiExitKeyValuePair("&user&", sessionHeader.getUserId()),
				new VtiExitKeyValuePair("&driver&", driver),
				new VtiExitKeyValuePair("&pl&", pckline),
			};
					
			VtiUserExitHeaderInfo headerInfo = getHeaderInfo();		
			int deviceNumber = headerInfo.getDeviceNumber();

			//Invoking the print
				try
				{
					invokePrintTemplate("WBRej" + deviceNumber, keyValuePairs);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error with Printout", ee);
				}
				
		return new VtiUserExitResult();
	}	
}
