// -----BEGIN DISCLAIMER-----
/*******************************************************************************
 * Copyright (c) 2011, 2019 JCrypTool Team and Contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
// -----END DISCLAIMER-----
package org.jcryptool.visual.ECDH.ui.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.jcryptool.core.logging.utils.LogUtil;
import org.jcryptool.core.operations.util.PathEditorInput;
import org.jcryptool.core.util.constants.IConstants;
import org.jcryptool.core.util.directories.DirectoryService;
import org.jcryptool.core.util.fonts.FontService;
import org.jcryptool.visual.ECDH.ECDHPlugin;
import org.jcryptool.visual.ECDH.ECDHUtil;
import org.jcryptool.visual.ECDH.Messages;
import org.jcryptool.visual.ECDH.algorithm.EC;
import org.jcryptool.visual.ECDH.algorithm.ECFm;
import org.jcryptool.visual.ECDH.algorithm.ECPoint;
import org.jcryptool.visual.ECDH.ui.wizards.PublicParametersComposite;
import org.jcryptool.visual.ECDH.ui.wizards.PublicParametersWizard;
import org.jcryptool.visual.ECDH.ui.wizards.SecretKeyComposite;
import org.jcryptool.visual.ECDH.ui.wizards.SecretKeyWizard;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurve;
import de.flexiprovider.common.math.ellipticcurves.Point;

public class ECDHComposite extends Composite {

	private Button btnSetPublicParameters = null;
	private Button btnChooseSecrets = null;
	private GridData gd_btnChooseSecrets;
	private Button btnCreateSharedKeys = null;
	private Button btnExchangeKeys = null;
	private Button btnGenerateKey = null;
	private Button btnSecretA = null;
	private Button btnCalculateSharedA = null;
	private Button btnCalculateKeyA = null;
	private Button btnSecretB = null;
	private Button btnCalculateSharedB = null;
	private Button btnCalculateKeyB = null;
	private Button btn_showAnimation;
	private Canvas canvasBtn = null;
	private Canvas canvasExchange = null;
	private Canvas canvasKey = null;
	private Color cRed = new Color(Display.getCurrent(), 214, 100, 100);
	private Color cGreen = new Color(Display.getCurrent(), 140, 220, 132);
	private Color grey = new Color(Display.getCurrent(), 140, 138, 140);
	private Group groupAlice = null;
	private Group groupBob = null;
	private Group groupMain = null;
	private Group groupParameters = null;
	private Group settings;
	private Label placeholder;
	private Text infoText;
	private Text textCurve = null;
	private Text textGenerator = null;
	private Text textSecretA = null;
	private Text textSharedA = null;
	private Text textCommonKeyA = null;
	private Text textSecretB = null;
	private Text textSharedB = null;
	private Text textCommonKeyB = null;
	private EC curve; // @jve:decl-index=0:
	private int[] elements;
	private int secretA = -1;
	private int secretB = -1;
	private FlexiBigInt secretLargeA = null;
	private FlexiBigInt secretLargeB = null;
	private ECPoint shareA;
	private ECPoint shareB;
	private Point shareLargeA;
	private Point shareLargeB;
	private ECPoint keyA;
	private ECPoint keyB;
	private Point keyLargeA;
	private Point keyLargeB;
	private ECPoint generator;
	private int valueN;
	private ECDHView view;
	private File logFile;
	private boolean large;
	private EllipticCurve largeCurve;
	private Point pointG;
	private FlexiBigInt largeOrder;
	private boolean showAnimation = true;
	private final String saveToEditorCommandId = "org.jcryptool.visual.ecdh.commands.saveToEditor"; //$NON-NLS-1$
	private AbstractHandler saveToEditorHandler;
	private final String saveToFileCommandId = "org.jcryptool.visual.ecdh.commands.saveToFile"; //$NON-NLS-1$
	private AbstractHandler saveToFileHandler;
	private IServiceLocator serviceLocator;
	private boolean chooseSecretButtonResets;
	private Image id;

	private static final int RESET_ALL = 0;
	private static final int RESET_PUBLIC_PARAMETERS = 1;
	private static final int RESET_SECRET_PARAMETERS = 2;

	public ECDHComposite(Composite parent, int style, ECDHView view) {
		super(parent, style);
		this.view = view;
		setLayout(new GridLayout());
		createCompositeIntro();
		createGroupMain();
		// createInfoGroup();

		serviceLocator = PlatformUI.getWorkbench();
		IMenuManager dropDownMenu = view.getViewSite().getActionBars().getMenuManager();
		final String showAnimationCommandId = "org.jcryptool.visual.ecdh.commands.showAnimation"; //$NON-NLS-1$
		AbstractHandler showAnimationHandler = new AbstractHandler() {
			public Object execute(ExecutionEvent event) {
				toggleAnimation();
				return null;
			}
		};
		defineCommand(showAnimationCommandId, Messages.getString("ECDHComposite.0"), showAnimationHandler); //$NON-NLS-1$
		addContributionItem(dropDownMenu, showAnimationCommandId, null, null, SWT.CHECK);
		dropDownMenu.add(new Separator());
		saveToEditorHandler = new AbstractHandler() {
			public Object execute(ExecutionEvent event) {
				saveToEditor();
				return null;
			}
		};
		defineCommand(saveToEditorCommandId, Messages.getString("ECDHComposite.2"), null); // don't enable the //$NON-NLS-1$
																							// command
																							// until we have results to
																							// save
		addContributionItem(dropDownMenu, saveToEditorCommandId, null, null, SWT.PUSH);
		saveToFileHandler = new AbstractHandler() {
			public Object execute(ExecutionEvent event) {
				saveToFile();
				return null;
			}
		};
		defineCommand(saveToFileCommandId, Messages.getString("ECDHComposite.3"), null); // don't enable the command //$NON-NLS-1$
																							// until we have results to
																							// save
		addContributionItem(dropDownMenu, saveToFileCommandId, null, null, SWT.PUSH);

		IToolBarManager toolBarMenu = view.getViewSite().getActionBars().getToolBarManager();
		final String resetCommandId = "org.jcryptool.visual.ecdh.commands.reset"; //$NON-NLS-1$
		AbstractHandler resetHandler = new AbstractHandler() {
			public Object execute(ExecutionEvent event) {
				reset(RESET_ALL);
				return null;
			}
		};
		defineCommand(resetCommandId, Messages.getString("ECDHComposite.4"), resetHandler); // $NON_NLS-1$ //$NON-NLS-1$
		addContributionItem(toolBarMenu, resetCommandId, ECDHPlugin.getImageDescriptor("icons/reset.gif"), null, //$NON-NLS-1$
				SWT.PUSH);
	}

	private void createInfoGroup() {
		settings = new Group(this, SWT.NONE);
		settings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		settings.setLayout(new GridLayout());
		settings.setText(Messages.getString("ECDHComposite.settingsGroupTitle")); //$NON-NLS-1$

		btn_showAnimation = new Button(settings, SWT.CHECK);
		btn_showAnimation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btn_showAnimation.setSelection(showAnimation);
		btn_showAnimation.setText(Messages.getString("ECDHComposite.6")); //$NON-NLS-1$
		btn_showAnimation.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				showAnimation = showAnimation ? false : true;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

	private void createCanvasBtn(Group parent) {
		canvasBtn = new Canvas(parent, SWT.NO_REDRAW_RESIZE);
		canvasBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 3));
		canvasBtn.setLayout(new GridLayout());

		// All the Buttons and Buttonlistener are here
		btnSetPublicParameters = new Button(canvasBtn, SWT.NONE);
		GridData gd_btnSetPublicParameters = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd_btnSetPublicParameters.heightHint = 60;
		btnSetPublicParameters.setLayoutData(gd_btnSetPublicParameters);
		btnSetPublicParameters.setBackground(cRed);
		btnSetPublicParameters.setText(Messages.getString("ECDHView.setPublicParameters")); //$NON-NLS-1$
		btnSetPublicParameters.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Information needed to adapt the button distance
				int previousSize = textCurve.getLineCount() * textCurve.getLineHeight();
				int currentSize;
				
				PublicParametersWizard wiz = new PublicParametersWizard(curve, generator);
				WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
				dialog.setHelpAvailable(false);
				if (dialog.open() == Window.OK) {
					reset(RESET_PUBLIC_PARAMETERS);
					groupMain.requestLayout();
					infoText.setText(Messages.getString("ECDHView.Step1") +
									 Messages.getString("ECDHView.Step2"));
					large = wiz.isLarge();
					String curveString, generatorString;
					if (large) {
						largeCurve = wiz.getLargeCurve();
						pointG = wiz.getLargeGenerator();
						largeOrder = wiz.getLargeOrder();
						curveString = formatLargeCurve(largeCurve.toString(), wiz.getLargeCurveType());
						generatorString = formatLargeGenerator(pointG.getXAffin().toString(),
															   pointG.getYAffin().toString(),
															   wiz.getLargeCurveType());				
						textCurve.setText(curveString);			
						textGenerator.setText(generatorString);
					} else {
						curve = wiz.getCurve();
						if (curve != null && curve.getType() == ECFm.ECFm)
							elements = ((ECFm) curve).getElements();
						textCurve.setText(curve.toString());
						generator = wiz.getGenerator();
						valueN = wiz.getOrder();
						textGenerator.setText(generator.toString());
					}
					btnChooseSecrets.setEnabled(true);
					btnSetPublicParameters.setBackground(cGreen);
				}
			
				currentSize = textCurve.getLineCount() * textCurve.getLineHeight();
				gd_btnChooseSecrets.verticalIndent += currentSize - previousSize;  // Calculate the new size this way
				canvasBtn.requestLayout();
				canvasBtn.layout(true);
				btnSecretA.setEnabled(true);
				btnSecretB.setEnabled(true);
			}
		});

		btnChooseSecrets = new Button(canvasBtn, SWT.NONE);
		gd_btnChooseSecrets = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd_btnChooseSecrets.verticalIndent = 40;
		gd_btnChooseSecrets.heightHint = 60;
		btnChooseSecrets.setLayoutData(gd_btnChooseSecrets);
		btnChooseSecrets.setEnabled(false);
		btnChooseSecrets.setBackground(cRed);
		btnChooseSecrets.setText(Messages.getString("ECDHView.chooseSecrets")); //$NON-NLS-1$
		btnChooseSecrets.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// This button changes behaviour. In the first place it has no special purpose.
				// If the program has proceeded it will function as reset-to-this-step button
				// Therefore this is false by default and set true in the next step
				if (chooseSecretButtonResets) {
					reset(ECDHComposite.RESET_SECRET_PARAMETERS);
					infoText.setText(Messages.getString("ECDHView.Step1") +
					         		 Messages.getString("ECDHView.Step2") +
					                 Messages.getString("ECDHView.Step3"));
					chooseSecretButtonResets = false;
				}
			}
		});

		btnCreateSharedKeys = new Button(canvasBtn, SWT.NONE);
		GridData gd_btnCreateSharedKeys = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd_btnCreateSharedKeys.verticalIndent = 50;
		gd_btnCreateSharedKeys.heightHint = 60;
		btnCreateSharedKeys.setLayoutData(gd_btnCreateSharedKeys);
		btnCreateSharedKeys.setEnabled(false);
		btnCreateSharedKeys.setBackground(cRed);
		btnCreateSharedKeys.setText(Messages.getString("ECDHView.createSharedKeys")); //$NON-NLS-1$

		btnExchangeKeys = new Button(canvasBtn, SWT.NONE);
		GridData gd_btnExchangeKeys = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd_btnExchangeKeys.verticalIndent = 50;
		gd_btnExchangeKeys.heightHint = 60;
		btnExchangeKeys.setLayoutData(gd_btnExchangeKeys);
		btnExchangeKeys.setEnabled(false);
		btnExchangeKeys.setBackground(cRed);
		btnExchangeKeys.setText(Messages.getString("ECDHView.exchangeSharedKeys")); //$NON-NLS-1$
		btnExchangeKeys.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new Animate().run();
				infoText.setText(Messages.getString("ECDHView.Step1") +
						         Messages.getString("ECDHView.Step2") +
						         Messages.getString("ECDHView.Step3") +
						         Messages.getString("ECDHView.Step4") +
						         Messages.getString("ECDHView.Step5"));
				btnGenerateKey.setEnabled(true);
				btnExchangeKeys.setBackground(cGreen);
				btnCalculateKeyA.setEnabled(true);
				btnCalculateKeyB.setEnabled(true);
			}
		});

		btnGenerateKey = new Button(canvasBtn, SWT.NONE);
		GridData gd_btnGenerateKey = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd_btnGenerateKey.verticalIndent = 50;
		gd_btnGenerateKey.heightHint = 60;
		btnGenerateKey.setLayoutData(gd_btnGenerateKey);
		btnGenerateKey.setEnabled(false);
		btnGenerateKey.setBackground(cRed);
		btnGenerateKey.setText(Messages.getString("ECDHView.generateCommonKey")); //$NON-NLS-1$
		btnGenerateKey.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				generateBothCommonKeys();
			}
		});

		canvasBtn.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {

				// der Strich soll bei 1/3 links der Buttonbreite verlaufen
				int x1 = btnSetPublicParameters.getBounds().x + (btnSetPublicParameters.getBounds().width / 3) - 5;
				int y1 = btnSetPublicParameters.getBounds().y;
				int width = 10;

				Path connection = new Path(Display.getCurrent());
				// waagerechte linie Oben
				connection.moveTo(x1, y1);

				int x2 = x1 + width;
				connection.lineTo(x2, y1);

				// 60(Buttonhöhe) + 40(Abstand der auch zeischen den anderen bUttons ist) -
				// 5(damit ser Strich mittig ist)
				int y3 = btnGenerateKey.getBounds().y + 120 - 5;
				connection.lineTo(x2, y3);

				// 40 Platz den die Pfeilspitze haben soll
				int x4 = canvasBtn.getBounds().x + canvasBtn.getBounds().width - 40;
				connection.lineTo(x4, y3);

				// Strich geht 10 nach oben
				int y5 = y3 - 10;
				connection.lineTo(x4, y5);

				// Pfeilspitze
				int x6 = canvasBtn.getBounds().x + canvasBtn.getBounds().width - 10;
				int y6 = y5 + 15;
				connection.lineTo(x6, y6);

				int y7 = y6 + 15;
				connection.lineTo(x4, y7);

				int y8 = y7 - 10;
				connection.lineTo(x4, y8);

				// weiter zur linken unteren Ecke
				connection.lineTo(x1, y8);

				// und wieder nach oben
				connection.lineTo(x1, y1);

				e.gc.setBackground(grey);
				e.gc.fillPath(connection);
			}
		});

		placeholder = new Label(canvasBtn, SWT.NONE);
		GridData gd_placeholder = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd_placeholder.verticalIndent = 65;
		gd_placeholder.heightHint = 10;
		placeholder.setLayoutData(gd_placeholder);
		placeholder.setVisible(false);

	}

	private void defineCommand(final String commandId, final String name, AbstractHandler handler) {
		ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
		Command command = commandService.getCommand(commandId);
		command.define(name, null, commandService.getCategory(CommandManager.AUTOGENERATED_CATEGORY_ID));
		command.setHandler(handler);
	}

	private void addContributionItem(IContributionManager manager, final String commandId, final ImageDescriptor icon,
			final String tooltip, int Style) {
		CommandContributionItemParameter param = new CommandContributionItemParameter(serviceLocator, null, commandId, Style);
		if (icon != null)
			param.icon = icon;
		if (tooltip != null && !tooltip.equals("")) //$NON-NLS-1$
			param.tooltip = tooltip;
		CommandContributionItem item = new CommandContributionItem(param);
		manager.add(item);
	}

	@Override
	public void dispose() {
		id.dispose();
		cRed.dispose();
		cGreen.dispose();
		super.dispose();
	}

	/**
	 * This method initializes compositeIntro
	 *
	 */
	private void createCompositeIntro() {
		Composite compositeIntro = new Composite(this, SWT.NONE);
		compositeIntro.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		compositeIntro.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		compositeIntro.setLayout(new GridLayout(1, false));

		Label label = new Label(compositeIntro, SWT.NONE);
		label.setFont(FontService.getHeaderFont());
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		label.setText(Messages.getString("ECDHView.title")); //$NON-NLS-1$

		StyledText stDescription = new StyledText(compositeIntro, SWT.READ_ONLY | SWT.WRAP);
		stDescription.setText(Messages.getString("ECDHView.description")); //$NON-NLS-1$
		GridData gd_stDescription = new GridData(SWT.FILL, SWT.FILL, false, false); // TODO Plugin Description
		gd_stDescription.widthHint = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		stDescription.setLayoutData(gd_stDescription);
	}


	protected void toggleAnimation() {
		showAnimation = !showAnimation;
	}

	/**
	 * This method initializes groupMain
	 *
	 */
	private void createGroupMain() {
		groupMain = new Group(this, SWT.NONE); // TODO groupMain
		groupMain.setLayout(new GridLayout(5, false));
		groupMain.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		createCanvasBtn(groupMain);
		createGroupParameters(groupMain);
		createGroupAlice(groupMain);
		createCanvasExchange(groupMain);
		createGroupBob(groupMain);
		createGroupInfo();
		createCanvasKey(groupMain);

		groupMain.setText(Messages.getString("ECDHView.groupMain")); //$NON-NLS-1$
	}

	private void createCanvasKey(Group parent) {
		id = ECDHPlugin.getImageDescriptor("icons/key.png").createImage(); //$NON-NLS-1$
		canvasKey = new Canvas(parent, SWT.NO_REDRAW_RESIZE);
		GridData gd_canvasKey = new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1);
		gd_canvasKey.verticalIndent = 10;
		gd_canvasKey.widthHint = 850;
		gd_canvasKey.heightHint = 69;
		canvasKey.setLayoutData(gd_canvasKey);
		canvasKey.setVisible(false);
		canvasKey.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
					e.gc.drawImage(id, 305, 0);
			}
		});
	}

	private void createCanvasExchange(Group parent) {
		canvasExchange = new Canvas(parent, SWT.NO_REDRAW_RESIZE);
		GridData gd_canvasExchange = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd_canvasExchange.widthHint = 150;
		gd_canvasExchange.heightHint = 400;
		canvasExchange.setLayoutData(gd_canvasExchange);
		canvasExchange.setLayout(new GridLayout());

		canvasExchange.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {

				int canvasWidth = canvasExchange.getBounds().width;

				GC gc = e.gc;
				Path ab = new Path(Display.getCurrent());
				// linker rand des canvas
				int x1 = 0;
				// mitte von textSharedA -5
				int y1 = textSharedA.getBounds().y + (textSharedA.getBounds().height / 2) - 5;
				ab.moveTo(x1, y1);
				// linkes viertel des Canvas
				int x2 = (canvasWidth / 4) + 5;
				ab.lineTo(x2, y1);
				// schwierig zu beschreiben
				int y3 = textCommonKeyB.getBounds().y + (textCommonKeyB.getBounds().height / 2) - (canvasWidth * 2 / 4);
				ab.lineTo(x2, y3);
				// rechter rand des canvas
				int x4 = canvasWidth - canvasWidth / 4;
				int y4 = textCommonKeyB.getBounds().y + textCommonKeyB.getBounds().height / 2 - 5;
				ab.lineTo(x4, y4);
				// rechter Rand des Canvas - 20(Platz für den Pfeil)
				int x5 = canvasWidth - 20;
				ab.lineTo(x5, y4);
				int y6 = y4 - 5;
				ab.lineTo(x5, y6);
				// Pfeilspitze
				int x7 = canvasWidth;
				int y7 = textCommonKeyB.getBounds().y + textCommonKeyB.getBounds().height / 2;
				ab.lineTo(x7, y7);
				int y8 = y4 + 15;
				ab.lineTo(x5, y8);
				int y9 = y8 - 5;
				ab.lineTo(x5, y9);
				int x10 = x4 - 4;
				ab.lineTo(x10, y9);
				int x11 = x2 - 10;
				int y11 = y3 + 4;
				ab.lineTo(x11, y11);
				int y12 = y1 + 10;
				ab.lineTo(x11, y12);
				ab.lineTo(x1, y12);
				// und zurück zum anfang
				ab.lineTo(x1, y1);
				gc.setBackground(grey);
				gc.fillPath(ab);

				Path ba = new Path(Display.getCurrent());

				int bax1 = canvasWidth;
				int bay1 = textSharedB.getBounds().y + (textSharedB.getBounds().height / 2) - 5;
				ba.moveTo(bax1, bay1);
				int bax2 = (3 * canvasWidth / 4) - 5;
				ba.lineTo(bax2, bay1);
				int bay3 = textCommonKeyA.getBounds().y + (textCommonKeyA.getBounds().height / 2) - (canvasWidth * 2 / 4);
				ba.lineTo(bax2, bay3);
				// linker rand des canvas
				int bax4 = canvasWidth / 4;
				int bay4 = textCommonKeyA.getBounds().y + textCommonKeyA.getBounds().height / 2 - 5;
				ba.lineTo(bax4, bay4);
				// rechter Rand des Canvas - 20(Platz für den Pfeil)
				int bax5 = 20;
				ba.lineTo(bax5, bay4);
				int bay6 = bay4 - 5;
				ba.lineTo(bax5, bay6);
				// Pfeilspitze
				int bax7 = 0;
				int bay7 = textCommonKeyA.getBounds().y + textCommonKeyA.getBounds().height / 2;
				ba.lineTo(bax7, bay7);
				int bay8 = bay4 + 15;
				ba.lineTo(bax5, bay8);
				int bay9 = bay8 - 5;
				ba.lineTo(bax5, bay9);
				int bax10 = bax4 + 4;
				ba.lineTo(bax10, bay9);
				int bax11 = bax2 + 10;
				int bay11 = bay3 + 4;
				ba.lineTo(bax11, bay11);
				int bay12 = bay1 + 10;
				ba.lineTo(bax11, bay12);
				ba.lineTo(bax1, bay12);
				// und zurück zum anfang
				ba.lineTo(bax1, bay1);
				e.gc.fillPath(ba);
			}
		});

	}

	/**
	 * This method initializes groupParameters
	 *
	 */
	private void createGroupParameters(Group parent) {
		groupParameters = new Group(parent, SWT.NONE);
		groupParameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		groupParameters.setText(Messages.getString("ECDHView.groupParameters")); //$NON-NLS-1$
		GridLayout gridLayout = new GridLayout(2, false);
		groupParameters.setLayout(gridLayout);
		Label label = new Label(groupParameters, SWT.NONE);
		label.setText(Messages.getString("ECDHView.labelCurve")); //$NON-NLS-1$
		textCurve = new Text(groupParameters, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		textCurve.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		textCurve.setFont(FontService.getNormalMonospacedFont());
		label = new Label(groupParameters, SWT.NONE);
		label.setText(Messages.getString("ECDHView.labelGenerator")); //$NON-NLS-1$
		textGenerator = new Text(groupParameters, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		textGenerator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		textGenerator.setFont(FontService.getNormalMonospacedFont());
	}

	/**
	 * This method initializes groupAlice
	 *
	 */
	private void createGroupAlice(Group parent) {
		groupAlice = new Group(parent, SWT.NONE);
		GridData gd_groupAlice = new GridData(SWT.DEFAULT, SWT.FILL, false, false);
		gd_groupAlice.widthHint = 300;
		gd_groupAlice.heightHint = 400;
		groupAlice.setLayoutData(gd_groupAlice);
		groupAlice.setText("Alice"); //$NON-NLS-1$
		groupAlice.setLayout(new GridLayout(2, false));

		btnSecretA = new Button(groupAlice, SWT.NONE);
		btnSecretA.setText(Messages.getString("ECDHView.secret")); //$NON-NLS-1$
		btnSecretA.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 2, 1));
		btnSecretA.setBackground(cRed);
		btnSecretA.setEnabled(false);
		btnSecretA.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SecretKeyWizard wiz;
				if (large)
					wiz = new SecretKeyWizard("Alice", secretLargeA, largeOrder); //$NON-NLS-1$
				else
					wiz = new SecretKeyWizard("Alice", secretA, valueN); //$NON-NLS-1$
				WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
				dialog.setHelpAvailable(false);
				dialog.setPageSize(SecretKeyComposite.minimumWidth, SecretKeyComposite.minimumHeight);
				if (dialog.open() == Window.OK) {
					reset(RESET_SECRET_PARAMETERS); // If the user goes back to this point reset from here
					if (large) {
						secretLargeA = wiz.getLargeSecret();
						if (secretLargeA != null && secretLargeB != null) {
							enableCalculateSharedButtons();
							btnCreateSharedKeys.setEnabled(true);
							btnChooseSecrets.setBackground(cGreen);
						}
					} else {
						secretA = wiz.getSecret();
						if (secretA > 0 && secretB > 0) {
							enableCalculateSharedButtons();
							btnCreateSharedKeys.setEnabled(true);
							btnChooseSecrets.setBackground(cGreen);
						}
					}
					textSecretA.setText("xxxxxxxxxxxxxxxxxxxxxx"); //$NON-NLS-1$
					btnSecretA.setBackground(cGreen);
				}
			}
		});
		Label label = new Label(groupAlice, SWT.NONE);
		label.setText("a ="); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));

		textSecretA = new Text(groupAlice, SWT.BORDER | SWT.PASSWORD | SWT.READ_ONLY);
		textSecretA.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		btnCalculateSharedA = new Button(groupAlice, SWT.NONE);
		btnCalculateSharedA.setText(Messages.getString("ECDHView.calculate")); //$NON-NLS-1$
		btnCalculateSharedA.setEnabled(false);
		btnCalculateSharedA.setBackground(cRed);
		btnCalculateSharedA.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		btnCalculateSharedA.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (large) {
					shareLargeA = multiplyLargePoint(pointG, secretLargeA);
					textSharedA.setText("(" + shareLargeA.getXAffin() + ", " + shareLargeA.getYAffin() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					shareA = curve.multiplyPoint(generator, secretA);
					textSharedA.setText(shareA.toString());
				}
				btnCalculateSharedA.setBackground(cGreen);
				// Tell the previous button btnChooseSecrets he acts in a reset function from now on 
				chooseSecretButtonResets = true;
				
				if ((large && shareLargeA != null && shareLargeB != null) || (!large && shareA != null && shareB != null)) {
					btnExchangeKeys.setEnabled(true);
					btnCreateSharedKeys.setBackground(cGreen);
					infoText.setText(Messages.getString("ECDHView.Step1") +
					         		 Messages.getString("ECDHView.Step2") +
					         		 Messages.getString("ECDHView.Step3") +
					         		 Messages.getString("ECDHView.Step4"));
				}
			}
		});
		label = new Label(groupAlice, SWT.NONE);
		label.setText("A ="); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		textSharedA = new Text(groupAlice, SWT.BORDER | SWT.READ_ONLY);
		textSharedA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		btnCalculateKeyA = new Button(groupAlice, SWT.NONE);
		btnCalculateKeyA.setText(Messages.getString("ECDHView.calculate")); //$NON-NLS-1$
		btnCalculateKeyA.setEnabled(false);
		btnCalculateKeyA.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true, 2, 1));
		btnCalculateKeyA.setBackground(cRed);
		btnCalculateKeyA.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				generateKeyA();
				btnCalculateKeyA.setBackground(cGreen);
				if (large) {
					keyLargeA = multiplyLargePoint(shareLargeB, secretLargeA);
					textCommonKeyA.setText(keyLargeA.getXAffin().toString());
				} else {
					keyA = curve.multiplyPoint(shareB, secretA);
					if (keyA == null)
						keyA = generator;
					textCommonKeyA.setText(keyA.toString());
				}
				btnCalculateKeyA.setBackground(cGreen);
				Boolean b;
				if (large)
					b = keyLargeA != null && keyLargeB != null;
				else
					b = keyA != null && keyB != null;
				if (b) {
					ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
					Command command = commandService.getCommand(saveToEditorCommandId);
					command.setHandler(saveToEditorHandler);
					command = commandService.getCommand(saveToFileCommandId);
					command.setHandler(saveToFileHandler);
					if (large)
						b = keyLargeA.getXAffin().equals(keyLargeB.getXAffin());
					else
						b = keyA.equals(keyB);
					if (b) {
						showKeyImageUpdateText();
						btnGenerateKey.setBackground(cGreen);
					} else {
						infoText.append(Messages.getString("ECDHView.messageFail"));
					}
				}
			}

		});

		label = new Label(groupAlice, SWT.NONE);
		label.setText("S ="); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));

		textCommonKeyA = new Text(groupAlice, SWT.BORDER | SWT.READ_ONLY);
		textCommonKeyA.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
	}

	/**
	 * This method initializes groupBob
	 *
	 */
	private void createGroupBob(Group parent) {
		groupBob = new Group(parent, SWT.NONE);
		GridData gd_groupBob = new GridData(SWT.DEFAULT, SWT.FILL, false, false);
		gd_groupBob.widthHint = 300;
		gd_groupBob.heightHint = 400;
		groupBob.setLayoutData(gd_groupBob);
		groupBob.setText("Bob"); //$NON-NLS-1$
		groupBob.setLayout(new GridLayout(2, false));

		btnSecretB = new Button(groupBob, SWT.NONE);
		btnSecretB.setText(Messages.getString("ECDHView.secret")); //$NON-NLS-1$
		btnSecretB.setEnabled(false);
		btnSecretB.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 2, 1));
		btnSecretB.setBackground(cRed);
		btnSecretB.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SecretKeyWizard wiz;
				if (large)
					wiz = new SecretKeyWizard("Bob", secretLargeB, largeOrder); //$NON-NLS-1$
				else
					wiz = new SecretKeyWizard("Bob", secretB, valueN); //$NON-NLS-1$

				WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
				dialog.setHelpAvailable(false);
				dialog.setPageSize(SecretKeyComposite.minimumWidth, SecretKeyComposite.minimumHeight);
				if (dialog.open() == Window.OK) {
					reset(RESET_SECRET_PARAMETERS); // If the user goes back to this point reset from here
					if (large) {
						secretLargeB = wiz.getLargeSecret();
						if (secretLargeA != null && secretLargeB != null) {
							enableCalculateSharedButtons();
							btnCreateSharedKeys.setEnabled(true);
							btnChooseSecrets.setBackground(cGreen);
						}
					} else {
						secretB = wiz.getSecret();
						if (secretA > 0 && secretB > 0) {
							enableCalculateSharedButtons();
							btnCreateSharedKeys.setEnabled(true);
							btnChooseSecrets.setBackground(cGreen);
						}
					}
					textSecretB.setText("xxxxxxxxxxxxxxxxxxxxxx"); //$NON-NLS-1$
					btnSecretB.setBackground(cGreen);
				}
			}

		});
		Label label = new Label(groupBob, SWT.NONE);
		label.setText("b ="); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));

		textSecretB = new Text(groupBob, SWT.BORDER | SWT.PASSWORD | SWT.READ_ONLY);
		textSecretB.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		btnCalculateSharedB = new Button(groupBob, SWT.NONE);
		btnCalculateSharedB.setText(Messages.getString("ECDHView.calculate")); //$NON-NLS-1$
		btnCalculateSharedB.setEnabled(false);
		btnCalculateSharedB.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		btnCalculateSharedB.setBackground(cRed);
		btnCalculateSharedB.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (large) {
					shareLargeB = multiplyLargePoint(pointG, secretLargeB);
					textSharedB.setText("(" + shareLargeB.getXAffin() + ", " + shareLargeB.getYAffin() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					shareB = curve.multiplyPoint(generator, secretB);
					textSharedB.setText(shareB.toString());
				}
				btnCalculateSharedB.setBackground(cGreen);
				// Tell the previous button btnChooseSecrets he acts in a reset function from now on 
				chooseSecretButtonResets = true;
				
				if ((large && shareLargeA != null && shareLargeB != null) || (!large && shareA != null && shareB != null)) {
					btnExchangeKeys.setEnabled(true);
					btnCreateSharedKeys.setBackground(cGreen);
					infoText.setText(Messages.getString("ECDHView.Step1") +
					         		 Messages.getString("ECDHView.Step2") +
					         		 Messages.getString("ECDHView.Step3") +
					         		 Messages.getString("ECDHView.Step4"));
				}
			}

		});
		label = new Label(groupBob, SWT.NONE);
		label.setText("B ="); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		textSharedB = new Text(groupBob, SWT.BORDER | SWT.READ_ONLY);
		textSharedB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		btnCalculateKeyB = new Button(groupBob, SWT.NONE);
		btnCalculateKeyB.setText(Messages.getString("ECDHView.calculate")); //$NON-NLS-1$
		btnCalculateKeyB.setEnabled(false);
		btnCalculateKeyB.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true, 2, 1));
		btnCalculateKeyB.setBackground(cRed);
		btnCalculateKeyB.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				generateKeyB();
				btnCalculateKeyB.setBackground(cGreen);
				Boolean b;
				if (large)
					b = keyLargeA != null && keyLargeB != null;
				else
					b = keyA != null && keyB != null;
				if (b) {
					ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
					Command command = commandService.getCommand(saveToEditorCommandId);
					command.setHandler(saveToEditorHandler);
					command = commandService.getCommand(saveToFileCommandId);
					command.setHandler(saveToFileHandler);
					if (large)
						b = keyLargeA.getXAffin().equals(keyLargeB.getXAffin());
					else
						b = keyA.equals(keyB);
					if (b) {
						showKeyImageUpdateText();
						btnGenerateKey.setBackground(cGreen);
					} else {
						infoText.append(Messages.getString("ECDHView.messageFail"));
					}
				}
			}
		});
		label = new Label(groupBob, SWT.NONE);
		label.setText("S ="); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));

		textCommonKeyB = new Text(groupBob, SWT.BORDER | SWT.READ_ONLY);
		textCommonKeyB.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
	}

	private void createGroupInfo() {
		Group groupInfo = new Group(groupMain, SWT.NONE);
		groupInfo.setLayout(new GridLayout());
		groupInfo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2));
		groupInfo.setText("Aktueller Schritt");

		infoText = new Text(groupInfo, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		infoText.setText(Messages.getString("ECDHView.Step1"));
		GridData infoTextLayout = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		infoTextLayout.widthHint = 370;
		infoText.setLayoutData(infoTextLayout);
	}

	private String intToBitString(int i, int length) {
		String s = ""; //$NON-NLS-1$
		int j = i;
		for (int k = 0; k < length; k++) {
			s = (j % 2) + s;
			j /= 2;
		}
		return s;
	}

	protected void saveToEditor() {
		saveToEditor(getLogString());
	}

	protected void saveToFile() {
		saveToFile(getLogString());
	}

	private String getLogString() {
		String s;
		if (large) {
			s = Messages.getString("ECDHView.logHeader") + "\n\n" + Messages.getString("ECDHView.curve") + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ largeCurve + "\n\n"; //$NON-NLS-1$

			s += Messages.getString("ECDHView.AliceParameters") + ":\n"; //$NON-NLS-1$ //$NON-NLS-2$
			s += Messages.getString("ECDHView.secretKey") + " = " + secretLargeA + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			s += Messages.getString("ECDHView.sharedKey") + " = " + shareLargeA.toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			s += Messages.getString("ECDHView.commonKey") + " = " + secretLargeA + " * " + shareLargeB + " = " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ keyLargeA + "\n\n"; //$NON-NLS-1$

			s += Messages.getString("ECDHView.BobParameters") + ":\n"; //$NON-NLS-1$ //$NON-NLS-2$
			s += Messages.getString("ECDHView.secretKey") + " = " + secretLargeB + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			s += Messages.getString("ECDHView.sharedKey") + " = " + shareLargeB + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			s += Messages.getString("ECDHView.commonKey") + " = " + secretLargeB + " * " + shareLargeA + " = " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ keyLargeB + "\n\n"; //$NON-NLS-1$
		} else {
			s = Messages.getString("ECDHView.logHeader") + "\n\n" + Messages.getString("ECDHView.curve") + ": " + curve //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "\n\n"; //$NON-NLS-1$

			s += Messages.getString("ECDHView.AliceParameters") + ":\n"; //$NON-NLS-1$ //$NON-NLS-2$
			s += Messages.getString("ECDHView.secretKey") + " = " + secretA + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			s += Messages.getString("ECDHView.sharedKey") + " = " + shareA.toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			s += Messages.getString("ECDHView.commonKey") + " = " + secretA + " * " + shareB + " = " + keyA + "\n\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

			s += Messages.getString("ECDHView.BobParameters") + ":\n"; //$NON-NLS-1$ //$NON-NLS-2$
			s += Messages.getString("ECDHView.secretKey") + " = " + secretB + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			s += Messages.getString("ECDHView.sharedKey") + " = " + shareB + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			s += Messages.getString("ECDHView.commonKey") + " = " + secretB + " * " + shareA + " = " + keyB + "\n\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return s;
	}

	private void saveToEditor(String s) {
		if (logFile == null) {
			logFile = new File(DirectoryService.getTempDir() + "ECDH results.txt"); //$NON-NLS-1$ //$NON-NLS-2$
			logFile.deleteOnExit();
		}

		saveToFile(s);

		IWorkbenchPage editorPage = view.getSite().getPage();

		IEditorReference[] er = editorPage.getEditorReferences();
		for (int i = 0; i < er.length; i++) {
			if (er[i].getName().equals("ECDH results.txt")) { //$NON-NLS-1$
				er[i].getEditor(false).getSite().getPage().closeEditor(er[i].getEditor(false), false);
			}
		}

		try {
			IPath location = new org.eclipse.core.runtime.Path(logFile.getAbsolutePath());
			editorPage.openEditor(new PathEditorInput(location), "org.jcryptool.editor.text.editor.JCTTextEditor"); //$NON-NLS-1$
		} catch (PartInitException e) {
			LogUtil.logError(ECDHPlugin.PLUGIN_ID, e);
		}
	}

	private void saveToFile(String s) {
		selectFileLocation();
		if (logFile != null) {
			try {
				String[] sa = s.split("\n"); //$NON-NLS-1$
				if (sa.length > 1 || !sa[0].equals("")) { //$NON-NLS-1$
					if (!logFile.exists())
						logFile.createNewFile();
					FileWriter fw = new FileWriter(logFile, true);
					BufferedWriter bw = new BufferedWriter(fw);
					for (int i = 0; i < sa.length; i++) {
						if (i < sa.length - 1 || (i == sa.length - 1 && !sa[i].equals(""))) { //$NON-NLS-1$
							bw.write(sa[i]);
							bw.newLine();
						}
					}
					bw.close();
					fw.close();
				}
			} catch (Exception e) {
				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell());
				messageBox.setText(Messages.getString("ECDHComposite.160")); //$NON-NLS-1$
				messageBox.setMessage(Messages.getString("ECDHComposite.161") + e.getMessage()); //$NON-NLS-1$
				messageBox.open();
			}
		}
	}

	private void selectFileLocation() {
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setFilterNames(new String[] { IConstants.TXT_FILTER_NAME, IConstants.ALL_FILTER_NAME });
		dialog.setFilterExtensions(new String[] { IConstants.TXT_FILTER_EXTENSION, IConstants.ALL_FILTER_EXTENSION });
		dialog.setFilterPath(DirectoryService.getUserHomeDir());
		dialog.setFileName("ECDH.txt"); //$NON-NLS-1$
		dialog.setOverwrite(true);
		String filename = dialog.open();
		if (filename == null) {
			logFile = null;
			return;
		} else
			logFile = new File(filename);
	}

	/**
	 *(Re)set the state of the plug-in.
	 *
	 * Multiple states are available which are specified by following constants
	 * 
	 * <ul>
	 * 	<li>{@code ECDHComposite.RESET_ALL}</li>
	 * <li>{@code ECDHComposite.RESET_PUBLIC_PARAMETERS}</li>
	 * <li>{@code ECDHComposite.RESET_SECRET_PARAMETERS}</li>
	 * </ul>
	 * @param state to reset to
	 */
	private void reset(int state) {
		switch (state) {
		case RESET_ALL: // complete reset
			curve = null;
			valueN = 0;
			generator = null;
			elements = null;

			textCurve.setText(""); //$NON-NLS-1$
			textGenerator.setText(""); //$NON-NLS-1$
			btnSetPublicParameters.setBackground(cRed);
			infoText.setText(Messages.getString("ECDHView.Step1"));
		case RESET_PUBLIC_PARAMETERS:// reset from Set public parameters button
			secretA = -1;
			secretB = -1;
			secretLargeA = null;
			secretLargeB = null;

			btnChooseSecrets.setEnabled(false);
			btnChooseSecrets.setBackground(cRed);
			btnSecretA.setEnabled(false);
			btnSecretA.setBackground(cRed);
			textSecretA.setText(""); //$NON-NLS-1$
			btnSecretB.setEnabled(false);
			btnSecretB.setBackground(cRed);
			textSecretB.setText(""); //$NON-NLS-1$
			btnCreateSharedKeys.setEnabled(false);		
			btnCalculateSharedA.setEnabled(false);
			btnCalculateSharedB.setEnabled(false);

			/*
			 * This part of the switch (RESET_PUBLIC_PARAMETERS) is called when the public
			 * parameters are changed. As we want a layout change (on textCurve and
			 * textGenerator) when choosing large or small curves, we layout here. The text
			 * fields are set empty beforehand to prevent layout to make them weird when
			 * they are filled with a long string.
			 */
			textCurve.setText("");
			textGenerator.setText("");
			layout();
		case RESET_SECRET_PARAMETERS: // this line is just for your information what will be reset
		default:
			shareA = null;
			shareB = null;
			keyA = null;
			keyB = null;
			shareLargeA = null;
			shareLargeB = null;
			keyLargeA = null;
			keyLargeB = null;

			canvasKey.setVisible(false);
			btnCreateSharedKeys.setBackground(cRed);
			btnCalculateSharedA.setBackground(cRed);
			textSharedA.setText(""); //$NON-NLS-1$
			btnCalculateSharedB.setBackground(cRed);
			textSharedB.setText(""); //$NON-NLS-1$
			btnExchangeKeys.setEnabled(false);
			btnExchangeKeys.setBackground(cRed);
			btnGenerateKey.setEnabled(false);
			btnGenerateKey.setBackground(cRed);
			btnCalculateKeyA.setEnabled(false);
			btnCalculateKeyA.setBackground(cRed);
			textCommonKeyA.setText(""); //$NON-NLS-1$
			btnCalculateKeyB.setEnabled(false);
			btnCalculateKeyB.setBackground(cRed);
			textCommonKeyB.setText(""); //$NON-NLS-1$
			ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
			Command command = commandService.getCommand(saveToEditorCommandId);
			command.setHandler(null);
			command = commandService.getCommand(saveToFileCommandId);
			command.setHandler(null);
		}
		groupMain.redraw();
	}
	
	private static String formatLargeCurve(String largeCurveString, int type) {
		String str = largeCurveString;
		String lines[] = str.split("\\r?\\n");
		String lineA, lineB, lineOrder;
		
		if (type == PublicParametersComposite.TYPE_FP) {
			lineA = lines[1].substring(4, lines[1].length() - 1);
			lineB = lines[2].substring(4, lines[2].length() - 1);
			lineOrder = lines[3].trim().substring(14, lines[3].length() - 1);
			
			lineA = ECDHUtil.spaceString(lineA.toUpperCase());
			lineB = ECDHUtil.spaceString(lineB.toUpperCase());
			lineOrder = ECDHUtil.spaceString(lineOrder.toUpperCase());
		}
		else if (type == PublicParametersComposite.TYPE_FM) {			
			lineA = lines[1].substring(4, lines[1].length() - 1);
			lineB = lines[2].substring(4, lines[2].length() - 1);
			lineOrder = lines[3].trim().substring(14, lines[3].length() - 1);
			
			lineA = lineA.toUpperCase();
			lineB = lineB.toUpperCase();
			lineOrder = lineA.toUpperCase();
		}
		else
			return "";
		
		str = lines[0] + "\na = " + lineA + "\nb = " + lineB + "\nfield order = " + lineOrder;  
		
		str = str.replace("<sup>", "^"); //$NON-NLS-1$ //$NON-NLS-2$
		str = str.replace("</sup>", "");  //$NON-NLS-1$ //$NON-NLS-2$
		return str;
	}
	
	private static String formatLargeGenerator(String x, String y, int type) {
		String str;
		x = x.trim().toUpperCase();
		y = y.trim().toUpperCase();
		
		if (type == PublicParametersComposite.TYPE_FP) {
			x = ECDHUtil.spaceString(x);
			y = ECDHUtil.spaceString(y);
		}
		
		str = "(" + x + ", " + y + ")";  
		return str;
	}
	
	/**
	 * Calculates the Key A (for Alice) and sets the text {@code textCommonKeyA}
	 */
	private void generateKeyA() {
		if (large) {
			keyLargeA = multiplyLargePoint(shareLargeB, secretLargeA);
			textCommonKeyA.setText(keyLargeA.getXAffin().toString());
		} else {
			keyA = curve.multiplyPoint(shareB, secretA);
			if (keyA == null)
				keyA = generator;
			textCommonKeyA.setText(keyA.toString());
		}
	}
	
	/**
	 * Calculates the Key B (for Bob) and sets the text {@code textCommonKeyB}
	 */
	private void generateKeyB() {
		if (large) {
			keyLargeB = multiplyLargePoint(shareLargeA, secretLargeB);
			textCommonKeyB.setText(keyLargeB.getXAffin().toString());
		} else {
			keyB = curve.multiplyPoint(shareA, secretB);
			if (keyB == null)
				keyB = generator;
			textCommonKeyB.setText(keyB.toString());
		}
	}
	
	private void generateBothCommonKeys() {
		generateKeyA();
		generateKeyB();
		btnCalculateKeyA.setBackground(cGreen);
		btnCalculateKeyB.setBackground(cGreen);
		btnGenerateKey.setBackground(cGreen);
		showKeyImageUpdateText();
	}
	
	private void showKeyImageUpdateText() {
		canvasKey.setVisible(true);
		canvasKey.redraw();
		infoText.setText(Messages.getString("ECDHView.Step1") +
						 Messages.getString("ECDHView.Step2") +
						 Messages.getString("ECDHView.Step3") +
						 Messages.getString("ECDHView.Step4") +
						 Messages.getString("ECDHView.Step5") +
						 Messages.getString("ECDHView.messageSucces"));
	}
	
	/**
	 * Enable the buttons {@code btnCalculateSharedA} and {@code btnCalculateSharedA} needed in next step
	 */
	private void enableCalculateSharedButtons() {
		btnCalculateSharedA.setEnabled(true);
		btnCalculateSharedB.setEnabled(true);
		infoText.setText(Messages.getString("ECDHView.Step1") +
		         		 Messages.getString("ECDHView.Step2") +
		         		 Messages.getString("ECDHView.Step3"));
	}


	private Point multiplyLargePoint(Point p, FlexiBigInt m) {
		if (m.doubleValue() == 0)
			return null;
		if (m.doubleValue() == 1)
			return p;
		if (m.mod(new FlexiBigInt("2")).doubleValue() == 0) //$NON-NLS-1$
			return multiplyLargePoint(p, m.divide(new FlexiBigInt("2"))).multiplyBy2(); //$NON-NLS-1$
		else
			return p.add(multiplyLargePoint(p, m.subtract(new FlexiBigInt("1")))); //$NON-NLS-1$
	}

	class Animate extends Thread {
		public void run() {
			if (showAnimation) {
				GC gc = new GC(canvasExchange);
				Image original = new Image(canvasExchange.getDisplay(), canvasExchange.getBounds().width,
						canvasExchange.getBounds().height);
				gc.copyArea(original, 0, canvasExchange.getBounds().height / 2);

				int canvasExchangeWidth = canvasExchange.getBounds().width;
				int canvasExchangeHeight = canvasExchange.getBounds().height;

				// x und y bestimmen die startposition von den 1 und 0
				int x = -70;
				int y = 10;
				String msg;

				if (large) {
					msg = shareLargeA.getXAffin().toString(2).substring(0, 4) + " " //$NON-NLS-1$
							+ shareLargeA.getYAffin().toString(2).substring(0, 4);
				} else {
					if (curve.getType() == ECFm.ECFm)
						msg = intToBitString(shareA.x == elements.length ? 0 : elements[shareA.x], 5) + " " //$NON-NLS-1$
								+ intToBitString(shareA.y == elements.length ? 0 : elements[shareA.y], 5);
					else
						msg = intToBitString(shareA.x, 5) + " " + intToBitString(shareA.y, 5); //$NON-NLS-1$
				}

				for (int i = 0; i < 136; i++) {
					// Hier wird der Verlauf der 1 und 0 bestimmt
					if (i < 12) {
						x += canvasExchangeWidth / (4 * 12);
					} else if (i < 36) {
						// Die -72 machen 18 Pixel aus, da 72/(4*12)=1,5*12=18
						y += (canvasExchangeHeight) / (4 * 24);
					} else if (i < 60) {
						x += canvasExchangeWidth / (2 * 24);
						y += (canvasExchangeHeight - 72) / (4 * 24);
					} else if (i < 68) {
						x += canvasExchangeWidth / (4 * 8);
					} else if (i == 68) {
						y = 10;
						x = canvasExchangeWidth - 70;
						if (large) {
							msg = shareLargeB.getXAffin().toString(2).substring(0, 4) + " " //$NON-NLS-1$
									+ shareLargeB.getYAffin().toString(2).substring(0, 4);
						} else {
							if (curve.getType() == ECFm.ECFm)
								msg = intToBitString(shareB.x == elements.length ? 0 : elements[shareB.x], 5) + " " //$NON-NLS-1$
										+ intToBitString(shareB.y == elements.length ? 0 : elements[shareB.y], 5);
							else
								msg = intToBitString(shareB.x, 5) + " " + intToBitString(shareB.y, 5); //$NON-NLS-1$
						}
					} else if (i < 80) {
						x -= canvasExchangeWidth / (4 * 12);
					} else if (i < 104) {
						// Die -72 machen 18 Pixel aus, da 72/(4*12)=1,5*12=18
						y += (canvasExchangeHeight) / (4 * 24);
					} else if (i < 128) {
						x -= canvasExchangeWidth / (2 * 24);
						y += (canvasExchangeHeight - 72) / (4 * 24);
					} else if (i < 136) {
						x -= canvasExchangeWidth / (4 * 8);
					}

					Image im = new Image(canvasExchange.getDisplay(), original, SWT.IMAGE_COPY);
					GC gcI = new GC(im);
					gcI.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
					gcI.setFont(FontService.getHeaderFont());
					gcI.drawText(msg, x, y, true);
					gc.drawImage(im, 0, (canvasExchange.getBounds().height / 2));

					try {
						sleep(50);
					} catch (InterruptedException ex) {
						LogUtil.logError(ECDHPlugin.PLUGIN_ID, ex);
					}
					gcI.dispose();
				}
				gc.dispose();
				canvasExchange.redraw();
			}
		}
	}
}
