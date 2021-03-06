/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.kichwacoders.cdt.dsf.gdb.tutorial.framespy.polling.solution;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

public class FrameSpyPollingHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		FrameSpyPollingView part = (FrameSpyPollingView) HandlerUtil.getActivePartChecked(event);
		part.setToggledState(!part.getToggledState());
		return null;
	}
}
