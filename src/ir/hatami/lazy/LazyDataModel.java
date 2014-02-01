package ir.hatami.lazy;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.richfaces.component.SortOrder;
import org.richfaces.model.Arrangeable;
import org.richfaces.model.ArrangeableState;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

import javax.el.Expression;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LazyDataModel<T> extends ExtendedDataModel<T> implements Arrangeable {

    private List<T> data = new ArrayList<>();
    private Object rowKey = null;
    private List<FilterField> filterFields;
    private List<SortField> sortFields;
    private Locale locale;
    private List<T> cachedList = new ArrayList<>();
    private SequenceRange cachedRange;
    private Integer cachedRawCount;
    private int firstRow;
    private int row;
    private boolean filterd = false;

    public LazyDataModel(List<T> data) {
        this.data = data;
        cachedList.addAll(data);
    }

    @Override
    public void setRowKey(Object object) {
        rowKey = object;
    }

    @Override
    public Object getRowKey() {
        return rowKey;
    }

    protected static boolean areEqualRanges(SequenceRange range1, SequenceRange range2) {
        if (range1 == null || range2 == null) {
            return range1 == null && range2 == null;
        } else {
            return range1.getFirstRow() == range2.getFirstRow() && range1.getRows() ==
                    range2.getRows();
        }
    }

    @Override
    public void walk(FacesContext facesContext, DataVisitor dataVisitor, Range range, Object object) {
        SequenceRange sequenceRange = (SequenceRange) range;
        if (!areEqualRanges(cachedRange, sequenceRange)) {
            if (cachedList == null || cachedList.isEmpty())
                cachedList.addAll(data);
        }
        this.appendFilters();
        this.appendSorts();
        if (sequenceRange != null) {
            cachedRange = sequenceRange;
            firstRow = sequenceRange.getFirstRow();
            row = sequenceRange.getRows();

        }
        int thereshold = getRowCount();
        for (int i = 0; i < (row) && (firstRow < thereshold); i++) {
            dataVisitor.process(facesContext, firstRow++, object);
        }

    }


    @Override
    public boolean isRowAvailable() {
        return rowKey != null;
    }


    @Override
    public int getRowCount() {
        if (cachedRawCount == null) {

            appendFilters();
            if (filterd)
                cachedRawCount = cachedList.size();
            else {
                cachedRawCount = data.size();
                cachedList.clear();
                cachedList.addAll(data);
            }
        }
        return cachedRawCount;
    }

    @Override
    public T getRowData() {
        if ((Integer) rowKey < cachedList.size())
            return (T) cachedList.get((Integer) rowKey);

        return null;
    }

    @Override
    public int getRowIndex() {
        return -1;
    }

    @Override
    public void setRowIndex(int rowIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getWrappedData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWrappedData(Object data) {
        throw new UnsupportedOperationException();
    }

    private void appendFilters() {
        if (filterFields != null) {
            if (cachedList.size() != data.size()) {
                cachedList.clear();
                cachedList.addAll(data);
            }
            for (FilterField filterField : filterFields) {
                if (filterField.getFilterValue() != null) {
                    if (filterField.getFilterValue().toString().isEmpty()) {
                        continue;
                    }
                    Expression expression = filterField.getFilterExpression();
                    String expressionString = expression.getExpressionString();
                    if (!expression.isLiteralText()) {
                        expressionString = expressionString.replaceAll("[#|$]{1}\\{.*?\\.", "").replaceAll("\\}", "");
                    }
                    GenericFilter genericFilter = new GenericFilter(expressionString, filterField.getFilterValue().toString());
//                    cachedList.clear();
                    filterd = true;

                    for (T item : data) {
                        try {
                            if (!genericFilter.filtering(item)) {
                                cachedList.remove(item);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void appendSorts() {
        if (sortFields != null) {
            for (SortField sortField : sortFields) {
                Expression expression = sortField.getSortBy();
                String expressionString = expression.getExpressionString();
                if (!expression.isLiteralText()) {
                    expressionString = expressionString.replaceAll("[#|$]{1}\\{.*?\\.", "").replaceAll("\\}", "");
                }
                SortOrder ordering = sortField.getSortOrder();
                if (SortOrder.ascending.equals(ordering)) {
                    Collections.sort(this.cachedList, new GenericComparator(expressionString, true, this.locale));
                } else {
                    Collections.sort(this.cachedList, new GenericComparator(expressionString, false, this.locale));
                }
            }
        }
    }

    @Override
    public void arrange(FacesContext facesContext, ArrangeableState arrangeableState) {
        if (arrangeableState != null) {
            this.filterFields = arrangeableState.getFilterFields();
            this.sortFields = arrangeableState.getSortFields();
            this.locale = arrangeableState.getLocale();
//            this.cachedList.clear();
            this.cachedRange = null;
            this.cachedRawCount = null;
            filterd = false;
        }
    }

}


