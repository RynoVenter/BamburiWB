package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CompleteWagonProcess extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Screen Fields
		VtiUserExitScreenField scrWFSalesOrd = getScreenField("IS_SO");
		VtiUserExitScreenField scrWFPurchOrd = getScreenField("IS_PO");
		VtiUserExitScreenField scrWFIntComOrd = getScreenField("IS_IC");
		VtiUserExitScreenField scrFTrain = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrFTruckReg = getScreenField("WAGON");
		VtiUserExitScreenField scrWTransfer = getScreenField("C_UB");
		VtiUserExitScreenField scrVtiRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrFShift = getScreenField("SHIFT");
		VtiUserExitScreenField scrUser = getScreenField("LOADER");
		VtiUserExitScreenField scrDriver = getScreenField("DRIVER");
		VtiUserExitScreenField scrTransporter = getScreenField("TRANSPORTER");
		VtiUserExitScreenField scrPacker = getScreenField("PACKER");
		VtiUserExitScreenField scrAllocW = getScreenField("ALLOCWEIGHT");
		VtiUserExitScreenField scrCustomer = getScreenField("CUSTOMER");		
		VtiUserExitScreenField scrGP = getScreenField("GATEPASS");	
		VtiUserExitScreenField scrDocNo = getScreenField("REFDOC");
				
		VtiUserExitScreenTable scrTbl = getScreenTable("TB_ITEMS");
		
		//Variables
		long tranQNo = 0;
		Date currNow = new Date();
		DBCalls dbCall = new DBCalls();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String hostName = getHostInterfaceName();
		String ordType =  "";
		String packOrdType =  "";
		String qOrder = "";
		long allocW = 0L;
		long gateWeight  =0L;
		
		boolean hostConnected = isHostInterfaceConnected(hostName);
		//LDB
		VtiExitLdbTable soHeaderCLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable tranQueLdbTable = getLocalDatabaseTable("YSWB_TRAN_QUEUE");
		VtiExitLdbTable soItemsTWLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		VtiExitLdbTable loadingLdbTable = getLocalDatabaseTable("YSWB_LOADING");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable poHeaderCLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable ldbWb = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable gatepassLdb = getLocalDatabaseTable("YSWB_GATEPASS");
		VtiExitLdbTable icItemsLdb = getLocalDatabaseTable("YSWB_IC_ITEMS");
		
		if (icItemsLdb == null) return new VtiUserExitResult(999,"Table YSWB_IC_ITEMS was not loaded.");
		if (gatepassLdb == null) return new VtiUserExitResult(999,"Table YSWB_GATEPASS was not loaded.");
		if (ldbWb == null) return new VtiUserExitResult(999,"Table YSWB_WB was not loaded.");
		if (soHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (tranQueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_TRAN_QUEUE.");
		if (soItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");
		if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		if (loadingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOADING.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (poHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEASDER.");
		
		//Get Loading Line
		VtiExitLdbSelectCriterion [] loadSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDocNo.getFieldValue()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVtiRef.getFieldValue()),
		};
      
		VtiExitLdbSelectConditionGroup loadSelCondGrp = new VtiExitLdbSelectConditionGroup(loadSelConds, true);
		VtiExitLdbTableRow[] loadLdbRows = loadingLdbTable.getMatchingRows(loadSelCondGrp);
		
		if (loadLdbRows.length == 0) return new VtiUserExitResult(999, "No load information found.");

		
		long slipNo = 0;
		
		try
		{
			slipNo = getNextNumberFromNumberRange("YSWB_SLIP");
		}
		catch(VtiExitException ee)
		{
			Log.error("Error creating next Slip No.",ee);
			return new VtiUserExitResult(999,"Unable to generate slip no.");
		}
		
								
		if(scrWFSalesOrd.getFieldValue().length() > 0)
		{
			qOrder = scrWFSalesOrd.getFieldValue();
			ordType = "VBELN";
			packOrdType = "VBELN";
		}
		if(scrWFPurchOrd.getFieldValue().length() > 0)
		{
			qOrder = scrWFPurchOrd.getFieldValue();
			ordType = "STOCKTRNF";
			packOrdType = "EBELN";
		}
		if(scrWFIntComOrd.getFieldValue().length() > 0)
		{
			qOrder = scrWFIntComOrd.getFieldValue();
			ordType = "DELIVDOC";
			packOrdType = "VBELN";
		}
		
		//Add packing tran queue trigger
			VtiExitLdbSelectCriterion [] packSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(packOrdType, VtiExitLdbSelectCondition.EQ_OPERATOR, qOrder),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
			};
      
			VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
			VtiExitLdbTableRow[] packLdbRows = packingLdbTable.getMatchingRows(packSelCondGrp);
		
			if(packLdbRows.length == 0)
				return new VtiUserExitResult(999, "Could not find the packing details for  Order " + scrWFSalesOrd.getFieldValue() + " to do the bags movement.");
		
				try
				{
					tranQNo = 0;
					tranQNo = getNextNumberFromNumberRange("YSWB_TRANS");
				}
				catch(VtiExitException ee)
				{
					Log.error("Unable to create number from YSWB_TRANS",ee);
					return new VtiUserExitResult(999,"Unable to retrieve a transaction number from number range YSWB_TRANS");
				}
			
					VtiExitLdbTableRow ldbTQPacking = tranQueLdbTable.newRow();
					ldbTQPacking.setFieldValue("SERVERID", getServerId());
					ldbTQPacking.setFieldValue("TRAN_NO", tranQNo);
					ldbTQPacking.setFieldValue(packOrdType, qOrder);
					ldbTQPacking.setFieldValue("TRUCK", scrFTruckReg.getFieldValue());
					ldbTQPacking.setFieldValue("VTIREF", packLdbRows[0].getFieldValue("VTIREF"));
					if(scrWFSalesOrd.getFieldValue().length() > 0)
						ldbTQPacking.setFieldValue("TRANTYPE", "PACKING");
					else
						ldbTQPacking.setFieldValue("TRANTYPE", "PACKING_PO");
					ldbTQPacking.setFieldValue("TIMESTAMP", "");
					ldbTQPacking.setFieldValue("TRANDATE", currLdbDate);
									
					try
					{	 
						tranQueLdbTable.saveRow(ldbTQPacking);
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to Save data to the YSWB_TRAN_QUEUE table.",ee);
						return new VtiUserExitResult(999,"Unable to Save data to the Transaction Queue table.");
					}
					//Packing bag move complete
					
					//Complete Order
					VtiExitLdbTableRow ldbRowTranQ = tranQueLdbTable.newRow();
					
					//Sales Order
					if(scrWFSalesOrd.getFieldValue().length() > 0)
					{
						VtiExitLdbSelectCriterion [] soHeaderCSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
						};
      
						VtiExitLdbSelectConditionGroup soHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderCSelConds, true);
						VtiExitLdbTableRow[] soHeaderCLdbRows = soHeaderCLdbTable.getMatchingRows(soHeaderCSelCondGrp);
		
						if(soHeaderCLdbRows.length == 0)
							return new VtiUserExitResult(999, "Could not find the Sales Order to complete, please ensure second weight has been taken.");
						//Create Trigger
						
						ldbRowTranQ.setFieldValue("SERVERID", getServerId());
						ldbRowTranQ.setFieldValue("TRAN_NO", slipNo);
						ldbRowTranQ.setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
						ldbRowTranQ.setFieldValue("VTIREF", soHeaderCLdbRows[0].getFieldValue("VTIREF"));
						ldbRowTranQ.setFieldValue("TRANTYPE", "DELIVERY");
						ldbRowTranQ.setFieldValue("TRANDATE", currLdbDate);
						ldbRowTranQ.setFieldValue("TRUCK", scrFTruckReg.getFieldValue());
						ldbRowTranQ.setFieldValue("TIMESTAMP", "");
						
						soHeaderCLdbRows[0].setFieldValue("STATUS","Complete");
						soHeaderCLdbRows[0].setFieldValue("VTIREF",slipNo);
						soHeaderCLdbRows[0].setFieldValue("TIMESTAMP","");
						
						allocW = soHeaderCLdbRows[0].getLongFieldValue("LSMENGE");
						
						try
						{	
								soHeaderCLdbTable.saveRow(soHeaderCLdbRows[0]);
								tranQueLdbTable.saveRow(ldbRowTranQ);
								
						}
						catch (VtiExitException ee)
						{
							Log.error("Unable to Save data to the YSWB_TRAN_QUEUE table.",ee);
							return new VtiUserExitResult(999,"Unable to to complete rail order.");
						}
					}
					
					
					//Update Register
					VtiExitLdbSelectCriterion [] regSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition(ordType, VtiExitLdbSelectCondition.EQ_OPERATOR, qOrder),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
											new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVtiRef.getFieldValue()),
					};
      
					VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
					VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
			
					if(regLdbRows.length == 0)
						return new VtiUserExitResult(999,"Sales order not found in the register.");		
			
					regLdbRows[0].setFieldValue("INSPSTATUS","C");
					regLdbRows[0].setFieldValue("DEPTIME",currLdbTime);
					regLdbRows[0].setFieldValue("TIMESTAMP","");
					
					
					try
					{	 
						registerLdbTable.saveRow(regLdbRows[0]);
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to update the register table.",ee);
						return new VtiUserExitResult(999,"Unable to to complete rail order.");
					}
					
					//Status details for STO and IC
					VtiExitLdbSelectCriterion [] statusSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition(ordType, VtiExitLdbSelectCondition.EQ_OPERATOR, qOrder),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
												new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVtiRef.getFieldValue()),
						};
      
						VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
						VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);

						

					//STO Order
					if(scrWFPurchOrd.getFieldValue().length() > 0)
					{
						if(statusLdbRows.length == 0)
							return new VtiUserExitResult(999, "Could not find the Order to complete.");
						
						VtiExitLdbSelectCriterion [] poHeaderCSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
										new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWTransfer.getFieldValue()),
						};
      
						VtiExitLdbSelectConditionGroup poHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderCSelConds, true);
						VtiExitLdbTableRow[] poHeaderCLdbRows = poHeaderCLdbTable.getMatchingRows(poHeaderCSelCondGrp);
		
						if(poHeaderCLdbRows.length == 0)
							return new VtiUserExitResult(999, "Could not find the Purchase Order to complete.");		
				
						ldbRowTranQ.setFieldValue("SERVERID", getServerId());
						ldbRowTranQ.setFieldValue("TRAN_NO", slipNo);
						ldbRowTranQ.setFieldValue("EBELN", scrWFPurchOrd.getFieldValue());
						ldbRowTranQ.setFieldValue("VTIREF", scrVtiRef.getFieldValue());
						ldbRowTranQ.setFieldValue("TRUCK", scrFTruckReg.getFieldValue());
						ldbRowTranQ.setFieldValue("TIMESTAMP", "");
						ldbRowTranQ.setFieldValue("TRANTYPE", "TRANSFER");
						ldbRowTranQ.setFieldValue("TRANDATE", currLdbDate);
						
						statusLdbRows[0].setFieldValue("WGH_STATUS","Complete");
						statusLdbRows[0].setFieldValue("STATUS","C");
						statusLdbRows[0].setFieldValue("TIMESTAMP","");
						
						VtiExitLdbSelectCriterion [] gateSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, scrGP.getFieldValue()),
										new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVtiRef.getFieldValue()),
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
						
						allocW = gateWeight;
						
						try
						{	 
							tranQueLdbTable.saveRow(ldbRowTranQ);
							statusLdbTable.saveRow(statusLdbRows[0]);
						}
						catch (VtiExitException ee)
						{
							Log.error("Unable to Save data to the YSWB_TRAN_QUEUE & YSWB_STATUS table.",ee);
							return new VtiUserExitResult(999,"Unable to to complete rail order.");
						}
					}
					
					//InterCompany
					if(scrWFIntComOrd.getFieldValue().length() > 0)
					{
						if(statusLdbRows.length == 0)
							return new VtiUserExitResult(999, "Could not find the Order to complete.");
									
						//Ic Items
						VtiExitLdbSelectCriterion [] icSelConds = 
						{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFIntComOrd.getFieldValue()),
						};
        
						VtiExitLdbSelectConditionGroup icSelCondGrp = new VtiExitLdbSelectConditionGroup(icSelConds, true);
						VtiExitLdbTableRow[] icLdbRows = icItemsLdb.getMatchingRows(icSelCondGrp);
										
						if(icLdbRows.length == 0)
							return new VtiUserExitResult(999, "No Inter Company details found.");
		
						ldbRowTranQ.setFieldValue("SERVERID", getServerId());
						ldbRowTranQ.setFieldValue("TRAN_NO", slipNo);
						ldbRowTranQ.setFieldValue("VBELN", scrWFIntComOrd.getFieldValue());
						ldbRowTranQ.setFieldValue("VTIREF", scrVtiRef.getFieldValue());
						ldbRowTranQ.setFieldValue("TRUCK", scrFTruckReg.getFieldValue());
						ldbRowTranQ.setFieldValue("TRANTYPE", "INTERCOMPANY");
						ldbRowTranQ.setFieldValue("TRANDATE", currLdbDate);
						ldbRowTranQ.setFieldValue("TIMESTAMP", "");
									
						statusLdbRows[0].setFieldValue("WGH_STATUS","Complete");
						statusLdbRows[0].setFieldValue("STATUS","C");
						statusLdbRows[0].setFieldValue("TIMESTAMP","");
									
						allocW = icLdbRows[0].getLongFieldValue("LFIMG");
									
						try
						{	 
							tranQueLdbTable.saveRow(ldbRowTranQ);
							statusLdbTable.saveRow(statusLdbRows[0]);
						}
						catch (VtiExitException ee)
						{
							Log.error("Unable to Save data to the YSWB_TRAN_QUEUE & YSWB_STATUS table.",ee);
							return new VtiUserExitResult(999,"Unable to to complete rail order.");
						}
					}
					
					//Update Q

					
					VtiExitLdbSelectCriterion [] qSelConds = 
						{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, qOrder),
											new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
												new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "WEIGH 1"),
													new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, "RAIL"),
						};
      
						VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
						VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
						Log.error("Order " + qOrder);
						Log.error("Reg " + scrFTruckReg.getFieldValue());
						
						if(qTLdbRows.length > 0)
						{
							qTLdbRows[0].setFieldValue("Q_STATUS","Complete");
							qTLdbRows[0].setFieldValue("TIMESTAMP","");
						}
						
					try
					{	 
						queueLdbTable.saveRow(qTLdbRows[0]);	
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to Save data to the Queue table.",ee);

						return new VtiUserExitResult(999,"Unable to to complete rail order.");
					}
					// end update q
						

					//Complete the status for the train in the register if the last wagon was completed.
					
					//Look for wagons not complete for this train
						VtiExitLdbSelectCriterion [] wagonSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
											new VtiExitLdbSelectCondition("TRAIN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTrain.getFieldValue()),
												new VtiExitLdbSelectCondition("WAGON", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),		
						};
      
						VtiExitLdbSelectConditionGroup wagonSelCondGrp = new VtiExitLdbSelectConditionGroup(wagonSelConds, true);
						VtiExitLdbTableRow[] wagonLdbRows = registerLdbTable.getMatchingRows(wagonSelCondGrp);
			
						if(wagonLdbRows.length == 0)
						{
							VtiExitLdbSelectCriterion [] trainSelConds = 
							{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
											new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
												new VtiExitLdbSelectCondition("TRAIN", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),
													new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTrain.getFieldValue()),
							};
      
							VtiExitLdbSelectConditionGroup trainSelCondGrp = new VtiExitLdbSelectConditionGroup(trainSelConds, true);
							VtiExitLdbTableRow[] trainLdbRows = registerLdbTable.getMatchingRows(trainSelCondGrp);
						
							if(trainLdbRows.length > 0)
							{
								trainLdbRows[0].setFieldValue("INSPSTATUS","C");
								trainLdbRows[0].setFieldValue("DEPTIME",currLdbTime);
								trainLdbRows[0].setFieldValue("TIMESTAMP","");
						
								try
								{	 
									registerLdbTable.saveRow(trainLdbRows[0]);
								}
								catch (VtiExitException ee)
								{
									Log.error("Unable to Save data to the YSWB_TRAN_QUEUE table.",ee);
									return new VtiUserExitResult(999,"Unable to to complete rail order.");
								}
							}
						}
						
		//Fill WB table with record
		
		//Get Bags unit of measure
		long bagsUoM = 0L;
		
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "BAGS_UOM"),
		};
        
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbTableRow[] configLdbRows = configLdbTable.getMatchingRows(configSelCondGrp);
		
		if(configLdbRows.length == 0)
		{
			bagsUoM = 0;
			return new VtiUserExitResult(000,"Bags Unit of Measure details not maintained in Config.");
		}
		else
		{
			bagsUoM = configLdbRows[0].getLongFieldValue("KEYVAL1");;
		}
		
		//Loop through table to calculate alloc weight and weigh 2
		
		long weight = 0L;
		for(int i = 0;i < scrTbl.getRowCount(); i++)
		{
			VtiUserExitScreenTableRow currRow = scrTbl.getRow(i);
			weight = weight +  (currRow.getLongFieldValue("PACKED")*bagsUoM);
		}
		
		//Add row to WB table.
		
		VtiExitLdbTableRow wbRec = ldbWb.newRow();
	
		wbRec.setFieldValue("SERVERGROUP", getServerGroup());
		wbRec.setFieldValue("SERVERID", getServerId());
		wbRec.setFieldValue("TRANDATE", currLdbDate);
		wbRec.setFieldValue("VTIREF", slipNo);
		wbRec.setFieldValue("VTIREFA",scrVtiRef.getFieldValue());
		wbRec.setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
		wbRec.setFieldValue("EBELN", scrWFPurchOrd.getFieldValue());
		wbRec.setFieldValue("DELIVDOC", scrWFIntComOrd.getFieldValue());
		wbRec.setFieldValue("STOCKTRNF", scrWFPurchOrd.getFieldValue());
		wbRec.setFieldValue("WEIGHT1", "0");
		wbRec.setFieldValue("WEIGHT1_D", currLdbDate);
		wbRec.setFieldValue("WEIGHT1_T", "0");
		wbRec.setFieldValue("WEIGHT2", "0");
		wbRec.setFieldValue("WEIGHT2_D", currLdbDate);
		wbRec.setFieldValue("WEIGHT2_T", "0");
		wbRec.setFieldValue("NETTWEIGHT", weight);
		wbRec.setFieldValue("NETTWEIGHT_T", currLdbTime);
		wbRec.setFieldValue("STATUS", "WEIGH 2");
		wbRec.setFieldValue("WEIGHBRIDGE", "Rail");
		wbRec.setFieldValue("WEIGHBRIDGE2", "Rail");
		wbRec.setFieldValue("TRUCKREG", scrFTruckReg.getFieldValue());
		wbRec.setFieldValue("DRIVER", scrDriver.getFieldValue());
		wbRec.setFieldValue("CUSTOMER", scrCustomer.getFieldValue());
		wbRec.setFieldValue("TRANSPORTER", scrTransporter.getFieldValue());
		wbRec.setFieldValue("TRANSPORTTYPE", "Wagon");
		wbRec.setFieldValue("ALLOC_WHT", allocW * 1000);
		wbRec.setFieldValue("PACKLOADER", scrPacker.getFieldValue());
		wbRec.setFieldValue("PACKLINE", loadLdbRows[0].getFieldValue("QUEUENO"));
		wbRec.setFieldValue("SHIFT", scrFShift.getFieldValue());
		wbRec.setFieldValue("USERID", scrUser.getFieldValue());

			
		try
		{
			ldbWb.saveRow(wbRec);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to Save data to the wb table.",ee);
			return new VtiUserExitResult(999,"Unable to Save data to the WB table.");
		}
					
					//Upload all the data.
					try
					{	 
						if (hostConnected)
						{
							dbCall.ldbUpload("YSWB_REGISTER", this);
							dbCall.ldbUpload("YSWB_WB", this);
							dbCall.ldbUpload("YSWB_QUEUE", this);
							dbCall.ldbUpload("YSWB_STATUS", this);
							dbCall.ldbUpload("YSWB_SO_HEADER", this);
							dbCall.ldbUpload("YSWB_PACKING", this);
							dbCall.ldbUpload("YSWB_LOADING", this);

							
							try
							{
								Thread.sleep(2000);
							}
							catch (InterruptedException ie)
							{
								Log.error("Sleep Thread during completion failed.", ie);
							}							
						}
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to upload rail data.",ee);
						return new VtiUserExitResult(999,"Unable to upload rail data.");
					}
					
					try
					{	 
						if (hostConnected)
						{
							dbCall.ldbUpload("YSWB_TRAN_QUEUE", this);
						}
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to upload rail data.",ee);
						return new VtiUserExitResult(999,"Unable to upload rail data.");
					}
						
		return new VtiUserExitResult(000,1,"Wagon processed, collect printouts.");
	}
}
