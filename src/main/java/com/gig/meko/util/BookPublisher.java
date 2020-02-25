package com.gig.meko.util;

import com.gig.meko.entity.Book;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.observables.ConnectableObservable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author spp
 */
public class BookPublisher {
    private final Flowable<Book> publisher;

    public BookPublisher() {
        Observable<Book> bookObservable = Observable.create(emitter -> {

            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            // 以上一个任务开始的时间计时，2秒过去后，检测上一个任务是否执行完毕
            // 如果上一个任务执行完毕，则当前任务立即执行，
            // 如果上一个任务没有执行完毕，则需要等上一个任务执行完毕后立即执行
            executorService.scheduleAtFixedRate(updateBook(emitter), 0, 2, TimeUnit.SECONDS);

        });

        ConnectableObservable<Book> connectableObservable = bookObservable.share().publish();
        connectableObservable.connect();

        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER);
    }

    private Runnable updateBook(ObservableEmitter<Book> emitter) {
        return () -> {
            // 随机添加随机个随机生成的stock
            List<Book> books = getUpdates(rollDice(0, 5));
            if (books != null) {
                // 推送新消息
                emitBooks(emitter, books);
            }
        };
    }

    private void emitBooks(ObservableEmitter<Book> emitter, List<Book> books) {
        for (Book book : books) {
            try {
                // 发送一次数据
                emitter.onNext(book);
            } catch (RuntimeException rte) {
                rte.printStackTrace();
            }
        }
    }

    public Flowable<Book> getPublisher(Integer bookId) {
        return publisher.filter(book -> bookId.equals(book.getId()));
    }

    private List<Book> getUpdates(int number) {
        List<Book> updates = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            updates.add(rollUpdate());
        }
        return updates;
    }


    private final static Map<String, BigDecimal> CURRENT_STOCK_PRICES = new ConcurrentHashMap<>();

    static {
        CURRENT_STOCK_PRICES.put("TEAM", dollars(39, 64));
        CURRENT_STOCK_PRICES.put("IBM", dollars(147, 10));
        CURRENT_STOCK_PRICES.put("AMZN", dollars(1002, 94));
        CURRENT_STOCK_PRICES.put("MSFT", dollars(77, 49));
        CURRENT_STOCK_PRICES.put("GOOGL", dollars(1007, 87));
    }

    private Book rollUpdate() {
        ArrayList<String> STOCK_CODES = new ArrayList<>(CURRENT_STOCK_PRICES.keySet());
        String stockCode = STOCK_CODES.get(rollDice(0, STOCK_CODES.size() - 1));
        BigDecimal currentPrice = CURRENT_STOCK_PRICES.get(stockCode);
        BigDecimal incrementDollars = dollars(rollDice(0, 1), rollDice(0, 99));
        if (rollDice(0, 10) > 7) {
            // 0.3 of the time go down
            incrementDollars = incrementDollars.negate();
        }
        BigDecimal newPrice = currentPrice.add(incrementDollars);
        // 随机选择一个stock 随机更新价格
        CURRENT_STOCK_PRICES.put(stockCode, newPrice);
        // 将新增的stock 返回
        return new Book("book-" + incrementDollars.toPlainString(), 10, null);
    }

    private static BigDecimal dollars(int dollars, int cents) {
        return truncate("" + dollars + "." + cents);
    }

    private static BigDecimal truncate(final String text) {
        BigDecimal bigDecimal = new BigDecimal(text);
        if (bigDecimal.scale() > 2)
            bigDecimal = new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
        return bigDecimal.stripTrailingZeros();
    }

    private final static Random rand = new Random();

    private static int rollDice(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

}
