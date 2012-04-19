package org.juxtapose.streamline.util.subscriber;

public interface ISequencedDataSubscriber
{
	/**
	 * Requires that inSequencer.get() is called.
	 */
	public void dataUpdated( DataSequencer inSequencer );
}
