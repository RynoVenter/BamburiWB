package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckPOQty extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrLocalPoQty = getScreenField("LPOQTY");
		VtiUserExitScreenField scrPoQty = getScreenField("POQTY");
		VtiUserExitScreenField scrConfigQty = getScreenField("CONFIGQTY");
		VtiUserExitScreenField scrIsPerc	=  getScreenField("ISPERCENT");
		VtiUserExitScreenField scrCurrentWght	=  getScreenField("KTMNG");
		VtiUserExitScreenField scrIsBlock = getScreenField("ISBLOCK");
		VtiUserExitScreenField scrCscIntervention = getScreenField("GETCSC");
		VtiUserExitScreenField scrWeigh1 = getScreenField("RB_WEIGH1");
		
		VtiUserExitHeaderInfo sessionInfo = getHeaderInfo();
		
		StringBuffer errorMsg = new StringBuffer();
		
		if(scrWeigh1.getFieldValue().length() > 0)
		{
			if(scrIsPerc.getFieldValue().length() > 0)
			{
				if(((scrConfigQty.getDoubleFieldValue() / 100) * scrLocalPoQty.getDoubleFieldValue()) >= scrCurrentWght.getDoubleFieldValue())
				{
					errorMsg.append("The PO is nearly exhausted and needs intervention from CSC.");
					scrCscIntervention.setFieldValue("X");
					sessionInfo.setNextFunctionId("YSWB_POQTYCHECK");
				}
			}
			else if(scrPoQty.getDoubleFieldValue() > 0 
					    && scrPoQty.getDoubleFieldValue() <= scrConfigQty.getDoubleFieldValue()
					    && scrIsPerc.getFieldValue().length() == 0)
			{
				errorMsg.append("The PO is nearly exhausted and needs intervention from CSC.");
				scrCscIntervention.setFieldValue("X");
				sessionInfo.setNextFunctionId("YSWB_POQTYCHECK");
			}
			else if(scrCurrentWght.getDoubleFieldValue() > 0
					    && scrPoQty.getDoubleFieldValue() == 0
					    && scrIsPerc.getFieldValue().length() == 0
					    && scrCurrentWght.getDoubleFieldValue() <= scrConfigQty.getDoubleFieldValue())
			{
				errorMsg.append("The PO is nearly exhausted and needs intervention from CSC.");
				scrCscIntervention.setFieldValue("X");
				sessionInfo.setNextFunctionId("YSWB_POQTYCHECK");
			}
		
			if(scrIsBlock.getFieldValue().length() > 0)
			{
				
				sessionInfo.setNextFunctionId("YSWB_MAIN");
				return new VtiUserExitResult(999,1, "The PO is nearly exhausted and processing of near exhausted orders have been blocked.");
			}
		}
		
		if(errorMsg.length() > 0)
			return new VtiUserExitResult(000,errorMsg.toString());
		else
			return new VtiUserExitResult();
	}
}
