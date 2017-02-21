//-----BEGIN DISCLAIMER-----
/*******************************************************************************
* Copyright (c) 2017 JCrypTool Team and Contributors
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
//-----END DISCLAIMER-----
package org.jcryptool.visual.babystepgiantstep.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jcryptool.visual.babystepgiantstep.views.BabystepGiantstepView;

/**
 * @author Miray Inel
 */
public class RestartHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.getActivePart(event) instanceof BabystepGiantstepView) {
			BabystepGiantstepView view = ((BabystepGiantstepView) HandlerUtil.getActivePart(event));

			view.resetView();
		}
		return null;
	}

}
