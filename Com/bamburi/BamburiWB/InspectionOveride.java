package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class InspectionOveride extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		
		//VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrRegNo = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrWKey = getScreenField("VTI_REF");
		VtiUserExitScreenField scrFOvrReason = getScreenField("OVR_REASON");
		
		//if (scrVbeln == null) return new VtiUserExitResult(999, "Unable to initialise screen field VBELN.");
		//if (scrEbeln == null) return new VtiUserExitResult(999, "Unable to initialise screen field EBELN.");
		if (scrRegNo == null) return new VtiUserExitResult(999, "Unable to initialise screen field TRUCKREG.");
		if (scrWKey == null) return new VtiUserExitResult(999, "Unable to initialise screen field VTIREF.");
		if (scrFOvrReason == null) return new VtiUserExitResult(999, "Unable to initialise screen field OVR_REASON.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		boolean isPo = false;
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		
		String errorMsg = "Overide and Assignment successful."; 
		long interval =0;
		
		//Database TBL Declaration
		/*VtiExitLdbTable soHeaderCLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		if (soHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		
		VtiExitLdbTable poHeaderCLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		if (poHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		*/
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		
		VtiExitLdbTable inspectionLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		if (inspectionLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");

		if(scrEbeln.getFieldValue().length() != 0)
			isPo = true;
		
			if(scrFOvrReason.getFieldValue().length() == 0)
				return new VtiUserExitResult(999,"Please provide a reason for the overide.");
												 
				VtiExitLdbSelectCriterion [] inHeaderCSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWKey.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "F"),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup inHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(inHeaderCSelConds, true);
				VtiExitLdbTableRow[] inHeaderCLdbRows = inspectionLdbTable.getMatchingRows(inHeaderCSelCondGrp);
				
				if(inHeaderCLdbRows.length == 0)
					return new VtiUserExitResult(999, "Inspection for truck " + scrRegNo.getFieldValue() + "not found  with matching reference" + scrWKey.getFieldValue());
				
				inHeaderCLdbRows[0].setFieldValue("INSPSTATUS","O");
				inHeaderCLdbRows[0].setFieldValue("OVR_COMMENT",scrFOvrReason.getFieldValue());
				inHeaderCLdbRows[0].setFieldValue("TIMESTAMP", "");
				
				try
				{
					inspectionLdbTable.saveRow(inHeaderCLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Failed to update Inspection",ee);
					return new VtiUserExitResult(999,"Overide Failed.");
				}

				
				//Register call
				VtiExitLdbSelectCriterion [] registerSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWKey.getFieldValue()),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "F"),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
		
				if(registerLdbRows.length > 1 || registerLdbRows.length == 0)
					return new VtiUserExitResult(999,"Unable to get the truck thats needs to be overide. Looking for " + scrRegNo.getFieldValue() + " matching reference" + scrWKey.getFieldValue() + ", Multiple matches made");
				
				registerLdbRows[0].setFieldValue("INSPSTATUS","O");
				registerLdbRows[0].setFieldValue("TIMESTAMP", "");
				
				try
				{
					registerLdbTable.saveRow(registerLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Failed to update register",ee);
					return new VtiUserExitResult(999,"Overide Failed.");
				}
				
				//Add entry to the YSWB_STATUS table
			//Entry for PO
				if(isPo)
				{
					if(scrEbeln.getFieldValue().length() != 0)
					{
						VtiExitLdbTableRow poStatus = statusLdbTable.newRow();

						poStatus.setFieldValue("SERVERGRP",getServerGroup());
						poStatus.setFieldValue("SERVERID",getServerId());
						poStatus.setFieldValue("VTIREF",scrWKey.getFieldValue());
						poStatus.setFieldValue("EBELN",scrEbeln.getFieldValue());
						poStatus.setFieldValue("STATUS","A");
						poStatus.setFieldValue("WGH_STATUS","ASSIGNED");
						poStatus.setFieldValue("INSP_VTI_REF",scrWKey.getFieldValue());
						poStatus.setFieldValue("INSP_DATE",currLdbDate);
						poStatus.setFieldValue("INSP_TIME",currLdbTime);
						poStatus.setFieldValue("ARR_DATE",registerLdbRows[0].getFieldValue("AUDAT"));
						poStatus.setFieldValue("ARR_TIME",registerLdbRows[0].getFieldValue("AUTIM"));
						poStatus.setFieldValue("TRUCKREG",scrRegNo.getFieldValue());
						//poStatus.setFieldValue("USERID",sessionHeader.getUserId());
						poStatus.setFieldValue("DOCTYPE","NB");
						poStatus.setFieldValue("TIMESTAMP","");
						
						VtiExitLdbSelectCriterion [] chngRegisterSelConds = 
						{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWKey.getFieldValue()),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
												new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue()),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
								
						};
      
						VtiExitLdbSelectConditionGroup chngRegisterSelCondGrp = new VtiExitLdbSelectConditionGroup(chngRegisterSelConds, true);
						VtiExitLdbTableRow[] chngRegisterLdbRows = registerLdbTable.getMatchingRows(chngRegisterSelCondGrp);
						
						if(chngRegisterLdbRows.length == 0)
							return new VtiUserExitResult(999,"Could not find this truck in the Register or Status table.");
						
						chngRegisterLdbRows[0].setFieldValue("INSPSTATUS","A");
						chngRegisterLdbRows[0].setFieldValue("TIMESTAMP", "");
						
						try
						{
							statusLdbTable.saveRow(poStatus);
							registerLdbTable.saveRow(chngRegisterLdbRows[0]);
						}
						catch(VtiExitException ee)
						{
							Log.error("Status Save Failed.",ee);
							return new VtiUserExitResult(999,"Failed to add the truck to the Status table.Tracking not possible anymore.");
						}
						
						try
						{
				
								GetQ addInQ = new GetQ(this,scrEbeln.getFieldValue(), scrRegNo.getFieldValue());
								String queue = "";
					
								queue = addInQ.getTruckQ();
					
								if(queue.length() == 0)
									return new VtiUserExitResult(999,"Q#: " + queue + " truck: " + scrRegNo.getFieldValue() + " ebeln: " + scrEbeln.getFieldValue() + " time: " + registerLdbRows[0].getFieldValue("AUTIM")+ " date: " + registerLdbRows[0].getFieldValue("AUDAT"));
				
								try
								{
									interval = getNextNumberFromNumberRange("YSWB_QPOS");
								}
								catch(VtiExitException ee)
								{
									Log.error("Error creating next queue no.",ee);
									return new VtiUserExitResult(999,"Unable to generate next queue pos no.");
								}
								
								AddToQ qTruck = new AddToQ(this, scrRegNo.getFieldValue(), scrEbeln.getFieldValue()
														,false, queue,"Put type here",registerLdbRows[0].getFieldValue("AUTIM"),registerLdbRows[0].getFieldValue("AUDAT")
														,interval, registerLdbRows[0].getFieldValue("DRIVER"));
				
								qTruck.addTruck2Q();		
						}
						catch (VtiExitException ee)
						{
							Log.error("Unable to Update Purchase Order with Registration No.",ee);
							errorMsg = errorMsg + "Truck not added to the queue. Inform administrator to add manually";
						}
					}
				}
			
		
				
		sessionHeader.setNextFunctionId("YSWB_MAIN");
		
		return new VtiUserExitResult(000,1,errorMsg);
	
	}
}
