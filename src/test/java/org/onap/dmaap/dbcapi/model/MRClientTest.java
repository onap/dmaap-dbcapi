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
package org.onap.dmaap.dbcapi.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class MRClientTest {

	private static final String  fmt = "%24s: %s%n";

	ReflectionHarness rh = new ReflectionHarness();

	String d, t, f, c, m;

	@Before
	public void setUp() throws Exception {
		d = "central-onap";
		t = "org.onap.dmaap.interestingTopic";
		f = "mrc.onap.org:3904/events/org.onap.dmaap.interestingTopic";
		c = "publisher";
		m = "m12345";
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test1() {


		rh.reflect( "org.onap.dmaap.dbcapi.model.MR_Client", "get", null );	
	
	}

	@Test
	public void test2() {
		String[] a = { "put", "view" };
		MR_Client m = new MR_Client( d, f, c, a );


		assertTrue( d.equals( m.getDcaeLocationName() ));
		assertTrue( f.equals( m.getFqtn() ));
		assertTrue( c.equals( m.getClientRole() ));
		assertTrue( a.equals( m.getAction() ));
	}

	@Test
	public void test3() {

		String v = "Validate";
		rh.reflect( "org.onap.dmaap.dbcapi.model.MR_Client", "set", v );
	}

}
