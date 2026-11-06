package photopaint.ui;

import photopaint.Theme;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Right side dock with Layers / History / Colour tabs. */
public class DockPanel extends JPanel {

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
    private final List<Tab> tabs = new ArrayList<>();
    private final ButtonGroup group = new ButtonGroup();

    public DockPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(248, 400));
        setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 4));
        tabBar.setOpaque(false);
        tabBar.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        add(tabBar, BorderLayout.NORTH);
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x8A8678)),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        add(cardPanel, BorderLayout.CENTER);
    }

    public void addTab(String title, JComponent content) {
        Tab tab = new Tab(title);
        tab.addActionListener(e -> {
            cards.show(cardPanel, title);
            tab.setSelected(true);
            for (Tab t : tabs) if (t != tab) t.setSelected(false);
            tab.repaint();
        });
        group.add(tab);
        tabBar.add(tab);
        tabs.add(tab);
        content.setOpaque(true);
        content.setBackground(Theme.WINDOW);
        cardPanel.add(content, title);
        if (tabs.size() == 1) tab.setSelected(true);
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg.create();
        Theme.vertical(g, 0, 0, getWidth(), getHeight(), new Color(0xECE9D8), new Color(0xE0DCCC));
        g.dispose();
    }

    private static class Tab extends JToggleButton {
        Tab(String text) {
            super(text);
            setUI(new BasicToggleButtonUI());
            setFont(Theme.ui());
            setForeground(Theme.TEXT);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(4, 11, 4, 11));
            setPreferredSize(new Dimension(getPreferredSize().width + 6, 24));
        }

        @Override
        protected void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg.create();
            Theme.aa(g);
            int w = getWidth();
            int h = getHeight();
            if (isSelected()) {
                g.setColor(Theme.WINDOW);
                g.fillRect(1, 3, w - 2, h - 3);
                g.setColor(new Color(0x8A8678));
                g.drawLine(1, 3, 1, h);
                g.drawLine(w - 2, 3, w - 2, h);
                g.drawLine(2, 2, w - 3, 2);
                g.setColor(new Color(0x2E6BC8));
                g.fillRect(3, 1, w - 6, 2);
            } else {
                g.setColor(new Color(0xE7E3D4));
                g.fillRect(1, 4, w - 2, h - 4);
                g.setColor(new Color(0xC3BFB0));
                g.drawLine(1, 4, 1, h);
                g.drawLine(w - 2, 4, w - 2, h);
                g.drawLine(2, 3, w - 3, 3);
                g.setColor(new Color(0x9C988A));
                g.drawLine(2, 2, w - 3, 2);
            }
            g.dispose();
            super.paintComponent(gg);
        }
    }
}
