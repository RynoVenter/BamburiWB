package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class SendHex extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
			VtiUserExitScreenField val2WB = getScreenField("VAL4WBU");
		
			VtiExitSerialDevice serialDevice = getSerialDevice("CEMENT");
			serialDevice.write(val2WB.getFieldValue());
			Log.info("Code is :" + val2WB.getFieldValue());
			
			return new VtiUserExitResult();
	}
		
}
