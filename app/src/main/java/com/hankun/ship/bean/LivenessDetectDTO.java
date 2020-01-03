package com.hankun.ship.bean;

public class LivenessDetectDTO {

    private ConfigInfo config_info;
    private ImageInfo image_info;
    private OptionInfo option_info;

    public ConfigInfo getConfig_info() {
        return config_info;
    }

    public void setConfig_info(ConfigInfo config_info) {
        this.config_info = config_info;
    }

    public ImageInfo getImage_info() {
        return image_info;
    }

    public void setImage_info(ImageInfo image_info) {
        this.image_info = image_info;
    }

    public OptionInfo getOption_info() {
        return option_info;
    }

    public void setOption_info(OptionInfo option_info) {
        this.option_info = option_info;
    }

    public class ConfigInfo {
        private String image_resolution;
        private String channel;
        private String OS;

        public String getImage_resolution() {
            return image_resolution;
        }

        public void setImage_resolution(String image_resolution) {
            this.image_resolution = image_resolution;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getOS() {
            return OS;
        }

        public void setOS(String OS) {
            this.OS = OS;
        }
    }

    public class ImageInfo {
        private String image_content;
        private String other_info;

        public String getImage_content() {
            return image_content;
        }

        public void setImage_content(String image_content) {
            this.image_content = image_content;
        }

        public String getOther_info() {
            return other_info;
        }

        public void setOther_info(String other_info) {
            this.other_info = other_info;
        }
    }

    public class OptionInfo {
        private String living_detect_threshold;

        public String getLiving_detect_threshold() {
            return living_detect_threshold;
        }

        public void setLiving_detect_threshold(String living_detect_threshold) {
            this.living_detect_threshold = living_detect_threshold;
        }
    }
}
