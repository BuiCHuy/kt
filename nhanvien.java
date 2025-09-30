package kiemthu;

import java.util.Date;

public class nhanvien {
	public int id;
	public String hoten;
	public String gioitinh;
	public Date ngaysinh;
	public String sdt;
	public String diachi;
	public String tk;
	public String mk;
	public nhanvien(String ht,String gt, Date ns,String sd,String dc,String tk,String mk) {
		id = 0;
		hoten = ht;
		gioitinh = gt;
		ngaysinh = ns;
		sdt = sd;
		diachi = dc;
		this.tk = tk;
		this.mk = mk;	
	}
	public int getId() { return id; }
	public String getHoten() { return hoten; }
    public String getGioitinh() { return gioitinh; }
    public Date getNgaysinh() { return ngaysinh; }
    public String getSdt() { return sdt; }
    public String getDiachi() { return diachi; }
    public String getTk() { return tk; }
    public String getMk() { return mk; } 
}
