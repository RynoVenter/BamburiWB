package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class QRowAdjust extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenTable scrTblQueue = getScreenTable("TB_QUEUE");
		if(scrTblQueue == null) return new VtiUserExitResult (999,"Failed to initialise TB_QUEUE.");
		
		VtiUserExitScreenTableRow changeRow = scrTblQueue.getActiveRow();
		
		String currQ = changeRow.getFieldValue("Q_QUEUE");
		String newQ = changeRow.getFieldValue("Q_NEWQUEUE");
		String currPType = changeRow.getFieldValue("Q_PROD_TYPE");
		String newPType = changeRow.getFieldValue("N_PROD_TYP");
		long currPos = changeRow.getLongFieldValue("Q_POSITION");
		long newPos = changeRow.getLongFieldValue("Q_NEW_QPOS");
		
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");

		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, currQ),
							new VtiExitLdbSelectCondition("PROD_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, currPType),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "ASSIGNED"),
									new VtiExitLdbSelectCondition("Q_POSITION", VtiExitLdbSelectCondition.EQ_OPERATOR, Long.toString(currPos)),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		
		VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("Q_POSITION",true),
			};
		
		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp, orderBy);
		
		ChangeQ changeQ = new ChangeQ(this,currQ,currPType,newQ,newPType,currPos);
		
		
		if(!currQ.equalsIgnoreCase(newQ) && newQ.length() > 0)
		{
			changeQ.move2Q();
		}
				
		if(!currPType.equalsIgnoreCase(newPType) && newPType.length() > 0)
		{
			changeQ.move2ProductQ();
		}
		
		if(newPos > 0)
			if(currPos>newPos && newPos > 0)
			{
				
				changeQ.bumpTruckUp(newPos);
				
				qTLdbRows[0].setFieldValue("Q_POSITION", newPos);
				qTLdbRows[0].setFieldValue("TIMESTAMP","");
				
				try
				{
					
					queueLdbTable.saveRow(qTLdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					return new VtiUserExitResult(999, "Unable to bump truck up");
				}

			}
			else
			{
				changeQ.bumpTruckDown(newPos);
				
				qTLdbRows[0].setFieldValue("Q_POSITION", newPos);
				qTLdbRows[0].setFieldValue("TIMESTAMP","");
				
				try
				{
					queueLdbTable.saveRow(qTLdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					return new VtiUserExitResult(999, "Unable to bump truck down");
				}
			}


		
		return new VtiUserExitResult();
	}
}