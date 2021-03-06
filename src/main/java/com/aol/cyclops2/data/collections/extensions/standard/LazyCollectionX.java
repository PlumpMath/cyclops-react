package com.aol.cyclops2.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import cyclops.collections.immutable.VectorX;
import cyclops.companion.Streams;
import cyclops.collections.mutable.ListX;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import org.reactivestreams.Publisher;

public interface LazyCollectionX<T> extends FluentCollectionX<T> {
    
    
   

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Folds#reduce(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        return stream().reduce(identity, accumulator);
    }

    /**
     * @param stream Create a MultableCollectionX from a Stream
     * @return LazyCollectionX
     */
    <X> LazyCollectionX<X> fromStream(ReactiveSeq<X> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default LazyCollectionX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return fromStream(stream().combine(predicate, op));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#reverse()
     */
    @Override
    default LazyCollectionX<T> reverse() {
        return fromStream(stream().reverse());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> filter(final Predicate<? super T> pred) {
        return fromStream(stream().filter(pred));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> CollectionX<R> map(final Function<? super T, ? extends R> mapper) {
        return fromStream(stream().map(mapper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> CollectionX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return fromStream(stream().flatMap(mapper.andThen(Streams::stream)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#limit(long)
     */
    @Override
    default LazyCollectionX<T> limit(final long num) {
        return fromStream(stream().limit(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#skip(long)
     */
    @Override
    default LazyCollectionX<T> skip(final long num) {
        return fromStream(stream().skip(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#takeRight(int)
     */
    @Override
    default LazyCollectionX<T> takeRight(final int num) {
        return fromStream(stream().limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#dropRight(int)
     */
    @Override
    default LazyCollectionX<T> dropRight(final int num) {
        return fromStream(stream().skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> takeWhile(final Predicate<? super T> p) {
        return fromStream(stream().limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> dropWhile(final Predicate<? super T> p) {
        return fromStream(stream().skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> takeUntil(final Predicate<? super T> p) {
        return fromStream(stream().limitUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> dropUntil(final Predicate<? super T> p) {
        return fromStream(stream().skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> LazyCollectionX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return fromStream(stream().trampoline(mapper));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#slice(long, long)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#slice(long, long)
     */
    @Override
    default LazyCollectionX<T> slice(final long from, final long to) {
        return fromStream(stream().slice(from, to));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#grouped(int)
     */
    @Override
    default LazyCollectionX<ListX<T>> grouped(final int groupSize) {
        return fromStream(stream().grouped(groupSize)
                                  .map(ListX::fromIterable));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#grouped(java.util.function.Function, java.util.reactiveStream.Collector)
     */
    @Override
    default <K, A, D> LazyCollectionX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
                                                            final Collector<? super T, A, D> downstream) {
        return fromStream(stream().grouped(classifier, downstream));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#grouped(java.util.function.Function)
     */
    @Override
    default <K> LazyCollectionX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return fromStream(stream().grouped(classifier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#zip(java.lang.Iterable)
     */
    @Override
    default <U> LazyCollectionX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return fromStream(stream().zip(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> LazyCollectionX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return fromStream(stream().zip(other, zipper));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#zip(java.util.reactiveStream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> LazyCollectionX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return fromStream(stream().zipS(other, zipper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#sliding(int)
     */
    @Override
    default LazyCollectionX<VectorX<T>> sliding(final int windowSize) {
        return fromStream(stream().sliding(windowSize));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#sliding(int, int)
     */
    @Override
    default LazyCollectionX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return fromStream(stream().sliding(windowSize, increment));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    default LazyCollectionX<T> scanLeft(final Monoid<T> monoid) {
        return fromStream(stream().scanLeft(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> LazyCollectionX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return fromStream(stream().scanLeft(seed, function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#scanRight(cyclops2.function.Monoid)
     */
    @Override
    default LazyCollectionX<T> scanRight(final Monoid<T> monoid) {
        return fromStream(stream().scanRight(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> LazyCollectionX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return fromStream(stream().scanRight(identity, combiner));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> LazyCollectionX<T> sorted(final Function<? super T, ? extends U> function) {
        return fromStream(stream().sorted(function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentCollectionX#plus(java.lang.Object)
     */
    @Override
    default LazyCollectionX<T> plus(final T e) {
        add(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentCollectionX#plusAll(java.util.Collection)
     */
    @Override
    default LazyCollectionX<T> plusAll(final Collection<? extends T> list) {
        addAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentCollectionX#minus(java.lang.Object)
     */
    @Override
    default LazyCollectionX<T> minus(final Object e) {
        remove(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentCollectionX#minusAll(java.util.Collection)
     */
    @Override
    default LazyCollectionX<T> minusAll(final Collection<?> list) {
        removeAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#cycle(int)
     */
    @Override
    default LazyCollectionX<T> cycle(final long times) {

        return fromStream(stream().cycle(times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#cycle(cyclops2.function.Monoid, int)
     */
    @Override
    default LazyCollectionX<T> cycle(final Monoid<T> m, final long times) {

        return fromStream(stream().cycle(m, times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> cycleWhile(final Predicate<? super T> predicate) {

        return fromStream(stream().cycleWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> cycleUntil(final Predicate<? super T> predicate) {

        return fromStream(stream().cycleUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> LazyCollectionX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return fromStream(stream().zipS(other));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, U> LazyCollectionX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return fromStream(stream().zip3(second, third));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <T2, T3, T4> LazyCollectionX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                     final Iterable<? extends T4> fourth) {

        return fromStream(stream().zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#zipWithIndex()
     */
    @Override
    default LazyCollectionX<Tuple2<T, Long>> zipWithIndex() {

        return fromStream(stream().zipWithIndex());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#distinct()
     */
    @Override
    default LazyCollectionX<T> distinct() {

        return fromStream(stream().distinct());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#sorted()
     */
    @Override
    default LazyCollectionX<T> sorted() {

        return fromStream(stream().sorted());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#sorted(java.util.Comparator)
     */
    @Override
    default LazyCollectionX<T> sorted(final Comparator<? super T> c) {

        return fromStream(stream().sorted(c));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> skipWhile(final Predicate<? super T> p) {

        return fromStream(stream().skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> skipUntil(final Predicate<? super T> p) {

        return fromStream(stream().skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> limitWhile(final Predicate<? super T> p) {

        return fromStream(stream().limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> limitUntil(final Predicate<? super T> p) {

        return fromStream(stream().limitUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#intersperse(java.lang.Object)
     */
    @Override
    default LazyCollectionX<T> intersperse(final T value) {

        return fromStream(stream().intersperse(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#shuffle()
     */
    @Override
    default LazyCollectionX<T> shuffle() {

        return fromStream(stream().shuffle());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#skipLast(int)
     */
    @Override
    default LazyCollectionX<T> skipLast(final int num) {

        return fromStream(stream().skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#limitLast(int)
     */
    @Override
    default LazyCollectionX<T> limitLast(final int num) {

        return fromStream(stream().limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default LazyCollectionX<T> onEmpty(final T value) {
        return fromStream(stream().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default LazyCollectionX<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return fromStream(stream().onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> LazyCollectionX<T> onEmptyThrow(final Supplier<? extends X> supplier) {
        return fromStream(stream().onEmptyThrow(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#shuffle(java.util.Random)
     */
    @Override
    default LazyCollectionX<T> shuffle(final Random random) {
        return fromStream(stream().shuffle(random));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> LazyCollectionX<U> ofType(final Class<? extends U> type) {

        return (LazyCollectionX) FluentCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> filterNot(final Predicate<? super T> fn) {
        return fromStream(stream().filterNot(fn));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#notNull()
     */
    @Override
    default LazyCollectionX<T> notNull() {
        return fromStream(stream().notNull());

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAll(java.util.reactiveStream.Stream)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#removeAll(java.util.reactiveStream.Stream)
     */
    @Override
    default LazyCollectionX<T> removeAllS(final Stream<? extends T> stream) {

        return fromStream(stream().removeAllS(stream));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default LazyCollectionX<T> removeAllI(final Iterable<? extends T> it) {
        return fromStream(stream().removeAllI(it));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default LazyCollectionX<T> removeAll(final T... values) {
        return fromStream(stream().removeAll(values));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default LazyCollectionX<T> retainAllI(final Iterable<? extends T> it) {
        return fromStream(stream().retainAllI(it));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#retainAllI(java.util.reactiveStream.Stream)
     */
    @Override
    default LazyCollectionX<T> retainAllS(final Stream<? extends T> stream) {
        return fromStream(stream().retainAllS(stream));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default LazyCollectionX<T> retainAll(final T... values) {
        return fromStream(stream().retainAll(values));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#cast(java.lang.Class)
     */
    @Override
    default <U> LazyCollectionX<U> cast(final Class<? extends U> type) {
        return fromStream(stream().cast(type));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#permutations()
     */
    @Override
    default LazyCollectionX<ReactiveSeq<T>> permutations() {
        return fromStream(stream().permutations());

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.ExtendedTraversable#combinations(int)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#combinations(int)
     */
    @Override
    default LazyCollectionX<ReactiveSeq<T>> combinations(final int size) {
        return fromStream(stream().combinations(size));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.ExtendedTraversable#combinations()
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#combinations()
     */
    @Override
    default LazyCollectionX<ReactiveSeq<T>> combinations() {
        return fromStream(stream().combinations());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> LazyCollectionX<C> grouped(final int size, final Supplier<C> supplier) {

        return fromStream(stream().grouped(size, supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return fromStream(stream().groupedUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return fromStream(stream().groupedWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> LazyCollectionX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromStream(stream().groupedWhile(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> LazyCollectionX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromStream(stream().groupedUntil(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default LazyCollectionX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return fromStream(stream().groupedStatefullyUntil(predicate));
    }

    @Override
    default <R> LazyCollectionX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return fromStream(stream().flatMap(fn));
    }

    @Override
    default <R> LazyCollectionX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return fromStream(stream().flatMapP(fn));
    }

    @Override
    default LazyCollectionX<T> prependS(Stream<? extends T> stream){
        return fromStream(stream().prependS(stream));
    }

    @Override
    default LazyCollectionX<T> append(T... values){
        return fromStream(stream().append(values));
    }

    @Override
    default LazyCollectionX<T> append(T value){
        return fromStream(stream().append(value));
    }

    @Override
    default LazyCollectionX<T> prepend(T value){
        return fromStream(stream().prepend(value));
    }

    @Override
    default LazyCollectionX<T> prepend(T... values){
        return fromStream(stream().prepend(values));
    }

    @Override
    default LazyCollectionX<T> insertAt(int pos, T... values){
        return fromStream(stream().insertAt(pos,values));
    }

    @Override
    default LazyCollectionX<T> deleteBetween(int start, int end){
        return fromStream(stream().deleteBetween(start,end));
    }

    @Override
    default LazyCollectionX<T> insertAtS(int pos, Stream<T> stream){
        return fromStream(stream().insertAtS(pos,stream));

    }
}
