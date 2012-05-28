package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class TruckCall extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		VtiUserExitScreenField scrQ1CallQty = getScreenField("Q1_CALL_QTY");
		VtiUserExitScreenField scrQ2CallQty = getScreenField("Q2_CALL_QTY");
		VtiUserExitScreenField scrQ3CallQty = getScreenField("Q3_CALL_QTY");
		VtiUserExitScreenField scrQ4CallQty = getScreenField("Q4_CALL_QTY");
		VtiUserExitScreenField scrQ5CallQty = getScreenField("Q5_CALL_QTY");
		
		VtiUserExitScreenField scrQ1 = getScreenField("Q1");
		VtiUserExitScreenField scrQ2 = getScreenField("Q2");
		VtiUserExitScreenField scrQ3 = getScreenField("Q3");
		VtiUserExitScreenField scrQ4 = getScreenField("Q4");
		VtiUserExitScreenField scrQ5 = getScreenField("Q5");
		
		if(scrQ1CallQty == null) return new VtiUserExitResult(999,"Field Q1_CALL_QTY failed to load.");
		if(scrQ2CallQty == null) return new VtiUserExitResult(999,"Field Q2_CALL_QTY failed to load.");
		if(scrQ3CallQty == null) return new VtiUserExitResult(999,"Field Q3_CALL_QTY failed to load.");
		if(scrQ4CallQty == null) return new VtiUserExitResult(999,"Field Q4_CALL_QTY failed to load.");
		if(scrQ5CallQty == null) return new VtiUserExitResult(999,"Field Q5_CALL_QTY failed to load.");
		if(scrQ1 == null) return new VtiUserExitResult(999,"Field Q1 failed to load.");
		if(scrQ2 == null) return new VtiUserExitResult(999,"Field Q2 failed to load.");
		if(scrQ3 == null) return new VtiUserExitResult(999,"Field Q3 failed to load.");
		if(scrQ4 == null) return new VtiUserExitResult(999,"Field Q4 failed to load.");
		if(scrQ5 == null) return new VtiUserExitResult(999,"Field Q5 failed to load.");
		
		VtiUserExitScreenTable tblQ1 = getScreenTable("TB_Q1");
		VtiUserExitScreenTable tblQ2 = getScreenTable("TB_Q2");
		VtiUserExitScreenTable tblQ3 = getScreenTable("TB_Q3");
		VtiUserExitScreenTable tblQ4 = getScreenTable("TB_Q4");
		VtiUserExitScreenTable tblQ5 = getScreenTable("TB_Q5");
		
		if(tblQ1 == null) return new VtiUserExitResult(999,"Table TB_Q1 failed to load.");
		if(tblQ2 == null) return new VtiUserExitResult(999,"Table TB_Q2 failed to load.");
		if(tblQ3 == null) return new VtiUserExitResult(999,"Table TB_Q3 failed to load.");
		if(tblQ4 == null) return new VtiUserExitResult(999,"Table TB_Q4 failed to load.");
		if(tblQ5 == null) return new VtiUserExitResult(999,"Table TB_Q5 failed to load.");
		
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		if(confLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_CONFIG failed to load.");
		if(queueLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_QUEUE failed to load.");
		long qCallNo;
		boolean newTruck = false;

		//mark q1 qty
		if(scrQ1CallQty.getIntegerFieldValue() > 0)
		{

			VtiUserExitScreenTableRow q1Row;
			int q1QPos = 0;
			for(int q1 = 0;q1 < scrQ1CallQty.getIntegerFieldValue();q1++)
			{
				
			
				while(newTruck == false && tblQ1.getRowCount() != q1QPos)
				{
					q1Row = tblQ1.getRow(q1QPos);
					if(q1Row.getFieldValue("Q1_CALL").length() == 0)
						newTruck = true;
					q1QPos++;
				}
				
				
				q1Row = tblQ1.getRow(q1QPos-1+q1);
				
					VtiExitLdbSelectCriterion [] q1SelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "ASSIGNED"),
									new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, q1Row.getFieldValue("Q1_TRUCK")),
										new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, scrQ1.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup q1SelCondGrp = new VtiExitLdbSelectConditionGroup(q1SelConds, true);
					VtiExitLdbTableRow[] q1LdbRows = queueLdbTable.getMatchingRows(q1SelCondGrp);
		
					if(q1LdbRows.length == 0)
						return new VtiUserExitResult(999, "Could not find the truck in the " + scrQ1.getFieldValue() + " queue.");
					
					
				
			try
			{
				qCallNo = getNextNumberFromNumberRange("YSWB_CALL_Q");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next queue no.",ee);
				return new VtiUserExitResult(999,"Unable to generate next queue no.");
			}
				q1LdbRows[0].setFieldValue("Q_NUMBER",qCallNo);
				q1LdbRows[0].setFieldValue("TIMESTAMP","");
			
				try
				{
					queueLdbTable.saveRow(q1LdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					return new VtiUserExitResult(999, "Failed to mark " + q1Row.getFieldValue("Q1_TRUCK") + 
																	" in the " + scrQ1.getFieldValue() + " queue.");
				}
			}
			newTruck = false;
		}
					
		//mark q2 qty
		
		if(scrQ2CallQty.getIntegerFieldValue() > 0)
		{

			VtiUserExitScreenTableRow q2Row;
			int q2QPos =0;
			for(int q2 = 0;q2 < scrQ2CallQty.getIntegerFieldValue();q2++)
			{
				 
				
				while(newTruck == false && tblQ2.getRowCount() != q2QPos)
				{

					q2Row = tblQ2.getRow(q2QPos);

					
					if(q2Row.getFieldValue("Q2_CALL").length() == 0)
						newTruck = true;


					q2QPos++;
				}
				
			
				q2Row = tblQ2.getRow(q2QPos-1+q2);
				
				VtiExitLdbSelectCriterion [] q2SelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "ASSIGNED"),
								new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, q2Row.getFieldValue("Q2_TRUCK")),
									new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, scrQ2.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup q2SelCondGrp = new VtiExitLdbSelectConditionGroup(q2SelConds, true);
				VtiExitLdbTableRow[] q2LdbRows = queueLdbTable.getMatchingRows(q2SelCondGrp);
		
				if(q2LdbRows.length == 0)
					return new VtiUserExitResult(999, "Could not find the truck in the " + scrQ2.getFieldValue() + " queue.");
			
			try
			{
				qCallNo = getNextNumberFromNumberRange("YSWB_CALL_Q");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next queue no.",ee);
				return new VtiUserExitResult(999,"Unable to generate next queue no.");
			}
				q2LdbRows[0].setFieldValue("Q_NUMBER",qCallNo);
				q2LdbRows[0].setFieldValue("TIMESTAMP","");
			
				try
				{
					queueLdbTable.saveRow(q2LdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					return new VtiUserExitResult(999, "Failed to mark " + q2Row.getFieldValue("Q2_TRUCK") + 
																" in the " + scrQ2.getFieldValue() + " queue.");
				}
			}
			newTruck = false;
		}
				

				
		//mark q3 qty
		if(scrQ3CallQty.getIntegerFieldValue() > 0)
		{

			VtiUserExitScreenTableRow q3Row;
			int q3QPos =0;
			for(int q3 = 0;q3 < scrQ3CallQty.getIntegerFieldValue();q3++)
			{
				 
	
				while(newTruck == false && tblQ3.getRowCount() != q3QPos)
				{
					q3Row = tblQ3.getRow(q3QPos);

					if(q3Row.getFieldValue("Q3_CALL").length() == 0)
						newTruck = true;
					q3QPos++;
				}
					

				q3Row = tblQ3.getRow(q3QPos-1+q3);

				
				VtiExitLdbSelectCriterion [] q3SelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "ASSIGNED"),
								new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, q3Row.getFieldValue("Q3_TRUCK")),
									new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, scrQ3.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup q3SelCondGrp = new VtiExitLdbSelectConditionGroup(q3SelConds, true);
				VtiExitLdbTableRow[] q3LdbRows = queueLdbTable.getMatchingRows(q3SelCondGrp);
		
				if(q3LdbRows.length == 0)
					return new VtiUserExitResult(999, "Could not find the truck in the " + scrQ3.getFieldValue() + " queue.");
				try
				{
					qCallNo = getNextNumberFromNumberRange("YSWB_CALL_Q");
				}
				catch(VtiExitException ee)
				{
					Log.error("Error creating next queue no.",ee);
					return new VtiUserExitResult(999,"Unable to generate next queue no.");
				}		
			
				q3LdbRows[0].setFieldValue("Q_NUMBER",qCallNo);
				q3LdbRows[0].setFieldValue("TIMESTAMP","");
			
				try
				{
					queueLdbTable.saveRow(q3LdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					return new VtiUserExitResult(999, "Failed to mark " + q3Row.getFieldValue("Q3_TRUCK") + 
																" in the " + scrQ3.getFieldValue() + " queue.");
				}
			}
			newTruck = false;
		}
				

				
		//mark q4 qty
		if(scrQ4CallQty.getIntegerFieldValue() > 0)
		{
			if(tblQ4.getRowCount()>scrQ4CallQty.getIntegerFieldValue())
			{
				VtiUserExitScreenTableRow q4Row;
				int q4QPos =0;
				for(int q4 = 0;q4 < scrQ4CallQty.getIntegerFieldValue();q4++)
				{
					
					while(newTruck == false  && tblQ4.getRowCount() != q4QPos)
					{
						q4Row = tblQ4.getRow(q4QPos);
						if(q4Row.getFieldValue("Q4_CALL").length() == 0)
							newTruck = true;
						else
						{
							q4QPos++;
						}
					}
					
					
					q4Row = tblQ4.getRow(q4QPos-1+q4);
					
					VtiExitLdbSelectCriterion [] q4SelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "ASSIGNED"),
								new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, q4Row.getFieldValue("Q4_TRUCK")),
									new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, scrQ4.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup q4SelCondGrp = new VtiExitLdbSelectConditionGroup(q4SelConds, true);
					VtiExitLdbTableRow[] q4LdbRows = queueLdbTable.getMatchingRows(q4SelCondGrp);
		
					if(q4LdbRows.length == 0)
						return new VtiUserExitResult(999, "Could not find the truck in the " + scrQ4.getFieldValue() + " queue.");
					
					try
					{
						qCallNo = getNextNumberFromNumberRange("YSWB_CALL_Q");
					}
					catch(VtiExitException ee)
					{
						Log.error("Error creating next queue no.",ee);
						return new VtiUserExitResult(999,"Unable to generate next queue no.");
					}	
					
					q4LdbRows[0].setFieldValue("Q_NUMBER",qCallNo);
					q4LdbRows[0].setFieldValue("TIMESTAMP","");
			
					try
					{
						queueLdbTable.saveRow(q4LdbRows[0]);
					}
					catch (VtiExitException ee)
					{
						return new VtiUserExitResult(999, "Failed to mark " + q4Row.getFieldValue("Q4_TRUCK") + 
																	" in the " + scrQ4.getFieldValue() + " queue.");
					}
				}
				newTruck = false;
			}
			else
			{
				return new VtiUserExitResult(999,1,"Please select a value less than the trucks availible for the Export queue.");
			}
		}
				

				
		//mark q5 qty
		if(scrQ5CallQty.getIntegerFieldValue() > 0)
		{
			if(tblQ5.getRowCount()>scrQ5CallQty.getIntegerFieldValue())
			{
	Log.trace(0,"Marking trucks for q 5");
				VtiUserExitScreenTableRow q5Row;
				int q5QPos =0;
				for(int q5 = 0;q5 < scrQ5CallQty.getIntegerFieldValue();q5++)
				{
					while(newTruck == false && tblQ5.getRowCount() != q5QPos)
					{
						q5Row = tblQ5.getRow(q5QPos);
						if(q5Row.getFieldValue("Q5_CALL").length() == 0)
							newTruck = true;
						q5QPos++;
					}
					
					
					q5Row = tblQ5.getRow(q5QPos-1+q5);
					
					VtiExitLdbSelectCriterion [] q5SelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "ASSIGNED"),
									new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, q5Row.getFieldValue("Q5_TRUCK")),
										new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, scrQ5.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
	      
					VtiExitLdbSelectConditionGroup q5SelCondGrp = new VtiExitLdbSelectConditionGroup(q5SelConds, true);
					VtiExitLdbTableRow[] q5LdbRows = queueLdbTable.getMatchingRows(q5SelCondGrp);
			
					if(q5LdbRows.length == 0)
						return new VtiUserExitResult(999, "Could not find the truck in the " + scrQ5.getFieldValue() + " queue.");
					
					try
					{
						qCallNo = getNextNumberFromNumberRange("YSWB_CALL_Q");
					}
					catch(VtiExitException ee)
					{
						Log.error("Error creating next queue no.",ee);
						return new VtiUserExitResult(999,"Unable to generate next queue no.");
					}
					
					q5LdbRows[0].setFieldValue("Q_NUMBER",qCallNo);
					q5LdbRows[0].setFieldValue("TIMESTAMP","");
				
					try
					{
						queueLdbTable.saveRow(q5LdbRows[0]);
					}
					catch (VtiExitException ee)
					{
						return new VtiUserExitResult(999, "Failed to mark " + q5Row.getFieldValue("Q5_TRUCK") + 
																	" in the " + scrQ5.getFieldValue() + " queue.");
					}
				}
				newTruck = false;
			}
			else
			{
				return new VtiUserExitResult(999,1,"Please select a value less than the trucks availible for the Transport queue.");
			}
		}
				

		return new VtiUserExitResult();
	}
}
