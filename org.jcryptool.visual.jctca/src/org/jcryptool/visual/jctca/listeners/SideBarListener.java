package org.jcryptool.visual.jctca.listeners;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.jcryptool.visual.jctca.UserViews.CreateCert;
import org.jcryptool.visual.jctca.UserViews.ShowCert;
import org.jcryptool.visual.jctca.UserViews.SignCert;

public class SideBarListener implements SelectionListener {

	CreateCert cCert;
	ShowCert sCert;
	SignCert siCert;
	Composite comp_right;
	Group grp_exp;

	public SideBarListener(CreateCert cCert, ShowCert sCert, SignCert siCert, Group grp_exp, Composite comp_right) {
		this.comp_right = comp_right;
		this.grp_exp = grp_exp;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		
	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		Button btn = (Button) arg0.getSource();
		//get what button was pressed: 0 - create cert, 1 - manage certs, 2 - sign stuff
		Integer data = (Integer) btn.getData();
		int pressed = data.intValue();

		if (cCert != null) {
			cCert.dispose();
		}
		if (sCert != null) {
			sCert.dispose();
		}
		if (siCert != null) {
			siCert.dispose();
		}
		switch (pressed) {
		case 0:
			cCert = new CreateCert(comp_right, grp_exp);
			cCert.setVisible(true);
			break;
		case 1:
			sCert = new ShowCert(comp_right, grp_exp);
			sCert.setVisible(true);
			break;
		case 2:
			siCert = new SignCert(comp_right, grp_exp);
			siCert.setVisible(true);
			break;
		}
		
		comp_right.layout(true);
		grp_exp.layout(true);
	}

}
