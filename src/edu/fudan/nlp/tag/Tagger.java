package edu.fudan.nlp.tag;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import ming.ner.WriteWrong;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import edu.fudan.data.reader.SequenceReader;
import edu.fudan.ml.classifier.linear.Linear;
import edu.fudan.ml.classifier.linear.ModelAnalysis;
import edu.fudan.ml.classifier.linear.OnlineTrainer;
import edu.fudan.ml.classifier.linear.inf.Inferencer;
import edu.fudan.ml.classifier.linear.update.Update;
import edu.fudan.ml.classifier.struct.inf.LinearViterbi;
import edu.fudan.ml.classifier.struct.inf.Viterbi;
import edu.fudan.ml.classifier.struct.update.LinearViterbiPAUpdate;
import edu.fudan.ml.classifier.struct.update.WeightUpdate;
import edu.fudan.ml.loss.Loss;
import edu.fudan.ml.loss.struct.HammingLoss;
import edu.fudan.ml.types.AlphabetFactory;
import edu.fudan.ml.types.FeatureAlphabet;
import edu.fudan.ml.types.Instance;
import edu.fudan.ml.types.InstanceSet;
import edu.fudan.ml.types.LabelAlphabet;
import edu.fudan.nlp.cn.tag.Format.SimpleFormatter;
import edu.fudan.nlp.pipe.Pipe;
import edu.fudan.nlp.pipe.SeriesPipes;
import edu.fudan.nlp.pipe.Target2Label;
import edu.fudan.nlp.pipe.seq.Sequence2FeatureSequence;
import edu.fudan.nlp.pipe.seq.SplitDataAndTarget;
import edu.fudan.nlp.pipe.seq.templet.CharRangeTemplet;
import edu.fudan.nlp.pipe.seq.templet.TempletGroup;

/**
 * 序列标注器训练和测试程序
 * 
 * @author xpqiu
 * 
 */
public class Tagger {

	Linear cl;
	String train;
	String testfile = null;
	String output = null;
	String templateFile;
	private String model;
	private int iterNum;
	private float c;
	private boolean useLoss = true;
	private String delimiter = "\\s+|\\t+";
	private boolean interim = false;
	private AlphabetFactory factory;
	private Pipe featurePipe;
	private TempletGroup templets;
	private String newmodel;

	public Tagger() {
	}

	public void setFile(String templateFile, String train, String model) {
		this.templateFile = templateFile;
		this.train = train;
		this.model = model;
	}

