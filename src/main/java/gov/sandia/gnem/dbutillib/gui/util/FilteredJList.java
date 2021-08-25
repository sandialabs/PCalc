package gov.sandia.gnem.dbutillib.gui.util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * This class implements a JList that a filter box capability. It is from the book Swing Hacks and the code was obtained
 * from: http://oreilly.com/catalog/9780596009076/. It has been modified very slightly in order to allow constructors to
 * be handed the initial list data.
 */
@SuppressWarnings("serial")
// Use this when we move to Java 1.7
// public class FilteredJList extends JList<Object>
public class FilteredJList extends JList {
    private FilterField filterField;

    public FilteredJList() {
        super();
        setModel(new FilterModel());
        this.filterField = new FilterField();
    }

    public FilteredJList(Object[] items) {
        super();
        setModel(new FilterModel(items));
        this.filterField = new FilterField();
    }

    public FilteredJList(Vector<?> items) {
        super();
        setModel(new FilterModel(items));
        this.filterField = new FilterField();
    }

    @Override
    public void setModel(ListModel m) {
        if (!(m instanceof FilterModel))
            throw new IllegalArgumentException();
        super.setModel(m);
    }

    @Override
    public void setListData(Object[] listData) {
        setModel(new FilterModel(listData));
    }

    @Override
    public void setListData(Vector listData) {
        setModel(new FilterModel(listData));
    }

    public void addItem(Object o) {
        ((FilterModel) getModel()).addElement(o);
    }

    public JTextField getFilterField() {
        return this.filterField;
    }

    public void setFilterFieldDimension(Dimension d) {

        this.filterField.setPreferredSize(d);
    }

    // // test filter list
    // public static void main(String[] args)
    // {
    // String[] listItems = { "Chris", "Joshua", "Daniel", "Michael", "Don", "Kimi", "Kelly", "Keagan" };
    // JFrame frame = new JFrame("FilteredJList");
    // frame.getContentPane().setLayout(new BorderLayout());
    // // populate list
    // FilteredJList list = new FilteredJList();
    // for (int i = 0; i < listItems.length; i++)
    // list.addItem(listItems[i]);
    // // add to gui
    // JScrollPane pane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
    // ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    // frame.getContentPane().add(pane, BorderLayout.CENTER);
    // frame.getContentPane().add(list.getFilterField(), BorderLayout.NORTH);
    // frame.pack();
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.setVisible(true);
    // }

    class FilterModel extends AbstractListModel {
        ArrayList<Object> items;
        ArrayList<Object> filterItems;

        public FilterModel() {
            super();
            this.items = new ArrayList<Object>();
            this.filterItems = new ArrayList<Object>();
        }

        public FilterModel(Object[] listItems) {
            this();
            for (Object listItem : listItems) {
                this.items.add(listItem);
                this.filterItems.add(listItem);
            }
        }

        public FilterModel(Vector<?> listItems) {
            this();
            for (Object listItem : listItems)
                this.items.add(listItem);
        }

        public Object getElementAt(int index) {
            if (index < this.filterItems.size())
                return this.filterItems.get(index);
            return null;
        }

        public int getSize() {
            return this.filterItems.size();
        }

        public void addElement(Object o) {
            this.items.add(o);
            refilter();
        }

        protected void refilter() {
            this.filterItems.clear();
            String term = getFilterField().getText();
            for (int i = 0; i < this.items.size(); i++)
                if (this.items.get(i).toString().indexOf(term, 0) != -1)
                    this.filterItems.add(this.items.get(i));
            fireContentsChanged(this, 0, getSize());
        }
    }

    // inner class provides filter-by-keystroke field
    class FilterField extends JTextField implements DocumentListener {
        public FilterField() {
            super();
            getDocument().addDocumentListener(this);
        }

        public void changedUpdate(DocumentEvent e) {
            ((FilterModel) getModel()).refilter();
        }

        public void insertUpdate(DocumentEvent e) {
            ((FilterModel) getModel()).refilter();
        }

        public void removeUpdate(DocumentEvent e) {
            ((FilterModel) getModel()).refilter();
        }
    }
}
