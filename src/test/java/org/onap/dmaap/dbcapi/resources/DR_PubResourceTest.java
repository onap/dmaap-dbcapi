
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
package org.onap.dmaap.dbcapi.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.dmaap.dbcapi.database.DatabaseClass;
import org.onap.dmaap.dbcapi.model.DR_Pub;
import org.onap.dmaap.dbcapi.model.Feed;
import org.onap.dmaap.dbcapi.testframework.DmaapObjectFactory;

public class DR_PubResourceTest {

    private static final DmaapObjectFactory DMAAP_OBJECT_FACTORY = new DmaapObjectFactory();

    private static final String DCAE_LOCATION_NAME = "central-onap";
    private static final String USERNAME = "user1";
    private static final String USRPWD = "secretW0rd";
    private static final String FEED_ID = "someFakeFeedId";
    private static final String PUB_ID = "0";
    private static FastJerseyTest testContainer;

    @BeforeClass
    public static void setUpClass() throws Exception {
        //TODO: init is still needed here to assure that dmaap is not null
        DatabaseClass.getDmaap().init(DMAAP_OBJECT_FACTORY.genDmaap());
        DatabaseClass.getDmaap().update(DMAAP_OBJECT_FACTORY.genDmaap());

        testContainer = new DR_PubResourceTest.FastJerseyTest(new ResourceConfig()
            .register(DR_PubResource.class)
            .register(FeedResource.class)
            .register(DmaapResource.class));

        testContainer.init();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        testContainer.destroy();
    }

    @Before
    public void cleanupDatabase() {
        DatabaseClass.clearDatabase();
    }

    @Test
    public void getDr_Pub_test() {
        Response resp = testContainer.target("dr_pubs").request().get(Response.class);
        assertTrue(resp.getStatus() == 200);
    }

    @Test
    public void addDr_Pub_shallReturnError_whenNoFeedIdAndFeedNameInPubProvided() {
        //given
        DR_Pub drPub = new DR_Pub(DCAE_LOCATION_NAME, USERNAME, USRPWD, null, PUB_ID);
        Entity<DR_Pub> requestedEntity = Entity.entity(drPub, MediaType.APPLICATION_JSON);

        //when
        Response resp = testContainer.target("dr_pubs")
            .request()
            .post(requestedEntity, Response.class);

        //then
        assertEquals(400, resp.getStatus());
        assertTrue(resp.hasEntity());
    }

    @Test
    public void addDr_Pub_shallReturnError_whenFeedNameProvided_butFeedNotExist() {
        //given
        DR_Pub drPub = new DR_Pub(DCAE_LOCATION_NAME, USERNAME, USRPWD, null, PUB_ID);
        Entity<DR_Pub> requestedEntity = Entity.entity(drPub, MediaType.APPLICATION_JSON);
        drPub.setFeedId(FEED_ID);

        //when
        Response resp = testContainer.target("dr_pubs")
            .request()
            .post(requestedEntity, Response.class);

        //then
        assertEquals(404, resp.getStatus());

    }

    @Test
    public void addDr_Pub_shallReturnError_whenFeedNameProvided_andManyFeedsWithTheSameNameInDB() {
        //given
        String notDistinctFeedName = "notDistinctFeedName";
        Feed feed1 = new Feed(notDistinctFeedName, "1.0", "description", "dgl", "unrestricted");
        Feed feed2 = new Feed(notDistinctFeedName, "1.0", "description", "dgl", "unrestricted");
        DatabaseClass.getFeeds().put("1", feed1);
        DatabaseClass.getFeeds().put("2", feed2);
        DR_Pub drPub = new DR_Pub(DCAE_LOCATION_NAME, USERNAME, USRPWD, null, "0");
        drPub.setFeedName(notDistinctFeedName);
        Entity<DR_Pub> requestedEntity = Entity.entity(drPub, MediaType.APPLICATION_JSON);

        //when
        Response resp = testContainer.target("dr_pubs")
            .request()
            .post(requestedEntity, Response.class);

        //then
        assertEquals(0, resp.getStatus());
    }

    @Test
    public void addDr_Pub_shallReturnError_whenFeedIdProvided_butFeedNotExist() {
        //given
        DR_Pub drPub = new DR_Pub(DCAE_LOCATION_NAME, USERNAME, USRPWD, FEED_ID, PUB_ID);
        Entity<DR_Pub> requestedEntity = Entity.entity(drPub, MediaType.APPLICATION_JSON);

        //when
        Response resp = testContainer.target("dr_pubs")
            .request()
            .post(requestedEntity, Response.class);

        //then
        assertEquals(404, resp.getStatus());
    }

    @Test
    public void addDr_Pub_shallExecuteSuccessfully_whenValidFeedIdProvided() {
        //given
        String feedId = assureFeedIsInDB();
        DR_Pub drPub = new DR_Pub(DCAE_LOCATION_NAME, USERNAME, USRPWD, feedId, PUB_ID);
        Entity<DR_Pub> requestedEntity = Entity.entity(drPub, MediaType.APPLICATION_JSON);

        //when
        Response resp = testContainer.target("dr_pubs")
            .request()
            .post(requestedEntity, Response.class);

        //then
        assertEquals(201, resp.getStatus());
    }

