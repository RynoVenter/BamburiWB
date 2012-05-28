package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class TruckStatus extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrSo = getScreenField("SO_SRCH");
		VtiUserExitScreenField scrPo = getScreenField("PO_SRCH");
		VtiUserExitScreenField scrIc = getScreenField("IC_SRCH");
		VtiUserExitScreenField scrSt = getScreenField("TO_SRCH");
		VtiUserExitScreenField scrSDate = getScreenField("S_DATE");
		VtiUserExitScreenField scrTruckReg = getScreenField("SRCH_REGNO");
		
		VtiUserExitScreenTable scrTblResult = getScreenTable("TBL_RESULTS");
		if(scrTblResult == null) return new VtiUserExitResult (999,"Failed to initialise TBL_RESULTS.");
		
		VtiUserExitScreenTableRow resRow;
		
		String status = "";
		String order = "";
		String type = "";
		
		if((scrSt.getFieldValue().length() + scrSo.getFieldValue().length() + scrIc.getFieldValue().length() + scrPo.getFieldValue().length()) > 10)
			return new VtiUserExitResult(999,"Please provide only one order number");
		
		if(scrSo.getFieldValue().length() != 0 || scrSo.getFieldValue().length() == 10)
		{
			order = scrSo.getFieldValue();
			type = "VBELN";
		}
			
		if(scrPo.getFieldValue().length() != 0 || scrPo.getFieldValue().length() == 10)
		{
			order = scrPo.getFieldValue();
			type = "EBELN";
		}
				
		if(scrIc.getFieldValue().length() != 0 || scrIc.getFieldValue().length() == 10)
		{
			order = scrIc.getFieldValue();
			type = "DELIVDOC";
		}
					
		if(scrSt.getFieldValue().length() != 0 || scrSt.getFieldValue().length() == 10)
		{
			order = scrSt.getFieldValue();
			type = "STOCKTRNF";
		}
		
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable packLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		VtiExitLdbTable loadLdbTable = getLocalDatabaseTable("YSWB_LOADING");

		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (packLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		if (loadLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOADING.");
		
		VtiExitLdbTableRow[] registerLdbRow;
			
		if(order.length() == 0)
		{
			VtiExitLdbSelectCriterion [] registerSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrSDate.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			    
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						
			VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
			
			if(registerLdbRows.length == 0)
			return new VtiUserExitResult(999, "No instance details found for vehicle.");
			
			if(registerLdbRows.length > 1)
			{
				VtiUserExitScreenTableRow resMRow; 
				for(int i = 0;i<registerLdbRows.length;i++)
				{
					
					resMRow = scrTblResult.getNewRow();
					if(!registerLdbRows[i].getFieldValue("INSPSTATUS").equalsIgnoreCase("C"))
					{
						resMRow.setFieldValue("RESULT", registerLdbRows[i].getFieldValue("INSPSTATUS"));
						resMRow.setFieldValue("RESTIM", registerLdbRows[i].getFieldValue("AUTIM"));
						scrTblResult.appendRow(resMRow);
					}
				}
				return new VtiUserExitResult(000,"Multiple registration entries.");
			}
			
				if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("R"))
				{
					status = "Registered";
					resRow = scrTblResult.getNewRow();
					resRow.setFieldValue("RESULT", status);
					resRow.setFieldValue("RESTIM", registerLdbRows[0].getFieldValue("AUTIM"));
		
					scrTblResult.appendRow(resRow);
						
					return new VtiUserExitResult(000,"Result found.");
				}
				if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("P"))
				{
					status = "Passed";
				}
				if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("F") || registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("O"))
				{
					status = "Failed";
				}
				if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("A"))
				{
					status = "Assigned";
					
					resRow = scrTblResult.getNewRow();
					resRow.setFieldValue("RESULT", status);
					resRow.setFieldValue("RESTIM", registerLdbRows[0].getFieldValue("ASSTIME"));
		
					scrTblResult.appendRow(resRow);
						
					return new VtiUserExitResult(000,"Result found.");
				}
				
				if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("C"))
				{
					status = "Complete";
					
					resRow = scrTblResult.getNewRow();
					resRow.setFieldValue("RESULT", status);
					resRow.setFieldValue("RESTIM", registerLdbRows[0].getFieldValue("DEPTIME"));
					
					scrTblResult.appendRow(resRow);
						
					return new VtiUserExitResult(000,"Result found.");
				}
			}
		else
		{
			VtiExitLdbSelectCriterion [] registerSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition(type, VtiExitLdbSelectCondition.CS_OPERATOR, order),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
									new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrSDate.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

			};
			    
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						
			VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
			
			if(registerLdbRows.length == 0)
			return new VtiUserExitResult(999, "No instance details found for vehicle.");
			
			if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("C"))
				{
					status = "Complete";
					
					resRow = scrTblResult.getNewRow();
					resRow.setFieldValue("RESULT", status);
					resRow.setFieldValue("RESTIM", registerLdbRows[0].getFieldValue("DEPTIME"));
					
					scrTblResult.appendRow(resRow);
						
					return new VtiUserExitResult(000,"Result found.");
				}
			
			if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("A"))
			{
				status = "ASSIGNED";
					
				resRow = scrTblResult.getNewRow();
				resRow.setFieldValue("RESULT", status);
				resRow.setFieldValue("RESTIM", registerLdbRows[0].getFieldValue("ASSTIME"));
		
				scrTblResult.appendRow(resRow);
					
				return new VtiUserExitResult(000,"Result found.");
			}
			
			if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("W"))
			{
				VtiExitLdbSelectCriterion [] wbSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition(type, VtiExitLdbSelectCondition.CS_OPERATOR, order),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
				    
				VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
							
				VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);
				
				if(wbLdbRows.length == 0)
					return new VtiUserExitResult(999, "No wb records.");
				
				status = wbLdbRows[0].getFieldValue("STATUS");
				
				if(status.equalsIgnoreCase("Rejected"))
				{
					status  = "Rejected - " + wbLdbRows[0].getFieldValue("REJREASON");
					resRow = scrTblResult.getNewRow();
					resRow.setFieldValue("RESULT", status);
					resRow.setFieldValue("RESTIM", wbLdbRows[0].getFieldValue("REJTIME"));
					scrTblResult.appendRow(resRow);
					return new VtiUserExitResult(000,"Result found.");
				}
				if(status.equalsIgnoreCase("Weigh 1"))	
				{
					status  = "Weigh 1";
					resRow = scrTblResult.getNewRow();
					resRow.setFieldValue("RESULT", status);
					resRow.setFieldValue("RESTIM", wbLdbRows[0].getFieldValue("WEIGHT1_T"));
					scrTblResult.appendRow(resRow);
					return new VtiUserExitResult(000,"Result found.");
				}
				
				if(status.equalsIgnoreCase("Weigh 2"))
				{
					status  = "Weigh 2-Finishing";
					
					resRow = scrTblResult.getNewRow();
					resRow.setFieldValue("RESULT", status);
					resRow.setFieldValue("RESTIM", wbLdbRows[0].getFieldValue("WEIGHT2_T"));
					scrTblResult.appendRow(resRow);
					return new VtiUserExitResult(000,"Result found.");
				}
									
				if(status.equalsIgnoreCase("Weigh 1"))
				{
					VtiExitLdbSelectCriterion [] loadSelConds = 
					{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

					};
        
					VtiExitLdbSelectConditionGroup loadSelCondGrp = new VtiExitLdbSelectConditionGroup(loadSelConds, true);
							
					VtiExitLdbTableRow[] loadLdbRows = loadLdbTable.getMatchingRows(loadSelCondGrp);
				
					if(loadLdbRows.length > 0)
					{
						status = status + " - Loaded";
						resRow = scrTblResult.getNewRow();
						resRow.setFieldValue("RESULT", status);
						resRow.setFieldValue("RESTIM", loadLdbRows[0].getFieldValue("END_TIME"));
						scrTblResult.appendRow(resRow);
						return new VtiUserExitResult(000,"Result found.");
					}
					else
					{
						if(type.equalsIgnoreCase("STOCKTRNF"))
						   type = "EBELN";
						
						VtiExitLdbSelectCriterion [] packSelConds = 
						{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition(type, VtiExitLdbSelectCondition.EQ_OPERATOR, order),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

						};
        
						VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
								
						VtiExitLdbTableRow[] packLdbRows = packLdbTable.getMatchingRows(packSelCondGrp);
				
						if(packLdbRows.length > 0)
						{
							status = status + "Packing";
							resRow = scrTblResult.getNewRow();
							resRow.setFieldValue("RESULT", status);
							resRow.setFieldValue("RESTIM", loadLdbRows[0].getFieldValue("START_TIME"));
							scrTblResult.appendRow(resRow);
							return new VtiUserExitResult(000,"Result found.");
						}
					}
				}
			}
		}

		resRow = scrTblResult.getNewRow();
		resRow.setFieldValue("RESULT", status);
		
		scrTblResult.appendRow(resRow);
		
		
		return new VtiUserExitResult();
	}
}
