package com.kartoflane.ftl.errorchecker.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import net.vhati.ftldat.FTLDat.FTLPack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.located.LocatedElement;

import com.kartoflane.common.search.RegexFilter;
import com.kartoflane.common.search.RegexFilter.TextPos;
import com.kartoflane.common.search.SearchFilter;
import com.kartoflane.common.search.SearchListener;
import com.kartoflane.common.search.SearchPerformer;
import com.kartoflane.common.swing.SwingUIUtils;
import com.kartoflane.common.utils.AlphanumComparator;
import com.kartoflane.common.utils.IOUtils;
import com.kartoflane.common.utils.Predicate;
import com.kartoflane.ftl.errorchecker.core.CheckerConfig;
import com.kartoflane.ftl.errorchecker.core.CheckerContext;
import com.kartoflane.ftl.errorchecker.core.CheckerInitThread;
import com.kartoflane.ftl.errorchecker.core.Database;
import com.kartoflane.ftl.errorchecker.core.ErrorInstance;
import com.kartoflane.ftl.errorchecker.core.FilePointer;
import com.kartoflane.ftl.errorchecker.core.IssueSeverity;
import com.kartoflane.ftl.errorchecker.processing.ProcessingThread;
import com.kartoflane.ftl.errorchecker.processing.ValidationManager;
import com.kartoflane.ftl.errorchecker.ui.components.CheckBoxIcon;
import com.kartoflane.ftl.errorchecker.ui.components.ErrorTableModel;
import com.kartoflane.ftl.errorchecker.ui.components.ErrorTableRenderer;
import com.kartoflane.ftl.errorchecker.ui.components.FileTreeCellRenderer;
import com.kartoflane.ftl.errorchecker.ui.components.FileTreeModel;
import com.kartoflane.ftl.errorchecker.ui.components.FileTreeModel.FileTreeNode;
import com.kartoflane.ftl.errorchecker.ui.components.FileViewerPanel;
import com.kartoflane.ftl.errorchecker.ui.components.IconStates;


