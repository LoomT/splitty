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
import commons.Expense;
import commons.Participant;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import server.database.ExpenseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@SuppressWarnings("NullableProblems")
public class TestExpenseRepository implements ExpenseRepository {
    private final List<Expense> expenses = new ArrayList<>();
    private final List<String> calledMethods = new ArrayList<>();
    private final TestRandom random = new TestRandom();
    private TestEventRepository eventRepo;

    /**
     * @param eventRepo the test event repository
     */
    public void setEventRepo(TestEventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    /**
     * @return called methods
     */
    public List<String> getCalledMethods() {
        return calledMethods;
    }

    /**
     * @param name name of call
     */
    private void call(String name) {
        calledMethods.add(name);
    }

    /**
     * @return expense list
     */
    public List<Expense> getExpenses() {
        return expenses;
    }
    /**
     * flush
     */
    @Override
    public void flush() {

    }

    /**
     * @param entity entity
     * @param <S> class
     * @return saved entity
     */
    @Override
    public <S extends Expense> S saveAndFlush(S entity) {
        return null;
    }

    /**
     * @param entities to save
     * @param <S> class
     * @return list
     */
    @Override
    public <S extends Expense> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    /**
     * @param entities to delete
     */
    @Override
    public void deleteAllInBatch(Iterable<Expense> entities) {

    }

    /**
     * @param ids to delete by
     */
    @Override
    public void deleteAllByIdInBatch(Iterable<EventWeakKey> ids) {

    }

    /**
     * delete
     */
    @Override
    public void deleteAllInBatch() {

    }

    /**
     * @param id id
     * @return Expense
     */
    @Override
    public Expense getOne(EventWeakKey id) {
        return null;
    }

    /**
     * @param id id
     * @return Expense
     */
    @Override
    public Expense getById(EventWeakKey id) {
        return null;
    }

    /**
     * @param id id
     * @return Expense
     */
    @Override
    public Expense getReferenceById(EventWeakKey id) {
        call("getReferenceById");
        return find(id).orElse(null);
    }

    /**
     * @param example entity
     * @param <S> class
     * @return list
     */
    @Override
    public <S extends Expense> List<S> findAll(Example<S> example) {
        return null;
    }

    /**
     * @param example entity
     * @param sort sort
     * @param <S> class
     * @return list of Expenses
     */
    @Override
    public <S extends Expense> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    /**
     * @param entities to save
     * @param <S> class
     * @return list of saved entities
     */
    @Override
    public <S extends Expense> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        entities.forEach(e -> saved.add(save(e)));
        return saved;
    }

    /**
     * @return all expenses
     */
    @Override
    public List<Expense> findAll() {
        calledMethods.add("findAll");
        return expenses;
    }

    /**
     * @param ids to find by
     * @return list
     */
    @Override
    public List<Expense> findAllById(Iterable<EventWeakKey> ids) {
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
    public <S extends Expense> S save(S entity) {
        call("save");
        if(entity.getEventID() == null) return null;
        Optional<Event> optionalEvent = eventRepo.getEvents()
                .stream().filter(e -> e.getId().equals(entity.getEventID())).findAny();
        if(optionalEvent.isEmpty()) return null;
        Event event = optionalEvent.get();

        // check if the participants in expense are in the event
        Set<Long> participantIds = event.getParticipants().stream()
                .map(Participant::getId).collect(Collectors.toSet());
        if(!participantIds.contains(entity.getExpenseAuthor().getId()))
            return null;
        for(Participant expenseParticipant : entity.getExpenseParticipants()) {
            if(!participantIds.contains(expenseParticipant.getId()))
                return null;
        }

        // check if there is already an expense with the same id and overwrite it if yes
        for(int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            if (e.getId() == entity.getId()
                    && e.getEventID().equals(entity.getEventID())) {
                expenses.remove(i);
                expenses.add(i, entity);
                optionalEvent.get().getExpenses().remove(e);
                optionalEvent.get().addExpense(entity);
                return entity;
            }
        }
        // if it's a new expense, generate an id and save
        entity.setId(random.nextLong());
        expenses.add(entity);
        event.addExpense(entity);

        return entity;
    }

    /**
     * Replaces the old expense while keeping the same object address
     *
     * @param oldExp old expense
     * @param newExp new expense
     */
    private void replaceFields(Expense oldExp, Expense newExp) {
        oldExp.setAmount(newExp.getAmount().toString());
        oldExp.setCurrency(newExp.getCurrency());
        oldExp.setExpenseAuthor(newExp.getExpenseAuthor());
        oldExp.setPurpose(newExp.getPurpose());
        oldExp.setType(newExp.getType());
        oldExp.getExpenseParticipants().clear();
        oldExp.getExpenseParticipants().addAll(newExp.getExpenseParticipants());
    }

    /**
     * @param id composite expense key
     * @return expense or null if not found
     */
    public Optional<Expense> find(EventWeakKey id) {
        return expenses.stream().filter(e -> e.getId() == id.getId()
                && e.getEventID().equals(id.getEventID())).findAny();
    }

    /**
     * @param id id
     * @return Expense
     */
    @Override
    public Optional<Expense> findById(EventWeakKey id) {
        call("findById");
        return find(id);
    }

    /**
     * @param id to search
     * @return true if present
     */
    @Override
    public boolean existsById(EventWeakKey id) {
        call("existsById");
        return findById(id).isPresent();
    }

    /**
     * @return the amount of expenses
     */
    @Override
    public long count() {
        return expenses.size();
    }

    /**
     * @param id to delete by
     */
    @Override
    public void deleteById(EventWeakKey id) {
        call("deleteById");
        if(!expenses.removeIf(e -> e.getId() == id.getId()
                && e.getEventID().equals(id.getEventID()))) return;
        eventRepo.getEvents().stream().filter(e -> e.getId().equals(id.getEventID()))
                .findAny().get().getExpenses().removeIf(e -> e.getId() == id.getId());
    }

    /**
     * @param entity to delete
     */
    @Override
    public void delete(Expense entity) {
        call("delete");
        if(!expenses.remove(entity)) return;
        eventRepo.getEvents().stream().filter(e -> e.getId().equals(entity.getEventID()))
                .findAny().get().getExpenses().remove(entity);
    }

    /**
     * @param ids to delete by
     */
    @Override
    public void deleteAllById(Iterable<? extends EventWeakKey> ids) {

    }

    /**
     * @param entities to delete
     */
    @Override
    public void deleteAll(Iterable<? extends Expense> entities) {

    }

    /**
     * delete everything
     */
    @Override
    public void deleteAll() {
        expenses.clear();
    }

    /**
     * @param sort for sorting function
     * @return sorted list
     */
    @Override
    public List<Expense> findAll(Sort sort) {
        return null;
    }

    /**
     * @param pageable p
     * @return page
     */
    @Override
    public Page<Expense> findAll(Pageable pageable) {
        return null;
    }

    /**
     * @param example entity
     * @param <S> class
     * @return Expense
     */
    @Override
    public <S extends Expense> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    /**
     * @param example entity
     * @param pageable p
     * @param <S> class
     * @return page
     */
    @Override
    public <S extends Expense> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    /**
     * @param example entity
     * @param <S> class
     * @return count
     */
    @Override
    public <S extends Expense> long count(Example<S> example) {
        return 0;
    }

    /**
     * @param example entity
     * @param <S> class
     * @return true if and only idf it exists
     */
    @Override
    public <S extends Expense> boolean exists(Example<S> example) {
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
    public <S extends Expense, R> R findBy(Example<S> example,
                                           Function<FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

}
