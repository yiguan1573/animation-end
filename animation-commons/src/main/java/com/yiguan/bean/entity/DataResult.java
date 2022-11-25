package com.yiguan.bean.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yiguan.bean.enums.ResponseCode;

import java.io.Serializable;

/**
 * @Author: lw
 * @CreateTime: 2022-10-06  15:35
 * @Description: TODO
 * @Version: 1.0
 */
//保证序列化json的时候,如果是null的对象,key也会消失
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DataResult<T> implements Serializable {
    private int status;
    private String msg;
    private T data;
    private MyPage page;

    public MyPage getPage() {
        return page;
    }

    public void setPage(MyPage page) {
        this.page = page;
    }

    public DataResult() {
    }

    public DataResult(int status){
        this.status = status;
    }

    public DataResult(int status,String msg){
        this.status = status;
        this.msg = msg;
    }

    public DataResult(int status,T data){
        this.status = status;
        this.data = data;
    }

    public DataResult(int status,T data,MyPage page){
        this.status = status;
        this.data = data;
        this.page = page;
    }

    public DataResult(int status,String msg,MyPage page){
        this.status = status;
        this.msg = msg;
        this.page = page;
    }

    public DataResult(int status,String msg,T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public DataResult(int status, String msg, T data, MyPage page) {
        this.status = status;
        this.msg = msg;
        this.data = data;
        this.page = page;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    /*判断当前状态是否成功.成功返回true.失败返回false
     */
    @JsonIgnore
    //使isSuccess返回值不被序列化,因为该方法不需要被转化成json对象
    public Boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    //返回成功的ServerResponse对象.
    public static<T> DataResult<T> createBySuccess(){
        return new DataResult<>(ResponseCode.SUCCESS.getCode());
    }
    public static<T> DataResult<T> createBySuccessMassage(String msg){
        return new DataResult<>(ResponseCode.SUCCESS.getCode(),msg);
    }
    public static<T> DataResult<T> createBySuccess(T data){
        return new DataResult<>(ResponseCode.SUCCESS.getCode(),data);
    }
    public static<T> DataResult<T> createBySuccess(T data,MyPage page){
        return new DataResult<>(ResponseCode.SUCCESS.getCode(),data,page);
    }
    public static<T> DataResult<T> createBySuccess(String msg,T data){
        return new DataResult<>(ResponseCode.SUCCESS.getCode(),msg,data);
    }
    public static<T> DataResult<T> createBySuccess(String msg,T data,MyPage page){
        return new DataResult<>(ResponseCode.SUCCESS.getCode(),msg,data,page);
    }

    //返回失败的ServerResponse对象.
    public static<T> DataResult<T> createByError(){
        return new DataResult<>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }
    public static<T> DataResult<T> createByError(String msg){
        return new DataResult<>(ResponseCode.ERROR.getCode(),msg);
    }
    public static<T> DataResult<T> createByError(int status,String msg){
        return new DataResult<>(status,msg);
    }

    public static<T> DataResult<T> createByError(int status,String msg,MyPage page){
        return new DataResult<>(status,msg,page);
    }
}
