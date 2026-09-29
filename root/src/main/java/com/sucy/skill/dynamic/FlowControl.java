package com.sucy.skill.dynamic;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Maintains the stop state for dynamic skill execution roots.
 *
 * <p>Dynamic mechanics can invoke another skill or emit a signal while the
 * current skill is running. A single static boolean would let a nested call
 * accidentally stop its parent, or let a parent stop flag leak into the next
 * event. This class uses a per-thread stack so every root invocation owns one
 * frame, and nested invocations can be entered and left in strict LIFO order.
 * The stack is removed when empty to avoid retaining thread-local state on
 * executor threads.</p>
 *
 * <p>Only the current frame is marked by {@link #stopSkill()}; callers are
 * responsible for calling {@link #enter()} and {@link #leave()} in a
 * {@code try/finally} pair.</p>
 */
public final class FlowControl {
    private static final ThreadLocal<Deque<Frame>> FRAMES = ThreadLocal.withInitial(ArrayDeque::new);
    /** Mutable stop marker belonging to exactly one dynamic execution root. */
    private static final class Frame { boolean stopped; }
    private FlowControl() { }

    /** Pushes a new independent execution frame for the current thread. */
    public static void enter() { FRAMES.get().push(new Frame()); }
    /**
     * Removes the current frame. Unbalanced calls are tolerated so cleanup code
     * cannot throw while handling a failed skill execution.
     */
    public static void leave() {
        Deque<Frame> frames = FRAMES.get();
        if (!frames.isEmpty()) frames.pop();
        if (frames.isEmpty()) FRAMES.remove();
    }
    /** Marks the current execution root as stopped, if one is active. */
    public static void stopSkill() {
        Deque<Frame> frames = FRAMES.get();
        if (!frames.isEmpty()) frames.peek().stopped = true;
    }
    /** @return whether the current execution root has been stopped */
    public static boolean isStopped() {
        Deque<Frame> frames = FRAMES.get();
        return !frames.isEmpty() && frames.peek().stopped;
    }
}
