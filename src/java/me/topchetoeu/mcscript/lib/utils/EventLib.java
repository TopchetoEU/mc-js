package me.topchetoeu.mcscript.lib.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.topchetoeu.jscript.lib.PromiseLib;
import me.topchetoeu.jscript.runtime.Context;
import me.topchetoeu.jscript.runtime.EventLoop;
import me.topchetoeu.jscript.runtime.Extensions;
import me.topchetoeu.jscript.runtime.exceptions.EngineException;
import me.topchetoeu.jscript.runtime.values.ArrayValue;
import me.topchetoeu.jscript.runtime.values.FunctionValue;
import me.topchetoeu.jscript.runtime.values.NativeFunction;
import me.topchetoeu.jscript.runtime.values.Values;
import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import me.topchetoeu.mcscript.MessageQueue;

@WrapperName("Event")
public class EventLib {
    private HashMap<FunctionValue, Extensions> handles = new HashMap<>();
    private HashMap<FunctionValue, Extensions> onceHandles = new HashMap<>();
    private Thread thread;

    public void invoke(Object ...args) {
        List<Map.Entry<FunctionValue, Extensions>> arr;

        synchronized (handles) {
            synchronized (onceHandles) {
                arr = Stream.concat(handles.entrySet().stream(), onceHandles.entrySet().stream()).collect(Collectors.toList());
                onceHandles.clear();
            }
        }

        for (var handle : arr) {
            var env = handle.getValue();
            var func = handle.getKey();

            try {
                var awaitable = EventLoop.get(env).pushMsg(false, env, func, null, args);
                if (thread != null) MessageQueue.get(thread).await(awaitable);
            }
            catch (EngineException e) { Values.printError(e, "in event handler"); }
        }
    }

    public boolean invokeCancellable(Object ...args) {
        var cancelled = new boolean[1];
        var newArgs = new Object[args.length + 1];
        newArgs[0] = new NativeFunction("cancel", _args -> { cancelled[0] = true; return null; });
        System.arraycopy(args, 0, newArgs, 1, args.length);

        invoke(newArgs);

        return !cancelled[0];
    }

    @Expose
    public void __on(Arguments args) {
        var func = args.convert(0, FunctionValue.class);
        handles.put(func, Context.clean(args.ctx));
    }
    @Expose
    public void __once(Arguments args) {
        var func = args.convert(0, FunctionValue.class);
        onceHandles.put(func, Context.clean(args.ctx));
    }
    @Expose
    public PromiseLib __next(Arguments args) {
        var promise = new PromiseLib();
        onceHandles.put(new NativeFunction(_args -> {
            promise.fulfill(_args.ctx, new ArrayValue(_args.ctx, _args.args));
            return null;
        }), Context.clean(args.ctx));

        return promise;
    }

    public EventLib(Thread thread) {
        this.thread = thread;
    }
}
