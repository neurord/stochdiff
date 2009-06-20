package org.catacomb.druid.swing;


import org.catacomb.icon.IconLoader;
import org.catacomb.interlish.structure.Labelled;
import org.catacomb.interlish.structure.Progressed;
import org.catacomb.interlish.structure.StateProcess;


import java.awt.*;

import javax.swing.*;




public class DListProgressRenderer extends JPanel implements DListCellRenderer {

    static final long serialVersionUID = 1001;

    JLabel nameLabel;

    JLabel statusLabel;

    JLabel progressLabel;

    JProgressBar progressBar;
    JPanel rtPanel;


    Icon newIcon;
    Icon readyIcon;
    Icon runningIcon;
    Icon doneIcon;
    Icon stoppedIcon;
    Icon errorIcon;

    Color csel = new Color(0xe6, 0xe6, 0Xdc);


    public DListProgressRenderer() {
        setLayout(new GridLayout(3, 1, 6, 2));
        //     setLayout(new FlowLayout(FlowLayout.LEFT));


        Font plainfont = new Font("sansserif", Font.PLAIN, 12);


        nameLabel = new JLabel();
//      nameLabel.setOpaque(true);
        nameLabel.setFont(plainfont);


        statusLabel = new JLabel("");
//      statusLabel.setOpaque(false);
        statusLabel.setFont(plainfont);

        progressLabel = new JLabel("             ", null, JLabel.TRAILING);
//      progressLabel.setOpaque(true);
        progressLabel.setFont(plainfont);

        newIcon = IconLoader.createImageIcon("statusNew.gif");
        doneIcon = IconLoader.createImageIcon("statusDone.gif");
        readyIcon = IconLoader.createImageIcon("statusReady.gif");
        runningIcon = IconLoader.createImageIcon("statusRunning.gif");
        stoppedIcon = IconLoader.createImageIcon("statusStopped.gif");
        errorIcon = IconLoader.createImageIcon("statusErr.gif");


        statusLabel.setIcon(doneIcon);


        progressBar = new JProgressBar(0, 100);
        progressBar.setOpaque(false);
        progressBar.setPreferredSize(new Dimension(140, 8));
        progressBar.setBorderPainted(true);

        // progressBar.setForeground(new Color(0x80ff80));


        Color borderC = new Color(0xa0a0a0);


        progressBar.setBorder(BorderFactory.createCompoundBorder(
                                  BorderFactory.createEmptyBorder(4, 4, 4, 4),
                                  BorderFactory.createLineBorder(borderC)));

        rtPanel = new JPanel();
        rtPanel.setBackground(Color.white);
        rtPanel.setLayout(new BorderLayout(4, 4));
        rtPanel.add("East", statusLabel);
        rtPanel.add("Center", progressBar);




        add(nameLabel);
        add(rtPanel);
        add(progressLabel);

    }



    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {


        String s = "";
        if (value instanceof Labelled) {
            s = ((Labelled)value).getLabel();
        } else {
            s = value.toString();
        }

        int istat = StateProcess.RAW;
        if (value instanceof StateProcess) {
            istat = ((StateProcess)value).getProcessState();
        }

        double fprog = 0.0;
        String tprog = "";
        if (value instanceof Progressed) {
            fprog = ((Progressed)value).getProgress();
            tprog = ((Progressed)value).getProgressDescription();
        }



        nameLabel.setText(s);

        Color cbg = (isSelected ? csel : list.getBackground());
        Color cfg = (isSelected ? list.getSelectionForeground() : list.getForeground());

        setBackground(cbg);

//      nameLabel.setBackground(cbg);
        rtPanel.setBackground(cbg);

        nameLabel.setForeground(cfg);

//       progressLabel.setBackground(cbg);
        progressBar.setBackground(cbg);

        switch (istat) {
        case StateProcess.RAW:
            statusLabel.setIcon(newIcon);
            break;

        case StateProcess.READY:
            statusLabel.setIcon(readyIcon);
            break;

        case StateProcess.RUNNING:
            statusLabel.setIcon(runningIcon);
            break;

        case StateProcess.ERROR:
            statusLabel.setIcon(errorIcon);
            break;

        case StateProcess.FINISHED:
            statusLabel.setIcon(doneIcon);
            break;

        case StateProcess.PAUSED:
            statusLabel.setIcon(stoppedIcon);
            break;

        }


        progressBar.setValue((int)(100 * fprog));

        progressLabel.setText(tprog);

        /*
         * setEnabled(list.isEnabled()); setFont(list.getFont()); setOpaque(true);
         */
        return this;
    }


}
