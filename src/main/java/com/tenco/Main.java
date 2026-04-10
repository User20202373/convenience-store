package com.tenco;

import com.tenco.view.StoreView;

import java.sql.SQLException;

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
public class Main {
    public static void main(String[] args) {
        //실행
        StoreView storeView = new StoreView();
        try {
            storeView.start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
