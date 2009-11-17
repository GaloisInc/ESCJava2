package javax.swing.tree;

import javax.swing.event.*;
import java.beans.PropertyChangeListener;

public interface TreeSelectionModel {
    public static final int SINGLE_TREE_SELECTION = 1;
    public static final int CONTIGUOUS_TREE_SELECTION = 2;
    public static final int DISCONTIGUOUS_TREE_SELECTION = 4;
    
    void setSelectionMode(int mode);
    
    int getSelectionMode();
    
    void setSelectionPath(TreePath path);
    
    void setSelectionPaths(TreePath[] paths);
    
    void addSelectionPath(TreePath path);
    
    void addSelectionPaths(TreePath[] paths);
    
    void removeSelectionPath(TreePath path);
    
    void removeSelectionPaths(TreePath[] paths);
    
    TreePath getSelectionPath();
    
    TreePath[] getSelectionPaths();
    
    int getSelectionCount();
    
    boolean isPathSelected(TreePath path);
    
    boolean isSelectionEmpty();
    
    void clearSelection();
    
    void setRowMapper(RowMapper newMapper);
    
    RowMapper getRowMapper();
    
    int[] getSelectionRows();
    
    int getMinSelectionRow();
    
    int getMaxSelectionRow();
    
    boolean isRowSelected(int row);
    
    void resetRowSelection();
    
    int getLeadSelectionRow();
    
    TreePath getLeadSelectionPath();
    
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    void removePropertyChangeListener(PropertyChangeListener listener);
    
    void addTreeSelectionListener(TreeSelectionListener x);
    
    void removeTreeSelectionListener(TreeSelectionListener x);
}
