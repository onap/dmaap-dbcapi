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

package org.onap.dmaap.dbcapi.database;

import java.io.*;
import java.sql.*;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class LoadSchema	{
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(LoadSchema.class);
	
	static int getVer(Statement s) throws SQLException {
		ResultSet rs = null;
		try {
			rs = s.executeQuery("SELECT version FROM dmaapbc_sch_ver");
			rs.next();
			return(rs.getInt(1));
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}
	static void upgrade() throws SQLException {
		ConnectionFactory cf = ConnectionFactory.getDefaultInstance();
		InputStream is = null;
		try(
				Connection c = cf.get(true);
				Statement stmt = c.createStatement();
		) {

			
			// this sets the PG search_path to a consistent schema, otherwise sometimes
			// we get public, and sometimes we get dmaap_admin
			String cmd = String.format( "SET search_path to %s;", cf.getSchema());
			try {
				stmt.execute(cmd);
				logger.info("SCHEMA: " + cmd);
			} catch (SQLException sqle) {
				logger.error("Error", sqle);
					throw sqle;
			}
			
			
			// determine if an upgrade is needed
			int newver = -1;
			try {
				newver = getVer(stmt);
			} catch (Exception e) {
				logger.error("Error", e);
			}
			logger.info("Database schema currently at version " + newver++);
			


			while ((is = LoadSchema.class.getClassLoader().getResourceAsStream("schema_" + newver + ".sql")) != null) {
				logger.info("Upgrading database schema to version " + newver);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s;
				String sofar = null;
				

				
				while ((s = br.readLine()) != null) {
					logger.info("SCHEMA: " + s);
					s = s.trim();
					if (s.length() == 0 || s.startsWith("--")) {
						continue;
					}
					if (sofar == null) {
						sofar = s;
					} else {
						sofar = sofar + " " + s;
					}
					if (s.endsWith(";")) {
						sofar = sofar.substring(0, sofar.length() - 1);
						boolean ignore = false;
						if (sofar.startsWith("@")) {
							ignore = true;
							sofar = sofar.substring(1).trim();
						}
						try {
							stmt.execute(sofar);
						} catch (SQLException sqle) {
							if (!ignore) {
								throw sqle;
							}
						}
						sofar = null;
					}
				}
				is.close();
				is = null;
				if (getVer(stmt) != newver) {
					throw new SQLException("Schema version not properly updated to " + newver + " by upgrade script");
				}
				logger.info("Upgrade to database schema version " + newver + " successful");
				newver++;
			}
		} catch (IOException ioe) {
			throw new SQLException(ioe);
		}
	}
	public static void main(String[] args) throws Exception {
		upgrade();
	}
}
