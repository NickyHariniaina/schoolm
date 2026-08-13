package hei.student.schoolm.util;

public record Fraction(int numerator, int denominator) {
  public Fraction {
    if (denominator == 0) {
      throw new IllegalArgumentException("Denominator must not be zero");
    }
  }

  public Fraction add(Fraction fraction) {
    int num = numerator * fraction.denominator + fraction.numerator * denominator;
    int den = denominator * fraction.denominator;
    return simplify(num, den);
  }

  public double toDouble() {
    return numerator / (double) denominator;
  }

  public boolean isOne() {
    Fraction simplified = simplify(numerator, denominator);
    return simplified.numerator() == simplified.denominator();
  }

  private static Fraction simplify(int num, int den) {
    int divisor = gcd(Math.abs(num), Math.abs(den));
    return new Fraction(num / divisor, den / divisor);
  }

  private static int gcd(int a, int b) {
    return b == 0 ? a : gcd(b, a % b);
  }
}
