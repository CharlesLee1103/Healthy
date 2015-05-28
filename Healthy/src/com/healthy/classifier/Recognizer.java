package com.healthy.classifier;

import java.util.ArrayList;
import java.util.List;

import com.healthy.logic.BackgroundService;
import com.healthy.logic.model.SensorInDb;

/**
 * ɾ���Զ����ݸĶ���
 * @author Administrator
 *
 */
public class Recognizer {
	public static int recResult = 0;
	public static int strideCount = 0;

	public static int recognize(SensorInDb data,
			BackgroundService backgroundService) {
		Lift lift = new Lift();
		if (lift.isLift(data.xAcc, data.yAcc, data.zAcc)) {
			strideCount = 0;
			return ActivityCategories.Lift;
		}

		if (nonPeriodic(data)) {
//			recResult = decisionTree(data.MagxData, data.MagyData,
//					data.MagzData);
			recResult = ActivityCategories.Stationary;
			strideCount = 0;
			return recResult;
		} else {

			List<Double> features = Features.getFeatures(data);

			List<Double> feature = new ArrayList<Double>();
			// ����õ�������ֵ��ȡ��һ��
			if (features.size() > 0) {
				feature = features.subList(0, Configure.FeatureNum);
				recResult = getResult(feature, backgroundService);
				strideCount = Features.getstrides().size();
				return recResult;
			}
			// ���û�õ�����ֵ���������������ڻ��ʶ��
			else {
//				recResult = decisionTree(data.MagxData, data.MagyData,
//						data.MagzData);
				recResult = ActivityCategories.Stationary;
				strideCount = 0;
				return recResult;
			}

		}
	}

	/**
	 * �����ٶ�����Ͷ�䵽��ֱ�����ж��Ƿ�Ϊ�������Ի��
	 * 
	 * @param data
	 * @return
	 */
	private static boolean nonPeriodic(SensorInDb data) {
		// �ƶ�ƽ���˲�֮��
		List<Double> Sx = new ArrayList<Double>();
		List<Double> Sy = new ArrayList<Double>();
		List<Double> Sz = new ArrayList<Double>();
		// Ͷ��֮��
		List<Double> projects = new ArrayList<Double>();
		// ��׼��
		double sd = 0;

		Features.getSmooth(data.xAcc, data.yAcc, data.zAcc, Sx, Sy, Sz);
		Features.project(Sx, Sy, Sz, projects);
		sd = Compute.getStandardDeviation(projects);

		if (sd > Configure.SD_ACC) {
			return false;
		} else
			return true;
		/*
		 * Features.getFeatures(x, y, z); List<Double> projects =
		 * Features.getProjects(); if(Compute.getAverageEnergy(projects) >
		 * Configure.E){ return true; } else return false;
		 */
	}

//	/**
//	 * �ж��Ǿ�ֹ���Ƿ��� ����mag��ģ֮��ķ�����������ֵ�����ж�Ϊ��������
//	 * 
//	 * @param x
//	 * @param y
//	 * @param z
//	 * @return
//	 */
//	private static int decisionTree(List<Double> x, List<Double> y,
//			List<Double> z) {
//		// ��ģ
//		List<Double> mods = new ArrayList<Double>();
//		for (int i = 0; i < x.size(); ++i) {
//			mods.add(Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(y.get(i), 2)
//					+ Math.pow(z.get(i), 2)));
//		}
//		// �󷽲�
//		double sd = Compute.getStandardDeviation(mods);
//
//		if (sd > Configure.SD_MAG) {
//			return ActivityCategories.Escalator;
//		} else
//			return ActivityCategories.Stationary;
//	}

	/**
	 * ʹ�÷������õ����
	 * 
	 * @param feature
	 * @param backgroundService
	 * @return
	 */
	private static int getResult(List<Double> feature,
			BackgroundService backgroundService) {
		String result = null;

		PNNClassifier pnn = new PNNClassifier();
		result = pnn.predict(feature, backgroundService);

		if (result.equals("����")) {
			return ActivityCategories.Walking;
		} else if (result.equals("�ܲ�")) {
			return ActivityCategories.Jogging;
		} else if (result.equals("��¥��")) {
			return ActivityCategories.AscendingStairs;
		} else if (result.equals("��¥��")) {
			return ActivityCategories.DescendingStairs;
		} else if (result.equals("�����г�")) {
			return ActivityCategories.Bicycling;
		} else {
			return 0;
		}
	}

}
