package com.foodmate.backend.enums;

public enum EnrollmentStatus {
    SUBMIT,          // 신청완료
    CANCEL,          // 신청취소
    ACCEPT,          // 수락
    REFUSE,          // 거절
    GROUP_CANCEL,    // 모임취소
    GROUP_COMPLETE;   // 모임완료


    public static EnrollmentStatus fromString(String text) {
        for (EnrollmentStatus status : EnrollmentStatus.values()) {
            if (status.toString().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return null; // 일치하는 열거형 값이 없을 경우 null 반환
    }
}
