package com.example.greenribboncalimassignment.common;

public interface CodeValue {
    String getCode();        // Enum 이름 (PENDING, NORMAL_PREPAID...) - 필요 시 사용
    String getDescription(); // DB에 저장할 값 (대행 신청 완료, 일반 선불...)
}
