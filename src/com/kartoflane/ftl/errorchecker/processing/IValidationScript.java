package com.kartoflane.ftl.errorchecker.processing;

import java.util.Set;

import org.jdom2.located.LocatedElement;

import bsh.EvalError;

import com.kartoflane.ftl.errorchecker.core.CheckerContext;
import com.kartoflane.ftl.layout.LayoutObject;


public interface IValidationScript {

	public void cacheData(CheckerContext context) throws EvalError;

	public Set<Error> validate(CheckerContext context, LocatedElement el) throws EvalError;

	public Set<Error> validate(CheckerContext context, LayoutObject lo) throws EvalError;
}
