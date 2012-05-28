package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class NDGetTareWeight extends VtiUserExit 
{/*Get the Tare weight for the PO from the WB table as per the 1st weight.
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
			//Generate Slip number into vti_ref field from number range object from yswb_slip
		VtiUserExitScreenField scrFWeight1 = getScreenField("WEIGHT1");
		VtiUserExitScreenField scrFWTStamp1 = getScreenField("WGH1_TIMESTAMP");
		VtiUserExitScreenField btnReject = getScreenField("BT_REJECT");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrRBWeigh1 = getScreenField("RB_WEIGH1");
		VtiUserExitScreenField scrRBWeigh2 = getScreenField("RB_WEIGH2");
		VtiUserExitScreenField scrFWeight = getScreenField("WEIGHT");
		VtiUserExitScreenField scrFWeight2 = getScreenField("WEIGHT2");
		VtiUserExitScreenField scrFWTStamp2 = getScreenField("WGH2_TIMESTAMP");
		VtiUserExitScreenField scrChkPrn = getScreenField("CHK_PRINT");
		VtiUserExitScreenField scrFNettW = getScreenField("NETT_WEIGHT");
		VtiUserExitScreenField scrFNettTS = getScreenField("NETT_TIMESTAMP");
		VtiUserExitScreenField scrCBBridge = getScreenField("WEIGHBRIDGE");
		VtiUserExitScreenField scrRBFull = getScreenField("RB_FULL");
		VtiUserExitScreenField scrRBPartial = getScreenField("RB_PARTIAL");
		VtiUserExitScreenField btnSave = getScreenField("BT_SAVE");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrMatDisc = getScreenField("ARKTX");
		VtiUserExitScreenField scrOrigin = getScreenField("ORIGIN");
		VtiUserExitScreenField scrDestination = getScreenField("DESTINATION");
		VtiUserExitScreenField scrCostCentre = getScreenField("KOSTL");
		VtiUserExitScreenField scrPlant = getScreenField("WERKS");
		VtiUserExitScreenField scrVendor = getScreenField("LIFNR");
		VtiUserExitScreenField scrSloc = getScreenField("LGORT");
		VtiUserExitScreenField scrMatNr = getScreenField("MATNR");
		VtiUserExitScreenField scrGL = getScreenField("GENERALLEDGER");

		
		if(btnReject == null) return new VtiUserExitResult (999,"Failed to initialise BT_REJECT.");
		if(scrFWeight1 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT1.");
		if(scrFWTStamp1 == null) return new VtiUserExitResult (999,"Failed to initialise WGH1_TIMESTAMP.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrRBWeigh1 == null) return new VtiUserExitResult (999,"Failed to initialise RB_WEIGH1.");
		if(scrRBWeigh2 == null) return new VtiUserExitResult (999,"Failed to initialise RB_WEIGH2.");
		if(scrFWeight == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT.");
		if(scrFWeight2 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT2.");
		if(scrFWTStamp2 == null) return new VtiUserExitResult (999,"Failed to initialise WGH2_TIMESTAMP.");
		if(scrChkPrn == null) return new VtiUserExitResult (999,"Failed to initialise CHK_PRINT.");
		if(scrFNettW == null) return new VtiUserExitResult (999,"Failed to initialise NETT_WEIGHT.");
		if(scrFNettTS == null) return new VtiUserExitResult (999,"Failed to initialise NETT_TIMESTAMP.");
		if(scrCBBridge == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHBRIDGE.");
		if(scrRBFull == null) return new VtiUserExitResult (999,"Failed to initialise RB_FULL.");
		if(scrRBPartial == null) return new VtiUserExitResult (999,"Failed to initialise RB_PARTIAL.");
		if(btnSave == null) return new VtiUserExitResult (999,"Failed to initialise BT_SAVE.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");

		
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");

		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		
		String poStatus ="";
		
		FormatUtilities fu = new FormatUtilities();
		
		VtiExitLdbSelectCriterion [] regValSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

		};
			
		VtiExitLdbSelectConditionGroup regValSelCondGrp = new VtiExitLdbSelectConditionGroup(regValSelConds, true);
		VtiExitLdbTableRow[] regValLdbRows = registerLdbTable.getMatchingRows(regValSelCondGrp);
		
		if(regValLdbRows.length == 0)
			return new VtiUserExitResult(999, "Please check the inspection registration.");
		
		if(scrDestination.getFieldValue().length() == 0 
		   && !regValLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate Destination.");
			
		if(scrMatNr.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate Material.");
		
		if(scrVendor.getFieldValue().length() == 0
		   && !regValLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate Vendor.");
		
		if(scrSloc.getFieldValue().length() == 0
		   && !regValLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate SLOC.");
		
		if(scrCostCentre.getFieldValue().length() == 0
		   && !regValLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate Cost Centre.");
		
		if(scrGL.getFieldValue().length() == 0
		   && !regValLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate GL.");
		
		if(scrOrigin.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate Origin.");
			
			VtiExitLdbSelectCriterion [] wbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Rejected"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
			
			VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("VTIREF",false),
			};
						
			VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp,orderBy);

			if(wbLdbRows.length == 0)
				return new VtiUserExitResult(999,"No previous weigh bridge data.");
			
			if(wbLdbRows[0].getLongFieldValue("WEIGHT1") == 0)
				return new VtiUserExitResult(999,1,"Tare weight is zero, retake weigh 1.");
			
			scrFWeight1.setIntegerFieldValue(wbLdbRows[0].getIntegerFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbLdbRows[0].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime(wbLdbRows[0].getFieldValue("WEIGHT1_T")));

				//Declarations of variables and elements. Followed by the checking of the elements.
			
		//Variable Declarations
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		float w1 = 0;
		float w2 = 0;
		float nett = 0;
		String weighTS = currDate + " " + currTime;
		
		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Order is not ready for processing, please check the status.");
		

		//Database TBL Declaration
		VtiExitLdbTable wbTWLdbTable = getLocalDatabaseTable("YSWB_WB");
		
		if (wbTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		
		VtiExitLdbSelectCriterion [] regSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
			
		VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
		VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
		
		if(regLdbRows.length == 0)
			return new VtiUserExitResult(999, "No vehicle registration.");
		
		if(regLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("F"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
			
		if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
		{
			//Set weight and time fields
			if(scrFWeight1.getFieldValue().length() == 0)
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight1.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight1.setIntegerFieldValue(scrFWeight.getIntegerFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}
			
			//update status
			regLdbRows[0].setFieldValue("INSPSTATUS","W");
			regLdbRows[0].setFieldValue("TIMESTAMP","");
			
			VtiExitLdbSelectCriterion [] statusRejSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "REJECTED"),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statusRejSelCondGrp = new VtiExitLdbSelectConditionGroup(statusRejSelConds, true);
			VtiExitLdbTableRow[] statusRejLdbRows = statusLdbTable.getMatchingRows(statusRejSelCondGrp);
		
			if(statusRejLdbRows.length == 0)
				return new VtiUserExitResult(999,1,"Details of this trucks status is incorrect, please investigate.");
			
			statusRejLdbRows[0].setFieldValue("STATUS","W");
			statusRejLdbRows[0].setFieldValue("WGH_STATUS","Weigh 1");
			statusRejLdbRows[0].setFieldValue("TIMESTAMP","");
			
			VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "Complete"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)
			{			
				qTLdbRows[0].setFieldValue("Q_STATUS","Weigh 1");
				qTLdbRows[0].setFieldValue("TIMESTAMP", "");
			
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
			
			//Create weight transactions in YSWB_WB
			VtiExitLdbTableRow ldbRowWeigh1 = wbTWLdbTable.newRow();
			
			//Populate TBL Fields
			ldbRowWeigh1.setFieldValue("SERVERGROUP", getServerGroup());
			ldbRowWeigh1.setFieldValue("SERVERID", getServerId());
			ldbRowWeigh1.setFieldValue("TRANDATE", currLdbDate);
			ldbRowWeigh1.setFieldValue("VTIREF", scrFSlip.getFieldValue());
			ldbRowWeigh1.setFieldValue("EBELN",scrEbeln.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLdbTime);
			ldbRowWeigh1.setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
			ldbRowWeigh1.setFieldValue("STATUS", "Weigh 1");
			ldbRowWeigh1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
			ldbRowWeigh1.setFieldValue("TRUCKREG",scrRegNo.getFieldValue());
			ldbRowWeigh1.setFieldValue("VTIREFA",scrVRef.getFieldValue());
			ldbRowWeigh1.setFieldValue("ORIGIN",scrOrigin.getFieldValue());
			ldbRowWeigh1.setFieldValue("DEST",scrDestination.getFieldValue());
			ldbRowWeigh1.setFieldValue("MATNR",scrMatNr.getFieldValue());
			ldbRowWeigh1.setFieldValue("EBELN",scrEbeln.getFieldValue());
			ldbRowWeigh1.setFieldValue("KOSTL",scrCostCentre.getFieldValue());
			ldbRowWeigh1.setFieldValue("WERKS",scrPlant.getFieldValue());
			ldbRowWeigh1.setFieldValue("LIFNR",scrVendor.getFieldValue());
			ldbRowWeigh1.setFieldValue("LGORT",scrSloc.getFieldValue());
			ldbRowWeigh1.setFieldValue("SAKNR",scrGL.getFieldValue());
			
			try
			{
				registerLdbTable.saveRow(regLdbRows[0]);
				statusLdbTable.saveRow(statusRejLdbRows[0]);
				wbTWLdbTable.saveRow(ldbRowWeigh1);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save weighin to the Order.");
			}
			
				scrRBWeigh2.setFieldValue("X");
				scrRBWeigh2.setDisplayOnlyFlag(false);
				scrRBWeigh1.setFieldValue("");
				scrRBWeigh1.setDisplayOnlyFlag(true);
				btnSave.setHiddenFlag(false);
				
		}
		btnReject.setHiddenFlag(false);
		return new VtiUserExitResult();
	}

}
