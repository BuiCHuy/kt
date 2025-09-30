package kiemthu;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Iterator;

public class NhanVienManagementGUI extends JFrame {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private List<nhanvien> nhanVienList;
    private DefaultTableModel tableModel;
    private JTable nhanVienTable;

    // Các thành phần GUI cho việc nhập liệu mới
    private JTextField hotenField, sdtField, diachiField, tkField, mkField, ngaysinhField, searchField;
    private JComboBox<String> gioitinhBox; // Dùng JComboBox cho Giới tính
    private JButton addButton, editButton, deleteButton, searchButton, clearButton;
    String emessage;
    public NhanVienManagementGUI() {
        // 1. Khởi tạo dữ liệu
        nhanVienList = new ArrayList<>();
        // Thêm dữ liệu mẫu
        try {
            nhanVienList.add(new nhanvien("Nguyễn Văn A", "Nam", dateFormat.parse("15/05/1990"), "0981234567", "Hà Nội", "nva", "123"));
            nhanVienList.add(new nhanvien("Trần Thị B", "Nữ", dateFormat.parse("20/12/1995"), "0909876543", "TP. HCM", "ttb", "456"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 2. Cài đặt cửa sổ chính (JFrame)
        setTitle("HỆ THỐNG QUẢN LÝ NHÂN VIÊN (Thông tin chi tiết)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 3. Tạo Panel Nhập liệu (Phía Bắc)
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);

        // 4. Tạo Panel Bảng (Giữa)
        JScrollPane tableScrollPane = createTablePanel();
        add(tableScrollPane, BorderLayout.CENTER);

        // 5. Cập nhật dữ liệu vào bảng lần đầu
        loadnhanvienDataToTable();

        // 6. Gán các hành động
        addButton.addActionListener(e -> addnhanvien());
        editButton.addActionListener(e -> editnhanvien());
        deleteButton.addActionListener(e -> deletenhanvien());
        searchButton.addActionListener(e -> searchnhanvien());
        clearButton.addActionListener(e -> clearFields());

        nhanVienTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && nhanVienTable.getSelectedRow() != -1) {
                fillFieldsFromTable();
            }
        });

        setVisible(true);
    }

