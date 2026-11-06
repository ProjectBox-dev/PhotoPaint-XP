package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;
import photopaint.tools.ToolPaint;
import photopaint.ui.XpDialog;
import photopaint.ui.XpSliderUI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Shape;
import java.awt.Window;
import java.awt.image.BufferedImage;

/** One slider filter dialog with live preview. */
public class FilterDialog extends XpDialog {

    public interface PixelFilter {
        void apply(BufferedImage img, Shape clip, int value);
    }

    private final Editor ed;
    private final PixelFilter filter;
    private final String actionName;
    private final JSlider slider;
    private final JLabel valueLabel = new JLabel();
    private boolean applied;

    public FilterDialog(Window owner, Editor ed, String title, String label, int min, int max,
                        int initial, String actionName, PixelFilter filter) {
        super(owner, title);
        this.ed = ed;
        this.filter = filter;
        this.actionName = actionName;

        slider = new JSlider(min, max, Math.max(min, Math.min(max, initial)));
        slider.setUI(new XpSliderUI(slider));
        slider.setPreferredSize(new Dimension(210, 20));
        valueLabel.setText(String.valueOf(slider.getValue()));
        valueLabel.setFont(Theme.uiSmall());
        valueLabel.setPreferredSize(new Dimension(38, 16));

        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.ui());
        l.setPreferredSize(new Dimension(110, 17));
        row.add(l, BorderLayout.WEST);
        row.add(slider, BorderLayout.CENTER);
        row.add(valueLabel, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(row, BorderLayout.NORTH);
        JLabel note = new JLabel(I18n.t("Предпросмотр на холсте. ОК — применить."));
        note.setFont(Theme.uiSmall());
        note.setForeground(new java.awt.Color(0x555555));
        JPanel noteWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        noteWrap.setOpaque(false);
        noteWrap.add(note);
        wrap.add(noteWrap, BorderLayout.SOUTH);
        wrap.setPreferredSize(new Dimension(350, 60));
        body().add(wrap, BorderLayout.CENTER);

        slider.addChangeListener(e -> {
            valueLabel.setText(String.valueOf(slider.getValue()));
            preview();
        });

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(I18n.t("ОК"), true, this::apply);
    }

    private void preview() {
        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        BufferedImage copy = ToolPaint.copy(img);
        filter.apply(copy, ed.doc.clipShape(), slider.getValue());
        ed.doc.setPreview(ed.doc.active, copy);
        ed.fire("canvas");
    }

    private void apply() {
        BufferedImage img = ed.doc.activeImage();
        if (img != null) {
            filter.apply(img, ed.doc.clipShape(), slider.getValue());
            applied = true;
            ed.doc.clearPreview();
            ed.commit(actionName);
        }
        dispose();
    }

    @Override
    public void dispose() {
        if (!applied) {
            ed.doc.clearPreview();
            ed.fire("canvas");
        }
        super.dispose();
    }

    public void showDialog() {
        showCentered();
    }
}
