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

import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class ServerUtilsImpl implements ServerUtils {

    private final String server;

    /**
     * @param userConfig user configuration with server url
     */
    @Inject
    public ServerUtilsImpl(UserConfig userConfig) {
        server = "http:" + userConfig.getUrl();
    }

    /**
     * @param id the id of the event to get
     * @return the found event, null if not found
     */
    @Override
    public Event getEvent(String id) {
        try{
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/events/" + id)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(Event.class);
        } catch (NotFoundException e) {
            return null;
        }
    }

    /**
     * @param event the new event to be created
     * @return the created entry in the db
     */
    @Override
    public Event createEvent(Event event) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(event, APPLICATION_JSON), Event.class);
    }

    /**
     * Sends a delete request for event
     *
     * @param id event id
     * @return status code
     */
    @Override
    public int deleteEvent(String id) {
        try(Response response = ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/events/" + id)
                .request(APPLICATION_JSON)
                .delete()) {
            return response.getStatus();
        }
    }

    /**
     * @param eventId     tbe event in which the participant should be created
     * @param participant the participant to be created
     * @return 204 for success,
     * 400 if the participant is badly formatted,
     * 404 if event is not found
     */
    @Override
    public int createParticipant(String eventId, Participant participant) {
        try(Response response = ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/events/" + eventId + "/participants")
                .request(APPLICATION_JSON)
                .post(Entity.entity(participant, APPLICATION_JSON))) {
            return response.getStatus();
        }
    }

    /**
     * @param eventId     the event in which the participant should be updated
     * @param participant the participant to be updated
     * @return 204 for success,
     * 400 if the participant is badly formatted,
     * 404 if event is not found
     */
    @Override
    public int updateParticipant(String eventId, Participant participant) {
        try(Response response = ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + eventId + "/participants/" + participant.getId())
                .request(APPLICATION_JSON)
                .put(Entity.entity(participant, APPLICATION_JSON))) {
            return response.getStatus();
        }
    }

    /**
     * @param eventId       the event in which the participant should be deleted
     * @param participantId the participant to be deleted
     * @return 204 for success,
     * 400 if the participant is badly formatted,
     * 404 if event is not found
     */
    @Override
    public int deleteParticipant(String eventId, long participantId) {
        try(Response response = ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + eventId + "/participants/" + participantId)
                .request(APPLICATION_JSON)
                .delete()) {
            return response.getStatus();
        }
    }

    /**
     * @param id id of the expense to retrieve
     * @param eventID ID of the event that contains the expense
     * @return the retrieved expense
     */
    public Expense getExpense(long id, String eventID) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + eventID + "/expenses/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(Expense.class);
    }

    /**
     * @param eventID ID of the event to which the expense belongs
     * @param expense the expense to be created
     * @return status code
     */
    public int createExpense(String eventID, Expense expense) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + eventID + "/expenses")
                .request(APPLICATION_JSON)
                .post(Entity.entity(expense, APPLICATION_JSON))
                .getStatus();
    }

    /**
     * @param id id of the expense to update
     * @param eventID ID of the event containing the expense
     * @param expense the updated expense object
     * @return status code
     */
    public int updateExpense(long id, String eventID, Expense expense) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + eventID + "/expenses/" + id)
                .request(APPLICATION_JSON)
                .put(Entity.entity(expense, APPLICATION_JSON))
                .getStatus();
    }

    /**
     * @param id id of the expense to delete
     * @param eventID ID of the event containing the expense
     * @return status code
     */
    public int deleteExpense(long id, String eventID) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + eventID + "/expenses/" + id)
                .request(APPLICATION_JSON)
                .delete()
                .getStatus();
    }

    /**
     * Verify the input password
     * @param inputPassword the password to verify
     * @return true iff password is correct
     */
    @Override
    public boolean verifyPassword(String inputPassword) {
        try(Response response = ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("admin/verify") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(inputPassword, APPLICATION_JSON))) {
            return response.getStatus() == Response.Status.OK.getStatusCode();
        }
    }


    /**
     * Sends an API call to server to get all events
     *
     * @param inputPassword the admin password
     * @return all events
     */
    @Override
    public List<Event> getEvents(String inputPassword) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("admin/events") //
                .request(APPLICATION_JSON) //
                .header("Authorization", inputPassword)
                .accept(APPLICATION_JSON) //
                .get(new GenericType<>() {});
    }

    /**
     * @param inputPassword admin password
     * @return HTTP response - 204 if there is an update and 408 if not
     */
    @Override
    public int pollEvents(String inputPassword, Long timeOut) {
        try(Response response = ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("admin/events/poll") //
                .request(APPLICATION_JSON) //
                .header("Authorization", inputPassword)
                .header("TimeOut", timeOut)
                .accept(APPLICATION_JSON) //
                .get()) {
            return response.getStatus();
        }
    }

    /**
     * Sends an API call to add the event
     * The ids of expenses and participants gets reassigned so use the returned event!
     *
     * @param password the admin password
     * @param event event to import
     * @return imported event
     */
    @Override
    public int importEvent(String password, Event event) {
        try(Response response = ClientBuilder.newClient(new ClientConfig())
                .target(server).path("admin/events")
                .request(APPLICATION_JSON)
                .header("Authorization", password)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(event, APPLICATION_JSON))) {
            return response.getStatus();
        }
    }

    /**
     * @param event     the event in which the participant should be updated
     * @return 204 for success,
     * 400 if the event is badly formatted,
     * 404 if event is not found
     */
    @Override
    public int updateEventTitle(Event event) {
        try(Response response = ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/events/" + event.getId())
                .request(APPLICATION_JSON)
                .post(Entity.entity(event, APPLICATION_JSON)))
        {
            System.out.println(response.toString());
            return response.getStatus();
        }




    }
}