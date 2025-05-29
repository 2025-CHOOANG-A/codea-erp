package kr.co.codea.order;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

@Data
public class OrderDto {
    private Long ordId;
    private String ordCode;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date orderDate;
    private String bpName;
    private Double totalAmount;
    private String remark;
    private String status;
}