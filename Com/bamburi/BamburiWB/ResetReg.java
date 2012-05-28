package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class ResetReg extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrSRegno = getScreenField("REGNO_S");
		VtiUserExitScreenField scrWDriver = getScreenField("DRIVER_S");
		
		scrSRegno.setFieldValue("");
		scrWDriver.setFieldValue("");
		
		return new VtiUserExitResult();
	}
}
