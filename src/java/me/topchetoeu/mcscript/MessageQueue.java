package me.topchetoeu.mcscript;

import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import me.topchetoeu.jscript.common.ResultRunnable;
import me.topchetoeu.jscript.common.events.DataNotifier;
import me.topchetoeu.jscript.lib.PromiseLib;
import me.topchetoeu.jscript.runtime.exceptions.EngineException;
import me.topchetoeu.jscript.runtime.exceptions.InterruptException;

public class MessageQueue {
    private static final WeakHashMap<Thread, MessageQueue> queues = new WeakHashMap<>();
    private Queue<Runnable> tasks = new ConcurrentLinkedDeque<>();

    private final Thread thread;
    private boolean awaiting = false;
    private boolean interrupted = false;

    public void runQueue() {
        synchronized (tasks) {
            while (!tasks.isEmpty()) {
                var task = tasks.poll();
                task.run();
            }
        }
    }

    public PromiseLib enqueuePromise(Runnable runnable) {
        var res = new PromiseLib();

        if (Thread.currentThread() == thread) {
            runnable.run();
            res.fulfill(null, null);
            return res;
        }

        synchronized (tasks) {
            tasks.add(() -> {
                try {
                    runnable.run();
                    res.fulfill(null, null);
                }
                catch (EngineException e) {
                    res.reject(null, e);
                }
                catch (Exception e) {
                    res.reject(null, new EngineException(e));
                }
            });
        }
        return res;
    }
    public <T> T enqueueSync(ResultRunnable<T> runnable) {
        if (Thread.currentThread() == thread) {
            return runnable.run();
        }

        var notif = new DataNotifier<T>();

        synchronized (tasks) {
            tasks.add(() -> {
                try {
                    notif.next(runnable.run());
                }
                catch (RuntimeException e){
                    notif.error(e);
                }
                notif.next(null);
            });
        }

        synchronized (this) {
            if (awaiting) {
                interrupted = true;
                thread.interrupt();
            }
        }

        return notif.await();
    }
    public void enqueueSync(Runnable runnable) {
        enqueueSync(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> T await(DataNotifier<T> notif) {
        if (thread != Thread.currentThread()) {
            runQueue();
            System.out.println("Tried to await outside the queue's thread, ignoring...");
            return null;
        }
        if (awaiting) {
            runQueue();
            System.out.println("Tried to double-await, ignoring...");
            return null;
        }

        synchronized (Thread.currentThread()) {
            while (true) {
                runQueue();
                try {
                    awaiting = true;
                    return (T)notif.await();
                }
                catch (InterruptException e) {
                    if (!interrupted) throw new InterruptException();

                    Thread.interrupted();
                    synchronized (this) { interrupted = false; }
                    runQueue();
                }
                finally {
                    awaiting = interrupted = false;
                    Thread.interrupted();
                    runQueue();
                }
            }
        }
    }

    private MessageQueue(Thread thread) {
        this.thread = thread;
    }

    public static MessageQueue get() {
        return get(Thread.currentThread());
    }
    public static MessageQueue get(Thread thread) {
        queues.putIfAbsent(thread, new MessageQueue(thread));
        return queues.get(thread);
    }
}
