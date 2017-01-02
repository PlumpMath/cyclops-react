package cyclops.collections;

import com.aol.cyclops.data.collections.extensions.lazy.LazyQueueX;
import com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX;
import cyclops.Streams;
import cyclops.collections.immutable.PVectorX;
import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import com.aol.cyclops.types.OnEmptySwitch;
import com.aol.cyclops.types.To;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface QueueX<T> extends To<QueueX<T>>,Queue<T>, MutableCollectionX<T>, OnEmptySwitch<T, Queue<T>> {

    static <T> Collector<T, ?, Queue<T>> defaultCollector() {
        return Collectors.toCollection(() -> new LinkedList<>());
    }

    /**
    * Create a QueueX that contains the Integers between start and end
    * 
    * @param start
    *            Number of range to start from
    * @param end
    *            Number for range to end at
    * @return Range QueueX
    */
    public static QueueX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .toQueueX();
    }

    /**
     * Create a QueueX that contains the Longs between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range QueueX
     */
    public static QueueX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .toQueueX();
    }

    /**
     * Unfold a function into a QueueX
     * 
     * <pre>
     * {@code 
     *  QueueX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</code>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return QueueX generated by unfolder function
     */
    static <U, T> QueueX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .toQueueX();
    }

    /**
     * Generate a QueueX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate QueueX elements
     * @return QueueX generated from the provided Supplier
     */
    public static <T> QueueX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .toQueueX();
    }
    /**
     * Generate a QueueX from the provided value up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Value for QueueX elements
     * @return QueueX generated from the provided Supplier
     */
    public static <T> QueueX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit)
                          .toQueueX();
    }
    /**
     * Create a QueueX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return QueueX generated by iterative application
     */
    public static <T> QueueX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .toQueueX();

    }

    public static <T> QueueX<T> empty() {
        return fromIterable((Queue<T>) defaultCollector().supplier()
                                                         .get());
    }

    @SafeVarargs
    public static <T> QueueX<T> of(final T... values) {
        final Queue<T> res = (Queue<T>) defaultCollector().supplier()
                                                          .get();
        for (final T v : values)
            res.add(v);
        return fromIterable(res);
    }
    /**
     * 
     * Construct a QueueX from the provided Iterator
     * 
     * @param it Iterator to populate QueueX
     * @return Newly populated QueueX
     */
    public static <T> QueueX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }
    public static <T> QueueX<T> singleton(final T value) {
        return QueueX.<T> of(value);
    }

    /**
     * Construct a QueueX from an Publisher
     * 
     * @param publisher
     *            to construct QueueX from
     * @return QueueX
     */
    public static <T> QueueX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>) publisher)
                          .toQueueX();
    }

    public static <T> QueueX<T> fromIterable(final Iterable<T> it) {
        
        if (it instanceof QueueX)
            return (QueueX) it;
        if (it instanceof Deque)
            return new LazyQueueX<T>(
                                     (Queue) it, defaultCollector());
        return new LazyQueueX<T>(
                                 Streams.stream(it)
                                            .collect(defaultCollector()),
                                            defaultCollector());
    }

    public static <T> QueueX<T> fromIterable(final Collector<T, ?, Queue<T>> collector, final Iterable<T> it) {
        if (it instanceof QueueX)
            return ((QueueX) it).withCollector(collector);
        if (it instanceof Deque)
            return new LazyQueueX<T>(
                                     (Queue) it, collector);
        return new LazyQueueX<T>(
                                 Streams.stream(it)
                                            .collect(collector),
                                 collector);
    }

    QueueX<T> withCollector(Collector<T, ?, Queue<T>> collector);

    public <T> Collector<T, ?, Queue<T>> getCollector();

    @Override
    default QueueX<T> take(final long num) {

        return (QueueX<T>) MutableCollectionX.super.limit(num);
    }
    @Override
    default QueueX<T> drop(final long num) {

        return (QueueX<T>) MutableCollectionX.super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> QueueX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (QueueX)MutableCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> QueueX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (QueueX)MutableCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> QueueX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (QueueX)MutableCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> QueueX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (QueueX)MutableCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> QueueX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (QueueX)MutableCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> QueueX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (QueueX)MutableCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
     */
    @Override
    default QueueX<T> toQueueX() {
        return this;
    }
  
    /**
     * coflatMap pattern, can be used to perform lazy reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *     QueueX.of(1,2,3)
     *           .map(i->i*2)
     *           .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *      //QueueX[12]
     * }
     * </pre>
     * 
     * 
     * @param fn mapping function
     * @return Transformed Queue
     */
    default <R> QueueX<R> coflatMap(Function<? super QueueX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#from(java.util.Collection)
     */
    @Override
    default <T1> QueueX<T1> from(final Collection<T1> c) {
        return QueueX.<T1> fromIterable(getCollector(), c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#fromStream(java.util.stream.Stream)
     */
    @Override
    default <X> QueueX<X> fromStream(final Stream<X> stream) {
        return new LazyQueueX<>(
                                stream.collect(getCollector()), getCollector());
    }

    /**
     * Combine two adjacent elements in a QueueX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  QueueX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced QueueX
     */
    @Override
    default QueueX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (QueueX<T>) MutableCollectionX.super.combine(predicate, op);
    }

    @Override
    default <R> QueueX<R> unit(final Collection<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> QueueX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> QueueX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }
    @Override
    default QueueX<T> materialize() {
        return (QueueX<T>)MutableCollectionX.super.materialize();
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
     */
    @Override
    default QueueX<T> reverse() {

        return (QueueX<T>) MutableCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> filter(final Predicate<? super T> pred) {

        return (QueueX<T>) MutableCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> QueueX<R> map(final Function<? super T, ? extends R> mapper) {

        return (QueueX<R>) MutableCollectionX.super.<R> map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> QueueX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (QueueX<R>) MutableCollectionX.super.<R> flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
     */
    @Override
    default QueueX<T> limit(final long num) {

        return (QueueX<T>) MutableCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
     */
    @Override
    default QueueX<T> skip(final long num) {

        return (QueueX<T>) MutableCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> takeWhile(final Predicate<? super T> p) {

        return (QueueX<T>) MutableCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> dropWhile(final Predicate<? super T> p) {

        return (QueueX<T>) MutableCollectionX.super.dropWhile(p);
    }

    @Override
    default QueueX<T> takeRight(final int num) {
        return (QueueX<T>) MutableCollectionX.super.takeRight(num);
    }

    @Override
    default QueueX<T> dropRight(final int num) {
        return (QueueX<T>) MutableCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> takeUntil(final Predicate<? super T> p) {

        return (QueueX<T>) MutableCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> dropUntil(final Predicate<? super T> p) {

        return (QueueX<T>) MutableCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> QueueX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (QueueX<R>) MutableCollectionX.super.<R> trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
     */
    @Override
    default QueueX<T> slice(final long from, final long to) {

        return (QueueX<T>) MutableCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> QueueX<T> sorted(final Function<? super T, ? extends U> function) {

        return (QueueX<T>) MutableCollectionX.super.sorted(function);
    }

    @Override
    default QueueX<ListX<T>> grouped(final int groupSize) {
        return (QueueX<ListX<T>>) MutableCollectionX.super.grouped(groupSize);
    }

    @Override
    default <K, A, D> QueueX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {
        return (QueueX) MutableCollectionX.super.grouped(classifier, downstream);
    }

    @Override
    default <K> QueueX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return (QueueX) MutableCollectionX.super.grouped(classifier);
    }

    @Override
    default <U> QueueX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (QueueX) MutableCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> QueueX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (QueueX<R>) MutableCollectionX.super.zip(other, zipper);
    }



    @Override
    default <U, R> QueueX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (QueueX<R>) MutableCollectionX.super.zipS(other, zipper);
    }

    @Override
    default QueueX<PVectorX<T>> sliding(final int windowSize) {
        return (QueueX<PVectorX<T>>) MutableCollectionX.super.sliding(windowSize);
    }

    @Override
    default QueueX<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return (QueueX<PVectorX<T>>) MutableCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default QueueX<T> scanLeft(final Monoid<T> monoid) {
        return (QueueX<T>) MutableCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> QueueX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (QueueX<U>) MutableCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default QueueX<T> scanRight(final Monoid<T> monoid) {
        return (QueueX<T>) MutableCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> QueueX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (QueueX<U>) MutableCollectionX.super.scanRight(identity, combiner);
    }

    @Override
    default QueueX<T> plus(final T e) {
        add(e);
        return this;
    }

    @Override
    default QueueX<T> plusAll(final Collection<? extends T> list) {
        addAll(list);
        return this;
    }

    @Override
    default QueueX<T> minus(final Object e) {
        remove(e);
        return this;
    }

    @Override
    default QueueX<T> minusAll(final Collection<?> list) {
        removeAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default QueueX<T> peek(final Consumer<? super T> c) {

        return (QueueX<T>) MutableCollectionX.super.peek(c);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> QueueX<U> cast(final Class<? extends U> type) {

        return (QueueX<U>) MutableCollectionX.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(int)
     */
    @Override
    default QueueX<T> cycle(final long times) {

        return (QueueX<T>) MutableCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    default QueueX<T> cycle(final Monoid<T> m, final long times) {

        return (QueueX<T>) MutableCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (QueueX<T>) MutableCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (QueueX<T>) MutableCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.util.stream.Stream)
     */
    @Override
    default <U> QueueX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (QueueX) MutableCollectionX.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> QueueX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (QueueX) MutableCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> QueueX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (QueueX) MutableCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zipWithIndex()
     */
    @Override
    default QueueX<Tuple2<T, Long>> zipWithIndex() {

        return (QueueX<Tuple2<T, Long>>) MutableCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#distinct()
     */
    @Override
    default QueueX<T> distinct() {

        return (QueueX<T>) MutableCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted()
     */
    @Override
    default QueueX<T> sorted() {

        return (QueueX<T>) MutableCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default QueueX<T> sorted(final Comparator<? super T> c) {

        return (QueueX<T>) MutableCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> skipWhile(final Predicate<? super T> p) {

        return (QueueX<T>) MutableCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> skipUntil(final Predicate<? super T> p) {

        return (QueueX<T>) MutableCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> limitWhile(final Predicate<? super T> p) {

        return (QueueX<T>) MutableCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> limitUntil(final Predicate<? super T> p) {

        return (QueueX<T>) MutableCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default QueueX<T> intersperse(final T value) {

        return (QueueX<T>) MutableCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#shuffle()
     */
    @Override
    default QueueX<T> shuffle() {

        return (QueueX<T>) MutableCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipLast(int)
     */
    @Override
    default QueueX<T> skipLast(final int num) {

        return (QueueX<T>) MutableCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
     */
    @Override
    default QueueX<T> limitLast(final int num) {

        return (QueueX<T>) MutableCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default QueueX<T> onEmptySwitch(final Supplier<? extends Queue<T>> supplier) {
        if (isEmpty())
            return QueueX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default QueueX<T> onEmpty(final T value) {

        return (QueueX<T>) MutableCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default QueueX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (QueueX<T>) MutableCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> QueueX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (QueueX<T>) MutableCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
     */
    @Override
    default QueueX<T> shuffle(final Random random) {

        return (QueueX<T>) MutableCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> QueueX<U> ofType(final Class<? extends U> type) {

        return (QueueX<U>) MutableCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default QueueX<T> filterNot(final Predicate<? super T> fn) {

        return (QueueX<T>) MutableCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
     */
    @Override
    default QueueX<T> notNull() {

        return (QueueX<T>) MutableCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAllS(java.util.stream.Stream)
     */
    @Override
    default QueueX<T> removeAllS(final Stream<? extends T> stream) {

        return (QueueX<T>) MutableCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAllS(java.lang.Iterable)
     */
    @Override
    default QueueX<T> removeAllS(final Iterable<? extends T> it) {

        return (QueueX<T>) MutableCollectionX.super.removeAllS(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAllS(java.lang.Object[])
     */
    @Override
    default QueueX<T> removeAllS(final T... values) {

        return (QueueX<T>) MutableCollectionX.super.removeAllS(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAllS(java.lang.Iterable)
     */
    @Override
    default QueueX<T> retainAllS(final Iterable<? extends T> it) {

        return (QueueX<T>) MutableCollectionX.super.retainAllS(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAllS(java.util.stream.Stream)
     */
    @Override
    default QueueX<T> retainAllS(final Stream<? extends T> seq) {

        return (QueueX<T>) MutableCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAllS(java.lang.Object[])
     */
    @Override
    default QueueX<T> retainAllS(final T... values) {

        return (QueueX<T>) MutableCollectionX.super.retainAllS(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> QueueX<C> grouped(final int size, final Supplier<C> supplier) {

        return (QueueX<C>) MutableCollectionX.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default QueueX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (QueueX<ListX<T>>) MutableCollectionX.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default QueueX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (QueueX<ListX<T>>) MutableCollectionX.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> QueueX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (QueueX<C>) MutableCollectionX.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> QueueX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (QueueX<C>) MutableCollectionX.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default QueueX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (QueueX<ListX<T>>) MutableCollectionX.super.groupedStatefullyUntil(predicate);
    }



    @Override
    default <R> QueueX<R> retry(final Function<? super T, ? extends R> fn) {
        return (QueueX<R>)MutableCollectionX.super.retry(fn);
    }

    @Override
    default <R> QueueX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (QueueX<R>)MutableCollectionX.super.retry(fn);
    }

    @Override
    default <R> QueueX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (QueueX<R>)MutableCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> QueueX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (QueueX<R>)MutableCollectionX.super.flatMapP(fn);
    }

    @Override
    default QueueX<T> prependS(Stream<? extends T> stream) {
        return (QueueX<T>)MutableCollectionX.super.prependS(stream);
    }

    @Override
    default QueueX<T> append(T... values) {
        return (QueueX<T>)MutableCollectionX.super.append(values);
    }

    @Override
    default QueueX<T> append(T value) {
        return (QueueX<T>)MutableCollectionX.super.append(value);
    }

    @Override
    default QueueX<T> prepend(T value) {
        return (QueueX<T>)MutableCollectionX.super.prepend(value);
    }

    @Override
    default QueueX<T> prepend(T... values) {
        return (QueueX<T>)MutableCollectionX.super.prepend(values);
    }

    @Override
    default QueueX<T> insertAt(int pos, T... values) {
        return (QueueX<T>)MutableCollectionX.super.insertAt(pos,values);
    }

    @Override
    default QueueX<T> deleteBetween(int start, int end) {
        return (QueueX<T>)MutableCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default QueueX<T> insertAtS(int pos, Stream<T> stream) {
        return (QueueX<T>)MutableCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default QueueX<T> recover(final Function<Throwable, ? extends T> fn) {
        return (QueueX<T>)MutableCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> QueueX<T> recover(Class<EX> exceptionClass, final Function<EX, ? extends T> fn) {
        return (QueueX<T>)MutableCollectionX.super.recover(exceptionClass,fn);
    }
    /**
     * Narrow a covariant Queue
     * 
     * <pre>
     * {@code 
     * QueueX<? extends Fruit> set = QueueX.of(apple,bannana);
     * QueueX<Fruit> fruitSet = QueueX.narrow(queue);
     * }
     * </pre>
     * 
     * @param queueX to narrow generic type
     * @return QueueX with narrowed type
     */
    public static <T> QueueX<T> narrow(final QueueX<? extends T> queueX) {
        return (QueueX<T>) queueX;
    }
}
