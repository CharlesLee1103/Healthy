package com.healthy.classifier;

/**
 * ȡ���شű�׼�ֵ��ʶ���Զ�����
 * @author Administrator
 *
 */
public class Configure {
	//ÿ��Frame�е�������
	public static final int Nfc = 3; 
	
	// ƽ���˲�
	public static final int Average_N = 3;
	
	// ��ֵ,peakƽ��ֵ�ı���
	public static final double C = 2;
	
	//����ֵ����
	public static final int FeatureNum =23;
	
	//ʶ��Ļ��
	public static final int ScenesNum = 5;
	
	//�ļ�·��
	//public static final String FilePath = BackgroundService.sdcardDir.getPath()+"//����������"+"//";
	
	//SVM��������
	public static final String SVM_MODEL = "SVM_model.txt";
	
	//��ͬ�ķ����㷨
	public static final int SVM_CLASSIFIER = 1; 
	public static final int PNN_CLASSIFIER = 2; 
	public static final int BAYES_CLASSIFIER = 3; 
	
	public static final int Classfier_type = PNN_CLASSIFIER; 
	
	//ƽ�����������������Ƿ�Ϊ�����Ի
	public static final double E = 1;
	
	//���ٶȱ�׼����ֵ�������ж��Ƿ�Ϊ�������Ի
	public static final double SD_ACC = 0.8;
	
//	//�شű�׼����ֵ�������ж��Ƿ�Ϊ�Զ�����
//	public static final double SD_MAG = 3;
	
	//��С�Ϸ�����
	public static final int MIN_WIN = 30;
	
	//ÿ�����ȡ������������
	//public static int sample_num_each_scene = 0;
	
	//ÿ�����ȡ������������,����a��w����������
	public static int num_total_train_sample = 1400;
	
	////Ͷ�䴰��
	public static final int ProjectWin = 50;
}
