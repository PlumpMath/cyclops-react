package com.aol.cyclops2.types.anyM;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import cyclops.monads.AnyM;
import cyclops.async.Future;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import com.aol.cyclops2.types.foldable.CyclopsCollectable;

/**
 * Interface for manipulating monads nested inside monad transformers
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of the elements in the nested Monad
 */
public interface NestedCollectable<W extends WitnessType<W>,T> {
    public AnyM<W,? extends CyclopsCollectable<T>> nestedCollectables();

    
    /**
     * @return Convert toNested a List Transformer
     */
    default ListT<W,T> listT(Function<? super Iterable<T>,? extends IndexedSequenceX<T>> toList) {
        if (this instanceof ListT)
            return (ListT) this;
        final AnyM<W,? extends IndexedSequenceX<T>> anyM = nestedCollectables().map(s -> toList.apply(s));
        return ListT.of(anyM);
    }
    
    default <R> FutureT<W,R> futureT(Function<? super Iterable<T>,? extends R> fn,Executor exec) {
        final AnyM<W,Future<R>> anyM = nestedCollectables().map(s -> Future.ofSupplier(()->fn.apply(s),exec));
        return FutureT.of(anyM);
    }

    
   

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.reactiveStream.Collector)
     */
    default <R, A> AnyM<W,R> collect(final Collector<? super T, A, R> collector) {
        return nestedCollectables().map(s -> s.collect(collector));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.reactiveStream.Collector)
     */
    default <R, A, C extends Collection<R>> C collect(final Supplier<C> supplier, final Collector<? super T, A, R> collector) {
        return nestedCollectables().map(s -> s.collect(collector))
                                   .collect(Collectors.toCollection(supplier));
    }

    /**
     * Each nested value will be true if predicate matches all elements when Monad converted toNested a Stream
     * Nested analog toNested {@link ReactiveSeq#allMatch}
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate toNested check if all match
     */
    default AnyM<W,Boolean> allMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.allMatch(c));
    }

    /**
     * Each nested value will be True if a single element matches when Monad converted toNested a Stream
     * Nested analog toNested {@link ReactiveSeq#anyMatch}
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate toNested check if any match
     */
    default AnyM<W,Boolean> anyMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.anyMatch(c));
    }

    /**
     * Reduce each nested monad toNested a boolean value - true if the predicates match none of it's elements, otherwise false
     * 
     * @param c  Predicate toNested check if no match
     * @return Monad of booleans wrapped inside an AnyM
     */
    default AnyM<W,Boolean> noneMatch(final Predicate<? super T> c) {
        return nestedCollectables().map(s -> s.noneMatch(c));
    }

    /**
     * @return This monad transformer converted toNested nested Lists
     */
    default ListX<ListX<T>> toListOfLists() {
        return nestedCollectables().stream()
                                   .map(s -> s.collect(ListX.listXCollector()))
                                   .toListX();
    }

    /**
     * @return This monad transformer converted toNested nested Sets
     */
    default SetX<SetX<T>> toSetOfSets() {
        return nestedCollectables().stream()
                                   .map(s -> s.collect(SetX.setXCollector()))
                                   .toSetX();
    }

    /**
     * @return This monad transformer converted toNested a ListX nested in an AnyM
     */
    default AnyM<W,ListX<T>> toNestedListX() {
        return nestedCollectables().map(s -> s.collect(ListX.listXCollector()));
    }

    /**
     * @return This monad transformer converted toNested a SetX nested in an AnyM
     */
    default AnyM<W,SetX<T>> toNestedSetX() {
        return nestedCollectables().map(s -> s.collect(SetX.setXCollector()));
    }

}