@SuppressWarnings("serial")
public class CheckerFrame extends JFrame
		implements ActionListener, MouseListener, KeyListener, SearchListener, SearchPerformer {

	private static final Logger log = LogManager.getLogger(CheckerFrame.class);

	// @formatter:off
	private static final int DEFAULT_FRAME_WIDTH =				900;
	private static final int DEFAULT_FRAME_HEIGHT =				600;
	private static final double DEFAULT_FRAME_VSPLIT_FACTOR =	0.4;
	private static final double DEFAULT_FRAME_HSPLIT_FACTOR =	0.5;

	private static final int TABLE_LINE_COL_WIDTH =				65;
	private static final int TABLE_MESSAGE_COL_WIDTH =			100;
	// @formatter:on

	private final CheckerConfig config;
	private final CheckerContext context;

	private final Lock checkerLock = new ReentrantLock();
	private final Condition scanEndedCond = checkerLock.newCondition();

	private boolean working = false;

	private FilePointer currentFile;
	private int searchResultIndex;
	private List<FileTextPos> searchResults;

	private FTLPack data;
	private FTLPack resource;

	// UI components
	private final JSplitPane outerSplitPane;
	private final JSplitPane innerSplitPane;

	private final JTable errorTable;
	private final JTree fileTree;
	private final FileViewerPanel viewerPanel;

	private final SearchDialog searchDialog;

	private final JCheckBox btnError;
	private final JCheckBox btnWarn;
	private final JCheckBox btnInfo;

	private final JMenuItem mntmPreferences;
	private final JMenuItem mntmExit;
	private final JMenuItem mntmAbout;

	private final JPanel toolPanel;
	private final JToolBar toolBar;
	private final JButton btnRescan;
	private final JButton btnParse;
	private final JButton btnSearch;

	public CheckerFrame(CheckerConfig config, CheckerContext context) {
		super("FTL Error Checker");

		setSize(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.config = config;
		this.context = context;

		searchDialog = new SearchDialog(this, context);
		searchDialog.addSearchListener(this);
		searchDialog.setSearchPerformer(this);

		// TODO keybinds file
		SwingUIUtils.installOperation(this, JComponent.WHEN_IN_FOCUSED_WINDOW,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
				getClass().getCanonicalName() + ":SEARCH_DIALOG",
				new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						searchDialog.setVisible(true);
					}
				});

		// ===============================================================================

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmPreferences = new JMenuItem("Preferences");
		mntmPreferences.addActionListener(this);

		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(this);

		mnFile.add(mntmPreferences);
		mnFile.addSeparator();
		mnFile.add(mntmExit);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(this);

		mnHelp.add(mntmAbout);

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// ===============================================================================
		// Toolbar

		toolPanel = new JPanel();
		add(toolPanel);
		toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		toolBar = new JToolBar();
		toolPanel.add(toolBar);
		toolBar.setFloatable(false);

		btnRescan = new JButton();
		btnRescan.setIcon(new ImageIcon(context.getCache().checkOutImage(this, "cpath://assets/refresh.png")));
		btnRescan.setToolTipText("<html>Refresh the list of files and reload the database.<br/>Use this if the archvies or scripts have been modified.</html>");
		btnRescan.addActionListener(this);

		btnParse = new JButton();
		btnParse.setIcon(new ImageIcon(context.getCache().checkOutImage(this, "cpath://assets/parse.png")));
		btnParse.setToolTipText("Parse and validate all files.");
		btnParse.addActionListener(this);

		btnSearch = new JButton();
		btnSearch.setIcon(new ImageIcon(context.getCache().checkOutImage(this, "cpath://assets/search.png")));
		btnSearch.setToolTipText("Search");
		btnSearch.addActionListener(this);

		toolBar.add(btnRescan);
		toolBar.add(btnParse);
		toolBar.addSeparator(new Dimension(10, 20));
		toolBar.add(btnSearch);

		// ===============================================================================
		// Split panes

		outerSplitPane = new JSplitPane();
		outerSplitPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		outerSplitPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		outerSplitPane.setContinuousLayout(true);
		outerSplitPane.setResizeWeight(DEFAULT_FRAME_VSPLIT_FACTOR);
		outerSplitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
				UIManager.getDefaults().getColor("SplitPane.shadow")));
		add(outerSplitPane);

		innerSplitPane = new JSplitPane();
		innerSplitPane.setContinuousLayout(true);
		innerSplitPane.setResizeWeight(DEFAULT_FRAME_HSPLIT_FACTOR);
		innerSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		innerSplitPane.setBorder(BorderFactory.createEmptyBorder());

		outerSplitPane.setLeftComponent(innerSplitPane);

		// ===============================================================================
		// Files tree

		fileTree = new JTree(new FileTreeModel());
		final FileTreeCellRenderer renderer = new FileTreeCellRenderer(context.getCache());
		fileTree.setCellRenderer(renderer);
		fileTree.setAutoscrolls(true);
		fileTree.addMouseListener(this);
		fileTree.addKeyListener(this);

		JScrollPane treeScrollPane = new JScrollPane();
		treeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		treeScrollPane.setViewportView(fileTree);

		innerSplitPane.setLeftComponent(treeScrollPane);

		// ===============================================================================
		// Errors table

		ErrorTableModel errorModel = new ErrorTableModel();
		errorTable = new JTable(errorModel);
		errorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		errorTable.setFillsViewportHeight(true);
		errorTable.getTableHeader().setReorderingAllowed(false);
		errorTable.setAutoCreateRowSorter(true);
		// Disable default 'Enter' behaviour
		errorTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");

		ErrorTableRenderer errorRenderer = new ErrorTableRenderer(context);
		errorTable.getColumnModel().getColumn(0).setCellRenderer(errorRenderer);
		errorTable.getColumnModel().getColumn(1).setCellRenderer(errorRenderer);
		errorTable.addMouseListener(this);
		errorTable.addKeyListener(this);

		TableColumn col = errorTable.getColumnModel().getColumn(0);
		col.setResizable(false);
		col.setPreferredWidth(TABLE_LINE_COL_WIDTH);
		col.setMinWidth(TABLE_LINE_COL_WIDTH);
		col.setMaxWidth(TABLE_LINE_COL_WIDTH);

		col = errorTable.getColumnModel().getColumn(1);
		col.setResizable(false);
		col.setMinWidth(TABLE_MESSAGE_COL_WIDTH);

		errorTable.getRowSorter().toggleSortOrder(0);

		JScrollPane tableScrollPane = new JScrollPane();
		tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tableScrollPane.setViewportView(errorTable);

		innerSplitPane.setRightComponent(tableScrollPane);

		// ===============================================================================
		// Text viewer panels

		viewerPanel = new FileViewerPanel();
		updateViewer();
		outerSplitPane.setRightComponent(viewerPanel);

		JPanel statusBar = new JPanel();
		statusBar.setAlignmentY(Component.TOP_ALIGNMENT);
		statusBar.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		add(statusBar);

		outerSplitPane.setDividerLocation((int) (DEFAULT_FRAME_WIDTH * DEFAULT_FRAME_HSPLIT_FACTOR));
		innerSplitPane.setDividerLocation((int) (DEFAULT_FRAME_HEIGHT * DEFAULT_FRAME_VSPLIT_FACTOR));

		// ===============================================================================
		// Checkbuttons & icons below the table

		btnError = new JCheckBox("");
		btnError.addActionListener(this);
		ImageIcon icon = new ImageIcon(context.getCache().checkOutImage(this, "cpath://assets/error.png"));
		btnError.setIcon(new CheckBoxIcon(btnError, icon));
		btnError.setSelected(true);
		statusBar.add(btnError);

		btnWarn = new JCheckBox("");
		btnWarn.addActionListener(this);
		icon = new ImageIcon(context.getCache().checkOutImage(this, "cpath://assets/warning.png"));
		btnWarn.setIcon(new CheckBoxIcon(btnWarn, icon));
		btnWarn.setSelected(true);
		statusBar.add(btnWarn);

		btnInfo = new JCheckBox("");
		btnInfo.addActionListener(this);
		icon = new ImageIcon(context.getCache().checkOutImage(this, "cpath://assets/info.png"));
		btnInfo.setIcon(new CheckBoxIcon(btnInfo, icon));
		btnInfo.setSelected(true);
		statusBar.add(btnInfo);

		setGeometryFromConfig();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// dispose() was called.

				renderer.dispose();

				CheckerConfig config = CheckerFrame.this.config;

				if (config.getPropertyAsBoolean(CheckerConfig.REM_GEOMETRY)) {
					if (CheckerFrame.this.getExtendedState() == JFrame.NORMAL) {
						Rectangle managerBounds = CheckerFrame.this.getBounds();
						int innerDivLoc = innerSplitPane.getDividerLocation();
						int outerDivLoc = outerSplitPane.getDividerLocation();
						String geometry = String.format("x,%d;y,%d;w,%d;h,%d;idiv,%d;odiv,%d", managerBounds.x, managerBounds.y, managerBounds.width, managerBounds.height, innerDivLoc, outerDivLoc);
						config.setProperty(CheckerConfig.GEOMETRY, geometry);
					}
				}

				try {
					config.writeConfig();
				}
				catch (IOException f) {
					log.error(String.format("Error writing config to \"%s\".", config.getConfigFile().getName()), f);
				}

				System.gc();
				System.exit(0);
			}
		});
	}

	public void init() {
		CheckerInitThread initThread = new CheckerInitThread(this, config);
		initThread.setDaemon(true);
		initThread.setPriority(Thread.MIN_PRIORITY);
		initThread.start();
	}

	private void setGeometryFromConfig() {
		String geometry = config.getProperty(CheckerConfig.GEOMETRY);

		if (geometry != null) {
			int[] xywh = new int[4];
			int innerDivLoc = -1;
			int outerDivLoc = -1;
			Matcher m = Pattern.compile("([^;,]+),(\\d+)").matcher(geometry);

			while (m.find()) {
				if (m.group(1).equals("x"))
					xywh[0] = Integer.parseInt(m.group(2));
				else if (m.group(1).equals("y"))
					xywh[1] = Integer.parseInt(m.group(2));
				else if (m.group(1).equals("w"))
					xywh[2] = Integer.parseInt(m.group(2));
				else if (m.group(1).equals("h"))
					xywh[3] = Integer.parseInt(m.group(2));
				else if (m.group(1).equals("idiv"))
					innerDivLoc = Integer.parseInt(m.group(2));
				else if (m.group(1).equals("odiv"))
					outerDivLoc = Integer.parseInt(m.group(2));
			}

			boolean badGeometry = false;
			for (int n : xywh) {
				if (n <= 0) {
					badGeometry = true;
					break;
				}
			}
			if (!badGeometry && innerDivLoc > 0 && outerDivLoc > 0) {
				Rectangle newBounds = new Rectangle(xywh[0], xywh[1], xywh[2], xywh[3]);
				CheckerFrame.this.setBounds(newBounds);
				innerSplitPane.setDividerLocation(innerDivLoc);
				outerSplitPane.setDividerLocation(outerDivLoc);
			}
		}
	}

	/**
	 * @return a lock for synchronizing thread operations.
	 */
	public Lock getLock() {
		return checkerLock;
	}

	/**
	 * Call getLock().lock() first.
	 * Loop while isWorking() is true, calling this condition's await().
	 * Finally, call getLock().unlock().
	 * 
	 * @return a condition that will signal when the data.dat archive has been scanned.
	 * 
	 */
	public Condition getScanEndedCondition() {
		return scanEndedCond;
	}

	/**
	 * @eturn true if the data.dat archive is currently being scanned. (thread-safe)
	 */
	public boolean isWorking() {
		checkerLock.lock();
		try {
			return working;
		}
		finally {
			checkerLock.unlock();
		}
	}

	/**
	 * Clears and synchronises the file tree with contents of data.dat archive,
	 * and loads contents of the database.
	 */
	public void rescanData() {
		checkerLock.lock();
		try {
			if (working)
				return;
			working = true;
			btnRescan.setEnabled(!working);
			btnParse.setEnabled(!working);
			btnSearch.setEnabled(!working);
		}
		finally {
			checkerLock.unlock();
		}

		clearFileTree();
		clearErrorTable();

		if (searchResults != null)
			searchResults.clear();
		currentFile = null;

		Database db = context.getDatabase();
		db.clear();

		ValidationManager vm = context.getValidationManager();
		vm.clear();

		try {
			if (data != null)
				data.close();
			if (resource != null)
				resource.close();
			data = null;
			resource = null;
			System.gc();

			log.info("Loading game archives...");
			String datsDir = config.getProperty(CheckerConfig.FTL_DAT_PATH);
			File dataFile = new File(datsDir + "/data.dat");
			data = new FTLPack(dataFile, "r");
			File resFile = new File(datsDir + "/resource.dat");
			resource = new FTLPack(resFile, "r");

			// Create pointers to files in data.dat
			List<FilePointer> dataFileList = new ArrayList<FilePointer>();
			List<String> pathList = data.list();
			Collections.sort(pathList, new AlphanumComparator());

			Predicate<String> pathFilter = new Predicate<String>() {
				public boolean accept(String s) {
					return !s.endsWith("~") && !s.contains(".git/");
				}
			};

			for (String innerPath : pathList) {
				try {
					if (pathFilter.accept(innerPath))
						dataFileList.add(new FilePointer(data, innerPath));
				}
				catch (IOException e) {
					log.warn(e.getMessage());
				}
			}

			// Create pointers to files in resource.dat
			List<FilePointer> resFileList = new ArrayList<FilePointer>();

			pathList = resource.list();
			// Sort the list initially so that items are added in alphanumerical order later.
			Collections.sort(pathList, new AlphanumComparator());

			for (String innerPath : pathList) {
				try {
					if (pathFilter.accept(innerPath))
						resFileList.add(new FilePointer(resource, innerPath));
				}
				catch (IOException e) {
					log.warn(e.getMessage());
				}
			}

			db.init(dataFileList, resFileList);

			// Populate the tree
			FileTreeModel model = (FileTreeModel) fileTree.getModel();
			for (FilePointer fi : dataFileList) {
				model.addNode(fi);
			}
			fileTree.expandRow(0);

			log.info("Archives loaded successfully.");
		}
		catch (IOException e) {
			log.error("Error while loading dat archives:", e);
			SwingUIUtils.showErrorDialog("An error has occured while loading .dat archives:\n" + e.getMessage());
			return;
		}

		try {
			log.info("Loading database...");

			db.loadIgnoredNames();
			db.loadErrorDescriptions();
			db.loadScripts(context.getScriptManager());
			
			db.debugAssertErrorDescriptions();

			log.info("Database loaded successfully.");
		}
		catch (IOException e) {
			log.error("Error while loading database files.", e);
		}
		catch (Exception e) {
			log.error("An unknown exception occured while loading the database.", e);
		}

		taskEnded();
	}

	public void parseFiles() {
		checkerLock.lock();
		try {
			if (working)
				return;
			working = true;
			btnRescan.setEnabled(!working);
			btnParse.setEnabled(!working);
			btnSearch.setEnabled(!working);
		}
		finally {
			checkerLock.unlock();
		}

		ValidationManager vm = context.getValidationManager();
		vm.clear();
		vm.init();

		TaskProgressDialog progressDlg = new TaskProgressDialog(this, false);
		ProcessingThread processThread = new ProcessingThread(config, context, progressDlg);
		progressDlg.setTaskThread(processThread);
		processThread.start();
		progressDlg.setVisible(true);

		clearErrorTable();
		updateTreeNodes();

		taskEnded();
	}

	public void taskEnded() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				checkerLock.lock();
				try {
					working = false;
					btnRescan.setEnabled(!working);
					btnParse.setEnabled(!working);
					btnSearch.setEnabled(!working);
					scanEndedCond.signalAll();
				}
				finally {
					checkerLock.unlock();
				}
			}
		};

		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			SwingUtilities.invokeLater(r);
	}

	public void updateViewer() {
		viewerPanel.setTabSize(config.getPropertyAsInt(CheckerConfig.VIEWER_TAB_SIZE));
		viewerPanel.setWrapText(config.getPropertyAsBoolean(CheckerConfig.VIEWER_WRAP_TEXT));
		viewerPanel.setFontStyle(config.getProperty(CheckerConfig.VIEWER_FONT_STYLE, CheckerConfig.FONT_STYLE_DEFAULT),
				config.getPropertyAsInt(CheckerConfig.VIEWER_FONT_SIZE));
	}

	/*
	 * ===================================
	 * NOTE: Tree and table control methods
	 * ===================================
	 */

	public void updateTreeNodes() {
		ValidationManager vm = context.getValidationManager();
		if (!vm.isInitialized()) {
			return;
		}

		boolean hideEmpty = config.getPropertyAsBoolean(CheckerConfig.HIDE_EMPTY_FILES);

		FileTreeModel ftm = (FileTreeModel) fileTree.getModel();
		ftm.clear();

		for (FilePointer fi : context.getDatabase().listDataFiles()) {
			IconStates is = vm.getHighestSeverity(fi);

			if ((!hideEmpty || is != IconStates.EMPTY) && isEnabled(is)) {
				FileTreeNode node = ((FileTreeModel) fileTree.getModel()).addNode(fi);
				node.setIssueState(is);
			}
		}

		ftm.reload();
	}

	public void openFile(FilePointer fi) {
		if (currentFile == fi)
			return;

		try {
			FileTreeModel ftm = (FileTreeModel) fileTree.getModel();
			int row = ftm.getRowOf(fi);
			fileTree.setSelectionRow(row);
			fileTree.scrollRowToVisible(row);

			viewerPanel.viewFile(fi);
			populateTable(fi);

			currentFile = fi;
			searchDialog.paramsChanged();
		}
		catch (FileNotFoundException e) {
			log.warn(String.format("The innerPath '%s' doesn't exist in pack %s", fi.getInnerPath(), fi.getPack()));
		}
		catch (IOException e) {
		}
	}

	private void populateTable(FilePointer fi) {
		if (fi != null && context.getValidationManager().isInitialized()) {
			List<ErrorInstance> list = context.getValidationManager().listIssues(fi.getInnerPath());

			ErrorTableModel model = (ErrorTableModel) errorTable.getModel();
			model.clear();

			for (ErrorInstance ii : list) {
				if (isSeverityEnabled(ii.getSeverity()))
					model.addItem(ii);
			}
		}
	}

	private void clearFileTree() {
		((FileTreeModel) fileTree.getModel()).clear();
	}

	private void clearErrorTable() {
		((ErrorTableModel) errorTable.getModel()).clear();
	}

	private boolean isEnabled(IconStates is) {
		switch (is) {
			case ERROR:
				return btnError.isSelected();
			case WARNING:
				return btnWarn.isSelected();
			case INFO:
				return btnInfo.isSelected();
			default:
				return true;
		}
	}

	public boolean isSeverityEnabled(IssueSeverity severity) {
		switch (severity) {
			case ERROR:
				return btnError.isSelected();
			case WARN:
				return btnWarn.isSelected();
			case INFO:
				return btnInfo.isSelected();
			default:
				return true;
		}
	}

	/*
	 * ==========================
	 * NOTE: Search functionality
	 * ==========================
	 */

	@SuppressWarnings("unchecked")
	private List<FileTextPos> findOccurences(FilePointer fi, SearchFilter<?> filter) throws IOException {
		if (fi == null)
			return null;

		List<FileTextPos> result = new ArrayList<FileTextPos>();

		Class<?> filterFor = (Class<?>) filter.getSearchParameter(SearchDialog.SPK_FILTER_FOR);

		if (filterFor.equals(String.class)) {
			RegexFilter stringFilter = (RegexFilter) filter;

			InputStream is = null;
			try {
				is = fi.getInputStream();
				String contents = IOUtils.readStream(is, fi.getInnerPath());

				while (stringFilter.accept(contents)) {
					result.add(new FileTextPos(fi, stringFilter.getResultPos()));
				}
			}
			finally {
				if (is != null)
					is.close();
			}
		}
		else if (filterFor.equals(Element.class) && fi.isXML()) {
			if (context.getDatabase().isIgnoredName(fi.getPack().getName(), fi.getName()))
				return result;

			SearchFilter<Element> elementFilter = (SearchFilter<Element>) filter;

			InputStream is = null;
			try {
				is = fi.getInputStream();
				String contents = IOUtils.readStream(is, fi.getInnerPath());

				LocatedElement root = context.getValidationManager().getRootXMLElement(fi.getInnerPath());
				if (root == null) {
					throw new IllegalArgumentException("No root found for: " + fi.getInnerPath());
				}
				findXMLRecursive(contents, fi, root, elementFilter, result);
			}
			finally {
				if (is != null)
					is.close();
			}
		}

		return result;
	}

	private void findXMLRecursive(String contents, FilePointer fi, LocatedElement current, SearchFilter<Element> filter, List<FileTextPos> list) {
		if (filter == null || filter.accept(current)) {
			list.add(new FileTextPos(fi, current.getLine(), -1, true));
		}

		for (Element e : current.getChildren()) {
			findXMLRecursive(contents, fi, (LocatedElement) e, filter, list);
		}
	}

	@Override
	public void filterConstructed(SearchFilter<?> filter) {
		searchResultIndex = -1;
		if (searchResults != null)
			searchResults.clear();

		boolean broad = (Boolean) filter.getSearchParameter(SearchDialog.SPK_BROAD_SEARCH);

		try {
			if (broad) {
				searchResults = new ArrayList<FileTextPos>();
				for (FilePointer fi : context.getDatabase().listDataFiles()) {
					searchResults.addAll(findOccurences(fi, filter));
				}
			}
			else {
				searchResults = findOccurences(currentFile, filter);
			}
		}
		catch (IOException e) {
			log.error("An error occurred while performing search.", e);
			SwingUIUtils.showErrorDialog("An error occurred while performing search: " + e.getMessage());
		}
	}

	@Override
	public boolean hasAnyResults() {
		return searchResults != null && searchResults.size() > 0;
	}

	@Override
	public void prevResultRequested() {
		searchResultIndex--;
		if (searchResultIndex < 0) {
			searchResultIndex += searchResults.size();
			Toolkit.getDefaultToolkit().beep();
			searchDialog.setStatus("Wrapped search.");
		}

		FileTextPos pos = searchResults.get(searchResultIndex);
		openFile(pos.file);

		if (pos.line)
			viewerPanel.showLine(pos.start);
		else
			viewerPanel.showPos(pos.start, pos.end);
	}

	@Override
	public void nextResultRequested() {
		searchResultIndex++;
		if (searchResultIndex >= searchResults.size()) {
			searchResultIndex = 0;
			Toolkit.getDefaultToolkit().beep();
			searchDialog.setStatus("Wrapped search.");
		}

		FileTextPos pos = searchResults.get(searchResultIndex);
		openFile(pos.file);

		if (pos.line)
			viewerPanel.showLine(pos.start);
		else
			viewerPanel.showPos(pos.start, pos.end);
	}

	/** Dumb little class to store information about the location of search results. */
	private static class FileTextPos extends TextPos {

		public final FilePointer file;
		/**
		 * If this is true, then 'start' field indicates the line number; 'end' field is unused.
		 */
		public final boolean line;

		public FileTextPos(FilePointer file, int start, int end, boolean line) {
			super(start, end);
			this.line = line;
			this.file = file;
		}

		public FileTextPos(FilePointer file, TextPos pos) {
			super(pos.start, pos.end);
			this.line = false;
			this.file = file;
		}
	}

	/*
	 * ======================
	 * NOTE: Listener methods
	 * ======================
	 */

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == mntmPreferences) {
			if (!mntmPreferences.isEnabled())
				return;

			CheckerConfigDialog configFrame = new CheckerConfigDialog(this, config);
			configFrame.setLocationRelativeTo(null);
			configFrame.setVisible(true);
		}
		else if (source == btnRescan) {
			if (!btnRescan.isEnabled())
				return;

			rescanData();
		}
		else if (source == btnParse) {
			if (!btnParse.isEnabled())
				return;

			parseFiles();
		}
		else if (source == btnSearch) {
			if (!btnSearch.isEnabled())
				return;

			searchDialog.setVisible(true);
		}
		else if (source == mntmExit) {
			if (!mntmExit.isEnabled())
				return;

			dispose();
		}
		else if (source == mntmAbout) {
			if (!mntmAbout.isEnabled())
				return;

			AboutDialog aboutDialog = new AboutDialog(this);
			aboutDialog.setLocationRelativeTo(null);
			aboutDialog.setVisible(true);
		}
		else if (source == btnError) {
			if (!btnError.isEnabled())
				return;

			updateTreeNodes();
			populateTable(currentFile);
		}
		else if (source == btnWarn) {
			if (!btnWarn.isEnabled())
				return;

			updateTreeNodes();
			populateTable(currentFile);
		}
		else if (source == btnInfo) {
			if (!btnInfo.isEnabled())
				return;

			updateTreeNodes();
			populateTable(currentFile);
		}
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();

		if (source == fileTree) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				TreePath selectionPath = fileTree.getSelectionPath();
				if (fileTree.getSelectionCount() > 0 && selectionPath.getParentPath() != null) {
					String name = selectionPath.getLastPathComponent().toString();
					for (FilePointer fi : context.getDatabase().listDataFiles()) {
						if (fi.getName().equals(name)) {
							openFile(fi);
							break;
						}
					}
				}
			}
		}
		else if (source == errorTable) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				if (errorTable.getSelectedRowCount() > 0) {
					int row = errorTable.getSelectedRow();
					ErrorTableModel model = (ErrorTableModel) errorTable.getModel();
					ErrorInstance ii = model.getItem(errorTable.getRowSorter().convertRowIndexToModel(row));
					viewerPanel.showLine(ii.getLine());
				}
			}
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void keyTyped(KeyEvent e) {
		Object source = e.getSource();
		char keyChar = e.getKeyChar();

		if (source == fileTree) {
			if (keyChar == KeyEvent.VK_ENTER) {
				FilePointer file = null;

				TreePath selectionPath = fileTree.getSelectionPath();
				if (fileTree.getSelectionCount() > 0 && selectionPath.getParentPath() != null) {
					String name = selectionPath.getLastPathComponent().toString();
					for (FilePointer fi : context.getDatabase().listDataFiles()) {
						if (fi.getName().equals(name)) {
							file = fi;
							break;
						}
					}
				}

				if (file != null) {
					openFile(file);
				}
			}
		}
		else if (source == errorTable) {
			if (keyChar == KeyEvent.VK_ENTER) {
				if (errorTable.getSelectedRowCount() > 0) {
					int row = errorTable.getSelectedRow();
					ErrorTableModel model = (ErrorTableModel) errorTable.getModel();
					ErrorInstance ii = model.getItem(errorTable.getRowSorter().convertRowIndexToModel(row));
					viewerPanel.showLine(ii.getLine());
				}
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}
}