	/**
	 * 序列标注训练和测试主程序
	 * 训练： java -classpath fudannlp.jar edu.fudan.nlp.tag.Tagger -train template train model 
	 * 测试： java -classpath fudannlp.jar edu.fudan.nlp.tag.Tagger model test [result]
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options opt = new Options();

		opt.addOption("h", false, "Print help for this application");
		opt.addOption("iter", true, "iterative num, default 50");
		opt.addOption("c", true, "parameters C in PA algorithm, default 0.8");
		opt.addOption("train", false,
				"switch to training mode(Default: test model");
		opt.addOption("retrain", false,
				"switch to retraining mode(Default: test model");
		opt.addOption("margin", false, "use hamming loss as margin threshold");
		opt.addOption("interim", false, "save interim model file");

		BasicParser parser = new BasicParser();
		CommandLine cl;
		try {
			cl = parser.parse(opt, args);
		} catch (Exception e) {
			System.err.println("Parameters format error");
			return;
		}

		if (args.length == 0 || cl.hasOption('h')) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp(
					"Tagger:\n"
							+ "tagger [option] -train templet_file train_file model_file [test_file];\n"
							+ "tagger [option] -retrain train_file model_file newmodel_file [test_file];\n"
							+ "tagger [option] model_file test_file output_file\n",
							opt);
			return;
		}
		Tagger tagger = new Tagger();
		tagger.iterNum = Integer.parseInt(cl.getOptionValue("iter", "50"));
		tagger.c = Float.parseFloat(cl.getOptionValue("c", "0.8"));
		tagger.useLoss = cl.hasOption("margin");
		tagger.interim = cl.hasOption("interim");

		String[] arg = cl.getArgs();
		if (cl.hasOption("train") && arg.length == 3) {
			tagger.templateFile = arg[0];
			tagger.train = arg[1];
			tagger.model = arg[2];
			System.out.println("Training model ...");
			tagger.train();
		} else if (cl.hasOption("train") && arg.length == 4) {
			tagger.templateFile = arg[0];
			tagger.train = arg[1];
			tagger.model = arg[2];
			tagger.testfile = arg[3];
			System.out.println("Training model ...");
			tagger.train();
		} else if (cl.hasOption("train") && arg.length == 5) {
			tagger.templateFile = arg[0];
			tagger.train = arg[1];
			tagger.model = arg[2];
			tagger.testfile = arg[3];
			System.out.println("Training model ...");
			tagger.train();
			System.gc();
			tagger.output = arg[4];
			tagger.test();
		} else if (cl.hasOption("retrain") && arg.length == 3) {
			tagger.train = arg[0];
			tagger.model = arg[1];
			tagger.newmodel = arg[2];
			System.out.println("Re-Training model ...");
			tagger.train(true);
		} else if (cl.hasOption("retrain") && arg.length == 4) {
			tagger.train = arg[0];
			tagger.model = arg[1];
			tagger.newmodel = arg[2];
			tagger.testfile = arg[3];
			System.out.println("Re-Training model ...");
			tagger.train(true);
		} else if (cl.hasOption("retrain") && arg.length == 5) {
			tagger.train = arg[0];
			tagger.model = arg[1];
			tagger.newmodel = arg[2];
			tagger.testfile = arg[3];
			System.out.println("Re-Training model ...");
			tagger.train(true);
			System.gc();
			tagger.output = arg[4];
			tagger.test();
		} else if (arg.length == 3) {
			tagger.model = arg[0];
			tagger.testfile = arg[1];
			tagger.output = arg[2];
			tagger.test();
		} else if (arg.length == 2) {
			tagger.model = arg[0];
			tagger.testfile = arg[1];
			tagger.test();
		} else {
			System.err.println("paramenters format error!");
			System.err.println("Print option \"-h\" for help.");
			return;
		}

		System.gc();

	}



	/**
	 * @throws Exception
	 */
	public Pipe createProcessor(boolean flag) throws Exception {
		if (!flag) {
			templets = new TempletGroup();
			templets.load(templateFile);

			// Dictionary d = new Dictionary();
			// d.loadWithWeigth("D:/xpqiu/项目/自选/CLP2010/CWS/av-b-lut.txt",
			// "AV");
			// templets.add(new DictionaryTemplet(d, gid++, -1, 0));
			// templets.add(new DictionaryTemplet(d, gid++, 0, 1));
			// templets.add(new DictionaryTemplet(d, gid++, -1,0, 1));
			// templets.add(new DictionaryTemplet(d, gid++, -2,-1,0, 1));

			templets.add(new CharRangeTemplet(templets.gid++,new
					int[]{0}));
			templets.add(new CharRangeTemplet(templets.gid++,new
					int[]{-1,0}));
			templets.add(new CharRangeTemplet(templets.gid++,new
					int[]{0,1}));
		}

		if (cl != null)
			factory = cl.getAlphabetFactory();
		else
			factory = AlphabetFactory.buildFactory();

		/**
		 * 标签转为0、1、2、...
		 */
		LabelAlphabet labels = factory.DefaultLabelAlphabet();

		// 将样本通过Pipe抽取特征
		FeatureAlphabet features = factory.DefaultFeatureAlphabet();
		featurePipe = new Sequence2FeatureSequence(templets, features, labels);

		Pipe pipe = new SeriesPipes(new Pipe[] { new Target2Label(labels), featurePipe });
		return pipe;
	}

	public void train() throws Exception{
		train(false);
	}

