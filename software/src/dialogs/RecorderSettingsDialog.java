package dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sound.RecorderThread;

public class RecorderSettingsDialog extends TitleAreaDialog {
	private Text refreshPeriodText;
	private int newPeriod = -1;

	public RecorderSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();

		setTitle("Recorder settings");
		setMessage("", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		Label refreshPeriodLabel = new Label(container, SWT.NONE);
		refreshPeriodLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		refreshPeriodLabel.setText("Refresh period (ms): ");

		refreshPeriodText = new Text(container, SWT.BORDER);
		refreshPeriodText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		refreshPeriodText.setEditable(true);
		refreshPeriodText.setText(((Integer) RecorderThread.getWritingInterval()).toString());

		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	};

	@Override
	protected void okPressed() {
		// Save settings
		String name = refreshPeriodText.getText().trim();

		if (name.isEmpty()) {
			setMessage("Type a period to continue.", IMessageProvider.ERROR);
			refreshPeriodText.setFocus();
		} else {
			try {
				Integer newPeriodTmp = Integer.parseInt(name);
				if (!RecorderThread.checkWritingInterval(newPeriodTmp)) {
					refreshPeriodText.setText(((Integer) RecorderThread.getWritingInterval()).toString());
					setMessage("This period is not correct.", IMessageProvider.ERROR);
					refreshPeriodText.setFocus();
				} else {
					newPeriod = newPeriodTmp;
					super.okPressed();
				}
			} catch (NumberFormatException e) {
				refreshPeriodText.setText(((Integer) RecorderThread.getWritingInterval()).toString());
				setMessage("This period is not correct.", IMessageProvider.ERROR);
				refreshPeriodText.setFocus();
			}
		}
	}

	/**
	 * Return the new period
	 * 
	 * @return The new period (can only be valid or -1)
	 */
	public int getNewPeriod() {
		return newPeriod;
	}
}
