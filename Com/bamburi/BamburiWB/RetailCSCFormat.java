package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RetailCSCFormat extends VtiUserExit
{
		public VtiUserExitResult execute() throws VtiExitException
		{
			
				VtiUserExitScreenField scrRSO = getScreenField("RSONUMBER");
				VtiUserExitScreenField scrRSOVTI = getScreenField("RSOVTI");
				
				long lRsoVti = getNextNumberFromNumberRange("YSWB_RETAILVTI");
				
				
				if(scrRSO.getFieldValue().length() == 0)
				{
					long lRsoNumber = getNextNumberFromNumberRange("YSWB_RETAILSO");
					
					scrRSO.setFieldValue("R" + lRsoNumber);
				}
				
				//scrRSOVTI.setFieldValue(lRsoVti);
			
				
				return new VtiUserExitResult();
		}
}
