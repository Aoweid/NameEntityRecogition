package addressextraction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThread {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		List<String> input =new ArrayList<String>();
		input.add("abc");
		input.add("abc");
		input.add("abc");
		new MultiThread().processStrings(input);
	}
	
	public List<String> processStrings(List<String> input)
	        throws InterruptedException, ExecutionException {

	    int threads = Runtime.getRuntime().availableProcessors();
	    System.out.println(threads);
	    ExecutorService service = Executors.newFixedThreadPool(threads);

	    List<Future<String>> futures = new ArrayList<Future<String>>();
	    for (final String in : input) {
	        Callable<String> callable = new Callable<String>() {
	            public String call() throws Exception {
	                String opt = in+"opt";
	                System.out.println(opt);
	                return opt;
	            }
	        };
	        futures.add(service.submit(callable));
	    }

	    service.shutdown();

	    List<String> outputs = new ArrayList<String>();
	    for (Future<String> future : futures) {
	        outputs.add(future.get());
	    }
	    return outputs;
	}
}
