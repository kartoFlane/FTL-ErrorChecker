package com.kartoflane.ftl.errorchecker.processing;

import com.kartoflane.ftl.errorchecker.core.CheckerContext;
import com.kartoflane.ftl.errorchecker.core.FilePointer;


public class ValidateTask<T> implements Runnable {

	private final FilePointer fi;
	private final T e;
	private final CheckerContext context;

	public ValidateTask(FilePointer fi, T e, CheckerContext context) {
		this.fi = fi;
		this.e = e;
		this.context = context;
	}

	@Override
	public void run() {
		context.getValidationManager().validateElement(context, fi, e);
	}
}
