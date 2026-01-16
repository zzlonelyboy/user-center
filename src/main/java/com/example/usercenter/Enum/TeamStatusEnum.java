package com.example.usercenter.Enum;

public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私密"),
    SECRET(2,"加密");
    private Integer value;
    private String message;
    TeamStatusEnum(Integer code, String message) {
        this.value = code;
        this.message = message;
    }
    public static TeamStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    private Integer getValue() {
        return this.value;
    }
    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return message;
    }

    public void setText(String text) {
        this.message = text;
    }
}
