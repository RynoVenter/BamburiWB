package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ClearGPAssign extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		VtiUserExitScreenField scrTruck = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrVti = getScreenField("REFNO");
		
		scrTruck.setFieldValue("");
		scrVti.setFieldValue("");
		
		return new VtiUserExitResult();
	}
}
