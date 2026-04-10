package com.tenco.dao;

import com.mysql.cj.util.DataTypeUtil;
import com.tenco.dto.Product;
import com.tenco.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ### 1단계 - 상품 전체 목록 조회 (findAll)
 * <p>
 * ### 2단계 - 바코드로 상품 조회 (findByBarcode)
 * <p>
 * ### 3단계 - 상품 등록 (insert)
 * <p>
 * ## 4단계 - 상품 수정 (update)
 * <p>
 * ## 5단계 - 소프트 삭제 (delete)
 * <p>
 * ## 6단계 - 재고 부족 상품 조회 (findLowStock)
 */
public class ProductDAO {

    // 1단계 - 상품 전체 목록 조회 (findAll)
    public List<Product> findAll() throws SQLException {
        List<Product> productList = new ArrayList<>();

        String sql = """
                SELECT * FROM product WHERE is_active = TRUE
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productList.add(mapToProduct(rs));
            }
        }
        return productList;
    }

    //2단계 - 바코드로 상품 조회 (findByBarcode)
    public Product findByBarcode(String barcode) throws SQLException {

        String barcodeSql = """
                SELECT * FROM product WHERE barcode = ? AND is_active = TRUE
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(barcodeSql)) {
            pstmt.setString(1, barcode);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToProduct(rs);
                }
            }
        }
        return null;
    }

    //3단계 - 상품 등록 (insert)
    public boolean insert(Product product) throws SQLException {

        String insertSql = """
                INSERT INTO product(barcode, name, category, price, cost, stock, min_stock, expire_date, is_active)
                 VALUES (?, ?, ? ,? ,? ,? ,? ,? ,?)
                """;

        try (Connection conn = DBConnectionManager.getConnection()) {
            try (PreparedStatement inPstmt = conn.prepareStatement(insertSql)) {

                inPstmt.setString(1, product.getBarcode());
                inPstmt.setString(2, product.getName());
                inPstmt.setString(3, product.getCategory());
                inPstmt.setBigDecimal(4, product.getPrice());
                inPstmt.setBigDecimal(5, product.getCost());
                inPstmt.setInt(6, product.getStock());
                inPstmt.setInt(7, product.getMinStock());
                inPstmt.setDate(8, Date.valueOf(product.getExpireDate()));
                inPstmt.setBoolean(9, product.isActive());
                return inPstmt.executeUpdate() > 0;
            }
        }

    }


    //4단계 - 상품 수정 (update)
    public boolean update(Product product) throws SQLException {
        String updateSql = """
                UPDATE product
                SET barcode = ? , name = ? , category = ? , price = ? , cost = ? , stock = ?, min_stock = ? , expire_date = ?, is_active = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection()) {
            try (PreparedStatement upPstmt = conn.prepareStatement(updateSql)) {
                upPstmt.setString(1, product.getBarcode());
                upPstmt.setString(2, product.getName());
                upPstmt.setString(3, product.getCategory());
                upPstmt.setBigDecimal(4, product.getPrice());
                upPstmt.setBigDecimal(5, product.getCost());
                upPstmt.setInt(6, product.getStock());
                upPstmt.setInt(7, product.getMinStock());
                upPstmt.setDate(8, Date.valueOf(product.getExpireDate()));
                upPstmt.setBoolean(9, product.isActive());
                upPstmt.setInt(10, product.getId());
                return upPstmt.executeUpdate() > 0;
            }
        }
    }

    // 5단계 - 소프트 삭제 (delete)
    public boolean softDelete(int id) throws SQLException {
        String sdSql = """
                UPDATE product
                SET is_active = FALSE
                WHERE id = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection()) {
            try (PreparedStatement sdPstmt = conn.prepareStatement(sdSql)) {
                sdPstmt.setInt(1, id);

                return sdPstmt.executeUpdate() > 0;
            }
        }
    }

    //6단계 - 재고 부족 상품 조회 (findLowStock)
    public List<Product> findLowStock() throws SQLException {
        List<Product> lowStockList = new ArrayList<>();

        String flsSql = """
                SELECT *
                FROM product
                WHERE stock <= min_stock
                AND is_active = TRUE
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement flsPstmt = conn.prepareStatement(flsSql);
             ResultSet rs = flsPstmt.executeQuery()) {

            while (rs.next()) {
                lowStockList.add(mapToProduct(rs));
            }
        }
        return lowStockList;
    }

    private Product mapToProduct(ResultSet rs) throws SQLException {
        return Product.builder()
                .id(rs.getInt("id"))
                .barcode(rs.getString("barcode"))
                .name(rs.getString("name"))
                .category(rs.getString("category"))
                .price(rs.getBigDecimal("price"))
                .cost(rs.getBigDecimal("cost"))
                .stock(rs.getInt("stock"))
                .minStock(rs.getInt("min_stock"))
                .expireDate(
                        rs.getDate("expire_date") != null
                                ? rs.getDate("expire_date").toLocalDate()
                                : null
                )
                .isActive(rs.getBoolean("is_active"))
                .build();
    }
}
