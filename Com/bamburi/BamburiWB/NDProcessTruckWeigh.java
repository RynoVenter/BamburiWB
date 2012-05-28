package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class NDProcessTruckWeigh extends VtiUserExit
{/*Procees the weight reading from the weighbridge according to weither it is the 1st weight or second weight.
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		float w1 = 0;
		float w2 = 0;
		float nett = 0;
		String weighTS = currDate + " " + currTime;
		String currStatus = "";
		
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrRBWeigh1 = getScreenField("RB_WEIGH1");
		VtiUserExitScreenField scrRBWeigh2 = getScreenField("RB_WEIGH2");
		VtiUserExitScreenField scrFWeight = getScreenField("WEIGHT");
		VtiUserExitScreenField scrFWeight1 = getScreenField("WEIGHT1");
		VtiUserExitScreenField scrFWTStamp1 = getScreenField("WGH1_TIMESTAMP");
		VtiUserExitScreenField scrFWeight2 = getScreenField("WEIGHT2");
		VtiUserExitScreenField scrFWTStamp2 = getScreenField("WGH2_TIMESTAMP");
		VtiUserExitScreenField scrChkPrn = getScreenField("CHK_PRINT");
		VtiUserExitScreenField scrFNettW = getScreenField("NETT_WEIGHT");
		VtiUserExitScreenField scrFNettTS = getScreenField("NETT_TIMESTAMP");
		VtiUserExitScreenField scrCBBridge = getScreenField("WEIGHBRIDGE");
		VtiUserExitScreenField scrRBFull = getScreenField("RB_FULL");
		VtiUserExitScreenField scrRBPartial = getScreenField("RB_PARTIAL");
		VtiUserExitScreenField btnSave = getScreenField("BT_SAVE");
		VtiUserExitScreenField scrFIsStuck = getScreenField("IS_STUCK");	
		VtiUserExitScreenField btnBack = getScreenField("BT_BACK");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrMatDisc = getScreenField("ARKTX");
		VtiUserExitScreenField scrOrigin = getScreenField("ORIGIN");
		VtiUserExitScreenField scrDestination = getScreenField("DESTINATION");
		VtiUserExitScreenField scrCostCentre = getScreenField("KOSTL");
		VtiUserExitScreenField scrPlant = getScreenField("WERKS");
		VtiUserExitScreenField scrVendor = getScreenField("LIFNR");
		VtiUserExitScreenField scrSloc = getScreenField("LGORT");
		VtiUserExitScreenField scrMatNr = getScreenField("MATNR");
		VtiUserExitScreenField scrGL = getScreenField("GENERALLEDGER");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");


		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrRBWeigh1 == null) return new VtiUserExitResult (999,"Failed to initialise RB_WEIGH1.");
		if(scrRBWeigh2 == null) return new VtiUserExitResult (999,"Failed to initialise RB_WEIGH2.");
		if(scrFWeight == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT.");
		if(scrFWeight1 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT1.");
		if(scrFWTStamp1 == null) return new VtiUserExitResult (999,"Failed to initialise WGH1_TIMESTAMP.");
		if(scrFWeight2 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT2.");
		if(scrFWTStamp2 == null) return new VtiUserExitResult (999,"Failed to initialise WGH2_TIMESTAMP.");
		if(scrChkPrn == null) return new VtiUserExitResult (999,"Failed to initialise CHK_PRINT.");
		if(scrFNettW == null) return new VtiUserExitResult (999,"Failed to initialise NETT_WEIGHT.");
		if(scrFNettTS == null) return new VtiUserExitResult (999,"Failed to initialise NETT_TIMESTAMP.");
		if(scrCBBridge == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHBRIDGE.");
		if(scrRBFull == null) return new VtiUserExitResult (999,"Failed to initialise RB_FULL.");
		if(scrRBPartial == null) return new VtiUserExitResult (999,"Failed to initialise RB_PARTIAL.");
		if(btnSave == null) return new VtiUserExitResult (999,"Failed to initialise SAVE.");
		if(scrFIsStuck == null) return new VtiUserExitResult (999,"Failed to initialise IS_STUCK.");
		if(btnBack == null) return new VtiUserExitResult (999,"Failed to initialise BT_BACK.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		if(scrMatDisc == null) return new VtiUserExitResult (999,"Failed to initialise ARKTX .");
		if(scrOrigin == null) return new VtiUserExitResult (999,"Failed to initialise ORIGIN .");
		if(scrDestination == null) return new VtiUserExitResult (999,"Failed to initialise DESTINATION .");
		if(scrMatNr == null) return new VtiUserExitResult (999,"Failed to initialise MATNR .");
		if(scrCostCentre == null) return new VtiUserExitResult (999,"Failed to initialise KOSTL .");
		if(scrPlant == null) return new VtiUserExitResult (999,"Failed to initialise WERKS .");
		if(scrVendor == null) return new VtiUserExitResult (999,"Failed to initialise LIFNR .");
		if(scrSloc == null) return new VtiUserExitResult (999,"Failed to initialise LGORT .");
		if(scrGL == null) return new VtiUserExitResult (999,"Failed to initialise GENERALLEDGER .");
		
		if(scrFWeight.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "No weight.");
		
		if(scrFIsStuck.getFieldValue().equalsIgnoreCase("X"))
		{
			btnBack.setHiddenFlag(true);
		}
		
		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Purchase Order is not ready for processing, please check the status.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
			if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
			
		if(scrFNettW.getFieldValue().length() > 0)
			return new VtiUserExitResult(999, "This truck has been weighed a second time, please reject the weight if it was inconrrect.");
		
		if(scrOrigin.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate Origin.");

		//Database TBL Declaration
		VtiExitLdbTable wbTWLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable queueLdbTable =  getLocalDatabaseTable("YSWB_QUEUE");
					
		if (wbTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		
		//Dataset Declaration
		VtiExitLdbSelectCriterion [] regSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

		};
			
		VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
		VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
		
		if(regLdbRows.length == 0)
			return new VtiUserExitResult(999, "Please check the inspection registration.");
		
		if(scrDestination.getFieldValue().length() == 0 
		   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate Destination.");
			
		if(scrMatNr.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate Material.");
		
		if(scrVendor.getFieldValue().length() == 0
		   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate Vendor.");
		
		if(scrSloc.getFieldValue().length() == 0
		   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate SLOC.");
		
		if(scrCostCentre.getFieldValue().length() == 0
		   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate Cost Centre.");
		
		if(scrGL.getFieldValue().length() == 0
		   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
			return new VtiUserExitResult(999,1,"Please indicate GL.");
		
		VtiExitLdbSelectCriterion [] wbSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
			
		VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
		VtiExitLdbTableRow[] wbLdbRows = wbTWLdbTable.getMatchingRows(wbSelCondGrp);

		if(wbLdbRows.length == 0)
			currStatus = "ASSIGNED";
		else
			currStatus = wbLdbRows[0].getFieldValue("STATUS");
		
		VtiExitLdbSelectCriterion [] regisSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
			
		VtiExitLdbSelectConditionGroup regisSelCondGrp = new VtiExitLdbSelectConditionGroup(regisSelConds, true);
		VtiExitLdbTableRow[] regisLdbRows = registerLdbTable.getMatchingRows(regisSelCondGrp);
		
		if(regLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("F"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
			
		VtiExitLdbSelectCriterion [] statHeaderSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup statHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(statHeaderSelConds, true);
		VtiExitLdbTableRow[] statHeaderLdbRows = statusLdbTable.getMatchingRows(statHeaderSelCondGrp);

		if(statHeaderLdbRows.length == 0)
			return new VtiUserExitResult(999,"Truck not found in the Status table.");
		
		if(statHeaderLdbRows.length != 0)
		{
			if(statHeaderLdbRows[0].getFieldValue("ROTATE").equalsIgnoreCase("X"))
			{
				if(scrOrigin.getFieldValue().length() == 0 ||
					scrDestination.getFieldValue().length() == 0 ||
					scrMatNr.getFieldValue().length() == 0)
				{
					return new VtiUserExitResult (999,1,"Please indicate the Origin, Destination and Material carried by the truck.");
				}
			}
			
		}
					
		if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
		{
			if(scrRBPartial.getFieldValue().equalsIgnoreCase("X"))
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight1.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight1.setFieldValue(scrFWeight.getFieldValue() + scrFWeight1.getFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
				return new VtiUserExitResult(999, "Partial weight collected, move truck and take the next weight. Remember to select ''Full'' when taking the last weight.");
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight1.getFieldValue().length()!=0)
			{
				scrFWeight1.setFieldValue(scrFWeight.getFieldValue() + scrFWeight1.getFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight1.getFieldValue().length()==0)
			{
				scrFWeight1.setFieldValue(scrFWeight.getFieldValue());			
				scrFWTStamp1.setFieldValue(weighTS);
			}
			
			//Set weight and time fields
			if(scrFWeight1.getFieldValue().length() == 0)
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight1.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight1.setFieldValue(scrFWeight.getFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}

			VtiExitLdbSelectCriterion [] configSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "TONSPERAXLE"),
				new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, scrNoAxels.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
			VtiExitLdbTableRow[] configLdbRows = configLdbTable.getMatchingRows(configSelCondGrp);
		
				if(configLdbRows.length == 0)
				{
					return new VtiUserExitResult (999,"Maximum weight per axle not configured.");		
				}
			
				int axleVariance  = configLdbRows[0].getIntegerFieldValue("KEYVAL3") ;
				int maxLegalWght = configLdbRows[0].getIntegerFieldValue("KEYVAL1");

					
				if(axleVariance > 0)
					maxLegalWght = (maxLegalWght * axleVariance) / 100 + maxLegalWght;
					
				if(maxLegalWght < scrFWeight1.getFloatFieldValue())
				{
					return new VtiUserExitResult(999, "The total truck weight exceeds the legal axle weight for this truck by " + (scrFWeight2.getFloatFieldValue() - maxLegalWght));
				}
					

				
			//Create weight transactions in YSWB_WB
			VtiExitLdbTableRow ldbRowWeigh1 = wbTWLdbTable.newRow();
			
			//Populate TBL Fields
			ldbRowWeigh1.setFieldValue("SERVERGROUP", getServerGroup());
			ldbRowWeigh1.setFieldValue("SERVERID", getServerId());
			ldbRowWeigh1.setFieldValue("TRANDATE", currLdbDate);
			ldbRowWeigh1.setFieldValue("VTIREF", scrFSlip.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLdbTime);
			ldbRowWeigh1.setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
			ldbRowWeigh1.setFieldValue("STATUS", "Weigh 1");
			ldbRowWeigh1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
			ldbRowWeigh1.setFieldValue("USERID",sessionHeader.getUserId());
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
				wbTWLdbTable.saveRow(ldbRowWeigh1);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save weighin to Sales Order.");
			}
			
			VtiExitLdbSelectCriterion [] registerSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "A"),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
						
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				
			VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
		
			if(registerLdbRows.length == 0)
				return new VtiUserExitResult(999,"The trucks register status could not be updated.");
			
			registerLdbRows[0].setFieldValue("INSPSTATUS","W");
			registerLdbRows[0].setFieldValue("TIMESTAMP", "");
			
			try
			{
				registerLdbTable.saveRow(registerLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin status for the register, please try again.",ee);
				return new VtiUserExitResult(999,"The trucks register status could not be updated.");
			}
		
			statHeaderLdbRows[0].setFieldValue("WGH_STATUS","Weigh 1");
			statHeaderLdbRows[0].setFieldValue("STATUS","W");
			statHeaderLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				statusLdbTable.saveRow(statHeaderLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin status for the register, please try again.",ee);
				return new VtiUserExitResult(999,"The trucks register status could not be updated.");
			}
			
			VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)
			{
				qTLdbRows[0].setFieldValue("Q_STATUS","Weigh 1");
				qTLdbRows[0].setFieldValue("TIMESTAMP","");
					
				try
				{
					queueLdbTable.saveRow(qTLdbRows[0]);
				}
				catch ( VtiExitException ee)
				{
					Log.error("Error archiving queue.", ee);
				}
			}
				
			sessionHeader.setNextFunctionId("YSWB_MAIN");
		}
		else
		{
			if(scrRBPartial.getFieldValue().equalsIgnoreCase("X"))
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight2.setFieldValue(scrFWeight.getFieldValue() + scrFWeight2.getFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
				return new VtiUserExitResult(999, "Partial weight collected, move truck and take the next weight. Remeber to select Full when taking the last weight..");
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight2.getFieldValue().length()!=0)
			{
				scrFWeight2.setFieldValue(scrFWeight.getFieldValue() + scrFWeight2.getFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight2.getFieldValue().length()==0)
			{
				scrFWeight2.setFieldValue(scrFWeight.getFieldValue());	
				scrFWTStamp2.setFieldValue(weighTS);
			}
			
			//Set weight and time fields
			if(scrFWeight2.getFieldValue().length() == 0)
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight2.setFieldValue(scrFWeight.getFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
			}
			
			//Update weight transaction in YSWB_WB

			VtiExitLdbSelectCriterion [] wbTWSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup wbTWSelCondGrp = new VtiExitLdbSelectConditionGroup(wbTWSelConds, true);
			VtiExitLdbTableRow[] ldbRowWeigh2 = wbTWLdbTable.getMatchingRows(wbTWSelCondGrp);

			if(ldbRowWeigh2.length == 0)
				new VtiUserExitResult(999,"No valid rows availible to update in YSWB_WB.");
			//Populate LDB Fields 
			

				ldbRowWeigh2[0].setFieldValue("WEIGHT2", scrFWeight2.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("WEIGHT2_D", currLdbDate);
				ldbRowWeigh2[0].setFieldValue("WEIGHT2_T", currLdbTime);
				ldbRowWeigh2[0].setFieldValue("EBELN",scrEbeln.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("STATUS", "Weigh 2");
				ldbRowWeigh2[0].setFieldValue("USERID",sessionHeader.getUserId());
				ldbRowWeigh2[0].setFieldValue("TIMESTAMP", "");
				ldbRowWeigh2[0].setFieldValue("ORIGIN",scrOrigin.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("DEST",scrDestination.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("MATNR",scrMatNr.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("EBELN",scrEbeln.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("KOSTL",scrCostCentre.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("WERKS",scrPlant.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("LIFNR",scrVendor.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("LGORT",scrSloc.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("SAKNR",scrGL.getFieldValue());
				
				
				//Calculate Nettweight and write to screen
				w1 = scrFWeight1.getFloatFieldValue();
				w2 = scrFWeight2.getFloatFieldValue();
				
				nett =  w2 - w1;
				
				
				if(nett < 0)
					nett = nett * -1;
				
				scrFNettW.setFieldValue(nett);
				scrFNettTS.setFieldValue(currTime);
						
				//Check truck tolerence before allowing the save 
				
				VtiExitLdbSelectCriterion [] configSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "TONSPERAXLE"),
					new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, scrNoAxels.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
				VtiExitLdbTableRow[] configLdbRows = configLdbTable.getMatchingRows(configSelCondGrp);
		
				if(configLdbRows.length == 0)
				{
					return new VtiUserExitResult (999,"Maximum weight per axle not configured.");		
				}
				
				int axleVariance  = configLdbRows[0].getIntegerFieldValue("KEYVAL2") ;
				int maxLegalWght = configLdbRows[0].getIntegerFieldValue("KEYVAL1");

				
				if(axleVariance > 0)
					maxLegalWght = (maxLegalWght * axleVariance) / 100 + maxLegalWght;
				
				if(maxLegalWght < scrFWeight2.getFloatFieldValue())
				{
					return new VtiUserExitResult(999, "The Gross Vehicle Weight exceeds the legal axle weight for this truck by " + (scrFWeight2.getIntegerFieldValue() - maxLegalWght));
				}
				
				//End Tolerence check
				
				//Save Nett Weight
				ldbRowWeigh2[0].setFloatFieldValue("NETTWEIGHT", nett);
				ldbRowWeigh2[0].setFieldValue("NETTWEIGHT_T", currLdbTime);
				ldbRowWeigh2[0].setFieldValue("TIMESTAMP", "");
			
				try
				{
					wbTWLdbTable.saveRow(ldbRowWeigh2[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating weighin, please try again.",ee);
					return new VtiUserExitResult(999,"Unable to save Nett Weight data to the Purchase Order.");
				}
				
				statHeaderLdbRows[0].setFieldValue("WGH_STATUS","Weigh 2");
				statHeaderLdbRows[0].setFieldValue("STATUS","W");
				statHeaderLdbRows[0].setFieldValue("TIMESTAMP","");
			
				try
				{
					statusLdbTable.saveRow(statHeaderLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating weighin status for the register, please try again.",ee);
					return new VtiUserExitResult(999,"The trucks register status could not be updated.");
				}
				
				VtiExitLdbSelectCriterion [] qSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
				VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
				if(qTLdbRows.length > 0)
				{
					qTLdbRows[0].setFieldValue("Q_STATUS","Weigh 2");
					qTLdbRows[0].setFieldValue("TIMESTAMP","");
						
					try
					{
						queueLdbTable.saveRow(qTLdbRows[0]);
					}
					catch ( VtiExitException ee)
					{
						Log.error("Error archiving queue.", ee);
					}
				}
			
				btnSave.setHiddenFlag(false);
			
		}
		
		return new VtiUserExitResult();
	}
}
