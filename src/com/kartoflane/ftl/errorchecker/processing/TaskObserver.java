package com.kartoflane.ftl.errorchecker.processing;

/**
 * Appropriated from SMM's ModPatchObserver.
 */
public interface TaskObserver {

	/**
	 * Updates a progress bar.
	 *
	 * If either arg is -1, the bar will become indeterminate.
	 *
	 * @param value
	 *            the new value
	 * @param max
	 *            the new maximum
	 */
	public void taskProgress(final int value, final int max);

	/**
	 * Set the text indicating the current status of the task.
	 *
	 * @param message
	 *            a string, or null
	 */
	public void taskStatus(String message);

	/**
	 * Task has been completed.
	 *
	 * If anything went wrong, e may be non-null.
	 */
	public void taskFinished(boolean outcome, Exception e);

}
