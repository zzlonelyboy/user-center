package com.example.usercenter.model.Dto;

import lombok.Data;

@Data
public class PageRequest {
    int page=1;
    int pageSize=20;
}
