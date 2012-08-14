package org.plista.kornakapi.core.config;

public class FactorizationbasedRecommenderConfig {

  private String name;
  private boolean usesImplicitFeedback;
  private int numberOfFeatures;
  private int numberOfIterations;
  private double lambda;
  private double alpha;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isUsesImplicitFeedback() {
    return usesImplicitFeedback;
  }

  public void setUsesImplicitFeedback(boolean usesImplicitFeedback) {
    this.usesImplicitFeedback = usesImplicitFeedback;
  }

  public int getNumberOfFeatures() {
    return numberOfFeatures;
  }

  public void setNumberOfFeatures(int numberOfFeatures) {
    this.numberOfFeatures = numberOfFeatures;
  }

  public int getNumberOfIterations() {
    return numberOfIterations;
  }

  public void setNumberOfIterations(int numberOfIterations) {
    this.numberOfIterations = numberOfIterations;
  }

  public double getLambda() {
    return lambda;
  }

  public void setLambda(double lambda) {
    this.lambda = lambda;
  }

  public double getAlpha() {
    return alpha;
  }

  public void setAlpha(double alpha) {
    this.alpha = alpha;
  }
}