	/**
	 * 训练
	 * @param b 是否增量训练
	 * @throws Exception
	 */
	public void train(boolean b) throws Exception {

		System.out.print("Loading training data ...");
		long beginTime = System.currentTimeMillis();

		if(b)
			loadFrom(model);
		Pipe pipe = createProcessor(b);

		InstanceSet trainSet = new InstanceSet(pipe, factory);

		LabelAlphabet labels = factory.DefaultLabelAlphabet();
		FeatureAlphabet features = factory.DefaultFeatureAlphabet();

		if(b){
			features.setStopIncrement(false);
			labels.setStopIncrement(false);
		}

		// 训练集
		trainSet.loadThruStagePipes(new SequenceReader(train, true));

		long endTime = System.currentTimeMillis();
		System.out.println(" done!");
		System.out
		.println("Time escape: " + (endTime - beginTime) / 1000 + "s");
		System.out.println();

		// 输出
		System.out.println("Training Number: " + trainSet.size());

		System.out.println("Label Number: " + labels.size()); // 标签个数
		System.out.println("Feature Number: " + features.size()); // 特征个数

		// 冻结特征集
		features.setStopIncrement(true);
		labels.setStopIncrement(true);

		InstanceSet testSet = null;
		// /////////////////
		if (testfile != null) {

			boolean hasTarget;
			if (false) {// 如果test data没有标注
				hasTarget = false;
				pipe = featurePipe;
			} else {
				hasTarget = true;
			}

			// 测试集
			testSet = new InstanceSet(pipe);

			testSet.loadThruStagePipes(new SequenceReader(testfile, hasTarget, "utf8"));
			System.out.println("Test Number: " + testSet.size()); // 样本个数
		}


		/**
		 * 
		 * 更新参数的准则
		 */
		Update update;
		// viterbi解码
		Inferencer inference;
		boolean standard = true;
		HammingLoss loss = new HammingLoss();
		if (standard) {
			inference = new LinearViterbi(templets, labels.size());
			update = new LinearViterbiPAUpdate((LinearViterbi) inference, loss);
		} else {
			inference = new Viterbi(templets, labels.size());
			update = new WeightUpdate(templets, labels.size(), true);
		}

		OnlineTrainer trainer;

		if(b){
			trainer = new OnlineTrainer(cl, update, loss, features.size(),iterNum, c);
		}else{

			trainer = new OnlineTrainer(inference, update, loss,
					features.size(), iterNum, c);
		}
		trainer.innerOptimized = false;
		trainer.finalOptimized = false;

		cl = trainer.train(trainSet, testSet);

		ModelAnalysis ma = new ModelAnalysis(cl);
		ma.removeZero();

		if(b)
			saveTo(newmodel);
		else
			saveTo(model);

	}



	private void test() throws Exception {
		if (cl == null)
			loadFrom(model);

		long starttime = System.currentTimeMillis();
		// 将样本通过Pipe抽取特征
		Pipe pipe = createProcessor(true);

		// 测试集
		InstanceSet testSet = new InstanceSet(pipe);

		testSet.loadThruStagePipes(new SequenceReader(testfile,true,"utf8"));
		System.out.println("Test Number: " + testSet.size()); // 样本个数

		long featuretime = System.currentTimeMillis();

		boolean acc = true;
		float error = 0;
		int senError = 0;
		int len = 0;
		boolean hasENG = false;
		int ENG_all = 0, ENG_right = 0;
		Loss loss = new HammingLoss();

		String[][] labelsSet = new String[testSet.size()][];
		String[][] targetSet = new String[testSet.size()][];
		LabelAlphabet labels = cl.getAlphabetFactory().DefaultLabelAlphabet();
		for (int i = 0; i < testSet.size(); i++) {
			Instance carrier = testSet.get(i);
			int[] pred = (int[]) cl.predict(carrier).getPredAt(0);

			if (acc) {
				len += pred.length;
				float e = loss.calc(carrier.getTarget(), pred);
				error += e;
				if(e != 0) {
                    senError++;
                    WriteWrong.getInstance().wrintInfo((String[][])(carrier.getSource()));
                }
				//测试中英混杂语料
				if(hasENG) {
					String[][] origin = (String[][])carrier.getSource();
					int[] target = (int[])carrier.getTarget();
					for(int j = 0; j < target.length; j++) {
						if(origin[j][0].contains("ENG")) {
							ENG_all++;
							if(target[j] == pred[j])
								ENG_right++;
						}
					}
				}
			}
			labelsSet[i] = labels.lookupString(pred);
			targetSet[i] = labels.lookupString((int[])carrier.getTarget());
		}

		long endtime = System.currentTimeMillis();
		System.out.println("totaltime\t" + (endtime - starttime) / 1000.0);
		System.out.println("feature\t" + (featuretime - starttime) / 1000.0);
		System.out.println("predict\t" + (endtime - featuretime) / 1000.0);

		if (acc) {
			System.out.println("Test Accuracy:\t" + (1 - error / len));
			System.out.println("Sentence Accuracy:\t" + ((double)(testSet.size() - senError) / testSet.size()));
			if(hasENG)
				System.out.println("ENG Accuracy:\t" + ((double)ENG_right / ENG_all));
		}

		if (output != null) {
			BufferedWriter prn = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output), "utf8"));
			String s = SimpleFormatter.format(testSet, labelsSet, targetSet);
			prn.write(s.trim());
			prn.close();
		}
		System.out.println("Done");
	}

	protected void saveTo(String modelfile) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(
				new BufferedOutputStream(new GZIPOutputStream(
						new FileOutputStream(modelfile))));
		out.writeObject(templets);
		out.writeObject(cl);
		out.close();
	}

	protected void loadFrom(String modelfile) throws IOException,
	ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
				new GZIPInputStream(new FileInputStream(modelfile))));
		templets = (TempletGroup) in.readObject();
		cl = (Linear) in.readObject();
		in.close();
	}
}