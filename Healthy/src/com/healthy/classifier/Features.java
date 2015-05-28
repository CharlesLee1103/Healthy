package com.healthy.classifier;

import java.util.ArrayList;
import java.util.List;

import com.healthy.logic.model.SensorInDb;

public class Features {

	// Ͷ�䴰��
	private static int projectWin = Configure.ProjectWin;

	private static List<Double> features = new ArrayList<Double>();
	// �ƶ�ƽ���˲�֮��
	private static List<Double> Sx = new ArrayList<Double>();
	private static List<Double> Sy = new ArrayList<Double>();
	private static List<Double> Sz = new ArrayList<Double>();
	// Ͷ��֮��
	private static List<Double> projects = new ArrayList<Double>();

	// peaks
	private static List<Integer> peaks = new ArrayList<Integer>();
	// strides,2 steps= 1 stride
	private static List<Integer> strides = new ArrayList<Integer>();

	/**
	 * 
	 * @param x
	 *            x������
	 * @param y
	 *            y������
	 * @param z
	 *            z������
	 * @return һϵ������ֵ������Ϊƽ��ֵ����׼�ƽ���������ֲ������ƽ������
	 */
	public static List<Double> getFeatures(SensorInDb mydata) {
		features.clear();

		getSmooth(mydata.xAcc, mydata.yAcc, mydata.zAcc, Sx, Sy, Sz);
		project(Sx, Sy, Sz, projects);
		findPeaks(projects, peaks);
		findstrides(projects, peaks, strides);

		for (int i = 0; i < (strides.size() - 1) / Configure.Nfc; ++i) {
			// ���ٴ�����ԭʼ����
			List<Double> originalX = new ArrayList<Double>();
			List<Double> originalY = new ArrayList<Double>();
			List<Double> originalZ = new ArrayList<Double>();
			List<Double> data = new ArrayList<Double>();
			data = projects.subList(strides.get(i * Configure.Nfc),
					strides.get((i + 1) * Configure.Nfc));

			features.add(getAvePeriod(data));
			originalX = mydata.xAcc.subList(strides.get(i * Configure.Nfc),
					strides.get((i + 1) * Configure.Nfc));
			originalY = mydata.yAcc.subList(strides.get(i * Configure.Nfc),
					strides.get((i + 1) * Configure.Nfc));
			originalZ = mydata.zAcc.subList(strides.get(i * Configure.Nfc),
					strides.get((i + 1) * Configure.Nfc));
			List<Double> cor = getCorrelation(originalX, originalY, originalZ);

			features.add(cor.get(2));

			features.add(Compute.getStandardDeviation(originalX));
			features.add(Compute.getStandardDeviation(originalY));
			features.add(Compute.getStandardDeviation(originalZ));
			features.add(Compute.getAverageEnergy(originalX));
			features.add(Compute.getAverageEnergy(originalY));
			features.add(Compute.getAverageEnergy(originalZ));
			int[] binnedOrix = getBinnedDistribution(originalX);
			for (int j = 0; j < 5; ++j) {
				features.add((double) binnedOrix[j]);
			}
			int[] binnedOriy = getBinnedDistribution(originalY);
			for (int j = 0; j < 5; ++j) {
				features.add((double) binnedOriy[j]);
			}
			int[] binnedOriz = getBinnedDistribution(originalZ);
			for (int j = 0; j < 5; ++j) {
				features.add((double) binnedOriz[j]);
			}
		}
		return features;
	}

	/**
	 * �ƶ�ƽ���˲�
	 * 
	 * @param x
	 *            ԭʼ����
	 * @param y
	 * @param z
	 * @param Sx
	 *            �ƶ�ƽ���˲�֮��
	 * @param Sy
	 * @param Sz
	 */
	public static void getSmooth(List<Double> x, List<Double> y,
			List<Double> z, List<Double> Sx, List<Double> Sy, List<Double> Sz) {
		Sx.clear();
		Sy.clear();
		Sz.clear();
		// Ϊ�˲���ʧ���ݽ�ǰ��i/2��������ӽ���
		for (int i = 0; i < Configure.Average_N / 2; i++) {
			Sx.add(x.get(i));
			Sy.add(y.get(i));
			Sz.add(z.get(i));
		}
		for (int i = 0; i < x.size() - Configure.Average_N + 1; ++i) {
			double sumx = 0;
			double sumy = 0;
			double sumz = 0;
			for (int j = 0; j < Configure.Average_N; ++j) {
				sumx = sumx + x.get(i + j);
				sumy = sumy + y.get(i + j);
				sumz = sumz + z.get(i + j);
			}
			Sx.add(sumx / Configure.Average_N);
			Sy.add(sumy / Configure.Average_N);
			Sz.add(sumz / Configure.Average_N);
		}
		for (int i = x.size() - (Configure.Average_N - 1) / 2; i < x.size(); i++) {
			Sx.add(x.get(i));
			Sy.add(y.get(i));
			Sz.add(z.get(i));
		}
	}

