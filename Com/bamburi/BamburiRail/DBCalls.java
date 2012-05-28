package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class DBCalls
{
	/*General stored database calls, improvement on sky database stored procedures.
	*/
	public void ldbRefresh (String ldb, VtiUserExit vUE) throws VtiExitException
	{
	//Declarations of variables and elements. Followed by the checking of the elements.

		String hostName = vUE.getHostInterfaceName();
		boolean hostConnected = vUE.isHostInterfaceConnected(hostName);
		VtiUserExitHeaderInfo sessionHeader = vUE.getHeaderInfo();
		
		VtiExitLdbTable ldbTable = vUE.getLocalDatabaseTable(ldb);

		try
		{
			if (hostConnected)
			{ 
				VtiExitLdbRequest ldbReqRefresh = new VtiExitLdbRequest(ldbTable,VtiExitLdbRequest.REFRESH);
				ldbReqRefresh.submit(false);
			}
		}
		catch ( VtiExitException ee)
		{
			vUE.logError("Error refreshing the table.", ee);
		}
	}
	
	public void ldbUpload (String ldb, VtiUserExit vUE) throws VtiExitException
	{
	//Declarations of variables and elements. Followed by the checking of the elements.

		String hostName = vUE.getHostInterfaceName();
		boolean hostConnected = vUE.isHostInterfaceConnected(hostName);
		VtiUserExitHeaderInfo sessionHeader = vUE.getHeaderInfo();
		
		VtiExitLdbTable ldbTable = vUE.getLocalDatabaseTable(ldb);

		try
		{
			if (hostConnected)
			{ 
				VtiExitLdbRequest ldbReqUpload = new VtiExitLdbRequest(ldbTable,VtiExitLdbRequest.UPLOAD);
				ldbReqUpload.submit(false);
			}
		}
		catch ( VtiExitException ee)
		{
			vUE.logError("Error uploading the table.", ee);
		}
	}
	
	
	public void ldbDownload (String ldb, VtiUserExit vUE) throws VtiExitException
	{
	//Declarations of variables and elements. Followed by the checking of the elements.

		String hostName = vUE.getHostInterfaceName();
		boolean hostConnected = vUE.isHostInterfaceConnected(hostName);
		VtiUserExitHeaderInfo sessionHeader = vUE.getHeaderInfo();
		
		VtiExitLdbTable ldbTable = vUE.getLocalDatabaseTable(ldb);

		try
		{
			if (hostConnected)
			{ 
				VtiExitLdbRequest ldbReqDownload = new VtiExitLdbRequest(ldbTable,VtiExitLdbRequest.DOWNLOAD);
				ldbReqDownload.submit(false);
			}
		}
		catch ( VtiExitException ee)
		{
			Log.error("Error uploading the table.", ee);
		}
	}

}
