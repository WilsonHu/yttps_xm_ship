package com.hankun.ship.bean;

public class CheckImageResult {
    /**
     * Auto-generated: 2019-10-24 13:3:1
     *
     * @author bejson.com (i@bejson.com)
     * @website http://www.bejson.com/java2pojo/
     */

        private boolean is_living_person;
        private String detection_score;
        private int detection_advice;
        public void setIs_living_person(boolean is_living_person) {
            this.is_living_person = is_living_person;
        }
        public boolean getIs_living_person() {
            return is_living_person;
        }

        public void setDetection_score(String detection_score) {
            this.detection_score = detection_score;
        }
        public String getDetection_score() {
            return detection_score;
        }

        public void setDetection_advice(int detection_advice) {
            this.detection_advice = detection_advice;
        }
        public int getDetection_advice() {
            return detection_advice;
        }

}
