package com.kartoflane.ftl.errorchecker.ui;

import java.awt.Frame;

import net.vhati.modmanager.ui.ProgressDialog;

import com.kartoflane.ftl.errorchecker.processing.TaskObserver;


@SuppressWarnings("serial")
public class TaskProgressDialog extends ProgressDialog implements TaskObserver {

	private Thread taskThread = null;


	public TaskProgressDialog( Frame owner, boolean continueOnSuccess ) {
		super( owner, continueOnSuccess );
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		setTitle( "Processing..." );

		setSize( 500, 170 );
		setMinimumSize( this.getPreferredSize() );
		setLocationRelativeTo( owner );
	}

	@Override
	public void taskProgress( int value, int max ) {
		this.setProgressLater( value, max );
	}

	@Override
	public void taskStatus( String message ) {
		setStatusTextLater( message != null ? message : "..." );
	}

	@Override
	public void taskFinished( boolean outcome, Exception e ) {
		setTaskOutcomeLater( outcome, e );
	}

	public void setTaskThread( Thread thread ) {
		taskThread = thread;
	}

	@Override
	public void dispose() {
		if ( !done ) {
			if ( taskThread != null ) {
				taskThread.interrupt();
			}
			setProgressLater( 0, 100 );
			// setStatusTextLater( "Aborted by user." );
			setTaskOutcomeLater( false, null );
		}
		else {
			super.dispose();
		}
	}
}
