package uk.ac.rhul.cs.cl1.ui;

import java.io.PrintWriter;
import java.util.Arrays;

import uk.ac.rhul.cs.cl1.TaskMonitor;
import uk.ac.rhul.cs.utils.StringUtils;

/**
 * A task monitor that shows the progress of a task on the console
 * @author tamas
 */
public class ConsoleTaskMonitor implements TaskMonitor {
	/** The progress of the task in percentages, or -1 if the task will be running indeterminately */
	protected int percent = 0;

	/** State of the spinner when the task is running indeterminately */
	protected int spinnerState = 0;
	
	/** The time of the last update of the progress bar.
	 * 
	 * When in indeterminate state, this timestamp ensures that the progress bar is not updated
	 * too frequently.
	 */
	protected long lastUpdatedAt = -1;
	
	/** Width of the progress bar in characters */
	protected int progressBarWidth = 20;

	/** The message shown on the console */
	protected String message = "";
	
	/** Whether the display is "dirty" (i.e., something has changed and it needs to be repainted */
	protected boolean dirty = false;

	/** Whether the display is drawn in the current line of the console */
	protected boolean progressBarDrawn = false;

	/** The width of the console as determined by {@link #getConsoleWidth()}, cached locally */
	private Integer consoleWidth = null;
	
	/** PrintWriter object that will be used to write to the standard error stream */
	protected PrintWriter writer = new PrintWriter(System.err);
	
	/**
	 * Returns the width of the progress bar shown on the screen
	 * @return the width in characters
	 */
	public int getProgressBarWidth() {
		return progressBarWidth;
	}

	public void setEstimatedTimeRemaining(long time) {
		// TODO
		dirty = true;
	}

	public void setException(Throwable t, String userErrorMessage) {
		this.setException(t, userErrorMessage, null);
	}

	public void setException(Throwable t, String userErrorMessage,
			String recoveryTip) {
		if (userErrorMessage != null && !userErrorMessage.isEmpty()) {
			System.err.println("An unexpected error happened:");
			System.err.println(userErrorMessage);
			System.err.println();
			System.err.println("The corresponding stack trace is:");
		} else {
			System.err.println("An unexpected exception happened:");
		}
		t.printStackTrace();
		if (recoveryTip != null && !recoveryTip.isEmpty()) {
			System.err.println();
			System.err.println(recoveryTip);
		}
	}

	public void setPercentCompleted(int percent)
			throws IllegalArgumentException {
		if (percent < -1 || percent > 100)
			throw new IllegalArgumentException("percentage must be between -1 and 100");
		if (percent == -1) {
			this.percent = -1;
			if (System.currentTimeMillis() - lastUpdatedAt > 100) {
				this.spinnerState++;
				this.dirty = true;
				updateDisplay();
			}
		} else if (this.percent != percent) {
			this.percent = percent;
			this.dirty = true;
			updateDisplay();
		}
	}

	/**
	 * Sets the width of the progress bar shown on the screen
	 * @param width the new width
	 */
	public void setProgressBarWidth(int width) {
		this.progressBarWidth = Math.max(1, progressBarWidth);
		this.dirty = true;
	}

	public void setStatus(String message) {
		if (!message.isEmpty() && !message.equals(this.message)) {
			this.message = message;
			this.dirty = true;

			// Don't draw the progress bar if it is not drawn already in the current line;
			// wait for the first setPercentCompleted() call instead
			if (progressBarDrawn) {
				updateDisplay();
			}
		}
	}

	/**
	 * Updates the progress display
	 */
	protected void updateDisplay() {
		if (!this.dirty)
			return;
		
		char[] progress = new char[progressBarWidth];
		Arrays.fill(progress, ' ');
		
		writer.append('[');
		if (percent >= 0) {
			int numChars = (int)Math.round(progressBarWidth * percent / 100.0);
			for (int i = 0; i < numChars; i++)
				progress[i] = '=';
			if (numChars > 0 && percent < 100)
					progress[numChars-1] = '>';
		} else {
			spinnerState = spinnerState % progressBarWidth;
			progress[spinnerState] = '|';
		}
		writer.write(progress);
		writer.append(']');
		
		if (percent >= 0)
			writer.format("%4d%% ", percent);
		else
			writer.write("      ");

		writer.append(StringUtils.substring(message, 0, getConsoleWidth() - progressBarWidth - 8));
		
		if (percent == 100) {
			writer.write("\r\n");
			progressBarDrawn = false;
		} else {
			writer.append('\r');
			progressBarDrawn = true;
		}
		
		writer.flush();
		
		lastUpdatedAt = System.currentTimeMillis();
	}
	
	/**
	 * Tries to obtain the width of the console.
	 * 
	 * This is an absolutely unsafe, non-portable and maybe not-even-working solution
	 * to determine the width of the console.  It checks the environment variable named
	 * <tt>COLUMNS</tt> as it works under Linux.  If no such environment variable is
	 * found, it simply returns 80, which is a safe default for most of the cases.
	 */
	private int getConsoleWidth() {
		if (consoleWidth != null)
			return consoleWidth;
		
		consoleWidth = 80;
		
		try {
			if (System.getenv("COLUMNS") != null)
				consoleWidth = Integer.parseInt(System.getenv("COLUMNS"));
		} catch (NumberFormatException ex) {
			/* well, meh */
		}
		
		return consoleWidth;
	}
}
