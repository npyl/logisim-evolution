/*******************************************************************************
 * This file is part of logisim-evolution.
 *
 *   logisim-evolution is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   logisim-evolution is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with logisim-evolution.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Original code by Carl Burch (http://www.cburch.com), 2011.
 *   Subsequent modifications by :
 *     + Haute École Spécialisée Bernoise
 *       http://www.bfh.ch
 *     + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *       http://hepia.hesge.ch/
 *     + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *       http://www.heig-vd.ch/
 *   The project is currently maintained by :
 *     + REDS Institute - HEIG-VD
 *       Yverdon-les-Bains, Switzerland
 *       http://reds.heig-vd.ch
 *******************************************************************************/

package com.cburch.logisim.std.hdl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.cburch.hdl.HdlFile;
import com.cburch.hdl.HdlModel;
import com.cburch.hdl.HdlModelListener;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.FileUtil;
import com.cburch.logisim.util.JFileChoosers;
import com.cburch.logisim.util.JInputDialog;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;

public class HdlContentEditor extends JDialog implements JInputDialog {

	private class EditorListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent de) {

		}

		@Override
		public void insertUpdate(DocumentEvent de) {
			validate.setEnabled(!editor.getText().equals(model.getContent()));
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
			validate.setEnabled(!editor.getText().equals(model.getContent()));
		}

	}

	private class FrameListener extends WindowAdapter implements
			ActionListener, LocaleListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();

			if (source == open) {
				if (!editor.getText().equals(model.getContent()))
					if (!confirmImport(HdlContentEditor.this))
						return;

				JFileChooser chooser = JFileChoosers
						.createAt(getDefaultImportFile(null));
				chooser.setDialogTitle(Strings.get("openButton"));
				int choice = chooser.showOpenDialog(HdlContentEditor.this);
				if (choice == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					try {
						HdlFile.open(f, HdlContentEditor.this);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(HdlContentEditor.this,
								e.getMessage(),
								Strings.get("hexOpenErrorTitle"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			if (source == save) {
				JFileChooser chooser = JFileChoosers
						.createSelected(getDefaultExportFile(null));
				chooser.setDialogTitle(Strings.get("saveButton"));
				int choice = chooser.showSaveDialog(HdlContentEditor.this);
				if (choice == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					try {
						HdlFile.save(f, HdlContentEditor.this);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(HdlContentEditor.this,
								e.getMessage(),
								Strings.get("hexSaveErrorTitle"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			if (source == validate) {
				model.setContent(editor.getText());
			}
			if (source == close) {
				close();
			}
		}

		@Override
		public void localeChanged() {
			setTitle(Strings.get("hdlFrameTitle"));
			open.setText(Strings.get("openButton"));
			save.setText(Strings.get("saveButton"));
			validate.setText(Strings.get("validateButton"));
			close.setText(Strings.get("closeButton"));
		}

		@Override
		public void windowClosing(WindowEvent e) {
			close();
		}

	}

	private class ModelListener implements HdlModelListener {

		@Override
		public void contentSet(HdlModel source) {
			validate.setEnabled(false);
		}

	}

	public static boolean confirmImport(Component parent) {
		String[] options = { Strings.get("importOption"),
				Strings.get("cancelOption") };
		return JOptionPane.showOptionDialog(parent,
				Strings.get("importMessage"), Strings.get("importTitle"), 0,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]) == 0;
	}

	private static final long serialVersionUID = 1L;
	private static final int ROWS = 40;

	private static final int COLUMNS = 100;

	private static final String EXPORT_DIR = "hdl_export";

	private FrameListener frameListener = new FrameListener();
	private ModelListener modelListener = new ModelListener();
	private EditorListener editorListener = new EditorListener();

	private RSyntaxTextArea editor;
	private HdlModel model;
	private Project project;

	private JButton open = new JButton();
	private JButton save = new JButton();
	private JButton validate = new JButton();
	private JButton close = new JButton();

	public HdlContentEditor(Dialog parent, Project proj, HdlModel model) {
		super(parent, Strings.get("hdlFrameTitle"), true);
		configure(proj, model);
	}

	public HdlContentEditor(Frame parent, Project proj, HdlModel model) {
		super(parent, Strings.get("hdlFrameTitle"), true);
		configure(proj, model);
	}

	private void close() {
		if (editor.getText().equals(model.getContent())) {
			dispose();
			return;
		}

		if (model.setContent(editor.getText())) {
			dispose();
			return;
		}

		Object[] options = { Strings.get("confirmCloseYes"),
				Strings.get("confirmCloseNo"),
				Strings.get("confirmCloseBackup") };
		int n = JOptionPane.showOptionDialog(this,
				Strings.get("confirmCloseMessage"),
				Strings.get("confirmCloseTitle"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);

		switch (n) {
		case JOptionPane.YES_OPTION:
			dispose();
			break;
		case JOptionPane.CANCEL_OPTION:
			save.doClick();
			dispose();
			break;
		}
	}

	private void configure(Project proj, HdlModel model) {
		this.project = proj;
		this.model = model;
		this.model.addHdlModelListener(modelListener);
		this.addWindowListener(frameListener);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(open);
		buttonsPanel.add(save);
		buttonsPanel.add(validate);
		buttonsPanel.add(close);
		open.addActionListener(frameListener);
		save.addActionListener(frameListener);
		close.addActionListener(frameListener);
		validate.addActionListener(frameListener);

		editor = new RSyntaxTextArea(ROWS, COLUMNS);
		editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_VHDL);
		editor.setCodeFoldingEnabled(true);
		editor.setAntiAliasingEnabled(true);
		editor.getDocument().addDocumentListener(editorListener);

		RTextScrollPane sp = new RTextScrollPane(editor);
		sp.setFoldIndicatorEnabled(true);

		add(sp, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		LocaleManager.addLocaleListener(frameListener);
		frameListener.localeChanged();
		pack();

		Dimension size = getSize();
		Dimension screen = getToolkit().getScreenSize();
		if (size.width > screen.width || size.height > screen.height) {
			size.width = Math.min(size.width, screen.width);
			size.height = Math.min(size.height, screen.height);
			setSize(size);
		}
	}

	private File getDefaultExportFile(File defaultFile) {
		File projectFile = project.getLogisimFile().getLoader().getMainFile();
		if (projectFile == null) {
			if (defaultFile == null)
				return new File(model.getName() + ".vhd");
			return defaultFile;
		}

		File compFolder;
		try {
			compFolder = new File(FileUtil.correctPath(projectFile
					.getParentFile().getCanonicalPath()) + EXPORT_DIR);
			if (!compFolder.exists()
					|| (compFolder.exists() && !compFolder.isDirectory()))
				compFolder.mkdir();
			return new File(FileUtil.correctPath(compFolder.getCanonicalPath())
					+ model.getName() + ".vhd");
		} catch (IOException ex) {
			return defaultFile;
		}
	}

	private File getDefaultImportFile(File defaultFile) {
		File projectFile = project.getLogisimFile().getLoader().getMainFile();
		if (projectFile == null)
			return defaultFile;

		File compFolder;
		try {
			compFolder = new File(FileUtil.correctPath(projectFile
					.getParentFile().getCanonicalPath()) + EXPORT_DIR);
			if (!compFolder.exists()
					|| (compFolder.exists() && !compFolder.isDirectory()))
				compFolder.mkdir();
			return new File(FileUtil.correctPath(compFolder.getCanonicalPath()));
		} catch (IOException ex) {
			return defaultFile;
		}
	}

	public String getText() {
		return editor.getText();
	}

	@Override
	public Object getValue() {
		return model;
	}

	public void setText(String content) {
		editor.setText(content);
		editor.discardAllEdits();
	}

	@Override
	public void setValue(Object value) {
		model = (HdlModel) value;
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			editor.setText(model.getContent());
			editor.discardAllEdits();
		}

		super.setVisible(b);
	}

}
