package com.bamburi.bamburiwb;

import java.util.*;
import java.util.Hashtable.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetDocType extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if(sessionHeader == null) return new VtiUserExitResult();
		
		VtiUserExitScreenField scrTruck = getScreenField("TRUCK_REG");
		VtiUserExitScreenField scrDate = getScreenField("MDATE");
		VtiUserExitScreenField scrStatus = getScreenField("STATUS");
		VtiUserExitScreenField scrDocType = getScreenField("DOCTYP");
		VtiUserExitScreenField scrScreen = getScreenField("SCREEN");
		
		if(scrTruck == null) return new VtiUserExitResult(999, "TRUCK_REG not found");
		if(scrDate == null) return new VtiUserExitResult(999, "MDATE not found");
		if(scrStatus == null) return new VtiUserExitResult(999, "STATUS not found");
		if(scrDocType == null) return new VtiUserExitResult(999, "DOCTYP not found");
		if(scrScreen == null) return new VtiUserExitResult(999, "SCREEN not found");
		//Declare DB Tables
		VtiExitLdbTable statusLdb = getLocalDatabaseTable("YSWB_STATUS");
		if(statusLdb == null) return new VtiUserExitResult(999,"Status table not opened.");
		
		VtiExitLdbTable registerLdb = getLocalDatabaseTable("YSWB_REGISTER");
		if(registerLdb == null) return new VtiUserExitResult(999,"Register table not opened.");
		
		//Variables
		String token = "";
		String docNum = "";
		String truckNo = "";
		int colonIndex = 0;
		boolean hasColon = false;
		
		//screen preperation
		scrScreen.setFieldValue("");
		scrStatus.setFieldValue("");
		scrDocType.setFieldValue("");
		
		if(scrTruck.getFieldValue().length() != 0)
		{
			//Decode input
				colonIndex = scrTruck.getFieldValue().indexOf(":");
				if(colonIndex > 0)
				{
					hasColon = true;
					token = scrTruck.getFieldValue().substring(colonIndex+2, colonIndex+4);
					docNum = scrTruck.getFieldValue().substring(colonIndex + 5, scrTruck.getFieldValue().length());
					truckNo = scrTruck.getFieldValue().substring(0, colonIndex - 1);
				}
				else
				{
					truckNo = scrTruck.getFieldValue();
				}
				
			//If it has a colon position then there is a doc number
			if(hasColon)
			{
				//If the document is of type token s then it is a VBELN
				if(token.equalsIgnoreCase("S "))
				{

					if(scrDate.getFieldValue().length() != 0)
					{
						VtiExitLdbSelectCriterion [] registerSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
										new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, docNum),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
		 
						VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
						
						if(registerLdbRows.length != 0)
						{
							
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							scrDocType.setFieldValue("Sales Order");
							scrScreen.setFieldValue("YSWB_TROUTBOUND");
							scrDate.setFieldValue(registerLdbRows[0].getFieldValue("AUDAT"));
							scrDate.setFieldValue("");
						}
					}
					else
					{
						VtiExitLdbSelectCriterion [] registerSelConds = 
						{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, docNum),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
		 
						VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
						
						if(registerLdbRows.length != 0)
						{
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							scrDocType.setFieldValue("Sales Order");
							scrScreen.setFieldValue("YSWB_TROUTBOUND");
							scrDate.setFieldValue(registerLdbRows[0].getFieldValue("AUDAT"));
							scrDate.setFieldValue("");
						}
					}
				}
				//If the document is of type token PO then it is a EBELN

				if(token.equalsIgnoreCase("PO"))
				{
					if(scrDate.getFieldValue().length() != 0)
					{
						VtiExitLdbSelectCriterion [] registerSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
										new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, docNum),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
      
						VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
						
						if(registerLdbRows.length != 0)
						{
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							scrDocType.setFieldValue("Purchase Order");
							scrScreen.setFieldValue("YSWB_TRINBOUND");
						}
					}
					else
					{
						VtiExitLdbSelectCriterion [] registerSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, docNum),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
      
						VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
						
						if(registerLdbRows.length != 0)
						{
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							scrDocType.setFieldValue("Purchase Order");
							scrScreen.setFieldValue("YSWB_TRINBOUND");
						}
					}
				}
				//If the document is of type token ST then it is a STOCKTRNF		
				if(token.equalsIgnoreCase("ST"))
				{
					if(scrDate.getFieldValue().length() != 0)
					{
						VtiExitLdbSelectCriterion [] registerSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
										new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, docNum),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
      
						VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
						
						if(registerLdbRows.length != 0)
						{
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							scrDocType.setFieldValue("Stock Transfer");
							scrScreen.setFieldValue("YSWB_TRANSFER");
						}
					}
					else
					{
						VtiExitLdbSelectCriterion [] registerSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, docNum),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
      
						VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
						
						if(registerLdbRows.length != 0)
						{
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							scrDocType.setFieldValue("Stock Transfer");
							scrScreen.setFieldValue("YSWB_TRANSFER");
						}
					}
				}
				//If the document is of type token IC then it is a DELIVDOC		
				if(token.equalsIgnoreCase("IC"))
				{
					if(scrDate.getFieldValue().length() != 0)
					{
						VtiExitLdbSelectCriterion [] registerSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
										new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, docNum),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
		 
						VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
 
						if(registerLdbRows.length != 0)
						{
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							scrDocType.setFieldValue("Inter Company");
							scrScreen.setFieldValue("YSWB_TRIC");
						}
					}
					else
					{
						VtiExitLdbSelectCriterion [] registerSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, docNum),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
      
						VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
						VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);

						if(registerLdbRows.length != 0)
						{
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							scrDocType.setFieldValue("Inter Company");
							scrScreen.setFieldValue("YSWB_TRIC");
						}
					}
				}
			}
			else
			{
				if(scrDate.getFieldValue().length() != 0)
				{
					VtiExitLdbSelectCriterion [] registerSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
												new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
		 
					VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
					VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
					
					if(registerLdbRows.length != 0)
						{
							if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("P") ||
								registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("O"))
									scrStatus.setFieldValue("A");
							else
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
							
							scrDocType.setFieldValue("No Document");
							scrScreen.setFieldValue("YSWB_AUTH_WGH");
						}
				}
				else
				{
					VtiExitLdbSelectCriterion [] registerSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truckNo),
											new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
											new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
											new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
											new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
					VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
					
					if(registerLdbRows.length != 0)
					{
						if(registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("P") ||
						   registerLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("O"))
									scrStatus.setFieldValue("A");
						else
							scrStatus.setFieldValue(registerLdbRows[0].getFieldValue("INSPSTATUS"));
						
						scrDocType.setFieldValue("No Document");
						scrScreen.setFieldValue("YSWB_AUTH_WGH");
					}
				}
			}
		}	
		else
		{
			return new VtiUserExitResult(000,"Please provide the date of arrival and the truck registrasion.");
		}
		
		
		return new VtiUserExitResult();
	}
}
