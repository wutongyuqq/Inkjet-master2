package me.samlss.inkjet.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "INK_ANNAL".
 */
@Entity
public class InkAnnal {

    @Id(autoincrement = true)
    private Long id;
    private String user_id;
    private String content;
    private Integer line_number;
    private Long print_time;
    private Long print_consumed_time;
    private String address;
    private Double latitude;
    private Double longitude;

    @Generated
    public InkAnnal() {
    }

    public InkAnnal(Long id) {
        this.id = id;
    }

    @Generated
    public InkAnnal(Long id, String user_id, String content, Integer line_number, Long print_time, Long print_consumed_time, String address, Double latitude, Double longitude) {
        this.id = id;
        this.user_id = user_id;
        this.content = content;
        this.line_number = line_number;
        this.print_time = print_time;
        this.print_consumed_time = print_consumed_time;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLine_number() {
        return line_number;
    }

    public void setLine_number(Integer line_number) {
        this.line_number = line_number;
    }

    public Long getPrint_time() {
        return print_time;
    }

    public void setPrint_time(Long print_time) {
        this.print_time = print_time;
    }

    public Long getPrint_consumed_time() {
        return print_consumed_time;
    }

    public void setPrint_consumed_time(Long print_consumed_time) {
        this.print_consumed_time = print_consumed_time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

}
