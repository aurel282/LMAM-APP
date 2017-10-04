package com.example.aurel.lmam;


import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;

/**
 * Created by aurel on 08-12-16.
 */



public class message
{

    private final static String TAG = "ClassMessage";

    private Date MessageDate;
    private String MessageText;
    private String UserUid;
    private MyLatLng MessageLocation;
    private Boolean HasPicture = false;
    private String PictureName;
    private String Key;
    private Integer Availability; //Time when the message will be available in hours

    public Integer getAvailability() {
        return Availability;
    }

    public void setAvailability(Integer availability) {
        Availability = availability;
    }


    public Date getMessageDate() {
        return MessageDate;
    }

    public void setMessageDate(Date messageDate) {
        MessageDate = messageDate;
    }

    public String getMessageText() {
        return MessageText;
    }

    public void setMessageText(String messageText) {
        MessageText = messageText;
    }

    public String getUserUid() {
        return UserUid;
    }

    public void setUserUid(String userUid) {
        UserUid = userUid;
    }

    public MyLatLng getMessageLocation() {
        return MessageLocation;
    }

    public void setMessageLocation(MyLatLng messageLocation)
    {
        MessageLocation = messageLocation;
    }

    public message(Date ActDate, String ActText, String ActUser, MyLatLng ActLocation)
    {
        MessageDate = ActDate;
        MessageText = ActText;
        UserUid = ActUser;
        MessageLocation = ActLocation;
        HasPicture = false;
    }

    public message(Date ActDate, String ActText, String ActUser, MyLatLng ActLocation, String PicName)
    {
        MessageDate = ActDate;
        MessageText = ActText;
        UserUid = ActUser;
        MessageLocation = ActLocation;
        PictureName = PicName;
        HasPicture = true;
    }

    public message()
    {
        MessageLocation = new MyLatLng(0,0);
    }

    public Boolean getHasPicture() {
        return HasPicture;
    }

    public void setHasPicture(Boolean hasPicture) {
        HasPicture = hasPicture;
    }

    public String getPictureName() {
        return PictureName;
    }

    public void setPictureName(String pictureName) {
        PictureName = pictureName;
    }

    public void SendToDataBase()
    {
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String key = mDatabase.push().getKey();

        mDatabase.child("DbMessages").child(key).setValue(this);

    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public static void ModifyInList(List<message> ActList, message UpdatedMessage, String Key)
    {
        for(int i = 0; i < ActList.size(); i++)
        {
            if(ActList.get(i).getKey().equals(Key))
            {
                ActList.remove(i);
                ActList.add(i,UpdatedMessage);
                return;
            }
        }
    }

    public static void DeleteInList(List<message> ActList, String Key)
    {
        for(int i = 0; i < ActList.size(); i++)
        {
            if(ActList.get(i).getKey().equals(Key))
            {
                ActList.remove(i);
                return;
            }
        }
    }
}
