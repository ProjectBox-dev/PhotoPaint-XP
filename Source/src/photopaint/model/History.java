package photopaint.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Snapshot-based undo/redo with a memory budget. */
public class History {

    public static class Entry {
        public final String name;
        private final Document.Snapshot state;
        private final long bytes;

        private Entry(String name, Document.Snapshot state, long bytes) {
            this.name = name;
            this.state = state;
            this.bytes = bytes;
        }
    }

    private static final long MAX_BYTES = 220L * 1024 * 1024;

    private final List<Entry> entries = new ArrayList<>();
    private int index = -1;
    private long totalBytes;

    public void reset(String name, Document doc) {
        entries.clear();
        totalBytes = 0;
        index = -1;
        push(name, doc);
    }

    public void push(String name, Document doc) {
        while (entries.size() > index + 1) {
            Entry removed = entries.remove(entries.size() - 1);
            totalBytes -= removed.bytes;
        }
        Document.Snapshot snap = Document.Snapshot.of(doc);
        long bytes = snap.bytes();
        entries.add(new Entry(name, snap, bytes));
        totalBytes += bytes;
        index = entries.size() - 1;
        trim();
    }

    private void trim() {
        while (totalBytes > MAX_BYTES && entries.size() > 1 && index > 0) {
            Entry removed = entries.remove(0);
            totalBytes -= removed.bytes;
            index--;
        }
    }

    public boolean canUndo() {
        return index > 0;
    }

    public boolean canRedo() {
        return index >= 0 && index < entries.size() - 1;
    }

    public String undoName() {
        return canUndo() ? entries.get(index).name : null;
    }

    public String redoName() {
        return canRedo() ? entries.get(index + 1).name : null;
    }

    public void undo(Document doc) {
        if (!canUndo()) return;
        index--;
        entries.get(index).state.restoreTo(doc);
    }

    public void redo(Document doc) {
        if (!canRedo()) return;
        index++;
        entries.get(index).state.restoreTo(doc);
    }

    public void jump(int i, Document doc) {
        if (i < 0 || i >= entries.size() || i == index) return;
        index = i;
        entries.get(index).state.restoreTo(doc);
    }

    public int getIndex() {
        return index;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}
