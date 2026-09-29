/**
 * Appends an async operation to a shared queue and keeps the queue usable after
 * a rejection. Save callers must wait for the returned promise to observe their
 * own failure, while later saves still get a chance to write the latest state.
 */
export function enqueueSerial<T>(
  queue: { current: Promise<unknown> },
  operation: () => Promise<T>,
): Promise<T> {
  const next = queue.current.catch(() => undefined).then(operation);
  queue.current = next.catch(() => undefined);
  return next;
}
