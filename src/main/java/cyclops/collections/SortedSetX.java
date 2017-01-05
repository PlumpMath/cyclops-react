package cyclops.collections;

import com.aol.cyclops2.data.collections.extensions.lazy.LazySortedSetX;
import com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX;
import cyclops.Streams;
import cyclops.collections.immutable.PVectorX;
import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import com.aol.cyclops2.types.OnEmptySwitch;
import com.aol.cyclops2.types.To;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SortedSetX<T> extends To<SortedSetX<T>>,SortedSet<T>, MutableCollectionX<T>, OnEmptySwitch<T, SortedSet<T>> {
    static <T> Collector<T, ?, SortedSet<T>> defaultCollector() {
        return Collectors.toCollection(() -> new TreeSet<T>(
                                                            (Comparator) Comparator.<Comparable> naturalOrder()));
    }

    static <T> Collector<T, ?, SortedSet<T>> immutableCollector() {
        return Collectors.collectingAndThen(defaultCollector(), (final SortedSet<T> d) -> Collections.unmodifiableSortedSet(d));

    }

    /**
    * Create a SortedSetX that contains the Integers between skip and take
    * 
    * @param start
    *            Number of range to skip from
    * @param end
    *            Number for range to take at
    * @return Range SortedSetX
    */
    public static SortedSetX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .toSortedSetX();
    }

    /**
     * Create a SortedSetX that contains the Longs between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range SortedSetX
     */
    public static SortedSetX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .toSortedSetX();
    }

    /**
     * Unfold a function into a SortedSetX
     * 
     * <pre>
     * {@code 
     *  SortedSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return SortedSetX generated by unfolder function
     */
    static <U, T> SortedSetX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .toSortedSetX();
    }

    /**
     * Generate a SortedSetX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate SortedSetX elements
     * @return SortedSetX generated from the provided Supplier
     */
    public static <T> SortedSetX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .toSortedSetX();
    }

    /**
     * Create a SortedSetX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return SortedSetX generated by iterative application
     */
    public static <T> SortedSetX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .toSortedSetX();

    }

    public static <T> SortedSetX<T> empty() {
        return fromIterable((SortedSet<T>) defaultCollector().supplier()
                                                             .get());
    }

    public static <T> SortedSetX<T> of(final T... values) {
        final SortedSet<T> res = (SortedSet<T>) defaultCollector().supplier()
                                                                  .get();
        for (final T v : values)
            res.add(v);
        return fromIterable(res);
    }

    public static <T> SortedSetX<T> singleton(final T value) {
        return of(value);
    }

    @Override
    default SortedSetX<T> materialize() {
        return (SortedSetX<T>)MutableCollectionX.super.materialize();
    }


    @Override
    default SortedSetX<T> take(final long num) {

        return (SortedSetX<T>) MutableCollectionX.super.limit(num);
    }
    @Override
    default SortedSetX<T> drop(final long num) {

        return (SortedSetX<T>) MutableCollectionX.super.skip(num);
    }
    /**
     * Construct a SortedSetX from an Publisher
     * 
     * @param publisher
     *            to construct SortedSetX from
     * @return SortedSetX
     */
    public static <T> SortedSetX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>) publisher)
                          .toSortedSetX();
    }

    public static <T> SortedSetX<T> fromIterable(final Iterable<T> it) {
        if (it instanceof SortedSetX)
            return (SortedSetX<T>) it;
        if (it instanceof SortedSet)
            return new LazySortedSetX<T>(
                                         (SortedSet) it, defaultCollector());
        return new LazySortedSetX<T>(
                                     Streams.stream(it)
                                                .collect(defaultCollector()),
                                                defaultCollector());
    }
    public static <T> SortedSetX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }

    public static <T> SortedSetX<T> fromIterable(final Collector<T, ?, SortedSet<T>> collector, final Iterable<T> it) {
        if (it instanceof SortedSetX)
            return ((SortedSetX<T>) it).withCollector(collector);
        if (it instanceof SortedSet)
            return new LazySortedSetX<T>(
                                         (SortedSet) it, collector);
        return new LazySortedSetX<T>(
                                     Streams.stream(it)
                                                .collect(collector),
                                     collector);
    }
    
    SortedSetX<T> withCollector(Collector<T, ?, SortedSet<T>> collector);

    /**
     * coflatMap pattern, can be used to perform lazy reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *     SortedSetX.of(1,2,3)
     *               .map(i->i*2)
     *               .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *      //SortedSetX[12]
     * }
     * </pre>
     * 
     * 
     * @param fn mapping function
     * @return Transformed SortedSet
     */
    default <R> SortedSetX<R> coflatMap(Function<? super SortedSetX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> SortedSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (SortedSetX)MutableCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> SortedSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (SortedSetX)MutableCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> SortedSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (SortedSetX)MutableCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> SortedSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (SortedSetX)MutableCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> SortedSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (SortedSetX)MutableCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> SortedSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (SortedSetX)MutableCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.sequence.traits.ConvertableSequence#toListX()
     */
    @Override
    default SortedSetX<T> toSortedSetX() {
        return this;
    }

    /**
     * Combine two adjacent elements in a SortedSetX using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  SortedSetX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced SortedSetX
     */
    @Override
    default SortedSetX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (SortedSetX<T>) MutableCollectionX.super.combine(predicate, op);
    }
   
    @Override
    default <R> SortedSetX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> SortedSetX<R> unit(final Collection<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> SortedSetX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    @Override
    default <T1> SortedSetX<T1> from(final Collection<T1> c) {
        return SortedSetX.<T1> fromIterable(getCollector(), c);
    }

    public <T> Collector<T, ?, SortedSet<T>> getCollector();

    @Override
    default <X> SortedSetX<X> fromStream(final Stream<X> stream) {
        return new LazySortedSetX<>(
                                    stream.collect(getCollector()), getCollector());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#reverse()
     */
    @Override
    default SortedSetX<T> reverse() {
        return (SortedSetX<T>) MutableCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> filter(final Predicate<? super T> pred) {

        return (SortedSetX<T>) MutableCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> SortedSetX<R> map(final Function<? super T, ? extends R> mapper) {

        return (SortedSetX<R>) MutableCollectionX.super.<R> map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> SortedSetX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (SortedSetX<R>) MutableCollectionX.super.<R> flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#limit(long)
     */
    @Override
    default SortedSetX<T> limit(final long num) {
        return (SortedSetX<T>) MutableCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#skip(long)
     */
    @Override
    default SortedSetX<T> skip(final long num) {

        return (SortedSetX<T>) MutableCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> takeWhile(final Predicate<? super T> p) {

        return (SortedSetX<T>) MutableCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> dropWhile(final Predicate<? super T> p) {

        return (SortedSetX<T>) MutableCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> takeUntil(final Predicate<? super T> p) {

        return (SortedSetX<T>) MutableCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> dropUntil(final Predicate<? super T> p) {

        return (SortedSetX<T>) MutableCollectionX.super.dropUntil(p);
    }

    @Override
    default SortedSetX<T> takeRight(final int num) {
        return (SortedSetX<T>) MutableCollectionX.super.takeRight(num);
    }

    @Override
    default SortedSetX<T> dropRight(final int num) {
        return (SortedSetX<T>) MutableCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> SortedSetX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (SortedSetX<R>) MutableCollectionX.super.<R> trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#slice(long, long)
     */
    @Override
    default SortedSetX<T> slice(final long from, final long to) {

        return (SortedSetX<T>) MutableCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> SortedSetX<T> sorted(final Function<? super T, ? extends U> function) {

        return (SortedSetX<T>) MutableCollectionX.super.sorted(function);
    }

    @Override
    default SortedSetX<ListX<T>> grouped(final int groupSize) {
        return (SortedSetX<ListX<T>>) (SortedSetX<T>) MutableCollectionX.super.grouped(groupSize);
    }

    @Override
    default <K, A, D> SortedSetX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return (SortedSetX) MutableCollectionX.super.grouped(classifier, downstream);
    }

    @Override
    default <K> SortedSetX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return (SortedSetX) fromStream(stream().grouped(classifier)
                                               .map(t -> t.map2(Comparables::comparable)));
    }

    @Override
    default <U> SortedSetX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (SortedSetX<Tuple2<T, U>>) (SortedSetX<T>) MutableCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> SortedSetX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (SortedSetX<R>) MutableCollectionX.super.zip(other, zipper);
    }


    @Override
    default <U, R> SortedSetX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (SortedSetX<R>) MutableCollectionX.super.zipS(other, zipper);
    }

    @Override
    default SortedSetX<PVectorX<T>> sliding(final int windowSize) {
        return (SortedSetX<PVectorX<T>>) (SortedSetX<T>) MutableCollectionX.super.sliding(windowSize);
    }

    @Override
    default SortedSetX<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return (SortedSetX<PVectorX<T>>) (SortedSetX<T>) MutableCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default SortedSetX<T> scanLeft(final Monoid<T> monoid) {
        return (SortedSetX<T>) MutableCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> SortedSetX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (SortedSetX<U>) (SortedSetX<T>) MutableCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default SortedSetX<T> scanRight(final Monoid<T> monoid) {
        return (SortedSetX<T>) MutableCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> SortedSetX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (SortedSetX<U>) (SortedSetX<T>) MutableCollectionX.super.scanRight(identity, combiner);
    }

    @Override
    default SortedSetX<T> plus(final T e) {
        add(e);
        return this;
    }

    @Override
    default SortedSetX<T> plusAll(final Collection<? extends T> list) {
        addAll(list);
        return this;
    }

    @Override
    default SortedSetX<T> minus(final Object e) {
        remove(e);
        return this;
    }

    @Override
    default SortedSetX<T> minusAll(final Collection<?> list) {
        removeAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycle(int)
     */
    @Override
    default ListX<T> cycle(final long times) {

        return this.stream()
                   .cycle(times)
                   .toListX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default ListX<T> cycle(final Monoid<T> m, final long times) {

        return this.stream()
                   .cycle(m, times)
                   .toListX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleWhile(predicate)
                   .toListX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleUntil(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleUntil(predicate)
                   .toListX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#zip(java.util.stream.Stream)
     */
    @Override
    default <U> SortedSetX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (SortedSetX) MutableCollectionX.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> SortedSetX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (SortedSetX) MutableCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> SortedSetX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (SortedSetX) MutableCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#zipWithIndex()
     */
    @Override
    default SortedSetX<Tuple2<T, Long>> zipWithIndex() {

        return (SortedSetX<Tuple2<T, Long>>) MutableCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#distinct()
     */
    @Override
    default SortedSetX<T> distinct() {

        return (SortedSetX<T>) MutableCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#sorted()
     */
    @Override
    default SortedSetX<T> sorted() {

        return (SortedSetX<T>) MutableCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default SortedSetX<T> sorted(final Comparator<? super T> c) {

        return (SortedSetX<T>) MutableCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> skipWhile(final Predicate<? super T> p) {

        return (SortedSetX<T>) MutableCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> skipUntil(final Predicate<? super T> p) {

        return (SortedSetX<T>) MutableCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> limitWhile(final Predicate<? super T> p) {

        return (SortedSetX<T>) MutableCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> limitUntil(final Predicate<? super T> p) {

        return (SortedSetX<T>) MutableCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default SortedSetX<T> intersperse(final T value) {

        return (SortedSetX<T>) MutableCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#shuffle()
     */
    @Override
    default SortedSetX<T> shuffle() {

        return (SortedSetX<T>) MutableCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#skipLast(int)
     */
    @Override
    default SortedSetX<T> skipLast(final int num) {

        return (SortedSetX<T>) MutableCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#limitLast(int)
     */
    @Override
    default SortedSetX<T> limitLast(final int num) {

        return (SortedSetX<T>) MutableCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default SortedSetX<T> onEmptySwitch(final Supplier<? extends SortedSet<T>> supplier) {
        if (isEmpty())
            return SortedSetX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default SortedSetX<T> onEmpty(final T value) {

        return (SortedSetX<T>) MutableCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default SortedSetX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (SortedSetX<T>) MutableCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> SortedSetX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (SortedSetX<T>) MutableCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
     */
    @Override
    default SortedSetX<T> shuffle(final Random random) {

        return (SortedSetX<T>) MutableCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> SortedSetX<U> ofType(final Class<? extends U> type) {

        return (SortedSetX<U>) MutableCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> filterNot(final Predicate<? super T> fn) {

        return (SortedSetX<T>) MutableCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#notNull()
     */
    @Override
    default SortedSetX<T> notNull() {

        return (SortedSetX<T>) MutableCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#removeAllS(java.util.stream.Stream)
     */
    @Override
    default SortedSetX<T> removeAllS(final Stream<? extends T> stream) {

        return (SortedSetX<T>) MutableCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#removeAllS(java.lang.Iterable)
     */
    @Override
    default SortedSetX<T> removeAllS(final Iterable<? extends T> it) {

        return (SortedSetX<T>) MutableCollectionX.super.removeAllS(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#removeAllS(java.lang.Object[])
     */
    @Override
    default SortedSetX<T> removeAllS(final T... values) {

        return (SortedSetX<T>) MutableCollectionX.super.removeAllS(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#retainAllS(java.lang.Iterable)
     */
    @Override
    default SortedSetX<T> retainAllS(final Iterable<? extends T> it) {

        return (SortedSetX<T>) MutableCollectionX.super.retainAllS(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#retainAllS(java.util.stream.Stream)
     */
    @Override
    default SortedSetX<T> retainAllS(final Stream<? extends T> seq) {

        return (SortedSetX<T>) MutableCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#retainAllS(java.lang.Object[])
     */
    @Override
    default SortedSetX<T> retainAllS(final T... values) {

        return (SortedSetX<T>) MutableCollectionX.super.retainAllS(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cast(java.lang.Class)
     */
    @Override
    default <U> SortedSetX<U> cast(final Class<? extends U> type) {

        return (SortedSetX<U>) MutableCollectionX.super.cast(type);
    }


    @Override
    default <C extends Collection<? super T>> SortedSetX<C> grouped(final int size, final Supplier<C> supplier) {

        return (SortedSetX<C>) MutableCollectionX.super.grouped(size, supplier);
    }

    @Override
    default SortedSetX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (SortedSetX<ListX<T>>) MutableCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default SortedSetX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (SortedSetX<ListX<T>>) MutableCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> SortedSetX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (SortedSetX<C>) MutableCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> SortedSetX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (SortedSetX<C>) MutableCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default SortedSetX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (SortedSetX<ListX<T>>) MutableCollectionX.super.groupedStatefullyUntil(predicate);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.ExtendedTraversable#permutations()
     */
    @Override
    default SortedSetX<ReactiveSeq<T>> permutations() {
        return fromStream(stream().permutations()
                                  .map(Comparables::comparable));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.ExtendedTraversable#combinations(int)
     */
    @Override
    default SortedSetX<ReactiveSeq<T>> combinations(final int size) {
        return fromStream(stream().combinations(size)
                                  .map(Comparables::comparable));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.ExtendedTraversable#combinations()
     */
    @Override
    default SortedSetX<ReactiveSeq<T>> combinations() {
        return fromStream(stream().combinations()
                                  .map(Comparables::comparable));
    }


    @Override
    default <R> SortedSetX<R> retry(final Function<? super T, ? extends R> fn) {
        return (SortedSetX<R>)MutableCollectionX.super.retry(fn);
    }

    @Override
    default <R> SortedSetX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (SortedSetX<R>)MutableCollectionX.super.retry(fn);
    }

    @Override
    default <R> SortedSetX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (SortedSetX<R>)MutableCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> SortedSetX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (SortedSetX<R>)MutableCollectionX.super.flatMapP(fn);
    }

    @Override
    default SortedSetX<T> prependS(Stream<? extends T> stream) {
        return (SortedSetX<T>)MutableCollectionX.super.prependS(stream);
    }

    @Override
    default SortedSetX<T> append(T... values) {
        return (SortedSetX<T>)MutableCollectionX.super.append(values);
    }

    @Override
    default SortedSetX<T> append(T value) {
        return (SortedSetX<T>)MutableCollectionX.super.append(value);
    }

    @Override
    default SortedSetX<T> prepend(T value) {
        return (SortedSetX<T>)MutableCollectionX.super.prepend(value);
    }

    @Override
    default SortedSetX<T> prepend(T... values) {
        return (SortedSetX<T>)MutableCollectionX.super.prepend(values);
    }

    @Override
    default SortedSetX<T> insertAt(int pos, T... values) {
        return (SortedSetX<T>)MutableCollectionX.super.insertAt(pos,values);
    }

    @Override
    default SortedSetX<T> deleteBetween(int start, int end) {
        return (SortedSetX<T>)MutableCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default SortedSetX<T> insertAtS(int pos, Stream<T> stream) {
        return (SortedSetX<T>)MutableCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default SortedSetX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (SortedSetX<T>)MutableCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> SortedSetX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (SortedSetX<T>)MutableCollectionX.super.recover(exceptionClass,fn);
    }
    @Override
    default SortedSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (SortedSetX<T>)MutableCollectionX.super.plusLoop(max,value);
    }

    @Override
    default SortedSetX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (SortedSetX<T>)MutableCollectionX.super.plusLoop(supplier);
    }

    /**
     * Narrow a covariant SortedSet
     * 
     * <pre>
     * {@code 
     * SortedSetX<? extends Fruit> set = SortedSetX.of(apple,bannana);
     * SortedSetX<Fruit> fruitSet = SortedSetX.narrowK(set);
     * }
     * </pre>
     * 
     * @param sortedSetX to narrowK generic type
     * @return SortedSetX with narrowed type
     */
    public static <T> SortedSetX<T> narrow(final SortedSetX<? extends T> setX) {
        return (SortedSetX<T>) setX;
    }

    static class Comparables {

        static <T, R extends ReactiveSeq<T> & Comparable<T>> R comparable(final Seq<T> seq) {
            return comparable(ReactiveSeq.fromStream(seq));
        }

        @SuppressWarnings("unchecked")

        static <T, R extends ReactiveSeq<T> & Comparable<T>> R comparable(final ReactiveSeq<T> seq) {
            final Method compareTo = Stream.of(Comparable.class.getMethods())
                                           .filter(m -> m.getName()
                                                         .equals("compareTo"))
                                           .findFirst()
                                           .get();

            return (R) Proxy.newProxyInstance(SortedSetX.class.getClassLoader(), new Class[] { ReactiveSeq.class, Comparable.class },
                                              (proxy, method, args) -> {
                                                  if (compareTo.equals(method))
                                                      return Objects.compare(System.identityHashCode(seq), System.identityHashCode(args[0]),
                                                                             Comparator.naturalOrder());
                                                  else
                                                      return method.invoke(seq, args);
                                              });

        }
    }

}
