package com.gig.meko.util;

import com.gig.meko.entity.Book;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.observables.ConnectableObservable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author spp
 */
public class BookPublisher {
    private final Flowable<Book> bookFlowable;
    private List<Book> books;

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public BookPublisher() {
        Observable<Book> bookObservable = Observable.create(emitter -> {

            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            // 以上一个任务开始的时间计时，2秒过去后，检测上一个任务是否执行完毕
            // 如果上一个任务执行完毕，则当前任务立即执行，
            // 如果上一个任务没有执行完毕，则需要等上一个任务执行完毕后立即执行
            executorService.scheduleAtFixedRate(newBooks(emitter), 0, 6, TimeUnit.SECONDS);

        });

        ConnectableObservable<Book> connectableObservable = bookObservable.share().publish();
        connectableObservable.connect();

        bookFlowable = connectableObservable.toFlowable(BackpressureStrategy.BUFFER);
    }

    private Runnable newBooks(ObservableEmitter<Book> emitter) {
        return () -> emitBooks(emitter);
    }

    private void emitBooks(ObservableEmitter<Book> emitter) {
        if (getBooks() == null) {
            return;
        }
        for (Book book : getBooks()) {
            try {
                // 发送一次数据
                emitter.onNext(book);
            } catch (RuntimeException rte) {
                rte.printStackTrace();
            }
        }
    }

    public Flowable<Book> getPublisher(Integer bookId) {
        return bookFlowable.filter(book -> book.getId() != null && book.getId().equals(bookId));
    }
}
