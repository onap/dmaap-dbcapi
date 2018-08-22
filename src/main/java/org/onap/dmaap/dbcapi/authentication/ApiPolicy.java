/*-
 * ============LICENSE_START=======================================================
 * org.onap.dmaap
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dmaap.dbcapi.authentication;

import org.onap.dmaap.dbcapi.aaf.DmaapPerm;
import org.onap.dmaap.dbcapi.logging.BaseLoggingClass;
import org.onap.dmaap.dbcapi.logging.DmaapbcLogMessageEnum;
import org.onap.dmaap.dbcapi.util.DmaapConfig;

public class ApiPolicy extends BaseLoggingClass {
	static String allow = "allow";
	String dClass = null;
	private boolean useAuthClass;
	ApiAuthorizationCheckInterface perm = null;
	
	public ApiPolicy() {
		DmaapConfig p = (DmaapConfig)DmaapConfig.getConfig();
		dClass = p.getProperty( "ApiPermission.Class", allow );
		logger.info( "ApiPolicy implements " + dClass);
		if ( dClass.equalsIgnoreCase( allow )) {
			useAuthClass = false;
			return;
		} 		
		useAuthClass = true;
		logger.info( "dClass=" + dClass + " useAuthClass=" + useAuthClass );
		try {
			perm = (ApiAuthorizationCheckInterface) (Class.forName(dClass).newInstance());	
		} catch (Exception ee ) {
			errorLogger.error(DmaapbcLogMessageEnum.UNEXPECTED_CONDITION, "attempting to instantiate " + dClass  );		
			errorLogger.error( "trace is: " + ee );
		}	
	}
	
	public void check( String mechid, String pwd, DmaapPerm p ) throws AuthenticationErrorException {
		if ( dClass.equalsIgnoreCase( allow )) {
			return;
		}
		
		// execute check of loaded class
		perm.check( mechid, pwd, p );
	
	}
	
	public boolean getUseAuthClass() {
		return useAuthClass;
	}

}
