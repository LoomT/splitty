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
package server.api;

import commons.Event;
import commons.EventWeakKey;
import commons.Participant;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import server.database.EventRepository;
import server.database.ParticipantRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.random.RandomGenerator;

@SuppressWarnings("NullableProblems")
public class TestParticipantRepository implements ParticipantRepository {

    private final List<Participant> participants = new ArrayList<>();
    private final List<String> calledMethods = new ArrayList<>();
    private final RandomGenerator random = new TestRandom();
    private TestEventRepository eventRepo = null;

    public void setEventRepo(TestEventRepository repo) {
        eventRepo = repo;
    }
    /**
     * @return called methods
     */
    public List<String> getCalledMethods() {
        return calledMethods;
    }

    /**
     * @param name name
     */
    private void call(String name) {
        calledMethods.add(name);
    }

    /**
     * @return participant list
     */
    public List<Participant> getParticipants() {
        return participants;
    }
    /**
     * @return all quotes
     */
    @Override
    public List<Participant> findAll() {
        calledMethods.add("findAll");
        return participants;
    }

    /**
     * @param sort sort
     * @return list
     */
    @Override
    public List<Participant> findAll(Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param ids to find by
     * @return list
     */
    @Override
    public List<Participant> findAllById(Iterable<EventWeakKey> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param entities to save
     * @param <S> clas
     * @return list of saved
     */
    @Override
    public <S extends Participant> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        entities.forEach(e -> saved.add(save(e)));
        return saved;
    }

    /**
     * flush
     */
    @Override
    public void flush() {
        // TODO Auto-generated method stub

    }

    /**
     * @param entity entity
     * @param <S> class
     * @return saved entity
     */
    @Override
    public <S extends Participant> S saveAndFlush(S entity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param entities to save
     * @param <S> class
     * @return list
     */
    @Override
    public <S extends Participant> List<S> saveAllAndFlush(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param entities to delete
     */
    @Override
    public void deleteAllInBatch(Iterable<Participant> entities) {
        // TODO Auto-generated method stub

    }

    /**
     * @param ids to delete by
     */
    @Override
    public void deleteAllByIdInBatch(Iterable<EventWeakKey> ids) {
        // TODO Auto-generated method stub

    }

    /**
     * delete
     */
    @Override
    public void deleteAllInBatch() {
        // TODO Auto-generated method stub

    }

    /**
     * @param id id
     * @return quote
     */
    @Override
    public Participant getOne(EventWeakKey id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param id id
     * @return participant
     */
    @Override
    public Participant getById(EventWeakKey id) {
        call("getById");
        if(find(id).isPresent()) {
            return find(id).get();
        } else return null;
    }

    /**
     * @param id id
     * @return quote
     */
    @Override
    public Participant getReferenceById(EventWeakKey id) {
        call("getReferenceById");
        if(find(id).isEmpty()) return null;
        return find(id).get();
    }

    /**
     * @param id id
     * @return quote
     */
    private Optional<Participant> find(EventWeakKey id) {
        return participants.stream()
                .filter(q -> q.getId() == id.getId() && q.getEventID().equals(id.getEventID()))
                .findFirst();
    }

    /**
     * @param example entity
     * @param <S> class
     * @return list
     */
    @Override
    public <S extends Participant> List<S> findAll(Example<S> example) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param example entity
     * @param sort sort
     * @param <S> class
     * @return list of quotes
     */
    @Override
    public <S extends Participant> List<S> findAll(Example<S> example, Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param pageable p
     * @return page
     */
    @Override
    public Page<Participant> findAll(Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Saves an entity to the database
     *
     * @param entity to save
     * @param <S> class of entity
     * @return saved entity
     */
    @Override
    public <S extends Participant> S save(S entity) {
        call("save");
        if(entity.getEventID() == null) return null;
        Optional<Event> optionalEvent = eventRepo.getEvents()
                .stream().filter(e -> e.getId().equals(entity.getEventID())).findAny();
        if(optionalEvent.isEmpty()) return null;
        for(int i = 0; i < participants.size(); i++) {
            Participant p = participants.get(i);
            if (p.getId() == entity.getId()
                    && p.getEventID().equals(entity.getEventID())) {
                participants.remove(i);
                participants.add(i, entity);
                optionalEvent.get().getParticipants().remove(p);
                optionalEvent.get().addParticipant(entity);
                return entity;
            }
        }
        entity.setId(random.nextLong());
        participants.add(entity);
        optionalEvent.get().addParticipant(entity);

        return entity;
    }

    /**
     * @param id id
     * @return quote
     */
    @Override
    public Optional<Participant> findById(EventWeakKey id) {
        // TODO Auto-generated method stub
        call("findById");
        return participants.stream()
                .filter(e -> e.getId() == id.getId() && e.getEventID().equals(id.getEventID()))
                .findAny();
    }

    /**
     * @param id to search
     * @return true if present
     */
    @Override
    public boolean existsById(EventWeakKey id) {
        call("existsById");
        return find(id).isPresent();
    }

    /**
     * @return the amount of quotes
     */
    @Override
    public long count() {
        return participants.size();
    }

    /**
     * @param id to delete by
     */
    @Override
    public void deleteById(EventWeakKey id) {
        calledMethods.add("deleteById");
        if(!participants.removeIf(p -> p.getId() == id.getId()
                && p.getEventID().equals(id.getEventID()))) return;
        eventRepo.getEvents().stream().filter(e -> e.getId().equals(id.getEventID()))
                .findAny().get().getParticipants().removeIf(p -> p.getId() == id.getId());
    }

    /**
     * @param entity to delete
     */
    @Override
    public void delete(Participant entity) {
        calledMethods.add("delete");
        if(!participants.remove(entity)) return;
        eventRepo.getEvents().stream().filter(e -> e.getId().equals(entity.getEventID()))
                .findAny().get().getParticipants().remove(entity);
    }

    /**
     * @param ids to delete by
     */
    @Override
    public void deleteAllById(Iterable<? extends EventWeakKey> ids) {
        // TODO Auto-generated method stub

    }

    /**
     * @param entities to delete
     */
    @Override
    public void deleteAll(Iterable<? extends Participant> entities) {
        // TODO Auto-generated method stub

    }

    /**
     * delete everything
     */
    @Override
    public void deleteAll() {
        participants.clear();
    }

    /**
     * @param example entity
     * @param <S> class
     * @return quote
     */
    @Override
    public <S extends Participant> Optional<S> findOne(Example<S> example) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    /**
     * @param example entity
     * @param pageable p
     * @param <S> class
     * @return page
     */
    @Override
    public <S extends Participant> Page<S> findAll(Example<S> example, Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param example entity
     * @param <S> class
     * @return count
     */
    @Override
    public <S extends Participant> long count(Example<S> example) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param example entity
     * @param <S> class
     * @return true iff exists
     */
    @Override
    public <S extends Participant> boolean exists(Example<S> example) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param example entity
     * @param queryFunction function
     * @param <S> class
     * @param <R> a
     * @return a
     */
    @Override
    public <S extends Participant, R> R findBy(Example<S> example,
                                         Function<FetchableFluentQuery<S>, R> queryFunction) {
        // TODO Auto-generated method stub
        return null;
    }
}