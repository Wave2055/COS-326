package ui;

import model.Equipment;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class EquipmentTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "Equipment ID", "Name", "Category", "Purchase Date", "Replacement Cost", "Status"
    };

    private List<Equipment> equipmentList = new ArrayList<>();

    public void setEquipment(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList != null ? equipmentList : new ArrayList<>();
        fireTableDataChanged();
    }

    public Equipment getEquipmentAt(int row) {
        return equipmentList.get(row);
    }

    @Override
    public int getRowCount() {
        return equipmentList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 4 ? Double.class : Object.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Equipment e = equipmentList.get(rowIndex);
        switch (columnIndex) {
            case 0: return e.getEquipmentId();
            case 1: return e.getEquipmentName();
            case 2: return e.getEquipmentCategory();
            case 3: return e.getPurchaseDate();
            case 4: return e.getReplacementCost();
            case 5: return e.getStatus();
            default: return "";
        }
    }
}