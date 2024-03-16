/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import commons.Event;

import com.google.inject.Inject;

import commons.Participant;
import org.glassfish.jersey.client.ClientConfig;

import commons.Quote;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class ServerUtils {

    private final String server;

    /**
     * @param userConfig user configuration with server url
     */
    @Inject
    public ServerUtils(UserConfig userConfig) {
        server = userConfig.getUrl();
    }

    /**
     * @param id the id of the event to get
     * @return the found event
     */
    public Event getEvent(String id) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/events/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(Event.class);
    }

    /**
     * @param event the new event to be created
     * @return the created entry in the db
     */
    public Event createEvent(Event event) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(event, APPLICATION_JSON), Event.class);
    }

    /**
     * @param eventId     tbe event in which the participant should be created
     * @param participant the participant to be created
     */
    public void createParticipant(String eventId, Participant participant) {
        ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/events/" + eventId + "/participants")
                .request(APPLICATION_JSON)
                .post(Entity.entity(participant, APPLICATION_JSON), Participant.class);
    }

    /**
     * @param eventId     the event in which the participant should be updated
     * @param participant the participant to be updated
     */
    public void updateParticipant(String eventId, Participant participant) {
        ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + eventId + "/participant/" + participant.getParticipantId())
                .request(APPLICATION_JSON)
                .put(Entity.entity(participant, APPLICATION_JSON), Participant.class);
    }

    /**
     * @param eventId       the event in which the participant should be deleted
     * @param participantId the participant to be deleted
     */
    public void deleteParticipant(String eventId, String participantId) {
        ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + eventId + "/participant/" + participantId)
                .request(APPLICATION_JSON)
                .delete();
    }

    /**
     * S ends an API call to server for quotes
     *
     * @return all quotes
     */
    public List<Quote> getQuotes() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server)
                .path("api/quotes") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Quote>>() {
                });
    }

    /**
     * Send an API call to server to add a quote
     *
     * @param quote Quote to add
     * @return Added quote
     */
    public Quote addQuote(Quote quote) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/quotes") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(quote, APPLICATION_JSON), Quote.class);
    }
}