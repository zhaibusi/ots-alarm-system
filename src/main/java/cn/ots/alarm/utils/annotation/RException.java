package cn.ots.alarm.utils.annotation;

public class RException extends RuntimeException {
    private int code;

    /*无参构造函数*/
    public RException(){
        super();
    }

    //用详细信息指定一个异常
    public RException(int code,String message){
        super(message);
    }

    //用指定的详细信息和原因构造一个新的异常
    public RException(String message, Throwable cause){
        super(message,cause);
    }

    //用指定原因构造一个新的异常
    public RException(Throwable cause) {
        super(cause);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
