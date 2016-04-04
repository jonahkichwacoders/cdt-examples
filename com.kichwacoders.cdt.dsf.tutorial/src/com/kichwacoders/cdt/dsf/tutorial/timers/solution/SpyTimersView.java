package com.kichwacoders.cdt.dsf.tutorial.timers.solution;

import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import com.kichwacoders.cdt.dsf.tutorial.internal.TutorialPlugin;
import com.kichwacoders.cdt.dsf.tutorial.timers.TimerService;
import com.kichwacoders.cdt.dsf.tutorial.timers.TimerService.TimerDMContext;
import com.kichwacoders.cdt.dsf.tutorial.timers.TimersView;

public class SpyTimersView extends ViewPart {

	private static final String TOGGLE_STATE_PREF_KEY = SpyTimersView.class.getName() + ".toggle.state";
	private MenuManager fMenuManager;
	private StyledText fLogText;
	private Job fPollingJob;

	public SpyTimersView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		fLogText = new StyledText(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		fLogText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fMenuManager = new MenuManager();
		Menu menu = fMenuManager.createContextMenu(fLogText);
		fLogText.setMenu(menu);
		getViewSite().registerContextMenu(fMenuManager, null);

		// Display the new state to the user
		boolean toggledState = getToggledState();
		fLogText.setText(Boolean.toString(toggledState));
		// Create the polling job if the spy is enabled
		if (toggledState) {
			startPollingJob();
		}
	}

	@Override
	public void setFocus() {
		fLogText.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		fMenuManager.dispose();
		cancelPollingJob();
	}

	public boolean getToggledState() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(TutorialPlugin.PLUGIN_ID);
		String togglePrefValue = preferences.get(TOGGLE_STATE_PREF_KEY, Boolean.FALSE.toString());
		return Boolean.parseBoolean(togglePrefValue);
	}

	public void setToggledState(boolean newState) {
		boolean oldState = getToggledState();
		if (oldState != newState) {
			// Display the new state to the user
			if (!fLogText.isDisposed()) {
				fLogText.setText(Boolean.toString(newState));
			}

			// Save the toggle state in a preference so that it's remembered
			// next time the view is opened
			IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(TutorialPlugin.PLUGIN_ID);
			preferences.put(TOGGLE_STATE_PREF_KEY, Boolean.toString(newState));

			// Create the polling job if the spy is enabled
			if (newState) {
				startPollingJob();
			} else {
				cancelPollingJob();
			}
		}

	}

	private void startPollingJob() {
		fPollingJob = new Job("Timers Spy Polling Job - Solution") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Ignored
				}

				if (monitor.isCanceled()) {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							setToggledState(false); // small bug: this set state to false when it could have already been put back to true
						}
					});
					// Stop here to cancel the repeating job
					return Status.OK_STATUS;
				}

				doWork();
				
				schedule();

				return Status.OK_STATUS;
			}
			
			private void doWork() {
				DsfSession[] activeSessions = DsfSession.getActiveSessions();
				DsfSession session = null;
				for (DsfSession dsfSession : activeSessions) {
					if (dsfSession.getOwnerId().equals(TimersView.TIMERS_DSF_EXAMPLE_SESSION_OWNER_ID)) {
						session = dsfSession;
					}
				}
				if (session == null) {
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							if (!fLogText.isDisposed()) {
								fLogText.setText("The Timer View is not open, nothing to spy");
							}
						}
					});
					return;
				}
				
				String sessionId = session.getId();

			
				session.getExecutor().execute(new Runnable() {
					
					@Override
					public void run() {
						// Get Stack service using a DSF services tracker object
						DsfServicesTracker tracker = new DsfServicesTracker(TutorialPlugin.getBundleContext(), sessionId);	
						final TimerService timerService = tracker.getService(TimerService.class);
						tracker.dispose(); //must dispose or could have a service reference leak

						StringBuilder sb = new StringBuilder();
						TimerDMContext[] timers = timerService.getTimers();
						for (TimerDMContext timerDMContext : timers) {
							int timerValue = timerService.getTimerValue(timerDMContext);
							sb.append("Timer " + timerDMContext + " = " + timerValue + "\n");
						}
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								if (!fLogText.isDisposed()) {
									fLogText.setText(sb.toString());
								}
							}
						});
					}
				});
			}
		};
		fPollingJob.schedule();
	}

	private void cancelPollingJob() {
		if (fPollingJob != null) {
			fPollingJob.cancel();
		}
	}
	

}