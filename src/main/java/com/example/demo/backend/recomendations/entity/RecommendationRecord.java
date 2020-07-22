package com.example.demo.backend.recomendations.entity;

public class RecommendationRecord implements Comparable<RecommendationRecord> {
	private double score;
	private String attributeName;

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public int compareTo(RecommendationRecord other) {
		if (this.score > other.score)
			return -1;
		if (this.score < other.score)
			return 1;
		return 0;
	}

	public String toString() {
		return attributeName + " : " + score;
	}
}
