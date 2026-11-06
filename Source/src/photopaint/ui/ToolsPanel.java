package photopaint.ui;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;
import photopaint.tools.Tool;
import photopaint.ui.dialogs.ColorPickerDialog;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Vertical tool box with two columns of tools and the foreground/background colours. */
public class ToolsPanel extends JPanel {

    private final Editor ed;
    private final Map<Tool, JToggleButton> buttons = new LinkedHashMap<>();
    private final FgBgSwatch swatch;
    private boolean updating;

    public ToolsPanel(Editor ed) {
        this.ed = ed;
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());
        setPreferredSize(new Dimension(64, 300));
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        JPanel grid = new JPanel(new java.awt.GridLayout(0, 2, 1, 1));
        grid.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (Tool tool : ed.tools) {
            JToggleButton b = new JToggleButton(Icons.tool(tool.id));
            b.setUI(new XpToolButtonUI());
            // Keep keyboard focus on the canvas so single-letter tool shortcuts work.
            b.setFocusable(false);
            b.setToolTipText("<html><b>" + I18n.t(tool.name) + "</b> (" + tool.shortcut + ")<br>"
                    + I18n.t(tool.hint) + "</html>");
            b.setSelected(tool == ed.tool);
            b.addActionListener(e -> {
                if (updating) return;
                if (b.isSelected()) ed.setTool(tool);
                else b.setSelected(true);
            });
            group.add(b);
            grid.add(b);
            buttons.put(tool, b);
        }
        add(grid, java.awt.BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 4));
        bottom.setOpaque(false);
        swatch = new FgBgSwatch();
        swatch.setPreferredSize(new Dimension(52, 42));
        swatch.setToolTipText(I18n.t("Основной и фоновый цвет"));
        bottom.add(swatch);

        JPanel mini = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 0));
        mini.setOpaque(false);
        JButton swap = miniButton("swap", I18n.t("Поменять цвета (X)"));
        swap.addActionListener(e -> {
            ed.colors.swap();
            ed.fire("colors");
        });
        JButton reset = miniButton("resetColors", I18n.t("Чёрный и белый"));
        reset.addActionListener(e -> {
            ed.colors.reset();
            ed.fire("colors");
        });
        mini.add(swap);
        mini.add(reset);
        bottom.add(mini);
        add(bottom, java.awt.BorderLayout.CENTER);
    }

    private JButton miniButton(String icon, String tip) {
        JButton b = new JButton(Icons.icon(icon, 14));
        b.setUI(new XpButtonUI());
        b.setPreferredSize(new Dimension(22, 20));
        b.setMargin(new java.awt.Insets(0, 0, 0, 0));
        b.setToolTipText(tip);
        return b;
    }

    public void refreshTool() {
        updating = true;
        JToggleButton b = buttons.get(ed.tool);
        if (b != null) b.setSelected(true);
        updating = false;
    }

    public void refreshColors() {
        swatch.repaint();
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg.create();
        g.setPaint(new GradientPaint(0, 0, new Color(0xF8F7EF), getWidth(), 0,
                new Color(0xE2DECE)));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(0xFFFFFF));
        g.drawLine(0, 0, 0, getHeight());
        g.setColor(new Color(0xC8C4B4));
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        g.dispose();
    }

    private class FgBgSwatch extends JComponent {
        FgBgSwatch() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    boolean fgZone = e.getX() < 28 && e.getY() < 28;
                    Color initial = fgZone ? ed.colors.getForeground() : ed.colors.getBackground();
                    ColorPickerDialog dialog = new ColorPickerDialog(
                            SwingUtilities.getWindowAncestor(ToolsPanel.this), initial);
                    Color chosen = dialog.showDialog();
                    if (chosen != null) {
                        if (fgZone) ed.setForeground(chosen);
                        else ed.setBackground(chosen);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg.create();
            Theme.aa(g);
            g.setColor(new Color(0x8A8678));
            g.fillRect(16, 16, 30, 22);
            g.setColor(ed.colors.getBackground());
            g.fillRect(17, 17, 28, 20);
            g.setColor(Color.WHITE);
            g.drawLine(17, 17, 44, 17);
            g.setColor(new Color(0x5A5A5A));
            g.drawRect(16, 16, 30, 22);

            g.setColor(new Color(0x8A8678));
            g.fillRect(2, 2, 30, 22);
            g.setColor(ed.colors.getForeground());
            g.fillRect(3, 3, 28, 20);
            g.setColor(new Color(255, 255, 255, 120));
            g.setStroke(new BasicStroke(1f));
            g.drawRect(3, 3, 28, 20);
            g.setColor(new Color(0x5A5A5A));
            g.drawRect(2, 2, 30, 22);
            g.dispose();
        }
    }
}
