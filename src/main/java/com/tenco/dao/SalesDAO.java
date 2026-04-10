package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.dto.Sales;
import com.tenco.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    // 판매처리(트랜잭션)
    public boolean processSale(Product product, int quantity) throws SQLException {
        Connection conn = null;
        // 트랜잭션 시작
        try {
            conn = DBConnectionManager.getConnection();
            conn.setAutoCommit(false);


            // 판매기록 - insert

            String listSql = """
                    INSERT INTO sales(product_id, quantity ,unit_price,sold_at) VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(listSql)) {
                pstmt.setInt(1, product.getId());
                pstmt.setInt(2, quantity);
                pstmt.setBigDecimal(3, product.getPrice());
                pstmt.setDate(4, Date.valueOf(LocalDate.now()));
                pstmt.executeUpdate();
            }

            // 상품 재고 차감 -update

            String updateSql = """
                    UPDATE product
                    SET stock = stock - ?
                    WHERE barcode = ?
                    """;

            try (PreparedStatement udPstmt = conn.prepareStatement(updateSql)) {
                udPstmt.setInt(1, product.getId());
                udPstmt.setString(2,product.getBarcode());
                udPstmt.executeUpdate();
            }
            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("오류 발생 : " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }// end of processSale

    // 오늘 매출 집계
    public List<Sales> findTodaySales() throws SQLException {
        List<Sales> salesList = new ArrayList<>();


        String ftSql = """
                SELECT\s
                    p.name AS 상품명,
                    SUM(s.quantity) AS 판매수량,
                    SUM(s.unit_price * s.quantity) AS 매출합계,
                    SUM(s.quantity * (s.unit_price - p.cost)) AS 순이익
                FROM sales s
                JOIN product p ON s.product_id = p.id
                WHERE DATE(s.sold_at) = CURDATE()
                GROUP BY p.id, p.name
                ORDER BY 매출합계 DESC
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ftPstmt = conn.prepareStatement(ftSql)) {

            try (ResultSet rs = ftPstmt.executeQuery()) {

                while (rs.next()) {
                    Sales sales = Sales.builder()
                            .productName(rs.getString("상품명"))
                            .totalQuantity(rs.getInt("판매수량"))
                            .totalSales(rs.getBigDecimal("매출합계"))
                            .totalProfit(rs.getBigDecimal("순이익"))
                            .build();
                    salesList.add(sales);
                }

            }
        }
        return salesList;
    }
}
