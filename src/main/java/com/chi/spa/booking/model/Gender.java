package com.chi.spa.booking.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
  特殊的類別（Class）
  用來定義一組「固定的常數」。當某個欄位的值是可以被窮舉(數得完)，且不會輕易改變的時候，最適合用 Enum。
 */

public enum Gender {

    MALE,
    FEMALE;

    @JsonCreator
    public static Gender fromString(String value) {
        if (value == null) return null;
        return Gender.valueOf(value.toUpperCase());
    }
}
