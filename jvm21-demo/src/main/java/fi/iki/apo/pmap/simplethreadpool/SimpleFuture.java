package fi.iki.apo.pmap.simplethreadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleFuture<V> implements Runnable {
    private volatile Object result = null;
    public final Callable<V> callable;

    final Lock lock = new ReentrantLock();
    final Condition finished = lock.newCondition();

    public SimpleFuture(Callable<V> callable) {
        this.callable = callable;
    }

    @Override
    public void run() {
        try {
            V v = callable.call();
            if (v == null) {
                result = new AltResult(true, false, null);
            }
            result = v;
            lock.lockInterruptibly();
            try {
                finished.signalAll();
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            new AltResult(false, true, e);
        } catch (Exception e) {
            new AltResult(false, false, e);
        }
    }

    public V get() throws InterruptedException, ExecutionException {
        while (result == null) {
            lock.lockInterruptibly();
            try {
                if (result == null) {
                    finished.await();
                }
            } finally {
                lock.unlock();
            }
            if (result instanceof AltResult altResult) {
                if (altResult.isNull) {
                    return null;
                } else {
                    throw new ExecutionException(altResult.throwable);
                }
            }
        }
        return (V) result;
    }

    record AltResult(boolean isNull, boolean interrupted, Throwable throwable) {
    }
}
