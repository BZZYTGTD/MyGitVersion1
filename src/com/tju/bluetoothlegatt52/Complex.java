package com.tju.bluetoothlegatt52;

public final class Complex implements Comparable<Object> {
  /**
   * <code>re</code> Complex - the real part
   */
  private final double re;
  /**
   * <code>im</code> Complex - the imaginary part
   */
  private final double im;
  /**
   * create a new object with the given real and imaginary parts
   * 
   * @param real
   * @param imag
   */
  public Complex(final double real, final double imag) {
    re = real;
    im = imag;
  }
  /**
   * return a string representation of the invoking object
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return re + " + " + im + "i";
  }
  /**
   * return abs/modulus/magnitude
   * 
   * @return abs/modulus/magnitude
   */
  public double abs() {
    return Math.sqrt(re * re + im * im);
  }
  /**
   * return abs/modulus/magnitude squared
   * 
   * @return abs/modulus/magnitude squared
   */
  public double scale() {
    return re * re + im * im;
  }
  /**
   * return angle/phase/argument
   * 
   * @return angle/phase/argument
   */
  public double phase() {
    return Math.atan2(im, re);
  }
  /**
   * return a new object whose value is (this + b)
   * 
   * @param b
   * @return a new object whose value is (this + b)
   */
  public Complex plus(final Complex b) {
    final Complex a = this; // invoking object
    final double real = a.re + b.re;
    final double imag = a.im + b.im;
    final Complex sum = new Complex(real, imag);
    return sum;
  }
  /**
   * return a new object whose value is (this - b)
   * 
   * @param b
   * @return a new object whose value is (this - b)
   */
  public Complex minus(final Complex b) {
    final Complex a = this;
    final double real = a.re - b.re;
    final double imag = a.im - b.im;
    final Complex diff = new Complex(real, imag);
    return diff;
  }
  /**
   * return a new object whose value is (this * b)
   * 
   * @param b
   * @return a new object whose value is (this * b)
   */
  public Complex times(final Complex b) {
    final Complex a = this;
    final double real = a.re * b.re - a.im * b.im;
    final double imag = a.re * b.im + a.im * b.re;
    final Complex prod = new Complex(real, imag);
    return prod;
  }
  /**
   * return a new object whose value is (this * b)
   * 
   * @param b
   * @return a new object whose value is (this * b)
   */
  public Complex times(final double b) {
    return new Complex(re * b, im * b);
  }
  /**
   * return a new object whose value is the conjugate of this
   * 
   * @return a new object whose value is the conjugate of this
   */
  public Complex conjugate() {
    return new Complex(re, -im);
  }
  /**
   * return a new object whose value is the reciprocal of this
   * 
   * @return a new object whose value is the reciprocal of this
   */
  public Complex reciprocal() {
    final double scale = scale();
    return new Complex(re / scale, -im / scale);
  }
  /**
   * return a / b
   * 
   * @param b
   * @return a / b
   */
  public Complex divides(final Complex b) {
    final Complex a = this;
    return a.times(b.reciprocal());
  }
  /**
   * a static version of plus
   * 
   * @param a
   * @param b
   * @return a+b
   */
  public static Complex plus(final Complex a, final Complex b) {
    final double real = a.re + b.re;
    final double imag = a.im + b.im;
    final Complex sum = new Complex(real, imag);
    return sum;
  }
  /**
   * return the real part
   * 
   * @return the real part
   */
  public double real() {
    return re;
  }
  /**
   * return the imaginary part
   * 
   * @return the imaginary part
   */
  public double imaginary() {
    return im;
  }
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(final Object arg0) {
    if (arg0 instanceof Complex) {
      final Complex c = (Complex)arg0;
      if (c.re == re && c.im == im) {
        return 0;
      }
      if (scale() - c.scale() == 0.0) {
        return (int)(phase() - c.phase());
      }
      return (int)(scale() - c.scale());
    }
    throw new IllegalArgumentException("argument not a Complex");
  }
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object arg0) {
    return compareTo(arg0) == 0;
  }
}