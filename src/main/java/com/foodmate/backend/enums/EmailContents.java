package com.foodmate.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailContents {
    WELCOME("FoodMate 사이트 가입을 축하드립니다. ", "<p>FoodMate 사이트 가입을 축하드립니다.<p><p>아래 링크를 클릭하셔서 가입을 완료하세요.</p>"
            + "<div><a target='_blank' href='http://localhost:8080/member/email-auth?id={uuid}'> 가입 완료 </a></div>");

    private String subject;
    private String text;

    EmailContents(String subject, String text) {
        this.subject = subject;
        this.text = text;
    }


}
