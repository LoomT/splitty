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

    /**
     * Constructor-method for TestTagRepository
     * @param eventRepo TestEventRepository
     */
    public TestTagRepository(TestEventRepository eventRepo){
        this.eventRepo= eventRepo;
    }

    /**
     * flush method
     */
    @Override
    public void flush() {

    }

    /**
     * @param entity entity to be saved. Must not be {@literal null}.
     * @return the entity that was saved
     */
    @Override
    public <S extends Tag> S saveAndFlush(S entity) {
        return null;
    }

    /**
     * @param entities entities to be saved. Must not be {@literal null}.
     * @return the list of entities that have been saved
     * @param <S> extendable from Tag
     */
    @Override
    public <S extends Tag> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    /**
     * @param entities entities to be deleted. Must not be {@literal null}.
     */
    @Override
    public void deleteAllInBatch(Iterable<Tag> entities) {
    }

    /**
     * @param eventWeakKeys the ids of the entities to be deleted. Must not be {@literal null}.
     */
    @Override
    public void deleteAllByIdInBatch(Iterable<EventWeakKey> eventWeakKeys) {
    }

    /**
     * deletes everything
     */
    @Override
    public void deleteAllInBatch() {

    }

    /**
     * @param eventWeakKey must not be {@literal null}.
     * @return Tag found from ID
     */
    @Override
    public Tag getOne(EventWeakKey eventWeakKey) {
        return null;
    }

    /**
     * @param eventWeakKey must not be {@literal null}.
     * @return Tag found from ID
     */
    @Override
    public Tag getById(EventWeakKey eventWeakKey) {
        return null;
    }

    /**
     * @param eventWeakKey must not be {@literal null}.
     * @return reference for Tag found from ID
     */
    @Override
    public Tag getReferenceById(EventWeakKey eventWeakKey) {
        return null;
    }

    /**
     * @param example must not be {@literal null}.
     * @return the found example
     * @param <S> extendable from Tag
     */
    @Override
    public <S extends Tag> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    /**
     * @param example must not be {@literal null}.
     * @return the list with the found tags
     * @param <S> extendable from Tag
     */
    @Override
    public <S extends Tag> List<S> findAll(Example<S> example) {
        return List.of();
    }

    /**
     * @param example must not be {@literal null}.
     * @param sort the {@link Sort} specification to sort the
     *             results by, may be {@link Sort#unsorted()}, must not be
     *          {@literal null}.
     * @return list
     * @param <S> Tag
     */
    @Override
    public <S extends Tag> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    /**
     * @param example must not be {@literal null}.
     * @param pageable the pageable to request a
     *                 paged result, can be {@link Pageable#unpaged()}, must not be
     *          {@literal null}.
     * @return result
     * @param <S> Tag
     */
    @Override
    public <S extends Tag> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    /**
     * @param example the {@link Example} to count instances for. Must not be {@literal null}.
     * @return result
     * @param <S> tag
     */
    @Override
    public <S extends Tag> long count(Example<S> example) {
        return 0;
    }

    /**
     * @param example the {@link Example} to use for
     *                the existence check. Must not be {@literal null}.
     * @return result
     * @param <S> tag
     */
    @Override
    public <S extends Tag> boolean exists(Example<S> example) {
        return false;
    }

    /**
     * @param example must not be {@literal null}.
     * @param queryFunction the query function defining projection, sorting, and the result type
     * @return result
     * @param <S> tag
     * @param <R> query
     */
    @Override
    public <S extends Tag, R> R findBy(Example<S> example,
                                       Function<FluentQuery.FetchableFluentQuery<S>, R>
                                               queryFunction) {
        return null;
    }

    /**
     * @param entity must not be {@literal null}.
     * @return the entity that has been saved
     * @param <S> extendable from Tag
     */
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

    /**
     * @param entities must not be {@literal null} nor must it contain {@literal null}.
     * @return result
     * @param <S> tag
     */
    @Override
    public <S extends Tag> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    /**
     * @param eventWeakKey must not be {@literal null}.
     * @return the tag found by ID
     */
    @Override
    public Optional<Tag> findById(EventWeakKey eventWeakKey) {
        for(Tag t : tags) {
            if(t.getEventID().equals(eventWeakKey.getEventID())){
                if(t.getId() == eventWeakKey.getId()){return Optional.of(t);}
            }
        }
        return Optional.empty();
    }

    /**
     * @param eventWeakKey must not be {@literal null}.
     * @return result
     */
    @Override
    public boolean existsById(EventWeakKey eventWeakKey) {
        return false;
    }

    /**
     * @return result
     */
    @Override
    public List<Tag> findAll() {
        return List.of();
    }

    /**
     * @param eventWeakKeys must not be
     * {@literal null} nor contain any {@literal null} values.
     * @return result
     */
    @Override
    public List<Tag> findAllById(Iterable<EventWeakKey> eventWeakKeys) {
        return List.of();
    }

    /**
     * @return result
     */
    @Override
    public long count() {
        return 0;
    }

    /**
     * @param eventWeakKey must not be {@literal null}.
     */
    @Override
    public void deleteById(EventWeakKey eventWeakKey) {

    }

    /**
     * @param entity must not be {@literal null}.
     */
    @Override
    public void delete(Tag entity) {

    }

    /**
     * @param eventWeakKeys must not be {@literal null}. Must not contain {@literal null} elements.
     */
    @Override
    public void deleteAllById(Iterable<? extends EventWeakKey> eventWeakKeys) {

    }

    /**
     * @param entities must not be {@literal null}. Must not contain {@literal null} elements.
     */
    @Override
    public void deleteAll(Iterable<? extends Tag> entities) {

    }

    /**
     * deletes everything
     */
    @Override
    public void deleteAll() {

    }

    /**
     * @param sort the {@link Sort} specification to sort the results by,
     *             can be {@link Sort#unsorted()}, must not be
     *          {@literal null}.
     * @return result
     */
    @Override
    public List<Tag> findAll(Sort sort) {
        return List.of();
    }

    /**
     * @param pageable the pageable to request a paged result,
     *                 can be {@link Pageable#unpaged()}, must not be
     *          {@literal null}.
     * @return result
     */
    @Override
    public Page<Tag> findAll(Pageable pageable) {
        return null;
    }
}
