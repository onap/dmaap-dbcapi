/*-
 * ============LICENSE_START=======================================================
 * org.onap.dmaap
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
package org.onap.dmaap.dbcapi.client;

import org.onap.dmaap.dbcapi.client.MrProvConnection;
import org.onap.dmaap.dbcapi.model.*;
import org.onap.dmaap.dbcapi.service.*;
import org.onap.dmaap.dbcapi.testframework.ReflectionHarness;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MrProvConnectionTest {

	private static final String  fmt = "%24s: %s%n";

	ReflectionHarness rh = new ReflectionHarness();

	MrProvConnection ns;
	MR_ClusterService mcs;
	TopicService ts;

	@Before
	public void setUp() throws Exception {
		ns = new MrProvConnection();
		ts = new TopicService();
		mcs = new MR_ClusterService();
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test1() {


		rh.reflect( "org.onap.dmaap.dbcapi.aaf.client.MrProvConnection", "get", "idNotSet@namespaceNotSet:pwdNotSet" );	
	
	}

	@Test
	public void test2() {
		String v = "Validate";
		rh.reflect( "org.onap.dmaap.dbcapi.aaf.client.MrProvConnection", "set", v );

	}

	@Test
	public void test3() {
		String locname = "central-demo";

		DcaeLocationService dls = new DcaeLocationService();
		DcaeLocation loc = new DcaeLocation( "CLLI1234", "central-onap", locname, "aZone", "10.10.10.0/24" );
		dls.addDcaeLocation( loc );

		ApiError err = new ApiError();
		
		MR_Cluster cluster = new MR_Cluster( locname, "localhost", "http", "3904" );
		mcs.addMr_Cluster( cluster, err );
		ns.makeTopicConnection( cluster );
		Topic topic = new Topic();
		topic.setTopicName( "test5" );
		String resp = ns.doPostTopic( topic, err );

		try {
			InputStream is = new FileInputStream( "./etc/dmaapbc.properties" );				
			String body = ns.bodyToString( is );
		} catch ( FileNotFoundException fnfe ) {
		}

	}



}

