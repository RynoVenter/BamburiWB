package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ChangeQ
{
	VtiUserExit vUE;
	String q;
	String pQ;
	String nPQ;
	String nQ;
	long currPos;
	
	final String status = "ASSIGNED";
	
	public ChangeQ (VtiUserExit vUE) throws VtiExitException
	{
		this.vUE = vUE;
	}
	
	public ChangeQ (VtiUserExit vUE, String q, String pQ) throws VtiExitException
	{
		this.vUE = vUE;
		this.pQ = pQ;
		this.q = q;
	}
	
	public ChangeQ (VtiUserExit vUE, String q, String pQ, String nQ, String nPQ, long currPos) throws VtiExitException
	{
		this.vUE = vUE;
		this.pQ = pQ;
		this.q = q;
		this.nQ = nQ;
		this.nPQ = nPQ;
		this.currPos = currPos;
	}
		
	public void resetQPos() throws VtiExitException 
	{
		String lastResDate = "";
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable numRangeLdbTable = vUE.getLocalDatabaseTable("VTI_NUMBER_RANGE");
		VtiExitLdbTable configLdbTable = vUE.getLocalDatabaseTable("YSWB_CONFIG");

		Date now = new Date();
		String serialDate = DateFormatter.format("dd/MM/yyyy", now);
		
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
					new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "RES_DATE"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		
		VtiExitLdbTableRow[] configLdbRows = configLdbTable.getMatchingRows(configSelCondGrp);
		
		lastResDate = configLdbRows[0].getFieldValue("KEYVAL2");
		
		
		
		if(!lastResDate.equalsIgnoreCase(serialDate))
	    {
		
			VtiExitLdbSelectCriterion [] qSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
							new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, status),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			
			VtiExitLdbOrderSpecification []orderBy = 
			{
				new VtiExitLdbOrderSpecification("Q_POSITION",true),
			};
		
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp, orderBy);
		

					
			VtiExitLdbSelectCriterion [] numRangeSelConds = 
				{
					new VtiExitLdbSelectCondition("NRO_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, "YSWB_QPOS"),
				};
      
			VtiExitLdbSelectConditionGroup numRangeSelCondGrp = new VtiExitLdbSelectConditionGroup(numRangeSelConds, true);
			VtiExitLdbTableRow[] numRangeTLdbRows = numRangeLdbTable.getMatchingRows(numRangeSelCondGrp);
		
			numRangeTLdbRows[0].setLongFieldValue("NRO_VALUE",0);
		
			try
			{
				numRangeLdbTable.saveRow(numRangeTLdbRows[0]);
			}
			catch (VtiExitException ee) 
			{
				vUE.logError("The Number Range queue position was not changed to a 0 start position.");
			}
		
			long resPos;
		
			for(int upRow = 0; upRow < qTLdbRows.length; upRow++)
			{
				resPos  = vUE.getNextNumberFromNumberRange("YSWB_QPOS");
				
				qTLdbRows[upRow].setLongFieldValue("Q_POSITION", resPos);
				qTLdbRows[upRow].setFieldValue("TIMESTAMP","");
				try
				{
					queueLdbTable.saveRow(qTLdbRows[upRow]);
				}
				catch (VtiExitException ee)
				{
					vUE.logError("No delay/advance change possible", ee);
				}
				
			}
			
			configLdbRows[0].setFieldValue("KEYVAL2",serialDate);
			try
			{
				configLdbTable.saveRow(configLdbRows[0]);
			}
			catch(VtiExitException ee)
			{
				vUE.logError("Config last reset date did not save new date.");
			}
		}
		
		
	}
	
	public void bumpTruckDown(long cQPos) throws VtiExitException 
	{
		
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");

		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						//new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, q),
							//new VtiExitLdbSelectCondition("PROD_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, pQ),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, status),
									new VtiExitLdbSelectCondition("Q_POSITION", VtiExitLdbSelectCondition.NE_OPERATOR, Long.toString(currPos)),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		
		VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("Q_POSITION",true),
			};
		
		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp, orderBy);
		
		
		for(int upRow = 0; upRow < qTLdbRows.length; upRow++)
		{
			
			if(qTLdbRows[upRow].getLongFieldValue("Q_POSITION") >  currPos && qTLdbRows[upRow].getLongFieldValue("Q_POSITION") <= cQPos)
			{
				
				qTLdbRows[upRow].setLongFieldValue("Q_POSITION", qTLdbRows[upRow].getLongFieldValue("Q_POSITION")-1L);
				qTLdbRows[upRow].setFieldValue("TIMESTAMP","");
				
				try
				{
					queueLdbTable.saveRow(qTLdbRows[upRow]);
				}
				catch (VtiExitException ee)
				{
					vUE.logError("No delay/advance change possible", ee);
				}
			}
		}
	}
	
	
	public void bumpTruckUp(long cQPos) throws VtiExitException 
	{
		
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");

		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						//new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, q),
							//new VtiExitLdbSelectCondition("PROD_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, pQ),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, status),
									new VtiExitLdbSelectCondition("Q_POSITION", VtiExitLdbSelectCondition.NE_OPERATOR, Long.toString(currPos)),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		
		VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("Q_POSITION",true),
			};
		
		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp, orderBy);
				
		for(int upRow = 0; upRow < qTLdbRows.length; upRow++)
		{

			if(qTLdbRows[upRow].getLongFieldValue("Q_POSITION") >=  cQPos  && qTLdbRows[upRow].getLongFieldValue("Q_POSITION") <  currPos)
			{
							
				qTLdbRows[upRow].setLongFieldValue("Q_POSITION", qTLdbRows[upRow].getLongFieldValue("Q_POSITION")+1L);
				qTLdbRows[upRow].setFieldValue("TIMESTAMP","");
				try
				{
					queueLdbTable.saveRow(qTLdbRows[upRow]);
				}
				catch (VtiExitException ee)
				{
					vUE.logError("Bumping the truck up failed", ee);
				}
			}
		}
	}
	
	public void move2Q() throws VtiExitException 
	{
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");

		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, q),
							new VtiExitLdbSelectCondition("PROD_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, pQ),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, status),
									new VtiExitLdbSelectCondition("Q_POSITION", VtiExitLdbSelectCondition.EQ_OPERATOR, Long.toString(currPos)),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		
		VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("Q_POSITION",true),
			};
		
		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp, orderBy);
		
		if(qTLdbRows.length == 0)
		{
			VtiUserExitHeaderInfo sessionHeader = vUE.getHeaderInfo();
			

		}
		qTLdbRows[0].setFieldValue("Q_QUEUE", nQ);
		qTLdbRows[0].setFieldValue("TIMESTAMP","");
		
		try
		{
			queueLdbTable.saveRow(qTLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			vUE.logError("No delay/advance change possible", ee);
		}
	}
	
	public void move2ProductQ() throws VtiExitException
	{
		VtiExitLdbTable qLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");

		VtiExitLdbSelectCriterion [] changePqSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, q),
							new VtiExitLdbSelectCondition("PROD_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, pQ),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, status),
									new VtiExitLdbSelectCondition("Q_POSITION", VtiExitLdbSelectCondition.EQ_OPERATOR, Long.toString(currPos)),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
		VtiExitLdbSelectConditionGroup changePqSelCondGrp = new VtiExitLdbSelectConditionGroup(changePqSelConds, true);
		
		VtiExitLdbTableRow[] changePqTLdbRows = qLdbTable.getMatchingRows(changePqSelCondGrp);
			
	
		changePqTLdbRows[0].setFieldValue("PROD_TYPE", nPQ);
		changePqTLdbRows[0].setFieldValue("TIMESTAMP","");
		
		try
		{
			qLdbTable.saveRow(changePqTLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Product queue change not perfromed.", ee);
		}
	}	
}
