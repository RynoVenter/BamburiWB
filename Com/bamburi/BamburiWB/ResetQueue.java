package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class ResetQueue extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		ChangeQ rQ = new ChangeQ(this);
		rQ.resetQPos();
		return new VtiUserExitResult();
	}
}
