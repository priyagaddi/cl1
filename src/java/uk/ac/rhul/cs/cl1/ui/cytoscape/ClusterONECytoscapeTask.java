package uk.ac.rhul.cs.cl1.ui.cytoscape;

import uk.ac.rhul.cs.cl1.ClusterONE;
import uk.ac.rhul.cs.cl1.ClusterONEAlgorithmParameters;
import uk.ac.rhul.cs.cl1.ClusterONEException;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

/**
 * Wrapper object for {@link ClusterONE} that makes it compatible with
 * Cytoscape's {@link Task} interface.
 * 
 * @author ntamas
 */
public class ClusterONECytoscapeTask extends ClusterONE implements Task {
	/**
	 * Task monitor used to signal exceptions and such
	 */
	TaskMonitor cytoscapeTaskMonitor = null;
	
	public ClusterONECytoscapeTask(ClusterONEAlgorithmParameters parameters) {
		super(parameters);
	}

	public String getTitle() {
		return ClusterONE.applicationName;
	}

	public void run() {
		if (cytoscapeTaskMonitor == null)
			throw new IllegalStateException("Task monitor is not set");
		
		try {
			super.run();
		} catch (ClusterONEException e) {
			cytoscapeTaskMonitor.setException(e, e.getMessage());
		} catch (Exception e) {
			cytoscapeTaskMonitor.setException(e, "Unexpected error while running ClusterONE. "+
					"Please notify the plugin authors!");
		}
	}

	public void setTaskMonitor(TaskMonitor taskMonitor)
			throws IllegalThreadStateException {
		if (taskMonitor == null) {
			this.cytoscapeTaskMonitor = null;
			super.setTaskMonitor(null);
		} else {
			this.cytoscapeTaskMonitor = taskMonitor;
			super.setTaskMonitor(new CytoscapeTaskMonitorWrapper(this.cytoscapeTaskMonitor));
		}
	}
}
