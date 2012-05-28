package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatRailRegister extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField btnWagReg = getScreenField("BT_BACK");
		if(btnWagReg == null) return new VtiUserExitResult (999,"Failed to initialise BT_BACK.");
		btnWagReg.setHiddenFlag(true);
		return new VtiUserExitResult();
	}
}