	/**
	 * Ͷ�䵽��ֱ������������Ӱ��
	 * 
	 * @param Sx
	 * @param Sy
	 * @param Sz
	 * @param projects
	 */
	public static void project(List<Double> Sx, List<Double> Sy,
			List<Double> Sz, List<Double> projects) {
		// System.out.println("project---------->in");
		projects.clear();
		double[] g = new double[3];
		for (int i = 0; i < Sx.size() / projectWin; ++i) {
			g[0] = Compute.getAverage(Sx.subList(i * projectWin, i * projectWin
					+ projectWin));
			g[1] = Compute.getAverage(Sy.subList(i * projectWin, i * projectWin
					+ projectWin));
			g[2] = Compute.getAverage(Sz.subList(i * projectWin, i * projectWin
					+ projectWin));
			double[] g_norm = new double[3];

			double model = Math.sqrt(Math.pow(g[0], 2) + Math.pow(g[1], 2)
					+ Math.pow(g[2], 2));
			g_norm[0] = g[0] / (model);
			g_norm[1] = g[1] / (model);
			g_norm[2] = g[2] / (model);
			for (int j = 0; j < projectWin; ++j) {
				projects.add((Sx.get(i * projectWin + j) * g_norm[0]
						+ Sy.get(i * projectWin + j) * g_norm[1]
						+ Sz.get(i * projectWin + j) * g_norm[2] - model));
			}
		}
		// ʣ�಻����ʮ�����ݵ�Ͷ��
		g[0] = Compute.getAverage(Sx.subList(projects.size(), Sx.size()));
		g[1] = Compute.getAverage(Sy.subList(projects.size(), Sx.size()));
		g[2] = Compute.getAverage(Sz.subList(projects.size(), Sx.size()));
		double[] g_norm = new double[3];

		double model = Math.sqrt(Math.pow(g[0], 2) + Math.pow(g[1], 2)
				+ Math.pow(g[2], 2));
		g_norm[0] = g[0] / (model);
		g_norm[1] = g[1] / (model);
		g_norm[2] = g[2] / (model);
		int k = projects.size();
		for (int j = 0; j < Sx.size() - k; ++j) {
			projects.add((Sx.get(k + j) * g_norm[0] + Sy.get(k + j) * g_norm[1]
					+ Sz.get(k + j) * g_norm[2] - model));
		}

	}

	/**
	 * ��peak
	 * 
	 * @param projects
	 * @param peaks
	 */
	private static void findPeaks(List<Double> projects, List<Integer> peaks) {
		peaks.clear();
		// �Ҳ���
		for (int i = 1; i < projects.size() - 1; ++i) {
			if (projects.get(i) < projects.get(i - 1)
					&& projects.get(i) < projects.get(i + 1)) {
				peaks.add(i);
			}
		}
	}

	/**
	 * ��stride
	 * 
	 * @param projects
	 * @param peaks
	 * @param strides
	 */
	private static void findstrides(List<Double> projects, List<Integer> peaks,
			List<Integer> strides) {
		// System.out.println("findstrides---------->in");
		strides.clear();
		double peakAverage = 0;
		for (int i = 0; i < peaks.size(); ++i) {
			peakAverage += projects.get(peaks.get(i));
		}
		// peakƽ��ֵ
		peakAverage = peakAverage / peaks.size();
		for (int j = 0; j < peaks.size(); ++j) {
			// ���peak��ֵ������ֵ���ж��Ƿ�Ϊ��һ��stride�����ǣ�����������������ǣ��ж�����ǰһ��stride�ļ��
			if (projects.get(peaks.get(j)) < Configure.C * peakAverage) {
				if (strides.size() == 0) {
					if (localMin(peaks.get(j))) {
						strides.add(peaks.get(j));
					}
				} else {
					if (peaks.get(j) > Configure.MIN_WIN
							+ strides.get(strides.size() - 1)) {
						if (localMin(peaks.get(j))) {
							strides.add(peaks.get(j));
						}
					}
				}
			}
		}
	}

	private static double getAvePeriod(List<Double> data) {
		return data.size() / 3;
	}

	private static int[] getBinnedDistribution(List<Double> data) {
		return Compute.getBinnedDistribution(data);
	}

	private static List<Double> getCorrelation(List<Double> x, List<Double> y,
			List<Double> z) {
		return Compute.getCorrelation(x, y, z);
	}

	public static List<Double> getProjects() {
		return projects;
	}

	static boolean localMin(int peak) {
		for (int i = 1; i < Configure.MIN_WIN; ++i) {
			if ((peak + i < projects.size())
					&& projects.get(peak) > projects.get(peak + i))
				return false;
		}
		return true;
	}

	public static List<Integer> getstrides() {
		return strides;
	}
}
