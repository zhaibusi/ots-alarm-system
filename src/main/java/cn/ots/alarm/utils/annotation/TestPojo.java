package cn.ots.alarm.utils.annotation;

public class TestPojo {

    @ValidBase(name = "用户姓名", maxLength = 3, minLength = 1, valid = {AnnotationConstants.NOT_BLANK, AnnotationConstants.NOT_SPECIAL_CHAR})
    private String userName;

    @ValidSize(name = "用户年龄", min = 10, max = 30)
    @ValidBase()
    private int age;

    @ValidBase(name = "用户卡券信息", valid = {AnnotationConstants.NOT_SPECIAL_CHAR})
    private String cardInfo;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(String cardInfo) {
        this.cardInfo = cardInfo;
    }

    public static void main(String[] args) {
        TestPojo pojo = new TestPojo();
        pojo.setUserName("李%");
        pojo.setAge(233);
        pojo.setCardInfo("%^UGJHJH*TGKJH*&T");
        try {
            AnnotationUtils.valid(pojo);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
