package mosaic.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class SettingsPanel extends JPanel{
	// Retry with SpringLayout?
	/*
	 * gridWidth			-	NumField
	 * gridHeight			-	NumField
	 * targetMulti			-	NumField
	 * keepRatio			-	CheckBox
	 * overlapImages		-	CheckBox
	 * alphaThreshhold		-	Spinner (0-255)
	 *
	 * adaptionCount		-	NumField
	 * adaptionStep			-	Spinner (1.01 - 2.0)
	 * gridErrorThresh		-	Spinner (0.01 - 1.0)
	 * 
	 * 			---PERFORMANCE---
	 * inputWorkerLimit		-	NumField
	 * targetWorkerLimit	-	NumField
	 * matchWorkerLimit		-	NumField
	 * placeWorkerLimit		-	NumField
	 * 
	 */
	private final static String[] guiText = {
			"Grid Width:",
			"Grid Height:",
			"Magnification:",
			"Keep Image Ratio:",
			"Overlap Images:",
			"Alpha Threshhold:",
			"Number of Adaption Steps (max):",
			"Size of Adaption Step:",
			"Grid Error Threshhold:",
			"Input Worker Limit:",
			"Target Worker Limit:",
			"Match Worker Limit:",
			"Place Worker Limit:",
			"Settings"
	};
	
	private JPanel topPanel, gridPanel, 
	gridWidthPanel, gridHeightPanel, targetMultiPanel,
	adaptionCountPanel, adaptionStepPanel, inputWorkerPanel, 
	targetWorkerPanel, matchWorkerPanel, placeWorkerPanel,
	gridErrorPanel, alphaThreshPanel, keepRatioPanel, 
	overlapPanel;
	private JLabel titleLabel;
	private JTextField gridWidthField, gridHeightField, targetMultiField, 
	adaptionCountField, inputWorkerField, targetWorkerField, matchWorkerField,
	placeWorkerField;
	private JSpinner adaptionStepSpinner, gridErrorSpinner, alphaThreshSpinner;
	private JCheckBox keepRatioCheck, overlapCheck;
	
	private JLabel gridWidthLabel, gridHeightLabel, targetMultiLabel,
	keepRatioLabel, overlapLabel, alphaThreshLabel, adaptionCountLabel,
	adaptionStepLabel, gridErrorLabel, inputWorkerLabel, targetWorkerLabel,
	matchWorkerLabel, placeWorkerLabel;
	
	public SettingsPanel(){
		//Initiate
		gridPanel = new JPanel();
		topPanel = new JPanel();
		gridWidthPanel = new JPanel();
		gridHeightPanel = new JPanel();
		targetMultiPanel = new JPanel();
		adaptionCountPanel = new JPanel();
		adaptionStepPanel = new JPanel();
		inputWorkerPanel = new JPanel();
		targetWorkerPanel = new JPanel();
		matchWorkerPanel = new JPanel();
		placeWorkerPanel = new JPanel();
		gridErrorPanel = new JPanel();
		alphaThreshPanel = new JPanel();
		keepRatioPanel = new JPanel();
		overlapPanel = new JPanel();
		
		gridWidthField = new JTextField();
		gridHeightField = new JTextField();
		targetMultiField = new JTextField();
		adaptionCountField = new JTextField();
		inputWorkerField = new JTextField();
		targetWorkerField = new JTextField();
		matchWorkerField = new JTextField();
		placeWorkerField = new JTextField();
		
		adaptionStepSpinner = new JSpinner();
		gridErrorSpinner = new JSpinner();
		alphaThreshSpinner = new JSpinner();
		
		keepRatioCheck = new JCheckBox();
		overlapCheck = new JCheckBox();
		
		gridWidthLabel = new JLabel(guiText[0]);
		gridHeightLabel = new JLabel(guiText[1]);
		targetMultiLabel = new JLabel(guiText[2]);
		keepRatioLabel = new JLabel(guiText[3]);
		overlapLabel = new JLabel(guiText[4]);
		alphaThreshLabel = new JLabel(guiText[5]);
		adaptionCountLabel = new JLabel(guiText[6]);
		adaptionStepLabel = new JLabel(guiText[7]);
		gridErrorLabel = new JLabel(guiText[8]);
		inputWorkerLabel = new JLabel(guiText[9]);
		targetWorkerLabel = new JLabel(guiText[10]);
		matchWorkerLabel = new JLabel(guiText[11]);
		placeWorkerLabel = new JLabel(guiText[12]);
		titleLabel = new JLabel(guiText[13]);
		// End Initiate
		
		//Config Component
		titleLabel.setFont(titleLabel.getFont().deriveFont(40f));
		
		Dimension lineSize = new Dimension(100, 40);
		gridWidthField.setMaximumSize(lineSize);
		gridHeightField.setMaximumSize(lineSize);
		targetMultiField.setMaximumSize(lineSize);
		adaptionCountField.setMaximumSize(lineSize);
		inputWorkerField.setMaximumSize(lineSize);
		targetWorkerField.setMaximumSize(lineSize);
		matchWorkerField.setMaximumSize(lineSize);
		placeWorkerField.setMaximumSize(lineSize);
		
		adaptionStepSpinner.setMaximumSize(lineSize);
		gridErrorSpinner.setMaximumSize(lineSize);
		alphaThreshSpinner.setMaximumSize(lineSize);
		
		keepRatioCheck.setMaximumSize(lineSize);
		overlapCheck.setMaximumSize(lineSize);
		//End Config Components
		
		// Add to Panels
		add(topPanel, BorderLayout.WEST);
		add(gridPanel, BorderLayout.WEST);

		topPanel.add(titleLabel);
		
		gridWidthPanel.add(gridWidthLabel);
		gridWidthPanel.add(gridWidthField);
		
		gridHeightPanel.add(gridHeightLabel);
		gridHeightPanel.add(gridHeightField);
		
		targetMultiPanel.add(targetMultiLabel);
		targetMultiPanel.add(targetMultiField);
		
		keepRatioPanel.add(keepRatioLabel);
		keepRatioPanel.add(keepRatioCheck);
		
		overlapPanel.add(overlapLabel);
		overlapPanel.add(overlapCheck);
		
		alphaThreshPanel.add(alphaThreshLabel);
		alphaThreshPanel.add(alphaThreshSpinner);
		
		adaptionCountPanel.add(adaptionCountLabel);
		adaptionCountPanel.add(adaptionCountField);
		
		adaptionStepPanel.add(adaptionStepLabel);
		adaptionStepPanel.add(adaptionStepSpinner);
		
		gridErrorPanel.add(gridErrorLabel);
		gridErrorPanel.add(gridErrorSpinner);
		
		inputWorkerPanel.add(inputWorkerLabel);
		inputWorkerPanel.add(inputWorkerField);
		
		targetWorkerPanel.add(targetWorkerLabel);
		targetWorkerPanel.add(targetWorkerField);
		
		targetMultiPanel.add(targetMultiLabel);
		targetMultiPanel.add(targetMultiField);
		
		matchWorkerPanel.add(matchWorkerLabel);
		matchWorkerPanel.add(matchWorkerField);
		
		placeWorkerPanel.add(placeWorkerLabel);
		placeWorkerPanel.add(placeWorkerField);
		
		gridPanel.add(gridWidthPanel);
		gridPanel.add(gridHeightPanel);
		gridPanel.add(targetMultiPanel);
		gridPanel.add(overlapPanel);
		gridPanel.add(keepRatioPanel);
		gridPanel.add(alphaThreshPanel);
		gridPanel.add(adaptionCountPanel);
		gridPanel.add(adaptionStepPanel);
		gridPanel.add(inputWorkerPanel);
		gridPanel.add(targetWorkerPanel);
		gridPanel.add(matchWorkerPanel);
		gridPanel.add(placeWorkerPanel);
		gridPanel.add(gridErrorPanel);
		//End Add to Panels
		
		//Setup Layouts
		gridWidthPanel.setLayout(new BoxLayout(gridWidthPanel, BoxLayout.LINE_AXIS));
		gridHeightPanel.setLayout(new BoxLayout(gridHeightPanel, BoxLayout.LINE_AXIS));
		targetMultiPanel.setLayout(new BoxLayout(targetMultiPanel, BoxLayout.LINE_AXIS));
		keepRatioPanel.setLayout(new BoxLayout(keepRatioPanel, BoxLayout.LINE_AXIS));
		overlapPanel.setLayout(new BoxLayout(overlapPanel, BoxLayout.LINE_AXIS));
		alphaThreshPanel.setLayout(new BoxLayout(alphaThreshPanel, BoxLayout.LINE_AXIS));
		adaptionCountPanel.setLayout(new BoxLayout(adaptionCountPanel, BoxLayout.LINE_AXIS));
		adaptionStepPanel.setLayout(new BoxLayout(adaptionStepPanel, BoxLayout.LINE_AXIS));
		gridErrorPanel.setLayout(new BoxLayout(gridErrorPanel, BoxLayout.LINE_AXIS));
		inputWorkerPanel.setLayout(new BoxLayout(inputWorkerPanel, BoxLayout.LINE_AXIS));
		targetWorkerPanel.setLayout(new BoxLayout(targetWorkerPanel, BoxLayout.LINE_AXIS));
		targetMultiPanel.setLayout(new BoxLayout(targetMultiPanel, BoxLayout.LINE_AXIS));
		matchWorkerPanel.setLayout(new BoxLayout(matchWorkerPanel, BoxLayout.LINE_AXIS));
		placeWorkerPanel.setLayout(new BoxLayout(placeWorkerPanel, BoxLayout.LINE_AXIS));
		
		gridPanel.setLayout(new GridLayout(13, 1));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		//End Setup Layouts
	}
}
