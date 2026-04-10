package com.tenco.view;

import com.tenco.dao.ProductDAO;
import com.tenco.dto.Product;
import com.tenco.dto.Sales;
import com.tenco.service.StoreService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StoreView {

    private final StoreService service = new StoreService();
    private final Scanner scanner = new Scanner(System.in);
    //private final ProductDAO productDAO = new ProductDAO();

    private String correntAdminId = null; //관리자 Id
    private String currentAdminPassword = null; // 관리자 비번
    private String correntAdminName = null; // 관리자 이름


    public void start() throws SQLException {
        System.out.println("무인 편의점 재고 관리 시스템 시작");

        while (true) {
            printMenu();
            int choice = readInt("선택: ");

            try {
                switch (choice) {
                    case 1:
                        getProductList();
                        break;
                    case 2:
                        findByBarcode();
                        break;
                    case 3:
                        processSale();
                        break;
                    case 4:
                        findTodaySales();
                        break;
                    case 5:
                        login();
                        break;
                    case 6:
                        System.out.println("프로그램을 종료합니다.");
                        scanner.close();
                        return;
                    case 7:
                        if (service.login(correntAdminId, currentAdminPassword) == null) {
                            System.out.println("로그인이 필요한 시스템입니다");
                            break;
                        }
                        insert();
                        break;
                    case 8:
                        if (service.login(correntAdminId, currentAdminPassword) == null) {
                            System.out.println("로그인이 필요한 시스템입니다");
                            break;
                        }
                        update();
                        break;
                    case 9:
                        if (service.login(correntAdminId, currentAdminPassword) == null) {
                            System.out.println("로그인이 필요한 시스템입니다");
                            break;
                        }
                        softDelete();
                        break;
                    case 10:
                        if (service.login(correntAdminId, currentAdminPassword) == null) {
                            System.out.println("로그인이 필요한 시스템입니다");
                            break;
                        }
                        isLowStock();
                        break;
                    case 11:
                        if (service.login(correntAdminId, currentAdminPassword) == null) {
                            System.out.println("로그인이 필요한 시스템입니다");
                            break;
                        }
                        logout();
                    default:
                        System.out.println("1~11 사이의 숫자를 입력하세요.");
                }
            } catch (SQLException e) {
                System.out.println("오류: " + e.getMessage());
            }
        }

    }

    private void printMenu() throws SQLException {
        System.out.println("\n=== 편의점 재고 관리 시스템 ===");
        if (!service.isLoggedIn()) {
            System.out.println("[ 로그아웃 상태 ]");
        } else {
            System.out.println("[ 로그인 중 ]");
        }
        System.out.println("──────────────────────");
        System.out.println("1.  상품 목록 조회");
        System.out.println("2.  바코드로 상품 검색");
        System.out.println("3.  판매 처리");
        System.out.println("4.  오늘 매출 조회");
        System.out.println("5.  관리자 로그인");
        if (service.login(correntAdminId, currentAdminPassword) != null) {
            System.out.println("6.  상품 등록");
            System.out.println("7.  상품 수정");
            System.out.println("8.  상품 소프트 삭제");
            System.out.println("9.  재고 부족 알림");
            System.out.println("10. 로그아웃");
            System.out.println("11. 종료");
        }


    }

    private void getProductList() throws SQLException {

        List<Product> products = service.getProductList();
        System.out.println("상품 목록 조회");
        if (products.isEmpty()) {
            System.out.println("등록된 상품이 없습니다");
        } else {
            for (Product p : products) {
                StringBuilder stringBuilder = new StringBuilder();
                System.out.printf("ID: %2d | %-15s | %-10s | %-5s | %-10s | %-10s | %-5s | %-5s | %-10s | %s%n",
                        p.getId(),
                        p.getBarcode(),
                        p.getName(),
                        p.getCategory(),
                        p.getPrice(),
                        p.getCost(),
                        p.getStock(),
                        p.getMinStock(),
                        p.getExpireDate(),
                        p.isActive() ? "판매중" : "판매 불가");
                if (service.isLowStock(p)) stringBuilder.append("[재고부족]");
                if (service.isNearExpiry(p)) stringBuilder.append("[유통기한임박]");
                System.out.printf("%-5s | %s%n",
                        p.getName(),
                        stringBuilder.toString()
                );
            }
        }
    }

    //.바코드로 물품 찾기

    private void findByBarcode() throws SQLException {
        System.out.print("바코드를 입력 : ");
        String barcode = scanner.nextLine().trim();
        if (barcode == null) {
            System.out.println("바코드를 입력해 주세요");
        }
        System.out.println(service.productDAO.findByBarcode(barcode));
    }



    private int readInt(String prompt) throws SQLException {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    // 판매처리할 상품 바코드와 개수
    private void processSale() throws SQLException {
        System.out.println("상품판매처리");
        System.out.print("바코드 입력 :");
        String barcode = scanner.nextLine().trim();
        System.out.print("수량 입력 : ");
        int quantity = scanner.nextInt();
        //수량 방어적 코드 작성 해야됨
        service.processSale(barcode, quantity);
    }

    //오늘 매출 조회
    private void findTodaySales() throws SQLException {
        System.out.println("오늘 매출 조회");
        service.getTodaySales();

    }

    //관리자 로그인
    private void login() throws SQLException {
        System.out.println("관리자 로그인");
        System.out.print("아이디 입력 : " );
        String id = scanner.nextLine().trim();
        System.out.println("비밀번호 입력 : ");
        String password = scanner.nextLine().trim();
        service.login(id,password);
    }

    //상품등록
    private void insert() throws SQLException {
        Product newProduct = Product.builder()
                .barcode(scanner.nextLine())
                .name(scanner.nextLine())
                .build();

        boolean success = service.insertProduct(newProduct);

        if (success) {
            System.out.println("상품 등록 성공!");
        }
    }

    //상품수정
    private void update() throws SQLException {
        System.out.print("수정할 상품의 바코드 입력: ");
        String barcode = scanner.nextLine().trim();


        Product existingProduct = service.productDAO.findByBarcode(barcode);
        if (existingProduct == null) {
            System.out.println("해당 바코드의 상품이 없습니다.");
            return;
        }

        // 2. 수정할 내용 입력받기 (Enter 시 유지 로직 적용)
        System.out.print("새 이름 (기존: " + existingProduct.getName() + "): ");
        String newName = scanner.nextLine();
        if (!newName.isEmpty()) existingProduct.setName(newName);

        // 3. 서비스 호출
        boolean success = service.updateProduct(existingProduct);
        if (success) {
            System.out.println("상품 정보가 수정되었습니다.");
        }
    }

    //상품 소프트 삭제
    private void softDelete() throws SQLException {

    }

    //재고부족알림
    private void isLowStock() throws SQLException {

    }


    //종료/로그아웃
    private void logout() throws SQLException {

    }


}