    @Test
    public void addDr_Pub_shallExecuteSuccessfully_whenValidFeedNameProvided() {
        //given
        String feedName = "testFeed";
        addFeed(feedName, "test feed");
        DR_Pub drPub = new DR_Pub(DCAE_LOCATION_NAME, USERNAME, USRPWD, null, PUB_ID);
        drPub.setFeedName(feedName);
        Entity<DR_Pub> requestedEntity = Entity.entity(drPub, MediaType.APPLICATION_JSON);

        //when
        Response resp = testContainer.target("dr_pubs")
            .request()
            .post(requestedEntity, Response.class);

        //then
        assertEquals(201, resp.getStatus());
    }

    @Test
    public void updateDr_Pub_shallExecuteSuccessfully_whenAddingNewPublishing() {
        //given
        String pubId = "5";
        String feedId = assureFeedIsInDB();
        DR_Pub drPub = new DR_Pub(DCAE_LOCATION_NAME, USERNAME, USRPWD, feedId, PUB_ID);
        Entity<DR_Pub> reqEntity2 = Entity.entity(drPub, MediaType.APPLICATION_JSON);
        drPub.setUserpwd("newSecret");

        //when
        Response resp = testContainer.target("dr_pubs")
            .path(pubId)
            .request()
            .put(reqEntity2, Response.class);

        //then
        assertTrue(resp.getStatus() == 200);

    }

    @Test
    public void updateDr_Pub_shallReturnError_whenPathIsWrong() {
        //given
        DR_Pub drPub = new DR_Pub(DCAE_LOCATION_NAME, USERNAME, USRPWD, FEED_ID, PUB_ID);
        Entity<DR_Pub> reqEntity2 = Entity.entity(drPub, MediaType.APPLICATION_JSON);
        drPub.setUserpwd("newSecret");

        //when
        Response resp = testContainer.target("dr_pubs")
            .path("")
            .request()
            .put(reqEntity2, Response.class);

        //then
        assertEquals(resp.getStatus(), 405);
    }

    @Test
    public void deleteDr_Pub_shouldDeleteObjectWithSuccess() {
        //given
        String feedId = assureFeedIsInDB();
        DR_Pub dr_pub = addPub(DCAE_LOCATION_NAME, USERNAME, USRPWD, feedId);

        //when
        Response resp = testContainer.target("dr_pubs")
            .path(dr_pub.getPubId())
            .request()
            .delete();

        //then
        assertEquals("Shall delete subscription with success", 204, resp.getStatus());
        assertFalse("No entity object shall be returned", resp.hasEntity());
    }

    @Test
    public void deleteDr_Pub_shouldReturnErrorResponse_whenGivenPubIdNotFound() {
        //given
        String notExistingPubId = "6789";

        //when
        Response resp = testContainer.target("dr_pubs")
            .path(notExistingPubId)
            .request()
            .delete();

        //then
        assertEquals("Shall return error, when trying to delete not existing subscription", 404, resp.getStatus());
    }

    @Test
    public void get_shallReturnExistingObject() {
        //given
        String feedId = assureFeedIsInDB();
        DR_Pub dr_Pub = addPub(DCAE_LOCATION_NAME, USERNAME, USRPWD, feedId);

        //when
        Response resp = testContainer.target("dr_pubs")
            .path(dr_Pub.getPubId())
            .request()
            .get();

        //then
        assertEquals("Subscription shall be found", 200, resp.getStatus());
    }

    private Feed addFeed(String name, String desc) {
        Feed feed = new Feed(name, "1.0", desc, "dgl", "unrestricted");
        Entity<Feed> reqEntity = Entity.entity(feed, MediaType.APPLICATION_JSON);
        Response resp = testContainer.target("feeds").request().post(reqEntity, Response.class);
        int rc = resp.getStatus();
        System.out.println("POST feed resp=" + rc);
        assertTrue(rc == 200 || rc == 409);
        feed = resp.readEntity(Feed.class);
        return feed;
    }


    private DR_Pub addPub(String d, String un, String up, String feedId) {
        DR_Pub dr_pub = new DR_Pub(d, un, up, feedId, "");
        Entity<DR_Pub> reqEntity2 = Entity.entity(dr_pub, MediaType.APPLICATION_JSON);
        Response resp = testContainer.target("dr_pubs").request().post(reqEntity2, Response.class);
        System.out.println("POST dr_pubs resp=" + resp.getStatus());
        assertTrue(resp.getStatus() == 201);
        dr_pub = resp.readEntity(DR_Pub.class);
        return dr_pub;
    }

    //TODO: move it outside class and use in other Resource integration tests
    private static class FastJerseyTest extends JerseyTest {

        FastJerseyTest(Application jaxrsApplication) {
            super(jaxrsApplication);
        }

        void init() throws Exception {
            this.setUp();
        }

        void destroy() throws Exception {
            this.tearDown();
        }
    }

    private String assureFeedIsInDB() {
        Feed feed = addFeed("SubscriberTestFeed", "feed for DR_Sub testing");
        assertNotNull("Feed shall be added into DB properly", feed);
        return feed.getFeedId();
    }
}


