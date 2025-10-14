public class GreatestCommonDivisor {
    public static void main(String[] args) {
       System.out.println(greatestCommonDivisor(320,8)); // 8
       System.out.println(greatestCommonDivisor(321,7)); // 1
       System.out.println(greatestCommonDivisor(322,6)); // 2

    }
    public static int greatestCommonDivisor(int num1, int num2){
        if(num2 == 0){
            return num1;
        }
        return greatestCommonDivisor(num2, num1 % num2);
    }
}
