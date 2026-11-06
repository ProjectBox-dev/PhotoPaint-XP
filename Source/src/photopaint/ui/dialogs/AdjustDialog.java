package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;
import photopaint.filters.Filters;
import photopaint.tools.ToolPaint;
import photopaint.ui.XpDialog;
import photopaint.ui.XpSliderUI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.image.BufferedImage;

/** Brightness/contrast or hue/saturation with a live canvas preview. */
public class AdjustDialog extends XpDialog {

    private final Editor ed;
    private final boolean hue;
    private final JSlider a;
    private final JSlider b;
    private final JLabel av = new JLabel();
    private final JLabel bv = new JLabel();
    private boolean applied;

    public AdjustDialog(Window owner, Editor ed, boolean hue) {
        super(owner, hue ? I18n.t("Оттенок / насыщенность") : I18n.t("Яркость / контраст"));
        this.ed = ed;
        this.hue = hue;
        a = new JSlider(hue ? -180 : -100, hue ? 180 : 100, 0);
        b = new JSlider(-100, 100, 0);
        a.setUI(new XpSliderUI(a));
        b.setUI(new XpSliderUI(b));
        a.setPreferredSize(new Dimension(210, 20));
        b.setPreferredSize(new Dimension(210, 20));

        JPanel form = new JPanel(new GridLayout(2, 1, 0, 8));
        form.setOpaque(false);
        form.add(row(hue ? I18n.t("Сдвиг оттенка:") : I18n.t("Яркость:"), a, av));
        form.add(row(hue ? I18n.t("Насыщенность:") : I18n.t("Контраст:"), b, bv));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(form, BorderLayout.NORTH);
        JLabel note = new JLabel(I18n.t("Предпросмотр на холсте. ОК — применить."));
        note.setFont(Theme.uiSmall());
        note.setForeground(new java.awt.Color(0x555555));
        JPanel noteWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        noteWrap.setOpaque(false);
        noteWrap.add(note);
        wrap.add(noteWrap, BorderLayout.SOUTH);
        wrap.setPreferredSize(new Dimension(340, 92));
        body().add(wrap, BorderLayout.CENTER);

        javax.swing.event.ChangeListener cl = e -> {
            av.setText(String.valueOf(a.getValue()));
            bv.setText(String.valueOf(b.getValue()));
            preview();
        };
        a.addChangeListener(cl);
        b.addChangeListener(cl);
        av.setText("0");
        bv.setText("0");

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(I18n.t("ОК"), true, this::apply);
    }

    private JPanel row(String name, JSlider slider, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(name);
        l.setFont(Theme.ui());
        l.setPreferredSize(new Dimension(110, 17));
        p.add(l, BorderLayout.WEST);
        p.add(slider, BorderLayout.CENTER);
        value.setFont(Theme.uiSmall());
        value.setPreferredSize(new Dimension(34, 16));
        p.add(value, BorderLayout.EAST);
        return p;
    }

    private void preview() {
        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        BufferedImage copy = ToolPaint.copy(img);
        if (hue) Filters.hueSaturation(copy, ed.doc.clipShape(), a.getValue(), b.getValue());
        else Filters.brightnessContrast(copy, ed.doc.clipShape(), a.getValue(), b.getValue());
        ed.doc.setPreview(ed.doc.active, copy);
        ed.fire("canvas");
    }

    private void apply() {
        BufferedImage img = ed.doc.activeImage();
        if (img != null) {
            if (hue) Filters.hueSaturation(img, ed.doc.clipShape(), a.getValue(), b.getValue());
            else Filters.brightnessContrast(img, ed.doc.clipShape(), a.getValue(), b.getValue());
            applied = true;
            ed.doc.clearPreview();
            ed.commit(hue ? I18n.t("Оттенок/насыщенность") : I18n.t("Яркость/контраст"));
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
