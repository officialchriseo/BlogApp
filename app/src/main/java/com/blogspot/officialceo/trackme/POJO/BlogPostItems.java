package com.blogspot.officialceo.trackme.POJO;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

public class BlogPostItems {

    public String user_id, image_uri, desc, image_thumb;
    public Date timeStamp;

    public BlogPostItems(String user_id, String image_uri, String desc, String image_thumb, Date timeStamp){

        this.user_id = user_id;
        this.image_uri = image_uri;
        this.desc = desc;
        this.image_thumb = image_thumb;
        this.timeStamp = timeStamp;

    }

    public BlogPostItems(){

    }

    public Date getTimeStamp(){
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp){
        this.timeStamp = timeStamp;
    }

    public String getUser_id(){
        return user_id;
    }

    public void setUser_id(String user_id){
        this.user_id = user_id;
    }

    public String getImage_uri(){
        return image_uri;
    }

    public void setImage_uri(String image_uri){
        this.image_uri = image_uri;
    }

    public String getDesc(){
        return desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public String getImage_thumb(){
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb){
        this.image_thumb = image_thumb;
    }


}
