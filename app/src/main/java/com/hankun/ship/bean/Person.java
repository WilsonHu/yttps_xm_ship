package com.hankun.ship.bean;

import java.util.List;

public class Person {
    private List<String> tag_id_list;
    private Integer upload_time;
    private PersonInformation person_information;
    private List<FaceListBean> face_list;
    private String identity;
    private Object meta;
    private String scene_image_id;
    private String person_id;

    public List<String> getTag_id_list() {
        return tag_id_list;
    }

    public void setTag_id_list(List<String> tagIdList) {
        this.tag_id_list = tagIdList;
    }

    public Integer getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(Integer uploadTime) {
        this.upload_time = uploadTime;
    }

    public PersonInformation getPerson_information() {
        return person_information;
    }

    public void setPerson_information(PersonInformation personInformation) {
        this.person_information = personInformation;
    }

    public List<FaceListBean> getFace_list() {
        return face_list;
    }

    public void setFace_list(List<FaceListBean> face_list) {
        this.face_list = face_list;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public String getScene_image_id() {
        return scene_image_id;
    }

    public void setScene_image_id(String sceneImageId) {
        this.scene_image_id = sceneImageId;
    }

    public String getPerson_id() {
        return person_id;
    }

    public void setPerson_id(String personId) {
        this.person_id = personId;
    }

    /**
     * 目前判断相同的条件是人名、电话都不变
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Person) {
            Person person = (Person) obj;
            return (person.person_information.getName().equals(person_information.getName())
                    && person.person_information.getPhone().equals(person_information.getPhone()));
        } else {
            return super.equals(obj);

        }
    }
}
