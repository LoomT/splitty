package server.api;

import commons.Event;
import commons.EventWeakKey;
import commons.Tag;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import server.database.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.random.RandomGenerator;


@SuppressWarnings("NullableProblems")
public class TestTagRepository implements TagRepository {
    private final List<Tag> tags = new ArrayList<>();
    private final List<String> calledMethods = new ArrayList<>();
    private final RandomGenerator random = new TestRandom();
    private final TestEventRepository eventRepo;

    public TestTagRepository(TestEventRepository eventRepo){
        this.eventRepo= eventRepo;
    }
    @Override
    public void flush() {

    }

    @Override
    public <S extends Tag> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Tag> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Tag> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<EventWeakKey> eventWeakKeys) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Tag getOne(EventWeakKey eventWeakKey) {
        return null;
    }

    @Override
    public Tag getById(EventWeakKey eventWeakKey) {
        return null;
    }

    @Override
    public Tag getReferenceById(EventWeakKey eventWeakKey) {
        return null;
    }

    @Override
    public <S extends Tag> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Tag> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends Tag> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends Tag> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Tag> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Tag> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Tag, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends Tag> S save(S entity) {
        calledMethods.add("save");
        if(entity.getEventID() == null) return null;
        Optional<Event> optionalEvent = eventRepo.getEvents()
                .stream().filter(e -> e.getId().equals(entity.getEventID())).findAny();
        if(optionalEvent.isEmpty()) return null;
        for(int i = 0; i < tags.size(); i++) {
            Tag p = tags.get(i);
            if (p.getId() == entity.getId()
                    && p.getEventID().equals(entity.getEventID())) {
                tags.remove(i);
                tags.add(i, entity);
                optionalEvent.get().getTags().remove(p);
                optionalEvent.get().getTags().add(p);
                return entity;
            }
        }
        entity.setId(random.nextLong());
        tags.add(entity);
        optionalEvent.get().getTags().add(entity);

        return entity;
    }

    @Override
    public <S extends Tag> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<Tag> findById(EventWeakKey eventWeakKey) {
        for(Tag t : tags) {
            if(t.getEventID().equals(eventWeakKey.getEventID())){
                if(t.getId() == eventWeakKey.getId()){return Optional.of(t);}
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(EventWeakKey eventWeakKey) {
        return false;
    }

    @Override
    public List<Tag> findAll() {
        return List.of();
    }

    @Override
    public List<Tag> findAllById(Iterable<EventWeakKey> eventWeakKeys) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(EventWeakKey eventWeakKey) {

    }

    @Override
    public void delete(Tag entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends EventWeakKey> eventWeakKeys) {

    }

    @Override
    public void deleteAll(Iterable<? extends Tag> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<Tag> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<Tag> findAll(Pageable pageable) {
        return null;
    }
}
