package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class NextRsoVti extends VtiUserExit
{
		public VtiUserExitResult execute() throws VtiExitException
		{
			

				VtiUserExitScreenField scrRSOVTI = getScreenField("RSOVTI");
				VtiUserExitScreenField scrVBELN = getScreenField("VBELN");
				
		

			//	long lRsoVti = getNextNumberFromNumberRange("YSWB_RETAILVTI");

				//scrRSOVTI.setFieldValue(lRsoVti);
				scrVBELN.setFieldValue("");
			scrRSOVTI.setFieldValue("");
				
				return new VtiUserExitResult();
		}
}