    /**
     * Tạo Panel chứa các ô nhập liệu và nút điều khiển
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Thông Tin Nhân Viên Chi Tiết & Thao Tác"));

        // Phần 1: Ô nhập liệu (sử dụng GridLayout 4 hàng x 4 cột)
        JPanel dataPanel = new JPanel(new GridLayout(4, 4, 10, 5));

        // Hàng 1
        dataPanel.add(new JLabel("Họ Tên:"));
        hotenField = new JTextField(15);
        dataPanel.add(hotenField);

        dataPanel.add(new JLabel("Giới Tính:"));
        gioitinhBox = new JComboBox<>(new String[]{"","Nam", "Nữ"});
        dataPanel.add(gioitinhBox);

        // Hàng 2
        dataPanel.add(new JLabel("Ngày Sinh (dd/MM/yyyy):"));
        ngaysinhField = new JTextField(10);
        dataPanel.add(ngaysinhField);
        
        dataPanel.add(new JLabel("SĐT:"));
        sdtField = new JTextField(10);
        dataPanel.add(sdtField);

        // Hàng 3
        dataPanel.add(new JLabel("Địa Chỉ:"));
        diachiField = new JTextField(20);
        dataPanel.add(diachiField);

        dataPanel.add(new JLabel("Tài Khoản:"));
        tkField = new JTextField(10);
        dataPanel.add(tkField);

        // Hàng 4
        dataPanel.add(new JLabel("Mật Khẩu:"));
        mkField = new JTextField(10);
        dataPanel.add(mkField);
        
        // Ô trống hoặc thông tin thêm
        dataPanel.add(new JLabel("")); 
        dataPanel.add(new JLabel(""));

        panel.add(dataPanel, BorderLayout.CENTER);

        // Phần 2: Nút chức năng (Phía Đông)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("➕ Thêm Mới");
        editButton = new JButton("✍️ Cập Nhật");
        deleteButton = new JButton("❌ Xóa");
        clearButton = new JButton("🧹 Xóa Trống");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        panel.add(buttonPanel, BorderLayout.EAST);

        // Phần 3: Tìm kiếm (Phía Nam)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchField = new JTextField(25);
        searchButton = new JButton("🔍 Tìm Kiếm");
        searchPanel.add(new JLabel("Tìm kiếm (Họ Tên/SĐT/Địa chỉ):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel fullInputPanel = new JPanel(new BorderLayout());
        fullInputPanel.add(panel, BorderLayout.CENTER);
        fullInputPanel.add(searchPanel, BorderLayout.SOUTH);

        return fullInputPanel;
    }

    /**
     * Tạo Panel chứa bảng hiển thị danh sách nhân viên
     */
    private JScrollPane createTablePanel() {
        // Định nghĩa tiêu đề cột mới
        String[] columnNames = {"Họ Tên", "Giới Tính", "Ngày Sinh", "SĐT", "Địa Chỉ", "Tài Khoản", "Mật Khẩu"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        nhanVienTable = new JTable(tableModel);
        nhanVienTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return new JScrollPane(nhanVienTable);
    }

    /**
     * Cập nhật dữ liệu từ nhanVienList vào JTable
     */
    private void loadnhanvienDataToTable() {
        tableModel.setRowCount(0);

        for (nhanvien nv : nhanVienList) {
            Object[] row = new Object[]{
                nv.getHoten(),
                nv.getGioitinh(),
                nv.getNgaysinh() != null ? dateFormat.format(nv.getNgaysinh()) : "",
                nv.getSdt(),
                nv.getDiachi(),
                nv.getTk(),
                nv.getMk() // Cảnh báo bảo mật: Mật khẩu không nên hiển thị
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Điền dữ liệu từ hàng được chọn vào các ô nhập liệu
     */
    private void fillFieldsFromTable() {
        int selectedRow = nhanVienTable.getSelectedRow();
        if (selectedRow != -1) {
            hotenField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            gioitinhBox.setSelectedItem(tableModel.getValueAt(selectedRow, 1).toString());
            ngaysinhField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            sdtField.setText(tableModel.getValueAt(selectedRow, 3).toString());
            diachiField.setText(tableModel.getValueAt(selectedRow, 4).toString());
            tkField.setText(tableModel.getValueAt(selectedRow, 5).toString());
            mkField.setText(tableModel.getValueAt(selectedRow, 6).toString());

            // Trong ví dụ này, coi Họ Tên là khóa chính, không cho sửa khi đang sửa
            hotenField.setEditable(false);
        }
    }

    /**
     * Xóa nội dung trong các ô nhập liệu
     */
    private void clearFields() {
        hotenField.setText("");
        gioitinhBox.setSelectedIndex(0);
        ngaysinhField.setText("");
        sdtField.setText("");
        diachiField.setText("");
        tkField.setText("");
        mkField.setText("");
        hotenField.setEditable(true);
        nhanVienTable.clearSelection();
        loadnhanvienDataToTable();
    }

    // =============================================================
    // CHỨC NĂNG XỬ LÝ DỮ LIỆU
    // =============================================================
    
    
    private boolean Validate() {
    	String ht = hotenField.getText();
    	String gt = gioitinhBox.getSelectedItem().toString();
    	String ns = ngaysinhField.getText().trim();
    	String sdt = sdtField.getText().trim();
	    String diachi = diachiField.getText().trim();
	    String tk = tkField.getText().trim();
	    String mk = mkField.getText().trim();
	    String regex = "^[a-zA-Z\\s\\p{L}]+$";
	    LocalDate dob; 
	    // validate tên nhân viên
	    if(ht.isEmpty()) {
	    	emessage = "Vui lòng nhập tên nhân viên" ;
	    	return false;
	    }
	    else if(!ht.matches(regex)) {
	    	emessage = "Tên nhân viên không được chứa ký tự đặc biệt";
	    	return false;
	    }
	    else if(ht.length()>30) {
	    	emessage = "Vui lòng nhập họ tên không quá 30 ký tự";
	    	return false;
	    }
	    // validate giới tính
	    if(gt=="") {
	    	emessage = "Vui lòng chọn giới tính";
	    	return false;
	    }
	    
	    // validate ngày sinh
	    if(ns.isEmpty()) {
	    	emessage = "Vui lòng nhập ngày sinh";
	    }
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd"); 
	 
		 try {
		     dob = LocalDate.parse(ns, formatter);
		         
		     if (dob.isAfter(LocalDate.now())) {
		         emessage = "Ngày sinh không được là ngày trong tương lai";
		         return false;
		     }
		         
		 } catch (DateTimeParseException ex) {
		     emessage = "Ngày sinh không hợp lệ (Định dạng YYYY/MM/DD hoặc ngày tháng không tồn tại)";
		     return false;
		 }
		 LocalDate minAgeDate = LocalDate.now().minusYears(18);   
	    if (dob.isAfter(minAgeDate)) {
	        emessage = "Độ tuổi không hợp lệ. Nhân viên phải từ 18 tuổi trở lên";
	        return false;
	    }
		   
	    //validate sdt
	    if (sdt.isEmpty()) {
	        emessage = "Vui lòng nhập số điện thoại";
	        return false;
	    }
	    else if (!sdt.matches("\\d+")) {
	    	emessage = "Số điện thoại chỉ được chứa ký tự số";
	        return false;
	    }
	    else if(!sdt.startsWith("0")) {
	    	emessage  = "Số điện thoại phải bắt đầu từ số 0";
	        return false;
	    }
	    else if(sdt.length()!= 11 && sdt.length()!=10) {
	    	emessage  = "Số điện thoại phải có 10 hoặc 11 chữ số";
	    	return false;
	    }
	    // validate cho địa chỉ
	    String addressPattern = "^[a-zA-Z0-9\\s\\p{L}/,.-]+$"; 
	    if(diachi.isEmpty()) {
	    	emessage  = "Vui lòng nhập địa chỉ";
	        return false;
	    }
	    else if(diachi.length()>100) {
	    	emessage = "Vui lòng nhập địa chỉ không quá 100 ký tự";
	    	return false;
	    }
	    else if(!diachi.matches(addressPattern)) {
	    	emessage = "Địa chỉ không được chứa ký tự đặc biệt";
	    	return false;
	    }
	    
	    //validate cho tài khoản
	    String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$"; 
	    if(tk.isEmpty()) {
	    	emessage = "Vui lòng nhập tên tài khoản";
	    	return false;
	    }
	    else if(tk.length()>30) {
	    	emessage = "Email không được vượt quá 30 ký tự";
	    	return false;
	    }
	    else if(!tk.matches(emailPattern)) {
	    	emessage = "Vui lòng nhập đúng định dạng Email";
	    	return false;
	    }
	    for (nhanvien nv : nhanVienList) {
            if (nv.getTk().equalsIgnoreCase(tk)) {
                emessage = "Email này đã được sử dụng, vui lòng nhập email khác";
                return false;
            }
        }
	    
	    //validate cho mk
	    boolean hasLetter = mk.matches(".*[a-zA-Z].*"); 
	    boolean hasDigit = mk.matches(".*\\d.*"); 
	    if(mk.isEmpty()) {
	    	emessage = "Vui lòng nhập mật khẩu";
	    	return false;
	    }
	    else if(mk.length()<6) {
	    	emessage = "Mật khẩu phải có ít nhất 6 ký tự";
	    	return false;
	    }
	    else if(mk.length()>20) {
	    	emessage = "Mật khẩu không được vượt quá 20 ký tự";
	    	return false;
	    }
	    else if(mk.contains(" ")) {
	    	emessage = "Mật khẩu không được chứa khoảng trắng";
	    	return false;
	    }
	    
	    else if(!hasLetter||!hasDigit) {
	    	emessage = "Mật khẩu phải bao gồm chữ cái và chữ số";
	    	return false;
	    }
	    return true;
    }

    /**
     * 1. Thêm nhân viên
     */
    private void addnhanvien() {
    	if(!Validate()) {
    		JOptionPane.showMessageDialog(this,emessage);
    		return;
    	}
    	else {
    		try {
                String hoten = hotenField.getText().trim();
                String gioitinh = (String) gioitinhBox.getSelectedItem();
                Date ngaysinh = dateFormat.parse(ngaysinhField.getText());
                String sdt = sdtField.getText().trim();
                String diachi = diachiField.getText().trim();
                String tk = tkField.getText().trim();
                String mk = mkField.getText().trim();

                if (hoten.isEmpty() || sdt.isEmpty() || diachi.isEmpty() || tk.isEmpty() || mk.isEmpty()) {
                     JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin bắt buộc.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                     return;
                }
                
                // Kiểm tra trùng lặp Họ Tên (coi như khóa chính tạm thời)
                for (nhanvien nv : nhanVienList) {
                    if (nv.getHoten().equalsIgnoreCase(hoten)) {
                        JOptionPane.showMessageDialog(this, "Nhân viên đã tồn tại (trùng Họ Tên).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                nhanvien newnhanvien = new nhanvien(hoten, gioitinh, ngaysinh, sdt, diachi, tk, mk);
                nhanVienList.add(newnhanvien);
                loadnhanvienDataToTable();
                clearFields();
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng dd/MM/yyyy.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xảy ra: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
    	}
        
    }

    /**
     * 2. Sửa nhân viên
     */
    private void editnhanvien() {
        try {
            String hotenToEdit = hotenField.getText().trim();
            String newGioiTinh = (String) gioitinhBox.getSelectedItem();
            Date newNgaySinh = dateFormat.parse(ngaysinhField.getText());
            String newSdt = sdtField.getText().trim();
            String newDiachi = diachiField.getText().trim();
            String newTk = tkField.getText().trim();
            String newMk = mkField.getText().trim();

            for (nhanvien nv : nhanVienList) {
                if (nv.getHoten().equalsIgnoreCase(hotenToEdit)) {
                    nv.gioitinh = newGioiTinh;
                    nv.ngaysinh = newNgaySinh;
                    nv.sdt = newSdt;
                    nv.diachi = newDiachi;
                    nv.tk = newTk;
                    nv.mk = newMk;

                    loadnhanvienDataToTable();
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên để sửa.", "Lỗi", JOptionPane.ERROR_MESSAGE);

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng dd/MM/yyyy.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 3. Xóa nhân viên
     */
    private void deletenhanvien() {
        int selectedRow = nhanVienTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa từ bảng.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa nhân viên này không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String hotenToDelete = tableModel.getValueAt(selectedRow, 0).toString();

            Iterator<nhanvien> iterator = nhanVienList.iterator();
            while (iterator.hasNext()) {
                nhanvien nv = iterator.next();
                if (nv.getHoten().equalsIgnoreCase(hotenToDelete)) {
                    iterator.remove();
                    loadnhanvienDataToTable();
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
        }
    }

    /**
     * 4. Tìm kiếm nhân viên
     */
    private void searchnhanvien() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadnhanvienDataToTable();
            return;
        }

        tableModel.setRowCount(0);

        for (nhanvien nv : nhanVienList) {
            if (nv.getHoten().toLowerCase().contains(keyword) ||
                nv.getSdt().contains(keyword) ||
                nv.getDiachi().toLowerCase().contains(keyword)) {

                Object[] row = new Object[]{
                    nv.getHoten(),
                    nv.getGioitinh(),
                    dateFormat.format(nv.getNgaysinh()),
                    nv.getSdt(),
                    nv.getDiachi(),
                    nv.getTk(),
                    nv.getMk()
                };
                tableModel.addRow(row);
            }
        }
    }

    // =============================================================
    // MAIN METHOD
    // =============================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NhanVienManagementGUI());
    }
}