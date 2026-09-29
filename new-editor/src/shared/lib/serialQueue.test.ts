import { describe, expect, it } from 'vitest';
import { enqueueSerial } from './serialQueue';

describe('enqueueSerial', () => {
  it('runs queued operations in order', async () => {
    const queue = { current: Promise.resolve() };
    const order: string[] = [];
    let release!: () => void;
    const first = enqueueSerial(queue, async () => {
      order.push('first-start');
      await new Promise<void>((resolve) => {
        release = resolve;
      });
      order.push('first-end');
      return 1;
    });
    const second = enqueueSerial(queue, async () => {
      order.push('second');
      return 2;
    });

    // The queue adds a recovery handler and then schedules the operation, so
    // allow both microtasks to run before checking the first write boundary.
    await Promise.resolve();
    await Promise.resolve();
    expect(order).toEqual(['first-start']);
    release();
    await expect(first).resolves.toBe(1);
    await expect(second).resolves.toBe(2);
    expect(order).toEqual(['first-start', 'first-end', 'second']);
  });

  it('continues with later operations after a failure', async () => {
    const queue = { current: Promise.resolve() };
    const failed = enqueueSerial(queue, async () => {
      throw new Error('write failed');
    });
    const recovered = enqueueSerial(queue, async () => 'retry');

    await expect(failed).rejects.toThrow('write failed');
    await expect(recovered).resolves.toBe('retry');
  });
});
