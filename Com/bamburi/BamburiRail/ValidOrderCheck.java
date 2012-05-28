package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ValidOrderCheck extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrWFSalesOrd = getScreenField("VBELN");
		VtiUserExitScreenField scrWFPurchOrd = getScreenField("EBELN");
		VtiUserExitScreenField scrFTruckReg = getScreenField("REGNO");
		
		if(scrWFSalesOrd != null) 
			if(scrWFPurchOrd != null)
				if(scrWFPurchOrd.getFieldValue().length() == 0 && scrWFSalesOrd.getFieldValue().length() == 0)
					return new VtiUserExitResult (999,"Please select, do not type, the correct registration number.");
		if(scrFTruckReg == null) return new VtiUserExitResult (999,"Failed to initialise TRUCKREG.");
		
		if(scrFTruckReg.getFieldValue().length() <= 2)
			return new VtiUserExitResult (999,"Please select, the correct registration number.");
			
		
		return new VtiUserExitResult();
	}
}
