/*  Copyright (C) 2019  Hannaneh Najdataei,
 * 			Vincenzo Gulisano,
 * 			Marina Papatriantafilou,
 * 			Philippas Tsigas
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Contact:
 *  	Hannaneh Najdataei, hannajd@chalmers.se
 *  	Vincenzo Gulisano vincenzo.gulisano@chalmers.se
 *
 */

package examples.financialMarket;

import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import common.ExecutionConfiguration;
import common.SourceConfig;
import common.tuple.Tuple;
import common.util.ThreadUtilizationLogger;
import controller.ElasticityControl;
import controller.JoinModelController;
import streaming.api.JoinOperator;
import streaming.api.Operator;
import streaming.api.OutputThread;

public class FinancialMarketTest {

	static int processingThreads = 1, duration;
	static int threadCapacity = Runtime.getRuntime().availableProcessors();
	static int buckets = 1000;
	static int maxSGLevels = 4;
	static long event_dt = 1000;
	static long ws;
	static String inputRatePath = "";
	static String rInPath = "", sInPath = "", avgPriceFile = "";
	static String directory = "results/financial";
	static boolean flowControlFlag = false, controllerFlag = false;
	static double alpha, beta, sigma;

	private static Options buildOptions() {
		Options options = new Options();
		options.addOption("p", "processing-threads", true, "Initial number of worker processing threads");
		options.addOption("ws", "windowSize", true, "Window size (in milliseconds)");
		options.addOption("t", "duration", true, "The duration of the experiment (in milliseconds)");
		options.addOption("h", "help", false, "Prints this help message");
		options.addOption("b", "buckets", true, "number of buckets");
		options.addOption("tc", "threadcapacity", true,
				"Maximum number of threads. Default is what the JVM identifies as logical number of threads.");
		options.addOption("r", "rate", true, "The file showing changes in the input rate");
		options.addOption("flow", false, "Using adaptive flow control");
		options.addOption("o", "output", true, "Path to store the results and statistics");
		options.addOption("control", false, "Using model-based controller.");
		options.addOption("alpha", true, "The value for alpha.");
		options.addOption("beta", true, "The value for alpha.");
		options.addOption("sigma", true, "The value for alpha.");
		options.addOption("avg", true, "The average values for data.");
		options.addOption("rin", true, "The input data for r.");
		options.addOption("sin", true, "The input data for s.");

		return options;
	}

	private static void updateParams(CommandLine cl, Options opts) throws ParseException {
		for (Iterator<Option> iter = cl.iterator(); iter.hasNext();) {
			Option opt = iter.next();

			switch (opt.getOpt()) {
			case "p":
				processingThreads = Integer.valueOf(opt.getValue());
				System.out.println("processingThreads: " + processingThreads);
				break;
			case "ws":
				ws = Long.valueOf(opt.getValue());
				System.out.println("winSize: " + ws);
				break;
			case "t":
				duration = Integer.valueOf(opt.getValue());
				break;
			case "b":
				buckets = Integer.valueOf(opt.getValue());
				break;
			case "tc":
				threadCapacity = Integer.valueOf(opt.getValue());
				System.out.println("threadCapacity: " + threadCapacity);
				break;
			case "r":
				inputRatePath = opt.getValue();
				break;
			case "avg":
				avgPriceFile = opt.getValue();
				break;
			case "rin":
				rInPath = opt.getValue();
				break;
			case "sin":
				sInPath = opt.getValue();
				break;
			case "flow":
				flowControlFlag = true;
				break;
			case "o":
				directory = opt.getValue();
				break;
			case "control":
				controllerFlag = true;
				break;
			case "alpha":
				alpha = Double.valueOf(opt.getValue());
				break;
			case "beta":
				beta = Double.valueOf(opt.getValue());
				break;
			case "sigma":
				sigma = Double.valueOf(opt.getValue());
				break;
			case "h":
				help(opts);
				System.exit(1);
			default:
				help(opts);
				throw new ParseException(opt.getOpt());
			}
		}
	}

	private static void help(Options opts) {
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp("Join test", opts);
	}

	public static void main(String[] args) throws Exception {
		CommandLineParser parser = new DefaultParser();
		Options options = buildOptions();
		CommandLine cl = null;

		try {
			cl = parser.parse(options, args);
		} catch (ParseException e1) {
			help(options);
			e1.printStackTrace();
		}
		updateParams(cl, options);

		if (controllerFlag && (alpha == 0 || beta == 0 || sigma == 0)) {
			System.out.println(
					"To activate the model based controller, you need to input the values of alpha, beta and sigma.");
			return;
		}

		ExecutionConfiguration exeConfig = new ExecutionConfiguration(threadCapacity, 2, 1, buckets, maxSGLevels,
				flowControlFlag, controllerFlag, directory);
		exeConfig.setParallelism(processingThreads);
		exeConfig.setMapping();

		SourceConfig srcConfig = new SourceConfig();
		srcConfig.setInputRatesPath(inputRatePath);
		srcConfig.setRFilePath(rInPath);
		srcConfig.setSFilePath(sInPath);
		srcConfig.setStatsDir(directory);
		srcConfig.setDuration(duration);

		JoinModelController controlObj = new JoinModelController(exeConfig, alpha, sigma, beta, ws, event_dt);
		Operator<Tuple, ?, ElasticityControl> operator = new JoinOperator<>(TradeFunction.class,
				new TradeFunction<>(buckets, ws, avgPriceFile), exeConfig, controlObj);

		ThreadUtilizationLogger logger = new ThreadUtilizationLogger(exeConfig);
		Thread optThread = new Thread(operator);
		Thread outThread = new Thread(new OutputThread(operator.getESGout(), exeConfig.getStatsDir(), controlObj));
		Thread injectorR = new Thread(new RInjector(0, operator.getESGin(), srcConfig, controlObj, event_dt));
		Thread injectorS = new Thread(new SInjector(1, operator.getESGin(), srcConfig, controlObj, event_dt));

		long startTime = System.nanoTime();
		
		outThread.setName("output");
		injectorR.setName("rInjector");
		injectorS.setName("sInjector");

		outThread.start();
		optThread.start();
		injectorR.start();
		injectorS.start();
		logger.enable();

		try {
			injectorR.join();
			injectorS.join();
			optThread.join();
			logger.disable();
			outThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long endTime = System.nanoTime();
		System.out.println("Done in " + (endTime - startTime) / 1000000);

	}

}
