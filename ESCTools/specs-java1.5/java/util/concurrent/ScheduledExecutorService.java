package java.util.concurrent;

import java.util.concurrent.atomic.*;
import java.util.*;

public interface ScheduledExecutorService extends ExecutorService {
    
    public ScheduledFuture schedule(Runnable command, long delay, TimeUnit unit);
    
    public ScheduledFuture schedule(Callable callable, long delay, TimeUnit unit);
    
    public ScheduledFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);
    
    public ScheduledFuture scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
