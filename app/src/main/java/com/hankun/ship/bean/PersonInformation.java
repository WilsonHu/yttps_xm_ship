package com.hankun.ship.bean;



/**
 * 人员详细信息
 * @author HT
 */
public class PersonInformation {
    private String birthday;
    private String visit_purpose;
    private String remark;
    private String identity_number;
    private String phone;
    private String name;
    private String company;
    private String id;
    private String visitee_name;
    private String employed_date;
    private Long visit_end_timestamp;
    private Long visit_start_timestamp;
    private String visit_time_type;
    private String card_no;


    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getVisit_purpose() {
        return visit_purpose;
    }

    public void setVisit_purpose(String visitPurpose) {
        this.visit_purpose = visitPurpose;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getIdentity_number() {
        return identity_number;
    }

    public void setIdentity_number(String identityNumber) {
        this.identity_number = identityNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVisitee_name() {
        return visitee_name;
    }

    public void setVisitee_name(String visiteeName) {
        this.visitee_name = visiteeName;
    }

    public String getEmployed_date() {
        return employed_date;
    }

    public void setEmployed_date(String employedDate) {
        this.employed_date = employedDate;
    }

    public Long getVisit_end_timestamp() {
        return visit_end_timestamp;
    }

    public void setVisit_end_timestamp(Long visitEndTimestamp) {
        this.visit_end_timestamp = visitEndTimestamp;
    }

    public Long getVisit_start_timestamp() {
        return visit_start_timestamp;
    }

    public void setVisit_start_timestamp(Long visitStartTimestamp) {
        this.visit_start_timestamp = visitStartTimestamp;
    }

    public String getVisit_time_type() {
        return visit_time_type;
    }

    public void setVisit_time_type(String visitTimeType) {
        this.visit_time_type = visitTimeType;
    }

    public String getCard_no() {
        return card_no;
    }

    public void setCard_no(String card_no) {
        this.card_no = card_no;
    }
}