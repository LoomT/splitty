package server.api;

import commons.Event;
import commons.EventWeakKey;
import commons.Transaction;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import server.database.TransactionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class TestTransactionRepository implements TransactionRepository {
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<String> calledMethods = new ArrayList<>();
    private final RandomGenerator random = new TestRandom();
    private TestEventRepository eventRepo = null;

    /**
     * @param repo event repo
     */
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
     * @return transaction list
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }
    /**
     * @return all quotes
     */
    @Override
    public List<Transaction> findAll() {
        calledMethods.add("findAll");
        return transactions;
    }

    /**
     * @param sort sort
     * @return list
     */
    @Override
    public List<Transaction> findAll(Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param ids to find by
     * @return list
     */
    @Override
    public List<Transaction> findAllById(Iterable<EventWeakKey> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param entities to save
     * @param <S> clas
     * @return list of saved
     */
    @Override
    public <S extends Transaction> List<S> saveAll(Iterable<S> entities) {
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
    public <S extends Transaction> S saveAndFlush(S entity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param entities to save
     * @param <S> class
     * @return list
     */
    @Override
    public <S extends Transaction> List<S> saveAllAndFlush(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param entities to delete
     */
    @Override
    public void deleteAllInBatch(Iterable<Transaction> entities) {
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
    public Transaction getOne(EventWeakKey id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param id id
     * @return Transaction
     */
    @Override
    public Transaction getById(EventWeakKey id) {
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
    public Transaction getReferenceById(EventWeakKey id) {
        call("getReferenceById");
        if(find(id).isEmpty()) return null;
        return find(id).get();
    }

    /**
     * @param id id
     * @return quote
     */
    private Optional<Transaction> find(EventWeakKey id) {
        return transactions.stream()
                .filter(q -> q.getId() == id.getId() && q.getEventID().equals(id.getEventID()))
                .findFirst();
    }

    /**
     * @param example entity
     * @param <S> class
     * @return list
     */
    @Override
    public <S extends Transaction> List<S> findAll(Example<S> example) {
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
    public <S extends Transaction> List<S> findAll(Example<S> example, Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param pageable p
     * @return page
     */
    @Override
    public Page<Transaction> findAll(Pageable pageable) {
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
    public <S extends Transaction> S save(S entity) {
        call("save");
        if(entity.getEventID() == null) return null;
        Optional<Event> optionalEvent = eventRepo.getEvents()
                .stream().filter(e -> e.getId().equals(entity.getEventID())).findAny();
        if(optionalEvent.isEmpty()) return null;
        for(int i = 0; i < transactions.size(); i++) {
            Transaction p = transactions.get(i);
            if (p.getId() == entity.getId()
                    && p.getEventID().equals(entity.getEventID())) {
                transactions.remove(i);
                Transaction clone = entity.clone();
                transactions.add(i, clone);
                optionalEvent.get().getTransactions().remove(p);
                optionalEvent.get().addTransaction(clone);
                return (S) entity.clone();
            }
        }
        Transaction clone = entity.clone();
        clone.setId(random.nextLong());
        transactions.add(clone);
        optionalEvent.get().addTransaction(clone);

        return (S) clone;
    }

    /**
     * @param id id
     * @return quote
     */
    @Override
    public Optional<Transaction> findById(EventWeakKey id) {
        // TODO Auto-generated method stub
        call("findById");
        return transactions.stream()
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
        return transactions.size();
    }

    /**
     * @param id to delete by
     */
    @Override
    public void deleteById(EventWeakKey id) {
        calledMethods.add("deleteById");
        if(!transactions.removeIf(p -> p.getId() == id.getId()
                && p.getEventID().equals(id.getEventID()))) return;
        eventRepo.getEvents().stream().filter(e -> e.getId().equals(id.getEventID()))
                .findAny().get().getTransactions().removeIf(p -> p.getId() == id.getId());
    }

    /**
     * @param entity to delete
     */
    @Override
    public void delete(Transaction entity) {
        calledMethods.add("delete");
        if(!transactions.remove(entity)) return;
        eventRepo.getEvents().stream().filter(e -> e.getId().equals(entity.getEventID()))
                .findAny().get().getTransactions().remove(entity);
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
    public void deleteAll(Iterable<? extends Transaction> entities) {
        // TODO Auto-generated method stub

    }

    /**
     * delete everything
     */
    @Override
    public void deleteAll() {
        transactions.clear();
    }

    /**
     * @param example entity
     * @param <S> class
     * @return quote
     */
    @Override
    public <S extends Transaction> Optional<S> findOne(Example<S> example) {
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
    public <S extends Transaction> Page<S> findAll(Example<S> example, Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param example entity
     * @param <S> class
     * @return count
     */
    @Override
    public <S extends Transaction> long count(Example<S> example) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param example entity
     * @param <S> class
     * @return true iff exists
     */
    @Override
    public <S extends Transaction> boolean exists(Example<S> example) {
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
    public <S extends Transaction, R> R findBy(Example<S> example,
                                               Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        // TODO Auto-generated method stub
        return null;
    }
}
