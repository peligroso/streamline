/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;

import com.lmax.disruptor.util.MutableLong;
import com.lmax.disruptor.util.PaddedAtomicLong;

import java.util.concurrent.locks.LockSupport;

import static com.lmax.disruptor.util.Util.getMinimumSequence;

/**
 * Strategy to be used when there are multiple publisher threads claiming sequences.
 *
 * This strategy requires sufficient cores to allow multiple publishers to be concurrently claiming sequences and those
 * thread a contented relatively infrequently.
 */
public final class MultiThreadedLowContentionClaimStrategy
    implements ClaimStrategy
{
    private final int bufferSize;
    private final PaddedAtomicLong claimSequence = new PaddedAtomicLong(Sequencer.INITIAL_CURSOR_VALUE);

    private final ThreadLocal<MutableLong> minGatingSequenceThreadLocal = new ThreadLocal<MutableLong>()
    {
        @Override
        protected MutableLong initialValue()
        {
            return new MutableLong(Sequencer.INITIAL_CURSOR_VALUE);
        }
    };

    /**
     * Construct a new multi-threaded publisher {@link ClaimStrategy} for a given buffer size.
     *
     * @param bufferSize for the underlying data structure.
     */
    public MultiThreadedLowContentionClaimStrategy(final int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    @Override
    public int getBufferSize()
    {
        return bufferSize;
    }

    @Override
    public long getSequence()
    {
        return claimSequence.get();
    }

    @Override
    public boolean hasAvailableCapacity(final int availableCapacity, final Sequence[] dependentSequences)
    {
        final long wrapPoint = (claimSequence.get() + availableCapacity) - bufferSize;
        final MutableLong minGatingSequence = minGatingSequenceThreadLocal.get();
        if (wrapPoint > minGatingSequence.get())
        {
            long minSequence = getMinimumSequence(dependentSequences);
            minGatingSequence.set(minSequence);

            if (wrapPoint > minSequence)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public long incrementAndGet(final Sequence[] dependentSequences)
    {
        final MutableLong minGatingSequence = minGatingSequenceThreadLocal.get();
        waitForCapacity(dependentSequences, minGatingSequence);

        final long nextSequence = claimSequence.incrementAndGet();
        waitForFreeSlotAt(nextSequence, dependentSequences, minGatingSequence);

        return nextSequence;
    }

    @Override
    public long incrementAndGet(final int delta, final Sequence[] dependentSequences)
    {
        final long nextSequence = claimSequence.addAndGet(delta);
        waitForFreeSlotAt(nextSequence, dependentSequences, minGatingSequenceThreadLocal.get());

        return nextSequence;
    }

    @Override
    public void setSequence(final long sequence, final Sequence[] dependentSequences)
    {
        claimSequence.lazySet(sequence);
        waitForFreeSlotAt(sequence, dependentSequences, minGatingSequenceThreadLocal.get());
    }

    @Override
    public void serialisePublishing(final long sequence, final Sequence cursor, final int batchSize)
    {
        final long expectedSequence = sequence - batchSize;
        while (expectedSequence != cursor.get())
        {
            // busy spin
        }

        cursor.set(sequence);
    }

    private void waitForCapacity(final Sequence[] dependentSequences, final MutableLong minGatingSequence)
    {
        final long wrapPoint = (claimSequence.get() + 1L) - bufferSize;
        if (wrapPoint > minGatingSequence.get())
        {
            long minSequence;
            while (wrapPoint > (minSequence = getMinimumSequence(dependentSequences)))
            {
                LockSupport.parkNanos(1L);
            }

            minGatingSequence.set(minSequence);
        }
    }

    private void waitForFreeSlotAt(final long sequence, final Sequence[] dependentSequences, final MutableLong minGatingSequence)
    {
        final long wrapPoint = sequence - bufferSize;
        if (wrapPoint > minGatingSequence.get())
        {
            long minSequence;
            while (wrapPoint > (minSequence = getMinimumSequence(dependentSequences)))
            {
                LockSupport.parkNanos(1L);
            }

            minGatingSequence.set(minSequence);
        }
    }
}
