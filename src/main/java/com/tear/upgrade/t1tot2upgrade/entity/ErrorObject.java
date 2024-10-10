package com.tear.upgrade.t1tot2upgrade.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ErrorObject {

    private Integer statusCode;

    private String message;

    private Date timestamp;
}
