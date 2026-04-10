package com.tenco.service;

import com.tenco.dao.AdminDAO;
import com.tenco.dao.ProductDAO;
import com.tenco.dao.SalesDAO;
import com.tenco.dto.Admin;
import com.tenco.dto.Product;
import com.tenco.dto.Sales;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoreService {

    private final AdminDAO adminDAO = new AdminDAO();
    public final ProductDAO productDAO = new ProductDAO();
    private final SalesDAO salesDAO = new SalesDAO();

    private Admin adminInfo;

    //로그인
    public Boolean login(String adminId, String password) throws SQLException {
        Admin admin = adminDAO.login(adminId, password);
        if (admin != null) {
            adminInfo = admin;
            return true;
        }
        return false;
    }

    //로그아웃
    public void logout() throws SQLException {
        if (adminInfo == null) {
            System.out.println("로그아웃 상태 입니다");
        } else {
            adminInfo = null;
            System.out.println("로그아웃되었습니다");
        }


    }

    //로그인 상태 확인
    public Boolean isLoggedIn() throws SQLException {
        return adminInfo != null;
    }

    //상품 목록
    public List<Product> getProductList() throws SQLException {
        return productDAO.findAll();
    }

    //판매 처리 (결과 메시지 반환)
    public String processSale(String barcode, int quantity) throws SQLException {
        Product product = productDAO.findByBarcode(barcode);

        if (product == null) {
            return "존재하지 않는 상품입니다";
        }
        if (product != null) {
            if (product.getStock() < quantity) {
                return "재고가 부족한 상품입니다";
            } else {
                salesDAO.processSale(product, quantity);
                return "판매완료";
            }
        }
        return "판매 오류";
    }

    //재고 부족 판단 - 비즈니스 판단 메서드 - 기준은 서비스가 정함
    public boolean isLowStock(Product product) throws SQLException {
        return product.getStock() <= product.getMinStock();
    }

    //유통기한 임박 판단
    public boolean isNearExpiry(Product product) throws SQLException {

        if ((product.getExpireDate()) == null) {
            return false;
        }
        if (!product.getExpireDate().isBefore(LocalDate.now()) && product.getExpireDate().isBefore((LocalDate.now()).plusDays(3))) {
            System.out.println("유통기한이 3일 전입니다");
            return true;
        }
        return false;
    }

    //총매출
    public List<Sales> getTodaySales() throws SQLException {

        return salesDAO.findTodaySales();
    }

    //상품등록
    public boolean insertProduct(Product product) throws SQLException {

        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        return productDAO.insert(product);
    }

    //상품 수정
    public boolean updateProduct(Product product) throws SQLException {

        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("오류: 가격은 0원 이상이어야 합니다.");
            return false;
        }

        return productDAO.update(product);
    }

    public static void main(String[] args) throws SQLException {
        StoreService service = new StoreService();

        boolean loginChack = service.login("admin", "admin123");
        System.out.println("로그인" + loginChack);

        System.out.println("-----------로그아웃-------------");
        service.logout();

        System.out.println("------------연결상태---------");
        boolean isLogIn = service.isLoggedIn();
        System.out.println(isLogIn);

        System.out.println("-----상품리스트----------");
        service.getProductList();

        System.out.println("판매처리");
        service.processSale("8801234560001",2);



    }


}
