package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ProcessTruckWeigh extends VtiUserExit
{/*Procees the weight reading from the weighbridge according to weither it is the 1st weight or second weight.
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.
		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrWFSalesOrd = getScreenField("VBELN");
		VtiUserExitScreenField scrTruckReg = getScreenField("TRUCK_REG");
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
		VtiUserExitScreenField scfTolWarning = getScreenField("TOLL_MESSAGE");
		VtiUserExitScreenField scrCBBridge = getScreenField("WEIGHBRIDGE");
		VtiUserExitScreenField scrRBFull = getScreenField("RB_FULL");
		VtiUserExitScreenField scrRBPartial = getScreenField("RB_PARTIAL");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");
		
		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		if(scrWFSalesOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		if(scrTruckReg == null) return new VtiUserExitResult (999,"Failed to initialise TRUCK_REG.");
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
		if(scfTolWarning == null) return new VtiUserExitResult (999,"Failed to initialise TOLL_MESSAGE.");
		if(scrCBBridge == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHBRIDGE.");
		if(scrRBFull == null) return new VtiUserExitResult (999,"Failed to initialise RB_FULL.");
		if(scrRBPartial == null) return new VtiUserExitResult (999,"Failed to initialise RB_PARTIAL.");
		
		if(scrFWeight.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "No weight.");
		
		if(scrFWeight.getFieldValue().equalsIgnoreCase(scrFWeight1.getFieldValue()))
			return new VtiUserExitResult(999,1,"Weigh 1 and bridge weight is the same.");
		   
		scfTolWarning.setHiddenFlag(true);
		//Variable Declarations
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		long w1 = 0;
		long w2 = 0;
		long nett = 0;
		
		DBCalls dbCall = new DBCalls();
		
		String weighTS = currDate + " " + currTime;
		
		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		boolean hostConnected = isHostInterfaceConnected(hostName);
		
		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Sales Order is not ready for processing, please check the status.");
		
		if(scrFWeight.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "No weight.");
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
			if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
			
		if(scrFNettW.getFieldValue().length() > 0)
			return new VtiUserExitResult(999, "This truck has been weighed a second time, please reject the weight if it was inconrrect.");
			
		//Database TBL Declaration
		VtiExitLdbTable soHeaderTWLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable soItemsTWLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable wbTWLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");

		if (soHeaderTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (soItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to YSWB_SO_ITEMS table YSWB_SO_HEADER.");
		if (wbTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		
		//Get Custom field values.
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		int tr = 0; 
		
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

		//Dataset Declaration
		VtiExitLdbSelectCriterion [] soHeaderTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soHeaderTWSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderTWSelConds, true);
		VtiExitLdbTableRow[] soHeaderTWLdbRows = soHeaderTWLdbTable.getMatchingRows(soHeaderTWSelCondGrp);

		if(soHeaderTWLdbRows.length == 0)
			return new VtiUserExitResult(999, "No sales order detail exist.");
		
		VtiExitLdbSelectCriterion [] soItemsTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, "DIEN"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soItemsTWSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsTWSelConds, true);
		VtiExitLdbTableRow[] soItemsTWLdbRows = soItemsTWLdbTable.getMatchingRows(soItemsTWSelCondGrp);
		
		if(soItemsTWLdbRows.length == 0)
			return new VtiUserExitResult(999, "No sales order detail exist.");
		
		if(soHeaderTWLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("New") || soHeaderTWLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("FAILED"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
		
		if(soHeaderTWLdbRows[0].getFieldValue("DELIVERY").equalsIgnoreCase("X"))
		{
			VtiUserExitScreenTable scrTItems = getScreenTable("TB_ITEMS");
			VtiUserExitScreenTable customTable = getScreenTable("TB_CUSTOM");
			VtiUserExitScreenTableRow customerRow = customTable.getRow(3);
			VtiUserExitScreenField bagVol = getScreenField("BAGVOL");
			
			for(int inspRow = 0;inspRow < 2;inspRow++)
			{
				VtiUserExitScreenTableRow aItemRow = scrTItems.getRow(inspRow);
				
				if(inspRow == 0)
				{
					if(aItemRow.getFieldValue("ARKTX").length()  == 0 || aItemRow.getFieldValue("TOTAL").length()  == 0)
						return new VtiUserExitResult(999, "Please fill the Material and Total columns in the Items table.");
			
					aItemRow.setFieldValue("BAGS", Integer.toString((aItemRow.getIntegerFieldValue("TOTAL")*1000)/bagVol.getIntegerFieldValue()));
					
					soItemsTWLdbRows[inspRow].setFieldValue("MATNR",aItemRow.getFieldValue("MATNR"));
					soItemsTWLdbRows[inspRow].setFieldValue("ARKTX",aItemRow.getFieldValue("ARKTX"));
					soItemsTWLdbRows[inspRow].setFieldValue("BAGS",aItemRow.getFieldValue("BAGS"));
					soItemsTWLdbRows[inspRow].setFieldValue("NTGEW",aItemRow.getFieldValue("TOTAL"));
					soItemsTWLdbRows[inspRow].setFieldValue("KWMENG",aItemRow.getFieldValue("TOTAL"));
					soItemsTWLdbRows[inspRow].setFieldValue("TIMESTAMP","");
					
					soHeaderTWLdbRows[inspRow].setFieldValue("NAME1",customerRow.getFieldValue("FIELDVALUE"));
					soHeaderTWLdbRows[inspRow].setFieldValue("LSMENGE",aItemRow.getFieldValue("TOTAL"));
					soHeaderTWLdbRows[inspRow].setFieldValue("DELIVERY","");
					soHeaderTWLdbRows[inspRow].setFieldValue("TIMESTAMP","");
					
					try
					{
						soItemsTWLdbTable.saveRow(soItemsTWLdbRows[inspRow]);
						soHeaderTWLdbTable.saveRow(soHeaderTWLdbRows[inspRow]);
					}
					catch (VtiExitException ee)
					{
						Log.error("Failed to save data of offline SO",ee);
						return new VtiUserExitResult(999, "Unable to save new information to Sales order header and items table.");
					}
				}
				if(inspRow == 1)
				{
					if(aItemRow.getFieldValue("ARKTX").length()  == 0 || aItemRow.getFieldValue("TOTAL").length()  == 0)
					{
						soItemsTWLdbTable.deleteRow(soItemsTWLdbRows[1]);
					}
					else
					{
						soItemsTWLdbRows[inspRow].setFieldValue("ARKTX",aItemRow.getFieldValue("ARKTX"));
						soItemsTWLdbRows[inspRow].setFieldValue("BAGS",aItemRow.getFieldValue("BAGS"));
						soItemsTWLdbRows[inspRow].setFieldValue("NTGEW",aItemRow.getFieldValue("TOTAL"));
						soItemsTWLdbRows[inspRow].setFieldValue("TIMESTAMP","");
						
						try
						{
							soItemsTWLdbTable.saveRow(soItemsTWLdbRows[inspRow]);
						}
						catch (VtiExitException ee)
						{
							Log.error("Failed to remove Multiple line item",ee);
							return new VtiUserExitResult(999, "Unable to save new information to Sales order items table. Try weigh in again.");
						}
					}
				}
			}
		}
		
		if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
		{
			if(scrRBPartial.getFieldValue().equalsIgnoreCase("X"))
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight1.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight1.setIntegerFieldValue(scrFWeight.getIntegerFieldValue() + scrFWeight1.getIntegerFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
				return new VtiUserExitResult(999, "Partial weight collected, move truck and take the next weight. Remeber to select Full when taking the last weight.");
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight1.getFieldValue().length()!=0)
			{
				scrFWeight1.setIntegerFieldValue(scrFWeight.getIntegerFieldValue() + scrFWeight1.getIntegerFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight1.getFieldValue().length()==0)
			{
				scrFWeight1.setIntegerFieldValue(scrFWeight.getIntegerFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}
			
			//Set weight and time fields
			if(scrFWeight1.getFieldValue().length() == 0)
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight1.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight1.setIntegerFieldValue(scrFWeight.getIntegerFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}

			//Create weight transactions in YSWB_WB
			VtiExitLdbTableRow ldbRowWeigh1 = wbTWLdbTable.newRow();
			
			//Populate TBL Fields
			ldbRowWeigh1.setFieldValue("SERVERGROUP", getServerGroup());
			ldbRowWeigh1.setFieldValue("SERVERID", getServerId());
			ldbRowWeigh1.setFieldValue("TRANDATE", currLdbDate);
			ldbRowWeigh1.setFieldValue("VTIREF", scrFSlip.getFieldValue());
			ldbRowWeigh1.setFieldValue("TRUCKREG", soHeaderTWLdbRows[0].getFieldValue("TRUCK"));
			ldbRowWeigh1.setFieldValue("VTIREFA", soHeaderTWLdbRows[0].getFieldValue("VTIREF"));
			ldbRowWeigh1.setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLdbTime);
			ldbRowWeigh1.setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
			ldbRowWeigh1.setFieldValue("STATUS", "Weigh 1");
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
			ldbRowWeigh1.setFieldValue("USERID",sessionHeader.getUserId());
			ldbRowWeigh1.setFieldValue("TIMESTAMP","");
			
	
			try
			{
				wbTWLdbTable.saveRow(ldbRowWeigh1);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save weighin to Sales Order.");
			}
			
			//Change Status
			soHeaderTWLdbRows[0].setFieldValue("STATUS","Weigh 1");
			soHeaderTWLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
			soHeaderTWLdbRows[0].setFieldValue("TIMESTAMP","");
			
			

			
			try
			{
				soHeaderTWLdbTable.saveRow(soHeaderTWLdbRows[0]);
				
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weigh 1 status",ee);
				return new VtiUserExitResult(999,"Unable to update the Sales Order.");
			}
			
			try
			{					
				// Trigger the uploads to SAP, if a connection is available.
				
					hostConnected = isHostInterfaceConnected(hostName);
					if (hostConnected)
					{ 
						dbCall.ldbUpload("YSWB_SO_HEADER", this);
					}
			}
			catch (VtiExitException ec)
			{
				Log.error("Host not connected to SAP to upload register data during Arrival save, check server.");
			}
			
			VtiExitLdbSelectCriterion [] registerSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
										new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
											new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "A"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				
			VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
		
			if(registerLdbRows.length == 0)
				return new VtiUserExitResult(999,"This Sales Order was not found in the register, the weihgin status could not be changed.");
			
			registerLdbRows[0].setFieldValue("INSPSTATUS","W");

			registerLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				registerLdbTable.saveRow(registerLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating SO weighin status for the register, please inform Admin",ee);
				return new VtiUserExitResult(999,"The register was not updated with the status.");
			}
			
			try
			{		
				hostConnected = isHostInterfaceConnected(hostName);
				
				if (hostConnected)
					{ 
						dbCall.ldbUpload("YSWB_REGISTER", this);
					}
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating SO weighin status for the register",ee);
			}
			
			
			VtiExitLdbSelectCriterion [] registerCheckSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "A"),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
										new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
						
			VtiExitLdbSelectConditionGroup registerSelCheckCondGrp = new VtiExitLdbSelectConditionGroup(registerCheckSelConds, true);
					
			VtiExitLdbTableRow[] registerCheckLdbRows = registerLdbTable.getMatchingRows(registerSelCheckCondGrp);
		
			if(registerCheckLdbRows.length != 0)
			{
				Log.error("Register status for Sales Order failed to save W status");
				Log.error("Truck " + scrTruckReg.getFieldValue() + " with order " + scrWFSalesOrd.getFieldValue() + " and reference " + scrVRef.getFieldValue() + " failed to save W status to the register");
				

				registerCheckLdbRows[0].setFieldValue("INSPSTATUS","W");					

				registerCheckLdbRows[0].setFieldValue("TIMESTAMP","");
				
				try
				{
					registerLdbTable.saveRow(registerCheckLdbRows[0]);
					
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating weighin status for the register.",ee);
					return new VtiUserExitResult(999,"The trucks register status could not be updated.");
				}
			}
			else
			{
				Log.info("Register status for Sales order correctly saved W status");
				Log.info("Truck " + scrTruckReg.getFieldValue() + " with order " + scrWFSalesOrd.getFieldValue() + " and reference " + scrVRef.getFieldValue() + " update not required during the double check.");
			}
			
			//change queue status for truck

			VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
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
					dbCall.ldbUpload("YSWB_QUEUE", this);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating queue status, please correct in the queue.",ee);
					return new VtiUserExitResult(999,"Unable to update the Sales Order and queue status.");
				}
			}
						
			//Set next Function
			sessionHeader.setNextFunctionId("YSWB_TROUTBOUND");
		}
		else
		{
			// Check if packing and loading has been done recorded.
		
			//Get tables
			VtiExitLdbTable packLdbTable = getLocalDatabaseTable("YSWB_PACKING");
			VtiExitLdbTable loadLdbTable = getLocalDatabaseTable("YSWB_LOADING");
			
			if(packLdbTable == null) return new VtiUserExitResult(999, "Packing Table load failed.");
			if(loadLdbTable == null) return new VtiUserExitResult(999, "Loading Table load failed.");
			//Check table row count
			//WB
			VtiExitLdbSelectCriterion [] wbTrucActSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("WEIGHT1", VtiExitLdbSelectCondition.GT_OPERATOR, "0"),
								//new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup wbTrucActSelCondGrp = new VtiExitLdbSelectConditionGroup(wbTrucActSelConds, true);
			VtiExitLdbTableRow[] wbTrucActLdbRows = wbTWLdbTable.getMatchingRows(wbTrucActSelCondGrp);
		
			if(wbTrucActLdbRows.length == 0)
				return new VtiUserExitResult(999, "No weighbridge detail for this truck.");
			
			//Pack
			VtiExitLdbSelectCriterion [] packTrucActSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup packTrucActSelCondGrp = new VtiExitLdbSelectConditionGroup(packTrucActSelConds, true);
			VtiExitLdbTableRow[] packTrucActLdbRows = packLdbTable.getMatchingRows(packTrucActSelCondGrp);
		
			//Load
			VtiExitLdbSelectCriterion [] loadTrucActSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup loadTrucActSelCondGrp = new VtiExitLdbSelectConditionGroup(loadTrucActSelConds, true);
			VtiExitLdbTableRow[] loadTrucActLdbRows = loadLdbTable.getMatchingRows(loadTrucActSelCondGrp);
			//Packing & Loading check end validation done in bulk check lower down
						
			//Set weight and time fields for weigh 2
			if(scrRBPartial.getFieldValue().equalsIgnoreCase("X"))
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight2.setIntegerFieldValue(scrFWeight.getIntegerFieldValue() + scrFWeight2.getIntegerFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
				
				return new VtiUserExitResult(999, "Partial weight collected, move truck and take the next weight. Remeber to select Full when taking the last weight.");
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight2.getFieldValue().length()!=0)
			{
				scrFWeight2.setIntegerFieldValue(scrFWeight.getIntegerFieldValue() + scrFWeight2.getIntegerFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight2.getFieldValue().length()==0)
			{
				scrFWeight2.setIntegerFieldValue(scrFWeight.getIntegerFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
			}
			
			//Set weight and time fields
			if(scrFWeight2.getFieldValue().length() == 0)
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight2.setIntegerFieldValue(scrFWeight.getIntegerFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
				
			}
			
			//Update weight transaction in YSWB_WB
			VtiExitLdbSelectCriterion [] wbTWSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "WEIGH 1"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup wbTWSelCondGrp = new VtiExitLdbSelectConditionGroup(wbTWSelConds, true);
			VtiExitLdbTableRow[] ldbRowWeigh2 = wbTWLdbTable.getMatchingRows(wbTWSelCondGrp);

			if(ldbRowWeigh2.length == 0)
				new VtiUserExitResult(999,"No valid rows availible to update in YSWB_WB.");
			//Populate LDB Fields 
			ldbRowWeigh2[0].setFieldValue("WEIGHT2", scrFWeight2.getIntegerFieldValue());
			ldbRowWeigh2[0].setFieldValue("WEIGHT2_D", currLdbDate);
			ldbRowWeigh2[0].setFieldValue("WEIGHT2_T", currLdbTime);
			ldbRowWeigh2[0].setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
			ldbRowWeigh2[0].setFieldValue("STATUS", "Weigh 2");
			ldbRowWeigh2[0].setFieldValue("WEIGHBRIDGE2",scrCBBridge.getFieldValue());
			ldbRowWeigh2[0].setFieldValue("USERID",sessionHeader.getUserId());
			ldbRowWeigh2[0].setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("TRANSPORTTYPE",tranType.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("ALLOC_WHT",allwgh.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("DELVNO",ordNo.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("REBAG",rebag.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
			ldbRowWeigh2[0].setFieldValue("TIMESTAMP","");
			ldbRowWeigh2[0].setFieldValue("PACKLINE",packline.getFieldValue("FIELDVALUE"));

			
			//Calculate Nettweight and write to screen
			w1 = scrFWeight1.getLongFieldValue();
			w2 = scrFWeight2.getLongFieldValue();
			
			nett = w2 - w1;
				
			scrFNettW.setFieldValue(nett);
			scrFNettTS.setFieldValue(currTime);	
			
			VtiExitLdbSelectCriterion [] registerSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
										new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
			
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				
			VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
		
			if(registerLdbRows.length == 0)
				return new VtiUserExitResult(999,"There are no info in the register table for this truck or for this Sales Order.");
			
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
			{
				maxLegalWght = (maxLegalWght * axleVariance) / 100 + maxLegalWght;
			}
			
			//Save Nett Weight
			ldbRowWeigh2[0].setLongFieldValue("NETTWEIGHT", nett);
			ldbRowWeigh2[0].setFieldValue("NETTWEIGHT_T", currLdbTime);
			ldbRowWeigh2[0].setFieldValue("TIMESTAMP", "");
			//Check compliance with tolerance
			float overTol = 0F;//soItemsTWLdbRows[0].getFloatFieldValue("UEBTO") / 100F;
			float underTol = 0F;//soItemsTWLdbRows[0].getFloatFieldValue("UNTTO")  / 100F;
			
			if((overTol + underTol) == 0)
			{
					//upper limit get
					VtiExitLdbSelectCriterion [] configUTolSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "CONFIG"),
									new VtiExitLdbSelectCondition("KEYVAL1", VtiExitLdbSelectCondition.EQ_OPERATOR, "SOUTL"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup configUTolSelCondGrp = new VtiExitLdbSelectConditionGroup(configUTolSelConds, true);
					VtiExitLdbTableRow[] configUTolLdbRows = configLdbTable.getMatchingRows(configUTolSelCondGrp);
		
					if(configUTolLdbRows.length == 0)
					{
						return new VtiUserExitResult (999,"Sales Order tolerance upper limit not determined.");		
					}
					//lower limit get
					VtiExitLdbSelectCriterion [] configLTolSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "CONFIG"),
									new VtiExitLdbSelectCondition("KEYVAL1", VtiExitLdbSelectCondition.EQ_OPERATOR, "SOLTL"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup configLTolSelCondGrp = new VtiExitLdbSelectConditionGroup(configLTolSelConds, true);
					VtiExitLdbTableRow[] configLTolLdbRows = configLdbTable.getMatchingRows(configLTolSelCondGrp);
		
					if(configUTolLdbRows.length == 0)
					{
						return new VtiUserExitResult (999,"Sales Order tolerance lower limit not determined.");		
					}
			
				overTol= configUTolLdbRows[0].getFloatFieldValue("KEYVAL2");
				underTol = configLTolLdbRows[0].getFloatFieldValue("KEYVAL2");
			}
			
			float tol = overTol + underTol;
			float soItemWht =0;
			
			float underWTol = 0;
			float overWTol = 0;
			
				VtiExitLdbSelectCriterion [] exclMatSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "BULK"),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup exclMatSelCondGrp = new VtiExitLdbSelectConditionGroup(exclMatSelConds, true);
				VtiExitLdbTableRow[] exclMatLdbRows = configLdbTable.getMatchingRows(exclMatSelCondGrp);
				
				if(exclMatLdbRows.length == 0)
					return new VtiUserExitResult(999, "Bulk tolerance configuration not maintained in YSWB_CONFIG");
				
				String bulkMat = "";
				boolean isBulk = false;
				boolean getBulk = false;
				String soMatNr = "";
				
				for(int ib = 0;soItemsTWLdbRows.length > ib;ib++)
				{
					soMatNr = soItemsTWLdbRows[ib].getFieldValue("MATNR");

					for(int ibcon = 0;exclMatLdbRows.length > ibcon;ibcon++)
					{
						bulkMat = exclMatLdbRows[ibcon].getFieldValue("KEYVAL1");
						getBulk = soMatNr.equalsIgnoreCase(bulkMat);
						if(getBulk)
							isBulk = true;
					}
				}
				
			if(!isBulk)
			{
				//Inform clerk of any capturing issue
					if(packTrucActLdbRows.length == 0 || packTrucActLdbRows.length < wbTrucActLdbRows.length)
						return new VtiUserExitResult(999, "The packing plant has not captured the packing for this truck, please remove truck from bridge and inform the packing office.");
					if(loadTrucActLdbRows.length == 0 || loadTrucActLdbRows.length < wbTrucActLdbRows.length)
						return new VtiUserExitResult(999, "The packing/loading plant has not captured the loading for this truck, please remove truck from bridge and inform the packing/loading office.");	
					
					
				if(maxLegalWght < scrFWeight2.getFloatFieldValue())
				{
					return new VtiUserExitResult(999, "The Gross Vehicle Weight exceeds the legal axle weight for this truck by " + (scrFWeight2.getIntegerFieldValue() - maxLegalWght));
				}
			}
			
			for(int tTon = 0;soItemsTWLdbRows.length > tTon;tTon++)
				{
						if(soItemsTWLdbRows[tTon].getFieldValue("GEWEI").equalsIgnoreCase("TO"))
						{
							soItemWht = soItemWht + soItemsTWLdbRows[tTon].getFloatFieldValue("NTGEW") * 1000;
						}
				}
			
			if(tol != 0)
			{
				if(!isBulk)
				{
					underWTol = soItemWht - soItemWht * underTol;
					overWTol = soItemWht * overTol + soItemWht;
				
					if(nett < underWTol)
					{
						scrFWeight2.setFieldValue("");
						scrFWTStamp2.setFieldValue("");
						scrFNettW.setFieldValue("");
						scrFNettTS.setFieldValue("");
						scfTolWarning.setHiddenFlag(false);
						return new VtiUserExitResult(999,"Nett weight is under order weight by " + (underWTol - nett));
					}
				
					if(nett > overWTol)
					{
						scrFWeight2.setFieldValue("");
						scrFWTStamp2.setFieldValue("");
						scrFNettW.setFieldValue("");
						scrFNettTS.setFieldValue("");
						scfTolWarning.setHiddenFlag(false);
						return new VtiUserExitResult(999,"Nett weight is over order weight by " + (nett - overWTol)); 
					}
				}
			}

			try
			{
				wbTWLdbTable.saveRow(ldbRowWeigh2[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save Nett Weight data to the weighbridge record.");
			}
			
			//Change Status
			soHeaderTWLdbRows[0].setFieldValue("STATUS","Weigh 2");
			soHeaderTWLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
			soHeaderTWLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				soHeaderTWLdbTable.saveRow(soHeaderTWLdbRows[0]);
				
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating so header weighin status, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to update weigh 2 status.");
			}
			
			
			//change queue status for truck

			VtiExitLdbSelectCriterion [] qw2SelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qw2SelCondGrp = new VtiExitLdbSelectConditionGroup(qw2SelConds, true);
			VtiExitLdbTableRow[] qw2TLdbRows = queueLdbTable.getMatchingRows(qw2SelCondGrp);
			
			if(qw2TLdbRows.length > 0)
			{
				qw2TLdbRows[0].setFieldValue("Q_STATUS","Weigh 2");
				qw2TLdbRows[0].setFieldValue("TIMESTAMP","");
			
				try
				{
					queueLdbTable.saveRow(qw2TLdbRows[0]);
					dbCall.ldbUpload("YSWB_QUEUE", this);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating queue status, please correct in the queue.",ee);
					return new VtiUserExitResult(999,"Unable to update the Sales Order.");
				}
			}
			
		}
		
		try
		{
					
			hostConnected = isHostInterfaceConnected(hostName);
			
			if (hostConnected)
			{ 
				dbCall.ldbUpload("YSWB_WB", this);
				dbCall.ldbUpload("YSWB_SO_HEADER", this);
				dbCall.ldbUpload("YSWB_REGISTER", this);
			}
		}
		catch (VtiExitException ee)
		{
			Log.error("Error updating weighin status, please try again.",ee);
			return new VtiUserExitResult(999,"Unable to update Sales Order, check status.");
		}

		return new VtiUserExitResult();
	}
}
